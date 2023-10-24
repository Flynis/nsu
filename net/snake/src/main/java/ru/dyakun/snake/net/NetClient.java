package ru.dyakun.snake.net;

import ru.dyakun.snake.protocol.GameMessage;

public interface NetClient extends MessageReceiver {
    void send(GameMessage message);
}
