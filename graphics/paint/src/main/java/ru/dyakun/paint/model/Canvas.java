package ru.dyakun.paint.model;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static ru.dyakun.paint.model.GraphicsUtil.*;

public class Canvas {

    public static final Color BACKGROUND_COLOR = Color.WHITE;

    private BufferedImage image = null;
    private final List<CanvasListener> listeners = new ArrayList<>();

    Canvas() {}

    void setImage(BufferedImage image) {
        this.image = image;
        image.createGraphics();
        clear();
        for(var listener: listeners) {
            listener.onSourceChange(this.image);
        }
    }

    void resize(BufferedImage image) {
        var oldImage = this.image;
        setImage(image);
        var g = image.getGraphics();
        g.drawImage(oldImage, 0, 0, null);
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
            drawBresenhamLine(x0, y0, x1, y1, image, c);
        } else {
            Graphics2D g = (Graphics2D) image.getGraphics();
            g.setStroke(new BasicStroke(thickness));
            g.setColor(c);
            g.drawLine(x0, y0, x1, y1);
        }
        notifyListeners();
    }

    public void fill(int x0, int y0, Color c) {
        spanFilling(x0, y0, image, c);
        notifyListeners();
    }

    public void drawRegularStar(int cx, int cy, int n, int r, int theta, Color c) {
        Point[] outer = getRegularPolygonPoints(cx, cy, n, r, theta);
        theta += 180 / n;
        Point[] inner = getRegularPolygonPoints(cx, cy, n, r / 2, theta);
        for(int i = 0; i < n; i++) {
            drawLine(outer[i].x, outer[i].y, inner[i].x, inner[i].y, 1, c);
            int next = (i + 1) % n;
            drawLine(inner[i].x, inner[i].y, outer[next].x, outer[next].y, 1, c);
        }
        notifyListeners();
    }

    public void drawRegularPolygon(int cx, int cy, int n, int r, int theta, Color c) {
        if(n % 2 == 0) {
            theta += 180 / n;
        }
        Point[] points = getRegularPolygonPoints(cx, cy, n, r, theta);
        for(int i = 0; i < n; i++) {
            int next = (i + 1) % n;
            drawLine(points[i].x, points[i].y, points[next].x, points[next].y, 1, c);
        }
        notifyListeners();
    }

}
