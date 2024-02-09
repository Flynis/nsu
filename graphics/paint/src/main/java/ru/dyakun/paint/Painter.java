package ru.dyakun.paint;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public abstract class Painter implements MouseListener {
    protected final Canvas canvas;

    public Painter(Canvas canvas) {
        this.canvas = canvas;
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

}
