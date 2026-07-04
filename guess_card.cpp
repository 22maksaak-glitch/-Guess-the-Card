// guess_card.cpp
#include <iostream>
#include <vector>
#include <string>
#include <map>
#include <random>
#include <fstream>
#include <cctype>
#include <algorithm>
#include <filesystem>

using namespace std;
namespace fs = std::filesystem;

const string RESET = "\033[0m";
const string RED = "\033[91m";
const string GREEN = "\033[92m";
const string YELLOW = "\033[93m";
const string BLUE = "\033[94m";
const string CYAN = "\033[96m";
const string MAGENTA = "\033[95m";
const string BOLD = "\033[1m";

string colorize(const string& text, const string& color) {
    return color + text + RESET;
}

vector<string> SUITS = {"пики", "червы", "бубны", "трефы"};
vector<string> RANKS = {"2","3","4","5","6","7","8","9","10","J","Q","K","A"};
map<string, string> SUIT_COLORS = {
    {"пики", BLUE}, {"червы", RED}, {"бубны", RED}, {"трефы", GREEN}
};

string toLower(string s) {
    transform(s.begin(), s.end(), s.begin(), ::tolower);
    return s;
}

string getHomeDir() {
    const char* home = getenv("HOME");
    if (!home) home = getenv("USERPROFILE");
    return string(home);
}

class GuessCardGame {
public:
    string mode;
    string recordFile;
    int record;
    vector<pair<string,string>> deck;
    pair<string,string> target;
    int attempts;
    bool gameOver;

    GuessCardGame(string m) : mode(m), attempts(0), gameOver(false) {
        recordFile = getHomeDir() + "/.guess_card_record.json";
        loadRecord();
        for (auto& s : SUITS)
            for (auto& r : RANKS)
                deck.push_back({s, r});
    }

    void loadRecord() {
        ifstream f(recordFile);
        if (!f) { record = -1; return; }
        string content((istreambuf_iterator<char>(f)), istreambuf_iterator<char>());
        size_t pos = content.find("\"record\"");
        if (pos != string::npos) {
            size_t start = content.find(":", pos) + 1;
            size_t end = content.find(",", start);
            if (end == string::npos) end = content.find("}", start);
            try { record = stoi(content.substr(start, end-start)); }
            catch (...) { record = -1; }
        } else record = -1;
    }

    void saveRecord(int rec) {
        ofstream f(recordFile);
        if (f) f << "{\"record\":" << rec << "}";
    }

    void chooseCard() {
        random_device rd;
        mt19937 gen(rd());
        uniform_int_distribution<> dis(0, deck.size()-1);
        target = deck[dis(gen)];
    }

    string getSuit(const string& name) {
        string lower = toLower(name);
        for (auto& s : SUITS)
            if (toLower(s) == lower) return s;
        return "";
    }

    string getRank(const string& name) {
        string lower = toLower(name);
        for (auto& r : RANKS)
            if (toLower(r) == lower) return r;
        return "";
    }

    string displayCard(const pair<string,string>& card) {
        string color = SUIT_COLORS[card.first];
        return colorize(card.second + " " + card.first, color);
    }

