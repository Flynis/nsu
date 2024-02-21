package ru.dyakun.paint.gui;

import ru.dyakun.paint.gui.components.CanvasPane;
import ru.dyakun.paint.gui.components.ColorObserverPane;
import ru.dyakun.paint.gui.components.ToolRadioButtonGroup;
import ru.dyakun.paint.gui.components.WidgetKit;
import ru.dyakun.paint.model.ImageManager;
import ru.dyakun.paint.tool.ToolManager;
import ru.dyakun.paint.tool.ToolType;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {

    private final ImageManager imageManager;
    private final ToolManager toolManager;
    private JScrollPane scrollPane;

    public MainFrame(ImageManager imageManager, ToolManager toolManager) {
        super("Paint");
        this.imageManager = imageManager;
        this.toolManager = toolManager;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        createUI(this);
        setPreferredSize(new Dimension(1600, 900));
        setMinimumSize(new Dimension(640, 480));
        pack();
        setLocationRelativeTo(null);
        maximizeCanvas();
        setVisible(true);
    }

    private void maximizeCanvas() {
        var bounds = scrollPane.getViewportBorderBounds();
        imageManager.createEmptyImage(bounds.width - bounds.x, bounds.height - bounds.y);
    }

    private void createUI(JFrame frame) {
        CanvasPane canvasPane = new CanvasPane(toolManager, imageManager.getCanvas());
        scrollPane = new JScrollPane(canvasPane);
        frame.add(scrollPane);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu toolsMenu = new JMenu("Tools");
        JMenu helpMenu = new JMenu("Help");
        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);
        frame.setJMenuBar(menuBar);

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        frame.add(toolBar, BorderLayout.NORTH);

        ActionKit actionKit = new ActionKit();

        List<Action> fileActions = actionKit.createImageActions(frame, imageManager);
        for(var action: fileActions) {
            fileMenu.add(action);
            toolBar.add(action);
        }
        fileMenu.add(new JSeparator());
        fileMenu.add(actionKit.createExitAction(frame));
        toolBar.add(new JToolBar.Separator());

        SettingsManager settingsManager = new SettingsManager(frame, toolManager);
        Action settingsAction = actionKit.createToolSettingsAction(settingsManager);
        toolBar.add(settingsAction);
        toolsMenu.add(settingsAction);

        Action clearAction = actionKit.createClearAction(imageManager);
        toolBar.add(clearAction);
        toolsMenu.add(clearAction);
        toolBar.add(new JToolBar.Separator());

        Action chooseAction = actionKit.createColorChooseAction(frame);
        toolsMenu.add(chooseAction);
        toolsMenu.add(new JSeparator());

        ToolRadioButtonGroup toolsMenuGroup = new ToolRadioButtonGroup(toolManager);
        ToolRadioButtonGroup toolsGroup = new ToolRadioButtonGroup(toolManager);
        List<Action> toolActions = actionKit.createToolsActions(toolManager);
        for(var action: toolActions) {
            ToolType type = (ToolType) action.getValue(Action.SELECTED_KEY);
            JRadioButton button = WidgetKit.toolbarButtonFromAction(action);
            toolBar.add(button);
            toolsGroup.add(type, button);
            JRadioButton menuButton = WidgetKit.menuButtonFromAction(action);
            toolsMenu.add(menuButton);
            toolsMenuGroup.add(type, menuButton);
        }
        toolBar.add(new JToolBar.Separator());

        var colorObserver = new ColorObserverPane();
        toolBar.add(colorObserver);
        toolBar.add(new JToolBar.Separator());

        ButtonGroup colorsGroup = new ButtonGroup();
        List<Action> colorActions = actionKit.createPaletteActions();
        for(var action: colorActions) {
            JRadioButton button = WidgetKit.toolbarButtonFromAction(action);
            toolBar.add(button);
            colorsGroup.add(button);
        }
        JRadioButton chooseButton = WidgetKit.toolbarButtonFromAction(chooseAction);
        toolBar.add(chooseButton);
        colorsGroup.add(chooseButton);
        toolBar.add(new JToolBar.Separator());

        Action aboutAction = actionKit.createAboutAction(frame);
        toolBar.add(aboutAction);
        helpMenu.add(aboutAction);

        toolManager.setCurrent(ToolType.PENCIL); // choose tool only after all button groups initialized
    }

}
