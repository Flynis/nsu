package ru.dyakun.snake.game.util;

import ru.dyakun.snake.game.entity.Player;
import ru.dyakun.snake.protocol.NodeRole;

import java.net.InetSocketAddress;
import java.util.Collection;

public class Players {
    private Players() {
        throw new AssertionError();
    }

    public static Player findMaster(Collection<Player> players) {
        return findByRole(players, NodeRole.MASTER);
    }

    public static Player findDeputy(Collection<Player> players) {
        return findByRole(players, NodeRole.DEPUTY);
    }

    public static Player findByRole(Collection<Player> players, NodeRole role) {
        for(var player : players) {
            if(player.getRole() == role) {
                return player;
            }
        }
        return null;
    }

    public static Player findPlayerByAddress(Collection<Player> players, InetSocketAddress address) {
        for(var player : players) {
            if(address.equals(player.getAddress())) {
                return player;
            }
        }
        return null;
    }

    public static Player findById(Collection<Player> players, int id) {
        for(var player : players) {
            if(player.getId() == id) {
                return player;
            }
        }
        return null;
    }

    public static int maxId(Collection<Player> players, int initial) {
        int max = initial;
        for(var player: players) {
            if(player.getId() > max) {
                max = player.getId();
            }
        }
        return max;
    }

    public static boolean contains(Collection<Player> players, String nickname) {
        for(var player: players) {
            if (player.getName().equals(nickname)) {
                return true;
            }
        }
        return false;
    }
}
