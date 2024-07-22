package persistence;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;

import model.*;
import model.exceptions.*;
import persistence.*;

import org.json.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class SimulationReadWriterTest {
    private static final float EPSILON = 0.001f;
    private Simulation sim;

    @Before
    public void init() {
        sim = new Simulation();
        sim.addPlanet(new Planet("a", 5));
        sim.addPlanet(new Planet("b", 3));
        sim.addHistoricPlanet(new Planet("c", 1));
        sim.addHistoricPlanet(new Planet("d", 2));
        sim.addPlanet(new Planet("e", 8));
    }

    @Test
    public void testSaveAndLoad() {
        try {
            SimulationReadWriter.writeSimulation(sim, "temp");
        } catch (FileNotFoundException fefe) {
            fail("should NOT have thrown FileNotFoundException: " + fefe.getMessage());
        } catch (IOException ioex) {
            fail("should NOT have thrown IOException: " + ioex.getMessage());
        }

        Simulation sim2 = null;
        try {
            sim2 = SimulationReadWriter.readSimulation("temp");
        } catch (FileNotFoundException fefe) {
            fail("should NOT have throw FileNotFoundException");
        }

        // sanity check
        assertEquals(sim.getTimeElapsed(), sim2.getTimeElapsed(), EPSILON);
        assertEquals(sim.getPlanets().size(), sim2.getPlanets().size());
        assertEquals(sim.getHistoricPlanets().size(), sim2.getHistoricPlanets().size());
        assertEquals(sim.getCollisions().size(), sim2.getCollisions().size());
    }

    @Test
    public void testLoadAndFail() {
        try {
            SimulationReadWriter.readSimulation("thisFileDoesNOTExist");
        } catch (FileNotFoundException fefe) {
            // goodo
            return;
        }
        fail("expected FileNotFoundException but never thrown!");
    }
}
