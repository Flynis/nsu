package ru.dyakun.snake.model.person;

import ru.dyakun.snake.model.GameInfo;
import ru.dyakun.snake.model.entity.SnakeView;
import ru.dyakun.snake.model.event.GameEvent;
import ru.dyakun.snake.model.util.MessageType;
import ru.dyakun.snake.model.util.Messages;
import ru.dyakun.snake.model.util.Snakes;
import ru.dyakun.snake.protocol.Direction;
import ru.dyakun.snake.protocol.GameMessage;
import ru.dyakun.snake.protocol.NodeRole;

import java.net.InetSocketAddress;

public class Normal extends Viewer {
    Normal(String nickname, GameInfo gameInfo, MemberParams params, ChangeRoleListener listener) {
        super(NodeRole.NORMAL, nickname, gameInfo, params);
        addChangeRoleListener(listener);
    }

    protected Normal(Normal normal, NodeRole role) {
        super(normal, role);
    }

    @Override
    protected void onRoleChangeMsg(GameMessage message, InetSocketAddress sender) {
        super.onRoleChangeMsg(message, sender);
        var roleChangeMsg = message.getRoleChange();
        if(!roleChangeMsg.hasReceiverRole()) {
            return;
        }
        var newRole = roleChangeMsg.getReceiverRole();
        switch (newRole) {
            case VIEWER -> {
                notifyListeners(GameEvent.MESSAGE, "Поражение");
                var newMember = new Viewer(this, NodeRole.VIEWER);
                notifyChangeRoleListeners(newMember);
            }
            case DEPUTY -> {
                var newMember = new Deputy(this);
                notifyChangeRoleListeners(newMember);
            }
            default -> logger.error("Illegal change role from {} to {}", role, newRole);
        }
    }

    @Override
    public SnakeView getSnake() {
        return Snakes.findById(state.getSnakes(), id);
    }

    @Override
    public void moveSnake(Direction direction) {
        if(isConnected()) {
            var steer = Messages.steerMessage(direction, id);
            client.send(MessageType.STEER, steer, masterAddress);
        }
    }
}
