package ru.dyakun.snake.game.entity;

import ru.dyakun.snake.protocol.Direction;

public interface SnakeView extends Iterable<Point> {
    Point getHead();

    Direction getDirection();
}
