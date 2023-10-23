package ru.dyakun.snake.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.gui.base.SceneFactory;
import ru.dyakun.snake.gui.base.SceneNames;
import ru.dyakun.snake.model.ActiveGames;
import ru.dyakun.snake.model.GameConfig;
import ru.dyakun.snake.model.GameInfo;
import ru.dyakun.snake.model.event.GameEventListener;
import ru.dyakun.snake.net.MessageReceiver;
import ru.dyakun.snake.net.MulticastMessageReceiver;
import ru.dyakun.snake.protocol.NodeRole;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class GameController {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    private final SceneManager manager;
    private final ActiveGames activeGames;
    private final MessageReceiver announcementReceiver;
    private final GameConfig config;

    public GameController(SceneFactory factory, SceneManager manager, GameConfig config) {
        this.manager = manager;
        this.config = config;
        this.activeGames = new ActiveGames();
        try {
            announcementReceiver = new MulticastMessageReceiver(InetAddress.getByName("239.192.0.4"), 9192);
            announcementReceiver.addMessageListener(activeGames);
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
        manager.addScene(SceneNames.MENU, factory.create(SceneNames.MENU, this));
        manager.addScene(SceneNames.GAME, factory.create(SceneNames.GAME, this));
        manager.changeScene(SceneNames.MENU);
        manager.showCurrentScene();
    }

    List<GameInfo> getActiveGames() {
        return activeGames.getActiveGames();
    }

    void addGameEventListener(GameEventListener listener) {
        activeGames.addGameEventListener(listener);
    }

    void exit() {
        manager.exit();
    }

    void connect(String nickname, String game, NodeRole role) {
        if(nickname.isBlank()) {
            throw new IllegalArgumentException("Nickname is empty");
        }
        if(game.isBlank()) {
            throw new IllegalArgumentException("Game name is empty");
        }
        if(role == NodeRole.NORMAL || role == NodeRole.VIEWER) {
            manager.changeScene(SceneNames.GAME);
        }
        throw new IllegalArgumentException("Illegal role");
    }

    void createGame(String nickname) {

        manager.changeScene(SceneNames.GAME);
    }
}
