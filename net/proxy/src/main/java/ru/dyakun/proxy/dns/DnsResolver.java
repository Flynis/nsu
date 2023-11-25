package ru.dyakun.proxy.dns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;
import ru.dyakun.proxy.connection.ConnectionListener;
import ru.dyakun.proxy.connection.ConnectionParams;
import ru.dyakun.proxy.connection.UdpConnection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;

public class DnsResolver implements ConnectionListener {
    private static final Logger logger = LoggerFactory.getLogger(DnsResolver.class);
    private record Entry(String domain, ResolveExpectant expectant) { }
    private static DnsResolver instance;
    private final Map<String, InetAddress> resolved = new HashMap<>();
    private final List<Entry> expectants = new ArrayList<>();
    private final UdpConnection connection;

    private DnsResolver(ConnectionParams params) {
        InetSocketAddress resolverAddr = ResolverConfig.getCurrentConfig().server();
        this.connection = new UdpConnection(resolverAddr, this, params);
    }

    public void resolve(String domain, ResolveExpectant expectant) throws ResolveException {
        InetAddress address = resolved.get(domain);
        if(address != null) {
            expectant.onResolve(domain, address);
            return;
        }
        expectants.add(new Entry(domain, expectant));
        try {
            logger.debug("Resolve query for {}", domain);
            Record queryRecord = Record.newRecord(Name.fromString(domain), Type.A, DClass.IN);
            Message queryMessage = Message.newQuery(queryRecord);
            byte[] bytes = queryMessage.toWire();
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            connection.requestSend(buffer);
        } catch (TextParseException e) {
            throw new ResolveException("Domain name parse failed", e);
        }
    }

    public static UdpConnection init(ConnectionParams params) {
        if(instance == null) {
            instance = new DnsResolver(params);
            return instance.connection;
        } else {
            throw new IllegalStateException("Dns resolver is already initialized");
        }
    }

    public static DnsResolver getInstance() {
        return instance;
    }

    @Override
    public void onReceive(ByteBuffer buffer) {
        try {
            Message response = new Message(buffer);
            // TODO
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
