package ru.dyakun.proxy.connection;

import java.io.IOException;

public interface ConnectableConnection extends Connection {

    void connect() throws IOException;

}
