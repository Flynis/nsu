package ru.dyakun.snake.gui.scene;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import ru.dyakun.snake.game.Game;
import ru.dyakun.snake.gui.controller.SceneController;

import java.io.IOException;

public class JFXScene extends javafx.scene.Scene implements Scene {
    public JFXScene(Parent parent) {
        super(parent);
    }

    public static JFXScene fromFXML(String resourcePath, Game controller, SceneManager window) {
        try {
            FXMLLoader loader = new FXMLLoader(JFXScene.class.getResource(resourcePath));
            Parent parent = loader.load();
            SceneController sceneController = loader.getController();
            sceneController.init(controller, window);
            return new JFXScene(parent);
        } catch (IOException e) {
            throw new IllegalStateException(resourcePath, e);
        }
    }
}
