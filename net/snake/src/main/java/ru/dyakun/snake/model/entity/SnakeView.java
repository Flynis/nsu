package ru.dyakun.snake.model.entity;

import java.util.List;

public interface SnakeView {
    List<Point> points();
    Point getHead();
    Point getTail();
}
