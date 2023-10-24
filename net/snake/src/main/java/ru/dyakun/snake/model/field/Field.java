package ru.dyakun.snake.model.field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.model.GameConfig;
import ru.dyakun.snake.model.entity.Player;
import ru.dyakun.snake.model.entity.Point;
import ru.dyakun.snake.model.entity.Snake;
import ru.dyakun.snake.util.IdGenerator;
import ru.dyakun.snake.util.SimpleIdGenerator;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Field implements GameField {
    private static final int FREE_SPACE_SIZE = 5;
    private static final Logger logger = LoggerFactory.getLogger(Field.class);
    private final int width;
    private final int height;
    private final int foodStatic;
    private final Tile[] field;
    private final List<Point> foods;
    private final Map<Integer, Snake> snakes;
    private final Map<Integer, Player> players;
    private final IdGenerator generator = new SimpleIdGenerator();
    private boolean updating = false;

    public Field(GameConfig config, Map<Integer, Player> players, Map<Integer, Snake> snakes, List<Point> foods) {
        this.width = config.getWidth();
        this.height = config.getHeight();
        this.foodStatic = config.getFoodStatic();
        this.field = new Tile[height * width];
        Arrays.fill(field, Tile.EMPTY);
        this.foods = foods;
        fillPoints(foods, Tile.FOOD);
        this.snakes = snakes;
        for(var snake : this.snakes.values()) {
            fillPoints(snake.points(), Tile.SNAKE);
        }
        this.players = players;
    }

    public boolean isUpdating() {
        return updating;
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

    private void createFood() {
        while (foods.size() < foodStatic + snakes.size()) {
            int x = ThreadLocalRandom.current().nextInt(0, width);
            int y = ThreadLocalRandom.current().nextInt(0, height);
            if(get(x, y) == Tile.EMPTY) {
                set(x, y, Tile.FOOD);
                logger.debug("Created food ({}, {})", x, y);
                foods.add(new Point(x, y));
            }
        }
    }

    public void updateField() {
        updating = true;
        logger.debug("Food {}", foods);
        for(var snake: snakes.values()) {
            var head = snake.moveHead();
            switch (get(head.x, head.y)) {
                case FOOD -> {
                    set(head.x, head.y, Tile.SNAKE);
                    foods.removeIf(p -> p.x == head.x && p.y == head.y);
                    players.get(snake.playerId()).addScore(1);
                }
                case SNAKE -> {
                    // TODO destroy
                }
                case EMPTY -> {
                    set(head.x, head.y, Tile.SNAKE);
                    var tail = snake.getTail();
                    set(tail.x, tail.y, Tile.EMPTY);
                    snake.moveTail();
                }
            }
            logger.debug("Snake move {}", snake.points());
        }
        createFood();
        updating = false;
    }

    public int createSnake() throws SnakeCreateException {
        logger.debug("Creating new snake");
        SubMatrix freeSpace = findFreeSpace();
        logger.debug("Free space {}", freeSpace);
        if(freeSpace.n() < FREE_SPACE_SIZE) {
            throw new SnakeCreateException("No free space for new snake");
        }
        List<Point> points = placeSnake(freeSpace);
        if (points.isEmpty()) {
            throw new SnakeCreateException("No free space for new snake");
        }
        logger.debug("Snake: {}", points);
        int id = generator.next();
        Snake snake = new Snake(points, id);
        snakes.put(id, snake);
        fillPoints(points, Tile.SNAKE);
        return id;
    }

    private void set(int x, int y, Tile tile) {
        x = mod(x, width);
        y = mod(y, height);
        field[y * height + x] = tile;
    }

    Tile get(int x, int y) {
        x = mod(x, width);
        y = mod(y, height);
        return field[y * height + x];
    }

    private int mod(int n, int size) {
        return (n % size + size) % size;
    }

    private void fillPoints(List<Point> points, Tile tile) {
        for(var point: points) {
            set(point.x, point.y, tile);
        }
    }

    private List<Point> placeSnake(SubMatrix matrix) {
        List<Point> points = new ArrayList<>();
        int centerX = matrix.leftUpper().x + (matrix.bottomRight().x - matrix.leftUpper().x) / 2;
        int centerY = matrix.leftUpper().y + (matrix.bottomRight().y - matrix.leftUpper().y) / 2;
        for(int s = 0; s <= matrix.n() / 2; s++) {
            int y = centerY - s;
            // TODO refactor
            for(int j = centerX - s; j <= centerX + s; j++) {
                if(get(j, y) == Tile.EMPTY) {
                    Point tail = findFreeNeighbor(j, y);
                    if(tail != null) {
                        points.add(tail);
                        points.add(new Point(j, y));
                        return points;
                    }
                }
            }
            y = centerY + s;
            for(int j = centerX - s; j <= centerX + s; j++) {
                if(get(j, y) == Tile.EMPTY) {
                    Point tail = findFreeNeighbor(j, y);
                    if(tail != null) {
                        points.add(tail);
                        points.add(new Point(j, y));
                        return points;
                    }
                }
            }
            int x = centerX - s;
            for(int i = centerY - s + 1; i <= centerX + s - 1; i++) {
                if(get(x, i) == Tile.EMPTY) {
                    Point tail = findFreeNeighbor(x, i);
                    if(tail != null) {
                        points.add(tail);
                        points.add(new Point(x, i));
                        return points;
                    }
                }
            }
            x = centerX + s;
            for(int i = centerY - s + 1; i <= centerX + s - 1; i++) {
                if(get(x, i) == Tile.EMPTY) {
                    Point tail = findFreeNeighbor(x, i);
                    if(tail != null) {
                        points.add(tail);
                        points.add(new Point(x, i));
                        return points;
                    }
                }
            }
        }
        return points;
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
        // TODO correct calc n
        return new SubMatrix(ans, new Point(left, upper), new Point(right, bottom));
    }
}
