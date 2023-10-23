package ru.dyakun.snake.model.field;

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

    public static Direction determineDirection(Point head, Point tail) {
        if(head.x == tail.x) {
            if(head.y - tail.y > 0) {
                return Direction.DOWN;
            } else {
                return Direction.UP;
            }
        } else if(head.y == tail.y) {
            if(head.x - tail.x > 0) {
                return Direction.RIGHT;
            } else {
                return Direction.LEFT;
            }
        } else {
            throw new IllegalStateException("At least one coordinate must coincide between tail and head");
        }
    }
}
