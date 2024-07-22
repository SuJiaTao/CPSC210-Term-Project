package persistence;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import model.*;
import org.json.*;
import java.util.*;

public class JsonConverterTest {
    private static final float EPSILON = 0.001f;

    public Random rand = new Random();
    public Simulation sim;
    public Planet p1;
    public Planet p2;
    public Planet p3;

    @BeforeEach
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

    public Vector3 randomVector3() {
        float randX = (rand.nextFloat() - 0.5f) * 100.0f;
        float randY = (rand.nextFloat() - 0.5f) * 100.0f;
        float randZ = (rand.nextFloat() - 0.5f) * 100.0f;
        return new Vector3(randX, randY, randZ);
    }
}
