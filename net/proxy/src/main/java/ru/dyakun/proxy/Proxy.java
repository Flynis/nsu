package ru.dyakun.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.proxy.connection.*;
import ru.dyakun.proxy.dns.DnsResolver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

public class Proxy {
    private static final Logger logger = LoggerFactory.getLogger(Proxy.class);
    private static final int SELECT_TIMEOUT = 10 * 1000; // ms
    private final Selector selector;
    private final Queue<ChangeOpReq> changeRequests = new ArrayDeque<>();
    private boolean running = false;

    public Proxy(int port) {
        if(port < 0 || port > 65535) {
            throw new IllegalArgumentException("Illegal port: " + port);
        }
        try {
            selector = Selector.open();
            var id = ConnectionTable.getInstance().getFreeId();
            var params = new ConnectionParams(id, selector, changeRequests::add);
            var address = new InetSocketAddress(port);
            var mainConnection = new MainProxyConnection(address, params);
            ConnectionTable.getInstance().add(id, mainConnection);
            var dnsId = ConnectionTable.getInstance().getFreeId();
            var dnsParams = new ConnectionParams(dnsId, selector, changeRequests::add);
            var dnsConnection = DnsResolver.init(dnsParams);
            ConnectionTable.getInstance().add(dnsId, dnsConnection);
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException("Proxy initialize failed");
        }
    }

    public void listen() {
        logger.info("Start listening");
        running = true;
        while (running) {
            try {
                changeKeyOps();
                selector.select(SELECT_TIMEOUT);
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while(keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if(!key.isValid()) continue;
                    if(key.isAcceptable()) accept(key);
                    else if(key.isReadable()) read(key);
                    else if(key.isWritable()) write(key);
                    else if(key.isConnectable()) connect(key);
                }
                var table =  ConnectionTable.getInstance();
                table.removeClosed();
                table.removeInactive();
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

    private void changeKeyOps() {
        while (!changeRequests.isEmpty()) {
            var request = changeRequests.poll();
            SelectionKey key = request.key();
            if (key.isValid()) {
                logger.debug("Switch key op {} to {}", ((Connection)key.attachment()).getId(), request.ops());
                key.interestOps(request.ops());
            }
        }
    }

    private void accept(SelectionKey key) {
        AcceptableConnection connection = (AcceptableConnection) key.attachment();
        try {
            connection.accept();
        } catch (IOException e) {
            if(!(connection instanceof MainProxyConnection)) {
                disconnect(connection);
            }
            logger.warn("Failed to accept a connection", e);
        }
    }

    private void read(SelectionKey key) {
        ReadWriteConnection connection = (ReadWriteConnection) key.attachment();
        try {
            connection.receive();
        } catch (IOException e) {
            disconnect(connection);
        }
    }

    private void write(SelectionKey key) {
        ReadWriteConnection connection = (ReadWriteConnection) key.attachment();
        try {
            connection.send();
        } catch (IOException e) {
            disconnect(connection);
        }
    }

    private void connect(SelectionKey key) {
        ConnectableConnection connection = (ConnectableConnection) key.attachment();
        try {
            connection.connect();
        } catch (IOException e) {
            disconnect(connection);
        }
    }

    public void stop() {
        if(!running) return;
        running = false;
        try {
            ConnectionTable.getInstance().clear();
            selector.close();
        } catch (IOException e) {
            logger.warn("Selector close failed", e);
        }
    }

    public void disconnect(Connection connection) {
        ConnectionTable.getInstance().remove(connection);
        connection.close();
        logger.info("Connection with {} successfully closed", connection.getAddress());
    }

}
