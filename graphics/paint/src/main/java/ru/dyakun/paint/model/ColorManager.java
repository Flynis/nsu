package ru.dyakun.paint.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ColorManager {

    private final static ColorManager instance = new ColorManager();
    private Color color;
    private final List<ColorChangeListener> listeners = new ArrayList<>();

    private ColorManager() {
        this.color = Color.BLACK;
    }

    public static ColorManager getInstance() {
        return instance;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        for(var listener: listeners) {
            listener.colorChanged(color);
        }
    }

    public void addColorChangedListener(ColorChangeListener listener) {
        listeners.add(listener);
    }

}
