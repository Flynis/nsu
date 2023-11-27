package ru.dyakun.proxy.connection;

import ru.dyakun.proxy.dns.DnsResolver;
import ru.dyakun.proxy.dns.ResolveException;
import ru.dyakun.proxy.dns.ResolveExpectant;
import ru.dyakun.proxy.message.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.UUID;

import static java.nio.channels.SelectionKey.*;
import static ru.dyakun.proxy.message.ReplyCode.*;

public class TcpClientConnection extends AbstractTcpConnection implements ResolveExpectant {
    private TcpRedirectConnection destination;
    private ConnectionState state;
    private final InetAddress address;
    private final int port;
    private int dstPort;

    public TcpClientConnection(SocketChannel socket, ConnectionParams params) throws IOException {
        super(socket, params);
        socket.register(selector, OP_READ, this);
        InetSocketAddress socketAddress = (InetSocketAddress) socket.getRemoteAddress();
        address = socketAddress.getAddress();
        port = socketAddress.getPort();
        changeState(ConnectionState.HANDSHAKE);
    }

    private void changeState(ConnectionState state) {
        this.state = state;
        logger.debug("Change connection state to {}", state);
    }

    @Override
    public void close() {
        super.close();
        if(destination != null && !destination.isClosed()) {
            destination.close();
        }
    }

    @Override
    protected void handleReceivedData() throws IOException {
        try {
            switch (state) {
                case HANDSHAKE -> {
                    HandshakeMsg msg = HandshakeMsg.parseFrom(inputBuffer);
                    logger.debug("Receive {}", msg);
                    if (msg.getVersion() != SocksMessages.VERSION) {
                        var reply = SocksMessages.buildHandshakeReplyMsg(SocksMessages.NO_ACCEPTABLE_METHODS);
                        requestSend(reply);
                    } else {
                        var reply = SocksMessages.buildHandshakeReplyMsg(SocksMessages.NO_AUTHENTICATION_REQUIRED);
                        requestSend(reply);
                        changeState(ConnectionState.REQUEST);
                    }
                }
                case REQUEST -> {
                    RequestMsg msg = RequestMsg.parseFrom(inputBuffer);
                    logger.debug("Receive {}", msg);
                    if(msg.getVersion() != SocksMessages.VERSION) {
                        sendErrorReply(SOCKS_SERVER_FAILURE);
                        break;
                    }
                    if (msg.getCommand() != RequestCommand.CONNECT) {
                        sendErrorReply(COMMAND_NOT_SUPPORTED);
                        break;
                    }
                    if(msg.getAddressType() != AddressType.IPV4 && msg.getAddressType() != AddressType.DOMAIN_NAME) {
                        sendErrorReply(ADDRESS_TYPE_NOT_SUPPORTED);
                        break;
                    }
                    dstPort = msg.getDstPort();
                    switch (msg.getAddressType()) {
                        case IPV4 -> {
                            InetAddress address = msg.getDstAddress();
                            connectToDesiredAddress(address);
                        }
                        case DOMAIN_NAME -> {
                            String domain = msg.getDstDomainName();
                            DnsResolver.getInstance().resolve(domain, this);
                        }
                    }
                    changeState(ConnectionState.WAIT_CONNECT);
                }
                case CONNECT -> redirectInput(destination);
                case WAIT_CONNECT -> logger.info("Unexpected receive when wait connect to desired address");
            }
        } catch (MessageParseException | ResolveException e) {
            throw new IOException("Message parse failed", e);
        }
    }

    public void destFinishConnect() {
        logger.info("Connection established with {}", destination.getAddress());
        changeState(ConnectionState.CONNECT);
        var reply = SocksMessages.buildReplyMsg(SUCCEEDED, address, port);
        logger.debug("Send: {}", SUCCEEDED);
        requestSend(reply);
    }

    public void destFailedConnect() {
        sendErrorReply(HOST_UNREACHABLE);
    }

    private void connectToDesiredAddress(InetAddress dstAddress) throws IOException {
        UUID id = ConnectionTable.getInstance().getFreeId();
        var params = new ConnectionParams(id, selector, changeRequests);
        var connection = new TcpRedirectConnection(this, params);
        destination = connection;
        ConnectionTable.getInstance().add(id, connection);
        connection.requestConnect(new InetSocketAddress(dstAddress, dstPort));
    }

    @Override
    public void onResolve(InetAddress address) {
        try {
            connectToDesiredAddress(address);
        } catch (IOException e) {
            ConnectionTable.getInstance().markClosed(id);
        }
    }

    @Override
    public void onException(Exception e) {
        logger.info("Domain name resolve failed", e);
        sendErrorReply(HOST_UNREACHABLE);
    }

    private void sendErrorReply(ReplyCode code) {
        logger.debug("Send: {}", code);
        var reply = SocksMessages.buildReplyMsg(code, address, port);
        requestSend(reply);
        ConnectionTable.getInstance().markClosed(id);
    }

}
