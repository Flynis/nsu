package ru.dyakun.paint;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class IconUtil {

    public static final int ICON_SIZE = 20;

    private IconUtil() {
        throw new AssertionError();
    }

    public static ImageIcon loadIcon(String resource) {
        try {
            Image image = ImageIO.read(Objects.requireNonNull(IconUtil.class.getResource(resource)));
            return new ImageIcon(image.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_DEFAULT));
        } catch (IOException e) {
            throw new AssertionError();
        }
    }

}
