package ru.dyakun.snake.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.controller.SceneManager;
import ru.dyakun.snake.gui.base.SceneFactory;
import ru.dyakun.snake.gui.base.SceneNames;
import ru.dyakun.snake.model.event.GameEventListener;
import ru.dyakun.snake.model.timer.GameTimer;
import ru.dyakun.snake.model.util.MessageType;
import ru.dyakun.snake.model.util.MessageUtils;
import ru.dyakun.snake.net.*;
import ru.dyakun.snake.protocol.Direction;
import ru.dyakun.snake.protocol.GameMessage;
import ru.dyakun.snake.protocol.NodeRole;

import java.net.InetSocketAddress;
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
    private boolean connected = false;
    private InetSocketAddress masterAddress;

    public GameController(SceneFactory factory, SceneManager manager, ClientConfig config, GameConfig initialConfig) {
        this.manager = manager;
        this.initialConfig = initialConfig;
        this.activeGames = new ActiveGames(config.getAnnouncementTimeToLive());
        var groupAddress = new InetSocketAddress(
                config.getMulticastGroupAddress(),
                config.getMulticastGroupPort());
        client = new UdpNetClient();
        client.addMessageListener(activeGames);
        client.addMessageListener(this);
        new Thread(client).start();
        announcementReceiver = new MulticastMessageReceiver(groupAddress);
        announcementReceiver.addMessageListener(activeGames);
        announcementReceiver.addMessageListener(this);
        new Thread(announcementReceiver).start();
        timer = new GameTimer(activeGames, config.getAnnouncementPeriod(), groupAddress);
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
            var gameInfo = activeGames.getByName(game);
            var masterAddress = gameInfo.findMaster().getAddress();
            var message = MessageUtils.joinMessage(nickname, game, role);
            client.send(MessageType.JOIN, message, masterAddress);
        }
        throw new IllegalArgumentException("Illegal role");
    }

    public void createGame(String nickname) {
        if(nickname.isBlank()) {
            throw new IllegalArgumentException("Nickname is empty");
        }
        state = new GameState(initialConfig, nickname);
        timer.startGameStateUpdate(state, initialConfig.getDelay(), client);
        connected = true;
        manager.changeScene(SceneNames.GAME);
    }

    public void changeSnakeDirection(Direction direction) {
        if(manager.getCurrentScene() != SceneNames.GAME) {
            throw new IllegalStateException("Game not started");
        }
        var player = state.getCurrentPlayer();
        if(player.getRole() == NodeRole.MASTER) {
            if(!state.isUpdating()) {
                state.changeSnakeDirection(state.getCurrentPlayer().getId(), direction);
            }
        } else {
            var steer = MessageUtils.steerMessage(direction, player.getId());
            client.send(MessageType.STEER, steer, masterAddress);
        }
    }

    public void back() {
        connected = false;
        timer.cancelGameStateUpdate();
        manager.changeScene(SceneNames.MENU);
    }

    @Override
    public void handle(GameMessage message, InetSocketAddress sender) {
        if(!connected) {
            return;
        }
        try {
            if(message.hasAck()) {

            } else if(message.hasError()) {

            } else if(message.hasState()) {

            } else if(message.hasDiscover()) {
                handlerDiscoverMessage(sender);
            } else if(message.hasRoleChange()) {

            } else if(message.hasJoin()) {
                handleJoinMessage(message, sender);
            } else if(message.hasPing()) {
                handlePingMessage(sender);
            } else if(message.hasSteer()) {
                handleSteerMessage(message, sender);
            }
        } catch (Exception e) {
            logger.error("Message handle failed", e);
        }
    }

    private void handlePingMessage(InetSocketAddress sender) {
        state.updateActivePlayer(sender);
    }

    private void handlerDiscoverMessage(InetSocketAddress sender) {
        if(state != null && state.getCurrentPlayer().getRole() == NodeRole.MASTER) {
            var message = MessageUtils.announcementMessage(state.getGameInfo());
            client.send(MessageType.ANNOUNCEMENT, message, sender);
        }
    }

    private void handleErrorMessage(GameMessage message, InetSocketAddress sender) {
        // TODO impl
    }

    private void handleAckMessage(GameMessage message, InetSocketAddress sender) {
        // TODO impl
    }

    private void handleSteerMessage(GameMessage message, InetSocketAddress sender) {
        var steer = message.getSteer();
        int id;
        if(!message.hasSenderId()) {
            logger.error("Steer message without sender id");
            var player = state.findPlayerByAddress(sender);
            if(player == null) {
                return;
            }
            id = player.getId();
        } else {
            id = message.getSenderId();
        }
        state.changeSnakeDirection(id, steer.getDirection());
    }

    private void handleJoinMessage(GameMessage message, InetSocketAddress sender) {
        var join = message.getJoin();
        try {
            var player = state.addPlayer(join.getPlayerName(), join.getRequestedRole(), sender);
            var ack = MessageUtils.ackMessage(0, player.getId());
            client.send(MessageType.ACK, ack, sender);
        } catch (PlayerJoinException e) {
            var errorMessage = MessageUtils.errorMessage(e.getMessage());
            client.send(MessageType.ERROR, errorMessage, sender);
        }
    }

    private void handleRoleChangeMessage(GameMessage message, InetSocketAddress sender) {
        // TODO impl
    }

    private void handleStateMessage(GameMessage message, InetSocketAddress sender) {
        // TODO impl
    }
}
