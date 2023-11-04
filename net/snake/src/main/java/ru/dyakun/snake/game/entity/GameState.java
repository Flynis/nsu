package ru.dyakun.snake.game.entity;

import ru.dyakun.snake.game.GameConfig;
import ru.dyakun.snake.game.field.*;

import java.util.*;

public class GameState implements GameStateView {
    private int stateOrder;
    private EntitiesCollections collections;
    private final GameField field;

    public record EntitiesCollections(Collection<Player> players, Collection<Point> foods, Collection<Snake> snakes) {}

    public GameState(GameField field, EntitiesCollections collections) {
        this(field, 0 , collections);
    }

    private GameState(GameField field, int stateOrder, EntitiesCollections collections) {
        this.stateOrder = stateOrder;
        this.collections = collections;
        this.field = field;
    }

    public Collection<Player> getPlayers() {
        return collections.players;
    }

    public Collection<Snake> getSnakes() {
        return collections.snakes;
    }

    public Collection<Point> getFoods() {
        return collections.foods;
    }

    public int getStateOrder() {
        return stateOrder;
    }

    public void incStateOrder() {
        stateOrder++;
    }

    @Override
    public Collection<PlayerView> getGamePlayers() {
        return collections.players.stream().map(p -> (PlayerView)p).toList();
    }

    @Override
    public GameField getField() {
        return field;
    }

    public void updateBy(ru.dyakun.snake.protocol.GameState gameState) {
        if(field instanceof StaticField staticField) {
            var collections = collectionsFrom(gameState);
            this.collections = collections;
            staticField.fillBy(collections.snakes, collections.foods);
            stateOrder = gameState.getStateOrder();
        } else {
            throw new UnsupportedOperationException("Can not update non static field");
        }
    }

    public static GameState from(ru.dyakun.snake.protocol.GameState gameState, GameConfig config) {
        var collections = collectionsFrom(gameState);
        var field = new StaticField(config.getWidth(), config.getHeight());
        System.out.println(collections.snakes);
        System.out.println(collections.foods);
        field.fillBy(collections.snakes, collections.foods);
        return new GameState(field, gameState.getStateOrder(), collections);
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
}
