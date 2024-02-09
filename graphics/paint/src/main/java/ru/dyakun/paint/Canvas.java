package ru.dyakun.paint;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Canvas {
    private static final Color BACKGROUND_COLOR = Color.WHITE;

    private BufferedImage image;
    private Color color;

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

    public void clear() {
        var g = image.getGraphics();
        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
    }

}
