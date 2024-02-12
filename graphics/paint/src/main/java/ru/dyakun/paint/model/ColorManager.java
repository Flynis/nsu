package ru.dyakun.paint.model;

import java.awt.*;

public class ColorManager {

    private final static ColorManager instance = new ColorManager(Color.BLACK);
    private Color color;

    private ColorManager(Color initialColor) {
        this.color = initialColor;
    }

    public static ColorManager getInstance() {
        return instance;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

}
