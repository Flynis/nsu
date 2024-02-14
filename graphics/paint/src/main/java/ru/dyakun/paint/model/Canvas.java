package ru.dyakun.paint.model;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Canvas {

    public static final Color BACKGROUND_COLOR = Color.WHITE;

    private BufferedImage image = null;
    private final List<CanvasListener> listeners = new ArrayList<>();

    Canvas() {}

    void setImage(BufferedImage image) {
        this.image = image;
        image.createGraphics();
        for(var listener: listeners) {
            listener.onSourceChange(this.image);
        }
    }

    public BufferedImage getImage() {
        return image;
    }

    public void addCanvasListener(CanvasListener listener) {
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
        notifyListeners();
    }

    public void drawRegularStar(int cx, int cy, int n, int r, int theta, Color c) {
        double offset = theta * Math.PI / 180;
        double a = Math.PI / n;
        double vx = - r * Math.sin(offset);
        double vy = r * Math.cos(offset);
        double asin = Math.sin(a);
        double acos = Math.cos(a);
        int x0 = cx + (int) (vx);
        int y0 = cy - (int) (vy);
        int prevX = x0;
        int prevY = y0;
        for (int i = 1; i < n * 2; i++) {
            double x = vx * acos - vy * asin;
            double y = vx * asin + vy * acos;
            int x1 = cx - r / 2 + (int) (x);
            int y1 = cy + r / 2 - (int) (y);
            vx = x;
            vy = y;
            drawLine(prevX, prevY, x1, y1, 1, c);
            prevX = x1;
            prevY = y1;
        }
        drawLine(prevX, prevY, x0, y0, 1, c);
        notifyListeners();
    }

    public void drawRegularPolygon(int cx, int cy, int n, int r, int theta, Color c) {
        double offset = theta * Math.PI / 180;
        double a = 2 * Math.PI / n;
        if(n % 2 == 0) {
            offset += a / 2;
        }
        double vx = - r * Math.sin(offset);
        double vy = r * Math.cos(offset);
        double asin = Math.sin(a);
        double acos = Math.cos(a);
        int x0 = cx + (int) (vx);
        int y0 = cy - (int) (vy);
        int prevX = x0;
        int prevY = y0;
        for (int i = 1; i < n; i++) {
            double x = vx * acos - vy * asin;
            double y = vx * asin + vy * acos;
            int x1 = cx + (int) (x);
            int y1 = cy - (int) (y);
            vx = x;
            vy = y;
            drawLine(prevX, prevY, x1, y1, 1, c);
            prevX = x1;
            prevY = y1;
        }
        drawLine(prevX, prevY, x0, y0, 1, c);
        notifyListeners();
    }

}
