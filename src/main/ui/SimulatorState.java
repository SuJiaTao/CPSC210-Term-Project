package ui;

import model.*;

// Contains all the simulation state related data
public class SimulatorState implements Tickable {
    private static SimulatorState instance;
    private Simulation simulation;
    private boolean isRunning;
    private long lastTickNanoseconds;
    private float lastDeltaTime;

    // EFFECTS: creates a new simulation that is paused
    private SimulatorState() {
        if (instance != null) {
            throw new IllegalStateException();
        }
        simulation = new Simulation();
        isRunning = false;
        lastTickNanoseconds = System.nanoTime();
        lastDeltaTime = 0.0f;
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

    public boolean getIsRunning() {
        return isRunning;
    }

    public void setIsRunning(boolean val) {
        isRunning = val;
    }

    public float getLastDeltaTime() {
        return lastDeltaTime;
    }

    // MODIFIES: this
    // EFFECTS: updates the simulation state
    @Override
    public void tick() {
        if (isRunning) {
            long deltaTimeNanoseconds = System.nanoTime() - lastTickNanoseconds;
            float deltaTimeSeconds = (float) deltaTimeNanoseconds / 1000000000.0f;
            simulation.progressBySeconds(deltaTimeSeconds);
            lastDeltaTime = deltaTimeSeconds;
            handleCollisionBehavior();

        }
        lastTickNanoseconds = System.nanoTime();
    }

    // MODIFIES: this
    // EFFECTS: handles the collision behavior between planets after each tick
    private void handleCollisionBehavior() {
        // TODO: make more interesting... perhaps ejecting debris and stuff!
        for (Collision collision : simulation.getCollisions()) {
            try {
                simulation.removePlanet(collision.getPlanetsInvolved().get(0));
            } catch (Exception e) {

            }
            try {
                simulation.removePlanet(collision.getPlanetsInvolved().get(1));
            } catch (Exception e) {

            }
        }
    }
}
