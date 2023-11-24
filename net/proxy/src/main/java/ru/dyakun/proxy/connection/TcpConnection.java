package ru.dyakun.proxy.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.proxy.ChangeKeyOpsRequest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Consumer;

import static java.nio.channels.SelectionKey.OP_WRITE;

public class TcpConnection implements ReadWriteConnection, ConnectableConnection {
    private static final Logger logger = LoggerFactory.getLogger(TcpConnection.class);
    private SocketChannel socket;
    private final UUID id;
    private final Consumer<ChangeKeyOpsRequest> changeRequests;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(4096);
    private final Queue<ByteBuffer> dataQueue = new ArrayDeque<>();
    private final Selector selector;
    private TcpClientConnection destination;

    public TcpConnection(UUID id, Selector selector,
                               Consumer<ChangeKeyOpsRequest> changeRequests, TcpClientConnection destination) {
        this.id = id;
        this.selector = selector;
        this.changeRequests = changeRequests;
        this.destination = destination;
    }

    @Override
    public UUID getId() {
        return null;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public String getAddress() {
        return null;
    }

    @Override
    public void receive() throws IOException {

    }

    @Override
    public void requestSend(ByteBuffer data) {

    }

    @Override
    public void send() throws IOException {

    }

    @Override
    public void requestConnect(InetSocketAddress address) throws IOException {
        logger.info("Try to connect...");
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        connection = new Connection(selector, channel, changeRequests::add);
        channel.register(selector, SelectionKey.OP_CONNECT, connection);
        channel.connect(new InetSocketAddress(config.getHost(), config.getPort()));
        selector.wakeup();
    }

    @Override
    public void connect() {
        if(socket.finishConnect()) {
            SelectionKey key = getSelectionKey();
            key.interestOps(OP_WRITE);
        }
    }

}
