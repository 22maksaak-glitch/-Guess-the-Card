#!/usr/bin/env ruby
# guess_card.rb
# encoding: UTF-8

require 'json'
require 'fileutils'

COLORS = {
  reset: "\e[0m",
  red: "\e[91m",
  green: "\e[92m",
  yellow: "\e[93m",
  blue: "\e[94m",
  cyan: "\e[96m",
  magenta: "\e[95m",
  bold: "\e[1m"
}

def colorize(text, color)
  "#{COLORS[color]}#{text}#{COLORS[:reset]}"
end

SUITS = ['пики', 'червы', 'бубны', 'трефы']
RANKS = ['2','3','4','5','6','7','8','9','10','J','Q','K','A']
SUIT_COLORS = {'пики'=>'blue', 'червы'=>'red', 'бубны'=>'red', 'трефы'=>'green'}

class GuessCardGame
  attr_reader :mode, :record_file, :record, :deck, :target, :attempts, :game_over

  def initialize(mode='full')
    @mode = mode
    @record_file = File.join(Dir.home, '.guess_card_record.json')
    @record = load_record
    @deck = SUITS.product(RANKS)
    @target = nil
    @attempts = 0
    @game_over = false
  end

  def load_record
    return nil unless File.exist?(@record_file)
    data = JSON.parse(File.read(@record_file))
    data['record'] rescue nil
  end

  def save_record(attempts)
    File.write(@record_file, JSON.pretty_generate({ 'record' => attempts }))
  end

  def choose_card
    @target = @deck.sample
  end

  def get_suit(name)
    SUITS.find { |s| s.casecmp(name).zero? }
  end

  def get_rank(name)
    RANKS.find { |r| r.casecmp(name).zero? }
  end

  def display_card(suit, rank)
    col = SUIT_COLORS[suit] || 'reset'
    colorize("#{rank} #{suit}", col)
  end

  def play
    puts colorize("🃏 Добро пожаловать в 'Угадай карту'!", :bold)
    puts "Компьютер загадал карту. Попробуйте угадать!"
    if @mode == 'full'
      puts "Вам нужно угадать и масть, и достоинство."
    elsif @mode == 'suit'
      puts "Вам нужно угадать только масть."
    else
      puts "Вам нужно угадать только достоинство."
    end
    puts "Текущий рекорд: #{@record} попыток." if @record
    puts "Для выхода введите 'q'.\n"

    choose_card
    @attempts = 0

    until @game_over
      @attempts += 1
      if @mode == 'full'
        print "Введите масть (пики/червы/бубны/трефы): "
        suit_input = gets.chomp.strip
        break if suit_input.downcase == 'q'
        print "Введите достоинство (2-10, J, Q, K, A): "
        rank_input = gets.chomp.strip
        break if rank_input.downcase == 'q'
        suit = get_suit(suit_input)
        rank = get_rank(rank_input)
        unless suit && rank
          puts colorize("Неверный ввод. Попробуйте снова.", :red)
          @attempts -= 1
          next
        end
        if suit == @target[0] && rank == @target[1]
          puts colorize("🎉 Поздравляем! Вы угадали карту!", :green)
          puts "Загадана была: #{display_card(@target[0], @target[1])}"
          @game_over = true
        else
          puts suit == @target[0] ? colorize("Масть угадана верно!", :green) : colorize("Масть не угадана.", :red)
          puts rank == @target[1] ? colorize("Достоинство угадано верно!", :green) : colorize("Достоинство не угадано.", :red)
        end
      elsif @mode == 'suit'
        print "Введите масть (пики/червы/бубны/трефы): "
        suit_input = gets.chomp.strip
        break if suit_input.downcase == 'q'
        suit = get_suit(suit_input)
        unless suit
          puts colorize("Неверная масть.", :red)
          @attempts -= 1
          next
        end
        if suit == @target[0]
          puts colorize("🎉 Поздравляем! Вы угадали масть!", :green)
          puts "Загадана была: #{display_card(@target[0], @target[1])}"
          @game_over = true
        else
          puts colorize("Неверно. Попробуйте снова.", :red)
        end
      else # rank
        print "Введите достоинство (2-10, J, Q, K, A): "
        rank_input = gets.chomp.strip
        break if rank_input.downcase == 'q'
        rank = get_rank(rank_input)
        unless rank
          puts colorize("Неверное достоинство.", :red)
          @attempts -= 1
          next
        end
        if rank == @target[1]
          puts colorize("🎉 Поздравляем! Вы угадали достоинство!", :green)
          puts "Загадана была: #{display_card(@target[0], @target[1])}"
          @game_over = true
        else
          puts colorize("Неверно. Попробуйте снова.", :red)
        end
      end
    end
    puts "Количество попыток: #{@attempts}"
    if @record.nil? || @attempts < @record
      save_record(@attempts)
      puts colorize("🏆 Новый рекорд: #{@attempts} попыток!", :yellow)
    end
  end
end

def main
  mode = 'full'
  show_record = false
  ARGV.each do |arg|
    case arg
    when 'full', 'suit', 'rank' then mode = arg
    when '-r', '--record' then show_record = true
    when '-h', '--help'
      puts "Usage: ruby guess_card.rb [full|suit|rank] [-r]"
      return
    end
  end
  if show_record
    rec_file = File.join(Dir.home, '.guess_card_record.json')
    if File.exist?(rec_file)
      data = JSON.parse(File.read(rec_file))
      puts "Рекорд: #{data['record'] || 'нет'} попыток"
    else
      puts "Рекордов пока нет."
    end
    return
  end
  game = GuessCardGame.new(mode)
  game.play
end

main if __FILE__ == $0
