// guess_card.java
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class guess_card {
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[91m";
    private static final String GREEN = "\u001B[92m";
    private static final String YELLOW = "\u001B[93m";
    private static final String BLUE = "\u001B[94m";
    private static final String CYAN = "\u001B[96m";
    private static final String MAGENTA = "\u001B[95m";
    private static final String BOLD = "\u001B[1m";

    private static String colorize(String text, String color) {
        return color + text + RESET;
    }

    private static final String[] SUITS = {"пики", "червы", "бубны", "трефы"};
    private static final String[] RANKS = {"2","3","4","5","6","7","8","9","10","J","Q","K","A"};
    private static final Map<String, String> SUIT_COLORS = new HashMap<>();
    static {
        SUIT_COLORS.put("пики", BLUE);
        SUIT_COLORS.put("червы", RED);
        SUIT_COLORS.put("бубны", RED);
        SUIT_COLORS.put("трефы", GREEN);
    }

    private static class RecordData {
        int record;
    }

    private String mode;
    private String recordFile;
    private int record;
    private List<String[]> deck;
    private String[] target;
    private int attempts;
    private boolean gameOver;

    public guess_card(String mode) {
        this.mode = mode;
        recordFile = System.getProperty("user.home") + "/.guess_card_record.json";
        loadRecord();
        deck = new ArrayList<>();
        for (String s : SUITS)
            for (String r : RANKS)
                deck.add(new String[]{s, r});
    }

    private void loadRecord() {
        try {
            String json = new String(Files.readAllBytes(Paths.get(recordFile)));
            // упрощённый парсинг
            int idx = json.indexOf("\"record\"");
            if (idx != -1) {
                int start = json.indexOf(":", idx) + 1;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                record = Integer.parseInt(json.substring(start, end).trim());
            } else record = -1;
        } catch (Exception e) {
            record = -1;
        }
    }

    private void saveRecord(int rec) {
        try {
            Files.write(Paths.get(recordFile), ("{\"record\":" + rec + "}").getBytes());
        } catch (IOException e) {}
    }

    private void chooseCard() {
        Random rand = new Random();
        target = deck.get(rand.nextInt(deck.size()));
    }

    private String getSuit(String name) {
        for (String s : SUITS)
            if (s.equalsIgnoreCase(name)) return s;
        return null;
    }

    private String getRank(String name) {
        for (String r : RANKS)
            if (r.equalsIgnoreCase(name)) return r;
        return null;
    }

    private String displayCard(String suit, String rank) {
        String col = SUIT_COLORS.get(suit);
        return colorize(rank + " " + suit, col);
    }

    public void play() {
        Scanner scanner = new Scanner(System.in);
        System.out.println(colorize("🃏 Добро пожаловать в 'Угадай карту'!", BOLD));
        System.out.println("Компьютер загадал карту. Попробуйте угадать!");
        if (mode.equals("full"))
            System.out.println("Вам нужно угадать и масть, и достоинство.");
        else if (mode.equals("suit"))
            System.out.println("Вам нужно угадать только масть.");
        else
            System.out.println("Вам нужно угадать только достоинство.");
        if (record != -1)
            System.out.println("Текущий рекорд: " + record + " попыток.");
        System.out.println("Для выхода введите 'q'.\n");

        chooseCard();
        attempts = 0;
        gameOver = false;

        while (!gameOver) {
            attempts++;
            if (mode.equals("full")) {
                System.out.print("Введите масть (пики/червы/бубны/трефы): ");
                String suitInput = scanner.nextLine().trim();
                if (suitInput.equalsIgnoreCase("q")) { System.out.println("Выход."); return; }
                System.out.print("Введите достоинство (2-10, J, Q, K, A): ");
                String rankInput = scanner.nextLine().trim();
                if (rankInput.equalsIgnoreCase("q")) { System.out.println("Выход."); return; }
                String suit = getSuit(suitInput);
                String rank = getRank(rankInput);
                if (suit == null || rank == null) {
                    System.out.println(colorize("Неверный ввод. Попробуйте снова.", RED));
                    attempts--;
                    continue;
                }
                if (suit.equals(target[0]) && rank.equals(target[1])) {
                    System.out.println(colorize("🎉 Поздравляем! Вы угадали карту!", GREEN));
                    System.out.println("Загадана была: " + displayCard(target[0], target[1]));
                    gameOver = true;
                } else {
                    if (suit.equals(target[0]))
                        System.out.println(colorize("Масть угадана верно!", GREEN));
                    else
                        System.out.println(colorize("Масть не угадана.", RED));
                    if (rank.equals(target[1]))
                        System.out.println(colorize("Достоинство угадано верно!", GREEN));
                    else
                        System.out.println(colorize("Достоинство не угадано.", RED));
                }
            } else if (mode.equals("suit")) {
                System.out.print("Введите масть (пики/червы/бубны/трефы): ");
                String suitInput = scanner.nextLine().trim();
                if (suitInput.equalsIgnoreCase("q")) { System.out.println("Выход."); return; }
                String suit = getSuit(suitInput);
                if (suit == null) {
                    System.out.println(colorize("Неверная масть.", RED));
                    attempts--;
                    continue;
                }
                if (suit.equals(target[0])) {
                    System.out.println(colorize("🎉 Поздравляем! Вы угадали масть!", GREEN));
                    System.out.println("Загадана была: " + displayCard(target[0], target[1]));
                    gameOver = true;
                } else {
                    System.out.println(colorize("Неверно. Попробуйте снова.", RED));
                }
            } else { // rank
                System.out.print("Введите достоинство (2-10, J, Q, K, A): ");
                String rankInput = scanner.nextLine().trim();
                if (rankInput.equalsIgnoreCase("q")) { System.out.println("Выход."); return; }
                String rank = getRank(rankInput);
                if (rank == null) {
                    System.out.println(colorize("Неверное достоинство.", RED));
                    attempts--;
                    continue;
                }
                if (rank.equals(target[1])) {
                    System.out.println(colorize("🎉 Поздравляем! Вы угадали достоинство!", GREEN));
                    System.out.println("Загадана была: " + displayCard(target[0], target[1]));
                    gameOver = true;
                } else {
                    System.out.println(colorize("Неверно. Попробуйте снова.", RED));
                }
            }
        }
        System.out.println("Количество попыток: " + attempts);
        if (record == -1 || attempts < record) {
            saveRecord(attempts);
            System.out.println(colorize("🏆 Новый рекорд: " + attempts + " попыток!", YELLOW));
        }
        scanner.close();
    }

    public static void main(String[] args) {
        String mode = "full";
        boolean showRecord = false;
        for (String arg : args) {
            if (arg.equals("full") || arg.equals("suit") || arg.equals("rank")) mode = arg;
            else if (arg.equals("-r") || arg.equals("--record")) showRecord = true;
            else if (arg.equals("-h") || arg.equals("--help")) {
                System.out.println("Usage: java guess_card [full|suit|rank] [-r]");
                return;
            }
        }
        if (showRecord) {
            String recFile = System.getProperty("user.home") + "/.guess_card_record.json";
            try {
                String json = new String(Files.readAllBytes(Paths.get(recFile)));
                int idx = json.indexOf("\"record\"");
                if (idx != -1) {
                    int start = json.indexOf(":", idx) + 1;
                    int end = json.indexOf(",", start);
                    if (end == -1) end = json.indexOf("}", start);
                    int rec = Integer.parseInt(json.substring(start, end).trim());
                    System.out.println("Рекорд: " + rec + " попыток");
                } else System.out.println("Рекордов пока нет.");
            } catch (Exception e) {
                System.out.println("Рекордов пока нет.");
            }
            return;
        }
        guess_card game = new guess_card(mode);
        game.play();
    }
}
