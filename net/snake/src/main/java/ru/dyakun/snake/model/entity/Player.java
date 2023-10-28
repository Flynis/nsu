package ru.dyakun.snake.model.entity;

import ru.dyakun.snake.protocol.GamePlayer;
import ru.dyakun.snake.protocol.NodeRole;

import java.net.InetSocketAddress;
import java.util.Collection;

public class Player implements PlayerView {
    private final String name;
    private final int id;
    private InetSocketAddress address;
    private NodeRole role;
    private int score;

    @Override
    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }

    public NodeRole getRole() {
        return role;
    }

    @Override
    public int getScore() {
        return score;
    }

    public void setRole(NodeRole role) {
        this.role = role;
    }

    public void addScore(int s) {
        if(s <= 0) {
            throw new IllegalArgumentException("Addition must be positive");
        }
        this.score += s;
    }

    private Player(String name, int id, InetSocketAddress address, NodeRole role, int score) {
        this.name = name;
        this.id = id;
        this.address = address;
        this.role = role;
        this.score = score;
    }

    public static Player fromGamePlayer(GamePlayer gamePlayer) {
        Builder builder = new Builder(gamePlayer.getName(), gamePlayer.getId());
        builder.role(gamePlayer.getRole()).score(gamePlayer.getScore());
        if(gamePlayer.hasIpAddress() && gamePlayer.hasPort()) {
            return builder.address(gamePlayer.getIpAddress(), gamePlayer.getPort()).build();
        }
        return builder.build();
    }

    public static Player findMaster(Collection<Player> players) {
        return find(players, NodeRole.MASTER);
    }

    public static Player findDeputy(Collection<Player> players) {
        return find(players, NodeRole.DEPUTY);
    }

    public static Player find(Collection<Player> players, NodeRole role) {
        for(var player : players) {
            if(player.getRole() == role) {
                return player;
            }
        }
        return null;
    }

    public static class Builder {
        private final String name;
        private final int id;
        private InetSocketAddress address;
        private NodeRole role;
        private int score;

        public Builder(String name, int id) {
            if(name.isBlank()) {
                throw new IllegalArgumentException("Player name is empty");
            }
            if(id < 0) {
                throw new IllegalArgumentException("Player id must >= 0");
            }
            this.name = name;
            this.id = id;
            this.role = NodeRole.NORMAL;
            this.score = 0;
        }

        public Builder address(String ip, int port) {
            if(ip.isBlank() || port < 1024) {
                throw new IllegalArgumentException("Incorrect address");
            }
            this.address = new InetSocketAddress(ip, port);
            return this;
        }

        public Builder address(InetSocketAddress address) {
            this.address = address;
            return this;
        }

        public Builder role(NodeRole role) {
            this.role = role;
            return this;
        }

        public Builder score(int score) {
            if(score < 0) {
                throw new IllegalArgumentException("Player score must >= 0");
            }
            this.score = score;
            return this;
        }

        public Player build() {
            return new Player(name, id, address, role, score);
        }
    }
}
