package ru.dyakun.snake.model.timer;

import ru.dyakun.snake.model.person.ChangeRoleListener;
import ru.dyakun.snake.model.person.Member;
import ru.dyakun.snake.model.person.Viewer;
import ru.dyakun.snake.model.tracker.AbstractStatusTracker;

import java.util.TimerTask;

public class PlayersStatusTrackTask extends TimerTask implements ChangeRoleListener {
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
        var inactive = tracker.deleteInactive();
        member.onInactivePlayers(inactive);
    }

    @Override
    public void onChange(Member newRole) {
        member = newRole;
    }
}
