package ru.dyakun.snake.model;

public class PlayerJoinException extends Exception {
    public PlayerJoinException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlayerJoinException(String message) {
        super(message);
    }
}
