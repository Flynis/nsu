package ru.dyakun.paint;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static ru.dyakun.paint.IconUtil.ICON_SIZE;
import static ru.dyakun.paint.IconUtil.loadIcon;

public class ColorPane extends JPanel {

    private Color current = Color.BLACK;
    private final List<ColorChangedListener> listeners = new ArrayList<>();

    public ColorPane() {
        Dimension size = new Dimension(ICON_SIZE, ICON_SIZE);
        setMinimumSize(size);
        setMaximumSize(size);
    }

    private record ColorAction(String name, Color color) {}
    public List<Action> createColorActions(JFrame frame) {
        ColorAction[] commonColors = {
            new ColorAction("Black", Color.BLACK),
            new ColorAction("Red", Color.RED),
            new ColorAction("Yellow", Color.YELLOW),
            new ColorAction("Green", Color.GREEN),
            new ColorAction("Cyan", ColorUIResource.CYAN),
            new ColorAction("Blue", Color.BLUE),
            new ColorAction("Pink", new Color(255, 0, 255)),
            new ColorAction("White", Color.WHITE),
        };

        List<Action> res = new ArrayList<>();
        for(var colorAction: commonColors) {
            Action action = new AbstractAction(colorAction.name, createIcon(colorAction.color)) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setColor(colorAction.color);
                }
            };
            res.add(action);
        }
        Action colorChooseButton = new AbstractAction("Choose color", loadIcon("/icons/color_picker.png")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color color = JColorChooser.showDialog(frame, "Choose color", current);
                if(color != null) {
                    setColor(color);
                }
            }
        };
        res.add(colorChooseButton);
        return res;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(current);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private void setColor(Color newColor) {
        current = newColor;
        repaint();
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
        int size = ICON_SIZE;
        BufferedImage image = new BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(main);
        graphics.fillRect(0, 0, size, size);
        graphics.setXORMode(Color.DARK_GRAY);
        graphics.drawRect(0, 0, size - 1, size - 1);
        image.flush();
        return new ImageIcon(image);
    }

}