package ru.dyakun.snake.game.field;

import ru.dyakun.snake.game.entity.Point;
import ru.dyakun.snake.game.entity.Snake;

import java.util.Arrays;
import java.util.Collection;

public class AbstractField implements GameField {
    protected final int width;
    protected final int height;
    private final Tile[] field;
    protected int freeSpace;

    protected AbstractField(int width, int height) {
        this.width = width;
        this.height = height;
        this.field = new Tile[height * width];
        Arrays.fill(field, Tile.EMPTY);
        this.freeSpace = height * width;
    }

    protected void fill(Collection<Snake> snakes, Collection<Point> foods) {
        Arrays.fill(field, Tile.EMPTY);
        freeSpace = height * width;
        fillPoints(foods, Tile.FOOD);
        for(var snake : snakes) {
            fillPoints(snake, Tile.SNAKE);
        }
    }

    protected void fillPoints(Iterable<Point> points, Tile tile) {
        for(var point: points) {
            set(point.x, point.y, tile);
        }
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

    protected void set(int x, int y, Tile tile) {
        x = Math.floorMod(x, width);
        y = Math.floorMod(y, height);
        field[y * height + x] = tile;
        if(tile == Tile.EMPTY) {
            freeSpace++;
        } else {
            freeSpace--;
        }
    }

    protected Tile get(int x, int y) {
        x = Math.floorMod(x, width);
        y = Math.floorMod(y, height);
        return field[y * height + x];
    }
}
