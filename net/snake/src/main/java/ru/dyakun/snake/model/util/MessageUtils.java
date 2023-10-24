package ru.dyakun.snake.model.util;

import ru.dyakun.snake.model.GameInfo;
import ru.dyakun.snake.model.entity.Player;
import ru.dyakun.snake.protocol.*;
import ru.dyakun.snake.protocol.GameConfig;

import java.util.List;

public class MessageUtils {
    private MessageUtils() {
        throw new AssertionError();
    }

    public static GameMessage.AnnouncementMsg announcementFrom(GameInfo gameInfo) {
        var announce = GameAnnouncement.newBuilder()
                .setPlayers(gamePlayersFrom(gameInfo.getPlayers()))
                .setConfig(gameConfigFrom(gameInfo.getConfig()))
                .setGameName(gameInfo.getName())
                .setCanJoin(gameInfo.isMayJoin())
                .build();
        return GameMessage.AnnouncementMsg.newBuilder()
                .addGames(announce)
                .build();
    }

    public static GamePlayer gamePlayerFrom(Player player) {
        return GamePlayer.newBuilder()
                .setName(player.getName())
                .setId(player.getId())
                .setIpAddress(player.getIp())
                .setPort(player.getPort())
                .setRole(player.getRole())
                .setScore(player.getScore())
                .build();
    }

    public static GameConfig gameConfigFrom(ru.dyakun.snake.model.GameConfig config) {
        return GameConfig.newBuilder()
                .setWidth(config.getWidth())
                .setHeight(config.getHeight())
                .setFoodStatic(config.getFoodStatic())
                .setStateDelayMs(config.getDelay())
                .build();
    }

    public static GamePlayers gamePlayersFrom(List<Player> players) {
        return GamePlayers.newBuilder()
                .addAllPlayers(players.stream().map(MessageUtils::gamePlayerFrom).toList())
                .build();
    }
}
