package persistence;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;

import model.*;
import model.exceptions.PlanetDoesntExistException;

import org.json.*;
import java.util.*;

public class JsonConverterTest {
    private static final float EPSILON = 0.001f;

    private Random rand = new Random();
    private Simulation sim;
    private Planet p1;
    private Planet p2;
    private Planet p3;
    private Planet p4;
    private Planet p5;

    @Before
    public void init() {
        rand = new Random();
        sim = new Simulation();
        p1 = new Planet("bonnie", 1.0f);
        p2 = new Planet("dundie", 2.0f);
        p3 = new Planet("blackbear", 3.5f);
        p4 = new Planet("highland", 1.0f);
        p5 = new Planet("marie", 2.0f);
    }

    @Test
    public void testVector3EncodeAndDecode() {
        for (int i = 0; i < 100; i++) {
            Vector3 original = randomVector3();
            Vector3 testVec = JsonConverter.jsonObjectToVector3(JsonConverter.vector3ToJsonObject(original));
            assertEquals(original, testVec);
        }
    }

    @Test
    public void testPlanetEncodeDecode() {
        for (int i = 0; i < 100; i++) {
            String randishName = Long.toHexString(System.nanoTime());
            Vector3 originalPos = randomVector3();
            Vector3 originalVel = randomVector3();
            float originalRad = EPSILON + rand.nextFloat() * 5.0f;

            Planet originalPlanet = new Planet(randishName, originalPos, originalVel, originalRad);
            Planet testPlanet = JsonConverter.jsonObjectToPlanet(JsonConverter.planetToJsonObject(originalPlanet));

            assertEquals(randishName, testPlanet.getName());
            assertEquals(originalPos, testPlanet.getPosition());
            assertEquals(originalVel, testPlanet.getVelocity());
            assertEquals(originalRad, testPlanet.getRadius(), EPSILON);
        }
    }

    @Test
    public void testPlanetRefToJsonThrow() {
        sim.addPlanet(p2);
        try {
            JsonConverter.planetReferenceToJsonObject(p1, sim);
        } catch (PlanetDoesntExistException e) {
            // goodo
            return;
        }
        fail("expected throw PlanetDoesntExistException");
    }

    @Test
    public void testJsonToPlanetRefThrowBadValue() {
        sim.addPlanet(p1);
        try {
            JSONObject jsonObject = JsonConverter.planetReferenceToJsonObject(p1, sim);
            jsonObject.remove(JsonConverter.PLANETREF_KEY_TYPE);
            jsonObject.put(JsonConverter.PLANETREF_KEY_TYPE, "blehh corrupted value");

            JsonConverter.jsonObjectToPlanetReference(jsonObject, sim);
        } catch (PlanetDoesntExistException e) {
            fail("expected throw JSONExcepetion");
        } catch (JSONException e) {
            // goodo
            return;
        }
        fail("expected throw JSONExcpetion");
    }

    @Test
    public void testJsonToPlanetRefThrowOutOfBounds() {
        sim.addPlanet(p1);
        try {
            JSONObject jsonObject = JsonConverter.planetReferenceToJsonObject(p1, sim);
            sim.removePlanet(p1);

            JsonConverter.jsonObjectToPlanetReference(jsonObject, sim);
        } catch (PlanetDoesntExistException e) {
            fail("expected throw JSONExcepetion");
        } catch (JSONException e) {
            // goodo
            return;
        }
        fail("expected throw JSONExcpetion");
    }

    @Test
    public void testPlanetRefInSimEncodeDecode() {
        sim.addPlanet(p1);
        Planet refPlanet = JsonConverter.jsonObjectToPlanetReference(JsonConverter.planetReferenceToJsonObject(p1, sim),
                sim);
        assertTrue(p1 == refPlanet);
    }

