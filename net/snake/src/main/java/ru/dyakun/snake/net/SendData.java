package ru.dyakun.snake.net;

import ru.dyakun.snake.model.util.MessageType;
import ru.dyakun.snake.protocol.GameMessage;

import java.net.InetSocketAddress;

public record SendData(MessageType type, GameMessage message, InetSocketAddress receiver) { }
