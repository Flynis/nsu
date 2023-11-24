package ru.dyakun.proxy.connection;

import java.io.IOException;

public interface AcceptableConnection extends Connection {

    void accept() throws IOException;

}
