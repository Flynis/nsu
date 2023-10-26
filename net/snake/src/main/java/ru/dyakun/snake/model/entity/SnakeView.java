package ru.dyakun.snake.model.entity;

import java.util.Collection;

public interface SnakeView {
    Collection<Point> points();
    Point getHead();
    Point getTail();
}
