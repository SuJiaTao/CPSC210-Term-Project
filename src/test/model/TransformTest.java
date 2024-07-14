package model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class TransformTest {
    private static final float EPSILON = 0.001f;

    private Transform m;
    private Vector3 v1;
    private Vector3 v2;

    @Before
    public void test() {
        m = new Transform();
        v1 = new Vector3(1.0f, 2.0f, 3.0f);
        v2 = new Vector3();
    }

    @Test
    public void testCtor() {
        float[][] comps = m.getComponents();
        assertEquals(1.0f, comps[0][0], EPSILON);
        assertEquals(1.0f, comps[1][1], EPSILON);
        assertEquals(1.0f, comps[2][2], EPSILON);
        assertEquals(1.0f, comps[3][3], EPSILON);
    }

    @Test
    public void testTranslation() {
        m = Transform.translationMatrix(new Vector3(3.0f, 4.0f, 5.0f));
        v2 = Transform.multiply(m, v1);
        assertEquals(Vector3.add(v1, new Vector3(3.0f, 4.0f, 5.0f)), v2);

        m = Transform.translationMatrix(new Vector3(-3.0f, -4.0f, -5.0f));
        v2 = Transform.multiply(m, v1);
        assertEquals(Vector3.sub(v1, new Vector3(3.0f, 4.0f, 5.0f)), v2);
    }

    @Test
    public void testTranslationIdentity() {
        m = Transform.translationMatrix(new Vector3(0.0f, 0.0f, 0.0f));
        v2 = Transform.multiply(m, v1);
        assertEquals(v1, v2);
    }

    @Test
    public void testScale() {
        m = Transform.scaleMatrix(new Vector3(3.0f, 4.0f, 5.0f));
        v2 = Transform.multiply(m, v1);
        assertEquals(v1.getX() * 3.0f, v2.getX(), EPSILON);
        assertEquals(v1.getY() * 4.0f, v2.getY(), EPSILON);
        assertEquals(v1.getZ() * 5.0f, v2.getZ(), EPSILON);
    }

    @Test
    public void testScaleZero() {
        m = Transform.scaleMatrix(new Vector3(0.0f, 0.0f, 0.0f));
        v2 = Transform.multiply(m, v1);
        assertEquals(new Vector3(), v2);
    }

    @Test
    public void testScaleIdentity() {
        m = Transform.scaleMatrix(new Vector3(1.0f, 1.0f, 1.0f));
        v2 = Transform.multiply(m, v1);
        assertEquals(v1, v2);
    }

    @Test
    public void testRotationXIdentity() {
        m = Transform.rotationMatrixX(0.0f);
        v2 = Transform.multiply(m, v1);
        assertEquals(v1, v2);
    }

    @Test
    public void testRotationX() {
        float degrees = 0.0f;
        for (int i = 0; i < 90; i++) {
            degrees += 4.0f;
            m = Transform.rotationMatrixX(degrees);
            v1 = Transform.multiply(m, new Vector3(0.0f, 0.0f, 1.0f));
            assertEquals(Math.cos(Math.toRadians(degrees)), v1.getZ(), EPSILON);
            assertEquals(Math.sin(Math.toRadians(degrees)), v1.getY(), EPSILON);
        }
    }

    @Test
    public void testRotationYIdentity() {
        m = Transform.rotationMatrixY(0.0f);
        v2 = Transform.multiply(m, v1);
        assertEquals(v1, v2);
    }

    @Test
    public void testRotationY() {
        float degrees = 0.0f;
        for (int i = 0; i < 90; i++) {
            degrees += 4.0f;
            m = Transform.rotationMatrixY(degrees);
            v1 = Transform.multiply(m, new Vector3(1.0f, 0.0f, 0.0f));
            assertEquals(Math.cos(Math.toRadians(degrees)), v1.getX(), EPSILON);
            assertEquals(Math.sin(Math.toRadians(degrees)), v1.getZ(), EPSILON);
        }
    }

    @Test
    public void testRotationZIdentity() {
        m = Transform.rotationMatrixZ(0.0f);
        v2 = Transform.multiply(m, v1);
        assertEquals(v1, v2);
    }

    @Test
    public void testRotationZ() {
        float degrees = 0.0f;
        for (int i = 0; i < 90; i++) {
            degrees += 4.0f;
            m = Transform.rotationMatrixZ(degrees);
            v1 = Transform.multiply(m, new Vector3(1.0f, 0.0f, 0.0f));
            assertEquals(Math.sin(Math.toRadians(degrees)), v1.getY(), EPSILON);
            assertEquals(Math.cos(Math.toRadians(degrees)), v1.getX(), EPSILON);
        }
    }
}
