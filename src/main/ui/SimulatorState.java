package ui;

import model.*;

// Contains all the simulation state related data
public class SimulatorState {
    private Simulation simulation;
    private Boolean isRunning;

    // EFFECTS: creates a new simulation that is paused
    public SimulatorState() {
        simulation = new Simulation();
        isRunning = false;
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public Boolean getIsRunning() {
        return isRunning;
    }
}
