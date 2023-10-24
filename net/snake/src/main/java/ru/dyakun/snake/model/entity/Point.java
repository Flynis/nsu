package ru.dyakun.snake.model.entity;

import ru.dyakun.snake.protocol.Direction;

public class Point {
    public int x;
    public int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof Point p) {
            return this.x == p.x && this.y == p.y;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }

    public void move(Direction direction) {
        switch (direction) {
            case UP -> y--;
            case DOWN -> y++;
            case LEFT -> x--;
            case RIGHT -> x++;
        }
    }
}
