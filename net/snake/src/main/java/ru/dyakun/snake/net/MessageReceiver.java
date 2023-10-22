package ru.dyakun.snake.net;

public interface MessageReceiver extends Runnable, Stoppable {
    void addMessageListener(GameMessageListener listener);
}
