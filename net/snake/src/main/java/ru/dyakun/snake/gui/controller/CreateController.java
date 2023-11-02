package ru.dyakun.snake.gui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import ru.dyakun.snake.game.GameConfig;
import ru.dyakun.snake.game.event.GameEvent;
import ru.dyakun.snake.gui.scene.SceneName;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateController extends AbstractController implements Initializable {
    public TextField nicknameField;
    public Label errorLabel;
    public TextField widthField;
    public TextField heightField;
    public TextField foodField;
    public TextField delayField;
    public Slider widthSlider;
    public Slider heightSlider;
    public Slider foodSlider;
    public Slider delaySlider;

    private boolean validate() {
        if(nicknameField.getText().isBlank()) {
            errorLabel.setText("Enter your nickname");
            errorLabel.setVisible(true);
            return false;
        }
        return true;
    }

    public void backClick(ActionEvent ignored) {
        manager.changeScene(SceneName.MENU);
    }

    public void createGameClick(ActionEvent ignored) {
        if(validate()) {
            var config = new GameConfig.Builder()
                    .width(Integer.parseInt(widthField.getText()))
                    .height(Integer.parseInt(heightField.getText()))
                    .food(Integer.parseInt(foodField.getText()))
                    .delay((int)(Double.parseDouble(delayField.getText()) * 1000))
                    .build();
            game.createGame(nicknameField.getText(), config);
            errorLabel.setVisible(false);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        errorLabel.setVisible(false);
        widthSlider.setOnMouseReleased(event -> widthField.setText(Integer.toString((int) widthSlider.getValue())));
        heightSlider.setOnMouseReleased(event -> heightField.setText(Integer.toString((int) heightSlider.getValue())));
        foodSlider.setOnMouseReleased(event -> foodField.setText(Integer.toString((int) foodSlider.getValue())));
        delaySlider.setOnMouseReleased(event -> delayField.setText(Double.toString(delaySlider.getValue())));
    }

    @Override
    public void onEvent(GameEvent event, Object payload) {
        if (event == GameEvent.JOINED) {
            manager.changeScene(SceneName.GAME);
        }
    }
}
