package ru.dyakun.snake.game.entity;

import ru.dyakun.snake.game.field.GameField;

import java.util.Collection;

public interface GameStateView {
    Collection<PlayerView> getGamePlayers();
    Collection<SnakeView> getSnakesView();
    GameField getField();
}
