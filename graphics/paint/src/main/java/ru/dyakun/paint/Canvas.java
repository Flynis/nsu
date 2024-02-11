package ru.dyakun.paint;

import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Canvas {
    public static final Color BACKGROUND_COLOR = Color.WHITE;

    private BufferedImage image = new BufferedImage(1000, 600, Image.SCALE_DEFAULT);
    private Color color = Color.BLACK;
    private final List<ChangeListener> listeners = new ArrayList<>();

    public Canvas() {
        Graphics2D g = image.createGraphics();
        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, image.getWidth() - 1, image.getHeight() - 1);
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public void repaint() {
        for(var listener: listeners) {
            listener.stateChanged(null);
        }
    }

}
