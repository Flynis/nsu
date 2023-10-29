package ru.dyakun.snake.model.util;

import ru.dyakun.snake.model.entity.Snake;

import java.util.Collection;

public class Snakes {
    private Snakes() {
        throw new AssertionError();
    }

    public static Snake findById(Collection<Snake> snakes, int id) {
        for(var snake: snakes) {
            if(snake.playerId() == id) {
                return snake;
            }
        }
        throw new IllegalStateException("Snake not found");
    }
}
