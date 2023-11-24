package ru.dyakun.proxy.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.proxy.ChangeKeyOpsRequest;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.UUID;
import java.util.function.Consumer;

public class TcpConnection implements Connection {
    private static final Logger logger = LoggerFactory.getLogger(TcpConnection.class);
    private final SocketChannel socket;
    private final UUID id;
    private final Consumer<ChangeKeyOpsRequest> changeRequests;
    private final Selector selector;
    private TcpConnection destination;
    private ConnectionState state;

    public TcpConnection(SocketChannel socket, UUID id, Selector selector,
                         Consumer<ChangeKeyOpsRequest> changeRequests, TcpConnection destination) {
        this.socket = socket;
        this.id = id;
        this.selector = selector;
        this.changeRequests = changeRequests;
        this.destination = destination;
        this.state = ConnectionState.CONNECT;
    }

    public TcpConnection(SocketChannel socket, UUID id, Selector selector, Consumer<ChangeKeyOpsRequest> changeRequests)
            throws ClosedChannelException {
        this(socket, id, selector, changeRequests, null);
        this.state = ConnectionState.HANDSHAKE;
        socket.register(selector, SelectionKey.OP_READ, this);
    }

    public void setDestination(TcpConnection connection) {
        this.destination = connection;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.error("Socket channel close failed", e);
        }
    }

}
