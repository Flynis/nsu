package ru.dyakun.snake.controller;

import ru.dyakun.snake.game.Game;
import ru.dyakun.snake.game.event.GameEventListener;
import ru.dyakun.snake.gui.SceneManager;

public interface SceneController extends GameEventListener {
    void setController(Game controller, SceneManager manager);
}
