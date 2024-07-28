package ui;

import model.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

// Contains all the rendering related data for the SWING based GUI
public class NBodySimulation {
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

    private Simulation simulation;

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

            add(listScroller, BorderLayout.NORTH);
            add(editorPanel, BorderLayout.SOUTH);

            updateListData();
        }

        protected JList<String> getList() {
            return list;
        }

        protected JComponent getEditorPanel() {
            return editorPanel;
        }

        // EFFECTS: expected that the user defines a means to initialize the editor
        // panel in this method, and returns it
        protected abstract JComponent initEditorPanel();

        // EFFECTS: expected that the user defines a means to conver their objectList
        // into an array of string
        protected abstract String[] convertListToStrings();

        // EFFECTS: updates the current JList list data
        public void updateListData() {
            list.setListData(convertListToStrings());
        }
    }

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

    // Planet list view panel which is used to view and edit planets
    private class PlanetListPanel extends ListEditorPanel<Planet> {
        // EFFECTS: constructs the list editor with the SimulationManager's planet list
        public PlanetListPanel() {
            super(simulation.getPlanets());
        }

        // Planet editor panel used to edit plant properties
        // NOTE: woaw!!! 2-levels of class nesting?? probably bad design
        private class PlanetEditorPanel extends JPanel {
            private JTextField nameEditField;
            private JTextField posEditField;
            private JTextField velEditField;
            private JTextField radEditField;

            public PlanetEditorPanel() {
                super(new GridBagLayout());

                JLabel editorTitleLabel = new JLabel("Edit Planet");
                editorTitleLabel.setHorizontalAlignment(JLabel.CENTER);
                editorTitleLabel.setFont(new Font(editorTitleLabel.getFont().getName(),
                        Font.BOLD, 15));

                add(editorTitleLabel, createConstraints(0, 0, 3));

                add(new JLabel("Name: ", JLabel.RIGHT), createConstraints(0, 1, 1));
                nameEditField = new JTextField(EDIT_FIELD_COLUMNS);
                add(nameEditField, createConstraints(1, 1, 2));

                add(new JLabel("Position: ", JLabel.RIGHT), createConstraints(0, 2, 1));
                posEditField = new JTextField(EDIT_FIELD_COLUMNS);
                add(posEditField, createConstraints(1, 2, 2));

                add(new JLabel("Velocity: ", JLabel.RIGHT), createConstraints(0, 3, 1));
                velEditField = new JTextField(EDIT_FIELD_COLUMNS);
                add(velEditField, createConstraints(1, 3, 2));

                add(new JLabel("Radius: ", JLabel.RIGHT), createConstraints(0, 4, 1));
                radEditField = new JTextField(EDIT_FIELD_COLUMNS);
                add(radEditField, createConstraints(1, 4, 2));

            }

            private GridBagConstraints createConstraints(int gx, int gy, int width) {
                GridBagConstraints gbConst = new GridBagConstraints();
                gbConst.fill = GridBagConstraints.BOTH;
                gbConst.gridx = gx;
                gbConst.gridy = gy;
                gbConst.gridwidth = width;
                gbConst.weightx = 0.5;
                gbConst.insets = new Insets(5, 0, 5, 20);
                return gbConst;
            }
        }

        @Override
        protected JComponent initEditorPanel() {
            return new PlanetEditorPanel();
        }

        @Override
        protected String[] convertListToStrings() {
            String[] names = new String[objList.size()];
            for (int i = 0; i < names.length; i++) {
                names[i] = objList.get(i).getName();
            }
            return names;
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

    public NBodySimulation() {
        simulation = new Simulation();
        for (int i = 0; i < 5; i++) {
            simulation.addPlanet(new Planet("h", 1.0f));
        }

        editorTabSelector = new EditorTabPanel(new PlanetListPanel(), new JPanel(), new JPanel());
        viewport = new ViewportPanel();
        window = new MainWindow(editorTabSelector, viewport);

    }
}
