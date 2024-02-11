package ru.dyakun.paint.painter;

import ru.dyakun.paint.Canvas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class PencilPainter extends Painter {

    private int x0;
    private int y0;
    private int strokeWidth = 1;
    private boolean mousePressed = false;

    public PencilPainter(Canvas canvas, JFrame frame) {
        super(canvas, frame);
        initSettingDialog();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mousePressed = true;
        x0 = e.getX();
        y0 = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mousePressed = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(mousePressed) {
            int x = e.getX();
            int y = e.getY();
            Graphics2D g = (Graphics2D) canvas.getImage().getGraphics();
            g.setStroke(new BasicStroke(strokeWidth));
            g.setColor(canvas.getColor());
            g.drawLine(x0, y0, x, y);
            canvas.repaint();
        }
    }

    private void initSettingDialog() {
        JLabel label = new JLabel("Stroke width");

        JSlider slider = new JSlider();
        slider.setMaximum(30);
        slider.setMinimum(1);
        slider.setMinorTickSpacing(1);
        slider.setMajorTickSpacing(5);
        slider.setValue(1);
        slider.setPaintLabels(true);

        JButton okButton = new JButton("Ok");
        okButton.addActionListener(e -> {
            strokeWidth = slider.getValue();
            dialog.setVisible(false);
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.setVisible(false));

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.add(label);
        panel.add(slider);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(okButton);
        buttons.add(cancelButton);

        dialog.add(panel);
        dialog.add(buttons, BorderLayout.SOUTH);
    }

}
