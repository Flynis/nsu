package ru.dyakun.snake.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import ru.dyakun.snake.game.event.GameEvent;
import ru.dyakun.snake.gui.base.SceneName;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateController extends AbstractController implements Initializable {
    @FXML
    public TextField nicknameField;
    @FXML
    public Label errorLabel;

    private void showErrorMessage(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private boolean validate() {
        if(nicknameField.getText().isBlank()) {
            showErrorMessage("Enter your nickname");
            return false;
        }
        return true;
    }

    public void backClick(ActionEvent ignored) {
        window.changeScene(SceneName.MENU);
    }

    public void createGameClick(ActionEvent ignored) {
        if(validate()) {
            game.createGame(nicknameField.getText());
            errorLabel.setVisible(false);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @Override
    public void onEvent(GameEvent event, Object payload) {

    }

}


