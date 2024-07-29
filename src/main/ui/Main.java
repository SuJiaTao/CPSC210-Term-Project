package ui;

import model.Planet;

public class Main {
    public static void main(String[] args) throws Exception {
        SimulatorState simState = SimulatorState.getInstance();
        SimulatorGUI simGUI = SimulatorGUI.getInstance();

        debugInit();

        while (true) {
            simState.tick();
            simGUI.tick();
        }
    }

    // TODO: remove
    public static void debugInit() {
        for (int i = 0; i < 5; i++) {
            SimulatorState.getInstance().getSimulation().addPlanet(new Planet("" + i, 1.0f));
        }
    }
}
