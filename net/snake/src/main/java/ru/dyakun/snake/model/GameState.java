package ru.dyakun.snake.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.model.entity.*;
import ru.dyakun.snake.model.field.Field;
import ru.dyakun.snake.model.field.GameField;
import ru.dyakun.snake.model.field.SnakeCreateException;
import ru.dyakun.snake.protocol.Direction;
import ru.dyakun.snake.protocol.NodeRole;

import java.util.*;

public class GameState implements GameStateView {
    private static final Logger logger = LoggerFactory.getLogger(GameState.class);
    private int stateOrder = 0;
    private final Map<Integer, Player> players = new HashMap<>();
    private final Map<Integer, Snake> snakes = new HashMap<>();
    private final List<Point> foods = new ArrayList<>();
    private final Field field;
    private final Player player;
    private final GameInfo gameInfo;

    public GameState(GameConfig config, String nickname) {
        field = new Field(config, players, snakes, foods);
        try {
            int id = field.createSnake();
            player = new Player.Builder(nickname, id).role(NodeRole.MASTER).score(0).build();
            players.put(player.getId(), player);
            gameInfo = new GameInfo.Builder(config, player.getName()).addPlayer(player).mayJoin(true).build();
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

    public int getStateOrder() {
        return stateOrder;
    }

    @Override
    public List<PlayerView> getPlayers() {
        return players.values().stream().map(p -> (PlayerView)p).toList();
    }

    public void update() {
        stateOrder++;
        field.updateField();
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
    public Player getPlayer() {
        return player;
    }

    @Override
    public Snake getSnake() {
        return snakes.get(player.getId());
    }

    @Override
    public GameInfo getGameInfo() {
        return gameInfo;
    }
}
