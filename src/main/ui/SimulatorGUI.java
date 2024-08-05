package ui;

import model.*;
import ui.panels.*;
import java.awt.*;

// Contains all the rendering related data for the SWING based GUI
public class SimulatorGUI implements Tickable {
    private static SimulatorGUI instance;

    private static final String WINDOW_TITLE = "N-Body Simulator";
    private static final Dimension WINDOW_DIMENSION = new Dimension(1000, 700);

    private MainWindow mainWindow;

    // EFFECTS: throws IllegalStateException of the instance of this class already
    // exists, initialzes the main window
    private SimulatorGUI() {
        if (instance != null) {
            throw new IllegalStateException();
        }

        mainWindow = new MainWindow(WINDOW_TITLE, WINDOW_DIMENSION);
    }

    // EFFECTS: if there is no current instance, initialize it, and return the
    // instance
    public static SimulatorGUI getInstance() {
        if (instance == null) {
            instance = new SimulatorGUI();
        }
        return instance;
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    // MODIFIES: this
    // EFFECTS: updates self and all relevant sub-components
    @Override
    public void tick() {
        mainWindow.tick();
    }

    // EFFECTS: this is hilarious and speaks to the terriblness of the design of
    // this code. returns the currently selected planet
    public Planet getSelectedPlanet() {
        return mainWindow.getEditorTabPanel().getPlanetListPanel().getSelectedPlanet();
    }

}
