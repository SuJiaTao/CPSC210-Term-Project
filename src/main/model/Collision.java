package model;

import java.util.ArrayList;

import model.exceptions.NonMatchingClassException;

import java.util.*;

// Represents a collision event between two planets at a given point in time
public class Collision {
    private Planet planet1;
    private Planet planet2;
    private float collisionTime;

    // EFFECTS: initializes collision based on given paramteres
    public Collision(Planet planet1, Planet planet2, float collisionTime) {
        this.planet1 = planet1;
        this.planet2 = planet2;
        this.collisionTime = collisionTime;
    }

    // EFFECTS:
    // returns list of planets involved in the collision
    public List<Planet> getPlanetsInvolved() {
        ArrayList<Planet> planetList = new ArrayList<>();
        planetList.add(planet1);
        planetList.add(planet2);
        return planetList;
    }

    public float getCollisionTime() {
        return collisionTime;
    }

    // EFFECTS: returns whether specified planet was involved in the collision
    public boolean wasPlanetInvolved(Planet toTest) {
        return (toTest == planet1) || (toTest == planet2);
    }

    // EFFECTS: returns whether collisions are identical
    @Override
    public boolean equals(Object otherObject) {
        if (otherObject == null) {
            return false;
        }

        if (!(otherObject instanceof Collision)) {
            throw new NonMatchingClassException();
        }

        Collision other = (Collision) otherObject;
        if (other.getCollisionTime() != collisionTime) {
            return false;
        }
        return other.wasPlanetInvolved(planet1) && other.wasPlanetInvolved(planet2);
    }
}
