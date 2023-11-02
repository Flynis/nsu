package ru.dyakun.snake.game.timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.game.person.ChangeRoleListener;
import ru.dyakun.snake.game.person.Member;
import ru.dyakun.snake.game.person.Viewer;
import ru.dyakun.snake.game.tracker.AbstractStatusTracker;

import java.util.TimerTask;

public class PlayersStatusTrackTask extends TimerTask implements ChangeRoleListener {
    private static final Logger logger = LoggerFactory.getLogger(PlayersStatusTrackTask.class);
    private final AbstractStatusTracker tracker;
    private Member member;

    public PlayersStatusTrackTask(AbstractStatusTracker tracker, Member member) {
        this.tracker = tracker;
        this.member = member;
        if(member instanceof Viewer viewer) {
            viewer.addChangeRoleListener(this);
        }
    }

    @Override
    public void run() {
        try {
            var inactive = tracker.deleteInactive();
            member.onInactivePlayers(inactive);
        } catch (Exception e) {
            logger.error("Player track failed", e);
        }
    }

    @Override
    public void onChange(Member newRole) {
        member = newRole;
    }
}
