package ru.dyakun.snake.gui.scene;

import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class JFXWindow implements Window {
    private static final Logger logger = LoggerFactory.getLogger(JFXWindow.class);
    private final Stage stage;

    public JFXWindow(Stage stage) {
        logger.info("Initialize window");
        this.stage = stage;
        stage.setTitle("snake");
        InputStream iconStream = getClass().getResourceAsStream("/images/icon.png");
        if(iconStream == null) {
            logger.error("Icon not found");
            return;
        }
        Image image = new Image(iconStream);
        stage.getIcons().add(image);
    }

    @Override
    public void setScene(Scene scene) {
        if(scene instanceof JFXScene jfxScene) {
            stage.setScene(jfxScene);
        }
    }

    @Override
    public void show() {
        stage.show();
    }

    @Override
    public void close() {
        logger.info("Close window");
        stage.close();
    }
}
