package ru.dyakun.paint.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Icons {

    public static final int ICON_SIZE = 20;

    private Icons() {
        throw new AssertionError();
    }

    public static ImageIcon loadIcon(String resource) {
        try {
            Image image = ImageIO.read(Objects.requireNonNull(Icons.class.getResource(resource)));
            return new ImageIcon(image.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_DEFAULT));
        } catch (IOException e) {
            System.out.println("Failed to load icon " + resource);
            return createIcon(Color.WHITE);
        }
    }

    public static ImageIcon createIcon(Color main) {
        int size = ICON_SIZE;
        BufferedImage image = new BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(main);
        graphics.fillRect(0, 0, size, size);
        graphics.setXORMode(Color.DARK_GRAY);
        graphics.drawRect(0, 0, size - 1, size - 1);
        image.flush();
        return new ImageIcon(image);
    }

}
