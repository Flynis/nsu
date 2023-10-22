package ru.dyakun.snake.model;

public class Game {
    private final GameConfig config;
    private Field field;
    public Game(GameConfig config) {
        this.config = config;
    }

    public void createGame() {
        field = new Field(config.getWidth(), config.getHeight(), config.getFoodStatic());
    }
}
