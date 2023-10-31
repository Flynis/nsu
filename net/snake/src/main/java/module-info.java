module snake {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires com.google.gson;
    requires protobuf.java;

    opens ru.dyakun.snake;
    opens ru.dyakun.snake.gui.javafx to javafx.fxml;
    opens ru.dyakun.snake.controller to javafx.fxml;
    opens ru.dyakun.snake.game to com.google.gson, javafx.fxml;
    opens ru.dyakun.snake.game.entity to com.google.gson, javafx.fxml;
    opens ru.dyakun.snake.game.person to com.google.gson, javafx.fxml;
}