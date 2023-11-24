package ru.dyakun.proxy.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.proxy.ChangeKeyOpsRequest;
import ru.dyakun.proxy.message.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Consumer;

import static java.nio.channels.SelectionKey.*;

public class TcpClientConnection implements ReadWriteConnection {
    private static final Logger logger = LoggerFactory.getLogger(TcpClientConnection.class);
    private final SocketChannel socket;
    private final UUID id;
    private final Consumer<ChangeKeyOpsRequest> changeRequests;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(4096);
    private final Queue<ByteBuffer> dataQueue = new ArrayDeque<>();
    private final Selector selector;
    private ReadWriteConnection destination;
    private ConnectionState state;
    private boolean closed = false;

    public TcpClientConnection(SocketChannel socket, UUID id, Selector selector, Consumer<ChangeKeyOpsRequest> changeRequests)
            throws ClosedChannelException {
        this.socket = socket;
        this.id = id;
        this.selector = selector;
        this.changeRequests = changeRequests;
        this.state = ConnectionState.HANDSHAKE;
        socket.register(selector, OP_READ, this);
    }

    public void setDestination(ReadWriteConnection connection) {
        this.destination = connection;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        try {
            socket.close();
            closed = true;
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

    @Override
    public void receive() throws IOException {
        int read = socket.read(readBuffer);
        if(read < 0) {
            throw new IOException("Cannot read data from channel");
        }
        handleReceivedData();
    }

    private void handleReceivedData() throws IOException {
        readBuffer.flip();
        try {
            switch (state) {
                case HANDSHAKE -> {
                    HandshakeMsg msg = HandshakeMsg.parseFrom(readBuffer);
                    if (msg.getVersion() != SocksMessages.VERSION) {
                        throw new IOException("Unsupported version");
                    }
                    var reply = SocksMessages.buildHandshakeReplyMsg(SocksMessages.NO_AUTHENTICATION_REQUIRED);
                    requestSend(reply);
                    state = ConnectionState.REQUEST;
                }
                case REQUEST -> {
                    RequestMsg msg = RequestMsg.parseFrom(readBuffer);
                    if (msg.getCommand() != RequestCommand.CONNECT) {
                        var reply = SocksMessages.buildReplyMsg(ReplyCode.COMMAND_NOT_SUPPORTED);
                        requestSend(reply);
                    }
                    if(msg.getAddressType() != AddressType.IPV4 || msg.getAddressType() != AddressType.DOMAIN_NAME) {
                        var reply = SocksMessages.buildReplyMsg(ReplyCode.ADDRESS_TYPE_NOT_SUPPORTED);
                        requestSend(reply);
                        // TODO close connection
                    }
                    switch (msg.getAddressType()) {
                        case IPV4 -> {
                            UUID id = ConnectionTable.getInstance().getFreeId();
                            TcpConnection connection = new TcpConnection(id, selector, changeRequests,this);
                            setDestination(connection);
                            InetAddress address = msg.getDstAddress();
                            connection.requestConnect(new InetSocketAddress(address, msg.getDstPort()));
                        }
                        case DOMAIN_NAME -> {
                            // TODO
                        }
                    }
                }
                case CONNECT -> {
                    if(destination.isClosed()) {
                        throw new IOException("Destination closed");
                    }
                    logger.debug("Receive {}b from {}", readBuffer.limit(), getAddress());
                    ByteBuffer data = ByteBuffer.allocate(readBuffer.limit());
                    data.put(readBuffer);
                    readBuffer.compact();
                    destination.requestSend(data);
                }
            }
        } catch (MessageParseException e) {
            throw new IOException("Message parse failed", e);
        }
    }

    public void destFinishConnect() {
        // TODO
    }

    @Override
    public void requestSend(ByteBuffer data) {
        data.flip();
        dataQueue.add(data);
        SelectionKey key = getSelectionKey();
        if(key != null && key.isValid() && key.interestOps() == OP_READ) {
            changeRequests.accept(new ChangeKeyOpsRequest(key, OP_WRITE));
            selector.wakeup();
        }
    }

    @Override
    public void send() throws IOException {
        while (!dataQueue.isEmpty()) {
            ByteBuffer data = dataQueue.peek();
            socket.write(data);
            if(data.hasRemaining()) {
                return;
            }
            logger.debug("Send {}b to {}", data.limit(), getAddress());
            dataQueue.remove();
        }
        changeRequests.accept(new ChangeKeyOpsRequest(getSelectionKey(), OP_READ));
    }

    private SelectionKey getSelectionKey() {
        return socket.keyFor(selector);
    }

}
