package ru.dyakun.proxy.connection;

public enum ConnectionState {
    HANDSHAKE,
    REQUEST,
    CONNECT,
    WAIT_CONNECT;
}
