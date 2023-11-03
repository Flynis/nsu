package ru.dyakun.snake.game.util;

import ru.dyakun.snake.game.entity.Point;
import ru.dyakun.snake.protocol.Direction;

import java.util.Collection;

public class Points {
    private Points() {
        throw new AssertionError();
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

    public static Point getRelativeDisplacement(Point head, Point tail) {
        if(tail.equals(head.x, head.y - 1)) {
            return new Point(0, -1);
        }
        if(tail.equals(head.x, head.y + 1)) {
            return new Point(0, 1);
        }
        if(tail.equals(head.x - 1, head.y)) {
            return new Point(-1, 0);
        }
        if(tail.equals(head.x + 1, head.y)) {
            return new Point(1, 0);
        }
        throw new IllegalArgumentException("Head and tail not on the same line");
    }

    public static Point find(Collection<Point> points, Point p) {
        for(var point: points) {
            if(point.equals(p)) {
                return point;
            }
        }
        return null;
    }
}
