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

    public UUID add(Connection connection) {
        UUID id = getFreeId();
        connections.put(id, connection);
        return id;
    }

    public void add(Connection connection, UUID id) {
        if(connections.containsKey(id)) {
            throw new IllegalArgumentException("Id must be unique: " + id);
        }
        connections.put(id, connection);
    }

    public void remove(Connection connection) {
        connections.remove(connection.getId());
    }

    public void clear() {
        for(var connection: connections.values()) {
            connection.close();
        }
        connections.clear();
    }

}
