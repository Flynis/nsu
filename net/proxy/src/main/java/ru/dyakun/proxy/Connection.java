package ru.dyakun.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

public class Connection {
    private static final Logger logger = LoggerFactory.getLogger(Connection.class);
    private final Selector selector;
    private final SocketChannel channel;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(4096);
    private final Queue<ByteBuffer> messagesToWrite = new ArrayDeque<>();
    private final Consumer<ChangeKeyOpsRequest> changeKeyOpsRequests;

    public Connection(Selector selector, SocketChannel channel, Consumer<ChangeKeyOpsRequest> changeKeyOpsRequests) {
        this.selector = selector;
        this.channel = channel;
        this.changeKeyOpsRequests = changeKeyOpsRequests;
    }

    public long getId() {
        return id;
    }

    public String nextCompletedMessage() {
        return completedMessages.poll();
    }

    public boolean hasCompletedMessage() {
        return !completedMessages.isEmpty();
    }

    public boolean connect() throws IOException {
        if(channel.finishConnect()) {
            SelectionKey key = getSelectionKey();
            key.interestOps(OP_WRITE);
            return true;
        }
        return false;
    }

    public void receive() throws IOException {
        int read = channel.read(readBuffer);
        if(read < 0) {
            throw new IOException("Cannot read data from channel");
        }
        readBuffer.flip();
        extractMessages();
        readBuffer.compact();
    }

    private void extractMessages() {
        while (true) {
            if(readBuffer.limit() - readBuffer.position() < Constants.HEADER_LENGTH) return;
            int length = readBuffer.getInt();
            if(!bufferHasCompletedMessage(length)) {
                readBuffer.position(readBuffer.position() - Constants.HEADER_LENGTH);
                return;
            }
            String message = new String(readBuffer.array(), Constants.HEADER_LENGTH, length);
            logger.debug("Receive: {}", message);
            readBuffer.position(readBuffer.position() + length);
            completedMessages.add(message);
        }
    }

    private boolean bufferHasCompletedMessage(int size) {
        return readBuffer.position() + size <= readBuffer.limit();
    }

    public void addMessageToSend(@NotNull String message) {
        ByteBuffer data = ByteBuffer.allocate(message.length() + Constants.HEADER_LENGTH);
        logger.debug("Data to send {} bytes: {}", message.length() + Constants.HEADER_LENGTH, message);
        data.putInt(message.length());
        data.put(message.getBytes(StandardCharsets.UTF_8));
        data.flip();
        synchronized (messagesToWrite) {
            messagesToWrite.add(data);
        }
        SelectionKey key = getSelectionKey();
        if(key != null && key.isValid() && key.interestOps() == OP_READ) {
            changeKeyOpsRequests.accept(new ChangeKeyOpsRequest(key, OP_WRITE));
            selector.wakeup();
        }
    }

    public void send() throws IOException {
        ByteBuffer message;
        synchronized (messagesToWrite) {
            while ((message = messagesToWrite.peek()) != null) {
                channel.write(message);
                if(message.hasRemaining()) {
                    return;
                }
                logger.debug("Send: {} bytes", message.limit());
                messagesToWrite.remove();
            }
            changeKeyOpsRequests.accept(new ChangeKeyOpsRequest(getSelectionKey(), OP_READ));
        }
    }

    public synchronized void close() throws IOException {
        channel.close();
    }

    private SelectionKey getSelectionKey() {
        return channel.keyFor(selector);
    }
}
