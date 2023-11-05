package ru.dyakun.snake.game.person;

import ru.dyakun.snake.game.event.GameEvent;
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
        client.removeReceiver(masterAddress);
        var newMember = new Master(this);
        notifyChangeRoleListeners(newMember);
    }
}
