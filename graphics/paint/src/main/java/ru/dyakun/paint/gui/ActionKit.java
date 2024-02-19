package ru.dyakun.paint.gui;

import ru.dyakun.paint.model.ImageManager;
import ru.dyakun.paint.model.ColorManager;
import ru.dyakun.paint.tool.StampTool;
import ru.dyakun.paint.tool.ToolManager;
import ru.dyakun.paint.tool.ToolType;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.Action.*;
import static ru.dyakun.paint.gui.Icons.*;
import static ru.dyakun.paint.tool.ToolType.*;

public class ActionKit {

    private void initAction(Action a, String name, ImageIcon icon) {
        a.putValue(SHORT_DESCRIPTION, name);
        a.putValue(NAME, name);
        a.putValue(SMALL_ICON, icon);
        a.putValue(LARGE_ICON_KEY, icon);
    }

    private record PaletteAction(String name, Color color) {}
    public List<Action> createPaletteActions() {
        PaletteAction[] commonColors = {
            new PaletteAction("Black", Color.BLACK),
            new PaletteAction("Red", Color.RED),
            new PaletteAction("Yellow", Color.YELLOW),
            new PaletteAction("Green", Color.GREEN),
            new PaletteAction("Cyan", ColorUIResource.CYAN),
            new PaletteAction("Blue", Color.BLUE),
            new PaletteAction("Pink", new Color(255, 0, 255)),
            new PaletteAction("White", Color.WHITE),
        };
        List<Action> res = new ArrayList<>();
        for(var action: commonColors) {
            Action a = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ColorManager.getInstance().setColor(action.color);
                }
            };
            initAction(a, action.name, createIcon(action.color));
            res.add(a);
        }
        res.get(0).putValue(SELECTED_KEY, Boolean.TRUE);
        return res;
    }

    public Action createColorChooseAction(JFrame frame) {
        String name = "Choose color";
        Action a = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color initial = ColorManager.getInstance().getColor();
                Color color = JColorChooser.showDialog(frame, name, initial);
                if(color != null) {
                    ColorManager.getInstance().setColor(color);
                }
            }
        };
        initAction(a, name, loadIcon("/icons/color_picker.png"));
        return a;
    }

    private record ToolAction(String name, String path, ToolType type) {}
    public List<Action> createToolActions(ToolManager toolManager) {
        ToolAction[] actions = {
                new ToolAction("Pencil", "/icons/pencil.png", PENCIL),
                new ToolAction("Line", "/icons/line.png", LINE),
                new ToolAction("Fill", "/icons/paint_bucket.png", FILL),
                new ToolAction("Eraser", "/icons/eraser.png", ERASER),
        };
        List<Action> res = new ArrayList<>();
        for(var action: actions) {
            Action a = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    toolManager.setCurrent(action.type);
                }
            };
            initAction(a, action.name, loadIcon(action.path));
            res.add(a);
        }
        res.get(0).putValue(SELECTED_KEY, Boolean.TRUE);
        return res;
    }

    private record StampAction(String name, String path, ToolType type, int n) {}
    public List<Action> createStampActions(ToolManager toolManager) {
        StampAction[] actions = {
                new StampAction("Square", "/icons/square.png", POLYGON_STAMP, 4),
                new StampAction("Star", "/icons/star.png", STAR_STAMP, 5),
        };
        List<Action> res = new ArrayList<>();
        for(var action: actions) {
            Action a = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    toolManager.setCurrent(action.type);
                    StampTool tool = (StampTool) toolManager.getCurrent();
                    tool.setN(action.n);
                }
            };
            initAction(a, action.name, loadIcon(action.path));
            res.add(a);
        }
        return res;
    }

    public Action createToolSettingsAction(SettingsManager settingsManager) {
        Action a = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                settingsManager.showSettings();
            }
        };
        initAction(a, "Settings", loadIcon("/icons/settings.png"));
        return a;
    }

    public Action createExitAction(JFrame frame) {
        String name = "Exit";
        Action a = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        };
        a.putValue(SHORT_DESCRIPTION, name);
        a.putValue(NAME, name);
        return a;
    }

    public Action createAboutAction(JFrame frame) {
        AboutDialog dialog = new AboutDialog(frame);
        Action a = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(true);
            }
        };
        initAction(a, "About program", loadIcon("/icons/info.png"));
        return a;
    }

    public List<Action> createFileActions(JFrame frame, ImageManager manager) {
        JFileChooser fileChooser = new JFileChooser();
        Action openAction = createOpenAction(frame, manager, fileChooser);
        Action saveAction = createSaveAction(frame, manager, fileChooser);
        return List.of(openAction, saveAction);
    }

    private Action createOpenAction(JFrame frame, ImageManager manager, JFileChooser fileChooser) {
        String desc = "*.jpg, *.jpeg, *.bpm, *.png, *.gif";
        var openFilter = new ExtensionFileFilter(desc, List.of("jpg", "jpeg", "bmp", "png", "gif"));
        Action a = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setDialogTitle("Choose file");
                fileChooser.setFileFilter(openFilter);
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        manager.loadImage(fileChooser.getSelectedFile());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame, "Failed to open file", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
        initAction(a, "Open", loadIcon("/icons/open.png"));
        return a;
    }

    private Action createSaveAction(JFrame frame, ImageManager manager, JFileChooser fileChooser) {
        var saveFilter = new ExtensionFileFilter("*.png", List.of("png"));
        Action a = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setDialogTitle("Save file");
                fileChooser.setFileFilter(saveFilter);
                int result = fileChooser.showSaveDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        manager.exportImageToPng(fileChooser.getSelectedFile());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame, "Failed to open file", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
        initAction(a, "Save", loadIcon("/icons/save.png"));
        return a;
    }

}
