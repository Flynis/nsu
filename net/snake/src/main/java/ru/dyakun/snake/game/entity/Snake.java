package ru.dyakun.snake.game.entity;

import ru.dyakun.snake.protocol.Direction;
import ru.dyakun.snake.protocol.GameState;

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

    public Snake(Deque<Point> points, Direction direction, State state, int playerId) {
        this.points = points;
        this.direction = direction;
        this.state = state;
        this.playerId = playerId;
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

    public static Snake from(GameState.Snake snake) {
        var builder = new Builder(snake.getPlayerId())
                .direction(snake.getHeadDirection())
                .state(snake.getState());
        for(int i = 0; i < snake.getPointsCount(); i++) {
            builder.addPoint(Point.from(snake.getPoints(i)));
        }
        return builder.build();
    }

    public static class Builder {
        private final Deque<Point> points = new ArrayDeque<>();
        private Direction direction;
        private State state;
        private final int playerId;

        public Builder(int playerId) {
            this.playerId = playerId;
        }

        public Builder direction(Direction direction) {
            this.direction = direction;
            return this;
        }

        public Builder state(GameState.Snake.SnakeState state) {
            switch (state) {
                case ALIVE -> this.state = State.ALIVE;
                case ZOMBIE -> this.state = State.ZOMBIE;
            }
            return this;
        }

        public void addPoint(Point p) {
            points.addFirst(p);
        }

        public Snake build() {
            return new Snake(points, direction, state, playerId);
        }
    }
}
