package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class Vector3Test {
    private static final float EPSILON = 0.001f;

    private Vector3 v1;
    private Vector3 v2;
    private Vector3 v3;
    private Vector3 v4;

    @Before
    public void init() {
        v1 = new Vector3();
        v2 = new Vector3(1.0f, 2.0f, -3.0f);
        v3 = new Vector3(-1.0f, -2.0f, 3.0f);
        v4 = new Vector3(3.0f, 4.0f, 0.0f);
    }

    @Test
    public void testCtorNoParams() {
        assertEquals(0.0f, v1.getX(), EPSILON);
        assertEquals(0.0f, v1.getY(), EPSILON);
        assertEquals(0.0f, v1.getZ(), EPSILON);
    }

    @Test
    public void testCtorWithParams() {
        assertEquals(1.0f, v2.getX(), EPSILON);
        assertEquals(2.0f, v2.getY(), EPSILON);
        assertEquals(-3.0f, v2.getZ(), EPSILON);
    }

    @Test
    public void testEqualsOverload() {
        assertEquals(new Vector3(0.0f, 0.0f, 0.0f), v1);
        assertEquals(new Vector3(1.0f, 2.0f, -3.0f), v2);
    }

    @Test
    public void testAdditionSymmetry() {
        assertEquals(v1, Vector3.add(v2, v3));
        assertEquals(v1, Vector3.add(v3, v2));
    }

    @Test
    public void testMultiplication() {
        assertEquals(v1, Vector3.multiply(v1, 5.0f));
        assertEquals(v1, Vector3.multiply(v1, -5.0f));
        assertEquals(v2, Vector3.multiply(v3, -1.0f));
        assertEquals(v3, Vector3.multiply(v2, -1.0f));
    }

    @Test
    public void testMultiplicationZero() {
        assertEquals(v1, Vector3.multiply(v2, 0.0f));
        assertEquals(v1, Vector3.multiply(v3, 0.0f));
    }

    @Test
    public void testMultiplicationIdentity() {
        assertEquals(v1, Vector3.multiply(v1, 1.0f));
        assertEquals(v2, Vector3.multiply(v2, 1.0f));
        assertEquals(v3, Vector3.multiply(v3, 1.0f));
    }

    @Test
    public void testMagnitude() {
        assertEquals(0.0f, v1.magnitude(), EPSILON);
        assertEquals(5.0f, v4.magnitude(), EPSILON);
        assertEquals((float) Math.sqrt(1.0f + 4.0f + 9.0f), v2.magnitude(), EPSILON);
        assertEquals(v3.magnitude(), v2.magnitude(), EPSILON);
    }

    @Test
    public void testNormalize() {
        assertEquals(v1, Vector3.normalize(v1));
        assertEquals(new Vector3(1.0f, 0.0f, 0.0f), Vector3.normalize(new Vector3(5.0f, 0.0f, 0.0f)));
        assertEquals(Vector3.multiply(v2, 1.0f / v2.magnitude()), Vector3.normalize(v2));
        assertEquals(Vector3.multiply(v4, 1.0f / v4.magnitude()), Vector3.normalize(v4));
    }

    @Test
    public void testDotProduct() {
        assertEquals(0.0f, Vector3.dotProduct(v1, v1));
        assertEquals(1.0f + 4.0f + 9.0f, Vector3.dotProduct(v2, v2));
        assertEquals(-14.0f, Vector3.dotProduct(v2, v3));
        assertEquals(-14.0f, Vector3.dotProduct(v3, v2));
        assertEquals(0.0f, Vector3.dotProduct(new Vector3(1.0f, 0.0f, 0.0f), new Vector3(0.0f, 1.0f, 0.0f)));
        assertEquals(32.0f, Vector3.dotProduct(new Vector3(1.0f, 2.0f, 3.0f), new Vector3(4.0f, 5.0f, 6.0f)));
    }
}