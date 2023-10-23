package ru.dyakun.snake.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.controller.SceneManager;
import ru.dyakun.snake.gui.base.SceneFactory;
import ru.dyakun.snake.gui.base.SceneNames;
import ru.dyakun.snake.model.event.GameEventListener;
import ru.dyakun.snake.model.field.Field;
import ru.dyakun.snake.model.field.GameField;
import ru.dyakun.snake.net.MessageReceiver;
import ru.dyakun.snake.net.MulticastMessageReceiver;
import ru.dyakun.snake.protocol.Direction;
import ru.dyakun.snake.protocol.NodeRole;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class GameController {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    private final SceneManager manager;
    private final ActiveGames activeGames;
    private final MessageReceiver announcementReceiver;
    private final GameConfig config;
    private final List<GameEventListener> listeners = new ArrayList<>();
    private final Timer timer = new Timer();
    private final GameTimerRoutine timerRoutine;
    private Field field;
    private Player player;

    public GameController(SceneFactory factory, SceneManager manager, GameConfig config) {
        this.manager = manager;
        this.config = config;
        this.activeGames = new ActiveGames();
        try {
            announcementReceiver = new MulticastMessageReceiver(InetAddress.getByName("239.192.0.4"), 9192);
            announcementReceiver.addMessageListener(activeGames);
            new Thread(announcementReceiver).start();
            // TODO refactor
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
        timerRoutine = new GameTimerRoutine(activeGames);
        timer.schedule(timerRoutine, 0, config.getDelay());
        manager.addScene(SceneNames.MENU, factory.create(SceneNames.MENU, this));
        manager.addScene(SceneNames.GAME, factory.create(SceneNames.GAME, this));
        manager.changeScene(SceneNames.MENU);
        manager.showCurrentScene();
    }

    public List<GameInfo> getActiveGames() {
        return activeGames.getActiveGames();
    }

    public GameField getField() {
        return field;
    }

    public void addGameEventListener(GameEventListener listener) {
        activeGames.addGameEventListener(listener);
        listeners.add(listener);
    }

    public void exit() {
        // TODO correct exit
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
        try {
            field = new Field(config.getWidth(), config.getHeight(), config.getFoodStatic());
            for(var listener : listeners) {
                field.addGameEventListener(listener);
            }
            int id = field.createSnake();
            timerRoutine.changeField(field);
            logger.debug("Field change request");
            player = new Player.Builder(nickname, id).role(NodeRole.MASTER).score(0).build();
            manager.changeScene(SceneNames.GAME);
        } catch (SnakeCreateException e) {
            throw new IllegalStateException(e);
        }
    }

    public void changeSnakeDirection(Direction direction) {
        if(manager.getCurrentScene() != SceneNames.GAME) {
            throw new IllegalStateException("Game not started");
        }
        field.changeSnakeDirection(player.getId(), direction);
    }

    public void back() {
        // TODO
        manager.changeScene(SceneNames.MENU);
    }
}
