package ru.dyakun.snake.game.entity;

import ru.dyakun.snake.game.util.Points;
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

    public enum Axis {
        X, Y;

        public static Axis fromDirection(Direction direction) {
            switch (direction) {
                case LEFT, RIGHT -> {
                    return X;
                }
                case UP, DOWN -> {
                    return Y;
                }
                default -> throw new IllegalArgumentException("Unknown direction");
            }
        }

        public static Axis fromPoint(Point p) {
            if(p.x != 0 && p.y == 0) {
                return X;
            }
            if(p.y != 0 && p.x == 0) {
                return Y;
            }
            throw new IllegalArgumentException("Point is not a vector direction");
        }
    }

    private final Deque<Point> points;
    private Direction direction;
    private State state;
    private final int playerId;
    private final Point tail;

    public Snake(Point head, Point tail, int playerId) {
        this.points = new ArrayDeque<>();
        this.tail = tail;
        points.addLast(head);
        var relativeDisplacement = Points.getRelativeDisplacement(head, tail);
        points.addLast(relativeDisplacement);
        this.playerId = playerId;
        this.state = State.ALIVE;
        this.direction = Points.determineDirection(head, tail);
    }

    public Snake(Deque<Point> points, Direction direction, State state, int playerId) {
        this.points = points;
        this.direction = direction;
        this.state = state;
        this.playerId = playerId;
        this.tail = findTail();
    }

    @Override
    public String toString() {
        return String.format("Snake{%d, points: %s}", playerId, points.toString());
    }

    public Collection<Point> points() {
        return points;
    }

    public Direction direction() {
        return direction;
    }

    public void setDirection(Direction direction) {
        if(this.direction != direction && direction != Direction.getReverse(this.direction)) {
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
    public Iterator<Point> iterator() {
        return new SnakeIterator(points);
    }

    @Override
    public Point getHead() {
        return points.peekFirst();
    }

    public Point getTail() {
        return tail;
    }

    private Point findTail() {
        if(points.isEmpty()) {
            throw new IllegalStateException("Points is empty");
        }
        Point p = new Point(points.peekFirst());
        for (var point : points) {
            p.add(point);
        }
        return p;
    }

    public Point moveHead() {
        Point head = points.pollFirst();
        Point prev = points.peekFirst();
        if(head == null || prev == null) {
            throw new IllegalStateException("Snake is empty");
        }
        Point newHead = new Point(head.x, head.y);
        newHead.move(direction);
        if(Axis.fromDirection(direction) == Axis.fromPoint(prev)) {
            prev.incRelativeDisplacement();
            head.move(direction);
            points.addFirst(head);
        } else {
            points.addFirst(Points.getRelativeDisplacement(newHead, head));
            points.addFirst(newHead);
        }
        return newHead;
    }

    public void moveTail() {
        var tail = points.peekLast();
        if(tail == null) {
            throw new IllegalStateException("Snake is empty");
        }
        this.tail.moveReverseByVector(tail);
        tail.decRelativeDisplacement();
        if(tail.isZero()) {
            points.pollLast();
        }
    }

    public void modPoints(int width, int height) {
        Point head = points.peekFirst();
        if(head == null) {
            throw new IllegalStateException("Snake is empty");
        }
        head.mod(width, height);
        tail.mod(width, height);
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

    public static class SnakeIterator implements Iterator<Point> {
        private final Point current;
        private final Point offset;
        private final Iterator<Point> pointIterator;
        private boolean hasNext;

        private SnakeIterator(Deque<Point> points) {
            if(points.isEmpty()) {
                throw new IllegalArgumentException("Snake is empty");
            }
            pointIterator = points.iterator();
            current = new Point(pointIterator.next());
            current.add(0 , 1);
            offset = new Point(0, -1);
            hasNext = true;
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public Point next() {
            current.moveByVector(offset);
            offset.decRelativeDisplacement();
            if(offset.isZero()) {
                if(pointIterator.hasNext()) {
                    offset.set(pointIterator.next());
                } else {
                    hasNext = false;
                }
            }
            return current;
        }
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
            points.addLast(p);
        }

        public Snake build() {
            return new Snake(points, direction, state, playerId);
        }
    }
}
