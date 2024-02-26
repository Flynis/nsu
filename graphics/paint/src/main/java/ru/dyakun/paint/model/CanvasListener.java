package ru.dyakun.paint.model;

import java.awt.image.BufferedImage;

public interface CanvasListener {

    void onUpdate(BufferedImage image);

    void onSourceChange(BufferedImage image);

}
