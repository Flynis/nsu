package ru.dyakun.snake.gui.base;

import ru.dyakun.snake.controller.GameController;

public interface SceneFactory {
    Scene create(SceneNames name, GameController controller);
}
