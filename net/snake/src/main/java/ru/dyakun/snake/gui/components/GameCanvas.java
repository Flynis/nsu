package ru.dyakun.snake.gui.components;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import ru.dyakun.snake.game.entity.GameStateView;
import ru.dyakun.snake.game.entity.SnakeView;
import ru.dyakun.snake.game.field.GameField;
import ru.dyakun.snake.protocol.Direction;
import ru.dyakun.snake.util.AssetsPool;

import java.util.Collection;

public class GameCanvas extends ResizableCanvas {
    private record Snake(Image head, Image body) { }
    private record Grid(int x, int y, int width, int height) {}
    private final Snake playerSnake;
    private final Snake enemySnake;
    private GameStateView gameState;
    private SnakeView current;

    public GameCanvas() {
        playerSnake = new Snake(
                AssetsPool.getImage("/images/green_head.png"),
                AssetsPool.getImage("/images/green.png"));
        enemySnake = new Snake(
                AssetsPool.getImage("/images/red_head.png"),
                AssetsPool.getImage("/images/red.png"));
    }

    @Override
    protected void redraw() {
        if(gameState == null) {
            clear();
        } else {
            drawState(gameState, current);
        }
    }

    private void clear() {
        var g = getGraphicsContext2D();
        g.setFill(Color.web("#101010"));
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawRotatedImage(Image image, double angle, int x, int y, int size) {
        var g = getGraphicsContext2D();
        g.save();
        var centerX = x + size / 2;
        var centerY = y + size / 2;
        Rotate r = new Rotate(angle, centerX, centerY);
        g.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
        g.drawImage(image, x, y, size, size);
        g.drawImage(image, x - 1, y - 1, size, 1);
        g.drawImage(image, x - 1, y - 1, 1, size);
        g.restore();
    }

    private void drawGridCellRandom(Grid grid, Image image, int xi, int yi, int size) {
        var random = Direction.random();
        drawGridCell(grid, image, xi, yi, size, random);
    }

    private void drawGridCell(Grid grid, Image image, int xi, int yi, int size, Direction direction) {
        var degrees = Direction.toDegrees(direction);
        var x = grid.x + xi * size;
        var y = grid.y + yi * size;
        drawRotatedImage(image, degrees, x, y, size);
    }

    private void drawFood(Grid grid, Image image, int xi, int yi, int size) {
        var x = grid.x + xi * size;
        var y = grid.y + yi * size;
        var g = getGraphicsContext2D();
        g.drawImage(image, x, y, size, size);
    }

    private Grid drawGrid(GameField field, int tileSize) {
        var gridWidth = tileSize * field.getWidth();
        var gridHeight = tileSize * field.getHeight();
        var gridX = (int)getWidth() / 2 - gridWidth / 2;
        var gridY = (int)getHeight() / 2 - gridHeight / 2;
        var g = getGraphicsContext2D();
        g.save();
        g.setStroke(Color.web("#606060"));
        g.setLineWidth(1);
        for(int i = 0; i < field.getHeight(); i++) {
            var y = gridY + i * tileSize;
            g.strokeLine(gridX, y, gridX + gridWidth, y);
        }
        g.strokeLine(gridX, gridY + gridHeight, gridX + gridWidth, gridY + gridHeight);
        for(int j = 0; j < field.getWidth(); j++) {
            var x = gridX + j * tileSize;
            g.strokeLine(x, gridY, x, gridY + gridHeight);
        }
        g.strokeLine(gridX + gridWidth, gridY, gridX + gridWidth, gridY + gridHeight);
        g.restore();
        return new Grid(gridX, gridY, gridWidth, gridHeight);
    }

    private void redrawField(GameField field, SnakeView current, Collection<SnakeView> snakes) {
        clear();
        int width = (int)getWidth() / field.getWidth();
        int height = (int)getHeight() / field.getHeight();
        int size = Math.min(width, height);
        var grid = drawGrid(field, size);
        var apple = AssetsPool.getImage("/images/apple.png");
        for(int i = 0; i < field.getHeight(); i++) {
            for(int j = 0; j < field.getWidth(); j++) {
                var tile = field.getTile(j, i);
                switch (tile) {
                    case FOOD -> drawFood(grid, apple, j, i, size);
                    case SNAKE -> drawGridCellRandom(grid, enemySnake.body(), j, i, size);
                }
            }
        }
        if(current != null) {
            for(var point: current) {
                var x = Math.floorMod(point.x, field.getWidth());
                var y = Math.floorMod(point.y, field.getHeight());
                drawGridCellRandom(grid, playerSnake.body(), x, y, size);
            }
        }
        for(var snake: snakes) {
            var head = snake.getHead();
            drawGridCell(grid, enemySnake.head(), head.x, head.y, size, snake.getDirection());
        }
        if(current != null) {
            var head = current.getHead();
            drawGridCell(grid, playerSnake.head(), head.x, head.y, size, current.getDirection());
        }
    }

    public void drawState(GameStateView gameState, SnakeView current) {
        this.gameState = gameState;
        this.current = current;
        redrawField(gameState.getField(), current, gameState.getSnakesView());
    }
}
