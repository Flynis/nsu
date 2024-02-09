package ru.dyakun.paint;

import java.util.EnumMap;
import java.util.Map;

public class PainterProxy {

    private Painter current;
    private final Map<PainterType, Painter> painters = new EnumMap<>(PainterType.class);

    public PainterProxy(Canvas canvas) {
        painters.put(PainterType.LINE, new LinePainter(canvas));
        painters.put(PainterType.STAMP, new StampPainter(canvas));
        painters.put(PainterType.FILLING, new FillingPainter(canvas));
    }

    public void setCurrent(PainterType painterType) {
        this.current = painters.get(painterType);
    }

    public Painter getCurrent() {
        return current;
    }

}
