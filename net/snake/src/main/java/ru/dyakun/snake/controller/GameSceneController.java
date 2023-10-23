package ru.dyakun.snake.controller;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.model.event.GameEvent;
import ru.dyakun.snake.model.field.Tile;
import ru.dyakun.snake.protocol.Direction;

import java.net.URL;
import java.util.ResourceBundle;

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
        if(event == GameEvent.REPAINT_FIELD) {
            var field = controller.getField();
            var g = canvas.getGraphicsContext2D();
            g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g.setFill(Color.BLACK);
            g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
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
    }
}
