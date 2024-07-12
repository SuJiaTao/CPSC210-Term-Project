package model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class CollisionTest {
    Planet p1;
    Planet p2;
    Planet p3;
    Collision col1;
    Collision col2;
    Collision col3;
    Collision col4;
    Collision col5;

    @Before
    public void init() {
        p1 = new Planet("plan1", 2.0f);
        p2 = new Planet("plan2", 3.0f);
        p3 = new Planet("plan3", 2.0f);
        col1 = new Collision(p1, p2, 50.0f);
        col2 = new Collision(p2, p1, 50.0f);
        col3 = new Collision(p1, p2, 25.0f);
        col4 = new Collision(p2, p3, 50.0f);
        col5 = new Collision(p2, p3, 1.0f);
    }

    @Test
    public void testCtor() {
        assertTrue(col1.getPlanetsInvolved().contains(p1));
        assertTrue(col1.getPlanetsInvolved().contains(p2));
        assertEquals(col1.getCollisionTime(), 50.0f);
    }

    @Test
    public void testPlanetInvolvement() {
        assertTrue(col1.wasPlanetInvolved(p1));
        assertTrue(col1.wasPlanetInvolved(p2));
        assertFalse(col1.wasPlanetInvolved(p3));
        assertFalse(col4.wasPlanetInvolved(p1));
    }

    @Test
    public void testEquals() {
        assertEquals(col1, col2);
        assertEquals(col2, col1);
    }

    @Test
    public void testNotEquals() {
        assertNotEquals(col1, col3);
        assertNotEquals(col3, col1);

        assertNotEquals(col2, col3);
        assertNotEquals(col3, col2);

        assertNotEquals(col1, col4);
        assertNotEquals(col4, col1);

        assertNotEquals(col4, col5);
        assertNotEquals(col5, col4);
    }
}
