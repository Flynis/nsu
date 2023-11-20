package ru.dyakun.snake.util;

public class SimpleIdGenerator implements IdGenerator {
    private int id = 0;

    public SimpleIdGenerator(int initial) {
        id = initial;
    }

    public SimpleIdGenerator() {}

    @Override
    public int next() {
        return id++;
    }
}
