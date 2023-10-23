package ru.dyakun.snake.gui.javafx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import ru.dyakun.snake.model.GameController;
import ru.dyakun.snake.gui.base.Scene;
import ru.dyakun.snake.gui.base.SceneController;

import java.io.IOException;

public class JFXScene extends javafx.scene.Scene implements Scene {
    public JFXScene(Parent parent) {
        super(parent);
    }

    public static JFXScene fromFXML(String resourcePath, GameController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(JFXScene.class.getResource(resourcePath));
            Parent parent = loader.load();
            SceneController sceneController = loader.getController();
            sceneController.setController(controller);
            return new JFXScene(parent);
        } catch (IOException e) {
            throw new IllegalStateException(resourcePath, e);
        }
    }
}
