package ru.dyakun.snake.game.person;

import ru.dyakun.snake.game.entity.GameInfo;
import ru.dyakun.snake.game.entity.SnakeView;
import ru.dyakun.snake.game.event.GameEvent;
import ru.dyakun.snake.game.util.MessageType;
import ru.dyakun.snake.game.util.Messages;
import ru.dyakun.snake.game.util.Snakes;
import ru.dyakun.snake.protocol.Direction;
import ru.dyakun.snake.protocol.GameMessage;
import ru.dyakun.snake.protocol.NodeRole;

import java.net.InetSocketAddress;

public class Normal extends Viewer {
    Normal(String nickname, GameInfo gameInfo, MemberParams params, ChangeRoleListener listener) {
        super(nickname, gameInfo, params);
        addChangeRoleListener(listener);
    }

    protected Normal(Normal normal) {
        super(normal);
    }

    @Override
    public NodeRole getRole() {
        return NodeRole.NORMAL;
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
                var newMember = new Viewer(this);
                notifyChangeRoleListeners(newMember);
            }
            case DEPUTY -> {
                var newMember = new Deputy(this);
                notifyChangeRoleListeners(newMember);
            }
            default -> logger.error("Illegal change role from {} to {}", getRole(), newRole);
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
