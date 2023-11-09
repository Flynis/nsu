package ru.dyakun.snake.gui.controller;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.game.entity.PlayerView;
import ru.dyakun.snake.game.entity.SnakeView;
import ru.dyakun.snake.game.event.GameEvent;
import ru.dyakun.snake.game.field.GameField;
import ru.dyakun.snake.gui.scene.SceneName;
import ru.dyakun.snake.protocol.Direction;
import ru.dyakun.snake.util.AssetsPool;

import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

public class GameController extends AbstractController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    public TableView<ScoreEntry> scoreTable;
    public TableColumn<ScoreEntry, Integer> numberColumn;
    public TableColumn<ScoreEntry, String> nickColumn;
    public TableColumn<ScoreEntry, Integer> scoreColumn;
    public Canvas canvas;
    public Label messageLabel;
    public Snake playerSnake;
    public Snake enemySnake;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        numberColumn.setCellValueFactory(cellData -> cellData.getValue().numberProperty().asObject());
        nickColumn.setCellValueFactory(cellData -> cellData.getValue().nicknameProperty());
        scoreColumn.setCellValueFactory(cellData -> cellData.getValue().scoreProperty().asObject());
        messageLabel.setVisible(false);
        playerSnake = new Snake(
                AssetsPool.getImage("/images/green_head.png"),
                AssetsPool.getImage("/images/green.png"));
        enemySnake = new Snake(
                AssetsPool.getImage("/images/red_head.png"),
                AssetsPool.getImage("/images/red.png"));
    }

    private void setMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
    }

    public void leaveClick() {
        game.leave();
        manager.changeScene(SceneName.MENU);
    }

    @Override
    public void onEvent(GameEvent event, Object payload) {
        switch (event) {
            case REPAINT -> {
                var state = game.getGameState();
                redrawField(state.getField(), game.getSnake(), game.getSnakes());
                updateScoreTable(state.getGamePlayers(), game.getPlayer());
            }
            case MESSAGE -> {
                if(manager.current() == SceneName.GAME) {
                    Platform.runLater(() -> setMessage((String) payload));
                }
            }
        }
    }

    private static class PlayerComparator implements Comparator<PlayerView> {
        @Override
        public int compare(PlayerView o1, PlayerView o2) {
            return Integer.compare(o1.getScore(), o2.getScore());
        }
    }

    private void updateScoreTable(Collection<PlayerView> players, PlayerView current) {
        // TODO cur player
        var sorted = players.stream().sorted(new PlayerComparator().reversed()).toList();
        var scores = IntStream.range(0, sorted.size())
                .mapToObj(i -> ScoreEntry.from(sorted.get(i), i + 1)).toList();
        scoreTable.setItems(FXCollections.observableList(scores));
    }

    private void clearCanvas() {
        var g = canvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
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
        clearCanvas();
        var g = canvas.getGraphicsContext2D();
        int width = (int)canvas.getWidth() / field.getWidth();
        int height = (int)canvas.getHeight() / field.getHeight();
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

    public void handleKey(KeyEvent keyEvent) {
        logger.debug("Pressed {}", keyEvent.getCode());
        switch (keyEvent.getCode()) {
            case W, UP -> game.moveSnake(Direction.UP);
            case S, DOWN -> game.moveSnake(Direction.DOWN);
            case D, RIGHT -> game.moveSnake(Direction.RIGHT);
            case A, LEFT -> game.moveSnake(Direction.LEFT);
        }
    }

    public static class ScoreEntry {
        IntegerProperty number;
        StringProperty nickname;
        IntegerProperty score;

        public int getNumber() {
            return number.get();
        }

        public IntegerProperty numberProperty() {
            return number;
        }

        public void setNumber(int number) {
            this.number.set(number);
        }

        public String getNickname() {
            return nickname.get();
        }

        public StringProperty nicknameProperty() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname.set(nickname);
        }

        public int getScore() {
            return score.get();
        }

        public IntegerProperty scoreProperty() {
            return score;
        }

        public void setScore(int score) {
            this.score.set(score);
        }

        public ScoreEntry(int number, String nickname, int score) {
            this.number = new SimpleIntegerProperty(number);
            this.nickname = new SimpleStringProperty(nickname);
            this.score = new SimpleIntegerProperty(score);
        }

        public static ScoreEntry from(PlayerView player, int number) {
            return new ScoreEntry(number, player.getName(), player.getScore());
        }
    }
}
