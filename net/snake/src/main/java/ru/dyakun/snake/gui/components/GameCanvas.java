package ru.dyakun.snake.gui.components;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import ru.dyakun.snake.game.entity.GameStateView;
import ru.dyakun.snake.game.entity.SnakeView;
import ru.dyakun.snake.game.field.GameField;
import ru.dyakun.snake.protocol.Direction;
import ru.dyakun.snake.util.AssetsPool;

import java.util.Collection;

public class GameCanvas extends ResizableCanvas {
    private record Snake(Image head, Image body) { }
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
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawSnakeRandom(Image image, int x, int y, int width, int height, GraphicsContext g) {
        var random = Direction.random();
        drawSnakeTile(image, x, y, width, height, random, g);
    }

    private void drawSnakeTile(Image image, int x, int y, int width, int height, Direction direction, GraphicsContext g) {
        var degrees = Direction.toDegrees(direction);
        g.save();
        g.rotate(degrees);
        g.drawImage(image, x, y, width, height);
        g.restore();
    }

    private void redrawField(GameField field, SnakeView current, Collection<SnakeView> snakes) {
        clear();
        var g = getGraphicsContext2D();
        int width = (int)getWidth() / field.getWidth();
        int height = (int)getHeight() / field.getHeight();
        var apple = AssetsPool.getImage("/images/apple.png");
        for(int i = 0; i < field.getHeight(); i++) {
            for(int j = 0; j < field.getWidth(); j++) {
                var tile = field.getTile(j, i);
                switch (tile) {
                    case FOOD -> g.drawImage(apple, j * width, i * height, width, height);
                    case SNAKE -> drawSnakeRandom(enemySnake.body(), j * width, i * height, width, height, g);
                }
            }
        }
        for(var point: current) {
            drawSnakeRandom(playerSnake.body(), point.x * width, point.y * height, width, height, g);
        }
        for(var snake: snakes) {
            var head = snake.getHead();
            drawSnakeTile(enemySnake.head(), head.x * width, head.y * height, width, height, snake.getDirection(), g);
        }
        var head = current.getHead();
        drawSnakeTile(playerSnake.head(), head.x * width, head.y * height, width, height, current.getDirection(), g);
    }

    public void drawState(GameStateView gameState, SnakeView current) {
        this.gameState = gameState;
        this.current = current;
        redrawField(gameState.getField(), current, gameState.getSnakesView());
    }
}
