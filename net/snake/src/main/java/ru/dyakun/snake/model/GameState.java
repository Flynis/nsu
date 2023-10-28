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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameState implements GameStateView {
    private static final Logger logger = LoggerFactory.getLogger(GameState.class);
    private int stateOrder = 0;
    private final Map<Integer, Player> players = new ConcurrentHashMap<>();
    private final Map<Integer, Snake> snakes = new ConcurrentHashMap<>();
    private final List<Point> foods = new ArrayList<>();
    private final Field field;
    private final Player currentPlayer;
    private Player master;
    private final GameInfo gameInfo;
    private final IdGenerator generator = new SimpleIdGenerator();
    private final AtomicBoolean busy = new AtomicBoolean(false);
    private boolean updating = false;

    private record EntitiesCollections(Collection<Player> players, Collection<Point> foods, Collection<Snake> snakes) {}

    public GameState(GameConfig config, String nickname) {
        field = new Field(config, players, snakes, foods);
        try {
            int id = generator.next();
            field.createSnake(id);
            currentPlayer = new Player.Builder(nickname, id).role(NodeRole.MASTER).score(0).build();
            master = currentPlayer;
            players.put(currentPlayer.getId(), currentPlayer);
            gameInfo = new GameInfo.Builder(config, currentPlayer.getName()).setPlayers(players.values()).mayJoin(true).build();
        } catch (SnakeCreateException e) {
            logger.info("Place snake into empty field failed");
            throw new AssertionError();
        }
    }

    private void fillMaps(EntitiesCollections collections) {
        this.players.clear();
        this.snakes.clear();
        this.foods.clear();
        for(var player : collections.players) {
            this.players.put(player.getId(), player);
        }
        for(var snake : collections.snakes) {
            this.snakes.put(snake.playerId(), snake);
        }
        this.foods.addAll(collections.foods);
    }

    private GameState(GameInfo gameInfo, int stateOrder, Player player, EntitiesCollections collections) {
        this.gameInfo = gameInfo;
        this.currentPlayer = player;
        this.stateOrder = stateOrder;
        fillMaps(collections);
        master = Player.findMaster(collections.players);
        if(master == null) {
            throw new IllegalStateException("Master not found");
        }
        field = new Field(gameInfo.getConfig(), this.players, this.snakes, this.foods);
    }

    public boolean hasDeputy() {
        return Player.findDeputy(players.values()) != null;
    }

    public Player setNewDeputy() {
        var deputy = Player.find(players.values(), NodeRole.NORMAL);
        if(deputy == null) {
            return null;
        } else {
            deputy.setRole(NodeRole.DEPUTY);
            return deputy;
        }
    }

    public void becomeMaster() {
        // TODO impl
    }

    public void changeMasterToDeputy() {

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

    public Player findDeputy(Collection<Integer> deleted) {
        for(var id: deleted) {
            var player = players.get(id);
            if(player.getRole() == NodeRole.DEPUTY) {
                return player;
            }
        }
        return null;
    }

    public void deleteInactivePlayers(Collection<Integer> inactive) {
        for(var id: inactive) {
            players.remove(id);
        }
    }

    public Player getMaster() {
        return master;
    }

    public boolean isMaster() {
        return master == currentPlayer;
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

    public void changePLayerRole() {
        // TODO impl
    }

    @Override
    public List<PlayerView> getPlayers() {
        return players.values().stream().map(p -> (PlayerView)p).toList();
    }

    public Collection<Player> update() {
        updating = true;
        stateOrder++;
        field.updateField();
        var viewers = new ArrayList<Player>();
        for(var snake : snakes.values()) {
            if (snake.state() == Snake.State.DESTROYED) {
                viewers.add(players.get(snake.playerId()));
            }
        }
        for(var viewer: viewers) {
            snakes.remove(viewer.getId());
        }
        updating = false;
        return viewers;
    }

    @Override
    public GameField getField() {
        return field;
    }

    public void changeSnakeDirection(int id, Direction direction) {
        if(!snakes.containsKey(id)) {
            return;
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

    public void updateBy(ru.dyakun.snake.protocol.GameState gameState) {
        var collections = collectionsFrom(gameState);
        fillMaps(collections);
        field.updateBy(players, snakes, foods);
        gameInfo.setPlayers(collections.players);
        stateOrder = gameState.getStateOrder();
    }

    private static EntitiesCollections collectionsFrom(ru.dyakun.snake.protocol.GameState gameState) {
        var players = new ArrayList<Player>();
        var gamePlayers = gameState.getPlayers();
        for(int i = 0; i < gamePlayers.getPlayersCount(); i++) {
            players.add(Player.fromGamePlayer(gamePlayers.getPlayers(i)));
        }
        var foods = new ArrayList<Point>();
        for(int i = 0; i < gameState.getFoodsCount(); i++) {
            foods.add(Point.from(gameState.getFoods(i)));
        }
        var snakes = new ArrayList<Snake>();
        for(int i = 0; i < gameState.getSnakesCount(); i++) {
            snakes.add(Snake.from(gameState.getSnakes(i)));
        }
        return new EntitiesCollections(players, foods, snakes);
    }

    public static class Builder {
        private final String nickname;
        private final NodeRole role;
        private int id;
        private final GameInfo gameInfo;

        public Builder(String nickname, NodeRole role, GameInfo gameInfo) {
            this.nickname = nickname;
            this.role = role;
            this.gameInfo = gameInfo;
        }

        public GameInfo getGameInfo() {
            return gameInfo;
        }

        public void setId(int id) {
            this.id = id;
        }

        public GameState build(ru.dyakun.snake.protocol.GameState gameState) {
            var player = new Player.Builder(nickname, id).score(0).role(role).build();
            return new GameState(gameInfo, gameState.getStateOrder(), player, collectionsFrom(gameState));
        }
    }
}
