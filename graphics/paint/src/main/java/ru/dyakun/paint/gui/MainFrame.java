package ru.dyakun.paint.gui;

import ru.dyakun.paint.model.CanvasManager;
import ru.dyakun.paint.tool.ToolManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;

public class MainFrame extends JFrame {

    private final CanvasManager canvasManager;
    private final ToolManager toolManager;

    public MainFrame(CanvasManager canvasManager, ToolManager toolManager) {
        super("Paint");
        this.canvasManager = canvasManager;
        this.toolManager = toolManager;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        createUI(this);
        setPreferredSize(new Dimension(1600, 900));
        setMinimumSize(new Dimension(640, 480));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createUI(JFrame frame) {
        CanvasPane canvasPane = new CanvasPane(toolManager, canvasManager.getCanvas());
        JScrollPane scrollPane = new JScrollPane(canvasPane);
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

        List<Action> fileActions = actionKit.createFileActions(frame, canvasManager);
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
        toolBar.add(new JToolBar.Separator());
        toolsMenu.add(settingsAction);

        ButtonGroup toolsGroup = new ButtonGroup();
        List<Action> toolActions = actionKit.createToolActions(toolManager);
        for(var action: toolActions) {
            toolsMenu.add(action);
            JRadioButton button = jRadioButtonFromAction(action);
            toolBar.add(button);
            toolsGroup.add(button);
        }
        toolBar.add(new JToolBar.Separator());
        List<Action> stampActions = actionKit.createStampActions(toolManager);
        for(var action: stampActions) {
            toolsMenu.add(action);
            JRadioButton button = jRadioButtonFromAction(action);
            toolBar.add(button);
            toolsGroup.add(button);
        }
        toolBar.add(new JToolBar.Separator());

        var colorObserver = new ColorObserverPane();
        toolBar.add(colorObserver);
        toolBar.add(new JToolBar.Separator());

        ButtonGroup colorsGroup = new ButtonGroup();
        List<Action> colorActions = actionKit.createPaletteActions();
        for(var action: colorActions) {
            JRadioButton button = jRadioButtonFromAction(action);
            toolBar.add(button);
            colorsGroup.add(button);
        }
        Action chooseAction = actionKit.createColorChooseAction(frame);
        toolsMenu.add(chooseAction);
        JRadioButton chooseButton = jRadioButtonFromAction(chooseAction);
        toolBar.add(chooseButton);
        colorsGroup.add(chooseButton);
        toolBar.add(new JToolBar.Separator());

        Action aboutAction = actionKit.createAboutAction(frame);
        toolBar.add(aboutAction);
        helpMenu.add(aboutAction);
    }

    private static JRadioButton jRadioButtonFromAction(Action action) {
        JRadioButton button = new JRadioButton();
        button.setIcon((Icon) action.getValue(Action.SMALL_ICON));
        button.setToolTipText((String) action.getValue(Action.SHORT_DESCRIPTION));
        button.addActionListener(action);
        button.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED) {
                button.setBackground(Colors.DARK_BACK_COLOR);
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                button.setBackground(Colors.LIGHT_BACK_COLOR);
            }
        });
        Object selectedKey = action.getValue(Action.SELECTED_KEY);
        if(selectedKey != null) {
            button.setSelected((Boolean) selectedKey);
        }
        return button;
    }

}
