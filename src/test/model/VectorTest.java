package model;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

public class VectorTest {
    private static final float EPSILON = 0.001f;

    private Vector v1;
    private Vector v2;
    private Vector v3;
    private Vector v4;

    @BeforeEach
    public void init() {
        v1 = new Vector();
        v2 = new Vector(1.0f, 2.0f, -3.0f);
        v3 = new Vector(-1.0f, -2.0f, 3.0f);
        v4 = new Vector(3.0f, 4.0f, 0.0f);
    }

    @Test
    public void testCtor(){
        assertEquals(0.0f, v1)
    }
}