    @Test
    public void testPlanetRefInSimEncodeDecodeMultiple() {
        sim.addPlanet(p1);
        sim.addPlanet(p2);
        Planet ref1 = JsonConverter.jsonObjectToPlanetReference(JsonConverter.planetReferenceToJsonObject(p1, sim),
                sim);
        Planet ref2 = JsonConverter.jsonObjectToPlanetReference(JsonConverter.planetReferenceToJsonObject(p2, sim),
                sim);
        assertTrue(p1 == ref1);
        assertTrue(p2 == ref2);
    }

    @Test
    public void testPlanetRefHistoricEncodeDecode() {
        sim.addHistoricPlanet(p1);
        Planet refPlanet = JsonConverter.jsonObjectToPlanetReference(JsonConverter.planetReferenceToJsonObject(p1, sim),
                sim);
        assertTrue(p1 == refPlanet);
    }

    @Test
    public void testPlanetRefHistoricEncodeDecodeMultiple() {
        sim.addHistoricPlanet(p1);
        sim.addHistoricPlanet(p2);
        Planet ref1 = JsonConverter.jsonObjectToPlanetReference(JsonConverter.planetReferenceToJsonObject(p1, sim),
                sim);
        Planet ref2 = JsonConverter.jsonObjectToPlanetReference(JsonConverter.planetReferenceToJsonObject(p2, sim),
                sim);
        assertTrue(p1 == ref1);
        assertTrue(p2 == ref2);
    }

    @Test
    public void testPlanetRefInterleavedEncodeDecodeMultiple() {
        sim.addHistoricPlanet(p1);
        sim.addPlanet(p2);
        sim.addHistoricPlanet(p3);
        Planet ref1 = JsonConverter.jsonObjectToPlanetReference(JsonConverter.planetReferenceToJsonObject(p1, sim),
                sim);
        Planet ref2 = JsonConverter.jsonObjectToPlanetReference(JsonConverter.planetReferenceToJsonObject(p2, sim),
                sim);
        Planet ref3 = JsonConverter.jsonObjectToPlanetReference(JsonConverter.planetReferenceToJsonObject(p3, sim),
                sim);
        assertTrue(p1 == ref1);
        assertTrue(p2 == ref2);
        assertTrue(p3 == ref3);
    }

    @Test
    public void testCollisionEncodeDecode() {
        sim.addPlanet(p1);
        sim.addHistoricPlanet(p2);
        sim.addPlanet(p3);

        Collision col1 = new Collision(p1, p2, 0.0f);
        Collision col2 = new Collision(p2, p3, 1.0f);
        sim.addCollision(col1);
        sim.addCollision(col2);

        Collision tc1 = JsonConverter.jsonObjectToCollision(JsonConverter.collisionToJsonObject(col1, sim), sim);
        assertEquals(col1, tc1);

        Collision tc2 = JsonConverter.jsonObjectToCollision(JsonConverter.collisionToJsonObject(col2, sim), sim);
        assertEquals(col2, tc2);
    }

    @Test
    public void testSimulationEncodeDecode() {
        sim.addPlanet(p1);
        sim.addPlanet(p2);
        sim.addHistoricPlanet(p3);
        sim.addHistoricPlanet(p4);
        sim.addPlanet(p5);
        sim.addCollision(new Collision(p2, p1, 0.0f));
        sim.addCollision(new Collision(p3, p2, 1.0f));
        sim.addCollision(new Collision(p1, p3, 5.0f));
        sim.addCollision(new Collision(p5, p1, 3.0f));
        sim.addCollision(new Collision(p4, p2, 3.0f));

        Simulation testSim = JsonConverter.jsonObjectToSimulation(JsonConverter.simulationToJsonObject(sim));
        try {
            checkPlanetListEquals(sim.getPlanets(), testSim.getPlanets());
            checkPlanetListEquals(sim.getHistoricPlanets(), testSim.getHistoricPlanets());
            checkCollisionListEquals(sim.getCollisions(), testSim.getCollisions());
        } catch (RuntimeException e) {
            fail("testSim not the same! error: " + e.getMessage());
        }
        assertEquals(sim.getTimeElapsed(), testSim.getTimeElapsed(), EPSILON);

    }

