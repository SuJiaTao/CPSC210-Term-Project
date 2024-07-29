package ui.panels;

import model.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

// Main window JFrame which is used to house all the graphics
public class MainWindow extends JFrame {
    public static final double HORIZONTAL_SPLIT_FACTOR = 0.65f;

    private EditorTabPanel editorTabPanel;
    private ViewportPanel viewportPanel;

    public MainWindow(String title, Dimension size) {
        setTitle(title);
        setPreferredSize(size);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        editorTabPanel = new EditorTabPanel();
        viewportPanel = new ViewportPanel();

        JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorTabPanel, viewportPanel);
        splitter.setDividerLocation(HORIZONTAL_SPLIT_FACTOR);
        splitter.setEnabled(false);

        add(splitter);
        pack();
        setResizable(false);
        setVisible(true);
    }

    public EditorTabPanel getEditorTabPanel() {
        return editorTabPanel;
    }

    public ViewportPanel gViewportPanel() {
        return viewportPanel;
    }
}
