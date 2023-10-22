package ru.dyakun.snake.gui.javafx;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.gui.base.Scene;
import ru.dyakun.snake.gui.base.Window;

public class JFXWindow implements Window {
    private static final Logger logger = LoggerFactory.getLogger(JFXWindow.class);
    private final Stage stage;

    public JFXWindow(Stage stage) {
        logger.info("Initialize window");
        this.stage = stage;
        stage.setTitle("snake");
        /*InputStream iconStream = getClass().getResourceAsStream("/icon.png");
        Image image = new Image(iconStream);
        primaryStage.getIcons().add(image);*/
    }

    @Override
    public void changeScene(Scene scene) {
        if(scene instanceof javafx.scene.Scene jfxScene) {
            stage.setScene(jfxScene);
        } else {
            throw new IllegalArgumentException("Scene isn't javafx scene");
        }
    }

    @Override
    public void show() {
        stage.show();
    }

    @Override
    public void close() {
        stage.close();
    }
}
