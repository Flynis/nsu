package ru.dyakun.snake.model.entity;

import ru.dyakun.snake.protocol.Direction;

import java.util.List;

public class Snake implements SnakeView {
    public enum State {
        ALIVE,
        ZOMBIE
    }

    private final List<Point> points;
    private Direction direction;
    private boolean needRotate;
    private State state;
    private final int playerId;

    public Snake(List<Point> points, int playerId) {
        if(points.size() != 2) {
            throw new IllegalArgumentException("Snake must have only 2 points");
        }
        this.points = points;
        this.playerId = playerId;
        this.state = State.ALIVE;
        this.direction = determineDirection(points.get(1), points.get(0));
        this.needRotate = false;
    }

    @Override
    public List<Point> points() {
        return points;
    }

    public Direction direction() {
        return direction;
    }

    public void setDirection(Direction direction) {
        if(this.direction != direction) {
            this.direction = direction;
            this.needRotate = true;
        }
    }

    public State state() {
        return state;
    }

    public void setStateZombie() {
        this.state = State.ZOMBIE;
    }

    public int playerId() {
        return playerId;
    }

    @Override
    public Point getHead() {
        return points.get(points.size() - 1);
    }

    @Override
    public Point getTail() {
        return points.get(0);
    }

    public Point moveHead() {
        Point head = getHead();
        if(needRotate) {
            Point newHead = new Point(head.x, head.y);
            newHead.move(direction);
            points.add(newHead);
            needRotate = false;
        } else {
            head.move(direction);
        }
        return getHead();
    }

    public void moveTail() {
        Point tail = points.get(0);
        Point next = points.get(1);
        if(tail.x == next.x) {
            if(Math.abs(tail.y - next.y) == 1 && points.size() > 2) {
                points.remove(0);
            } else {
                if(tail.y > next.y) {
                    tail.y--;
                } else {
                    tail.y++;
                }
            }
        } else if(tail.y == next.y) {
            if(Math.abs(tail.x - next.x) == 1 && points.size() > 2) {
                points.remove(0);
            } else {
                if(tail.x > next.x) {
                    tail.x--;
                } else {
                    tail.x++;
                }
            }
        } else {
            throw new IllegalStateException("At least one coordinate must coincide between tail and next");
        }
    }

    private static Direction determineDirection(Point head, Point tail) {
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
