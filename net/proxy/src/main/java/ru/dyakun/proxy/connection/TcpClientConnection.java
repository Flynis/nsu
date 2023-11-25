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
        this.state = ConnectionState.HANDSHAKE;
        socket.register(selector, OP_READ, this);
        InetSocketAddress socketAddress = (InetSocketAddress) socket.getRemoteAddress();
        address = socketAddress.getAddress();
        port = socketAddress.getPort();
    }

    @Override
    protected void handleReceivedData() throws IOException {
        try {
            switch (state) {
                case HANDSHAKE -> {
                    HandshakeMsg msg = HandshakeMsg.parseFrom(inputBuffer);
                    if (msg.getVersion() != SocksMessages.VERSION) {
                        var reply = SocksMessages.buildHandshakeReplyMsg(SocksMessages.NO_ACCEPTABLE_METHODS);
                        requestSend(reply);
                    } else {
                        var reply = SocksMessages.buildHandshakeReplyMsg(SocksMessages.NO_AUTHENTICATION_REQUIRED);
                        requestSend(reply);
                        state = ConnectionState.REQUEST;
                    }
                }
                case REQUEST -> {
                    RequestMsg msg = RequestMsg.parseFrom(inputBuffer);
                    if(msg.getVersion() != SocksMessages.VERSION) {
                        sendErrorReply(SOCKS_SERVER_FAILURE);
                        break;
                    }
                    if (msg.getCommand() != RequestCommand.CONNECT) {
                        sendErrorReply(COMMAND_NOT_SUPPORTED);
                        break;
                    }
                    if(msg.getAddressType() != AddressType.IPV4 || msg.getAddressType() != AddressType.DOMAIN_NAME) {
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
                    state = ConnectionState.WAIT_CONNECT;
                }
                case CONNECT -> redirectInput(destination);
                case WAIT_CONNECT -> logger.info("Unexpected receive when wait connect to desired address");
            }
        } catch (MessageParseException | ResolveException e) {
            throw new IOException("Message parse failed", e);
        }
    }

    public void destFinishConnect() {
        state = ConnectionState.CONNECT;
        var reply = SocksMessages.buildReplyMsg(SUCCEEDED, address, port);
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
        var reply = SocksMessages.buildReplyMsg(code, address, port);
        requestSend(reply);
        ConnectionTable.getInstance().markClosed(id);
    }

}
