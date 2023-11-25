package ru.dyakun.proxy.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public abstract class AbstractTcpConnection extends AbstractReadWriteConnection {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractTcpConnection.class);
    protected final SocketChannel socket;

    public AbstractTcpConnection(SocketChannel socket, ConnectionParams params) {
        super(params);
        this.socket = socket;
    }

    @Override
    protected SelectionKey getSelectionKey() {
        return socket.keyFor(selector);
    }

    @Override
    public boolean isClosed() {
        return !socket.isOpen();
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.warn("Socket channel close failed", e);
        }
    }

    @Override
    public String getAddress() {
        try {
            InetSocketAddress socketAddress = (InetSocketAddress) socket.getRemoteAddress();
            InetAddress address = socketAddress.getAddress();
            return String.format("%s:%s", address.getHostAddress(), socketAddress.getPort());
        } catch (IOException e) {
            logger.warn("Get remote address failed");
        }
        return "";
    }

}
