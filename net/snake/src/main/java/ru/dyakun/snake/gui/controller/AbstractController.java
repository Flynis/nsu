package ru.dyakun.snake.gui.controller;

import ru.dyakun.snake.game.Game;
import ru.dyakun.snake.gui.scene.SceneManager;

public abstract class AbstractController implements SceneController {
    protected Game game;
    protected SceneManager manager;

    @Override
    public void init(Game game, SceneManager manager) {
        if(this.game == null && this.manager == null) {
            this.game = game;
            this.manager = manager;
            game.addGameEventListener(this);
        } else {
            throw new IllegalStateException("Controller is already initialized");
        }
    }
}
