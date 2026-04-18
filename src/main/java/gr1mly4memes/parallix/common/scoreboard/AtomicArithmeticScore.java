package gr1mly4memes.parallix.common.scoreboard;

public interface AtomicArithmeticScore {
    int worldthreader$addToValueAndGet(int amount);
    int worldthreader$compareExchangeValue(int expectedValue, int newValue);
}
