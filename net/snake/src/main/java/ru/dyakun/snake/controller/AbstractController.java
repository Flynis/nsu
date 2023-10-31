package ru.dyakun.snake.controller;

import ru.dyakun.snake.game.Game;
import ru.dyakun.snake.gui.base.Window;

public abstract class AbstractController implements SceneController {
    protected Game game;
    protected Window window;

    @Override
    public void setController(Game game, Window window) {
        if(this.game == null && this.window == null) {
            this.game = game;
            this.window = window;
            game.addGameEventListener(this);

        } else {
            throw new IllegalStateException("Controller is already initialized");
        }
    }
}
