package model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class TransformTest {
    private static final float EPSILON = 0.001f;

    private Transform m0;
    private Vector3 v1;
    private Vector3 v2;

    @Before
    public void test() {
        m0 = new Transform();
        v1 = new Vector3(1.0f, 2.0f, 3.0f);
        v2 = new Vector3();
    }

    @Test
    public void testCtor() {
        float[][] comps = m0.getComponents();
        assertEquals(1.0f, comps[0][0], EPSILON);
        assertEquals(1.0f, comps[1][1], EPSILON);
        assertEquals(1.0f, comps[2][2], EPSILON);
        assertEquals(1.0f, comps[3][3], EPSILON);
    }

    @Test
    public void testTranslation() {
        m0 = Transform.translationMatrix(new Vector3(3.0f, 4.0f, 5.0f));
        v2 = Transform.multiply(m0, v1);
        assertEquals(Vector3.add(v1, new Vector3(3.0f, 4.0f, 5.0f)), v2);

        m0 = Transform.translationMatrix(new Vector3(-3.0f, -4.0f, -5.0f));
        v2 = Transform.multiply(m0, v1);
        assertEquals(Vector3.sub(v1, new Vector3(3.0f, 4.0f, 5.0f)), v2);
    }

    @Test
    public void testTranslationIdentity() {
        m0 = Transform.translationMatrix(new Vector3(0.0f, 0.0f, 0.0f));
        v2 = Transform.multiply(m0, v1);
        assertEquals(v1, v2);
    }

    @Test
    public void testScale() {
        m0 = Transform.scaleMatrix(new Vector3(3.0f, 4.0f, 5.0f));
        v2 = Transform.multiply(m0, v1);
        assertEquals(v1.getX() * 3.0f, v2.getX(), EPSILON);
        assertEquals(v1.getY() * 4.0f, v2.getY(), EPSILON);
        assertEquals(v1.getZ() * 5.0f, v2.getZ(), EPSILON);
    }

    @Test
    public void testScaleZero() {
        m0 = Transform.scaleMatrix(new Vector3(0.0f, 0.0f, 0.0f));
        v2 = Transform.multiply(m0, v1);
        assertEquals(new Vector3(), v2);
    }

    @Test
    public void testScaleIdentity() {
        m0 = Transform.scaleMatrix(new Vector3(1.0f, 1.0f, 1.0f));
        v2 = Transform.multiply(m0, v1);
        assertEquals(v1, v2);
    }

    @Test
    public void testRotationXIdentity() {
        m0 = Transform.rotationMatrixX(0.0f);
        v2 = Transform.multiply(m0, v1);
        assertEquals(v1, v2);
    }

    @Test
    public void testRotationX() {
        float degrees = 0.0f;
        for (int i = 0; i < 90; i++) {
            degrees += 4.0f;
            m0 = Transform.rotationMatrixX(degrees);
            v1 = Transform.multiply(m0, new Vector3(0.0f, 0.0f, 1.0f));
            assertEquals(Math.cos(Math.toRadians(degrees)), v1.getZ(), EPSILON);
            assertEquals(Math.sin(Math.toRadians(degrees)), v1.getY(), EPSILON);
        }
    }

    @Test
    public void testRotationYIdentity() {
        m0 = Transform.rotationMatrixY(0.0f);
        v2 = Transform.multiply(m0, v1);
        assertEquals(v1, v2);
    }

    @Test
    public void testRotationY() {
        float degrees = 0.0f;
        for (int i = 0; i < 90; i++) {
            degrees += 4.0f;
            m0 = Transform.rotationMatrixY(degrees);
            v1 = Transform.multiply(m0, new Vector3(1.0f, 0.0f, 0.0f));
            assertEquals(Math.cos(Math.toRadians(degrees)), v1.getX(), EPSILON);
            assertEquals(Math.sin(Math.toRadians(degrees)), v1.getZ(), EPSILON);
        }
    }

    @Test
    public void testRotationZIdentity() {
        m0 = Transform.rotationMatrixZ(0.0f);
        v2 = Transform.multiply(m0, v1);
        assertEquals(v1, v2);
    }

    @Test
    public void testRotationZ() {
        float degrees = 0.0f;
        for (int i = 0; i < 90; i++) {
            degrees += 4.0f;
            m0 = Transform.rotationMatrixZ(degrees);
            v1 = Transform.multiply(m0, new Vector3(1.0f, 0.0f, 0.0f));
            assertEquals(Math.sin(Math.toRadians(degrees)), v1.getY(), EPSILON);
            assertEquals(Math.cos(Math.toRadians(degrees)), v1.getX(), EPSILON);
        }
    }

    @Test
    public void testMatrixToMatrixMultiplyIdentity() {
        Transform m1 = Transform.rotationMatrixZ(90.0f);
        Transform m2 = Transform.multiply(m1, new Transform());
        Transform m3 = Transform.multiply(new Transform(), m1);
        v1 = Transform.multiply(m1, new Vector3(1.0f, 0.0f, 0.0f));
        v2 = Transform.multiply(m2, new Vector3(1.0f, 0.0f, 0.0f));
        assertEquals(v1, v2);
        v2 = Transform.multiply(m3, new Vector3(1.0f, 0.0f, 0.0f));
        assertEquals(v1, v2);
    }

    @Test
    public void testMatrixToMatrixMultiply() {
        Transform m1;
        Transform m2;

        m1 = Transform.rotationMatrixZ(90.0f);
        m2 = Transform.multiply(m1, Transform.translationMatrix(new Vector3(1.0f, 0.0f, 0.0f)));
        v1 = Transform.multiply(m2, new Vector3(1.0f, 0.0f, 0.0f));
        assertEquals(new Vector3(1.0f, 1.0f, 0.0f), v1);

        m1 = Transform.scaleMatrix(new Vector3(5.0f, 5.0f, 5.0f));
        m2 = Transform.multiply(m1, Transform.rotationMatrixZ(90.0f));
        v1 = Transform.multiply(m2, new Vector3(1.0f, 0.0f, 0.0f));
        assertEquals(new Vector3(0.0f, 5.0f, 0.0f), v1);

        m1 = Transform.scaleMatrix(new Vector3(5.0f, 5.0f, 5.0f));
        m2 = Transform.multiply(m1, Transform.translationMatrix(new Vector3(2.0f, 3.0f, 4.0f)));
        v1 = Transform.multiply(m2, new Vector3(1.0f, 0.0f, 0.0f));
        assertEquals(new Vector3(7.0f, 3.0f, 4.0f), v1);
    }

    @Test
    public void testMatrixRotationAllAxis() {
        m0 = Transform.rotationMatrix(new Vector3(90.0f, 0.0f, 0.0f));
        v1 = Transform.multiply(m0, new Vector3(0f, 0f, 1f));
        assertEquals(new Vector3(0f, 1f, 0f), v1);

        m0 = Transform.rotationMatrix(new Vector3(0.0f, 90.0f, 0.0f));
        v1 = Transform.multiply(m0, new Vector3(1f, 0f, 0f));
        assertEquals(new Vector3(0f, 0f, 1f), v1);

        m0 = Transform.rotationMatrix(new Vector3(0.0f, 0.0f, 90.0f));
        v1 = Transform.multiply(m0, new Vector3(1f, 0f, 0f));
        assertEquals(new Vector3(0f, 1f, 0f), v1);

        // TODO: maybe write more tests for rotations... technically I havent tested all
        // permutations of rotations but I could also be here all day doing that :(
    }

    @Test
    public void testMatrixTransform() {
        m0 = Transform.transformMatrix(new Vector3(1f, 0f, 0f), new Vector3(0f, 0f, 90f), new Vector3(2f, 2f, 2f));
        v1 = Transform.multiply(m0, new Vector3(1.0f, 0.0f, 0.0f));
        assertEquals(new Vector3(1f, 2f, 0f), v1);

        m0 = Transform.transformMatrix(new Vector3(3f, 5f, -9f), new Vector3(0f, 180f, 0f), new Vector3(5f, 5f, 5f));
        v1 = Transform.multiply(m0, new Vector3(1.0f, 0.0f, 0.0f));
        assertEquals(new Vector3(-2f, 5f, -9f), v1);

        m0 = Transform.transformMatrix(new Vector3(0f, 0f, 0f), new Vector3(0f, 0f, 0f), new Vector3(1f, 1f, 1f));
        v1 = Transform.multiply(m0, new Vector3(3.0f, 4.0f, 5.0f));
        assertEquals(new Vector3(3f, 4f, 5f), v1);
    }
}
