package ru.dyakun.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;

import static java.nio.channels.SelectionKey.*;

public class Proxy {
    private static final Logger logger = LoggerFactory.getLogger(Proxy.class);
    private final Selector selector;
    private final ServerSocketChannel socket;

    public Proxy(int port) {
        if(port < 0 || port > 65535) {
            throw new IllegalArgumentException("Illegal port: " + port);
        }
        try {
            selector = Selector.open();
            socket = ServerSocketChannel.open();
            socket.bind(new InetSocketAddress(port));
            socket.configureBlocking(false);
            socket.register(selector, OP_ACCEPT);
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException("Proxy initialize failed");
        }
    }

    public void listen() {
        logger.info("Start listening");
        while (true) {
            try {
                ChangeKeyOpsRequest request;
                while ((request = changeRequests.poll()) != null) {
                    SelectionKey key = request.key();
                    if (key.isValid()) {
                        logger.debug("Switch key op {} to {}", ((Connection)key.attachment()).getId(), request.ops());
                        key.interestOps(request.ops());
                    }
                }
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while(keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if(!key.isValid()) continue;
                    if(key.isAcceptable()) accept(key);
                    else if(key.isReadable()) read(key);
                    else if(key.isWritable()) write(key);
                }
            } catch (CancelledKeyException e) {
                logger.debug("Canceled key", e);
            } catch (ClosedSelectorException e) {
                logger.debug("Selector closed");
                break;
            } catch (IOException e) {
                logger.error("Other server error", e);
                stop();
            }
        }
    }

    private void accept(SelectionKey key) {
        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            SocketChannel channel = serverSocketChannel.accept();
            channel.configureBlocking(false);
            long id = getID();
            Connection connection = new Connection(id, selector, channel, changeRequests::add);
            channel.register(selector, SelectionKey.OP_READ, connection);
            connections.putIfAbsent(id, connection);
            logger.info("Connection established from client {}: {}", id, channel.getRemoteAddress().toString());
        } catch (IOException e) {
            logger.warn("Failed to accept a connection", e);
        }
    }

    private long getID() {
        nextID++;
        return nextID;
    }

    private void read(SelectionKey key) {
        Connection connection = (Connection) key.attachment();
        try {
            connection.receive();
            while (connection.hasCompletedMessage()) {
                receivedDataListener.accept(new ReceivedData(connection.getId(), connection.nextCompletedMessage()));
            }
        } catch (IOException e) {
            connectionEventListener.accept(new ConnectionEvent(connection.getId(), ConnectionEventTag.DISCONNECT));
        }
    }

    private void write(SelectionKey key) {
        Connection connection = (Connection) key.attachment();
        try {
            connection.send();
        } catch (IOException e) {
            connectionEventListener.accept(new ConnectionEvent(connection.getId(), ConnectionEventTag.DISCONNECT));
        }
    }

    @Override
    public void send(long id, String data) {
        Connection connection = connections.get(id);
        connection.addMessageToSend(data);
    }

    @Override
    public void disconnect(long id) {
        Connection connection = connections.remove(id);
        if(connection != null) {
            try {
                connection.close();
                logger.info("Connection with {} was successfully closed", id);
            } catch (IOException e) {
                logger.error("Failed to close connection with {}", id, e);
            }
        }
    }

}
