package ui.panels;

import model.*;
import ui.SimulatorState;
import ui.Tickable;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.concurrent.locks.*;

// Hosts the rendering logic code for ViewportPanel, functions similarly to ui.legacy's ViewportEngine class
public class RenderEngine implements Tickable {
    private static final int COLOR_CLEAR_VALUE = 0xFF000000;
    private static final float DEPTH_CLEAR_VALUE = Float.NEGATIVE_INFINITY;
    private static final float CAMERA_PULLBACK_FACTOR = 1.05f;
    private static final float CAMERA_PULLBACK_MIN = 5.0f;

    private Lock readWriteLock;

    private int bufferSize;
    private float[] depthBuffer;
    private int[] colorBuffer;
    private BufferedImage image;

    private Vector3 averagePlanetPos;
    private float furthestPlanetDistance;
    private Transform viewTransform;
    private SimulatorState simState;

    public RenderEngine(int size) {
        readWriteLock = new ReentrantLock();
        simState = simState.getInstance();

        bufferSize = size;
        depthBuffer = new float[size * size];
        image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        // NOTE:
        // this is a real hack of voodo magic to allow me to directly acess the internal
        // data of a buffered image object. the alternative would be to create a new
        // buffered image every frame which would be horribly slow
        colorBuffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        averagePlanetPos = new Vector3();
        furthestPlanetDistance = CAMERA_PULLBACK_MIN;
        viewTransform = new Transform();
    }

    public void lockEngine() {
        readWriteLock.lock();
    }

    public void unlockEngine() {
        readWriteLock.unlock();
    }

    public void drawCurrentFrame(Graphics gfx) {
        lockEngine();
        Rectangle bounds = gfx.getClipBounds();
        int imageSize = Math.min(bounds.width, bounds.height);
        int offsetX = (int) ((double) (bounds.width - imageSize) * 0.5);
        int offsetY = (int) ((double) (bounds.height - imageSize) * 0.5);
        gfx.drawImage(image, offsetX, offsetY, imageSize, imageSize, null);
        unlockEngine();
    }

    @Override
    public void tick() {

        updateAveragePlanetPos();
        updateFurthestPlanetDistance();
        updateViewportMatrix();

        lockEngine();

        clearBuffers();

        unlockEngine();
    }

    // MODIFIES: this
    // EFFECTS: updates average planet position
    private void updateAveragePlanetPos() {
        averagePlanetPos = new Vector3();
        if (simState.getSimulation().getPlanets().size() == 0) {
            return;
        }

        for (Planet planet : simState.getSimulation().getPlanets()) {
            Vector3 posWeighted = Vector3.multiply(planet.getPosition(),
                    1.0f / simState.getSimulation().getPlanets().size());
            averagePlanetPos = Vector3.add(averagePlanetPos, posWeighted);
        }
    }

    // MODIFIES: this
    // EFFECTS: updates the distance of the furthest planet away from the center
    private void updateFurthestPlanetDistance() {
        furthestPlanetDistance = 0.0f;
        for (Planet planet : simState.getSimulation().getPlanets()) {
            float dispFromCenter = Vector3.sub(averagePlanetPos, planet.getPosition()).magnitude();
            furthestPlanetDistance = Math.max(dispFromCenter + planet.getRadius() * 2.0f, furthestPlanetDistance);
        }
    }

    // MODIFIES: this
    // EFFECTS: sets up view matrix for viewing planets
    private void updateViewportMatrix() {
        viewTransform = new Transform();

        Vector3 trl = Vector3.multiply(averagePlanetPos, -1.0f);
        Vector3 rot = new Vector3();
        Vector3 scl = new Vector3(1.0f, 1.0f, 1.0f);
        viewTransform = Transform.multiply(viewTransform, Transform.transform(trl, rot, scl));
        float pullBack = Math.max(CAMERA_PULLBACK_MIN, furthestPlanetDistance * CAMERA_PULLBACK_FACTOR);
        Vector3 pullBackVector = new Vector3(0.0f, 0.0f, -pullBack);
        viewTransform = Transform.multiply(viewTransform, Transform.translation(pullBackVector));
    }

    private void clearBuffers() {
        for (int i = 0; i < bufferSize * bufferSize; i++) {
            colorBuffer[i] = COLOR_CLEAR_VALUE;
            depthBuffer[i] = DEPTH_CLEAR_VALUE;
        }
    }

    private int getBufferIndex(int x, int y) {
        return x + (bufferSize * y);
    }
}
