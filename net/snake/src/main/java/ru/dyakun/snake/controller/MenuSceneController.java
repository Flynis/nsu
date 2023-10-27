package ru.dyakun.snake.controller;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import ru.dyakun.snake.model.GameInfo;
import ru.dyakun.snake.model.GameInfoView;
import ru.dyakun.snake.model.event.GameEvent;
import ru.dyakun.snake.protocol.NodeRole;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MenuSceneController extends AbstractSceneController implements Initializable {
    public TableView<GameDesc> gamesInfoTable;
    public TableColumn<GameDesc, String> nameColumn;
    public TableColumn<GameDesc, String> ipColumn;
    public TableColumn<GameDesc, Integer> playersColumn;
    public TableColumn<GameDesc, String> fieldColumn;
    public TableColumn<GameDesc, String> foodColumn;
    public TextField nicknameField;
    public Label errorLabel;
    public ChoiceBox<String> choiceRole;

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

    public void exitClick(ActionEvent ignored) {
        controller.exit();
    }

    public void createGameClick(ActionEvent ignored) {
        if(validate()) {
            controller.createGame(nicknameField.getText());
            errorLabel.setVisible(false);
        }
    }

    private NodeRole getRole(String role) {
        switch (role) {
            case "Player" -> {
                return NodeRole.NORMAL;
            }
            case "Viewer" -> {
                return NodeRole.VIEWER;
            }
            default -> throw new IllegalStateException("Unknown role");
        }
    }

    public void connectClick(ActionEvent ignored) {
        if(validate()) {
            var desc = gamesInfoTable.getSelectionModel().getSelectedItem();
            if(desc == null) {
                showErrorMessage("Choose game");
                return;
            }
            controller.connect(nicknameField.getText(), desc.getName(), getRole(choiceRole.getValue()));
            errorLabel.setVisible(false);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().masterProperty());
        ipColumn.setCellValueFactory(cellData -> cellData.getValue().ipProperty());
        playersColumn.setCellValueFactory(cellData -> cellData.getValue().playersProperty().asObject());
        fieldColumn.setCellValueFactory(cellData -> cellData.getValue().fieldProperty());
        foodColumn.setCellValueFactory(cellData -> cellData.getValue().foodProperty());
        List<GameDesc> orders = new ArrayList<>();
        ObservableList<GameDesc> ol = FXCollections.observableArrayList(orders);
        gamesInfoTable.setItems(ol);
        errorLabel.setVisible(false);
        List<String> roles = List.of("Player", "Viewer");
        choiceRole.setItems(FXCollections.observableList(roles));
        choiceRole.setValue(roles.get(0));
    }

    @Override
    public void onEvent(GameEvent event, Object payload) {
        // TODO handle payload
        if(event == GameEvent.UPDATE_ACTIVE_GAMES) {
            var activeGames = controller.getActiveGames();
            var games = activeGames.stream().map(GameDesc::fromGameInfo).toList();
            ObservableList<GameDesc> ol = FXCollections.observableArrayList(games);
            gamesInfoTable.setItems(ol);
        }
    }

    public static class GameDesc {
        StringProperty name;
        StringProperty ip;
        IntegerProperty players;
        StringProperty field;
        StringProperty food;

        public String getName() {
            return name.get();
        }

        public StringProperty masterProperty() {
            return name;
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public String getIp() {
            return ip.get();
        }

        public StringProperty ipProperty() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip.set(ip);
        }

        public int getPlayers() {
            return players.get();
        }

        public IntegerProperty playersProperty() {
            return players;
        }

        public void setPlayers(int players) {
            this.players.set(players);
        }

        public String getField() {
            return field.get();
        }

        public StringProperty fieldProperty() {
            return field;
        }

        public void setField(String field) {
            this.field.set(field);
        }

        public String getFood() {
            return food.get();
        }

        public StringProperty foodProperty() {
            return food;
        }

        public void setFood(String food) {
            this.food.set(food);
        }

        public GameDesc(String name, String ip, int players, String field, String food) {
            this.name = new SimpleStringProperty(name);
            this.ip = new SimpleStringProperty(ip);
            this.players = new SimpleIntegerProperty(players);
            this.field = new SimpleStringProperty(field);
            this.food = new SimpleStringProperty(food);
        }

        public static GameDesc fromGameInfo(GameInfoView gameInfo) {
            return new GameDesc(
                gameInfo.getName(),
                "0.0.0.0",
                gameInfo.getPlayersCount(),
                String.format("%dx%d", gameInfo.getConfig().getWidth(), gameInfo.getConfig().getHeight()),
                Integer.toString(gameInfo.getConfig().getFoodStatic())
            );
        }
    }
}


