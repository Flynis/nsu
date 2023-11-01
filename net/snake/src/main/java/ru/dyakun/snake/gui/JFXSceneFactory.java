package ru.dyakun.snake.gui;

import ru.dyakun.snake.game.Game;

public class JFXSceneFactory {
    private JFXSceneFactory() {
        throw new AssertionError();
    }
    public static JFXScene create(SceneName name, Game controller, SceneManager window) {
        switch (name) {
            case GAME -> {
                return JFXScene.fromFXML("/scenes/GameScene.fxml", controller, window);
            }
            case MENU -> {
                return JFXScene.fromFXML("/scenes/MenuScene.fxml", controller, window);
            }
            case CONNECT -> {
                return JFXScene.fromFXML("/scenes/ConnectScene.fxml", controller, window);
            }
            case CREATE -> {
                return JFXScene.fromFXML("/scenes/CreateScene.fxml", controller, window);
            }
            default -> throw new IllegalArgumentException("Unknown scene name");
        }
    }
}
