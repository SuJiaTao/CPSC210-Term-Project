package persistence;

import static org.junit.Assert.assertFalse;
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

    @Before
    public void init() {
        rand = new Random();
        sim = new Simulation();
        p1 = new Planet("bonnie", 1.0f);
        p2 = new Planet("dundie", 2.0f);
        p3 = new Planet("blackbear", 3.5f);
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

    public Vector3 randomVector3() {
        float randX = (rand.nextFloat() - 0.5f) * 100.0f;
        float randY = (rand.nextFloat() - 0.5f) * 100.0f;
        float randZ = (rand.nextFloat() - 0.5f) * 100.0f;
        return new Vector3(randX, randY, randZ);
    }
}
