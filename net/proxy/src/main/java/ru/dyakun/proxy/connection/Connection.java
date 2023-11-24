package ru.dyakun.proxy.connection;

import java.util.UUID;

public interface Connection {

    UUID getId();

    void close();

}
