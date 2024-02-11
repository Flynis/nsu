package ru.dyakun.paint;

import ru.dyakun.paint.painter.ManipulatorProxy;
import ru.dyakun.paint.painter.ManipulatorType;
import ru.dyakun.paint.painter.StampManipulator;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ru.dyakun.paint.IconUtil.loadIcon;
import static ru.dyakun.paint.painter.ManipulatorType.*;

public class Main extends JFrame {

    private final Canvas canvas = new Canvas();
    private final ManipulatorProxy manipulatorProxy = new ManipulatorProxy(canvas, this);

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        super("Paint");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        createUI(this);
        setPreferredSize(new Dimension(1600, 900));
        setMinimumSize(new Dimension(640, 480));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createUI(JFrame frame) {
        JFileChooser fileChooser = new JFileChooser();

        CanvasPane canvasPane = new CanvasPane(manipulatorProxy, canvas);
        frame.add(canvasPane);

        Action openAction = new AbstractAction("Open", loadIcon("/icons/open.png")) {
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
        Action saveAction = new AbstractAction("Save", loadIcon("/icons/save.png")) {
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
        Action exitAction = new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        };
        Action settingsAction = new AbstractAction("Settings", loadIcon("/icons/settings.png")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                manipulatorProxy.getCurrent().showSettingsDialog();
            }
        };
        Action eraserAction = new AbstractAction("Eraser", loadIcon("/icons/eraser.png")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.clear();
                canvas.repaint();
            }
        };

        ColorPane colorPane = new ColorPane();
        colorPane.addColorChangedListener(canvas::setColor);

        List<Action> toolActions = createToolActions();
        List<Action> stampActions = createStampActions();

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu toolsMenu = new JMenu("Tools");
        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        frame.setJMenuBar(menuBar);

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        frame.add(toolBar, BorderLayout.NORTH);

        fileMenu.add(openAction);
        fileMenu.add(saveAction);
        fileMenu.add(new JSeparator());
        fileMenu.add(exitAction);

        toolBar.add(openAction);
        toolBar.add(saveAction);
        toolBar.add(new JToolBar.Separator());

        toolBar.add(settingsAction);
        toolBar.add(eraserAction);
        for(var action: toolActions) {
            toolsMenu.add(action);
            toolBar.add(action);
        }
        toolBar.add(new JToolBar.Separator());
        for(var action: stampActions) {
            toolsMenu.add(action);
            toolBar.add(action);
        }
        toolsMenu.add(eraserAction);
        toolsMenu.add(settingsAction);

        toolBar.add(new JToolBar.Separator());
        toolBar.add(colorPane);
        toolBar.add(new JToolBar.Separator());
        List<Action> colorActions = colorPane.createColorActions(frame);
        for(var action: colorActions) {
            toolBar.add(action);
        }
    }

    private record ToolAction(String name, String path, ManipulatorType type) {}
    private List<Action> createToolActions() {
        ToolAction[] actions = {
            new ToolAction("Pencil", "/icons/pencil.png", PENCIL),
            new ToolAction("Line", "/icons/line.png", LINE),
            new ToolAction("Filling", "/icons/paint_bucket.png", FILLING),
        };
        List<Action> res = new ArrayList<>();
        for(var action: actions) {
            res.add(new AbstractAction(action.name, loadIcon(action.path)) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    manipulatorProxy.setCurrent(action.type);
                }
            });
        }
        return res;
    }

    private record StampAction(String name, String path, ManipulatorType type, int n) {}
    private List<Action> createStampActions() {
        StampAction[] actions = {
            new StampAction("Square", "/icons/square.png", POLYGON_STAMP, 4),
            new StampAction("Pentagon", "/icons/pentagon.png", POLYGON_STAMP, 5),
            new StampAction("Hexagon", "/icons/hexagon.png", POLYGON_STAMP, 6),
            new StampAction("Star", "/icons/star.png", STAR_STAMP, 5),
            new StampAction("Hexagram", "/icons/hexagram.png", STAR_STAMP, 6),
        };
        List<Action> res = new ArrayList<>();
        for(var action: actions) {
            res.add(new AbstractAction(action.name, loadIcon(action.path)) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    manipulatorProxy.setCurrent(action.type);
                    StampManipulator painter = (StampManipulator) manipulatorProxy.getCurrent();
                    painter.setN(action.n);
                }
            });
        }
        return res;
    }

}
