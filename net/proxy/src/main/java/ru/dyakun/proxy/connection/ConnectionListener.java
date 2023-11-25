package ru.dyakun.proxy.connection;

import java.nio.ByteBuffer;

public interface ConnectionListener {

    void onReceive(ByteBuffer buffer);

}
