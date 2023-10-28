package ru.dyakun.snake.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.controller.SceneManager;
import ru.dyakun.snake.gui.base.SceneFactory;
import ru.dyakun.snake.gui.base.SceneNames;
import ru.dyakun.snake.model.entity.Player;
import ru.dyakun.snake.model.event.GameEvent;
import ru.dyakun.snake.model.event.GameEventListener;
import ru.dyakun.snake.model.timer.GameTimer;
import ru.dyakun.snake.model.util.MessageType;
import ru.dyakun.snake.model.util.MessageUtils;
import ru.dyakun.snake.net.*;
import ru.dyakun.snake.protocol.Direction;
import ru.dyakun.snake.protocol.GameMessage;
import ru.dyakun.snake.protocol.NodeRole;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class GameController implements GameMessageListener {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    private final SceneManager manager;
    private final ActiveGames activeGames;
    private final MessageReceiver announcementReceiver;
    private final NetClient client;
    private final GameConfig initialConfig;
    private final GameTimer timer;
    private final List<GameEventListener> listeners = new ArrayList<>();
    private PlayersStatusTracker tracker;
    private GameState state;
    private boolean connected = false;
    private InetSocketAddress masterAddress;
    private GameState.Builder gameStateBuilder;

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
        listeners.add(listener);
    }

    private void notifyListeners(GameEvent event, Object payload) {
        for(var listener : listeners) {
            listener.onEvent(event, payload);
        }
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
            masterAddress = gameInfo.findMaster().getAddress();
            gameStateBuilder = new GameState.Builder(nickname, role, gameInfo);
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
        tracker = new PlayersStatusTracker(initialConfig.getDelay() * 4 / 5);
        timer.startPlayersStatusTrack(tracker, state, client);
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
        gameStateBuilder = null;
        state = null;
        masterAddress = null;
        tracker = null;
        timer.cancelGameStateUpdate();
        timer.cancelPing();
        manager.changeScene(SceneNames.MENU);
    }

    @Override
    public void handle(GameMessage message, InetSocketAddress sender) {
        try {
            if(message.hasAck()) {
                handleAckMessage(message, sender);
                return;
            } else if(message.hasError()) {
                handleErrorMessage(message);
                return;
            }
            if(!connected) {
                return;
            }
            if(message.hasState()) {
                handleStateMessage(message);
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
        if(state.isMaster()) {
            var player = state.findPlayerByAddress(sender);
            if(player != null) {
                tracker.updateStatus(player.getId());
            }
        }
    }

    private void handlerDiscoverMessage(InetSocketAddress sender) {
        if(state != null && state.isMaster()) {
            var message = MessageUtils.announcementMessage(state.getGameInfo());
            client.send(MessageType.ANNOUNCEMENT, message, sender);
        }
    }

    private void handleErrorMessage(GameMessage message) {
        if(!connected || !state.isMaster()) {
            var errorMsg = message.getError();
            notifyListeners(GameEvent.MESSAGE, errorMsg.getErrorMessage());
        }
    }

    private void handleAckMessage(GameMessage message, InetSocketAddress sender) {
        if(!connected) {
            gameStateBuilder.setId(message.getReceiverId());
            manager.changeScene(SceneNames.GAME);
            connected = true;
            notifyListeners(GameEvent.CLEAR_FIELD, null);
            tracker = new PlayersStatusTracker(gameStateBuilder.getGameInfo().getConfig().getDelay() * 4 / 5);
            timer.startPlayersStatusTrack(tracker, state, client);
            int period = gameStateBuilder.getGameInfo().getConfig().getDelay() / 2;
            timer.startPing(masterAddress, period, client);
        } else {
            var player = state.findPlayerByAddress(sender);
            if(player != null) {
                tracker.updateStatus(player.getId());
            }
        }
    }

    private void handleSteerMessage(GameMessage message, InetSocketAddress sender) {
        if(!state.isMaster()) {
            return;
        }
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
        tracker.updateStatus(id);
    }

    private void handleJoinMessage(GameMessage message, InetSocketAddress sender) {
        if(!state.isMaster()) {
            return;
        }
        var join = message.getJoin();
        try {
            var player = state.addPlayer(join.getPlayerName(), join.getRequestedRole(), sender);
            var ack = MessageUtils.ackMessage(0, player.getId());
            client.send(MessageType.ACK, ack, sender);
            tracker.updateStatus(player.getId());
            if(player.getRole() == NodeRole.NORMAL && !state.hasDeputy()) {
                var roleChange = MessageUtils.roleChangeMessage(
                        NodeRole.MASTER,
                        NodeRole.DEPUTY,
                        state.getCurrentPlayer().getId(),
                        player.getId());
                client.send(MessageType.ROLE_CHANGE, roleChange, player.getAddress());
            }
        } catch (PlayerJoinException e) {
            var errorMessage = MessageUtils.errorMessage(e.getMessage());
            client.send(MessageType.ERROR, errorMessage, sender);
        }
    }

    private void handleRoleChangeMessage(GameMessage message, InetSocketAddress sender) {
        var roleChange = message.getRoleChange();
        if(state.isMaster()) {

        } else {

        }

    }

    private void handleStateMessage(GameMessage message) {
        if(state.isMaster()) {
            return;
        }
        tracker.updateStatus(state.getMaster().getId());
        var stateMsg = message.getState();
        if(state == null) {
            state = gameStateBuilder.build(stateMsg.getState());
            gameStateBuilder = null;
        } else {
            state.updateBy(stateMsg.getState());
        }
        notifyListeners(GameEvent.REPAINT_FIELD, null);
    }
}
