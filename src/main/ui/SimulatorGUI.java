package ui;

import ui.panels.*;
import java.awt.*;

// Contains all the rendering related data for the SWING based GUI
public class SimulatorGUI implements Tickable {
    private static SimulatorGUI instance;

    private static final String WINDOW_TITLE = "N-Body Simulator";
    private static final Dimension WINDOW_DIMENSION = new Dimension(1000, 700);

    private MainWindow mainWindow;

    private SimulatorGUI() {
        if (instance != null) {
            throw new IllegalStateException();
        }

        mainWindow = new MainWindow(WINDOW_TITLE, WINDOW_DIMENSION);
    }

    public static SimulatorGUI getInstance() {
        if (instance == null) {
            instance = new SimulatorGUI();
        }
        return instance;
    }

    // MODIFIES: this
    // EFFECTS: updates self and all relevant sub-components
    @Override
    public void tick() {
        mainWindow.tick();
    }

}
