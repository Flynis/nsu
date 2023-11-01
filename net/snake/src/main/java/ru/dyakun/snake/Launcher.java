package ru.dyakun.snake;

import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.game.ClientConfig;
import ru.dyakun.snake.game.Game;
import ru.dyakun.snake.gui.JFXWindow;

public class Launcher extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            var clientConfig = ClientConfig.load("client.cfg", ClientConfig.class);
            logger.info("Load client config: {}", clientConfig);
            var game = new Game(clientConfig);
            new JFXWindow(stage, game);
        } catch (Exception e) {
            logger.error("Fatal error", e);
        }
    }
}
