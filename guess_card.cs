// guess_card.cs
using System;
using System.Collections.Generic;
using System.IO;
using System.Text.Json;
using System.Linq;

class GuessCardGame
{
    static string Colorize(string text, string color)
    {
        string col = color switch
        {
            "red" => "\x1b[91m",
            "green" => "\x1b[92m",
            "yellow" => "\x1b[93m",
            "blue" => "\x1b[94m",
            "cyan" => "\x1b[96m",
            "magenta" => "\x1b[95m",
            "bold" => "\x1b[1m",
            _ => "\x1b[0m"
        };
        return col + text + "\x1b[0m";
    }

    static string[] SUITS = {"пики", "червы", "бубны", "трефы"};
    static string[] RANKS = {"2","3","4","5","6","7","8","9","10","J","Q","K","A"};
    static Dictionary<string, string> SUIT_COLORS = new Dictionary<string, string> {
        {"пики", "blue"}, {"червы", "red"}, {"бубны", "red"}, {"трефы", "green"}
    };

    class RecordData
    {
        public int record { get; set; }
    }

    private string mode;
    private string recordFile;
    private int record;
    private List<(string suit, string rank)> deck;
    private (string suit, string rank) target;
    private int attempts;
    private bool gameOver;

    public GuessCardGame(string mode)
    {
        this.mode = mode;
        recordFile = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.UserProfile), ".guess_card_record.json");
        LoadRecord();
        deck = new List<(string, string)>();
        foreach (var s in SUITS)
            foreach (var r in RANKS)
                deck.Add((s, r));
    }

    void LoadRecord()
    {
        if (!File.Exists(recordFile)) { record = -1; return; }
        try
        {
            string json = File.ReadAllText(recordFile);
            var data = JsonSerializer.Deserialize<RecordData>(json);
            record = data?.record ?? -1;
        }
        catch { record = -1; }
    }

    void SaveRecord(int rec)
    {
        var data = new RecordData { record = rec };
        string json = JsonSerializer.Serialize(data);
        File.WriteAllText(recordFile, json);
    }

    void ChooseCard()
    {
        Random rnd = new Random();
        target = deck[rnd.Next(deck.Count)];
    }

    string GetSuit(string name)
    {
        name = name.ToLower();
        foreach (var s in SUITS)
            if (s.ToLower() == name) return s;
        return null;
    }

    string GetRank(string name)
    {
        name = name.ToLower();
        foreach (var r in RANKS)
            if (r.ToLower() == name) return r;
        return null;
    }

    string DisplayCard(string suit, string rank)
    {
        string col = SUIT_COLORS[suit];
        return Colorize($"{rank} {suit}", col);
    }

    public void Play()
    {
        Console.WriteLine(Colorize("🃏 Добро пожаловать в 'Угадай карту'!", "bold"));
        Console.WriteLine("Компьютер загадал карту. Попробуйте угадать!");
        if (mode == "full")
            Console.WriteLine("Вам нужно угадать и масть, и достоинство.");
        else if (mode == "suit")
            Console.WriteLine("Вам нужно угадать только масть.");
        else
            Console.WriteLine("Вам нужно угадать только достоинство.");
        if (record != -1)
            Console.WriteLine($"Текущий рекорд: {record} попыток.");
        Console.WriteLine("Для выхода введите 'q'.\n");

        ChooseCard();
        attempts = 0;
        gameOver = false;

        while (!gameOver)
        {
            attempts++;
            if (mode == "full")
            {
                Console.Write("Введите масть (пики/червы/бубны/трефы): ");
                string suitInput = Console.ReadLine().Trim();
                if (suitInput.ToLower() == "q") { Console.WriteLine("Выход."); return; }
                Console.Write("Введите достоинство (2-10, J, Q, K, A): ");
                string rankInput = Console.ReadLine().Trim();
                if (rankInput.ToLower() == "q") { Console.WriteLine("Выход."); return; }
                string suit = GetSuit(suitInput);
                string rank = GetRank(rankInput);
                if (suit == null || rank == null)
                {
                    Console.WriteLine(Colorize("Неверный ввод. Попробуйте снова.", "red"));
                    attempts--;
                    continue;
                }
                if (suit == target.suit && rank == target.rank)
                {
                    Console.WriteLine(Colorize("🎉 Поздравляем! Вы угадали карту!", "green"));
                    Console.WriteLine($"Загадана была: {DisplayCard(target.suit, target.rank)}");
                    gameOver = true;
                }
                else
                {
                    if (suit == target.suit)
                        Console.WriteLine(Colorize("Масть угадана верно!", "green"));
                    else
                        Console.WriteLine(Colorize("Масть не угадана.", "red"));
                    if (rank == target.rank)
                        Console.WriteLine(Colorize("Достоинство угадано верно!", "green"));
                    else
                        Console.WriteLine(Colorize("Достоинство не угадано.", "red"));
                }
            }
            else if (mode == "suit")
            {
                Console.Write("Введите масть (пики/червы/бубны/трефы): ");
                string suitInput = Console.ReadLine().Trim();
                if (suitInput.ToLower() == "q") { Console.WriteLine("Выход."); return; }
                string suit = GetSuit(suitInput);
                if (suit == null)
                {
                    Console.WriteLine(Colorize("Неверная масть.", "red"));
                    attempts--;
                    continue;
                }
                if (suit == target.suit)
                {
                    Console.WriteLine(Colorize("🎉 Поздравляем! Вы угадали масть!", "green"));
                    Console.WriteLine($"Загадана была: {DisplayCard(target.suit, target.rank)}");
                    gameOver = true;
                }
                else
                {
                    Console.WriteLine(Colorize("Неверно. Попробуйте снова.", "red"));
                }
            }
            else // rank
            {
                Console.Write("Введите достоинство (2-10, J, Q, K, A): ");
                string rankInput = Console.ReadLine().Trim();
                if (rankInput.ToLower() == "q") { Console.WriteLine("Выход."); return; }
                string rank = GetRank(rankInput);
                if (rank == null)
                {
                    Console.WriteLine(Colorize("Неверное достоинство.", "red"));
                    attempts--;
                    continue;
                }
                if (rank == target.rank)
                {
                    Console.WriteLine(Colorize("🎉 Поздравляем! Вы угадали достоинство!", "green"));
                    Console.WriteLine($"Загадана была: {DisplayCard(target.suit, target.rank)}");
                    gameOver = true;
                }
                else
                {
                    Console.WriteLine(Colorize("Неверно. Попробуйте снова.", "red"));
                }
            }
        }
        Console.WriteLine($"Количество попыток: {attempts}");
        if (record == -1 || attempts < record)
        {
            SaveRecord(attempts);
            Console.WriteLine(Colorize($"🏆 Новый рекорд: {attempts} попыток!", "yellow"));
        }
    }

    static void Main(string[] args)
    {
        string mode = "full";
        bool showRecord = false;
        foreach (var arg in args)
        {
            if (arg == "full" || arg == "suit" || arg == "rank") mode = arg;
            else if (arg == "-r" || arg == "--record") showRecord = true;
            else if (arg == "-h" || arg == "--help")
            {
                Console.WriteLine("Usage: guess_card [full|suit|rank] [-r]");
                return;
            }
        }
        if (showRecord)
        {
            string recFile = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.UserProfile), ".guess_card_record.json");
            if (File.Exists(recFile))
            {
                try
                {
                    string json = File.ReadAllText(recFile);
                    var data = JsonSerializer.Deserialize<RecordData>(json);
                    Console.WriteLine($"Рекорд: {data?.record ?? 0} попыток");
                }
                catch { Console.WriteLine("Рекордов пока нет."); }
            }
            else Console.WriteLine("Рекордов пока нет.");
            return;
        }
        GuessCardGame game = new GuessCardGame(mode);
        game.Play();
    }
}
