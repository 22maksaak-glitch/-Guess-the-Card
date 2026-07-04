# guess_card.py
#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import os
import json
import random
from pathlib import Path

# ANSI-цвета
COLORS = {
    'reset': '\033[0m',
    'red': '\033[91m',
    'green': '\033[92m',
    'yellow': '\033[93m',
    'blue': '\033[94m',
    'cyan': '\033[96m',
    'magenta': '\033[95m',
    'bold': '\033[1m'
}

def colorize(text, color):
    return f"{COLORS.get(color, '')}{text}{COLORS['reset']}"

# Масти и достоинства
SUITS = ['пики', 'червы', 'бубны', 'трефы']
RANKS = ['2', '3', '4', '5', '6', '7', '8', '9', '10', 'J', 'Q', 'K', 'A']
SUIT_COLORS = {'пики': 'blue', 'червы': 'red', 'бубны': 'red', 'трефы': 'green'}

class GuessCardGame:
    def __init__(self, mode='full'):
        self.mode = mode
        self.record_file = Path.home() / '.guess_card_record.json'
        self.record = self.load_record()
        self.deck = [(s, r) for s in SUITS for r in RANKS]
        self.target = None
        self.attempts = 0
        self.game_over = False

    def load_record(self):
        if self.record_file.exists():
            with open(self.record_file, 'r') as f:
                data = json.load(f)
                return data.get('record', None)
        return None

    def save_record(self, attempts):
        with open(self.record_file, 'w') as f:
            json.dump({'record': attempts}, f)

    def choose_card(self):
        self.target = random.choice(self.deck)

    def get_suit(self, name):
        for s in SUITS:
            if s.lower() == name.lower():
                return s
        return None

    def get_rank(self, name):
        for r in RANKS:
            if r.lower() == name.lower():
                return r
        return None

    def display_card(self, card):
        suit, rank = card
        color = SUIT_COLORS[suit]
        return colorize(f"{rank} {suit}", color)

    def play(self):
        print(colorize("🃏 Добро пожаловать в 'Угадай карту'!", 'bold'))
        print("Компьютер загадал карту. Попробуйте угадать!")
        if self.mode == 'full':
            print("Вам нужно угадать и масть, и достоинство.")
        elif self.mode == 'suit':
            print("Вам нужно угадать только масть.")
        else:
            print("Вам нужно угадать только достоинство.")
        if self.record:
            print(f"Текущий рекорд: {self.record} попыток.")
        print("Для выхода введите 'q'.\n")

        self.choose_card()
        self.attempts = 0

        while not self.game_over:
            self.attempts += 1
            if self.mode == 'full':
                suit_input = input("Введите масть (пики/червы/бубны/трефы): ").strip()
                if suit_input.lower() == 'q':
                    print("Выход.")
                    return
                rank_input = input("Введите достоинство (2-10, J, Q, K, A): ").strip()
                if rank_input.lower() == 'q':
                    print("Выход.")
                    return
                suit = self.get_suit(suit_input)
                rank = self.get_rank(rank_input)
                if suit is None or rank is None:
                    print("Неверный ввод. Попробуйте снова.")
                    self.attempts -= 1
                    continue
                if suit == self.target[0] and rank == self.target[1]:
                    print(colorize("🎉 Поздравляем! Вы угадали карту!", 'green'))
                    print(f"Загадана была: {self.display_card(self.target)}")
                    self.game_over = True
                else:
                    if suit == self.target[0]:
                        print(colorize("Масть угадана верно!", 'green'))
                    else:
                        print(colorize("Масть не угадана.", 'red'))
                    if rank == self.target[1]:
                        print(colorize("Достоинство угадано верно!", 'green'))
                    else:
                        print(colorize("Достоинство не угадано.", 'red'))
            elif self.mode == 'suit':
                suit_input = input("Введите масть (пики/червы/бубны/трефы): ").strip()
                if suit_input.lower() == 'q':
                    print("Выход.")
                    return
                suit = self.get_suit(suit_input)
                if suit is None:
                    print("Неверная масть.")
                    self.attempts -= 1
                    continue
                if suit == self.target[0]:
                    print(colorize("🎉 Поздравляем! Вы угадали масть!", 'green'))
                    print(f"Загадана была: {self.display_card(self.target)}")
                    self.game_over = True
                else:
                    print(colorize("Неверно. Попробуйте снова.", 'red'))
            else:  # rank only
                rank_input = input("Введите достоинство (2-10, J, Q, K, A): ").strip()
                if rank_input.lower() == 'q':
                    print("Выход.")
                    return
                rank = self.get_rank(rank_input)
                if rank is None:
                    print("Неверное достоинство.")
                    self.attempts -= 1
                    continue
                if rank == self.target[1]:
                    print(colorize("🎉 Поздравляем! Вы угадали достоинство!", 'green'))
                    print(f"Загадана была: {self.display_card(self.target)}")
                    self.game_over = True
                else:
                    print(colorize("Неверно. Попробуйте снова.", 'red'))

        # Конец игры
        print(f"Количество попыток: {self.attempts}")
        if self.record is None or self.attempts < self.record:
            self.save_record(self.attempts)
            print(colorize(f"🏆 Новый рекорд: {self.attempts} попыток!", 'yellow'))

def main():
    mode = 'full'
    show_record = False
    args = sys.argv[1:]
    for arg in args:
        if arg in ['full', 'suit', 'rank']:
            mode = arg
        elif arg in ['-r', '--record']:
            show_record = True
        elif arg in ['-h', '--help']:
            print("Usage: guess_card.py [full|suit|rank] [-r]")
            return
    if show_record:
        record_file = Path.home() / '.guess_card_record.json'
        if record_file.exists():
            with open(record_file, 'r') as f:
                data = json.load(f)
                print(f"Рекорд: {data.get('record', 'нет')} попыток")
        else:
            print("Рекордов пока нет.")
        return
    game = GuessCardGame(mode)
    game.play()

if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        print(colorize("\nИгра прервана.", 'yellow'))
        sys.exit(0)
