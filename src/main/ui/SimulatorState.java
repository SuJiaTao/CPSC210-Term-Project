package ui;

import model.*;
import model.exceptions.PlanetDoesntExistException;

import java.util.concurrent.locks.*;

// Contains all the simulation state related data
public class SimulatorState implements Tickable {
    public static final float TIMESCALE_MIN = 1.0f;
    public static final float TIMESCALE_MAX = 20.0f;

    private static SimulatorState instance;
    private Simulation simulation;
    private float lastSimTime;
    private float timeScale;
    private boolean isRunning;
    private long lastTickNanoseconds;
    private Lock lock;

    // EFFECTS: creates a new simulation that is paused
    private SimulatorState() {
        if (instance != null) {
            throw new IllegalStateException();
        }
        simulation = new Simulation();
        lastSimTime = 0.0f;
        timeScale = 1.0f;
        isRunning = false;
        lastTickNanoseconds = System.nanoTime();
        lock = new ReentrantLock();
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

    public float getTimeScale() {
        return timeScale;
    }

    public void setTimeScale(float newTimeScale) {
        timeScale = newTimeScale;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    // MODIFIES: this
    // EFFECTS: updates the simulation state
    @Override
    public void tick() {
        if (isRunning) {
            long deltaTimeNanoseconds = System.nanoTime() - lastTickNanoseconds;
            float deltaTimeSeconds = (float) deltaTimeNanoseconds / 1000000000.0f;

            lock();
            lastSimTime = simulation.getTimeElapsed();
            simulation.progressBySeconds(deltaTimeSeconds * timeScale);
            handleCollisionBehavior();
            unlock();

            if (simulation.getPlanets().size() == 0) {
                isRunning = false;
            }
        }
        lastTickNanoseconds = System.nanoTime();
    }

    // MODIFIES: this
    // EFFECTS: handles the collision behavior between planets after each tick
    private void handleCollisionBehavior() {
        for (Collision collision : simulation.getCollisions()) {
            if (collision.getCollisionTime() < lastSimTime) {
                continue;
            }
            Planet planet1 = collision.getPlanetsInvolved().get(0);
            Planet planet2 = collision.getPlanetsInvolved().get(1);
            if (!(simulation.getPlanets().contains(planet1) && simulation.getPlanets().contains(planet2))) {
                return;
            }

            float p1Weight = planet1.getMass();
            float p2Weight = planet2.getMass();
            Vector3 newPos = interpolateVector3ByWeight(planet1.getPosition(), planet2.getPosition(), p1Weight,
                    p2Weight);
            Vector3 newVel = interpolateVector3ByWeight(planet1.getVelocity(), planet2.getVelocity(), p1Weight,
                    p2Weight);
            float newRadius = calculateCombinedRadius(planet1, planet2);

            Planet bigPlanet = null;
            Planet smallPlanet = null;
            if (planet1.getRadius() > planet2.getRadius()) {
                bigPlanet = planet1;
                smallPlanet = planet2;
            } else {
                bigPlanet = planet2;
                smallPlanet = planet1;
            }

            simulation.removePlanet(smallPlanet);
            bigPlanet.setPosition(newPos);
            bigPlanet.setVelocity(newVel);
            bigPlanet.setRadius(newRadius);
        }
    }

    // REQUIRES: weight1 + weight2 > 0
    // EFFECTS: interpolates between two Vector3s with a given weighting for each
    private Vector3 interpolateVector3ByWeight(Vector3 val1, Vector3 val2, float weight1, float weight2) {
        Vector3 weightedVal1 = Vector3.multiply(val1, weight1);
        Vector3 weightedVal2 = Vector3.multiply(val2, weight2);
        return Vector3.multiply(Vector3.add(weightedVal1, weightedVal2), 1.0f / (weight1 + weight2));
    }

    // EFFECTS: returns the radius of a planet with the combined volume of the given
    // two planets
    private float calculateCombinedRadius(Planet planet1, Planet planet2) {
        float rad1 = planet1.getRadius();
        float rad2 = planet2.getRadius();
        return (float) Math.cbrt(rad1 * rad1 * rad1 + rad2 * rad2 * rad2);
    }

    // MODIFIES: this
    // EFFECTS: tries to remove a planet from the simulation, if it still exists
    private void handleRemovePlanet(Planet toRemove) {
        try {
            simulation.removePlanet(toRemove);
        } catch (PlanetDoesntExistException pdee) {
            // oh well
        }
    }
}
