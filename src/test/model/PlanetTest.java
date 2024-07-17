package model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.Test;

import exceptions.ArgumentOutOfBoundsException;

public class PlanetTest {
    private static final float EPSILON = 0.001f;
    private Planet plnt;

    @Test
    public void testCtorNameAndRadius() {
        plnt = new Planet("testName", 5.0f);
        assertEquals("testName", plnt.getName());
        assertEquals(5.0f, plnt.getRadius());
    }

    @Test
    public void testCtorThrow() {
        try {
            plnt = new Planet("failure", -5.0f);
        } catch (ArgumentOutOfBoundsException excep) {
            return;
        }
        fail("failed to throw ArgumentOutOfBoundsException");
    }

    @Test
    public void testCtorThrowBoundary() {
        try {
            plnt = new Planet("failure", 0.0f);
        } catch (ArgumentOutOfBoundsException excep) {
            return;
        }
        fail("failed to throw ArgumentOutOfBoundsException");
    }

    @Test
    public void testCtorFull() {
        plnt = new Planet("B", new Vector3(1.0f, 0.0f, 0.0f), new Vector3(0.0f, 0.0f, 0.0f), 5.0f);
        assertEquals("B", plnt.getName());
        assertEquals(new Vector3(1.0f, 0.0f, 0.0f), plnt.getPosition());
        assertEquals(new Vector3(0.0f, 0.0f, 0.0f), plnt.getVelocity());
        assertEquals(5.0f, plnt.getRadius());
    }

    @Test
    public void testSetters() {
        plnt = new Planet("test", 5.0f);

        plnt.setName("newname");
        assertEquals("newname", plnt.getName());

        plnt.setPosition(new Vector3(1.0f, -2.0f, 3.0f));
        assertEquals(new Vector3(1.0f, -2.0f, 3.0f), plnt.getPosition());

        plnt.setVelocity(new Vector3(1.0f, -2.0f, 3.0f));
        assertEquals(new Vector3(1.0f, -2.0f, 3.0f), plnt.getVelocity());

        plnt.setRadius(5.0f);
        assertEquals(5.0f, plnt.getRadius(), EPSILON);
    }

    @Test
    public void testUpdatePositionThrow() {
        plnt = new Planet("failure", 1.0f);
        try {
            plnt.updatePosition(-5.0f);
        } catch (ArgumentOutOfBoundsException except) {
            return;
        }
        fail("failed to throw ArgumentOutOfBoundsException");
    }

    @Test
    public void testUpdatePositionThrowBoundary() {
        plnt = new Planet("failure", 1.0f);
        try {
            plnt.updatePosition(0.0f);
        } catch (ArgumentOutOfBoundsException except) {
            fail("0.0 is a valid deltaTime for updatePosition");
        }

    }

    @Test
    public void testUpdatePosition() {
        Vector3 velocity = new Vector3(1.0f, 1.0f, 1.0f);
        plnt = new Planet("P", new Vector3(), velocity, 1.0f);
        plnt.updatePosition(1.0f);
        assertEquals(velocity, plnt.getPosition());
    }

    @Test
    public void testUpdatePositionMultiple() {
        Vector3 velocity = new Vector3(1.0f, 1.0f, 1.0f);
        plnt = new Planet("P", new Vector3(), velocity, 1.0f);

        plnt.updatePosition(0.2f);
        plnt.updatePosition(1.5f);
        plnt.updatePosition(1.2f);
        assertEquals(Vector3.multiply(velocity, 0.2f + 1.5f + 1.2f), plnt.getPosition());
    }

    @Test
    public void testAddForceThrow() {
        plnt = new Planet("failure", 1.0f);
        try {
            plnt.addForce(new Vector3(), -5.0f);
        } catch (ArgumentOutOfBoundsException except) {
            return;
        }
        fail("failed to throw ArgumentOutOfBoundsException");
    }

    @Test
    public void testAddForceThrowBoundary() {
        plnt = new Planet("failure", 1.0f);
        try {
            plnt.addForce(new Vector3(), 0.0f);
        } catch (ArgumentOutOfBoundsException except) {
            fail("0.0 is a valid deltaTime for addForce");
        }

    }

    @Test
    public void testAddForceRadiusOneDeltaTimeOne() {
        Vector3 velocity = new Vector3(1.0f, 1.0f, 1.0f);

        plnt = new Planet("P", 1.0f);
        plnt.addForce(velocity, 1.0f);
        assertEquals(Vector3.multiply(velocity, 1.0f / plnt.getMass()), plnt.getVelocity());
    }

    @Test
    public void testAddForceRadiusOneDeltaTimeNotOne() {
        Vector3 velocity = new Vector3(1.0f, 1.0f, 1.0f);

        plnt = new Planet("P", 1.0f);
        plnt.addForce(velocity, 2.0f);
        assertEquals(Vector3.multiply(velocity, 2.0f / plnt.getMass()), plnt.getVelocity());
    }

    @Test
    public void testAddForceRadiusNotOneDeltaTimeOne() {
        Vector3 velocity = new Vector3(1.0f, 1.0f, 1.0f);

        plnt = new Planet("P", 5.0f);
        plnt.addForce(velocity, 1.0f);
        assertEquals(Vector3.multiply(velocity, 1.0f / plnt.getMass()), plnt.getVelocity());
    }

    @Test
    public void testAddForceRadiusNotOneDeltaNotTimeOne() {
        Vector3 velocity = new Vector3(1.0f, 1.0f, 1.0f);

        plnt = new Planet("P", 5.0f);
        plnt.addForce(velocity, 3.0f);
        assertEquals(Vector3.multiply(velocity, 3.0f / plnt.getMass()), plnt.getVelocity());
    }

    @Test
    public void testIsCollidingInfactColliding() {
        Planet l = new Planet("L", 2.0f);
        Planet r = new Planet("R", new Vector3(0, 1.0f, 0.0f), new Vector3(), 1.0f);
        assertTrue(l.isCollidingWith(r));
        assertTrue(r.isCollidingWith(l));
    }

    @Test
    public void testIsCollidingInfactCollidingBorder() {
        Planet l = new Planet("L", 2.0f);
        Planet r = new Planet("R", new Vector3(0, 3.0f, 0.0f), new Vector3(), 1.0f);
        assertTrue(l.isCollidingWith(r));
        assertTrue(r.isCollidingWith(l));
    }

    @Test
    public void testIsCollidingNotInfactColliding() {
        Planet l = new Planet("L", 2.0f);
        Planet r = new Planet("R", new Vector3(0, 5.0f, 0.0f), new Vector3(), 1.0f);
        assertFalse(l.isCollidingWith(r));
        assertFalse(r.isCollidingWith(l));
    }
}