    void play() {
        cout << colorize("🃏 Добро пожаловать в 'Угадай карту'!", BOLD) << endl;
        cout << "Компьютер загадал карту. Попробуйте угадать!" << endl;
        if (mode == "full") cout << "Вам нужно угадать и масть, и достоинство." << endl;
        else if (mode == "suit") cout << "Вам нужно угадать только масть." << endl;
        else cout << "Вам нужно угадать только достоинство." << endl;
        if (record != -1) cout << "Текущий рекорд: " << record << " попыток." << endl;
        cout << "Для выхода введите 'q'.\n" << endl;

        chooseCard();
        attempts = 0;
        string input;

        while (!gameOver) {
            attempts++;
            if (mode == "full") {
                cout << "Введите масть (пики/червы/бубны/трефы): ";
                getline(cin, input);
                if (toLower(input) == "q") { cout << "Выход." << endl; return; }
                string suit = getSuit(input);
                cout << "Введите достоинство (2-10, J, Q, K, A): ";
                getline(cin, input);
                if (toLower(input) == "q") { cout << "Выход." << endl; return; }
                string rank = getRank(input);
                if (suit.empty() || rank.empty()) {
                    cout << colorize("Неверный ввод. Попробуйте снова.", RED) << endl;
                    attempts--;
                    continue;
                }
                if (suit == target.first && rank == target.second) {
                    cout << colorize("🎉 Поздравляем! Вы угадали карту!", GREEN) << endl;
                    cout << "Загадана была: " << displayCard(target) << endl;
                    gameOver = true;
                } else {
                    if (suit == target.first)
                        cout << colorize("Масть угадана верно!", GREEN) << endl;
                    else
                        cout << colorize("Масть не угадана.", RED) << endl;
                    if (rank == target.second)
                        cout << colorize("Достоинство угадано верно!", GREEN) << endl;
                    else
                        cout << colorize("Достоинство не угадано.", RED) << endl;
                }
            } else if (mode == "suit") {
                cout << "Введите масть (пики/червы/бубны/трефы): ";
                getline(cin, input);
                if (toLower(input) == "q") { cout << "Выход." << endl; return; }
                string suit = getSuit(input);
                if (suit.empty()) {
                    cout << colorize("Неверная масть.", RED) << endl;
                    attempts--;
                    continue;
                }
                if (suit == target.first) {
                    cout << colorize("🎉 Поздравляем! Вы угадали масть!", GREEN) << endl;
                    cout << "Загадана была: " << displayCard(target) << endl;
                    gameOver = true;
                } else {
                    cout << colorize("Неверно. Попробуйте снова.", RED) << endl;
                }
            } else { // rank
                cout << "Введите достоинство (2-10, J, Q, K, A): ";
                getline(cin, input);
                if (toLower(input) == "q") { cout << "Выход." << endl; return; }
                string rank = getRank(input);
                if (rank.empty()) {
                    cout << colorize("Неверное достоинство.", RED) << endl;
                    attempts--;
                    continue;
                }
                if (rank == target.second) {
                    cout << colorize("🎉 Поздравляем! Вы угадали достоинство!", GREEN) << endl;
                    cout << "Загадана была: " << displayCard(target) << endl;
                    gameOver = true;
                } else {
                    cout << colorize("Неверно. Попробуйте снова.", RED) << endl;
                }
            }
        }
        cout << "Количество попыток: " << attempts << endl;
        if (record == -1 || attempts < record) {
            saveRecord(attempts);
            cout << colorize("🏆 Новый рекорд: " + to_string(attempts) + " попыток!", YELLOW) << endl;
        }
    }
};

int main(int argc, char* argv[]) {
    string mode = "full";
    bool showRecord = false;
    for (int i=1; i<argc; ++i) {
        string arg = argv[i];
        if (arg == "full" || arg == "suit" || arg == "rank") mode = arg;
        else if (arg == "-r" || arg == "--record") showRecord = true;
        else if (arg == "-h" || arg == "--help") {
            cout << "Usage: guess_card [full|suit|rank] [-r]" << endl;
            return 0;
        }
    }
    if (showRecord) {
        string f = getHomeDir() + "/.guess_card_record.json";
        ifstream file(f);
        if (file) {
            string content((istreambuf_iterator<char>(file)), istreambuf_iterator<char>());
            size_t pos = content.find("\"record\"");
            if (pos != string::npos) {
                size_t start = content.find(":", pos) + 1;
                size_t end = content.find(",", start);
                if (end == string::npos) end = content.find("}", start);
                try { cout << "Рекорд: " << stoi(content.substr(start, end-start)) << " попыток" << endl; }
                catch (...) { cout << "Рекордов пока нет." << endl; }
            } else cout << "Рекордов пока нет." << endl;
        } else cout << "Рекордов пока нет." << endl;
        return 0;
    }
    GuessCardGame game(mode);
    game.play();
    return 0;
}
