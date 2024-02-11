package ru.dyakun.paint.painter;

import ru.dyakun.paint.Canvas;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class FillingManipulator extends Manipulator {

    public FillingManipulator(Canvas canvas, JFrame frame) {
        super(canvas, frame);
        dialog.initByProperty(new ArrayList<>());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
    }

}
