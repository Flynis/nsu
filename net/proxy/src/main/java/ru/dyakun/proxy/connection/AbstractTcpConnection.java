package ru.dyakun.proxy.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.proxy.ChangeOpReq;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import static java.nio.channels.SelectionKey.OP_READ;

public abstract class AbstractTcpConnection extends AbstractReadWriteConnection {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractTcpConnection.class);
    protected final SocketChannel socket;

    public AbstractTcpConnection(SocketChannel socket, ConnectionParams params) {
        super(params);
        this.socket = socket;
    }

    @Override
    protected SelectionKey getSelectionKey() {
        return socket.keyFor(selector);
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
        int read = socket.read(inputBuffer);
        if(read < 0) {
            throw new IOException("Cannot read data from channel");
        }
        inputBuffer.flip();
        handleReceivedData();
        inputBuffer.clear();
    }

    protected abstract void handleReceivedData() throws IOException;

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

    protected void redirectInput(ReadWriteConnection dest) throws IOException {
        if(dest.isClosed()) {
            throw new IOException("Destination closed");
        }
        logger.debug("Receive {}b from {}", inputBuffer.limit(), getAddress());
        var data = ByteBuffers.copyFrom(inputBuffer);
        dest.requestSend(data);
    }

}
