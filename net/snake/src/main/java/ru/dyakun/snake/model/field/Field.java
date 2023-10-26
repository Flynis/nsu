package ru.dyakun.snake.model.field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.model.GameConfig;
import ru.dyakun.snake.model.entity.Player;
import ru.dyakun.snake.model.entity.Point;
import ru.dyakun.snake.model.entity.Snake;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Field implements GameField {
    private static final int FREE_SPACE_SIZE = 5;
    private static final int MIN_FREE_SPACE_FOR_RANDOM = 5;
    private static final Logger logger = LoggerFactory.getLogger(Field.class);
    private final int width;
    private final int height;
    private final int foodStatic;
    private final Tile[] field;
    private final Collection<Point> foods;
    private final Map<Integer, Snake> snakes;
    private final Map<Integer, Player> players;
    private int freeSpace;
    private int aliveSnakes;

    private record StartSnake(Point head, Point tail) {}

    public Field(GameConfig config, Map<Integer, Player> players, Map<Integer, Snake> snakes, Collection<Point> foods) {
        this.width = config.getWidth();
        this.height = config.getHeight();
        this.foodStatic = config.getFoodStatic();
        this.field = new Tile[height * width];
        Arrays.fill(field, Tile.EMPTY);
        freeSpace = height * width;
        this.foods = foods;
        fillPoints(foods, Tile.FOOD);
        this.snakes = snakes;
        for(var snake : this.snakes.values()) {
            fillPoints(snake.points(), Tile.SNAKE);
        }
        this.players = players;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public Tile getTile(int x, int y) {
        if(x < 0 || x >= width) {
            throw new IllegalArgumentException("Illegal x: " + x);
        }
        if(y < 0 || y >= height) {
            throw new IllegalArgumentException("Illegal y: " + y);
        }
        return field[y * height + x];
    }

    private void setFood(int x, int y) {
        if(get(x, y) == Tile.EMPTY) {
            set(x, y, Tile.FOOD);
            logger.debug("Created food ({}, {})", x, y);
            foods.add(new Point(x, y));
        }
    }

    private void createFood() {
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

    public void updateField() {
        logger.debug("Food {}", foods);
        aliveSnakes = 0;
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
            // TODO
            var head = snake.getHead();
            switch (get(head.x, head.y)) {
                case FOOD -> {
                    set(head.x, head.y, Tile.SNAKE);
                    foods.removeIf(p -> p.x == head.x && p.y == head.y);
                    players.get(snake.playerId()).addScore(1);
                }
                case EMPTY -> {
                    set(head.x, head.y, Tile.SNAKE);
                    var tail = snake.getTail();
                    set(tail.x, tail.y, Tile.EMPTY);
                    snake.moveTail();
                }
            }
        }
        createFood();
    }

    public void createSnake(int id) throws SnakeCreateException {
        logger.debug("Creating new snake");
        SubMatrix freeSpace = findFreeSpace();
        logger.debug("Free space {}", freeSpace);
        if(freeSpace.getSize() < FREE_SPACE_SIZE) {
            throw new SnakeCreateException("No free space for new snake");
        }
        StartSnake points = placeSnake(freeSpace);
        if (points == null) {
            throw new SnakeCreateException("No free space for new snake");
        }
        logger.debug("Snake: {}", points);
        Snake snake = new Snake(points.head, points.tail, id);
        snakes.put(id, snake);
        fillPoints(snake.points(), Tile.SNAKE);
    }

    private void set(int x, int y, Tile tile) {
        x = mod(x, width);
        y = mod(y, height);
        field[y * height + x] = tile;
        if(tile == Tile.EMPTY) {
            freeSpace++;
        } else {
            freeSpace--;
        }
    }

    Tile get(int x, int y) {
        x = mod(x, width);
        y = mod(y, height);
        return field[y * height + x];
    }

    private int mod(int n, int size) {
        return (n % size + size) % size;
    }

    private void fillPoints(Collection<Point> points, Tile tile) {
        for(var point: points) {
            set(point.x, point.y, tile);
        }
    }

    private StartSnake placeSnake(SubMatrix matrix) {
        int centerX = matrix.leftUpper().x + (matrix.bottomRight().x - matrix.leftUpper().x) / 2;
        int centerY = matrix.leftUpper().y + (matrix.bottomRight().y - matrix.leftUpper().y) / 2;
        for(int s = 0; s <= matrix.getSize() / 2; s++) {
            int y = centerY - s;
            // TODO refactor
            for(int j = centerX - s; j <= centerX + s; j++) {
                if(get(j, y) == Tile.EMPTY) {
                    Point tail = findFreeNeighbor(j, y);
                    if(tail != null) {
                        return new StartSnake(new Point(j, y), tail);
                    }
                }
            }
            y = centerY + s;
            for(int j = centerX - s; j <= centerX + s; j++) {
                if(get(j, y) == Tile.EMPTY) {
                    Point tail = findFreeNeighbor(j, y);
                    if(tail != null) {
                        return new StartSnake(new Point(j, y), tail);
                    }
                }
            }
            int x = centerX - s;
            for(int i = centerY - s + 1; i <= centerX + s - 1; i++) {
                if(get(x, i) == Tile.EMPTY) {
                    Point tail = findFreeNeighbor(x, i);
                    if(tail != null) {
                        return new StartSnake(new Point(x, i), tail);
                    }
                }
            }
            x = centerX + s;
            for(int i = centerY - s + 1; i <= centerX + s - 1; i++) {
                if(get(x, i) == Tile.EMPTY) {
                    Point tail = findFreeNeighbor(x, i);
                    if(tail != null) {
                        return new StartSnake(new Point(x, i), tail);
                    }
                }
            }
        }
        return null;
    }

    private Point findFreeNeighbor(int x, int y) {
        if(field[(y + 1) * height + x] == Tile.EMPTY) {
            return new Point(x, y + 1);
        }
        if(field[(y - 1) * height + x] == Tile.EMPTY) {
            return new Point(x, y - 1);
        }
        if(field[y * height + (x + 1)] == Tile.EMPTY) {
            return new Point(x + 1, y);
        }
        if(field[y * height + (x - 1)] == Tile.EMPTY) {
            return new Point(x - 1, y);
        }
        return null;
    }

    private SubMatrix findFreeSpace() {
        int ans = 0;
        int[] d = new int[width];
        Arrays.fill(d, -1);
        int[] d1 = new int[width];
        int[] d2 = new int[width];
        int left = -1, upper = -1, bottom = -1, right = -1;
        Stack<Integer> stack = new Stack<>();
        for(int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (field[i * height + j] == Tile.SNAKE) {
                    d[j] = i;
                }
            }
            while (!stack.isEmpty()) {
                stack.pop();
            }
            for (int j = 0; j < width; j++) {
                while (!stack.empty() && d[stack.peek()] <= d[j])  {
                    stack.pop();
                }
                d1[j] = stack.empty() ? -1 : stack.peek();
                stack.push (j);
            }
            while (!stack.empty()) stack.pop();
            for (int j = width - 1; j >= 0; j--) {
                while (!stack.empty() && d[stack.peek()] <= d[j])  {
                    stack.pop();
                }
                d2[j] = stack.empty() ? width : stack.peek();
                stack.push (j);
            }
            for (int j = 0; j < width; j++) {
                ans = Math.max(ans, (i - d[j]) * (d2[j] - d1[j] - 1));
                upper = d[j] + 1;
                bottom = i;
                left = d1[j] + 1;
                right = d2[j] - 1;
            }
        }
        return new SubMatrix(new Point(left, upper), new Point(right, bottom));
    }
}
