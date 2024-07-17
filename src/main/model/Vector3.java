package model;

import exceptions.NonMatchingClassException;

// Represents a 3-component vector which supports a common set of vector operations
public class Vector3 {
    private static final float EPSILON = 0.001f;

    private float compX;
    private float compY;
    private float compZ;

    // EFFECTS:
    // creates a vector all components as 0
    public Vector3() {
        this(0.0f, 0.0f, 0.0f);
    }

    // EFFECTS:
    // creates a vector with the same xyz components as specified in the parameters
    public Vector3(float compX, float compY, float compZ) {
        this.compX = compX;
        this.compY = compY;
        this.compZ = compZ;
    }

    public float getX() {
        return compX;
    }

    public float getY() {
        return compY;
    }

    public float getZ() {
        return compZ;
    }

    // EFFECTS:
    // returns a vector where each component is the sum of leftVector and
    // rightVector's corresponding components
    public static Vector3 add(Vector3 leftVector, Vector3 rightVector) {
        float x = leftVector.getX() + rightVector.getX();
        float y = leftVector.getY() + rightVector.getY();
        float z = leftVector.getZ() + rightVector.getZ();
        return new Vector3(x, y, z);
    }

    // EFFECTS:
    // returns a vector where each component is the difference of leftVector and
    // rightVector's corresponding componenets
    public static Vector3 sub(Vector3 lefVector3, Vector3 rightVector) {
        return add(lefVector3, multiply(rightVector, -1.0f));
    }

    // EFFECTS:
    // returns a vector where each component is the same as vector's corresponding
    // components multiplied by scalar
    public static Vector3 multiply(Vector3 vector, float scalar) {
        float x = vector.getX() * scalar;
        float y = vector.getY() * scalar;
        float z = vector.getZ() * scalar;
        return new Vector3(x, y, z);
    }

    // EFFECTS:
    // returns the magnitude of the vector
    public float magnitude() {
        return (float) Math.sqrt(compX * compX + compY * compY + compZ * compZ);
    }

    // EFFECTS:
    // returns a vector with the same direction as the passed vector but the sum of
    // all components is now 1.0f
    public static Vector3 normalize(Vector3 vector) {
        if (Math.abs(vector.magnitude()) < EPSILON) {
            return new Vector3(0.0f, 0.0f, 0.0f);
        }
        return Vector3.multiply(vector, 1.0f / vector.magnitude());
    }

    // EFFECTS:
    // returns the dot product of the left and right vectors
    public static float dotProduct(Vector3 leftVector, Vector3 rightVector) {
        float productX = leftVector.getX() * rightVector.getX();
        float productY = leftVector.getY() * rightVector.getY();
        float productZ = leftVector.getZ() * rightVector.getZ();
        return productX + productY + productZ;
    }

    // EFFECTS: returns whether all components of the vector are the same
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Vector3)) {
            throw new NonMatchingClassException();
        }
        Vector3 otherVector = (Vector3) other;
        float dx = Math.abs(this.getX() - otherVector.getX());
        float dy = Math.abs(this.getY() - otherVector.getY());
        float dz = Math.abs(this.getZ() - otherVector.getZ());
        return (dx < EPSILON) && (dy < EPSILON) && (dz < EPSILON);
    }

    // EFFECTS: produces a string of the form (x, y z)
    @Override
    public String toString() {
        return String.format("(%.2f %.2f %.2f)", compX, compY, compZ);
    }
}
