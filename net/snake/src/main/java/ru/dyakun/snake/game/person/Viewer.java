package ru.dyakun.snake.game.person;

import ru.dyakun.snake.game.entity.GameInfo;
import ru.dyakun.snake.game.entity.GameState;
import ru.dyakun.snake.game.entity.GameStateView;
import ru.dyakun.snake.game.entity.PlayerView;
import ru.dyakun.snake.game.entity.SnakeView;
import ru.dyakun.snake.game.event.GameEvent;
import ru.dyakun.snake.game.tracker.MasterStatusTracker;
import ru.dyakun.snake.game.util.MessageType;
import ru.dyakun.snake.game.util.Messages;
import ru.dyakun.snake.game.util.Players;
import ru.dyakun.snake.protocol.Direction;
import ru.dyakun.snake.protocol.GameMessage;
import ru.dyakun.snake.protocol.NodeRole;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;

public class Viewer extends Member {
    protected MasterStatusTracker tracker;
    protected GameState state;
    protected int id;
    protected int masterId;
    protected InetSocketAddress masterAddress;
    private final Collection<ChangeRoleListener> changeRoleListeners;

    Viewer(Viewer viewer) {
        super(viewer.getParams(), viewer.gameInfo);
        this.tracker = viewer.tracker;
        this.state = viewer.state;
        this.id = viewer.id;
        this.masterAddress = viewer.masterAddress;
        this.masterId = viewer.masterId;
        this.changeRoleListeners = viewer.changeRoleListeners;
    }

    protected Viewer(String nickname, GameInfo gameInfo, MemberParams params) {
        super(params, gameInfo);
        this.changeRoleListeners = new ArrayList<>();
        var master = gameInfo.findMaster();
        masterAddress = master.getAddress();
        masterId = master.getId();
        var message = Messages.joinMessage(nickname, gameInfo.getName(), getRole());
        client.send(MessageType.JOIN, message, masterAddress);
    }

    protected boolean isConnected() {
        return state != null;
    }

    protected boolean notMaster(InetSocketAddress sender) {
        if(!sender.equals(masterAddress)) {
            logger.error("Unknown sender {}", sender.getAddress().getHostAddress());
            return true;
        }
        return false;
    }

    public void addChangeRoleListener(ChangeRoleListener listener) {
        changeRoleListeners.add(listener);
    }

    protected void notifyChangeRoleListeners(Member newRole) {
        for(var listener: changeRoleListeners) {
            listener.onChange(newRole);
        }
    }

    @Override
    public NodeRole getRole() {
        return NodeRole.VIEWER;
    }

    @Override
    protected void onStateMsg(GameMessage message, InetSocketAddress sender) {
        if(notMaster(sender)) return;
        sendAck(id, masterId, sender, message);
        tracker.updateStatus(masterId);
        var stateMsg = message.getState();
        if(state == null) {
            state = GameState.from(stateMsg.getState(), gameInfo.getConfig());
            notifyListeners(GameEvent.JOINED, null);
        } else {
            state.updateBy(stateMsg.getState());
        }
        notifyListeners(GameEvent.REPAINT, null);
    }

    @Override
    protected void onRoleChangeMsg(GameMessage message, InetSocketAddress sender) {
        sendAck(id, message.getSenderId(), sender, message);
        var changeRoleMsg = message.getRoleChange();
        if(changeRoleMsg.hasSenderRole() && changeRoleMsg.getSenderRole() == NodeRole.MASTER) {
            changeMaster(message.getSenderId(), sender);
        }
    }

    @Override
    protected void onPingMsg(GameMessage message, InetSocketAddress sender) {
        logger.info("Unexpected ping from {}", sender.getAddress().getHostAddress());
    }

    @Override
    protected void onSteerMsg(GameMessage message, InetSocketAddress sender) {
        logger.info("Unexpected steer from {}", sender.getAddress().getHostAddress());
    }

    @Override
    protected void onAckMsg(GameMessage message, InetSocketAddress sender) {
        if(notMaster(sender)) return;
        if(!isConnected()) {
            id = message.getReceiverId();
            tracker = new MasterStatusTracker((gameInfo.getConfig().getDelay() * 4) / 5, masterId);
            timer.startPlayersStatusTrack(tracker, this);
            int period = gameInfo.getConfig().getDelay() / 2;
            timer.startPing(masterAddress, period, client);
        } else {
            tracker.updateStatus(masterId);
        }
    }

    @Override
    protected void onJoinMsg(GameMessage message, InetSocketAddress sender) {
        logger.info("Unexpected join from {}", sender.getAddress().getHostAddress());
    }

    @Override
    protected void onErrorMsg(GameMessage message, InetSocketAddress sender) {
        if(notMaster(sender)) return;
        sendAck(id, masterId, sender, message);
        var errorMsg = message.getError();
        notifyListeners(GameEvent.MESSAGE, errorMsg.getErrorMessage());
    }

    @Override
    protected void onDiscoverMsg(GameMessage message, InetSocketAddress sender) { }

    @Override
    public PlayerView getPlayer() {
        return Players.findById(state.getPlayers(), id);
    }

    @Override
    public SnakeView getSnake() {
        return null;
    }

    @Override
    public GameStateView getGameState() {
        return state;
    }

    @Override
    public void moveSnake(Direction direction) {}

    @Override
    public void onInactivePlayers(Collection<Integer> inactive) {
        if(isMasterInactive(inactive)) {
            var deputy = Players.findDeputy(state.getPlayers());
            if(deputy == null) {
                gameOver();
            } else {
                changeMaster(deputy.getId(), deputy.getAddress());
            }
        }
    }

    protected void gameOver() {
        client.removeReceiver(masterAddress);
        timer.cancelPing();
        timer.cancelPlayersStatusTrack();
        notifyListeners(GameEvent.MESSAGE, "GAME OVER");
    }

    protected void changeMaster(int masterId, InetSocketAddress masterAddress) {
        client.changeReceiver(masterAddress, this.masterAddress);
        this.masterId = masterId;
        this.masterAddress = masterAddress;
        timer.changeMasterAddress(masterAddress);
        tracker.changeMasterId(masterId);
    }

    protected boolean isMasterInactive(Collection<Integer> inactive) {
        return inactive.contains(masterId);
    }

    @Override
    public void exit() {
        state = null;
        masterAddress = null;
        tracker = null;
        timer.cancelPing();
        timer.cancelPlayersStatusTrack();
    }

    @Override
    public void onSendError(GameMessage message) {
        if(message.hasJoin()) {
            notifyListeners(GameEvent.MESSAGE, "Game host is not responding");
        }
    }
}
