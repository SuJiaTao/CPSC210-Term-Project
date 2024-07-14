package ui;

import model.*;
import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.*;
import com.googlecode.lanterna.input.*;

// Represents the internal graphics state and rendering logic that make up 
// the simulation viewport... it isn't a Bailey project without software
// rendering of some kind!
public class ViewportEngine {
    private float[] depthBuffer;
    private int[] frameBuffer;
    private int pixelCount;

    private Simulation simulation;

    private Transform viewTransform;
    private Vector3 averagePlanetPos;

    // EFFECTS: initalizes a square viewport and other data given the specified
    // parameters
    public ViewportEngine(int size, Simulation referenceSim) {
        simulation = referenceSim;

        pixelCount = size * size;
        depthBuffer = new float[pixelCount];
        frameBuffer = new int[pixelCount];

        viewTransform = new Transform();
        averagePlanetPos = new Vector3();
    }

    // MODIFIES: this
    // EFFECTS: updates viewport based on simulation
    public void update() {
        clearBuffers();

        if (simulation.getPlanets().size() == 0) {
            return;
        }

        updateAveragePlanetPos();
        updateViewportMatrix();
        for (Planet planet : simulation.getPlanets()) {
            drawPlanet(planet);
        }
    }

    // REQUIRES: there must be at least 1 planet present in the simulation
    // MODIFIES: this
    // EFFECTS: updates average planet position
    public void updateAveragePlanetPos() {
        averagePlanetPos = new Vector3();
        for (Planet planet : simulation.getPlanets()) {
            averagePlanetPos = Vector3.add(averagePlanetPos, planet.getPosition());
        }
        averagePlanetPos = Vector3.multiply(averagePlanetPos, 1.0f / simulation.getPlanets().size());
    }

    // MODIFIES: this
    // EFFECTS: sets up view matrix for viewing planets
    public void updateViewportMatrix() {

    }

    // MODIFIES: this
    // EFFECTS: draws specific planet
    public void drawPlanet(Planet p) {

    }

    // MODIFIES: this
    // EFFECTS: clears graphical buffers
    public void clearBuffers() {
        for (int i = 0; i < pixelCount; i++) {
            depthBuffer[i] = Float.NEGATIVE_INFINITY;
            frameBuffer[i] = 0;
        }
    }
}
