package ru.dyakun.snake.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.gui.base.SceneFactory;
import ru.dyakun.snake.gui.base.SceneNames;
import ru.dyakun.snake.model.GameConfig;

public class GameController {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    private final SceneManager manager;

    public GameController(SceneFactory factory, SceneManager manager, GameConfig config) {
        this.manager = manager;
        manager.addScene(SceneNames.MENU, factory.create(SceneNames.MENU, this));
        manager.addScene(SceneNames.GAME, factory.create(SceneNames.GAME, this));
        manager.changeScene(SceneNames.MENU);
        manager.showCurrentScene();
        // TODO config pass
    }

    void exit() {
        manager.exit();
    }

    void connect() {
        manager.changeScene(SceneNames.GAME);
    }

    void createGame() {
        manager.changeScene(SceneNames.GAME);
    }
}
