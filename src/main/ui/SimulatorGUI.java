package ui;

import model.*;
import ui.panels.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

// Contains all the rendering related data for the SWING based GUI
public class SimulatorGUI {
    private static SimulatorGUI instance;

    private static final String WINDOW_TITLE = "N-Body Simulator";
    private static final Dimension WINDOW_DIMENSION = new Dimension(1000, 700);

    private MainWindow mainWindow;

    private SimulatorGUI() {
        if (instance != null) {
            throw new IllegalStateException();
        }

        for (int i = 0; i < 5; i++) {
            // SimulatorState.getInstance().getSimulation().addPlanet(new Planet("h",
            // 1.0f));
        }

        mainWindow = new MainWindow(WINDOW_TITLE, WINDOW_DIMENSION);
    }

    public static SimulatorGUI getInstance() {
        if (instance == null) {
            instance = new SimulatorGUI();
        }
        return instance;
    }

}
