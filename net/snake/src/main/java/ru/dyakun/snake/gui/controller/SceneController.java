package ru.dyakun.snake.gui.controller;

import ru.dyakun.snake.game.Game;
import ru.dyakun.snake.game.event.GameEventListener;
import ru.dyakun.snake.gui.scene.SceneManager;

public interface SceneController extends GameEventListener {
    void init(Game controller, SceneManager manager);
}
