package ru.dyakun.proxy.connection;

import java.io.IOException;
import java.net.InetSocketAddress;

public interface ConnectableConnection extends Connection {

    void requestConnect(InetSocketAddress address) throws IOException;

    void connect() throws IOException;

}
