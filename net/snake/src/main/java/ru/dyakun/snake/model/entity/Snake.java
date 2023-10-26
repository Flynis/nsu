package ru.dyakun.snake.model.entity;

import ru.dyakun.snake.protocol.Direction;

import java.util.*;

public class Snake implements SnakeView {
    public enum State {
        ALIVE(0),
        ZOMBIE(1),
        DESTROYED(2);

        private final int code;

        public int getCode() {
            return code;
        }

        State(int code) {
            this.code = code;
        }
    }

    private final Deque<Point> points;
    private Direction direction;
    private State state;
    private final int playerId;

    public Snake(Point head, Point tail, int playerId) {
        this.points = new ArrayDeque<>();
        points.addLast(head);
        points.addLast(tail);
        this.playerId = playerId;
        this.state = State.ALIVE;
        this.direction = determineDirection(head, tail);
    }

    @Override
    public Collection<Point> points() {
        return points;
    }

    public Direction direction() {
        return direction;
    }

    public void setDirection(Direction direction) {
        if(this.direction != direction || direction == Direction.getReverse(this.direction)) {
            this.direction = direction;
        }
    }

    public State state() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int playerId() {
        return playerId;
    }

    @Override
    public Point getHead() {
        return points.peekFirst();
    }

    @Override
    public Point getTail() {
        return points.peekLast();
    }

    public Point moveHead() {
        Point head = getHead();
        Point newHead = new Point(head.x, head.y);
        newHead.move(direction);
        points.addFirst(newHead);
        return newHead;
    }

    public void moveTail() {
        points.pollLast();
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
