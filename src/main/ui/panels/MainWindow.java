package ui.panels;

import java.awt.*;
import java.awt.event.*;

import ui.SimulatorUtils;
import ui.Tickable;
import javax.swing.*;

import model.Event;
import model.EventLog;

// Main window JFrame which is used to house all the graphics
public class MainWindow extends JFrame implements Tickable {
    public static final double SPLIT_WEIGHT = 0.01f;

    private EditorTabPanel editorTabPanel;
    private ViewportPanel viewportPanel;

    // EFFECTS: initializes all window parameters, the editorTab and the 3D viewport
    public MainWindow(String title, Dimension size) {
        setLayout(new BorderLayout());
        setTitle(title);
        setPreferredSize(size);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(SimulatorUtils.loadImage("icon.png"));

        // NOTE:
        // i'm not happy to implement it this way but oh well
        addWindowListener(new WindowAdapter() {
            // Adapted from:
            // https://stackoverflow.com/questions/16295942/java-swing-adding-action-listener-for-exit-on-close
            // EFFECTS: prints all events in the eventlog instance
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                System.out.println("EVENT LOG:");
                for (Event event : EventLog.getInstance()) {
                    System.out.println(event.getDescription());
                }
            }
        });

        editorTabPanel = new EditorTabPanel();
        viewportPanel = new ViewportPanel();

        JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorTabPanel, viewportPanel);
        splitter.setResizeWeight(SPLIT_WEIGHT);
        splitter.setEnabled(false);

        add(splitter);
        pack();
        setResizable(false);
        setVisible(true);
    }

    public EditorTabPanel getEditorTabPanel() {
        return editorTabPanel;
    }

    public ViewportPanel getViewportPanel() {
        return viewportPanel;
    }

    // MODIFIES: this
    // EFFECTS: updates the editorTab and the 3D viewport
    @Override
    public void tick() {
        editorTabPanel.tick();
        viewportPanel.tick();
    }
}
