package ru.dyakun.dif.transport;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoordinateSystem {

    private static final Logger logger = LoggerFactory.getLogger(CoordinateSystem.class);
    private final Canvas canvas;
    private final Font font = new Font("Roboto", 20);
    private final Text digit = new Text();
    private final double cellW;
    private final double cellH;
    private final double pointRadius;
    private final double centerX;
    private final double centerY;

    public CoordinateSystem(Canvas canvas) {
        this.canvas = canvas;
        digit.setFont(font);
        cellW = 110;
        cellH = 600;
        centerX = 200;
        centerY = canvas.getHeight() - 100;
        pointRadius = 10;
        logger.info("Center ({}, {}), cell[{}, {}], point radius = {}", centerX, centerY, cellW, cellH, pointRadius);
    }

    private void drawDigit(int d, double x, double y, GraphicsContext g) {
        digit.setText(Integer.toString(d));
        var bounds = digit.getLayoutBounds();
        g.fillText(digit.getText(), x - bounds.getWidth() - 4, y + bounds.getHeight() - 4);
    }

    public void drawFunction(double a, double h, double[] values, Color c) {
        var g = canvas.getGraphicsContext2D();
        g.save();
        g.setFill(c);
        for(int j = 0; j < values.length; j++) {
            double x = a + j * h;
            double y = values[j];
            drawPoint(x, y, g);
        }
        g.restore();
    }

    private void drawPoint(double x0, double y0, GraphicsContext g) {
        double x = centerX + x0 * cellW - pointRadius / 2;
        double y = centerY - y0 * cellH - pointRadius / 2;
        g.fillOval(x, y, pointRadius, pointRadius);
    }

    public void drawContinuousFunction(double a, double b, double t, Function f, Color c) {
        var g = canvas.getGraphicsContext2D();
        g.save();
        g.setFill(c);
        double d = 0.01;
        double x = a;
        while (x <= b) {
            double y = f.apply(t, x);
            drawPoint(x, y, g);
            x += d;
        }
        g.restore();
    }

    private void clear() {
        var g = canvas.getGraphicsContext2D();
        g.save();
        g.setFill(Color.web("#F0F5F9"));
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.restore();
    }

    public void redraw() {
        clear();

        var g = canvas.getGraphicsContext2D();
        g.save();

        g.setStroke(Color.BLACK);
        g.setLineWidth(0.5);
        g.setFont(font);

        // print 0
        double x = centerX;
        double y = centerY;
        drawDigit(0, x, y, g);

        // print upper part
        y = centerY - cellH;
        int n = 1;
        while (y > 0) {
            drawDigit(n, x, y, g);
            g.strokeLine(0, y, canvas.getWidth(), y);
            y -= cellH;
            n += 1;
        }

        // print bottom part
        y = centerY + cellH;
        n = -1;
        while (y < canvas.getHeight()) {
            drawDigit(n, x, y, g);
            g.strokeLine(0, y, canvas.getWidth(), y);
            y += cellH;
            n -= 1;
        }

        // print right part
        x = centerX + cellW;
        y = centerY;
        n = 1;
        while (x < canvas.getWidth()) {
            drawDigit(n, x, y, g);
            g.strokeLine(x, 0, x, canvas.getHeight());
            x += cellW;
            n += 1;
        }

        // print left part
        x = centerX - cellW;
        n = -1;
        while (x > 0) {
            drawDigit(n, x, y, g);
            g.strokeLine(x, 0, x, canvas.getHeight());
            x -= cellW;
            n -= 1;
        }

        // print main axis
        g.setLineWidth(2);
        g.strokeLine(0, centerY, canvas.getWidth(), centerY);
        g.strokeLine(centerX, 0, centerX, canvas.getHeight());

        g.restore();
    }

}
