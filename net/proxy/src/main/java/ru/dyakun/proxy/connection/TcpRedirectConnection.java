package ru.dyakun.proxy.connection;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import static java.nio.channels.SelectionKey.OP_WRITE;

public class TcpRedirectConnection extends AbstractTcpConnection implements ConnectableConnection {
    private final TcpClientConnection destination;

    public TcpRedirectConnection(TcpClientConnection destination, ConnectionParams params) throws IOException {
        super(SocketChannel.open(), params);
        this.destination = destination;
        socket.configureBlocking(false);
    }

    @Override
    public void receive() throws IOException {

    }


    @Override
    public void send() throws IOException {

    }

    @Override
    public void requestConnect(InetSocketAddress address) throws IOException {
        socket.register(selector, SelectionKey.OP_CONNECT, this);
        socket.connect(address);
        selector.wakeup();
    }

    @Override
    public void connect() throws IOException {
        if(socket.finishConnect()) {
            SelectionKey key = getSelectionKey();
            key.interestOps(OP_WRITE);
            destination.destFinishConnect();
        }
    }

}
