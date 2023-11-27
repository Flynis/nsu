package ru.dyakun.proxy.connection;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface ReadWriteConnection extends Connection {

    void receive() throws IOException;

    void requestSend(ByteBuffer data);

    void send() throws IOException;

}
