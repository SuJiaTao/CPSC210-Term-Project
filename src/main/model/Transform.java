package model;

// Represents a 4x4 affine transform matrix (yayy computer graphics!)
public class Transform {
    private static final float DEGREE_TO_RAD = 0.0174533f;
    private static final int ROW_COUNT = 4;
    private static final int COL_COUNT = 4;
    private float[][] components;

    // EFFECTS: creates an identity matrix
    public Transform() {
        components = new float[ROW_COUNT][COL_COUNT];
        components[0][0] = 1.0f;
        components[1][1] = 1.0f;
        components[2][2] = 1.0f;
        components[3][3] = 1.0f;
    }

    public float[][] getComponents() {
        return components;
    }

    // EFFECTS: creates a translation matrix
    public static Transform translation(Vector3 trl) {
        Transform matrix = new Transform();
        matrix.components[3][0] = trl.getX();
        matrix.components[3][1] = trl.getY();
        matrix.components[3][2] = trl.getZ();
        return matrix;
    }

    // EFFECTS: creates a scale matrix
    public static Transform scale(Vector3 scl) {
        Transform matrix = new Transform();
        matrix.components[0][0] = scl.getX();
        matrix.components[1][1] = scl.getY();
        matrix.components[2][2] = scl.getZ();
        return matrix;
    }

    // EFFECTS: creates a rotation matrix about the x axis
    public static Transform rotationX(float rotDegrees) {
        Transform matrix = new Transform();

        float cosDeg = cosDegrees(rotDegrees);
        float sinDeg = sinDegrees(rotDegrees);
        matrix.components[1][1] = cosDeg;
        matrix.components[2][1] = sinDeg;
        matrix.components[1][2] = -sinDeg;
        matrix.components[2][2] = cosDeg;
        return matrix;
    }

    // EFFECTS: creates a rotation matrix about the y axis
    public static Transform rotationY(float rotDegrees) {
        Transform matrix = new Transform();

        float cosDeg = cosDegrees(rotDegrees);
        float sinDeg = sinDegrees(rotDegrees);
        matrix.components[0][0] = cosDeg;
        matrix.components[0][2] = sinDeg;
        matrix.components[2][0] = -sinDeg;
        matrix.components[2][2] = cosDeg;
        return matrix;
    }

    // EFFECTS: creates a rotation matrix about the z axis
    public static Transform rotationZ(float rotDegrees) {
        Transform matrix = new Transform();

        float cosDeg = cosDegrees(rotDegrees);
        float sinDeg = sinDegrees(rotDegrees);
        matrix.components[0][0] = cosDeg;
        matrix.components[0][1] = sinDeg;
        matrix.components[1][0] = -sinDeg;
        matrix.components[1][1] = cosDeg;
        return matrix;
    }

    // EFFECTS: creates a 3D rotation matrix
    public static Transform rotation(Vector3 rot) {
        Transform matrix = new Transform();

        Transform rotX = rotationX(rot.getX());
        Transform rotY = rotationY(rot.getY());
        Transform rotZ = rotationZ(rot.getZ());

        matrix = multiply(matrix, rotX);
        matrix = multiply(matrix, rotY);
        matrix = multiply(matrix, rotZ);

        return matrix;
    }

    // EFFECTS: creates a TRS matrix
    public static Transform transform(Vector3 trl, Vector3 rot, Vector3 scl) {
        Transform tformMatrix = new Transform();
        Transform matScale = scale(scl);
        Transform matRot = rotation(rot);
        Transform matTrans = translation(trl);

        tformMatrix = multiply(tformMatrix, matScale);
        tformMatrix = multiply(tformMatrix, matRot);
        tformMatrix = multiply(tformMatrix, matTrans);

        return tformMatrix;
    }

    // EFFECTS: returns the multiplication of two matricies
    public static Transform multiply(Transform leftMatrix, Transform rightMatrix) {
        Transform matrix = new Transform();
        // NOTE:
        // hehehe I havent actually taken MATH223 yet so I have kinda no idea what I'm
        // doing... I'm simply referencing the wikipedia article on matrix
        // multiplication to get this right
        // reference:
        // https://en.wikipedia.org/wiki/Matrix_multiplication
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COL_COUNT; j++) {
                matrix.components[j][i] = 0.0f;
                matrix.components[j][i] += rightMatrix.components[0][i] * leftMatrix.components[j][0];
                matrix.components[j][i] += rightMatrix.components[1][i] * leftMatrix.components[j][1];
                matrix.components[j][i] += rightMatrix.components[2][i] * leftMatrix.components[j][2];
                matrix.components[j][i] += rightMatrix.components[3][i] * leftMatrix.components[j][3];
            }
        }
        return matrix;
    }

    // EFFECTS: returns the multiplication of a matricie and a vector
    public static Vector3 multiply(Transform matrix, Vector3 vector) {
        float[] vector4 = new float[4];
        for (int i = 0; i < 4; i++) {
            vector4[i] += matrix.components[0][i] * vector.getX();
            vector4[i] += matrix.components[1][i] * vector.getY();
            vector4[i] += matrix.components[2][i] * vector.getZ();
            vector4[i] += matrix.components[3][i] * 1.0f;
        }
        return new Vector3(vector4[0], vector4[1], vector4[2]);
    }

    // EFFECTS: extracts a scale vector from the transform
    public static Vector3 extractScale(Transform matrix) {
        float[][] comp = matrix.components;
        float scaleX = new Vector3(comp[0][0], comp[0][1], comp[0][2]).magnitude();
        float scaleY = new Vector3(comp[1][0], comp[1][1], comp[1][2]).magnitude();
        float scaleZ = new Vector3(comp[2][0], comp[2][1], comp[2][2]).magnitude();
        return new Vector3(scaleX, scaleY, scaleZ);
    }

    // EFFECTS: extracts a translation vector from the transform
    public static Vector3 extractTranslation(Transform matrix) {
        float[][] comp = matrix.components;
        return new Vector3(comp[3][0], comp[3][1], comp[3][2]);
    }

    // EFFECTS: returns sinx in degrees
    private static float sinDegrees(float x) {
        return (float) Math.sin(x * DEGREE_TO_RAD);
    }

    // EFFECTS: returns cosx in degrees
    private static float cosDegrees(float x) {
        return (float) Math.cos(x * DEGREE_TO_RAD);
    }
}
