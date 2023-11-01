package ru.dyakun.snake.controller;

import javafx.event.ActionEvent;
import ru.dyakun.snake.gui.SceneName;
import ru.dyakun.snake.game.event.GameEvent;

public class MenuController extends AbstractController {
    public void exitClick(ActionEvent ignored) {
        manager.close();
    }

    public void createGameClick(ActionEvent ignored) {
        manager.changeScene(SceneName.CREATE);
    }

    public void connectClick(ActionEvent ignored) {
        manager.changeScene(SceneName.CONNECT);
    }

    @Override
    public void onEvent(GameEvent event, Object payload) {}
}
