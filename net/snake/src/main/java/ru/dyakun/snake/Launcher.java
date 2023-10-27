package ru.dyakun.snake;

import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.model.ClientConfig;
import ru.dyakun.snake.model.GameController;
import ru.dyakun.snake.controller.SceneManager;
import ru.dyakun.snake.gui.javafx.JFXSceneFactory;
import ru.dyakun.snake.gui.javafx.JFXWindow;
import ru.dyakun.snake.model.GameConfig;

public class Launcher extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            var gameConfig = GameConfig.load("game.cfg", GameConfig.class);
            var clientConfig = GameConfig.load("client.cfg", ClientConfig.class);
            logger.info("Load config: {}", gameConfig);
            var sceneManager = new SceneManager(new JFXWindow(stage));
            new GameController(new JFXSceneFactory(), sceneManager, clientConfig, gameConfig);
        } catch (Exception e) {
            logger.error("Fatal error", e);
        }
    }
}