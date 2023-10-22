package ru.dyakun.snake.gui.javafx;

import ru.dyakun.snake.controller.GameController;
import ru.dyakun.snake.gui.base.Scene;
import ru.dyakun.snake.gui.base.SceneFactory;
import ru.dyakun.snake.gui.base.SceneNames;

public class JFXSceneFactory implements SceneFactory {
    @Override
    public Scene create(SceneNames name, GameController controller) {
        switch (name) {
            case GAME -> {
                return JFXScene.fromFXML("/scenes/GameScene.fxml", controller);
            }
            case MENU -> {
                return JFXScene.fromFXML("/scenes/MenuScene.fxml", controller);
            }
            default -> throw new IllegalArgumentException("Unknown scene name");
        }
    }
}
