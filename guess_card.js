// guess_card.js
#!/usr/bin/env node
'use strict';

const fs = require('fs');
const path = require('path');
const os = require('os');
const readline = require('readline');

const COLORS = {
    reset: '\x1b[0m',
    red: '\x1b[91m',
    green: '\x1b[92m',
    yellow: '\x1b[93m',
    blue: '\x1b[94m',
    cyan: '\x1b[96m',
    magenta: '\x1b[95m',
    bold: '\x1b[1m'
};

function colorize(text, color) {
    return COLORS[color] + text + COLORS.reset;
}

const SUITS = ['пики', 'червы', 'бубны', 'трефы'];
const RANKS = ['2', '3', '4', '5', '6', '7', '8', '9', '10', 'J', 'Q', 'K', 'A'];
const SUIT_COLORS = {пики: 'blue', червы: 'red', бубны: 'red', трефы: 'green'};

class GuessCardGame {
    constructor(mode = 'full') {
        this.mode = mode;
        this.recordFile = path.join(os.homedir(), '.guess_card_record.json');
        this.record = this.loadRecord();
        this.deck = [];
        for (const s of SUITS)
            for (const r of RANKS)
                this.deck.push([s, r]);
        this.target = null;
        this.attempts = 0;
        this.gameOver = false;
    }

    loadRecord() {
        try {
            const data = JSON.parse(fs.readFileSync(this.recordFile, 'utf8'));
            return data.record || null;
        } catch {
            return null;
        }
    }

    saveRecord(attempts) {
        fs.writeFileSync(this.recordFile, JSON.stringify({ record: attempts }));
    }

    chooseCard() {
        const idx = Math.floor(Math.random() * this.deck.length);
        this.target = this.deck[idx];
    }

    getSuit(name) {
        const lower = name.toLowerCase();
        return SUITS.find(s => s.toLowerCase() === lower) || null;
    }

    getRank(name) {
        const lower = name.toLowerCase();
        return RANKS.find(r => r.toLowerCase() === lower) || null;
    }

    displayCard(suit, rank) {
        const col = SUIT_COLORS[suit] || 'reset';
        return colorize(`${rank} ${suit}`, col);
    }

    async play() {
        const rl = readline.createInterface({
            input: process.stdin,
            output: process.stdout
        });
        const question = (q) => new Promise(resolve => rl.question(q, resolve));

        console.log(colorize('🃏 Добро пожаловать в "Угадай карту"!', 'bold'));
        console.log('Компьютер загадал карту. Попробуйте угадать!');
        if (this.mode === 'full') {
            console.log('Вам нужно угадать и масть, и достоинство.');
        } else if (this.mode === 'suit') {
            console.log('Вам нужно угадать только масть.');
        } else {
            console.log('Вам нужно угадать только достоинство.');
        }
        if (this.record) {
            console.log(`Текущий рекорд: ${this.record} попыток.`);
        }
        console.log("Для выхода введите 'q'.\n");

        this.chooseCard();
        this.attempts = 0;

        while (!this.gameOver) {
            this.attempts++;
            if (this.mode === 'full') {
                const suitInput = await question('Введите масть (пики/червы/бубны/трефы): ');
                if (suitInput.toLowerCase() === 'q') {
                    console.log('Выход.');
                    rl.close();
                    return;
                }
                const rankInput = await question('Введите достоинство (2-10, J, Q, K, A): ');
                if (rankInput.toLowerCase() === 'q') {
                    console.log('Выход.');
                    rl.close();
                    return;
                }
                const suit = this.getSuit(suitInput);
                const rank = this.getRank(rankInput);
                if (!suit || !rank) {
                    console.log(colorize('Неверный ввод. Попробуйте снова.', 'red'));
                    this.attempts--;
                    continue;
                }
                if (suit === this.target[0] && rank === this.target[1]) {
                    console.log(colorize('🎉 Поздравляем! Вы угадали карту!', 'green'));
                    console.log(`Загадана была: ${this.displayCard(this.target[0], this.target[1])}`);
                    this.gameOver = true;
                } else {
                    if (suit === this.target[0]) {
                        console.log(colorize('Масть угадана верно!', 'green'));
                    } else {
                        console.log(colorize('Масть не угадана.', 'red'));
                    }
                    if (rank === this.target[1]) {
                        console.log(colorize('Достоинство угадано верно!', 'green'));
                    } else {
                        console.log(colorize('Достоинство не угадано.', 'red'));
                    }
                }
            } else if (this.mode === 'suit') {
                const suitInput = await question('Введите масть (пики/червы/бубны/трефы): ');
                if (suitInput.toLowerCase() === 'q') {
                    console.log('Выход.');
                    rl.close();
                    return;
                }
                const suit = this.getSuit(suitInput);
                if (!suit) {
                    console.log(colorize('Неверная масть.', 'red'));
                    this.attempts--;
                    continue;
                }
                if (suit === this.target[0]) {
                    console.log(colorize('🎉 Поздравляем! Вы угадали масть!', 'green'));
                    console.log(`Загадана была: ${this.displayCard(this.target[0], this.target[1])}`);
                    this.gameOver = true;
                } else {
                    console.log(colorize('Неверно. Попробуйте снова.', 'red'));
                }
            } else { // rank
                const rankInput = await question('Введите достоинство (2-10, J, Q, K, A): ');
                if (rankInput.toLowerCase() === 'q') {
                    console.log('Выход.');
                    rl.close();
                    return;
                }
                const rank = this.getRank(rankInput);
                if (!rank) {
                    console.log(colorize('Неверное достоинство.', 'red'));
                    this.attempts--;
                    continue;
                }
                if (rank === this.target[1]) {
                    console.log(colorize('🎉 Поздравляем! Вы угадали достоинство!', 'green'));
                    console.log(`Загадана была: ${this.displayCard(this.target[0], this.target[1])}`);
                    this.gameOver = true;
                } else {
                    console.log(colorize('Неверно. Попробуйте снова.', 'red'));
                }
            }
        }
        console.log(`Количество попыток: ${this.attempts}`);
        if (!this.record || this.attempts < this.record) {
            this.saveRecord(this.attempts);
            console.log(colorize(`🏆 Новый рекорд: ${this.attempts} попыток!`, 'yellow'));
        }
        rl.close();
    }
}

async function main() {
    let mode = 'full';
    let showRecord = false;
    const args = process.argv.slice(2);
    for (const arg of args) {
        if (arg === 'full' || arg === 'suit' || arg === 'rank') mode = arg;
        else if (arg === '-r' || arg === '--record') showRecord = true;
        else if (arg === '-h' || arg === '--help') {
            console.log('Usage: node guess_card.js [full|suit|rank] [-r]');
            return;
        }
    }
    if (showRecord) {
        const recFile = path.join(os.homedir(), '.guess_card_record.json');
        try {
            const data = JSON.parse(fs.readFileSync(recFile, 'utf8'));
            console.log(`Рекорд: ${data.record || 'нет'} попыток`);
        } catch {
            console.log('Рекордов пока нет.');
        }
        return;
    }
    const game = new GuessCardGame(mode);
    await game.play();
}

main().catch(console.error);
