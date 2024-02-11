package ru.dyakun.paint.painter;

import ru.dyakun.paint.Canvas;
import ru.dyakun.paint.SettingsDialog;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public abstract class Manipulator implements MouseListener, MouseMotionListener {

    protected final Canvas canvas;
    protected final SettingsDialog dialog;

    public Manipulator(Canvas canvas, JFrame frame) {
        this.canvas = canvas;
        this.dialog = new SettingsDialog(frame);
    }

    public void reset() {}

    public void showSettingsDialog() {
        dialog.show();
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {}

    @Override
    public void mousePressed(MouseEvent mouseEvent) {}

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {}

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {}

    @Override
    public void mouseExited(MouseEvent mouseEvent) {}

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {}

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {}

}
