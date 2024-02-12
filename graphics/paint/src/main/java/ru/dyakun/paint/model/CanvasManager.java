package ru.dyakun.paint.model;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CanvasManager {

    private final Canvas canvas;

    public CanvasManager(int width, int height) {
        canvas = new Canvas();
        createEmptyPicture(width, height);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void createEmptyPicture(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, Image.SCALE_DEFAULT);
        canvas.setImage(image);
        canvas.clear();
    }

    public void loadPicture(File file) throws IOException {
        BufferedImage image = ImageIO.read(file);
        canvas.setImage(image);
    }

    public void exportToPng(File file) throws IOException {
        BufferedImage image = canvas.getImage();
        ImageIO.write(image, "png", file);
    }

}
