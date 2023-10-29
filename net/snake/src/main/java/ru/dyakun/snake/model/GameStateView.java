package ru.dyakun.snake.model;

import ru.dyakun.snake.model.entity.PlayerView;
import ru.dyakun.snake.model.field.GameField;

import java.util.Collection;

public interface GameStateView {
    Collection<PlayerView> getGamePlayers();
    GameField getField();
}
