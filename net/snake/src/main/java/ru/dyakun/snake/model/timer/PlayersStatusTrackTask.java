package ru.dyakun.snake.model.timer;

import ru.dyakun.snake.model.GameState;
import ru.dyakun.snake.model.PlayersStatusTracker;
import ru.dyakun.snake.model.util.MessageType;
import ru.dyakun.snake.model.util.MessageUtils;
import ru.dyakun.snake.net.NetClient;
import ru.dyakun.snake.protocol.NodeRole;

import java.util.Collection;
import java.util.TimerTask;

public class PlayersStatusTrackTask extends TimerTask {
    private final PlayersStatusTracker tracker;
    private final GameState state;
    private final NetClient client;

    public PlayersStatusTrackTask(PlayersStatusTracker tracker, GameState state, NetClient client) {
        this.tracker = tracker;
        this.state = state;
        this.client = client;
    }

    @Override
    public void run() {
        var deletedPlayers = tracker.deleteInactivePlayers();
        switch (state.getCurrentPlayer().getRole()) {
            case MASTER -> {
                var deputy = state.findDeputy(deletedPlayers);
                if(deputy == null) {
                    findNewDeputy();
                }
            }
            case DEPUTY -> {
                if(!isMasterDeleted(deletedPlayers)) {
                    break;
                }
                state.becomeMaster();
                var members = state.getMembers();
                for(var member: members) {
                    var roleChange = MessageUtils.roleChangeMessage(
                            NodeRole.MASTER,
                            NodeRole.NORMAL,
                            state.getCurrentPlayer().getId(),
                            member.getId());
                    client.send(MessageType.ROLE_CHANGE, roleChange, member.getAddress());
                }
                findNewDeputy();
            }
            case NORMAL, VIEWER -> {
                if(isMasterDeleted(deletedPlayers)) {
                    state.changeMasterToDeputy();
                }
            }
        }
        state.deleteInactivePlayers(deletedPlayers);
    }

    private void findNewDeputy() {
        var newDeputy = state.setNewDeputy();
        if(newDeputy == null) {
            return;
        }
        var roleChange = MessageUtils.roleChangeMessage(
                NodeRole.MASTER,
                NodeRole.DEPUTY,
                state.getCurrentPlayer().getId(),
                newDeputy.getId());
        client.send(MessageType.ROLE_CHANGE, roleChange, newDeputy.getAddress());
    }

    private boolean isMasterDeleted(Collection<Integer> deleted) {
        for(var id : deleted) {
            if(id == state.getMaster().getId()) {
                return true;
            }
        }
        return false;
    }
}
