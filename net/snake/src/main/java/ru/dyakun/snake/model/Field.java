package ru.dyakun.snake.model;

import ru.dyakun.snake.protocol.Direction;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Field {
    private static final int FREE_SPACE_SIZE = 5;
    public enum Tiles {
        SNAKE,
        FOOD,
        EMPTY
    }
    private final int width;
    private final int height;
    private final int foodStatic;
    private final Tiles[] field;
    private int currentFoodCount;
    private final List<Point> foods = new ArrayList<>();
    private final Map<Integer, Snake> snakes = new HashMap<>();
    private int playersId;

    public Field(int width, int height, int foodStatic) {
        this.width = width;
        this.height = height;
        this.foodStatic = foodStatic;
        this.field = new Tiles[height * width];
        Arrays.fill(field, Tiles.EMPTY);
        this.currentFoodCount = 0;
        this.playersId = 0;
    }

    Collection<Point> getFoods() {
        return foods;
    }

    Collection<Snake> getSnakes() {
        return snakes.values();
    }

    void changeSnakeDirection(int id, Direction direction) {
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
        while (currentFoodCount < foodStatic + snakes.size()) {
            int x = ThreadLocalRandom.current().nextInt(0, width);
            int y = ThreadLocalRandom.current().nextInt(0, height);
            if(get(x, y) == Tiles.EMPTY) {
                set(x, y, Tiles.FOOD);
                foods.add(new Point(x, y));
                currentFoodCount++;
            }
        }
    }

    void updateField() {
        for(var snake: snakes.values()) {
            List<Point> points = snake.points;
            Point tail = points.get(0);
            Point next = points.get(1);
            set(tail.x, tail.y, Tiles.EMPTY);
            if(tail.x == next.x) {
                if(Math.abs(tail.y - next.y) == 1) {
                    points.remove(0);
                } else {
                    if(tail.y > next.y) {
                        tail.y--;
                    } else {
                        tail.y++;
                    }
                }
            } else if(tail.y == next.y) {
                if(Math.abs(tail.x - next.x) == 1) {
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
            Point head = points.get(points.size() - 1);
            if(snake.needRotate) {
                Point newHead = new Point(head.x, head.y);
                move(newHead, snake.direction);
                set(newHead.x, newHead.y, Tiles.EMPTY);
                points.add(newHead);
            } else {
                set(head.x, head.y, Tiles.EMPTY);
                move(head, snake.direction);
            }
        }
    }

    private void move(Point point, Direction direction) {
        switch (direction) {
            case UP -> point.y++;
            case DOWN -> point.y--;
            case LEFT -> point.x--;
            case RIGHT -> point.x++;
        }
    }

    void createSnake() throws SnakeCreateException {
        SubMatrix freeSpace = findFreeSpace();
        if(freeSpace.n < FREE_SPACE_SIZE) {
            throw new SnakeCreateException("No free space for new snake");
        }
        List<Point> points = placeSnake(freeSpace);
        if (points.isEmpty()) {
            throw new SnakeCreateException("No free space for new snake");
        }
        Snake snake = new Snake(points, playersId);
        playersId++;
        snakes.put(snake.playerId, snake);
        for(var point: points) {
            set(point.x, point.y, Tiles.SNAKE);
        }
    }

    private void set(int x, int y, Tiles tile) {
        field[y * height + x] = tile;
        // TODO module
    }

    public Tiles get(int x, int y) {
        x = mod(x, width);
        y = mod(y, height);
        return field[y * height + x];
    }

    private int mod(int n, int size) {
        return (n % size + size) % size;
    }

    private record SubMatrix(int n, Point leftUpper, Point bottomRight) {
        public SubMatrix {
            if (n < 0) {
                throw new IllegalArgumentException("Sub matrix size must be positive");
            }
            if(leftUpper.x < 0 || leftUpper.y < 0) {
                throw new IllegalArgumentException("Point coords must be positive");
            }
            if(bottomRight.x < 0 || bottomRight.y < 0) {
                throw new IllegalArgumentException("Point coords must be positive");
            }
        }
    }

    private List<Point> placeSnake(SubMatrix matrix) {
        List<Point> points = new ArrayList<>();
        int centerX = (matrix.bottomRight.x - matrix.leftUpper.x) / 2;
        int centerY = (matrix.leftUpper.y - matrix.bottomRight.y) / 2;
        for(int s = 0; s <= matrix.n / 2; s++) {
            int y = centerY - s;
            // TODO refactor
            for(int j = centerX - s; j <= centerX + s; j++) {
                if(field[y * height + j] == Tiles.EMPTY) {
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
                if(field[y * height + j] == Tiles.EMPTY) {
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
                if(field[i * height + x] == Tiles.EMPTY) {
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
                if(field[i * height + x] == Tiles.EMPTY) {
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
        if(field[(y + 1) * height + x] == Tiles.EMPTY) {
            return new Point(x, y + 1);
        }
        if(field[(y - 1) * height + x] == Tiles.EMPTY) {
            return new Point(x, y - 1);
        }
        if(field[y * height + (x + 1)] == Tiles.EMPTY) {
            return new Point(x + 1, y);
        }
        if(field[y * height + (x - 1)] == Tiles.EMPTY) {
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
                if (field[i * height + j] == Tiles.SNAKE) {
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
