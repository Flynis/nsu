package ru.dyakun.snake.model;

import ru.dyakun.snake.model.entity.PlayerView;
import ru.dyakun.snake.model.entity.SnakeView;
import ru.dyakun.snake.model.field.GameField;

import java.util.List;

public interface GameStateView {
    List<PlayerView> getPlayers();
    PlayerView getCurrentPlayer();
    SnakeView getSnake();
    GameInfoView getGameInfo();
    GameField getField();
}
