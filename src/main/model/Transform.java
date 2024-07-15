package model;

// Represents a 4x4 affine transform matrix (yayy computer graphics!)
public class Transform {
    private static final float DEGREE_TO_RAD = 0.0174533f;
    private static final int ROW_COUNT = 4;
    private static final int COL_COUNT = 4;
    private float components[][];

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
    public static Transform translationMatrix(Vector3 trl) {
        Transform tMatrix = new Transform();
        tMatrix.components[3][0] = trl.getX();
        tMatrix.components[3][1] = trl.getY();
        tMatrix.components[3][2] = trl.getZ();
        return tMatrix;
    }

    // EFFECTS: creates a scale matrix
    public static Transform scaleMatrix(Vector3 scl) {
        Transform sMatrix = new Transform();
        sMatrix.components[0][0] = scl.getX();
        sMatrix.components[1][1] = scl.getY();
        sMatrix.components[2][2] = scl.getZ();
        return sMatrix;
    }

    // EFFECTS: creates a rotation matrix about the x axis
    public static Transform rotationMatrixX(float rotDegrees) {
        Transform rMatrix = new Transform();

        float cosDeg = cosDegrees(rotDegrees);
        float sinDeg = sinDegrees(rotDegrees);
        rMatrix.components[1][1] = cosDeg;
        rMatrix.components[2][1] = sinDeg;
        rMatrix.components[1][2] = -sinDeg;
        rMatrix.components[2][2] = cosDeg;
        return rMatrix;
    }

    // EFFECTS: creates a rotation matrix about the y axis
    public static Transform rotationMatrixY(float rotDegrees) {
        Transform rMatrix = new Transform();

        float cosDeg = cosDegrees(rotDegrees);
        float sinDeg = sinDegrees(rotDegrees);
        rMatrix.components[0][0] = cosDeg;
        rMatrix.components[0][2] = sinDeg;
        rMatrix.components[2][0] = -sinDeg;
        rMatrix.components[2][2] = cosDeg;
        return rMatrix;
    }

    // EFFECTS: creates a rotation matrix about the z axis
    public static Transform rotationMatrixZ(float rotDegrees) {
        Transform rMatrix = new Transform();

        float cosDeg = cosDegrees(rotDegrees);
        float sinDeg = sinDegrees(rotDegrees);
        rMatrix.components[0][0] = cosDeg;
        rMatrix.components[0][1] = sinDeg;
        rMatrix.components[1][0] = -sinDeg;
        rMatrix.components[1][1] = cosDeg;
        return rMatrix;
    }

    // EFFECTS: creates a 3D rotation matrix
    public static Transform rotationMatrix(Vector3 rot) {
        Transform rMatrix = new Transform();

        Transform rotX = rotationMatrixX(rot.getX());
        Transform rotY = rotationMatrixY(rot.getY());
        Transform rotZ = rotationMatrixZ(rot.getZ());

        rMatrix = multiply(rMatrix, rotX);
        rMatrix = multiply(rMatrix, rotY);
        rMatrix = multiply(rMatrix, rotZ);

        return rMatrix;
    }

    // EFFECTS: creates a TRS matrix
    public static Transform transformMatrix(Vector3 trl, Vector3 rot, Vector3 scl) {
        Transform tformMatrix = new Transform();
        Transform sMatrix = scaleMatrix(scl);
        Transform rMatrix = rotationMatrix(rot);
        Transform tMatrix = translationMatrix(trl);

        tformMatrix = multiply(tformMatrix, sMatrix);
        tformMatrix = multiply(tformMatrix, rMatrix);
        tformMatrix = multiply(tformMatrix, tMatrix);

        return tformMatrix;
    }

    // EFFECTS: returns the multiplication of two matricies
    public static Transform multiply(Transform leftMatrix, Transform rightMatrix) {
        Transform mMatrix = new Transform();
        // NOTE:
        // hehehe I havent actually taken MATH223 yet so I have kinda no idea what I'm
        // doing... I'm simply referencing the wikipedia article on matrix
        // multiplication to get this right
        // reference:
        // https://en.wikipedia.org/wiki/Matrix_multiplication
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COL_COUNT; j++) {
                mMatrix.components[j][i] = 0.0f;
                mMatrix.components[j][i] += rightMatrix.components[0][i] * leftMatrix.components[j][0];
                mMatrix.components[j][i] += rightMatrix.components[1][i] * leftMatrix.components[j][1];
                mMatrix.components[j][i] += rightMatrix.components[2][i] * leftMatrix.components[j][2];
                mMatrix.components[j][i] += rightMatrix.components[3][i] * leftMatrix.components[j][3];
            }
        }
        return mMatrix;
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
        return null;
    }

    // EFFECTS: extracts a translation vector from the transform
    public static Vector3 extractTranslation(Transform matrix) {
        return null;
    }

    // EFFECTS: extracts a rotation vector from the transform
    public static Vector3 extractRotation(Transform matrix) {
        return null;
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
