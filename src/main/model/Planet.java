package model;

import model.exceptions.ArgumentOutOfBoundsException;

// Represents a Planet within the simulation in 3D-space
// The planet has a name, position, velocity and radius
public class Planet {
    private static final float SPHERE_VOLUME_COEFFICIENT = (4.0f / 3.0f) * 3.14159265f;
    private static final float EPSILON = 0.001f;

    private String name;
    private Vector3 position;
    private Vector3 velocity;
    private float radius;

    // EFFECTS:
    // creates a planet with a name and radius, the position and velocity are both
    // zero
    public Planet(String name, float radius) {
        this(name, new Vector3(), new Vector3(), radius);
    }

    // EFFECTS:
    // creates a planet with the specified, name, starting position, starting
    // velocity and radius
    public Planet(String name, Vector3 position, Vector3 velocity, float radius) {
        this.name = name;
        this.position = position;
        this.velocity = velocity;
        if (radius <= 0.0f) {
            throw new ArgumentOutOfBoundsException("radius must be > 0");
        }
        this.radius = radius;
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        name = newName;
    }

    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(Vector3 newPosition) {
        position = newPosition;
    }

    public Vector3 getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector3 newVelocity) {
        velocity = newVelocity;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float newRadius) {
        radius = newRadius;
    }

    // EFFECTS:
    // returns the mass of the planet as if the density is uniformly 1.0
    public float getMass() {
        return SPHERE_VOLUME_COEFFICIENT * radius * radius * radius;
    }

    // MODIFIES: this
    // EFFECTS: updates the planet's position based on it's velocity
    public void updatePosition(float deltaTime) {
        if (deltaTime < 0.0f) {
            throw new ArgumentOutOfBoundsException("deltaTime must be positive");
        }
        position = Vector3.add(position, Vector3.multiply(velocity, deltaTime));
    }

    // MODIFIES: this
    // EFFECTS: adds a force to the planet, changing it's velocity
    public void addForce(Vector3 forceVector, float deltaTime) {
        if (deltaTime < 0.0f) {
            throw new ArgumentOutOfBoundsException("deltaTime must be positive");
        }
        float planetMass = getMass();
        Vector3 acceleration = Vector3.multiply(forceVector, 1.0f / planetMass);
        acceleration = Vector3.multiply(acceleration, deltaTime);
        velocity = Vector3.add(velocity, acceleration);
    }

    // EFFECTS: returns whether this planet is colliding with the specified planet
    public boolean isCollidingWith(Planet other) {
        Vector3 displaceMent = Vector3.sub(position, other.getPosition());
        return displaceMent.magnitude() <= (radius + other.getRadius() + EPSILON);
    }
}
