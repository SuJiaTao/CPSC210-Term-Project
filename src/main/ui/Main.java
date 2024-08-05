package ui;

// Holds the entry point 
public class Main {
    // EFFECTS: constructs a simulation state and UI, then updates them forever
    public static void main(String[] args) throws Exception {
        SimulatorState simState = SimulatorState.getInstance();
        SimulatorGUI simGUI = SimulatorGUI.getInstance();

        while (true) {
            simState.tick();
            simGUI.tick();
        }
    }
}
