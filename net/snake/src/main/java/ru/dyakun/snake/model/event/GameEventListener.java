package ru.dyakun.snake.model.event;

@FunctionalInterface
public interface GameEventListener {
    void onEvent(GameEvent event, Object payload);
}
