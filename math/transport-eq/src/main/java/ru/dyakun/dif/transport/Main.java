package ru.dyakun.dif.transport;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Transport equation");
        stage.setResizable(false);
        var url = Main.class.getResource("/MainScene.fxml");
        if(url == null) {
            logger.error("Main scene not found");
            stage.close();
            return;
        }
        FXMLLoader loader = new FXMLLoader(url);
        Parent parent = loader.load();
        MainScene scene = loader.getController();
        scene.setStage(stage);
        stage.setScene(new Scene(parent));
        stage.show();
    }

}