package ru.dyakun.proxy.connection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConnectionTable {
    private static ConnectionTable instance;
    private final Map<UUID, Connection> connections = new HashMap<>();

    private ConnectionTable() {}

    public static ConnectionTable getInstance() {
        if(instance == null) {
            instance = new ConnectionTable();
        }
        return instance;
    }

    public UUID getFreeId() {
        UUID id = UUID.randomUUID();
        while (connections.containsKey(id)) {
            id = UUID.randomUUID();
        }
        return id;
    }

    public UUID addConnection(Connection connection) {
        UUID id = getFreeId();
        connections.put(id, connection);
        return id;
    }

}