    public void checkCollisionListEquals(List<Collision> cl1, List<Collision> cl2) {
        // NOTE:
        // to figure out what the hell im doing, please reference
        // planetListsEqualsButNotSameObject
        if (cl1 == cl2) {
            throw new RuntimeException("same object!");
        }
        if (cl1.size() != cl2.size()) {
            throw new RuntimeException("collisionlist mismatched size!");
        }

        for (Collision cl1Col : cl1) {
            boolean foundMatch = false;
            for (Collision cl2Col : cl2) {
                if (checkCollisionEquals(cl2Col, cl1Col)) {
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) {
                throw new RuntimeException("not all elements of cl1 were in cl2!");
            }
        }

        for (Collision cl2Col : cl2) {
            boolean foundMatch = false;
            for (Collision cl1Col : cl1) {
                if (checkCollisionEquals(cl1Col, cl2Col)) {
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) {
                throw new RuntimeException("not all elements of cl2 were in cl1!");
            }
        }
    }

    public boolean checkCollisionEquals(Collision c1, Collision c2) {
        if (c1 == c2) {
            return false;
        }
        if (Math.abs(c1.getCollisionTime() - c2.getCollisionTime()) >= 0.001f) {
            return false;
        }
        Planet c1P1 = c1.getPlanetsInvolved().get(0);
        Planet c1P2 = c1.getPlanetsInvolved().get(1);
        Planet c2P1 = c2.getPlanetsInvolved().get(0);
        Planet c2P2 = c2.getPlanetsInvolved().get(1);
        return checkPlanetEquals(c1P1, c2P1) || checkPlanetEquals(c1P1, c2P2) ||
                checkPlanetEquals(c1P2, c2P1) || checkPlanetEquals(c1P2, c2P2);
    }

    public void checkPlanetListEquals(List<Planet> pl1, List<Planet> pl2) {
        // NOTE:
        // this is kinda like a set equals, where if we show that x \in pl1 \implies x
        // \in pl2 and then x \in pl2 \implies x \in pl1 means that they are the same
        // list
        if (pl1 == pl2) {
            throw new RuntimeException("same object!");
        }
        if (pl1.size() != pl2.size()) {
            throw new RuntimeException("planetlist mismatched size");
        }

        // NOTE:
        // this is slow as hell
        for (Planet pl1Planet : pl1) {
            boolean foundMatch = false;
            for (Planet pl2Planet : pl2) {
                if (checkPlanetEquals(pl1Planet, pl2Planet)) {
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) {
                throw new RuntimeException("not all elements of pl1 were in pl2");
            }
        }

        for (Planet pl2Planet : pl2) {
            boolean foundMatch = false;
            for (Planet pl1Planet : pl1) {
                if (checkPlanetEquals(pl2Planet, pl1Planet)) {
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) {
                throw new RuntimeException("not all elements of pl2 were in pl1");
            }
        }
    }

    public boolean checkPlanetEquals(Planet p1, Planet p2) {
        if (p1 == p2) {
            throw new RuntimeException("same object!");
        }
        if (!p1.getName().equals(p2.getName())) {
            return false;
        }
        if (!p1.getPosition().equals(p2.getPosition())) {
            return false;
        }
        if (!p1.getVelocity().equals(p2.getVelocity())) {
            return false;
        }
        if (!(Math.abs(p1.getRadius() - p2.getRadius()) < 0.001f)) {
            return false;
        }
        return true;
    }

    public Vector3 randomVector3() {
        float randX = (rand.nextFloat() - 0.5f) * 100.0f;
        float randY = (rand.nextFloat() - 0.5f) * 100.0f;
        float randZ = (rand.nextFloat() - 0.5f) * 100.0f;
        return new Vector3(randX, randY, randZ);
    }
}
