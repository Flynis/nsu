package ru.dyakun.snake.model.field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.model.SnakeCreateException;
import ru.dyakun.snake.model.event.GameEvent;
import ru.dyakun.snake.model.event.GameEventListener;
import ru.dyakun.snake.protocol.Direction;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Field implements GameField {
    private static final int FREE_SPACE_SIZE = 5;
    private static final Logger logger = LoggerFactory.getLogger(Field.class);
    private final int width;
    private final int height;
    private final int foodStatic;
    private final Tile[] field;
    private final List<Point> foods = new ArrayList<>();
    private final Map<Integer, Snake> snakes = new HashMap<>();
    private int playersId;
    private final List<GameEventListener> listeners = new ArrayList<>();

    private void notifyListeners() {
        for(var listener : listeners) {
            listener.onEvent(GameEvent.REPAINT_FIELD);
        }
    }

    public void addGameEventListener(GameEventListener listener) {
        listeners.add(listener);
    }

    public Field(int width, int height, int foodStatic) {
        this.width = width;
        this.height = height;
        this.foodStatic = foodStatic;
        this.field = new Tile[height * width];
        Arrays.fill(field, Tile.EMPTY);
        this.playersId = 0;
    }

    public Collection<Point> getFoods() {
        return foods;
    }

    public Collection<Snake> getSnakes() {
        return snakes.values();
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

    public void changeSnakeDirection(int id, Direction direction) {
        if(!snakes.containsKey(id)) {
            throw new IllegalArgumentException("Illegal snake id");
        }
        var snake = snakes.get(id);
        if(snake.direction != direction) {
            snake.direction = direction;
            snake.needRotate = true;
        }
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
        logger.debug("Food {}", foods);
        for(var snake: snakes.values()) {
            List<Point> points = snake.points;
            Point head = points.get(points.size() - 1);
            if(snake.needRotate) {
                Point newHead = new Point(head.x, head.y);
                move(newHead, snake.direction);
                points.add(newHead);
                head = newHead;
                snake.needRotate = false;
            } else {
                move(head, snake.direction);
            }
            logger.debug("Snake move {}", points);
            switch (get(head.x, head.y)) {
                case FOOD -> {
                    set(head.x, head.y, Tile.SNAKE);
                    Point finalHead = head;
                    foods.removeIf(p -> p.x == finalHead.x && p.y == finalHead.y);
                    // TODO score
                }
                case SNAKE -> {
                    // TODO destroy
                }
                case EMPTY -> {
                    set(head.x, head.y, Tile.SNAKE);
                    Point tail = points.get(0);
                    Point next = points.get(1);
                    set(tail.x, tail.y, Tile.EMPTY);
                    if(tail.x == next.x) {
                        if(Math.abs(tail.y - next.y) == 1 && points.size() > 2) {
                            points.remove(0);
                        } else {
                            if(tail.y > next.y) {
                                tail.y--;
                            } else {
                                tail.y++;
                            }
                        }
                    } else if(tail.y == next.y) {
                        if(Math.abs(tail.x - next.x) == 1 && points.size() > 2) {
                            points.remove(0);
                        } else {
                            if(tail.x > next.x) {
                                tail.x--;
                            } else {
                                tail.x++;
                            }
                        }
                    } else {
                        throw new IllegalStateException("At least one coordinate must coincide between tail and next");
                    }
                }
            }
            logger.debug("Snake move {}", points);
        }
        createFood();
        notifyListeners();
    }

    private void move(Point point, Direction direction) {
        switch (direction) {
            case UP -> point.y--;
            case DOWN -> point.y++;
            case LEFT -> point.x--;
            case RIGHT -> point.x++;
        }
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
        int id = playersId;
        playersId++;
        Snake snake = new Snake(points, id);
        logger.debug("Snake direction {}", snake.direction);
        snakes.put(snake.playerId, snake);
        for(var point: points) {
            set(point.x, point.y, Tile.SNAKE);
        }
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
        return new SubMatrix(ans, new Point(left, upper), new Point(right, bottom));
    }
}
