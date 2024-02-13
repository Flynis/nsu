package ru.dyakun.paint.model;

import java.awt.*;

@FunctionalInterface
public interface ColorChangeListener {

    void colorChanged(Color newColor);

}
