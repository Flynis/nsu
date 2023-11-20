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

    public void set(Point p) {
        this.x = p.x;
        this.y = p.y;
    }

    public void add(Point p) {
        add(p.x, p.y);
    }

    public void add(int x, int y) {
        this.x += x;
        this.y += y;
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

    public void mod(int width, int height) {
        x = Math.floorMod(x, width);
        y = Math.floorMod(y, height);
    }

    public void moveReverseByVector(Point p) {
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

    public void moveByVector(Point p) {
        if(p.x != 0 && p.y == 0) {
            if(p.x > 0) {
                x++;
            } else {
                x--;
            }
        }
        if(p.y != 0 && p.x == 0) {
            if(p.y > 0) {
                y++;
            } else {
                y--;
            }
        }
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
