package ru.dyakun.snake.game.person;

import ru.dyakun.snake.game.entity.Snake;
import ru.dyakun.snake.game.event.GameEvent;
import ru.dyakun.snake.game.util.Players;
import ru.dyakun.snake.game.util.Snakes;
import ru.dyakun.snake.protocol.GameMessage;
import ru.dyakun.snake.protocol.NodeRole;

import java.net.InetSocketAddress;
import java.util.Collection;

public class Deputy extends Normal {
    Deputy(Normal normal) {
        super(normal);
    }

    @Override
    public NodeRole getRole() {
        return NodeRole.DEPUTY;
    }

    @Override
    protected void onRoleChangeMsg(GameMessage message, InetSocketAddress sender) {
        var roleChangeMsg = message.getRoleChange();
        sendAck(id, masterId, sender, message);
        if(masterAddress.equals(sender)
                && roleChangeMsg.hasReceiverRole()
                && roleChangeMsg.getReceiverRole() == NodeRole.MASTER
                && roleChangeMsg.hasSenderRole()
                && roleChangeMsg.getSenderRole() == NodeRole.VIEWER) {
            becomeMaster();
            return;
        }
        if(roleChangeMsg.hasReceiverRole() && roleChangeMsg.getReceiverRole() == NodeRole.VIEWER) {
            logger.info("Become viewer");
            notifyListeners(GameEvent.MESSAGE, "YOU LOSE");
            var newMember = new Viewer(this);
            notifyChangeRoleListeners(newMember);
        }
    }

    @Override
    public void onInactivePlayers(Collection<Integer> inactive) {
        if(isMasterInactive(inactive)) {
            becomeMaster();
        }
    }

    protected void becomeMaster() {
        logger.info("Become master");
        timer.cancelPing();
        timer.cancelPlayersStatusTrack();
        removeMasterFromState();
        var newMember = new Master(this);
        notifyChangeRoleListeners(newMember);
    }

    protected void removeMasterFromState() {
        client.removeReceiver(masterAddress);
        var player = Players.findById(state.getPlayers(), masterId);
        if(player != null) {
            state.getPlayers().remove(player);
        }
        var snake = Snakes.findById(state.getSnakes(), masterId);
        if(snake != null) {
            snake.setState(Snake.State.ZOMBIE);
        }
    }
}
