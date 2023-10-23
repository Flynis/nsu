package ru.dyakun.snake.net;

import ru.dyakun.snake.protocol.GameMessage;

import java.net.SocketAddress;

public interface GameMessageListener {
    void handle(GameMessage message, SocketAddress receiver);
}
