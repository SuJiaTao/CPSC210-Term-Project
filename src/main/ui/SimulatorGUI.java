package ui;

import model.*;
import ui.panels.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

// Contains all the rendering related data for the SWING based GUI
public class SimulatorGUI {
    private static final String WINDOW_TITLE = "N-Body Simulator";

    private static final Dimension WINDOW_DIMENSION = new Dimension(1000, 700);
    private static final float LISTVIEW_WIDTH_FACTOR = 0.4f;
    private static final float VIEWPORT_WIDTH_FACTOR = 0.6f;
    private static final float LISTVIEW_LIST_HEIGHT_FACTOR = 0.6f;
    private static final float LISTVIEW_EDITOR_HEIGHT_FACTOR = 0.4f;
    private static final Dimension LISTVIEW_DIMENSION = new Dimension((int) (WINDOW_DIMENSION.width * 0.4f),
            WINDOW_DIMENSION.height);
    private static final Dimension VIEWPORT_DIMENSION = new Dimension((int) (WINDOW_DIMENSION.width * 0.6f),
            WINDOW_DIMENSION.height);

    private static final Dimension LISTVIEW_LIST_DIMENSION = new Dimension(LISTVIEW_DIMENSION.width,
            (int) (LISTVIEW_DIMENSION.height * 0.65f));
    private static final Dimension LISTVIEW_EDITOR_DIMENSION = new Dimension(LISTVIEW_DIMENSION.width,
            (int) (LISTVIEW_DIMENSION.height * 0.25f));

    private static int EDIT_FIELD_COLUMNS = 20;

    // Tab panel which is used to cycle through different lists of objects
    private class EditorTabPanel extends JTabbedPane {
        private static final String PLANET_LIST_NAME = "Planet List";
        private static final String COLLISION_LIST_NAME = "Collision List";
        private static final String SAVE_LIST_NAME = "Saved Simulations";

        public EditorTabPanel(JPanel planetList, JPanel colList, JPanel saveList) {
            setPreferredSize(LISTVIEW_DIMENSION);
            addTab(PLANET_LIST_NAME, planetList);
            addTab(COLLISION_LIST_NAME, colList);
            addTab(SAVE_LIST_NAME, saveList);
            setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        }
    }

    

    // Viewport panel which is used to host the 3D view of the simulation
    private class ViewportPanel extends JPanel {
        public ViewportPanel() {
            setPreferredSize(VIEWPORT_DIMENSION);
        }
    }

    // Main window JFrame which is used to house all the graphics
    private class MainWindow extends JFrame {
        public MainWindow(JComponent editorPanel, JComponent viewPort) {
            setLayout(new BorderLayout());
            setTitle(WINDOW_TITLE);
            setPreferredSize(WINDOW_DIMENSION);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            add(editorPanel, BorderLayout.WEST);
            add(viewPort, BorderLayout.EAST);

            pack();
            setResizable(false);
            setVisible(true);
        }
    }

    private SimulatorState simState;

    private EditorTabPanel editorTabSelector;
    private PlanetListPanel planetListPanel;

    private MainWindow window;
    private ViewportPanel viewport;

    public SimulatorGUI(SimulatorState state) {
        simState = state;
        for (int i = 0; i < 5; i++) {
            simState.getSimulation().addPlanet(new Planet("h", 1.0f));
        }

        planetListPanel = new PlanetListPanel();
        editorTabSelector = new EditorTabPanel(planetListPanel, new JPanel(), new JPanel());
        viewport = new ViewportPanel();
        window = new MainWindow(editorTabSelector, viewport);
    }
}
