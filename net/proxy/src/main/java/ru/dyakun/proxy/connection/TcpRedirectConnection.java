package ru.dyakun.proxy.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import static java.nio.channels.SelectionKey.*;

public class TcpRedirectConnection extends AbstractTcpConnection implements ConnectableConnection {
    private final TcpClientConnection destination;

    public TcpRedirectConnection(TcpClientConnection destination, ConnectionParams params) throws IOException {
        super(SocketChannel.open(), params);
        this.destination = destination;
        socket.configureBlocking(false);
    }

    public void requestConnect(InetSocketAddress address) throws IOException {
        socket.register(selector, OP_CONNECT, this);
        socket.connect(address);
        selector.wakeup();
    }

    @Override
    public void close() {
        super.close();
        if(destination != null && !destination.isClosed()) {
            destination.close();
        }
    }

    @Override
    public void connect() throws IOException {
        try {
            if(socket.finishConnect()) {
                SelectionKey key = getSelectionKey();
                key.interestOps(OP_WRITE);
                destination.destFinishConnect();
            }
        } catch (IOException e) {
            destination.destFailedConnect();
            throw e;
        }
    }

    @Override
    protected void handleReceivedData() throws IOException {
        redirectInput(destination);
    }

}
