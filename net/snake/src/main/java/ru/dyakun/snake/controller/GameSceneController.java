package ru.dyakun.snake.controller;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.model.GameInfoView;
import ru.dyakun.snake.model.entity.PlayerView;
import ru.dyakun.snake.model.entity.SnakeView;
import ru.dyakun.snake.model.event.GameEvent;
import ru.dyakun.snake.model.field.GameField;
import ru.dyakun.snake.model.field.Tile;
import ru.dyakun.snake.protocol.Direction;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

public class GameSceneController extends AbstractSceneController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(GameSceneController.class);
    public TableView<ScoreEntry> scoreTable;
    public TableColumn<ScoreEntry, Integer> numberColumn;
    public TableColumn<ScoreEntry, String> nickColumn;
    public TableColumn<ScoreEntry, Integer> scoreColumn;
    public Label gameNameLabel;
    public Label fieldSizeLabel;
    public Label foodLabel;
    public Canvas canvas;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        numberColumn.setCellValueFactory(cellData -> cellData.getValue().numberProperty().asObject());
        nickColumn.setCellValueFactory(cellData -> cellData.getValue().nicknameProperty());
        scoreColumn.setCellValueFactory(cellData -> cellData.getValue().scoreProperty().asObject());
    }
    
    public void backClick() {
        controller.back();
    }

    @Override
    public void onEvent(GameEvent event) {
        switch (event) {
            case REPAINT_FIELD -> {
                var state = controller.getGameState();
                redrawField(state.getField(), state.getSnake());
                updateGameInfo(state.getGameInfo());
                updateScoreTable(state.getPlayers(), state.getPlayer());
            }
            case CLEAR_FIELD -> {
                clearCanvas();
            }
        }
    }

    private static class PlayerComparator implements Comparator<PlayerView> {
        @Override
        public int compare(PlayerView o1, PlayerView o2) {
            return Integer.compare(o1.getScore(), o2.getScore());
        }
    }

    private void updateScoreTable(List<PlayerView> players, PlayerView current) {
        // TODO cur player
        var sorted = players.stream().sorted(new PlayerComparator()).toList();
        var scores = IntStream.range(0, sorted.size()).mapToObj(i -> ScoreEntry.from(sorted.get(i), i)).toList();
        scoreTable.setItems(FXCollections.observableList(scores));
    }

    private void updateGameInfo(GameInfoView gameInfo) {
        gameNameLabel.setText(gameInfo.getName());
        var config = gameInfo.getConfig();
        fieldSizeLabel.setText(String.format("%dx%d", config.getWidth(), config.getHeight()));
        foodLabel.setText(Integer.toString(config.getFoodStatic()));
    }

    private void clearCanvas() {
        var g = canvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void redrawField(GameField field, SnakeView current) {
        // TODO cur snake
        clearCanvas();
        var g = canvas.getGraphicsContext2D();
        int width = (int)canvas.getWidth() / field.getWidth();
        int height = (int)canvas.getHeight() / field.getHeight();
        for(int i = 0; i < field.getHeight(); i++) {
            for(int j = 0; j < field.getWidth(); j++) {
                var tile = field.getTile(j, i);
                switch (tile) {
                    case FOOD, SNAKE -> {
                        if(tile == Tile.FOOD) {
                            g.setFill(Color.YELLOW);
                        } else {
                            g.setFill(Color.RED);
                        }
                        g.fillRect(j * width, i * height, width, height);
                        logger.debug("Draw ({}, {})", j, i);
                    }
                }
            }
        }
    }

    public void handleKey(KeyEvent keyEvent) {
        logger.debug("Pressed {}", keyEvent.getCode());
        switch (keyEvent.getCode()) {
            case W -> controller.changeSnakeDirection(Direction.UP);
            case S -> controller.changeSnakeDirection(Direction.DOWN);
            case D -> controller.changeSnakeDirection(Direction.RIGHT);
            case A -> controller.changeSnakeDirection(Direction.LEFT);
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
