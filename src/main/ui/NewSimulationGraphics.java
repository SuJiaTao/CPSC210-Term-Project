package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

// Contains all the rendering related data for the SWING based GUI
public class NewSimulationGraphics {
    private static final String WINDOW_TITLE = "N-Body Simulator";

    private static final Dimension WINDOW_DIMENSION = new Dimension(1000, 700);
    private static final Dimension LISTVIEW_DIMENSION = new Dimension((int) (WINDOW_DIMENSION.width * 0.4f),
            WINDOW_DIMENSION.height);
    private static final Dimension VIEWPORT_DIMENSION = new Dimension((int) (WINDOW_DIMENSION.width * 0.6f),
            WINDOW_DIMENSION.height);

    private static final Dimension LISTVIEW_LIST_DIMENSION = new Dimension(LISTVIEW_DIMENSION.width,
            (int) (LISTVIEW_DIMENSION.height * 0.75f));
    private static final Dimension LISTVIEW_EDITOR_DIMENSION = new Dimension(LISTVIEW_DIMENSION.width,
            (int) (LISTVIEW_DIMENSION.height * 0.25f));

    private SimulationManager manager;

    // Abstract List panel which is used to view and edit elements in a list
    private abstract class ListEditorPanel<T> extends JPanel {
        protected java.util.List<T> objList;
        private JList<String> list;
        private JScrollPane listScroller;
        private JComponent editorPanel;

        // EFFECTS: initializes list to be empty and listScroller to contain list, calls
        // on user defined initialization of editorpanel, and then packs the components
        public ListEditorPanel(java.util.List<T> objList) {
            setLayout(new BorderLayout());

            this.objList = objList;
            list = new JList<>();
            listScroller = new JScrollPane(list);
            listScroller.setPreferredSize(LISTVIEW_LIST_DIMENSION);

            editorPanel = initEditorPanel();
            editorPanel.setPreferredSize(LISTVIEW_EDITOR_DIMENSION);

            list.setListData(convertListToStrings());

            add(listScroller, BorderLayout.NORTH);
            add(editorPanel, BorderLayout.SOUTH);
        }

        protected JList<String> getList() {
            return list;
        }

        protected JComponent getEditorPanel() {
            return editorPanel;
        }

        // EFFECTS: expected that the user defines a means to initialize the editor
        // panel in this method, and returns it
        public abstract JComponent initEditorPanel();

        // EFFECTS: expected that the user defines a means to conver their objectList
        // into an array of string
        public abstract String[] convertListToStrings();

    }

    // Tab panel which is used to cycle through different lists of objects
    private class EditorTabPanel extends JTabbedPane {
        private static final String PLANET_LIST_NAME = "Planet List";
        private static final String COLLISION_LIST_NAME = "Collision List";
        private static final String SAVE_LIST_NAME = "Saved Simulations";

        public EditorTabPanel(JPanel planeList, JPanel colList, JPanel saveList) {
            setPreferredSize(LISTVIEW_DIMENSION);
            addTab(PLANET_LIST_NAME, planeList);
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

    MainWindow window;
    EditorTabPanel editorTabSelector;
    ViewportPanel viewport;

    public NewSimulationGraphics(SimulationManager simManager) {
        manager = simManager;
        editorTabSelector = new EditorTabPanel(null, null, null);
        viewport = new ViewportPanel();
        window = new MainWindow(editorTabSelector, viewport);
    }
}
