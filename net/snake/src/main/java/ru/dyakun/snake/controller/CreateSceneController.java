package ru.dyakun.snake.controller;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import ru.dyakun.snake.model.event.GameEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateSceneController extends AbstractSceneController implements Initializable {
    public TextField nicknameField;
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
        controller.exit();
    }

    public void createGameClick(ActionEvent ignored) {
        if(validate()) {
            controller.createGame(nicknameField.getText());
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


