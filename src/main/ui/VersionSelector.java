package ui;

import ui.legacy.*;
import javax.swing.*;

// Simple version selector for old or new version of the simulation manager
public class VersionSelector {
    // EFFECTS: creates a popup prompting the user to either run the new or legacy
    // version of the NBody simulator, and runs accordingly
    public VersionSelector() {
        String[] options = { "New", "Legacy" };
        int result = JOptionPane.showOptionDialog(null,
                "Which version of NBody to run??", "Haii :3", JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (result == 0) {
            SimulatorState simState = SimulatorState.getInstance();
            SimulatorGUI simGfx = SimulatorGUI.getInstance();
            while (true) {
                simState.tick();
                simGfx.tick();
            }
        }

        if (result == 1) {
            ensureRanWithJavawIfWindows();
            try {
                SimulationManager simManager = new SimulationManager();
                simManager.mainLoop();
            } catch (Exception exc) {
                // oops, shouldnt happen
                throw new IllegalStateException();
            }
        }
    }

    // EFFECTS: ensures that the user ran the program with javaw given that the
    // current OS is windows
    private static void ensureRanWithJavawIfWindows() {
        // NOTE: this is a hack
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            return;
        }

        if (System.console() != null) {
            JOptionPane.showMessageDialog(null,
                    "You must run the program with javaw if you want the legacy version", "Hey!",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }
}
