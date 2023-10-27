package ru.dyakun.snake.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.model.entity.*;
import ru.dyakun.snake.model.field.Field;
import ru.dyakun.snake.model.field.GameField;
import ru.dyakun.snake.model.field.SnakeCreateException;
import ru.dyakun.snake.protocol.Direction;
import ru.dyakun.snake.protocol.NodeRole;
import ru.dyakun.snake.util.IdGenerator;
import ru.dyakun.snake.util.SimpleIdGenerator;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameState implements GameStateView {
    private static final Logger logger = LoggerFactory.getLogger(GameState.class);
    private int stateOrder = 0;
    private final Map<Integer, Player> players = new ConcurrentHashMap<>();
    private final Map<Integer, Snake> snakes = new ConcurrentHashMap<>();
    private final Map<Integer, LocalDateTime> activePlayers = new ConcurrentHashMap<>();
    private final List<Point> foods = new ArrayList<>();
    private final Field field;
    private final Player currentPlayer;
    private final GameInfo gameInfo;
    private final IdGenerator generator = new SimpleIdGenerator();
    private final AtomicBoolean busy = new AtomicBoolean(false);
    private boolean updating = false;

    public GameState(GameConfig config, String nickname) {
        field = new Field(config, players, snakes, foods);
        try {
            int id = generator.next();
            field.createSnake(id);
            currentPlayer = new Player.Builder(nickname, id).role(NodeRole.MASTER).score(0).build();
            players.put(currentPlayer.getId(), currentPlayer);
            gameInfo = new GameInfo.Builder(config, currentPlayer.getName()).setPlayers(players.values()).mayJoin(true).build();
        } catch (SnakeCreateException e) {
            logger.info("Place snake into empty field failed");
            throw new AssertionError();
        }
    }

    public Collection<Player> getMembers() {
        return players.values();
    }

    public Collection<Snake> getSnakes() {
        return snakes.values();
    }

    public List<Point> getFoods() {
        return foods;
    }

    public boolean isUpdating() {
        return updating;
    }

    public int getStateOrder() {
        return stateOrder;
    }

    public Player addPlayer(String nickname, NodeRole role, InetSocketAddress address) throws PlayerJoinException {
        int id = generator.next();
        if(role != NodeRole.NORMAL && role != NodeRole.VIEWER) {
            throw new PlayerJoinException("Illegal role: " + role);
        }
        try {
            var player = new Player.Builder(nickname, id).role(role).address(address).score(0).build();
            if(role == NodeRole.NORMAL) {
                field.createSnake(id);
            }
            players.put(id, player);
            activePlayers.put(id, LocalDateTime.now());
            return player;
        } catch (SnakeCreateException e) {
            throw new PlayerJoinException("No space for snake", e);
        } catch (IllegalArgumentException e) {
            throw new PlayerJoinException(e.getMessage(), e);
        }
    }

    public Player findPlayerByAddress(InetSocketAddress address) {
        for(var player : players.values()) {
            if(player.getAddress().equals(address)) {
                return player;
            }
        }
        logger.error("Player not found");
        return null;
    }

    public void updateActivePlayer(InetSocketAddress playerAddress) {
        var player = findPlayerByAddress(playerAddress);
        if(player == null) {
            return;
        }
        activePlayers.put(player.getId(), LocalDateTime.now());
    }

    public void changePLayerRole() {
        // TODO impl
    }

    @Override
    public List<PlayerView> getPlayers() {
        return players.values().stream().map(p -> (PlayerView)p).toList();
    }

    public void update() {
        updating = true;
        stateOrder++;
        field.updateField();
        // TODO destroyed snakes
        updating = false;
    }

    @Override
    public GameField getField() {
        return field;
    }

    public void changeSnakeDirection(int id, Direction direction) {
        if(!snakes.containsKey(id)) {
            throw new IllegalArgumentException("Illegal snake id");
        }
        var snake = snakes.get(id);
        snake.setDirection(direction);
    }

    @Override
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    @Override
    public Snake getSnake() {
        return snakes.get(currentPlayer.getId());
    }

    @Override
    public GameInfo getGameInfo() {
        return gameInfo;
    }

    public GameState from(ru.dyakun.snake.protocol.GameState gameState) {
        // TODO impl
    }
}
