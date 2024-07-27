package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// Contains all the rendering related data for the SWING based GUI
public class NewSimulationGraphics {
    private static final String WINDOW_TITLE = "N-Body Simulation";

    private static final Dimension WINDOW_DIMENSION = new Dimension(1000, 700);
    private static final Dimension EDITORVIEW_DIMENSION = new Dimension((int) (WINDOW_DIMENSION.width * 0.4f),
            WINDOW_DIMENSION.height);
    private static final Dimension VIEWPORT_DIMENSION = new Dimension((int) (WINDOW_DIMENSION.width * 0.6f),
            WINDOW_DIMENSION.height);

    // Tab panel which is used to cycle through different lists of objects
    private class EditorTabPanel extends JTabbedPane {
        private static final String PLANET_LIST_NAME = "Planet List";
        private static final String COLLISION_LIST_NAME = "Collision List";
        private static final String SAVE_LIST_NAME = "Saved Simulations";

        public EditorTabPanel(JPanel planeList, JPanel colList, JPanel saveList) {
            setPreferredSize(EDITORVIEW_DIMENSION);
            addTab(PLANET_LIST_NAME, planeList);
            addTab(COLLISION_LIST_NAME, colList);
            addTab(SAVE_LIST_NAME, saveList);
            setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        }
    }

    JFrame window;
    EditorTabPanel editorTabSelector;

    public NewSimulationGraphics() {
        window = new JFrame(WINDOW_TITLE);
        window.setPreferredSize(WINDOW_DIMENSION);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        editorTabSelector = new EditorTabPanel(null, null, null);
        window.add(editorTabSelector);

        window.pack();
        window.setResizable(false);
        window.setVisible(true);
    }
}
