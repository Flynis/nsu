package ru.dyakun.snake.util;

public class SimpleIdGenerator implements IdGenerator {
    private int id = 0;

    @Override
    public int next() {
        int next = id;
        id++;
        return next;
    }
}
