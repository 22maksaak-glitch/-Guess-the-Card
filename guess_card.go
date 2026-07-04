// guess_card.go
package main

import (
	"bufio"
	"encoding/json"
	"fmt"
	"math/rand"
	"os"
	"path/filepath"
	"strings"
	"time"
)

const (
	reset  = "\033[0m"
	red    = "\033[91m"
	green  = "\033[92m"
	yellow = "\033[93m"
	blue   = "\033[94m"
	cyan   = "\033[96m"
	magenta= "\033[95m"
	bold   = "\033[1m"
)

func colorize(text, color string) string {
	return color + text + reset
}

var suits = []string{"пики", "червы", "бубны", "трефы"}
var ranks = []string{"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"}
var suitColors = map[string]string{
	"пики": blue, "червы": red, "бубны": red, "трефы": green,
}

type Record struct {
	Record int `json:"record"`
}

type Game struct {
	mode      string
	record    int
	recordFile string
	target    [2]string // [suit, rank]
	attempts  int
	gameOver  bool
}

func NewGame(mode string) *Game {
	g := &Game{mode: mode}
	g.recordFile = filepath.Join(os.Getenv("HOME"), ".guess_card_record.json")
	g.loadRecord()
	return g
}

func (g *Game) loadRecord() {
	data, err := os.ReadFile(g.recordFile)
	if err != nil {
		g.record = -1
		return
	}
	var rec Record
	err = json.Unmarshal(data, &rec)
	if err != nil {
		g.record = -1
	} else {
		g.record = rec.Record
	}
}

func (g *Game) saveRecord(rec int) {
	data, _ := json.Marshal(Record{Record: rec})
	os.WriteFile(g.recordFile, data, 0644)
}

func (g *Game) chooseCard() {
	rand.Seed(time.Now().UnixNano())
	s := rand.Intn(len(suits))
	r := rand.Intn(len(ranks))
	g.target = [2]string{suits[s], ranks[r]}
}

func (g *Game) getSuit(name string) string {
	for _, s := range suits {
		if strings.EqualFold(s, name) {
			return s
		}
	}
	return ""
}

func (g *Game) getRank(name string) string {
	for _, r := range ranks {
		if strings.EqualFold(r, name) {
			return r
		}
	}
	return ""
}

func (g *Game) displayCard(suit, rank string) string {
	col := suitColors[suit]
	return colorize(rank+" "+suit, col)
}

func (g *Game) play() {
	fmt.Println(colorize("🃏 Добро пожаловать в 'Угадай карту'!", bold))
	fmt.Println("Компьютер загадал карту. Попробуйте угадать!")
	if g.mode == "full" {
		fmt.Println("Вам нужно угадать и масть, и достоинство.")
	} else if g.mode == "suit" {
		fmt.Println("Вам нужно угадать только масть.")
	} else {
		fmt.Println("Вам нужно угадать только достоинство.")
	}
	if g.record != -1 {
		fmt.Printf("Текущий рекорд: %d попыток.\n", g.record)
	}
	fmt.Println("Для выхода введите 'q'.\n")

	g.chooseCard()
	g.attempts = 0
	scanner := bufio.NewScanner(os.Stdin)

	for !g.gameOver {
		g.attempts++
		if g.mode == "full" {
			fmt.Print("Введите масть (пики/червы/бубны/трефы): ")
			scanner.Scan()
			suitInput := strings.TrimSpace(scanner.Text())
			if strings.ToLower(suitInput) == "q" {
				fmt.Println("Выход.")
				return
			}
			fmt.Print("Введите достоинство (2-10, J, Q, K, A): ")
			scanner.Scan()
			rankInput := strings.TrimSpace(scanner.Text())
			if strings.ToLower(rankInput) == "q" {
				fmt.Println("Выход.")
				return
			}
			suit := g.getSuit(suitInput)
			rank := g.getRank(rankInput)
			if suit == "" || rank == "" {
				fmt.Println(colorize("Неверный ввод. Попробуйте снова.", red))
				g.attempts--
				continue
			}
			if suit == g.target[0] && rank == g.target[1] {
				fmt.Println(colorize("🎉 Поздравляем! Вы угадали карту!", green))
				fmt.Printf("Загадана была: %s\n", g.displayCard(g.target[0], g.target[1]))
				g.gameOver = true
			} else {
				if suit == g.target[0] {
					fmt.Println(colorize("Масть угадана верно!", green))
				} else {
					fmt.Println(colorize("Масть не угадана.", red))
				}
				if rank == g.target[1] {
					fmt.Println(colorize("Достоинство угадано верно!", green))
				} else {
					fmt.Println(colorize("Достоинство не угадано.", red))
				}
			}
		} else if g.mode == "suit" {
			fmt.Print("Введите масть (пики/червы/бубны/трефы): ")
			scanner.Scan()
			suitInput := strings.TrimSpace(scanner.Text())
			if strings.ToLower(suitInput) == "q" {
				fmt.Println("Выход.")
				return
			}
			suit := g.getSuit(suitInput)
			if suit == "" {
				fmt.Println(colorize("Неверная масть.", red))
				g.attempts--
				continue
			}
			if suit == g.target[0] {
				fmt.Println(colorize("🎉 Поздравляем! Вы угадали масть!", green))
				fmt.Printf("Загадана была: %s\n", g.displayCard(g.target[0], g.target[1]))
				g.gameOver = true
			} else {
				fmt.Println(colorize("Неверно. Попробуйте снова.", red))
			}
		} else { // rank
			fmt.Print("Введите достоинство (2-10, J, Q, K, A): ")
			scanner.Scan()
			rankInput := strings.TrimSpace(scanner.Text())
			if strings.ToLower(rankInput) == "q" {
				fmt.Println("Выход.")
				return
			}
			rank := g.getRank(rankInput)
			if rank == "" {
				fmt.Println(colorize("Неверное достоинство.", red))
				g.attempts--
				continue
			}
			if rank == g.target[1] {
				fmt.Println(colorize("🎉 Поздравляем! Вы угадали достоинство!", green))
				fmt.Printf("Загадана была: %s\n", g.displayCard(g.target[0], g.target[1]))
				g.gameOver = true
			} else {
				fmt.Println(colorize("Неверно. Попробуйте снова.", red))
			}
		}
	}
	fmt.Printf("Количество попыток: %d\n", g.attempts)
	if g.record == -1 || g.attempts < g.record {
		g.saveRecord(g.attempts)
		fmt.Println(colorize(fmt.Sprintf("🏆 Новый рекорд: %d попыток!", g.attempts), yellow))
	}
}

func main() {
	mode := "full"
	showRecord := false
	for _, arg := range os.Args[1:] {
		switch arg {
		case "full", "suit", "rank":
			mode = arg
		case "-r", "--record":
			showRecord = true
		case "-h", "--help":
			fmt.Println("Usage: guess_card [full|suit|rank] [-r]")
			return
		}
	}
	if showRecord {
		recFile := filepath.Join(os.Getenv("HOME"), ".guess_card_record.json")
		data, err := os.ReadFile(recFile)
		if err != nil {
			fmt.Println("Рекордов пока нет.")
			return
		}
		var rec Record
		if err := json.Unmarshal(data, &rec); err != nil {
			fmt.Println("Рекордов пока нет.")
			return
		}
		fmt.Printf("Рекорд: %d попыток\n", rec.Record)
		return
	}
	game := NewGame(mode)
	game.play()
}
