package ru.dyakun.snake.game.util;

import ru.dyakun.snake.game.entity.GameInfo;
import ru.dyakun.snake.game.entity.Player;
import ru.dyakun.snake.game.entity.Point;
import ru.dyakun.snake.game.entity.Snake;
import ru.dyakun.snake.protocol.*;
import ru.dyakun.snake.protocol.GameConfig;
import ru.dyakun.snake.util.IdGenerator;
import ru.dyakun.snake.util.SimpleIdGenerator;

import java.util.Collection;

public class Messages {
    private Messages() {
        throw new AssertionError();
    }
    private static final IdGenerator generator = new SimpleIdGenerator();

    public static GameMessage announcementMessage(GameInfo gameInfo) {
        var announce = GameAnnouncement.newBuilder()
                .setPlayers(gamePlayersFrom(gameInfo.getPlayers()))
                .setConfig(gameConfigFrom(gameInfo.getConfig()))
                .setGameName(gameInfo.getName())
                .setCanJoin(gameInfo.isMayJoin())
                .build();
        var msg = GameMessage.AnnouncementMsg.newBuilder()
                .addGames(announce)
                .build();
        return GameMessage.newBuilder()
                .setAnnouncement(msg)
                .setMsgSeq(generator.next())
                .build();
    }

    public static GameMessage stateMessage(ru.dyakun.snake.game.entity.GameState gameState) {
        var stateMsg = GameMessage.StateMsg.newBuilder()
                .setState(gameStateFrom(gameState))
                .build();
        return GameMessage.newBuilder()
                .setState(stateMsg)
                .setMsgSeq(generator.next())
                .build();
    }

    public static GameMessage pingMessage() {
        return GameMessage.newBuilder()
                .setPing(GameMessage.PingMsg.newBuilder().build())
                .setMsgSeq(generator.next())
                .build();
    }

    public static GameMessage joinMessage(String nickname, String game, NodeRole role) {
        var joinMsg = GameMessage.JoinMsg.newBuilder()
                .setPlayerName(nickname)
                .setGameName(game)
                .setRequestedRole(role)
                .build();
        return GameMessage.newBuilder()
                .setJoin(joinMsg)
                .setMsgSeq(generator.next())
                .build();
    }

    public static GameMessage steerMessage(Direction direction, int sender) {
        var steerMsg = GameMessage.SteerMsg.newBuilder()
                .setDirection(direction)
                .build();
        return GameMessage.newBuilder()
                .setSteer(steerMsg)
                .setMsgSeq(generator.next())
                .setSenderId(sender)
                .build();
    }

    public static GameMessage errorMessage(String message) {
        var errorMsg = GameMessage.ErrorMsg.newBuilder()
                .setErrorMessage(message)
                .build();
        return GameMessage.newBuilder()
                .setError(errorMsg)
                .setMsgSeq(generator.next())
                .build();
    }

    public static GameMessage ackMessage(int sender, int receiver, long msgSeq) {
        return GameMessage.newBuilder()
                .setAck(GameMessage.AckMsg.newBuilder().build())
                .setMsgSeq(msgSeq)
                .setSenderId(sender)
                .setReceiverId(receiver)
                .build();
    }

    public static GameMessage discoverMsg() {
        return GameMessage.newBuilder()
                .setDiscover(GameMessage.DiscoverMsg.newBuilder().build())
                .setMsgSeq(generator.next())
                .build();
    }

    public static GameMessage roleChangeMessage(NodeRole sender, NodeRole receiver, int senderId, int receiverId) {
        var builder = GameMessage.RoleChangeMsg.newBuilder();
        if(sender != null) {
            builder.setSenderRole(sender);
        }
        if(receiver != null) {
            builder.setReceiverRole(receiver);
        }
        var roleChangeMsg = builder.build();
        return GameMessage.newBuilder()
                .setRoleChange(roleChangeMsg)
                .setMsgSeq(generator.next())
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .build();
    }

    public static GameState gameStateFrom(ru.dyakun.snake.game.entity.GameState gameState) {
        return GameState.newBuilder()
                .setStateOrder(gameState.getStateOrder())
                .addAllSnakes(gameState.getSnakes().stream().map(Messages::snakeFrom).toList())
                .addAllFoods(gameState.getFoods().stream().map(Messages::coordinatesFrom).toList())
                .setPlayers(gamePlayersFrom(gameState.getPlayers()))
                .build();
    }

    public static GamePlayer gamePlayerFrom(Player player) {
        var builder = GamePlayer.newBuilder()
                .setName(player.getName())
                .setId(player.getId())
                .setRole(player.getRole())
                .setScore(player.getScore());
        if(player.getAddress() != null) {
            builder.setIpAddress(player.getAddress().getAddress().getHostAddress())
                    .setPort(player.getAddress().getPort());
        }
        return builder.build();
    }

    public static GameConfig gameConfigFrom(ru.dyakun.snake.game.GameConfig config) {
        return GameConfig.newBuilder()
                .setWidth(config.getWidth())
                .setHeight(config.getHeight())
                .setFoodStatic(config.getFoodStatic())
                .setStateDelayMs(config.getDelay())
                .build();
    }

    public static GamePlayers gamePlayersFrom(Collection<Player> players) {
        return GamePlayers.newBuilder()
                .addAllPlayers(players.stream().map(Messages::gamePlayerFrom).toList())
                .build();
    }

    public static GameState.Snake snakeFrom(Snake snake) {
        return GameState.Snake.newBuilder()
                .addAllPoints(snake.points().stream().map(Messages::coordinatesFrom).toList())
                .setPlayerId(snake.playerId())
                .setHeadDirection(snake.direction())
                .setState(GameState.Snake.SnakeState.forNumber(snake.state().getCode()))
                .build();
    }

    public static GameState.Coord coordinatesFrom(Point point) {
        return GameState.Coord.newBuilder()
                .setX(point.x)
                .setY(point.y)
                .build();
    }
}
