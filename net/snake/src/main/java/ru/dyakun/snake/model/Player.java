package ru.dyakun.snake.model;

import ru.dyakun.snake.protocol.GamePlayer;
import ru.dyakun.snake.protocol.NodeRole;

public class Player {
    private final String name;
    private final int id;
    private final String ip;
    private final int port;
    private NodeRole role;
    private int score;

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public NodeRole getRole() {
        return role;
    }

    public int getScore() {
        return score;
    }

    public void setRole(NodeRole role) {
        this.role = role;
    }

    public void setScore(int score) {
        if(score < this.score) {
            throw new IllegalArgumentException("New score is less then current");
        }
        this.score = score;
    }

    private Player(String name, int id, String ip, int port, NodeRole role, int score) {
        this.name = name;
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.role = role;
        this.score = score;
    }

    public static Player fromGamePlayer(GamePlayer gamePlayer) {
        Builder builder = new Builder(gamePlayer.getName(), gamePlayer.getId());
        builder.role(gamePlayer.getRole()).score(gamePlayer.getScore());
        if(gamePlayer.hasIpAddress()) {
            builder.address(gamePlayer.getIpAddress(), gamePlayer.getPort());
        }
        return builder.build();
    }

    public static class Builder {
        private final String name;
        private final int id;
        private String ip;
        private int port;
        private NodeRole role;
        private int score;

        public Builder(String name, int id) {
            this.name = name;
            this.id = id;
            this.ip = null;
            this.port = -1;
            this.role = NodeRole.NORMAL;
            this.score = 0;
        }

        public Builder address(String ip, int port) {
            if(ip.isBlank() || port < 1024) {
                throw new IllegalArgumentException("Incorrect address");
            }
            this.ip = ip;
            this.port = port;
            return this;
        }

        public Builder role(NodeRole role) {
            this.role = role;
            return this;
        }

        public Builder score(int score) {
            this.score = score;
            return this;
        }

        public Player build() {
            return new Player(name, id, ip, port, role, score);
        }
    }
}
