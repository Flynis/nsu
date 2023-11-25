package ru.dyakun.proxy.connection;

import ru.dyakun.proxy.ChangeOpReq;
import ru.dyakun.proxy.dns.ResolveExpectant;
import ru.dyakun.proxy.message.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.UUID;

import static java.nio.channels.SelectionKey.*;

public class TcpClientConnection extends AbstractTcpConnection implements ResolveExpectant {
    private TcpRedirectConnection destination;
    private ConnectionState state;

    public TcpClientConnection(SocketChannel socket, ConnectionParams params)
            throws ClosedChannelException {
        super(socket, params);
        this.state = ConnectionState.HANDSHAKE;
        socket.register(selector, OP_READ, this);
    }

    public void setDestination(TcpRedirectConnection connection) {
        this.destination = connection;
    }

    @Override
    public void receive() throws IOException {
        int read = socket.read(inputBuffer);
        if(read < 0) {
            throw new IOException("Cannot read data from channel");
        }
        inputBuffer.flip();
        handleReceivedData();
        inputBuffer.clear();
    }

    private void handleReceivedData() throws IOException {
        try {
            switch (state) {
                case HANDSHAKE -> {
                    HandshakeMsg msg = HandshakeMsg.parseFrom(inputBuffer);
                    if (msg.getVersion() != SocksMessages.VERSION) {
                        throw new IOException("Unsupported version");
                    }
                    var reply = SocksMessages.buildHandshakeReplyMsg(SocksMessages.NO_AUTHENTICATION_REQUIRED);
                    requestSend(reply);
                    state = ConnectionState.REQUEST;
                }
                case REQUEST -> {
                    RequestMsg msg = RequestMsg.parseFrom(inputBuffer);
                    if (msg.getCommand() != RequestCommand.CONNECT) {
                        var reply = SocksMessages.buildReplyMsg(ReplyCode.COMMAND_NOT_SUPPORTED);
                        requestSend(reply);
                        ConnectionTable.getInstance().markClosed(id);
                    }
                    if(msg.getAddressType() != AddressType.IPV4 || msg.getAddressType() != AddressType.DOMAIN_NAME) {
                        var reply = SocksMessages.buildReplyMsg(ReplyCode.ADDRESS_TYPE_NOT_SUPPORTED);
                        requestSend(reply);
                        ConnectionTable.getInstance().markClosed(id);
                    }
                    switch (msg.getAddressType()) {
                        case IPV4 -> {
                            UUID id = ConnectionTable.getInstance().getFreeId();
                            var params = new ConnectionParams(id, selector, changeRequests);
                            TcpRedirectConnection connection = new TcpRedirectConnection(this, params);
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
                    logger.debug("Receive {}b from {}", inputBuffer.limit(), getAddress());
                    var data = ByteBuffers.copyFrom(inputBuffer);
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

    private void connectToDesiredAddress(InetAddress address) {

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
        changeRequests.accept(new ChangeOpReq(getSelectionKey(), OP_READ));
    }

    @Override
    public void onResolve(String domain, InetAddress address) {

    }

    @Override
    public void onException(Exception e) {

    }

}
