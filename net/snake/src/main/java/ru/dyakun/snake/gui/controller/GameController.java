package ru.dyakun.snake.gui.controller;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.game.entity.PlayerView;
import ru.dyakun.snake.game.event.GameEvent;
import ru.dyakun.snake.gui.components.GameCanvas;
import ru.dyakun.snake.gui.scene.SceneName;
import ru.dyakun.snake.protocol.Direction;

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
    public Label messageLabel;
    public StackPane canvasParent;
    private GameCanvas canvas;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        numberColumn.setCellValueFactory(cellData -> cellData.getValue().numberProperty().asObject());
        nickColumn.setCellValueFactory(cellData -> cellData.getValue().nicknameProperty());
        scoreColumn.setCellValueFactory(cellData -> cellData.getValue().scoreProperty().asObject());
        messageLabel.setVisible(false);
        canvas = new GameCanvas();
        canvas.setParent(canvasParent);
        scoreTable.addEventFilter(KeyEvent.KEY_PRESSED, Event::consume);
        scoreTable.setRowFactory((TableView<ScoreEntry> row) -> new TableRow<>() {
            @Override
            public void updateItem(ScoreEntry entry, boolean empty) {
                setStyle("");
                super.updateItem(entry, empty);
                if (entry == null || empty) {
                    setStyle("");
                } else {
                    var player = game.getPlayer();
                    if(player != null && entry.getNickname().equals(player.getName())) {
                        setStyle("-fx-text-background-color: #33ff33");
                    }
                }
            }
        });
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
                if(state == null) return;
                updateScoreTable(state.getGamePlayers());
                canvas.drawState(state, game.getSnake());
                Platform.runLater(() -> scoreTable.refresh());
            }
            case MESSAGE -> {
                if(manager.current() == SceneName.GAME) {
                    Platform.runLater(() -> setMessage((String) payload));
                }
            }
            case JOINED -> {
                scoreTable.setItems(FXCollections.emptyObservableList());
                Platform.runLater(() -> messageLabel.setVisible(false));
            }
        }
    }

    private static class PlayerComparator implements Comparator<PlayerView> {
        @Override
        public int compare(PlayerView o1, PlayerView o2) {
            return Integer.compare(o1.getScore(), o2.getScore());
        }
    }

    private void updateScoreTable(Collection<PlayerView> players) {
        var sorted = players.stream().sorted(new PlayerComparator().reversed()).toList();
        var scores = IntStream.range(0, sorted.size())
                .mapToObj(i -> ScoreEntry.from(sorted.get(i), i + 1)).toList();
        scoreTable.setItems(FXCollections.observableList(scores));
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
