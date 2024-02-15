package ru.dyakun.paint.model;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;

public class GraphicsUtil {

    private GraphicsUtil() {
        throw new AssertionError();
    }

    private record Span(int left, int right, int y) {
        public boolean isEmpty() {
            return left == right;
        }
    }

    private static Span findSpan(int x0, int y0, int c, BufferedImage image) {
        if (image.getRGB(x0, y0) != c) {
            return new Span(x0, x0, y0);
        }
        int x = x0 - 1;
        while (x >= 0 && image.getRGB(x, y0) == c) {
            x--;
        }
        int left = x + 1;
        x = x0 + 1;
        while (x < image.getWidth() && image.getRGB(x, y0) == c) {
            x++;
        }
        int right = x - 1;
        return new Span(left, right, y0);
    }

    private static void findSpanNeighbors(Span span, int y, Deque<Span> stack, int c, BufferedImage image) {
        for (int i = span.left; i < span.right; i++) {
            var neighbor = findSpan(i, y, c, image);
            if (neighbor.isEmpty()) {
                continue;
            }
            i = neighbor.right + 1;
            stack.add(neighbor);
        }
    }

    public static void spanFilling(int x0, int y0, BufferedImage image, Color newColor) {
        int oldColor = image.getRGB(x0, y0);
        if (oldColor == newColor.getRGB()) {
            return;
        }
        Deque<Span> stack = new ArrayDeque<>();
        var firstSpan = findSpan(x0, y0, oldColor, image);
        stack.addFirst(firstSpan);
        while (!stack.isEmpty()) {
            var span = stack.pollFirst();
            // recolor span
            if(image.getRGB(span.left, span.y) == newColor.getRGB()) {
                continue;
            }
            for (int i = span.left; i <= span.right; i++) {
                image.setRGB(i, span.y, newColor.getRGB());
            }
            if (span.y - 1 >= 0) { // search upper spans
                findSpanNeighbors(span, span.y - 1, stack, oldColor, image);
            }
            if(span.y + 1 < image.getHeight()) { // search lower spans
                findSpanNeighbors(span, span.y + 1, stack, oldColor, image);
            }
        }
    }

    private static void setPixel(int x, int y, BufferedImage image, Color c) {
        if(x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight()) {
            image.setRGB(x, y, c.getRGB());
        }
    }

    public static void drawBresenhamLine(int x0, int y0, int x1, int y1, BufferedImage image, Color c) {
        int dx = x1 - x0;
        int dy = y1 - y0;
        int signX = Integer.compare(dx, 0);
        int signY = Integer.compare(dy, 0);
        // make dx and dy positive
        if (dx < 0) {
            dx = -dx;
        }
        if (dy < 0) {
            dy = -dy;
        }
        int pdx, pdy, es, el;
        if (dx > dy) {
            pdx = signX;
            pdy = 0;
            es = dy;
            el = dx;
        }
        else {
            pdx = 0;
            pdy = signY;
            es = dx;
            el = dy;
        }
        int x = x0;
        int y = y0;
        int err = el / 2;
        // first point
        setPixel(x, y, image, c);
        for (int i = 0; i < el; i++) {
            err -= es;
            if (err < 0) {
                err += el;
                x += signX;
                y += signY;
            }
            else {
                x += pdx;
                y += pdy;
            }
            setPixel(x, y, image, c);
        }
    }

    public static class Point {
        public int x;
        public int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static Point[] getRegularPolygonPoints(int cx, int cy, int n, int r, int theta) {
        double offset = theta * Math.PI / 180;
        double a = 2 * Math.PI / n;
        Point[] points = new Point[n];
        double vx = - r * Math.sin(offset);
        double vy = r * Math.cos(offset);
        double asin = Math.sin(a);
        double acos = Math.cos(a);
        points[0] = new Point(cx + (int) (vx), cy - (int) (vy));
        for (int i = 1; i < n; i++) {
            double x = vx * acos - vy * asin;
            double y = vx * asin + vy * acos;
            points[i] = new Point(cx + (int) (x), cy - (int) (y));
            vx = x;
            vy = y;
        }
        return points;
    }

}
