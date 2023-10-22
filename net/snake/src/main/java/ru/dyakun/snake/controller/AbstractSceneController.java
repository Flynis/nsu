package ru.dyakun.snake.controller;

import ru.dyakun.snake.gui.base.SceneController;

public class AbstractSceneController implements SceneController {
    protected GameController controller;

    @Override
    public void setController(GameController controller) {
        if(this.controller == null) {
            this.controller = controller;
        } else {
            throw new IllegalStateException("Controller is already initialized");
        }
    }
}
