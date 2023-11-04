package ru.dyakun.snake.gui.scene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.game.Game;

import java.util.EnumMap;
import java.util.Map;

public class JFXSceneManager implements SceneManager {
    private static final Logger logger = LoggerFactory.getLogger(JFXSceneManager.class);
    private final Map<SceneName, Scene> scenes = new EnumMap<>(SceneName.class);
    private final Game game;
    private final Window window;
    private SceneName current;

    public JFXSceneManager(Game game, SceneFactory factory, Window window) {
        this.game = game;
        this.window = window;
        for(var sceneName : SceneName.values()) {
            scenes.put(sceneName, factory.create(sceneName, game, this));
        }
        changeScene(SceneName.MENU);
        window.show();
    }

    @Override
    public void changeScene(SceneName name) {
        logger.info("Change scene to {}", name);
        current = name;
        window.setScene(scenes.get(name));
    }

    @Override
    public SceneName current() {
        return current;
    }

    @Override
    public void exit() {
        game.terminate();
        window.close();
    }
}
