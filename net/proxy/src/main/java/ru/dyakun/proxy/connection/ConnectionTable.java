package ru.dyakun.proxy.connection;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConnectionTable {
    private static final int MAX_INACTIVE_TIME = 10; // min
    private static final int CLOSE_DELAY = 10; // sec
    private static ConnectionTable instance;
    private final Map<UUID, Entry> connections = new HashMap<>();

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

    public void add(UUID id, Connection connection, boolean closable) {
        if(connections.containsKey(id)) {
            throw new IllegalArgumentException("Id must be unique: " + id);
        }
        var entry = new Entry(connection, closable);
        connections.put(id, entry);
    }

    public void add(UUID id, Connection connection) {
        add(id, connection, true);
    }

    public void remove(Connection connection) {
        connections.remove(connection.getId());
    }

    public void clear() {
        for(var entry: connections.values()) {
            entry.getConnection().close();
        }
        connections.clear();
    }

    public void markClosed(UUID id) {
        var entry = connections.get(id);
        entry.setClosed();
        entry.updateTime();
    }

    public void updateTime(UUID id) {
        var entry = connections.get(id);
        if(!entry.isClosed()) {
            entry.updateTime();
        }
    }

    public void removeInactive() {
        connections.entrySet().removeIf(e -> {
            var entry = e.getValue();
            if (!entry.isClosable()) {
                return false;
            }
            if(entry.getConnection().isClosed()) {
                return true;
            }
            if(!entry.isClosed() &&
                    ChronoUnit.MINUTES.between(entry.getTime(), LocalDateTime.now()) > MAX_INACTIVE_TIME) {
                entry.getConnection().close();
                return true;
            }
            return false;
        });
    }

    public void removeClosed() {
        connections.entrySet().removeIf(e -> {
            var entry = e.getValue();
            if(entry.isClosed() &&
                    ChronoUnit.SECONDS.between(entry.getTime(), LocalDateTime.now()) > CLOSE_DELAY) {
                entry.getConnection().close();
                return true;
            }
            return false;
        });
    }

    private static class Entry {
        private final Connection connection;
        private final boolean closable;
        private boolean closed;
        private LocalDateTime time;

        private Entry(Connection connection, boolean closable) {
            this.connection = connection;
            this.closable = closable;
            closed = false;
            time = LocalDateTime.now();
        }

        public Connection getConnection() {
            return connection;
        }

        public boolean isClosable() {
            return closable;
        }

        public boolean isClosed() {
            return closed;
        }

        public LocalDateTime getTime() {
            return time;
        }

        public void updateTime() {
            time = LocalDateTime.now();
        }

        public void setClosed() {
            if (closable) {
                closed = true;
            } else {
                throw new IllegalStateException("Attempt to close not closeable connection");
            }
        }

    }

}
