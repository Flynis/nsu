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
    opens ru.dyakun.snake.model to com.google.gson, javafx.fxml;
    opens ru.dyakun.snake.model.field to com.google.gson, javafx.fxml;
    opens ru.dyakun.snake.model.entity to com.google.gson, javafx.fxml; // TODO check
}