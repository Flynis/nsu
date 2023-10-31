package ru.dyakun.snake.game.field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.game.GameConfig;
import ru.dyakun.snake.game.entity.Player;
import ru.dyakun.snake.game.entity.Point;
import ru.dyakun.snake.game.entity.Snake;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Field extends AbstractField {
    private static final int FREE_SPACE_SIZE = 5;
    private static final int MIN_FREE_SPACE_FOR_RANDOM = 5;
    private static final Logger logger = LoggerFactory.getLogger(Field.class);
    private final int foodStatic;
    private final Collection<Point> foods;
    private final Map<Integer, Snake> snakes;
    private final Map<Integer, Player> players;

    private record StartSnake(Point head, Point tail) {}

    public Field(GameConfig config, Map<Integer, Player> players, Map<Integer, Snake> snakes, Collection<Point> foods) {
        super(config.getWidth(), config.getHeight());
        this.foodStatic = config.getFoodStatic();
        this.players = players;
        this.snakes = snakes;
        this.foods = foods;
        fill(snakes.values(), foods);
    }

    private void setFood(int x, int y) {
        if(get(x, y) == Tile.EMPTY) {
            set(x, y, Tile.FOOD);
            logger.debug("Created food ({}, {})", x, y);
            foods.add(new Point(x, y));
        }
    }

    private void createFood(int aliveSnakes) {
        while (foods.size() < foodStatic + aliveSnakes && freeSpace > MIN_FREE_SPACE_FOR_RANDOM) {
            int x = ThreadLocalRandom.current().nextInt(0, width);
            int y = ThreadLocalRandom.current().nextInt(0, height);
            setFood(x, y);
        }
        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                if(foods.size() == foodStatic + aliveSnakes || freeSpace == 0) {
                    return;
                }
                setFood(j, i);
            }
        }
    }

    private boolean isHeadsCollided(Snake current) {
        for(var snake: snakes.values()) {
            if(snake.playerId() != current.playerId() && current.getHead().equals(snake.getHead())) {
                return true;
            }
        }
        return false;
    }

    private Snake findSnakeByPoint(Point p) {
        for(var snake : snakes.values()) {
            for(var point: snake.points()) {
                if(!p.equals(snake.getHead()) && p.equals(point)) {
                    return snake;
                }
            }
        }
        return null;
    }

    private void removeDestroyedSnakes() {
        for(var snake : snakes.values()) {
            if(snake.state() == Snake.State.DESTROYED) {
                for(var point : snake.points()) {
                    if(point.equals(snake.getHead())) {
                        continue;
                    }
                    var rand = ThreadLocalRandom.current().nextInt(2);
                    if(rand == 1) {
                        set(point.x, point.y, Tile.FOOD);
                    } else {
                        set(point.x, point.y, Tile.EMPTY);
                    }
                }
            }
        }
    }

    public void updateField() {
        logger.debug("Food {}", foods);
        int aliveSnakes = 0;
        List<Point> eatenFoods = new ArrayList<>();
        for(var snake: snakes.values()) {
            if(snake.state() == Snake.State.ALIVE) {
                aliveSnakes++;
            }
            var head = snake.moveHead();
            if(get(head.x, head.y) == Tile.EMPTY) {
                var tail = snake.getTail();
                set(tail.x, tail.y, Tile.EMPTY);
                snake.moveTail();
            }
        }
        for(var snake: snakes.values()) {
            var head = snake.getHead();
            switch (get(head.x, head.y)) {
                case SNAKE -> {
                    var enemy = findSnakeByPoint(head);
                    if (enemy == null) {
                        logger.error("Snake point not found");
                        continue;
                    }
                    if(enemy.playerId() != snake.playerId()) {
                        players.get(enemy.playerId()).addScore(1);
                    }
                    snake.setState(Snake.State.DESTROYED);
                }
                case FOOD -> {
                    players.get(snake.playerId()).addScore(1);
                    eatenFoods.add(new Point(head.x, head.y));
                    if(isHeadsCollided(snake)) {
                        snake.setState(Snake.State.DESTROYED);
                    } else {
                        set(head.x, head.y, Tile.SNAKE);
                    }
                }
                case EMPTY -> {
                    if(isHeadsCollided(snake)) {
                        snake.setState(Snake.State.DESTROYED);
                    } else {
                        set(head.x, head.y, Tile.SNAKE);
                    }
                }
            }
        }
        removeDestroyedSnakes();
        foods.removeAll(eatenFoods);
        createFood(aliveSnakes);
    }

    public void createSnake(int id) throws SnakeCreateException {
        logger.debug("Creating new snake");
        StartSnake points = placeSnake();
        if (points == null) {
            throw new SnakeCreateException("No free space for new snake");
        }
        logger.debug("Snake: {}", points);
        Snake snake = new Snake(points.head, points.tail, id);
        snakes.put(id, snake);
        fillPoints(snake.points(), Tile.SNAKE);
    }

    private StartSnake placeSnake() {
        int offset = FREE_SPACE_SIZE / 2;
        for(int i = offset; i < height - offset; i++) {
            for(int j = offset; j < width - offset; j++) {
                if(!isFreeSpace(j, i, offset) || get(j, i) != Tile.EMPTY) {
                    continue;
                }
                var tail = findFreeNeighbor(j, i);
                if(tail != null) {
                    return new StartSnake(new Point(j, i), tail);
                }
            }
        }
        return null;
    }

    private Point findFreeNeighbor(int x, int y) {
        if(get(x, y + 1) == Tile.EMPTY) {
            return new Point(x, y + 1);
        }
        if(get(x, y - 1) == Tile.EMPTY) {
            return new Point(x, y - 1);
        }
        if(get(x + 1, y) == Tile.EMPTY) {
            return new Point(x + 1, y);
        }
        if(get(x - 1, y) == Tile.EMPTY) {
            return new Point(x - 1, y);
        }
        return null;
    }

    private boolean isFreeSpace(int centerX, int centerY, int offset) {
        for(int i = centerY - offset; i <= centerY + offset; i++) {
            for(int j = centerX - offset; j <= centerX + offset; j++) {
                if(get(j, i) == Tile.SNAKE) {
                    return false;
                }
            }
        }
        return true;
    }
}
