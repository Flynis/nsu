package ru.dyakun.snake.model.field;

import ru.dyakun.snake.protocol.Direction;

import java.util.List;

public class Snake {
    public enum State {
        ALIVE,
        ZOMBIE
    }

    public final List<Point> points;
    public Direction direction;
    public boolean needRotate;
    public State state;
    public final int playerId;

    public Snake(List<Point> points, int playerId) {
        if(points.size() != 2) {
            throw new IllegalArgumentException("Snake must have only 2 points");
        }
        this.points = points;
        this.playerId = playerId;
        this.state = State.ALIVE;
        this.direction = Point.determineDirection(points.get(1), points.get(0));
        this.needRotate = false;
    }
}
