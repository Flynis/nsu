package ru.dyakun.proxy.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.proxy.ChangeOpReq;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.UUID;
import java.util.function.Consumer;

import static java.nio.channels.SelectionKey.OP_ACCEPT;

public class MainProxyConnection implements AcceptableConnection {
    private static final Logger logger = LoggerFactory.getLogger(MainProxyConnection.class);
    private final ServerSocketChannel socket;
    private final Selector selector;
    private final Consumer<ChangeOpReq> changeRequests;
    private final UUID id;

    public MainProxyConnection(InetSocketAddress address, ConnectionParams params) throws IOException {
        this.selector = params.selector();
        this.changeRequests = params.changeRequests();
        this.id = params.id();
        socket = ServerSocketChannel.open();
        socket.configureBlocking(false);
        socket.bind(address);
        socket.register(selector, OP_ACCEPT, this);
    }

    @Override
    public void accept() throws IOException {
        SocketChannel client = socket.accept();
        client.configureBlocking(false);
        UUID id = ConnectionTable.getInstance().getFreeId();
        var params = new ConnectionParams(id,  selector, changeRequests);
        TcpClientConnection connection = new TcpClientConnection(client, params);
        ConnectionTable.getInstance().add(id, connection);
        logger.info("Connection established with {}", connection.getAddress());
    }

    @Override
    public UUID getId() {
        return id;
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
            logger.warn("Server socket channel close failed", e);
        }
    }

    @Override
    public String getAddress() {
        try {
            InetSocketAddress socketAddress = (InetSocketAddress) socket.getLocalAddress();
            InetAddress address = socketAddress.getAddress();
            return String.format("%s:%s", address.getHostAddress(), socketAddress.getPort());
        } catch (IOException e) {
            logger.warn("Get server local address failed");
        }
        return "";
    }

}
