package ru.dyakun.proxy.connection;

import ru.dyakun.proxy.ChangeOpReq;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Consumer;

import static java.nio.channels.SelectionKey.*;

public abstract class AbstractReadWriteConnection implements ReadWriteConnection {
    protected final UUID id;
    protected final Consumer<ChangeOpReq> changeRequests;
    protected final ByteBuffer inputBuffer = ByteBuffer.allocate(16384);
    protected final Queue<ByteBuffer> dataQueue = new ArrayDeque<>();
    protected final Selector selector;

    public AbstractReadWriteConnection(ConnectionParams params) {
        this.id = params.id();
        this.changeRequests = params.changeRequests();
        this.selector = params.selector();
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void requestSend(ByteBuffer data) {
        data.flip();
        dataQueue.add(data);
        SelectionKey key = getSelectionKey();
        if(key != null && key.isValid() && key.interestOps() == OP_READ) {
            changeRequests.accept(new ChangeOpReq(key, OP_WRITE));
            selector.wakeup();
        }
    }

    protected abstract SelectionKey getSelectionKey();

}
