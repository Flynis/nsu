package ru.dyakun.snake.controller;

import javafx.event.ActionEvent;
import ru.dyakun.snake.gui.base.SceneName;
import ru.dyakun.snake.game.event.GameEvent;

public class MenuController extends AbstractController {
    public void exitClick(ActionEvent ignored) {
        window.close();
    }

    public void createGameClick(ActionEvent ignored) {
        window.changeScene(SceneName.CREATE);
    }

    public void connectClick(ActionEvent ignored) {
        window.changeScene(SceneName.CONNECT);
    }

    @Override
    public void onEvent(GameEvent event, Object payload) {}
}
