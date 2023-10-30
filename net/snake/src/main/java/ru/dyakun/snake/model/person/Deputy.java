package ru.dyakun.snake.model.person;

import ru.dyakun.snake.model.event.GameEvent;
import ru.dyakun.snake.protocol.GameMessage;
import ru.dyakun.snake.protocol.NodeRole;

import java.net.InetSocketAddress;
import java.util.Collection;

public class Deputy extends Normal {
    Deputy(Normal normal) {
        super(normal, NodeRole.DEPUTY);
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
            notifyListeners(GameEvent.MESSAGE, "Поражение");
            var newMember = new Viewer(this, NodeRole.VIEWER);
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
        timer.cancelPing();
        timer.cancelPlayersStatusTrack();
        var newMember = new Master(this);
        notifyChangeRoleListeners(newMember);
    }
}
