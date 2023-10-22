package ru.dyakun.snake.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.gui.base.SceneFactory;
import ru.dyakun.snake.gui.base.SceneNames;
import ru.dyakun.snake.model.Game;
import ru.dyakun.snake.model.GameConfig;

public class GameController {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    private final SceneManager manager;
    private final Game game;

    public GameController(SceneFactory factory, SceneManager manager, Game game) {
        this.manager = manager;
        this.game = game;
        manager.addScene(SceneNames.MENU, factory.create(SceneNames.MENU, this));
        manager.addScene(SceneNames.GAME, factory.create(SceneNames.GAME, this));
        manager.changeScene(SceneNames.MENU);
        manager.showCurrentScene();
    }

    void exit() {
        manager.exit();
    }

    void connect() {
        // TODO
        manager.changeScene(SceneNames.GAME);
    }

    void createGame() {
        manager.changeScene(SceneNames.GAME);
    }
}
