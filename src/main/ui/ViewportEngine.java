package ui;

import model.*;

// Represents the internal graphics state and rendering logic that make up 
// the simulation viewport... it isn't a Bailey project without software
// rendering of some kind!
public class ViewportEngine {
    // NOTE: "camera" looks down the negative Z axis
    private static final float CLIPPING_PLANE_DEPTH = -0.1f;

    private float[] depthBuffer;
    private int[] frameBuffer;
    private int bufferWidth;
    private int pixelCount;

    private Simulation simulation;

    private Transform viewTransform;
    private Vector3 averagePlanetPos;

    // Represents an element in the render buffers
    private class BufferPoint {
        private int bufferX;
        private int bufferY;
        private float depth;

        public BufferPoint(int posX, int posY, float depth) {
            bufferX = posX;
            bufferY = posY;
            this.depth = depth;
        }

        public boolean isOutOfBounds(int bufferWidth) {
            if (bufferX < 0 || bufferX >= bufferWidth) {
                return true;
            }
            if (bufferY < 0 || bufferY >= bufferWidth) {
                return true;
            }
            return false;
        }

        public int getBufferX() {
            return bufferX;
        }

        public int getBufferY() {
            return bufferY;
        }

        public float getDepth() {
            return depth;
        }

        public int getBufferIndexOffset(int bufferWidth) {
            return bufferX + (bufferY * bufferWidth);
        }
    }

    // EFFECTS: initalizes a square viewport and other data given the specified
    // parameters
    public ViewportEngine(int size, Simulation referenceSim) {
        simulation = referenceSim;

        bufferWidth = size;
        pixelCount = size * size;
        depthBuffer = new float[pixelCount];
        frameBuffer = new int[pixelCount];

        viewTransform = new Transform();
        averagePlanetPos = new Vector3();
    }

    public int getFrameBufferValue(int x, int y) {
        return frameBuffer[new BufferPoint(x, y, 0.0f).getBufferIndexOffset(bufferWidth)];
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
        float totalMass = 0.0f;
        for (Planet planet : simulation.getPlanets()) {
            float planetMass = planet.getMass();
            averagePlanetPos = Vector3.multiply(Vector3.add(averagePlanetPos, planet.getPosition()), planetMass);
            totalMass += planetMass;
        }
        averagePlanetPos = Vector3.multiply(averagePlanetPos, 1.0f / totalMass);

    }

    // MODIFIES: this
    // EFFECTS: sets up view matrix for viewing planets
    public void updateViewportMatrix() {
        viewTransform = new Transform();
        /*
         * Vector3 centerTranslate = Vector3.multiply(averagePlanetPos, -1.0f);
         * viewTransform = Transform.multiply(viewTransform,
         * Transform.translationMatrix(centerTranslate));
         * viewTransform = Transform.multiply(viewTransform,
         * Transform.rotationMatrixX(35.0f));
         * viewTransform = Transform.multiply(viewTransform,
         * Transform.translationMatrix(new Vector3(0.0f, 0.0f, -25.0f)));
         */
    }

    // MODIFIES: this
    // EFFECTS: draws specific planet
    public void drawPlanet(Planet p) {
        // TODO: finish this so it actually looks nice
        if (p.getPosition().getZ() >= CLIPPING_PLANE_DEPTH) {
            return;
        }

        Vector3 planetPosViewSpace = Transform.multiply(viewTransform, p.getPosition());
        BufferPoint planetPoint = projectPointToScreenSpace(planetPosViewSpace);
        if (planetPoint.isOutOfBounds(bufferWidth)) {
            return;
        }

        depthBuffer[planetPoint.getBufferIndexOffset(bufferWidth)] = planetPoint.getDepth();
        frameBuffer[planetPoint.getBufferIndexOffset(bufferWidth)] = (int) '+';
    }

    // EFFECTS: projects a "worldspace" Vector3 into screenspace coordinates
    public BufferPoint projectPointToScreenSpace(Vector3 point) {
        float posX = point.getX() / point.getZ();
        float posY = point.getY() / point.getZ();
        // NOTE: this transforms a point from [-1, 1] to [0, width]
        posX = ((posX + 1.0f) * 0.5f) * (float) bufferWidth;
        posY = ((posY + 1.0f) * 0.5f) * (float) bufferWidth;
        return new BufferPoint((int) posX, (int) posY, point.getZ());
    }

    // MODIFIES: this
    // EFFECTS: clears graphical buffers
    public void clearBuffers() {
        for (int i = 0; i < pixelCount; i++) {
            depthBuffer[i] = Float.NEGATIVE_INFINITY;
            frameBuffer[i] = (int) ' ';
        }
    }
}