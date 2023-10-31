package ru.dyakun.snake.gui.javafx;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.game.Game;
import ru.dyakun.snake.gui.base.SceneName;
import ru.dyakun.snake.gui.base.Window;

import java.util.EnumMap;
import java.util.Map;

public class JFXWindow implements Window {
    private static final Logger logger = LoggerFactory.getLogger(JFXWindow.class);
    private final Stage stage;
    private final Game game;
    private final Map<SceneName, JFXScene> scenes = new EnumMap<>(SceneName.class);

    public JFXWindow(Stage stage, Game game) {
        logger.info("Initialize window");
        this.stage = stage;
        this.game = game;
        stage.setTitle("snake");
        /*InputStream iconStream = getClass().getResourceAsStream("/icon.png");
        Image image = new Image(iconStream);
        primaryStage.getIcons().add(image);*/
        for(var sceneName : SceneName.values()) {
            scenes.put(sceneName, JFXSceneFactory.create(sceneName, game, this));
        }
        changeScene(SceneName.MENU);
        stage.show();
    }

    @Override
    public void changeScene(SceneName name) {
        logger.info("Change scene to {}", name);
        stage.setScene(scenes.get(name));
    }

    @Override
    public void close() {
        logger.info("Close window");
        game.terminate();
        stage.close();
    }
}
