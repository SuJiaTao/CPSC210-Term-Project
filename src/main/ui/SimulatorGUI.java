package ui;

import model.*;
import ui.panels.*;
import java.awt.*;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

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

        // attemptToSetToDarkmode();
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

    public Planet getSelectedPlanet() {
        return mainWindow.getEditorTabPanel().getPlanetListPanel().getSelectedPlanet();
    }

    // EFFECTS: attempts to set the GUI to dark mode
    private void attemptToSetToDarkmode() {
        // NOTE:
        // this is such a horrific hack that I'm actually scared of myself.. ah well..
        // this is what happens when you make me do UI code in java
        String nimbName = null;
        for (LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
            if (lafInfo.getName().equals("Nimbus")) {
                nimbName = lafInfo.getClassName();
                break;
            }
        }

        // NOTE: if system doesn't have Nimbus, abort
        if (nimbName == null) {
            return;
        }

        // NOTE:
        // this is magic. literal JDK magic. please refer to
        // https://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/_nimbusDefaults.html
        // and
        // https://colorhunt.co/palette/151515301b3f3c415cb4a5a5
        // to decipher what is happening
        UIManager.put("control", new Color(0x3C514C));
        UIManager.put("text", new Color(0xB4A5A5));
        UIManager.put("nimbusLightBackground", new Color(0xB4A5A5));
        UIManager.put("nimbusSelectedText", new Color(0x301B3F));

        try {
            UIManager.setLookAndFeel(nimbName);
        } catch (Exception e) {
            // nothing we can do, whole program will have gone to hell probably
        }
    }

}
