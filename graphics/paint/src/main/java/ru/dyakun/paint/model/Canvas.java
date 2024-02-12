package ru.dyakun.paint.model;

import ru.dyakun.paint.util.GraphicsUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Canvas {

    public static final Color BACKGROUND_COLOR = Color.WHITE;

    private BufferedImage image = null;
    private final List<CanvasListener> listeners = new ArrayList<>();

    void setImage(BufferedImage image) {
        this.image = image;
        image.createGraphics();
        for(var listener: listeners) {
            listener.onSourceChange(this.image);
        }
    }

    BufferedImage getImage() {
        return image;
    }

    public void addUpdateListener(CanvasListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for(var listener: listeners) {
            listener.onUpdate(image);
        }
    }

    public void clear() {
        var g = image.getGraphics();
        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        notifyListeners();
    }

    public void fillCircle(int x, int y, int r, Color c) {
        var g = image.getGraphics();
        g.setColor(c);
        g.fillOval(x - r, y - r , r * 2, r * 2);
        notifyListeners();
    }

    public void drawLine(int x0, int y0, int x1, int y1, int thickness, Color c) {
        if(thickness == 1) {
            GraphicsUtil.drawBresenhamLine(x0, y0, x1, y1, image, c);
        } else {
            Graphics2D g = (Graphics2D) image.getGraphics();
            g.setStroke(new BasicStroke(thickness));
            g.setColor(c);
            g.drawLine(x0, y0, x1, y1);
        }
        notifyListeners();
    }

    public void fill(int x0, int y0, Color c) {
        GraphicsUtil.spanFilling(x0, y0, image, c);
    }

    public void drawRegularStar(int cx, int cy, int n, int r, int theta, Color c) {
        double offset = theta * Math.PI / 180;
        double a = Math.PI / n;
        int x0 = (int) (cx + r * Math.cos(offset));
        int y0 = (int) (cy - r * Math.sin(offset));
        int prevX = x0;
        int prevY = y0;
        for (int i = 0; i < n; i++) {
            double rcos = Math.cos(a * i + offset) * r;
            double rsin = Math.sin(a * i + offset) * r;

            int x = (int) (cx + rcos);
            int y = (int) (cy + rsin);
            drawLine(prevX, prevY, x, y, 1, c);

            int x1 = (int) (cx + rcos / 2);
            int y1 = (int) (cy + rsin / 2);
            drawLine(x, y, x1, y1, 1, c);
            prevX = x1;
            prevY = y1;
        }
        drawLine(prevX, prevY, x0, y0, 1, c);
    }

    public void drawRegularPolygon(int cx, int cy, int n, int r, int theta, Color c) {
        double offset = theta * Math.PI / 180;
        double a = 2 * Math.PI / n;
        int x0 = (int) (cx + r * Math.cos(offset));
        int y0 = (int) (cy - r * Math.sin(offset));
        int prevX = x0;
        int prevY = y0;
        for (int i = 1; i < n; i++) {
            int x = (int) (cx + r * Math.cos(a * i + offset));
            int y = (int) (cy - r * Math.sin(a * i + offset));
            drawLine(prevX, prevY, x, y, 1, c);
            prevX = x;
            prevY = y;
        }
        drawLine(prevX, prevY, x0, y0, 1, c);
    }

}
