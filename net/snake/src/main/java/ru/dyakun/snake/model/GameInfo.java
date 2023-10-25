package ru.dyakun.snake.model;

import ru.dyakun.snake.model.entity.Player;
import ru.dyakun.snake.protocol.GameAnnouncement;
import ru.dyakun.snake.protocol.GamePlayers;
import ru.dyakun.snake.protocol.NodeRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameInfo implements GameInfoView {
    private List<Player> players;
    private final GameConfig config;
    private final String name;
    private boolean mayJoin;

    public GameInfo(List<Player> players, GameConfig config, String name, boolean mayJoin) {
        this.players = Objects.requireNonNull(players);
        this.config = Objects.requireNonNull(config);
        if(name.isBlank()) {
            throw new IllegalArgumentException("Name is empty");
        }
        this.name = name;
        this.mayJoin = mayJoin;
    }

    public static GameInfo fromAnnouncement(GameAnnouncement announcement) {
        Builder builder = new Builder(GameConfig.from(announcement.getConfig()), announcement.getGameName());
        GamePlayers gamePlayers = announcement.getPlayers();
        for(int i = 0; i < gamePlayers.getPlayersCount(); i++) {
            builder.addPlayer(Player.fromGamePlayer(gamePlayers.getPlayers(i)));
        }
        if(announcement.hasCanJoin()) {
            builder.mayJoin(announcement.getCanJoin());
        }
        return builder.build();
    }

    public Player findMaster() {
        for(var player : players) {
            if(player.getRole() == NodeRole.MASTER) {
                return player;
            }
        }
        throw new IllegalStateException("No master in game");
    }

    public void updateFrom(GameInfo gameInfo) {
        if (this.name.equals(gameInfo.name)) {
            this.players = gameInfo.players;
            this.mayJoin = gameInfo.mayJoin;
        }
    }

    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public int getPlayersCount() {
        return players.size();
    }

    @Override
    public GameConfig getConfig() {
        return config;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isMayJoin() {
        return mayJoin;
    }

    public static class Builder {
        private final List<Player> players = new ArrayList<>();
        private final GameConfig config;
        private final String name;
        private boolean mayJoin = true;

        public Builder(GameConfig config, String name) {
            this.config = config;
            this.name = name;
        }

        public Builder mayJoin(boolean mayJoin) {
            this.mayJoin = mayJoin;
            return this;
        }

        public Builder addPlayer(Player player) {
            players.add(player);
            return this;
        }

        public GameInfo build() {
            return new GameInfo(players, config, name, mayJoin);
        }
    }
}
