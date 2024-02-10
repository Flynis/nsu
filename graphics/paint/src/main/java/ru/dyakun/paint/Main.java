package ru.dyakun.paint;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class Main {

    private static final Canvas canvas = new Canvas();
    private static final PainterProxy painterProxy = new PainterProxy(canvas);

    public static void main(String[] args) {
        JFrame frame = new JFrame("Paint");
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        createUI(frame);
        frame.setPreferredSize(new Dimension(1600, 900));
        frame.setMinimumSize(new Dimension(640, 480));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void createUI(JFrame frame) {
        JFileChooser fileChooser = new JFileChooser();

        CanvasPane canvasPane = new CanvasPane(painterProxy, canvas);
        frame.add(canvasPane);

        Action openAction = new AbstractAction("Open", getIcon("/icons/open.png")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setDialogTitle("Choose file");
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        BufferedImage image = ImageIO.read(fileChooser.getSelectedFile());
                        canvas.setImage(image);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame, "Failed to open file", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
        Action saveAction = new AbstractAction("Save", getIcon("/icons/save.png")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setDialogTitle("Save file");
                int result = fileChooser.showSaveDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        BufferedImage image = canvas.getImage();
                        ImageIO.write(image, "png", fileChooser.getSelectedFile());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame, "Failed to open file", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };

        ColorPane colorPane = new ColorPane();
        colorPane.addColorChangedListener(canvas::setColor);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        frame.setJMenuBar(menuBar);

        JToolBar toolBar = new JToolBar();
        toolBar.add(new Button("A"));
        toolBar.add(new Button("B"));
        toolBar.setFloatable(false);
        frame.add(toolBar, BorderLayout.NORTH);
    }

    private static ImageIcon getIcon(String resource) {
        return new ImageIcon(Objects.requireNonNull(Main.class.getResource(resource)));
    }

    private JMenu createFileMenu() {
        JMenu file = new JMenu("File");

        JMenuItem open = new JMenuItem("Open");
        open.addActionListener(e -> System.out.println("ActionListener.actionPerformed : open"));
        file.add(open);

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> dispose());
        file.add(exit);

        return file;
    }

}
