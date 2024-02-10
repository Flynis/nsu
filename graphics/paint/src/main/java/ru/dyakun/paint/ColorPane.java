package ru.dyakun.paint;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ColorPane extends JPanel {

    private static final int ICON_SIZE = 16;
    private Color current = Color.BLACK;
    private final List<ColorChangedListener> listeners = new ArrayList<>();
    private final JButton currentColorButton = new JButton();

    public ColorPane() {
        currentColorButton.setIcon(createIcon(current));
        add(currentColorButton);

        Color[] commonColors = {
                Color.BLACK, Color.RED,
                Color.YELLOW, Color.GREEN,
                Color.PINK, Color.BLUE,
                Color.CYAN, Color.WHITE};

        for(Color color: commonColors) {
            JButton button = new JButton();
            button.setIcon(createIcon(color));
            button.addActionListener(e -> setColor(color));
            add(button);
        }

        JButton colorChooseButton = new JButton();
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(ColorPane.class.getResource("/icons/color_picker.png")));
        colorChooseButton.setIcon(icon);
        colorChooseButton.addActionListener(e -> {
            Color color = JColorChooser.showDialog(getParent(), "Choose color", current);
            if(color != null) {
                setColor(color);
            }
        });
        add(colorChooseButton);
    }

    private void setColor(Color newColor) {
        current = newColor;
        currentColorButton.setIcon(createIcon(current));
        notifyListeners(newColor);
    }

    private void notifyListeners(Color newColor) {
        for(var listener: listeners) {
            listener.colorChanged(newColor);
        }
    }

    public void addColorChangedListener(ColorChangedListener listener) {
        listeners.add(listener);
    }

    public static ImageIcon createIcon(Color main) {
        int width = ICON_SIZE;
        int height = ICON_SIZE;
        BufferedImage image = new BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(main);
        graphics.fillRect(0, 0, width, height);
        graphics.setXORMode(Color.DARK_GRAY);
        graphics.drawRect(0, 0, width-1, height-1);
        image.flush();
        return new ImageIcon(image);
    }

}
