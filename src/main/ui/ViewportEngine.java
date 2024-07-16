package ui;

import exceptions.InvalidRenderStateException;
import model.*;

// Represents the internal graphics state and rendering logic that make up 
// the simulation viewport... it isn't a Bailey project without software
// rendering of some kind!
public class ViewportEngine {
    // NOTE: "camera" looks down the negative Z axis
    private static final float CLIPPING_PLANE_DEPTH = -0.1f;
    private static final char CLEAR_VALUE = ' ';
    private static final float CAMERA_PULLBACK_FACTOR = 1.05f;
    private static final float CAMERA_PULLBACL_MIN = 20.0f;
    private static final int PLANET_CIRCLE_VERTS = 10;
    private static final float PLANET_CIRCLE_VERT_STEP = (float) (Math.PI * 2.0f) / (float) PLANET_CIRCLE_VERTS;

    private float[] depthBuffer;
    private int[] frameBuffer;
    private int bufferWidth;
    private int pixelCount;

    private Simulation simulation;

    private Vector3 averagePlanetPos;
    private float furthestPlanetDistance;
    private Transform viewTransform;

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

    public int getSize() {
        return bufferWidth;
    }

    // MODIFIES: this
    // EFFECTS: updates viewport based on simulation
    public void update() {
        clearBuffers();

        if (simulation.getPlanets().size() == 0) {
            return;
        }

        updateAveragePlanetPos();
        updateFurthestPlanetDistance();
        updateViewportMatrix();
        for (Planet planet : simulation.getPlanets()) {
            drawPlanet(planet);
        }
    }

    // MODIFIES: this
    // EFFECTS: updates average planet position
    public void updateAveragePlanetPos() {
        averagePlanetPos = new Vector3();
        if (simulation.getPlanets().size() == 0) {
            return;
        }

        for (Planet planet : simulation.getPlanets()) {
            Vector3 posWeighted = Vector3.multiply(planet.getPosition(), 1.0f / simulation.getPlanets().size());
            averagePlanetPos = Vector3.add(averagePlanetPos, posWeighted);
        }
    }

    // MODIFIES: this
    // EFFECTS: updates the distance of the furthest planet away from the center
    public void updateFurthestPlanetDistance() {
        furthestPlanetDistance = 0.0f;
        for (Planet planet : simulation.getPlanets()) {
            float dispFromCenter = Vector3.sub(averagePlanetPos, planet.getPosition()).magnitude();
            furthestPlanetDistance = Math.max(dispFromCenter + planet.getRadius() * 2.0f, furthestPlanetDistance);
        }
    }

    // MODIFIES: this
    // EFFECTS: sets up view matrix for viewing planets
    public void updateViewportMatrix() {
        viewTransform = new Transform();

        Vector3 trl = Vector3.multiply(averagePlanetPos, -1.0f);
        Vector3 rot = new Vector3();
        Vector3 scl = new Vector3(1.0f, 1.0f, 1.0f);
        viewTransform = Transform.multiply(viewTransform, Transform.transform(trl, rot, scl));
        float pullBack = Math.max(CAMERA_PULLBACL_MIN, furthestPlanetDistance * CAMERA_PULLBACK_FACTOR);
        Vector3 pullBackVector = new Vector3(0.0f, 0.0f, -pullBack);
        viewTransform = Transform.multiply(viewTransform, Transform.translation(pullBackVector));
    }

    // MODIFIES: this
    // EFFECTS: draws specific planet
    public void drawPlanet(Planet planet) {

        // TODO: finish this so it actually looks nice

        Vector3 planetPosViewSpace = Transform.multiply(viewTransform, planet.getPosition());
        if (planetPosViewSpace.getZ() >= CLIPPING_PLANE_DEPTH) {
            return;
        }

        Vector3 planetScale = new Vector3(planet.getRadius(), planet.getRadius(), planet.getRadius());
        Transform planetTransform = Transform.transform(planet.getPosition(), new Vector3(), planetScale);
        planetTransform = Transform.multiply(planetTransform, viewTransform);

        Vector3 circleCenter = Transform.extractTranslation(planetTransform);
        float circleRadius = Transform.extractScale(planetTransform).getX();

        for (int i = 0; i < PLANET_CIRCLE_VERTS; i++) {
            Vector3 posI = getCircleVertPos(circleCenter, circleRadius, i);
            Vector3 posF = getCircleVertPos(circleCenter, circleRadius, i + 1);
            BufferPoint projI = projectPointToScreenSpace(posI);
            BufferPoint projF = projectPointToScreenSpace(posF);
            drawLine(projI, projF, planet.getName().charAt(0));
        }
    }

    // EFFECTS: returns the worldspace vertex position of the planet circle vertex
    // of index i
    public Vector3 getCircleVertPos(Vector3 center, float radius, int index) {
        float offsetX = (float) Math.cos(PLANET_CIRCLE_VERT_STEP * (float) index) * radius;
        float offsetY = (float) Math.sin(PLANET_CIRCLE_VERT_STEP * (float) index) * radius;
        return Vector3.add(center, new Vector3(offsetX, offsetY, 0.0f));
    }

    // MODIFIES: this
    // EFFECTS: draws a line of specified char from the specified buffer points
    public void drawLine(BufferPoint from, BufferPoint to, char visChar) {
        // NOTE:
        // yes, this is a terribly naiive way of drawing a line
        float deltaX = to.getBufferX() - from.getBufferX();
        float deltaY = to.getBufferY() - from.getBufferY();
        float dist = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        float drawX = from.getBufferX();
        float drawY = from.getBufferY();
        for (float step = 0; step <= dist; step++) {
            float depth = from.getDepth() + (to.getDepth() - from.getDepth()) * (1.0f - step / dist);
            drawPoint(new BufferPoint((int) (drawX + 0.5f), (int) (drawY + 0.5f), depth), visChar);
            drawX += deltaX / dist;
            drawY += deltaY / dist;
        }
    }

    // MODIFIES: this
    // EFFECTS: draws the specified char to the framebuffer
    public void drawPoint(BufferPoint point, char visChar) {
        if (point.isOutOfBounds(bufferWidth)) {
            return;
        }

        if (depthBuffer[point.getBufferIndexOffset(bufferWidth)] >= point.getDepth()) {
            return;
        }

        depthBuffer[point.getBufferIndexOffset(bufferWidth)] = point.getDepth();
        frameBuffer[point.getBufferIndexOffset(bufferWidth)] = (int) visChar;
    }

    // EFFECTS: projects a "worldspace" Vector3 into screenspace coordinates
    public BufferPoint projectPointToScreenSpace(Vector3 point) {
        if (point.getZ() >= CLIPPING_PLANE_DEPTH) {
            throw new InvalidRenderStateException();
        }
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
            frameBuffer[i] = (int) CLEAR_VALUE;
        }
    }
}
