package ui;

import model.*;

// Contains all the simulation state related data
public class SimulatorState implements Tickable {
    private static SimulatorState instance;
    private Simulation simulation;
    private Boolean isRunning;

    // EFFECTS: creates a new simulation that is paused
    private SimulatorState() {
        if (instance != null) {
            throw new IllegalStateException();
        }
        simulation = new Simulation();
        isRunning = false;
    }

    // EFFECTS: returns the simulation state instance
    public static SimulatorState getInstance() {
        if (instance == null) {
            instance = new SimulatorState();
        }
        return instance;
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public Boolean getIsRunning() {
        return isRunning;
    }

    // MODIFIES: this
    // EFFECTS: updates the simulation state
    @Override
    public void tick() {
        // stub
    }
}
