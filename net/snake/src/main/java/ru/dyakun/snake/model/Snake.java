package ru.dyakun.snake.model;

import java.util.List;

class Snake {
    public enum State {
        ALIVE,
        ZOMBIE
    }

    public final List<Point> points;
    public Direction direction;
    public State state;
    public final int playerId;

    public Snake(List<Point> points, int playerId) {
        this.points = points;
        this.playerId = playerId;
        this.state = State.ALIVE;
        this.direction = Direction.RIGHT;
    }
}
