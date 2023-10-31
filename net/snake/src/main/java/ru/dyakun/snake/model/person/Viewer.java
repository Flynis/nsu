package ru.dyakun.snake.model.person;

import ru.dyakun.snake.gui.base.SceneNames;
import ru.dyakun.snake.model.GameInfo;
import ru.dyakun.snake.model.GameState;
import ru.dyakun.snake.model.GameStateView;
import ru.dyakun.snake.model.entity.PlayerView;
import ru.dyakun.snake.model.entity.SnakeView;
import ru.dyakun.snake.model.event.GameEvent;
import ru.dyakun.snake.model.tracker.MasterStatusTracker;
import ru.dyakun.snake.model.util.MessageType;
import ru.dyakun.snake.model.util.Messages;
import ru.dyakun.snake.model.util.Players;
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

    Viewer(String nickname, GameInfo gameInfo, MemberParams params) {
        this(NodeRole.VIEWER, nickname, gameInfo, params);
    }

    Viewer(Viewer viewer, NodeRole role) {
        super(viewer.getParams(), role, viewer.gameInfo);
        this.tracker = viewer.tracker;
        this.state = viewer.state;
        this.id = viewer.id;
        this.masterAddress = viewer.masterAddress;
        this.masterId = viewer.masterId;
        this.changeRoleListeners = viewer.changeRoleListeners;
    }

    protected Viewer(NodeRole role, String nickname, GameInfo gameInfo, MemberParams params) {
        super(params, role, gameInfo);
        this.changeRoleListeners = new ArrayList<>();
        var master = gameInfo.findMaster();
        masterAddress = master.getAddress();
        masterId = master.getId();
        var message = Messages.joinMessage(nickname, gameInfo.getName(), role);
        client.send(MessageType.JOIN, message, masterAddress);
    }

    protected boolean isConnected() {
        return state != null;
    }

    protected boolean notMaster(InetSocketAddress sender) {
        if(!sender.equals(masterAddress)) {
            logger.error("Unknown sender {}", sender.getHostName());
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
    protected void onStateMsg(GameMessage message, InetSocketAddress sender) {
        if(notMaster(sender)) return;
        sendAck(id, masterId, sender, message);
        tracker.updateStatus(masterId);
        var stateMsg = message.getState();
        if(state == null) {
            state = GameState.from(stateMsg.getState(), gameInfo.getConfig());
            notifyListeners(GameEvent.REPAINT_FIELD, null);
            manager.changeScene(SceneNames.GAME);
        } else {
            state.updateBy(stateMsg.getState());
            notifyListeners(GameEvent.REPAINT_FIELD, null);
        }
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
        logger.info("Unexpected ping from {}", sender.getHostName());
    }

    @Override
    protected void onSteerMsg(GameMessage message, InetSocketAddress sender) {
        logger.info("Unexpected steer from {}", sender.getHostName());
    }

    @Override
    protected void onAckMsg(GameMessage message, InetSocketAddress sender) {
        if(notMaster(sender)) return;
        if(!isConnected()) {
            id = message.getReceiverId();
            tracker = new MasterStatusTracker(gameInfo.getConfig().getDelay() * 4 / 5, masterId);
            timer.startPlayersStatusTrack(tracker, this);
            int period = gameInfo.getConfig().getDelay() / 2;
            timer.startPing(masterAddress, period, client);
        } else {
            tracker.updateStatus(masterId);
        }
    }

    @Override
    protected void onJoinMsg(GameMessage message, InetSocketAddress sender) {
        logger.info("Unexpected join from {}", sender.getHostName());
    }

    @Override
    protected void onErrorMsg(GameMessage message, InetSocketAddress sender) {
        if(notMaster(sender)) return;
        sendAck(id, masterId, sender, message);
        tracker.updateStatus(masterId);
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
            changeMaster(deputy.getId(), deputy.getAddress());
        }
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
    public void back() {
        state = null;
        masterAddress = null;
        tracker = null;
        timer.cancelPing();
        timer.cancelPlayersStatusTrack();
        manager.changeScene(SceneNames.MENU);
    }
}