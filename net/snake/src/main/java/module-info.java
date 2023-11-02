module snake {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires com.google.gson;
    requires protobuf.java;

    opens ru.dyakun.snake;
    opens ru.dyakun.snake.gui.controller to javafx.fxml;
    opens ru.dyakun.snake.gui.scene to javafx.fxml;
    opens ru.dyakun.snake.game to com.google.gson;
}