package ru.dyakun.paint;

import javax.swing.*;
import java.util.Objects;

public class IconUtil {

    private IconUtil() {
        throw new AssertionError();
    }

    public static ImageIcon loadIcon(String resource) {
        return new ImageIcon(Objects.requireNonNull(IconUtil.class.getResource(resource)));
    }

}
