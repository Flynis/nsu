package ru.dyakun.snake.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.gui.base.Scene;
import ru.dyakun.snake.gui.base.SceneNames;
import ru.dyakun.snake.gui.base.Window;

import java.util.EnumMap;
import java.util.Map;

public class SceneManager {
    private static final Logger logger = LoggerFactory.getLogger(SceneManager.class);
    private final Window window;
    private final Map<SceneNames, Scene> scenes;
    private SceneNames current = SceneNames.MENU;

    public SceneManager(Window window) {
        this.window = window;
        scenes = new EnumMap<>(SceneNames.class);
    }

    public void addScene(SceneNames name, Scene scene) {
        scenes.put(name, scene);
    }

    public void showCurrentScene() {
        window.show();
    }

    public void changeScene(SceneNames name) {
        logger.info("Change scene to {}", name);
        current = name;
        window.changeScene(scenes.get(name));
    }

    public void exit() {
        logger.info("Close window");
        window.close();
    }

    public SceneNames getCurrentScene() {
        return current;
    }
}
