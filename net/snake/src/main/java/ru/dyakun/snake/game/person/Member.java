package ru.dyakun.snake.game.person;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.game.entity.GameInfo;
import ru.dyakun.snake.game.entity.GameStateView;
import ru.dyakun.snake.game.entity.PlayerView;
import ru.dyakun.snake.game.entity.SnakeView;
import ru.dyakun.snake.game.event.GameEvent;
import ru.dyakun.snake.game.event.GameEventListener;
import ru.dyakun.snake.game.timer.GameTimer;
import ru.dyakun.snake.game.util.MessageType;
import ru.dyakun.snake.game.util.Messages;
import ru.dyakun.snake.net.GameMessageListener;
import ru.dyakun.snake.net.NetClient;
import ru.dyakun.snake.protocol.Direction;
import ru.dyakun.snake.protocol.GameMessage;
import ru.dyakun.snake.protocol.NodeRole;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;

public abstract class Member implements GameMessageListener {
    protected static final Logger logger = LoggerFactory.getLogger(Member.class);
    protected final NodeRole role;
    protected final NetClient client;
    protected final GameTimer timer;
    protected final List<GameEventListener> listeners;
    protected GameInfo gameInfo;

    protected Member(MemberParams params, NodeRole role, GameInfo gameInfo) {
        this.role = role;
        this.client = params.client();
        this.timer = params.timer();
        this.listeners = params.listeners();
        this.gameInfo = gameInfo;
    }

    protected Member(MemberParams params, NodeRole role) {
        this(params, role, null);
        client.setTimeout(gameInfo.getConfig().getDelay() / 10);
    }

    protected void setGameInfo(GameInfo gameInfo) {
        if(this.gameInfo == null) {
            this.gameInfo = gameInfo;
            client.setTimeout(gameInfo.getConfig().getDelay() / 10);
        } else {
            throw new IllegalStateException("Game info is already assigned");
        }
    }

    protected void notifyListeners(GameEvent event, Object payload) {
        for(var listener : listeners) {
            listener.onEvent(event, payload);
        }
    }

    protected void sendAck(int from, int to, InetSocketAddress address, GameMessage message) {
        var ack = Messages.ackMessage(from, to, message.getMsgSeq());
        client.send(MessageType.ACK, ack, address);
    }

    public NodeRole getRole() {
        return role;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

    public static Member createForConnect(NodeRole role,
                                          String nickname,
                                          GameInfo gameInfo,
                                          MemberParams params,
                                          ChangeRoleListener listener) {
        switch (role) {
            case VIEWER -> {
                return new Viewer(nickname, gameInfo, params);
            }
            case NORMAL -> {
                return new Normal(nickname, gameInfo, params, listener);
            }
            default -> throw new IllegalArgumentException("Only viewer and normal can be created for connect");
        }
    }

    protected MemberParams getParams() {
        return new MemberParams(client, timer, listeners);
    }

    @Override
    public void handle(GameMessage message, InetSocketAddress sender) {
        try {
            if(message.hasState()) {
                onStateMsg(message, sender);
            } else if(message.hasRoleChange()) {
                onRoleChangeMsg(message, sender);
            } else if(message.hasPing()) {
                onPingMsg(message, sender);
            } else if(message.hasSteer()) {
                onSteerMsg(message, sender);
            } else if(message.hasAck()) {
                onAckMsg(message, sender);
            } else if(message.hasJoin()) {
                onJoinMsg(message, sender);
            } else if(message.hasError()) {
                onErrorMsg(message, sender);
            } else if(message.hasDiscover()) {
                onDiscoverMsg(message, sender);
            }
        } catch (Exception e) {
            logger.error("Person message handle failed", e);
        }
    }

    protected abstract void onStateMsg(GameMessage message, InetSocketAddress sender);
    protected abstract void onRoleChangeMsg(GameMessage message, InetSocketAddress sender);
    protected abstract void onPingMsg(GameMessage message, InetSocketAddress sender);
    protected abstract void onSteerMsg(GameMessage message, InetSocketAddress sender);
    protected abstract void onAckMsg(GameMessage message, InetSocketAddress sender);
    protected abstract void onJoinMsg(GameMessage message, InetSocketAddress sender);
    protected abstract void onErrorMsg(GameMessage message, InetSocketAddress sender);
    protected abstract void onDiscoverMsg(GameMessage message, InetSocketAddress sender);

    public abstract PlayerView getPlayer();

    public abstract SnakeView getSnake();

    public abstract GameStateView getGameState();

    public abstract void moveSnake(Direction direction);

    public abstract void onInactivePlayers(Collection<Integer> inactive);

    public abstract void exit();
}
