package ui;

import java.util.*;

import model.Collision;
import model.Planet;

// Represents the current n-body simulation state
public class Simulation {
    private float timeElapsed;
    private List<Planet> bodies;
    private List<Collision> collisions;

    // EFFECTS: creates a simulation with no time elapsed and no bodies or
    // collisions
    public Simulation() {
        timeElapsed = 0.0f;
        bodies = new ArrayList<Planet>();
        collisions = new ArrayList<Collision>();
    }

    // REQUIRES: planet object has not previously been added to the simulation
    // MODIFIES: this
    // EFFECTS:
    // adds a planet to the simulation which will be updated with
    // subsequent calls to update
    public void addPlanet(Planet planet) {
        bodies.add(planet);
    }

    //
}
