package ru.dyakun.snake.gui.javafx;

import ru.dyakun.snake.game.Game;
import ru.dyakun.snake.gui.base.SceneName;
import ru.dyakun.snake.gui.base.Window;

public class JFXSceneFactory {
    private JFXSceneFactory() {
        throw new AssertionError();
    }
    public static JFXScene create(SceneName name, Game controller, Window window) {
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
