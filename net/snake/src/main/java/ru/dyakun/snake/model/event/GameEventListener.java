package ru.dyakun.snake.model.event;

public interface GameEventListener {
    void onEvent(GameEvent event, Object payload);
}
