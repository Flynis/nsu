package ru.dyakun.snake.gui.scene;

import ru.dyakun.snake.game.Game;

public class JFXSceneFactory implements SceneFactory {
    @Override
    public JFXScene create(SceneName name, Game controller, SceneManager manager) {
        switch (name) {
            case GAME -> {
                return JFXScene.fromFXML("/scenes/GameScene.fxml", controller, manager);
            }
            case MENU -> {
                return JFXScene.fromFXML("/scenes/MenuScene.fxml", controller, manager);
            }
            case CONNECT -> {
                return JFXScene.fromFXML("/scenes/ConnectScene.fxml", controller, manager);
            }
            case CREATE -> {
                return JFXScene.fromFXML("/scenes/CreateScene.fxml", controller, manager);
            }
            default -> throw new IllegalArgumentException("Unknown scene name");
        }
    }
}
