package ru.dyakun.snake.game.entity;

import ru.dyakun.snake.protocol.Direction;
import ru.dyakun.snake.protocol.GameState;

public class Point {
    public int x;
    public int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point p) {
        this(p.x, p.y);
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof Point p) {
            return this.x == p.x && this.y == p.y;
        } else {
            return false;
        }
    }

    public boolean equals(int x, int y) {
        return this.x == x && this.y == y;
    }

    public boolean isZero() {
        return x == 0 && y == 0;
    }

    public void add(Point p) {
        this.x += p.x;
        this.y += p.y;
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

    public void moveReverse(Point p) {
        if(p.x != 0 && p.y == 0) {
            if(p.x > 0) {
                x--;
            } else {
                x++;
            }
        }
        if(p.y != 0 && p.x == 0) {
            if(p.y > 0) {
                y--;
            } else {
                y++;
            }
        }
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

    public void incRelativeDisplacement() {
        if(x != 0 && y == 0) {
            if(x > 0) {
                x++;
            } else {
                x--;
            }
        }
        if(y != 0 && x == 0) {
            if(y > 0) {
                y++;
            } else {
                y--;
            }
        }
    }

    public void decRelativeDisplacement() {
        if(x != 0 && y == 0) {
            if(x > 0) {
                x--;
            } else {
                x++;
            }
        }
        if(y != 0 && x == 0) {
            if(y > 0) {
                y--;
            } else {
                y++;
            }
        }
    }

    public static Point from(GameState.Coord coord) {
        return new Point(coord.getX(), coord.getY());
    }
}
