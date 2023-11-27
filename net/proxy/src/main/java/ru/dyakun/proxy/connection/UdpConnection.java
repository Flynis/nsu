package ru.dyakun.proxy.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.proxy.ChangeOpReq;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

public class UdpConnection extends AbstractReadWriteConnection {
    private static final Logger logger = LoggerFactory.getLogger(UdpConnection.class);
    private final DatagramChannel socket;
    private final InetSocketAddress receiver;
    private final ConnectionListener listener;

    public UdpConnection(InetSocketAddress receiver, ConnectionListener listener,
                         ConnectionParams params) {
        super(params);
        this.receiver = receiver;
        this.listener = listener;
        try {
            socket = DatagramChannel.open();
            socket.bind(null);
            socket.configureBlocking(false);
            socket.connect(receiver);
            socket.register(selector, SelectionKey.OP_READ, this);
        } catch (IOException e) {
            throw new IllegalStateException("Udp connection initialize failed", e);
        }
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
            logger.warn("Udp channel close failed", e);
        }
    }

    @Override
    public String getAddress() {
        try {
            InetSocketAddress socketAddress = (InetSocketAddress) socket.getLocalAddress();
            InetAddress address = socketAddress.getAddress();
            return String.format("%s:%s", address.getHostAddress(), socketAddress.getPort());
        } catch (IOException e) {
            logger.warn("Get remote address failed");
        }
        return "";
    }

    @Override
    public void receive() throws IOException {
        int read = socket.read(inputBuffer);
        if(read < 0) {
            throw new IOException("Cannot read data from channel");
        }
        inputBuffer.flip();
        listener.onReceive(inputBuffer);
        inputBuffer.clear();
    }

    @Override
    protected SelectionKey getSelectionKey() {
        return socket.keyFor(selector);
    }

    @Override
    public void send() throws IOException {
        while (!dataQueue.isEmpty()) {
            ByteBuffer data = dataQueue.poll();
            int length = data.limit();
            socket.send(data, socket.getRemoteAddress());
            logger.debug("Send datagram {}b to {}:{} remaining: {}",
                    length,
                    receiver.getAddress().getHostAddress(),
                    receiver.getPort(),
                    data.remaining());
        }
        changeRequests.accept(new ChangeOpReq(getSelectionKey(), SelectionKey.OP_READ));
    }

}
