package ru.dyakun.snake.game.entity;

import java.util.Collection;

public interface SnakeView {
    Collection<Point> points();
    Point getHead();
    Point getTail();
}
