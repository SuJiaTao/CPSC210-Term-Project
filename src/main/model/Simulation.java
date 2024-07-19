package model;

import java.util.*;

import model.exceptions.ArgumentOutOfBoundsException;
import model.exceptions.PlanetAlreadyExistsException;
import model.exceptions.PlanetDoesntExistException;

// Represents the current n-body simulation state
public class Simulation {
    public static final float GRAVITATIONAL_CONSTANT = 2.5f;
    public static final float EPSILON = 0.001f;

    private float timeElapsed;
    private List<Planet> planets;
    private List<Collision> collisions;

    // EFFECTS: creates a simulation with no time elapsed and no planets or
    // collisions
    public Simulation() {
        timeElapsed = 0.0f;
        planets = new ArrayList<Planet>();
        collisions = new ArrayList<Collision>();
    }

    public float getTimeElapsed() {
        return timeElapsed;
    }

    public List<Collision> getCollisions() {
        return collisions;
    }

    public List<Planet> getPlanets() {
        return planets;
    }

    // MODIFIES: this
    // EFFECTS:
    // adds a planet to the simulation which will be updated with
    // subsequent calls to update
    public void addPlanet(Planet planet) {
        if (planets.contains(planet)) {
            throw new PlanetAlreadyExistsException();
        }
        planets.add(planet);
    }

    // MODIFIES: this
    // EFFECTS: removes a specific planet from the simulation, if it exists
    public void removePlanet(Planet planet) {
        if (!planets.contains(planet)) {
            throw new PlanetDoesntExistException();
        }
        planets.remove(planet);
    }

    // MODIFIES: this
    // EFFECTS:
    // progresses the simulation forward by deltaTime, including increasing
    // timeElapsed
    public void progressBySeconds(float deltaTime) {
        for (Planet currentPlanet : planets) {
            for (Planet targetPlanet : planets) {
                if (currentPlanet == targetPlanet) {
                    continue;
                }
                applyGravity(targetPlanet, currentPlanet, deltaTime);
                checkPlanetCollision(currentPlanet, targetPlanet);
            }
        }
        // NOTE:
        // there are two passes, if I update the planet positions between checking for
        // collisions, this can result in unwanted behavior
        for (Planet currentPlanet : planets) {
            currentPlanet.updatePosition(deltaTime);
        }
        timeElapsed += deltaTime;
    }

    // MODIFIES: this
    // EFFECTS:
    // checks whether a planet is colliding with another planet, and adds a
    // collision if it doesn't already exist
    public void checkPlanetCollision(Planet currentPlanet, Planet otherPlanet) {
        if (currentPlanet.isCollidingWith(otherPlanet)) {
            Collision col = new Collision(currentPlanet, otherPlanet, timeElapsed);
            if (collisions.contains(col)) {
                return;
            }
            collisions.add(col);
        }
    }

    // MODIFIES: targetPlanet
    // EFFECTS:
    // applies a gravitational force between a specific planet to another
    public void applyGravity(Planet targetPlanet, Planet otherPlanet, float deltaTime) {
        Vector3 displacement = Vector3.sub(otherPlanet.getPosition(), targetPlanet.getPosition());

        float distance = displacement.magnitude();

        float forceMagnitude = calculateGravityMagnitude(targetPlanet.getMass(), otherPlanet.getMass(), distance);
        Vector3 gravityForce = Vector3.multiply(Vector3.normalize(displacement), forceMagnitude);
        targetPlanet.addForce(gravityForce, deltaTime);
    }

    // EFFECTS:
    // given two masses and a distance, calculates the gravitational force magnitude
    // between the two objects
    public float calculateGravityMagnitude(float mass1, float mass2, float dist) {
        if (mass1 <= 0.0f) {
            throw new ArgumentOutOfBoundsException("mass1 must be > 0");
        }
        if (mass2 <= 0.0f) {
            throw new ArgumentOutOfBoundsException("mass2 must be > 0");
        }
        return (GRAVITATIONAL_CONSTANT * mass1 * mass2) / Math.max(EPSILON, (dist * dist));
    }
}
