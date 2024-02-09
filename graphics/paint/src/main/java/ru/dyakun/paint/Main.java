package ru.dyakun.paint;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Main extends JFrame {

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        super("Paint");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Canvas canvas = new Canvas();
        PainterProxy painterProxy = new PainterProxy(canvas);

        JFileChooser fileChooser = new JFileChooser();
        JColorChooser colorChooser = new JColorChooser();

        CanvasPane canvasPane = new CanvasPane(painterProxy, canvas);
        add(canvasPane);

        Action openAction = new AbstractAction("Open", getIcon("/icons/open.png")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setDialogTitle("Choose file");
                int result = fileChooser.showOpenDialog(Main.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        BufferedImage image = ImageIO.read(fileChooser.getSelectedFile());
                        canvas.setImage(image);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(Main.this, "Failed to open file", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
        Action saveAction = new AbstractAction("Save", getIcon("/icons/save.png")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setDialogTitle("Save file");
                int result = fileChooser.showSaveDialog(Main.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        BufferedImage image = canvas.getImage();
                        ImageIO.write(image, "png", fileChooser.getSelectedFile());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(Main.this, "Failed to open file", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
        Action chooseColor = new AbstractAction("Choose color", getIcon("/icons/color_picker.png")) {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };



        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        setJMenuBar(menuBar);

        JToolBar toolBar = new JToolBar();
        toolBar.add(new Button("A"));
        toolBar.add(new Button("B"));
        toolBar.setFloatable(false);
        add(toolBar, BorderLayout.NORTH);

        setPreferredSize(new Dimension(1600, 900));
        setMinimumSize(new Dimension(640, 480));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private ImageIcon getIcon(String resource) {
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
