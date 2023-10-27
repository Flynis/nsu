package ru.dyakun.snake.util;

public class SimpleIdGenerator implements IdGenerator {
    private int id = 0;

    @Override
    public int next() {
        return id++;
    }
}
