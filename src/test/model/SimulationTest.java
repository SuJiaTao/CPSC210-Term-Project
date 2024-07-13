package model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class SimulationTest {
    private static final float EPSILON = 0.001f;
    private Planet p1;
    private Planet p2;
    private Planet p3;
    private Simulation sim;

    @Before
    public void init() {
        p1 = new Planet("P1", 1.0f);
        p2 = new Planet("P2", new Vector3(1.0f, 0.0f, 0.0f), new Vector3(), 1.5f);
        p3 = new Planet("P3", new Vector3(1000.0f, 0.0f, 0.0f), new Vector3(), 0.7f);
        sim = new Simulation();
    }

    @Test
    public void testCtor() {
        assertEquals(0.0f, sim.getTimeElapsed(), EPSILON);
        assertEquals(0, sim.getCollisions().size(), EPSILON);
        assertEquals(0, sim.getPlanets().size());
    }

    @Test
    public void testAddPlanet() {
        sim.addPlanet(p1);
        assertEquals(1, sim.getPlanets().size());
        assertTrue(sim.getPlanets().contains(p1));
    }

    @Test
    public void testAddPlanetMultiple() {
        sim.addPlanet(p1);
        sim.addPlanet(p2);
        assertEquals(2, sim.getPlanets().size());
        assertTrue(sim.getPlanets().contains(p1));
        assertTrue(sim.getPlanets().contains(p2));
        assertFalse(sim.getPlanets().contains(p3));
    }

    @Test
    public void testProgressTimeElapsed() {
        sim.progressBySeconds(1.0f);
        assertEquals(1.0f, sim.getTimeElapsed(), EPSILON);
    }

    @Test
    public void testProgressTimeElapsedMultiple() {
        sim.progressBySeconds(1.0f);
        sim.progressBySeconds(5.5f);
        sim.progressBySeconds(0.2f);
        assertEquals(1.0f + 5.5f + 0.2f, sim.getTimeElapsed(), EPSILON);
    }

    @Test
    public void testProgressSinglePlanetNoCollisions() {
        sim.addPlanet(p1);
        sim.progressBySeconds(5.0f);
        assertEquals(0, sim.getCollisions().size());
    }

    @Test
    public void testProgressSinglePlanetNoCollisionsMultiple() {
        sim.addPlanet(p1);
        sim.progressBySeconds(5.0f);
        sim.progressBySeconds(10.0f);
        assertEquals(0, sim.getCollisions().size());
    }

    @Test
    public void testProgressMultiPlanetNoCollision() {
        sim.addPlanet(p1);
        sim.addPlanet(p3);
        sim.progressBySeconds(0.5f);
        assertEquals(0, sim.getCollisions().size());
    }

    @Test
    public void testProgressMultiPlanetNoCollisionMultiple() {
        sim.addPlanet(p1);
        sim.addPlanet(p3);
        sim.progressBySeconds(0.5f);
        sim.progressBySeconds(1.5f);
        assertEquals(0, sim.getCollisions().size());
    }

    @Test
    public void testProgressMultiPlanetWithCollision() {
        sim.addPlanet(p1);
        sim.addPlanet(p2);
        sim.progressBySeconds(0.01f);
        assertEquals(1, sim.getCollisions().size());
        assertTrue(sim.getCollisions().get(0).wasPlanetInvolved(p1));
        assertTrue(sim.getCollisions().get(0).wasPlanetInvolved(p2));
    }

    @Test
    public void testProgressMultiPlanetWithCollisionMultiple() {
        sim.addPlanet(p1);
        sim.addPlanet(p2);
        sim.progressBySeconds(0.01f);
        assertEquals(1, sim.getCollisions().size());
        assertTrue(sim.getCollisions().get(0).wasPlanetInvolved(p1));
        assertTrue(sim.getCollisions().get(0).wasPlanetInvolved(p2));
        sim.progressBySeconds(0.01f);
        assertEquals(2, sim.getCollisions().size());
        assertTrue(sim.getCollisions().get(0).wasPlanetInvolved(p1));
        assertTrue(sim.getCollisions().get(0).wasPlanetInvolved(p2));
    }

    @Test
    public void testEnsurePlanetsGetCloserToEachOther() {
        sim.addPlanet(p1);
        sim.addPlanet(p2);
        float distInitial = Vector3.add(p1.getPosition(), Vector3.multiply(p3.getPosition(), -1.0f)).magnitude();
        sim.progressBySeconds(1.0f);
        float distFinal = Vector3.add(p1.getPosition(), Vector3.multiply(p3.getPosition(), -1.0f)).magnitude();
        assertTrue(distFinal < distInitial);
    }

    @Test
    public void testEnsurePlanetsGetCloserToEachOtherMultiple() {
        sim.addPlanet(p1);
        sim.addPlanet(p2);
        float distInitial = Vector3.add(p1.getPosition(), Vector3.multiply(p3.getPosition(), -1.0f)).magnitude();
        sim.progressBySeconds(1.0f);
        sim.progressBySeconds(1.0f);
        sim.progressBySeconds(1.0f);
        float distFinal = Vector3.add(p1.getPosition(), Vector3.multiply(p3.getPosition(), -1.0f)).magnitude();
        assertTrue(distFinal < distInitial);
    }

    @Test
    public void testEnsureGravityMagCalculationIsCorrect() {
        float m1 = 1.0f;
        float m2 = 2.0f;
        float rA = 2.0f;
        float expected1 = (Simulation.GRAVITATIONAL_CONSTANT * m1 * m2) / (rA * rA);
        assertEquals(expected1, sim.calculateGravityMagnitude(m1, m2, rA), EPSILON);
        assertFalse(Float.isInfinite(sim.calculateGravityMagnitude(m1, m2, rA)));
        assertFalse(Float.isNaN(sim.calculateGravityMagnitude(m1, m2, rA)));

        float m3 = 3.0f;
        float m4 = 4.0f;
        float rB = 5.0f;
        float expected2 = (Simulation.GRAVITATIONAL_CONSTANT * m3 * m4) / (rB * rB);
        assertEquals(expected2, sim.calculateGravityMagnitude(m3, m4, rB), EPSILON);
        assertFalse(Float.isInfinite(sim.calculateGravityMagnitude(m3, m4, rB)));
        assertFalse(Float.isNaN(sim.calculateGravityMagnitude(m3, m4, rB)));
    }

    @Test
    public void testEnsureGravityMagCalculationDoesntExplodeAtLowRadius() {
        float m1 = 1.0f;
        float m2 = 2.0f;
        float rTiny = Simulation.EPSILON / 5.0f;
        float expected1 = (Simulation.GRAVITATIONAL_CONSTANT * m1 * m2) / (Simulation.EPSILON);
        assertEquals(expected1, sim.calculateGravityMagnitude(m1, m2, rTiny), EPSILON);
        assertFalse(Float.isInfinite(sim.calculateGravityMagnitude(m1, m2, rTiny)));
        assertFalse(Float.isNaN(sim.calculateGravityMagnitude(m1, m2, rTiny)));
    }
}
