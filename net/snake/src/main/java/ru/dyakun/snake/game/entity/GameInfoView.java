package ru.dyakun.snake.game.entity;

import ru.dyakun.snake.game.GameConfig;

public interface GameInfoView {
    int getPlayersCount();
    GameConfig getConfig();
    String getName();
    String getIp();
}
