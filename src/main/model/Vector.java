package model;

// 3 component vector which contains general vector operation functionality
public class Vector {
    private static final float EPSILON = 0.001f;

    private float compX;
    private float compY;
    private float compZ;

    // EFFECTS: creates a vector with all components as zero
    public Vector() {
        compX = 0.0f;
        compY = 0.0f;
        compZ = 0.0f;
    }

    // EFFECTS:
    // creates a vector with the same xyz components as specified in the parameters
    public Vector(float compX, float compY, float compZ) {

    }

    public float getX() {
        return 0.0f;
    }

    public float getY() {
        return 0.0f;
    }

    public float getZ() {
        return 0.0f;
    }

    // EFFECTS:
    // returns a vector where each component is the sum of leftVector and
    // rightVector's corresponding components
    public static Vector add(Vector leftVector, Vector rightVector) {
        return null;
    }

    // EFFECTS:
    // returns a vector where each component is the same as vector's corresponding
    // components multiplied by scalar
    public static Vector multiply(Vector vector, float scalar) {
        return null;
    }

    // EFFECTS:
    // returns the magnitude of the vector
    public float magnitude() {
        return 0.0f;
    }

    // EFFECTS:
    // returns a vector with the same direction as the passed vector but the sum of
    // all components is now 1.0f
    public static Vector normalize(Vector vector) {
        return null;
    }

    // EFFECTS:
    // returns the dot product of the left and right vectors
    public static float dotProduct(Vector leftVector, Vector rightVector) {
        return 0.0f;
    }

    // REQUIRES: other is also of type Vector
    // EFFECTS: returns whether all components of the vector are the same
    @Override
    public boolean equals(Object other) {
        return false;
    }
}
