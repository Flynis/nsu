package ru.dyakun.snake.net;

import ru.dyakun.snake.model.util.MessageType;
import ru.dyakun.snake.protocol.GameMessage;

import java.net.SocketAddress;

public interface NetClient extends MessageReceiver {
    void send(MessageType type, GameMessage message, SocketAddress receiver);
}
