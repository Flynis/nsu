package ru.dyakun.paint.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
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
            throw new AssertionError();
        }
    }

}
