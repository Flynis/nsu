package ru.dyakun.paint.painter;

import ru.dyakun.paint.Canvas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class FillingManipulator extends Manipulator {

    public FillingManipulator(Canvas canvas, JFrame frame) {
        super(canvas, frame);
        dialog.initByProperty(new ArrayList<>());
    }

    private record Span(int left, int right, int y) {

        public boolean isEmpty() {
            return left == right;
        }

    }

    private boolean equalsColors(int x, int y, BufferedImage image, Color color) {
        int c = image.getRGB(x, y);
        int red =   (c & 0x00ff0000) >> 16;
        int green = (c & 0x0000ff00) >> 8;
        int blue =   c & 0x000000ff;
        return red == color.getRed() && green == color.getGreen() && blue == color.getBlue();
    }

    private Span findSpan(int x0, int y0, BufferedImage image, Color newColor) {
        int x = x0;
        while (x >= 0) {
            if(equalsColors(x, y0, image, newColor)) {
                break;
            }
            x--;
        }
        int left = x + 1;
        x = x0 + 1;
        while (x < image.getWidth()) {
            if(equalsColors(x, y0, image, newColor)) {
                break;
            }
            x++;
        }
        int right = x - 1;
        return new Span(left, right, y0);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int x0 = e.getX();
        int y0 = e.getY();
        BufferedImage image = canvas.getImage();
        Color newColor = canvas.getColor();
        var g = image.getGraphics();
        g.setColor(newColor);
        Queue<Span> stack = new ArrayDeque<>();
        var firstSpan = findSpan(x0, y0, image, newColor);
        if (firstSpan.isEmpty()) {
            return;
        }
        stack.add(firstSpan);
        while (!stack.isEmpty()) {
            var span = stack.poll();
            // recolor span
            for (int i = span.left; i <= span.right; i++) {
                g.drawLine(i, span.y, i, span.y);
            }
            // search upper spans
            int y = span.y - 1;
            if (y >= 0) {
                for (int i = span.left; i < span.right; i++) {
                    var upper = findSpan(i, y, image, newColor);
                    if (upper.isEmpty()) {
                        continue;
                    }
                    i = upper.right + 1;
                    stack.add(upper);
                }
            }
            // search lower spans
            y = span.y + 1;
            if(y < image.getHeight()) {
                for (int i = span.left; i < span.right; i++) {
                    var lower = findSpan(i, y, image, newColor);
                    if (lower.isEmpty()) {
                        continue;
                    }
                    i = lower.right + 1;
                    stack.add(lower);
                }
            }
        }
        canvas.repaint();
    }

}
