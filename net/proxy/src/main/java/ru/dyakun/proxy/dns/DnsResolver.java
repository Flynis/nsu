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
    private record ResolveMessage(String domain, Message message, ByteBuffer bytes) {}
    private static DnsResolver instance;
    private final Map<String, InetAddress> resolved = new HashMap<>();
    private final List<Entry> expectants = new ArrayList<>();
    private final List<ResolveMessage> messages = new ArrayList<>();
    private final UdpConnection connection;

    private DnsResolver(ConnectionParams params) {
        InetSocketAddress resolverAddr = ResolverConfig.getCurrentConfig().server();
        logger.info("DNS {}:{}", resolverAddr.getAddress().getHostAddress(), resolverAddr.getPort());
        this.connection = new UdpConnection(resolverAddr, this, params);
    }

    public void resolve(String domain, ResolveExpectant expectant) throws ResolveException {
        InetAddress address = resolved.get(domain);
        if(address != null) {
            expectant.onResolve(address);
            return;
        }
        expectants.add(new Entry(domain, expectant));
        try {
            logger.info("Resolve query for {}", domain);
            Record queryRecord = Record.newRecord(Name.fromString(domain + '.'), Type.A, DClass.IN);
            Message queryMessage = Message.newQuery(queryRecord);
            byte[] bytes = queryMessage.toWire(1024);
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            connection.requestSend(buffer);
            messages.add(new ResolveMessage(domain, queryMessage, buffer));
        } catch (TextParseException e) {
            throw new ResolveException("Domain name parse failed", e);
        }
    }

    public void tryResolve() {
        for(var msg: messages) {
            connection.requestSend(msg.bytes);
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

    private Message findQuery(int id) {
        for(var msg: messages) {
            if(msg.message.getHeader().getID() == id) {
                return msg.message;
            }
        }
        return null;
    }

    private boolean validateRecord(Message response, Message query) {
        if (response.getQuestion() == null) {
            logger.warn("DNS response question null");
            return false;
        }
        if (!query.getQuestion().getName().equals(response.getQuestion().getName())) {
            logger.warn("DNS response question name not match");
            return false;
        }
        if (query.getQuestion().getDClass() != response.getQuestion().getDClass()) {
            logger.warn("DNS response question dclass not match");
            return false;
        }
        if (query.getQuestion().getType() != response.getQuestion().getType()) {
            logger.warn("DNS response question type not match");
            return false;
        }
        return true;
    }

    @Override
    public void onReceive(ByteBuffer buffer) {
        try {
            Message response = new Message(buffer);
            if(response.getRcode() == Rcode.NOERROR) {
                var query = findQuery(response.getHeader().getID());
                if(query == null) return;
                if(!validateRecord(response, query)) return;
                var records = response.getSection(Section.ANSWER);
                logger.info("DNS response answers count: {}", records.size());
                for(var r: records) {
                    if(r instanceof ARecord aRecord) {
                        logger.debug(r.toString());
                        String str = aRecord.getName().toString();
                        String domain = str.substring(0, str.length() - 1);
                        InetAddress address = aRecord.getAddress();
                        resolved.put(domain, address);
                        messages.removeIf(msg -> msg.domain.equals(domain));
                        expectants.removeIf(e -> {
                            if(e.domain.equals(domain)) {
                                e.expectant.onResolve(address);
                                return true;
                            }
                            return false;
                        });
                        break;
                    }
                }
            } else {
                logger.info("DNS response: {}", Rcode.string(response.getRcode()));
                var question = response.getQuestion();
                if (question == null) return;
                expectants.removeIf(e -> {
                    if(e.domain.equals(question.getName().toString())) {
                        e.expectant.onException(new ResolveException("Resolve failed"));
                        return true;
                    }
                    return false;
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
