package ru.dyakun.paint.gui;

import ru.dyakun.paint.model.ColorChangeListener;
import ru.dyakun.paint.model.ColorManager;

import javax.swing.*;
import java.awt.*;

public class ColorObserverPane extends JPanel implements ColorChangeListener {

    public ColorObserverPane() {
        Dimension size = new Dimension(Icons.ICON_SIZE, Icons.ICON_SIZE);
        setMaximumSize(size);
        setPreferredSize(size);
        ColorManager.getInstance().addColorChangedListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(ColorManager.getInstance().getColor());
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public void colorChanged(Color newColor) {
        var g = getGraphics();
        g.setColor(newColor);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

}