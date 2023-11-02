package ru.dyakun.snake.gui.scene;

import ru.dyakun.snake.game.Game;

public interface SceneFactory {
    Scene create(SceneName name, Game controller, SceneManager manager);
}
