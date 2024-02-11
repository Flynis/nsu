package ru.dyakun.paint.painter;

import ru.dyakun.paint.Canvas;

import javax.swing.*;
import java.util.EnumMap;
import java.util.Map;

public class PainterProxy {

    private Painter current;
    private final Map<PainterType, Painter> painters = new EnumMap<>(PainterType.class);

    public PainterProxy(Canvas canvas, JFrame frame) {
        painters.put(PainterType.LINE, new LinePainter(canvas, frame));
        painters.put(PainterType.POLYGON_STAMP, new PolygonStampPainter(canvas, frame));
        painters.put(PainterType.STAR_STAMP, new StarStampPainter(canvas, frame));
        painters.put(PainterType.FILLING, new FillingPainter(canvas, frame));
        painters.put(PainterType.PENCIL, new PencilPainter(canvas, frame));
        setCurrent(PainterType.PENCIL);
    }

    public void setCurrent(PainterType painterType) {
        this.current = painters.get(painterType);
        System.out.println("Painter changed to " + painterType.name());
    }

    public Painter getCurrent() {
        return current;
    }

}
