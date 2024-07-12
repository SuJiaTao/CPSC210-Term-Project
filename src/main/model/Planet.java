package model;

// Represents a Planet within the simulation in 3D-space
// The planet has a name, position, velocity and radius
public class Planet {
    private static final float SPHERE_VOLUME_COEFFICIENT = (4.0f / 3.0f) * 3.14159265f;
    private static final float EPSILON = 0.001f;

    private String name;
    private Vector3 position;
    private Vector3 velocity;
    private float radius;

    // REQUIRES: radius > 0
    // EFFECTS:
    // creates a planet with a name and radius, the position and velocity are both
    // zero
    public Planet(String name, float radius) {
        this(name, new Vector3(), new Vector3(), radius);
    }

    // REQUIRES: radius > 0
    // EFFECTS:
    // creates a planet with the specified, name, starting position, starting
    // velocity and radius
    public Planet(String name, Vector3 position, Vector3 velocity, float radius) {
        this.name = name;
        this.position = position;
        this.velocity = velocity;
        this.radius = radius;
    }

    public String getName() {
        return name;
    }

    public Vector3 getPosition() {
        return position;
    }

    public Vector3 getVelocity() {
        return velocity;
    }

    public float getRadius() {
        return radius;
    }

    // EFFECTS:
    // returns the mass of the planet as if the density is uniformly 1.0
    public float getMass() {
        return SPHERE_VOLUME_COEFFICIENT * radius * radius * radius;
    }

    // REQUIRES: deltaTime > 0
    // MODIFIES: this
    // EFFECTS: updates the planet's position based on it's velocity
    public void updatePosition(float deltaTime) {
        position = Vector3.add(position, Vector3.multiply(velocity, deltaTime));
    }

    // REQUIRES: deltaTime > 0
    // MODIFIES: this
    // EFFECTS: adds a force to the planet, changing it's velocity
    public void addForce(Vector3 forceVector, float deltaTime) {
        float planetMass = getMass();
        Vector3 acceleration = Vector3.multiply(forceVector, 1.0f / planetMass);
        acceleration = Vector3.multiply(acceleration, deltaTime);
        velocity = Vector3.add(velocity, acceleration);
    }

    // EFFECTS: returns whether this planet is colliding with the specified planet
    public boolean isCollidingWith(Planet other) {
        Vector3 displaceMent = Vector3.add(position, Vector3.multiply(other.getPosition(), -1.0f));
        return displaceMent.magnitude() <= (radius + other.getRadius() + EPSILON);
    }
}
