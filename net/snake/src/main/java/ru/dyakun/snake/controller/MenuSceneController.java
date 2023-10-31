package ru.dyakun.snake.controller;

import javafx.event.ActionEvent;
import ru.dyakun.snake.gui.base.SceneNames;
import ru.dyakun.snake.model.event.GameEvent;

public class MenuSceneController extends AbstractSceneController {
    public void exitClick(ActionEvent ignored) {
        controller.exit();
    }

    public void createGameClick(ActionEvent ignored) {
        controller.changeScene(SceneNames.CREATE);
    }

    public void connectClick(ActionEvent ignored) {
        controller.changeScene(SceneNames.CONNECT);
    }

    @Override
    public void onEvent(GameEvent event, Object payload) {}
}
