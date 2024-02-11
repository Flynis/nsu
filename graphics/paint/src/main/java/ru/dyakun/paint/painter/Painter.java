package ru.dyakun.paint.painter;

import ru.dyakun.paint.Canvas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public abstract class Painter implements MouseListener, MouseMotionListener {

    protected final Canvas canvas;
    protected final JDialog dialog;

    public Painter(Canvas canvas, JFrame frame) {
        this.canvas = canvas;
        this.dialog = new JDialog(frame, "Settings", Dialog.ModalityType.DOCUMENT_MODAL);
    }

    public void showSettingsDialog() {
        dialog.setVisible(true);
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
