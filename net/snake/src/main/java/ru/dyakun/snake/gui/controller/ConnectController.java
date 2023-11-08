package ru.dyakun.snake.gui.controller;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import ru.dyakun.snake.game.entity.GameInfoView;
import ru.dyakun.snake.game.event.GameEvent;
import ru.dyakun.snake.gui.scene.SceneName;
import ru.dyakun.snake.protocol.NodeRole;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ConnectController extends AbstractController implements Initializable {
    public TableView<GameDesc> gamesInfoTable;
    public TableColumn<GameDesc, String> nameColumn;
    public TableColumn<GameDesc, String> ipColumn;
    public TableColumn<GameDesc, Integer> playersColumn;
    public TableColumn<GameDesc, String> fieldColumn;
    public TextField nicknameField;
    public Label errorLabel;
    public ChoiceBox<String> choiceRole;
    public ObservableList<GameDesc> games;

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
        game.finishCurrentSession();
        manager.changeScene(SceneName.MENU);
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
            game.connect(nicknameField.getText(), desc.getName(), getRole(choiceRole.getValue()));
            errorLabel.setVisible(false);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().masterProperty());
        ipColumn.setCellValueFactory(cellData -> cellData.getValue().ipProperty());
        playersColumn.setCellValueFactory(cellData -> cellData.getValue().playersProperty().asObject());
        fieldColumn.setCellValueFactory(cellData -> cellData.getValue().fieldProperty());
        games = FXCollections.observableArrayList(new ArrayList<>());
        gamesInfoTable.setItems(games);
        errorLabel.setVisible(false);
        List<String> roles = List.of("Player", "Viewer");
        choiceRole.setItems(FXCollections.observableList(roles));
        choiceRole.setValue(roles.get(0));
    }

    @Override
    public void onEvent(GameEvent event, Object payload) {
        switch (event) {
            case NEW_ACTIVE_GAME -> {
                games.add(GameDesc.fromGameInfo((GameInfoView) payload));
                Platform.runLater(() -> gamesInfoTable.refresh());
            }
            case UPDATE_GAME_INFO -> {
                var gameInfo = (GameInfoView) payload;
                for(var gameDesc: games) {
                    if(gameDesc.getName().equals(gameInfo.getName())) {
                        gameDesc.update(gameInfo);
                        break;
                    }
                }
                Platform.runLater(() -> gamesInfoTable.refresh());
            }
            case DELETE_INACTIVE_GAMES -> {
                var inactive = (String) payload;
                games.removeIf(gameDesc -> gameDesc.getName().equals(inactive));
            }
        }
        if(manager.current() != SceneName.CONNECT) return;
        switch (event) {
            case JOINED -> Platform.runLater(() -> manager.changeScene(SceneName.GAME));
            case MESSAGE -> Platform.runLater(() -> showErrorMessage((String) payload));
        }
    }

    public static class GameDesc {
        StringProperty name;
        StringProperty ip;
        IntegerProperty players;
        StringProperty field;

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

        public GameDesc(String name, String ip, int players, String field) {
            this.name = new SimpleStringProperty(name);
            this.ip = new SimpleStringProperty(ip);
            this.players = new SimpleIntegerProperty(players);
            this.field = new SimpleStringProperty(field);
        }

        public void update(GameInfoView gameInfo) {
            setPlayers(gameInfo.getPlayersCount());
            setField(getFieldString(gameInfo));
        }

        public static GameDesc fromGameInfo(GameInfoView gameInfo) {
            return new GameDesc(
                gameInfo.getName(),
                gameInfo.getIp(),
                gameInfo.getPlayersCount(),
                getFieldString(gameInfo)
            );
        }

        public static String getFieldString(GameInfoView gameInfo) {
            return String.format("%dx%d", gameInfo.getConfig().getWidth(), gameInfo.getConfig().getHeight());
        }
    }
}


