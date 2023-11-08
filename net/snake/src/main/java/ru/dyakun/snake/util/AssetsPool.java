package ru.dyakun.snake.util;

import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

public class AssetsPool {
    private static final Map<String, Image> images = new HashMap<>();

    public static Image getImage(String path) {
        if(images.containsKey(path)) {
            return images.get(path);
        } else {
            try {
                Image image = new Image(path);
                images.put(path, image);
                return image;
            }
            catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Incorrect name", e);
            }
        }
    }
}
