package ru.dyakun.paint.painter;

import ru.dyakun.paint.Canvas;

import javax.swing.*;
import java.util.EnumMap;
import java.util.Map;

public class ManipulatorProxy {

    private Manipulator current;
    private final Map<ManipulatorType, Manipulator> painters = new EnumMap<>(ManipulatorType.class);

    public ManipulatorProxy(Canvas canvas, JFrame frame) {
        painters.put(ManipulatorType.LINE, new LineManipulator(canvas, frame));
        painters.put(ManipulatorType.POLYGON_STAMP, new PolygonStampManipulator(canvas, frame));
        painters.put(ManipulatorType.STAR_STAMP, new StarStampManipulator(canvas, frame));
        painters.put(ManipulatorType.FILLING, new FillingManipulator(canvas, frame));
        painters.put(ManipulatorType.PENCIL, new PencilManipulator(canvas, frame));
        setCurrent(ManipulatorType.PENCIL);
    }

    public void setCurrent(ManipulatorType manipulatorType) {
        this.current = painters.get(manipulatorType);
        System.out.println("Painter changed to " + manipulatorType.name());
    }

    public Manipulator getCurrent() {
        return current;
    }

}
