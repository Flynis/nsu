package ru.dyakun.proxy.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.proxy.ChangeKeyOpsRequest;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.UUID;
import java.util.function.Consumer;

public class AcceptableConnection implements Connection {
    private static final Logger logger = LoggerFactory.getLogger(AcceptableConnection.class);
    private final ServerSocketChannel socket;
    private final UUID id;
    private final boolean isMain;

    public AcceptableConnection(ServerSocketChannel socket, boolean isMain) {
        this.socket = socket;
        this.isMain = isMain;
        this.id = ConnectionTable.getInstance().addConnection(this);
    }

    public void accept(Selector selector, Consumer<ChangeKeyOpsRequest> changeRequests) throws IOException {
        SocketChannel client = socket.accept();
        client.configureBlocking(false);
        UUID id = ConnectionTable.getInstance().getFreeId();
        TcpConnection connection = new TcpConnection(client, id,  selector, changeRequests);
        connections.putIfAbsent(id, connection);
        logger.info("Connection established from client {}: {}", id, channel.getRemoteAddress().toString());
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
            logger.error("Server socket channel close failed", e);
        }
    }

}
