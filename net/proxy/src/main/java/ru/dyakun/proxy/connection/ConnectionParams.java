package ru.dyakun.proxy.connection;

import ru.dyakun.proxy.ChangeOpReq;

import java.nio.channels.Selector;
import java.util.UUID;
import java.util.function.Consumer;

public record ConnectionParams(
        UUID id,
        Selector selector,
        Consumer<ChangeOpReq> changeRequests) {
}
