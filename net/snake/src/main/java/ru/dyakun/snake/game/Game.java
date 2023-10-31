package ru.dyakun.snake.game;

import ru.dyakun.snake.game.entity.GameInfoView;
import ru.dyakun.snake.game.entity.GameStateView;
import ru.dyakun.snake.game.entity.PlayerView;
import ru.dyakun.snake.game.entity.SnakeView;
import ru.dyakun.snake.game.event.GameEventListener;
import ru.dyakun.snake.game.person.ChangeRoleListener;
import ru.dyakun.snake.game.person.Master;
import ru.dyakun.snake.game.person.Member;
import ru.dyakun.snake.game.person.MemberParams;
import ru.dyakun.snake.game.timer.GameTimer;
import ru.dyakun.snake.game.tracker.ActiveGamesTracker;
import ru.dyakun.snake.net.*;
import ru.dyakun.snake.protocol.Direction;
import ru.dyakun.snake.protocol.GameMessage;
import ru.dyakun.snake.protocol.NodeRole;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class Game implements GameMessageListener, ChangeRoleListener {
    private final ActiveGamesTracker activeGamesTracker;
    private final MessageReceiver announcementReceiver;
    private final NetClient client;
    private final GameConfig initialConfig;
    private final GameTimer timer;
    private final MemberParams params;
    private final List<GameEventListener> listeners = new ArrayList<>();
    private Member member;

    public Game(ClientConfig config, GameConfig initialConfig) {
        this.initialConfig = initialConfig;
        this.activeGamesTracker = new ActiveGamesTracker(config.getAnnouncementTimeToLive());
        var groupAddress = new InetSocketAddress(
                config.getMulticastGroupAddress(),
                config.getMulticastGroupPort());
        client = new UdpNetClient();
        client.addMessageListener(activeGamesTracker);
        client.addMessageListener(this);
        new Thread(client).start();
        announcementReceiver = new MulticastMessageReceiver(groupAddress);
        announcementReceiver.addMessageListener(activeGamesTracker);
        announcementReceiver.addMessageListener(this);
        new Thread(announcementReceiver).start();
        timer = new GameTimer(activeGamesTracker, config.getAnnouncementPeriod(), groupAddress);
        params = new MemberParams(client, timer, listeners);
    }

    public List<GameInfoView> getActiveGames() {
        return activeGamesTracker.getActiveGames();
    }

    public GameStateView getGameState() {
        return member.getGameState();
    }

    public GameInfoView getGameInfo() {
        return member.getGameInfo();
    }

    public PlayerView getPlayer() {
        return member.getPlayer();
    }

    public SnakeView getSnake() {
        return member.getSnake();
    }

    public void addGameEventListener(GameEventListener listener) {
        if(member != null) {
            throw new IllegalStateException("Game already started");
        }
        activeGamesTracker.addGameEventListener(listener);
        timer.addGameEventListener(listener);
        listeners.add(listener);
    }

    public void terminate() {
        client.stop();
        timer.cancel();
        announcementReceiver.stop();
    }

    public void connect(String nickname, String game, NodeRole role) {
        if(nickname.isBlank()) {
            throw new IllegalArgumentException("Nickname is empty");
        }
        if(game.isBlank()) {
            throw new IllegalArgumentException("Game name is empty");
        }
        if(role == NodeRole.NORMAL || role == NodeRole.VIEWER) {
            var gameInfo = activeGamesTracker.getByName(game);
            if(gameInfo == null) {
                throw new IllegalStateException("Game info not found for " + game);
            }
            member = Member.createForConnect(role, nickname, gameInfo, params, this);
        }
        throw new IllegalArgumentException("Illegal role");
    }

    public void createGame(String nickname) {
        if(nickname.isBlank()) {
            throw new IllegalArgumentException("Nickname is empty");
        }
        member = new Master(nickname, initialConfig, params);
    }

    public void moveSnake(Direction direction) {
        if(member == null) {
            throw new IllegalStateException("Game not started");
        }
        member.moveSnake(direction);
    }

    public void finishCurrentSession() {
        member.exit();
        member = null;
    }

    @Override
    public void handle(GameMessage message, InetSocketAddress sender) {
        member.handle(message, sender);
    }

    @Override
    public void onChange(Member newRole) {
        member = newRole;
    }
}
