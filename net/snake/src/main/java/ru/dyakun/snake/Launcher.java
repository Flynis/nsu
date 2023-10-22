package ru.dyakun.snake;

import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.controller.GameController;
import ru.dyakun.snake.controller.SceneManager;
import ru.dyakun.snake.gui.javafx.JFXSceneFactory;
import ru.dyakun.snake.gui.javafx.JFXWindow;
import ru.dyakun.snake.model.Game;
import ru.dyakun.snake.model.GameConfig;

public class Launcher extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            var config = GameConfig.load("game.cfg", GameConfig.class);
            logger.info("Load config: {}", config);
            var sceneManager = new SceneManager(new JFXWindow(stage));
            new GameController(new JFXSceneFactory(), sceneManager, new Game(config));
        } catch (Exception e) {
            logger.info("Fatal error", e);
        }
    }
}