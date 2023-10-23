package ru.dyakun.snake.controller;

import ru.dyakun.snake.gui.base.SceneController;
import ru.dyakun.snake.model.GameController;
import ru.dyakun.snake.model.event.GameEventListener;

public abstract class AbstractSceneController implements SceneController, GameEventListener {
    protected GameController controller;

    @Override
    public void setController(GameController controller) {
        if(this.controller == null) {
            this.controller = controller;
            controller.addGameEventListener(this);
        } else {
            throw new IllegalStateException("Controller is already initialized");
        }
    }
}
