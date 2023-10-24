package ru.dyakun.snake.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.controller.SceneManager;
import ru.dyakun.snake.gui.base.SceneFactory;
import ru.dyakun.snake.gui.base.SceneNames;
import ru.dyakun.snake.model.event.GameEventListener;
import ru.dyakun.snake.model.timer.GameTimer;
import ru.dyakun.snake.net.*;
import ru.dyakun.snake.protocol.Direction;
import ru.dyakun.snake.protocol.GameMessage;
import ru.dyakun.snake.protocol.NodeRole;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.List;

public class GameController implements GameMessageListener {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    private final SceneManager manager;
    private final ActiveGames activeGames;
    private final MessageReceiver announcementReceiver;
    private final NetClient client;
    private final GameConfig initialConfig;
    private final GameTimer timer;
    private GameState state;

    public GameController(SceneFactory factory, SceneManager manager, ClientConfig config, GameConfig initialConfig) {
        this.manager = manager;
        this.initialConfig = initialConfig;
        this.activeGames = new ActiveGames(config.getAnnouncementTimeToLive());
        try {
            client = new UdpNetClient();
            client.addMessageListener(this);
            new Thread(client).start();
            announcementReceiver = new MulticastMessageReceiver(
                    InetAddress.getByName(config.getMulticastGroupAddress()),
                    config.getMulticastGroupPort());
            announcementReceiver.addMessageListener(activeGames);
            announcementReceiver.addMessageListener(this);
            new Thread(announcementReceiver).start();
            // TODO refactor
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
        timer = new GameTimer(activeGames, config.getAnnouncementPeriod());
        manager.addScene(SceneNames.MENU, factory.create(SceneNames.MENU, this));
        manager.addScene(SceneNames.GAME, factory.create(SceneNames.GAME, this));
        manager.changeScene(SceneNames.MENU);
        manager.showCurrentScene();
    }

    public List<GameInfoView> getActiveGames() {
        return activeGames.getActiveGames();
    }

    public GameStateView getGameState() {
        return state;
    }

    public void addGameEventListener(GameEventListener listener) {
        activeGames.addGameEventListener(listener);
        timer.addGameEventListener(listener);
    }

    public void exit() {
        client.stop();
        timer.cancel();
        announcementReceiver.stop();
        manager.exit();
    }

    public void connect(String nickname, String game, NodeRole role) {
        if(nickname.isBlank()) {
            throw new IllegalArgumentException("Nickname is empty");
        }
        if(game.isBlank()) {
            throw new IllegalArgumentException("Game name is empty");
        }
        if(role == NodeRole.NORMAL || role == NodeRole.VIEWER) {
            // TODO connect
            manager.changeScene(SceneNames.GAME);
        }
        throw new IllegalArgumentException("Illegal role");
    }

    public void createGame(String nickname) {
        if(nickname.isBlank()) {
            throw new IllegalArgumentException("Nickname is empty");
        }
        state = new GameState(initialConfig, nickname);
        timer.startGameStateUpdate(state, initialConfig.getDelay());
        manager.changeScene(SceneNames.GAME);
    }

    public void changeSnakeDirection(Direction direction) {
        if(manager.getCurrentScene() != SceneNames.GAME) {
            throw new IllegalStateException("Game not started");
        }
        state.changeSnakeDirection(state.getPlayer().getId(), direction);
    }

    public void back() {
        timer.cancelGameStateUpdate();
        manager.changeScene(SceneNames.MENU);
    }

    @Override
    public void handle(GameMessage message, SocketAddress receiver) {
        // TODO impl
    }
}
