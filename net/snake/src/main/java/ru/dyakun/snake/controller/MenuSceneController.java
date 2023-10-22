package ru.dyakun.snake.controller;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MenuSceneController extends AbstractSceneController implements Initializable {
    public TableView<GameInfo> gamesInfoTable;
    public TableColumn<GameInfo, String> masterColumn;
    public TableColumn<GameInfo, String> ipColumn;
    public TableColumn<GameInfo, Integer> playersColumn;
    public TableColumn<GameInfo, String> fieldColumn;
    public TableColumn<GameInfo, String> foodColumn;

    public void exitClick(ActionEvent ignored) {
        controller.exit();
    }

    public void createGameClick(ActionEvent ignored) {
        controller.createGame();
    }

    public void connectClick(ActionEvent ignored) {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        masterColumn.setCellValueFactory(cellData -> cellData.getValue().masterProperty());
        ipColumn.setCellValueFactory(cellData -> cellData.getValue().ipProperty());
        playersColumn.setCellValueFactory(cellData -> cellData.getValue().playersProperty().asObject());
        fieldColumn.setCellValueFactory(cellData -> cellData.getValue().fieldProperty());
        foodColumn.setCellValueFactory(cellData -> cellData.getValue().foodProperty());

        List<GameInfo> orders = new ArrayList<>();
        orders.add(new GameInfo("Vasya", "192.168.255.255", 2, "20x20", "1+2x"));
        orders.add(new GameInfo("Vasya", "192.168.255.255", 2, "20x20", "1+2x"));
        orders.add(new GameInfo("Vasya", "192.168.255.255", 2, "20x20", "1+2x"));
        orders.add(new GameInfo("Vasya", "192.168.255.255", 2, "20x20", "1+2x"));
        orders.add(new GameInfo("Vasya", "192.168.255.255", 2, "20x20", "1+2x"));
        orders.add(new GameInfo("Vasya", "192.168.255.255", 2, "20x20", "1+2x"));
        orders.add(new GameInfo("Vasya", "192.168.255.255", 2, "20x20", "1+2x"));
        orders.add(new GameInfo("Vasya", "192.168.255.255", 2, "20x20", "1+2x"));
        orders.add(new GameInfo("Vasya", "192.168.255.255", 2, "20x20", "1+2x"));
        orders.add(new GameInfo("Vasya", "192.168.255.255", 2, "20x20", "1+2x"));
        orders.add(new GameInfo("Vasya", "192.168.255.255", 2, "20x20", "1+2x"));
        orders.add(new GameInfo("Vasya", "192.168.255.255", 2, "20x20", "1+2x"));
        orders.add(new GameInfo("Vasya", "192.168.255.255", 2, "20x20", "1+2x"));
        orders.add(new GameInfo("Vasya", "192.168.255.255", 2, "20x20", "1+2x"));
        orders.add(new GameInfo("Vasya", "192.168.255.255", 2, "20x20", "1+2x"));
        orders.add(new GameInfo("Vasya", "192.168.255.255", 2, "20x20", "1+2x"));
        orders.add(new GameInfo("Vasya", "192.168.255.255", 2, "20x20", "1+2x"));

        ObservableList<GameInfo> ol = FXCollections.observableArrayList(orders);
        gamesInfoTable.setItems(ol);
    }

    public static class GameInfo {
        StringProperty master;
        StringProperty ip;
        IntegerProperty players;
        StringProperty field;

        public String getMaster() {
            return master.get();
        }

        public StringProperty masterProperty() {
            return master;
        }

        public void setMaster(String master) {
            this.master.set(master);
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

        StringProperty food;

        public GameInfo(String master, String ip, int players, String field, String food) {
            this.master = new SimpleStringProperty(master);
            this.ip = new SimpleStringProperty(ip);
            this.players = new SimpleIntegerProperty(players);
            this.field = new SimpleStringProperty(field);
            this.food = new SimpleStringProperty(food);
        }
    }
}


