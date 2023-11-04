package ru.dyakun.snake.game.person;

import ru.dyakun.snake.game.event.GameEvent;
import ru.dyakun.snake.game.*;
import ru.dyakun.snake.game.entity.*;
import ru.dyakun.snake.game.field.Field;
import ru.dyakun.snake.game.field.SnakeCreateException;
import ru.dyakun.snake.game.tracker.PlayersStatusTracker;
import ru.dyakun.snake.game.util.MessageType;
import ru.dyakun.snake.game.util.Messages;
import ru.dyakun.snake.game.util.Players;
import ru.dyakun.snake.protocol.Direction;
import ru.dyakun.snake.protocol.GameMessage;
import ru.dyakun.snake.protocol.NodeRole;
import ru.dyakun.snake.util.IdGenerator;
import ru.dyakun.snake.util.SimpleIdGenerator;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Master extends Member {
    private final Map<Integer, Player> players = new ConcurrentHashMap<>();
    private final Map<Integer, Snake> snakes = new ConcurrentHashMap<>();
    private final GameState state;
    private final Field field;
    private final int id;
    private int deputyId;
    private boolean hasDeputy = false;
    private final PlayersStatusTracker tracker;
    private final IdGenerator generator;
    private final Object lock = new Object();

    public Master(String nickname, GameConfig config, MemberParams params) {
        super(params);
        generator = new SimpleIdGenerator();
        id = generator.next();
        var player = new Player.Builder(nickname, id).role(NodeRole.MASTER).score(0).build();
        players.put(id, player);
        setGameInfo(new GameInfo.Builder(config, nickname).setPlayers(players.values()).mayJoin(true).build());
        Collection<Point> foods = new ArrayList<>();
        field = new Field(config, players, snakes, foods);
        try {
            field.createSnake(id);
            field.createFood();
        } catch (SnakeCreateException e) {
            logger.info("Place snake into empty field failed");
            throw new AssertionError();
        }
        state = new GameState(field, new GameState.EntitiesCollections(players.values(), foods, snakes.values()));
        timer.startGameStateUpdate(this, config.getDelay(), client);
        tracker = new PlayersStatusTracker((config.getDelay() * 4) / 5);
        timer.startPlayersStatusTrack(tracker, this);
        notifyListeners(GameEvent.JOINED, null);
    }

    Master(Deputy deputy) {
        super(deputy.getParams(), deputy.gameInfo);
        id = deputy.id;
        var oldState = deputy.state;
        generator = new SimpleIdGenerator(Players.maxId(oldState.getPlayers(), id));
        fillMaps(oldState.getPlayers(), oldState.getSnakes());
        var foods = oldState.getFoods();
        var config = deputy.gameInfo.getConfig();
        field = new Field(config, players, snakes, foods);
        state = new GameState(field, new GameState.EntitiesCollections(players.values(), foods, snakes.values()));
        timer.startGameStateUpdate(this, config.getDelay(), client);
        tracker = new PlayersStatusTracker((config.getDelay() * 4) / 5);
        for(var player: players.values()) {
            if(player.getId() != id) {
                tracker.updateStatus(player.getId());
            }
        }
        timer.startPlayersStatusTrack(tracker, this);
        for(var player: players.values()) {
            var roleChange = Messages.roleChangeMessage(NodeRole.MASTER, null, id, player.getId());
            client.send(MessageType.ROLE_CHANGE, roleChange, player.getAddress());
        }
        changeDeputy();
    }

    private void fillMaps(Collection<Player> players, Collection<Snake> snakes) {
        for(var player : players) {
            this.players.put(player.getId(), player);
        }
        for(var snake : snakes) {
            this.snakes.put(snake.playerId(), snake);
        }
    }

    private Player addPlayer(String nickname, NodeRole role, InetSocketAddress address) throws PlayerJoinException {
        var newPlayerId = generator.next();
        if(role != NodeRole.NORMAL && role != NodeRole.VIEWER) {
            throw new PlayerJoinException("Illegal role: " + role);
        }
        if(Players.contains(players.values(), nickname)) {
            throw new PlayerJoinException("Not unique nickname " + nickname);
        }
        try {
            var player = new Player.Builder(nickname, newPlayerId).role(role).address(address).score(0).build();
            synchronized (lock) {
                if(role == NodeRole.NORMAL) {
                    field.createSnake(newPlayerId);
                }
                players.put(newPlayerId, player);
            }
            return player;
        } catch (SnakeCreateException e) {
            throw new PlayerJoinException("No space for snake", e);
        } catch (IllegalArgumentException e) {
            throw new PlayerJoinException(e.getMessage(), e);
        }
    }

    private void changeSnakeDirection(int id, Direction direction) {
        if(!snakes.containsKey(id)) {
            return;
        }
        var snake = snakes.get(id);
        synchronized (lock) {
            snake.setDirection(direction);
        }
    }

    public GameState getState() {
        return state;
    }

    public void update() {
        var viewers = new ArrayList<Player>();
        synchronized (lock) {
            state.incStateOrder();
            field.updateField();
            for(var snake : snakes.values()) {
                if (snake.state() == Snake.State.DESTROYED) {
                    viewers.add(players.get(snake.playerId()));
                }
            }
        }
        for(var viewer: viewers) {
            snakes.remove(viewer.getId());
            if(viewer.getId() == id) {
                continue;
            }
            var roleChange = Messages.roleChangeMessage(null, NodeRole.VIEWER, id, viewer.getId());
            client.send(MessageType.ROLE_CHANGE, roleChange, viewer.getAddress());
        }
    }

    @Override
    public NodeRole getRole() {
        return NodeRole.MASTER;
    }

    @Override
    protected void onStateMsg(GameMessage message, InetSocketAddress sender) {
        logger.info("Unexpected state msg from {}", sender.getAddress().getHostAddress());
    }

    @Override
    protected void onRoleChangeMsg(GameMessage message, InetSocketAddress sender) {
        var roleChangeMsg = message.getRoleChange();
        if(roleChangeMsg.hasSenderRole() && roleChangeMsg.getSenderRole() == NodeRole.VIEWER) {
            var senderId = message.getSenderId();
            sendAck(id, senderId, sender, message);
            var snake = snakes.get(senderId);
            if(snake != null) {
                snake.setState(Snake.State.ZOMBIE);
            }
            var player = players.get(senderId);
            if(player.getRole() == NodeRole.DEPUTY) {
                changeDeputy();
            }
            player.setRole(NodeRole.VIEWER);
        }
    }

    @Override
    protected void onPingMsg(GameMessage message, InetSocketAddress sender) {
        var player = Players.findPlayerByAddress(players.values(), sender);
        if(player != null) {
            sendAck(id, player.getId(), sender, message);
            tracker.updateStatus(player.getId());
        }
    }

    @Override
    protected void onSteerMsg(GameMessage message, InetSocketAddress sender) {
        var steer = message.getSteer();
        int playerId;
        if(!message.hasSenderId()) {
            logger.error("Steer message without sender id");
            var player = Players.findPlayerByAddress(players.values(), sender);
            if(player == null) {
                return;
            }
            playerId = player.getId();
        } else {
            playerId = message.getSenderId();
        }
        sendAck(id, playerId, sender, message);
        changeSnakeDirection(playerId, steer.getDirection());
        tracker.updateStatus(playerId);
    }

    @Override
    protected void onAckMsg(GameMessage message, InetSocketAddress sender) {
        var player = Players.findPlayerByAddress(players.values(), sender);
        if(player != null) {
            tracker.updateStatus(player.getId());
        }
    }

    @Override
    protected void onJoinMsg(GameMessage message, InetSocketAddress sender) {
        var join = message.getJoin();
        try {
            var player = addPlayer(join.getPlayerName(), join.getRequestedRole(), sender);
            sendAck(id, player.getId(), sender, message);
            tracker.updateStatus(player.getId());
            var stateMessage = Messages.stateMessage(state);
            client.send(MessageType.STATE, stateMessage, sender);
            if(player.getRole() == NodeRole.NORMAL && !hasDeputy) {
                setDeputy(player.getId());
            }
        } catch (PlayerJoinException e) {
            logger.info("Player join failed: {}", e.getMessage());
            var errorMessage = Messages.errorMessage(e.getMessage());
            client.send(MessageType.ERROR, errorMessage, sender);
        }
    }

    @Override
    protected void onErrorMsg(GameMessage message, InetSocketAddress sender) {
        logger.info("Unexpected error msg from {}", sender.getAddress().getHostAddress());
    }

    @Override
    protected void onDiscoverMsg(GameMessage message, InetSocketAddress sender) {
        var announcement = Messages.announcementMessage(gameInfo);
        client.send(MessageType.ANNOUNCEMENT, announcement, sender);
    }

    @Override
    public PlayerView getPlayer() {
        return players.get(id);
    }

    @Override
    public SnakeView getSnake() {
        return snakes.get(id);
    }

    @Override
    public GameStateView getGameState() {
        return state;
    }

    @Override
    public void moveSnake(Direction direction) {
        changeSnakeDirection(id, direction);
    }

    @Override
    public void onInactivePlayers(Collection<Integer> inactive) {
        if(inactive.contains(deputyId)) {
            changeDeputy();
        }
        for(var playerId : inactive) {
            var player = players.remove(playerId);
            client.removeReceiver(player.getAddress());
        }
    }

    private void changeDeputy() {
        hasDeputy = false;
        var normal = Players.findByRole(players.values(), NodeRole.NORMAL);
        if(normal == null) {
            return;
        }
        setDeputy(normal.getId());
    }

    public void setDeputy(int deputy) {
        deputyId = deputy;
        hasDeputy = true;
        var roleChange = Messages.roleChangeMessage(null, NodeRole.DEPUTY, id, deputy);
        client.send(MessageType.ROLE_CHANGE, roleChange, players.get(deputy).getAddress());
    }

    @Override
    public void exit() {
        timer.cancelGameStateUpdate();
        timer.cancelPing();
    }
}
