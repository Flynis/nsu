package ru.dyakun.snake.game.event;

@FunctionalInterface
public interface GameEventListener {
    void onEvent(GameEvent event, Object payload);
}
