package ru.dyakun.snake.gui.scene;

public interface SceneManager {
    void changeScene(SceneName name);
    SceneName current();
    void exit();
}
