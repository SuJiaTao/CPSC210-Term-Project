package ui.panels;

import model.*;
import ui.Tickable;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

// Main window JFrame which is used to house all the graphics
public class MainWindow extends JFrame implements Tickable {
    public static final double SPLIT_WEIGHT = 0.0f;

    private EditorTabPanel editorTabPanel;
    private ViewportPanel viewportPanel;

    public MainWindow(String title, Dimension size) {
        setLayout(new BorderLayout());
        setTitle(title);
        setPreferredSize(size);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
    // EFFECTS: updates self and all relevant sub-components
    @Override
    public void tick() {
        editorTabPanel.tick();
        viewportPanel.tick();
    }
}
