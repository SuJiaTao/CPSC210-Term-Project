package ui.engine;

import model.*;
import ui.SimulatorGUI;
import ui.SimulatorState;
import ui.Tickable;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.concurrent.locks.*;

// Hosts the rendering logic code for ViewportPanel, functions similarly to ui.legacy's ViewportEngine class
public class RenderEngine implements Tickable {
    private static final int COLOR_CLEAR_VALUE = 0xFF000000;
    private static final float DEPTH_CLEAR_VALUE = Float.NEGATIVE_INFINITY;
    private static final float VIEWPORT_SCALE_FACTOR = 0.97f;
    private static final float CLIPPING_PLANE_DEPTH = -1.0f;
    private static final Color[] PLANET_COLORS = { new Color(0xF6995C), new Color(0x51829B), new Color(0x9BB0C1),
            new Color(0xEADFB4) };

    private Lock readWriteLock;

    private int bufferSize;
    private float[] depthBuffer;
    private int[] colorBuffer;
    private BufferedImage image;

    private SimulatorState simState;
    private JPanel parent;

    private Transform viewTransform;
    private CameraController cameraController;

    // TODO: improve
    private Mesh planetMesh;

    public RenderEngine(JPanel parent, int size) {
        this.parent = parent;
        parent.setFocusable(true);

        readWriteLock = new ReentrantLock();
        simState = SimulatorState.getInstance();

        bufferSize = size;
        depthBuffer = new float[size * size];
        image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        // NOTE:
        // this is a real hack of voodo magic to allow me to directly acess the internal
        // data of a buffered image object. the alternative would be to create a new
        // buffered image every frame which would be horribly slow
        colorBuffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        planetMesh = Mesh.getPlanetMesh();

        viewTransform = new Transform();
        cameraController = new CameraController(this);
    }

    public void setViewTransform(Transform viewTransform) {
        this.viewTransform = viewTransform;
    }

    public JPanel getPanel() {
        return parent;
    }

    public CameraController getCameraController() {
        return cameraController;
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
        int imageSize = (int) ((float) Math.min(bounds.width, bounds.height) * VIEWPORT_SCALE_FACTOR);
        int offsetX = (int) ((double) (bounds.width - imageSize) * 0.5);
        int offsetY = (int) ((double) (bounds.height - imageSize) * 0.5);
        gfx.drawImage(image, offsetX, offsetY, imageSize, imageSize, null);
        unlockEngine();
    }

    @Override
    public void tick() {
        cameraController.tick();

        lockEngine();

        clearBuffers();
        // NOTE:
        // because swing is multithreaded, I cant simply iterate over planets, so I have
        // to do this hacky nonsense
        for (int i = 0; i < simState.getSimulation().getPlanets().size(); i++) {
            try {
                Planet planet = simState.getSimulation().getPlanets().get(i);
                drawPlanet(planet);
            } catch (Exception exc) {
                // ignore
            }
        }

        unlockEngine();
    }

    private void drawPlanet(Planet planet) {
        // TODO: make look good
        Vector3 planetWorldPos = Transform.multiply(viewTransform, planet.getPosition());
        if (planetWorldPos.getZ() >= CLIPPING_PLANE_DEPTH) {
            return;
        }

        Vector3 planetScale = new Vector3(planet.getRadius(), planet.getRadius(), planet.getRadius());
        Transform planetTransform = Transform.transform(planet.getPosition(), new Vector3(), planetScale);
        Transform meshTransform = Transform.multiply(planetTransform, viewTransform);

        shadeMesh(planetMesh, meshTransform);
    }

    private int getPlanetColor(Planet planet) {
        if (planet == SimulatorGUI.getInstance().getSelectedPlanet()) {
            return 0xFFFFFFFF;
        }

        int randIndex = planet.getName().hashCode() % PLANET_COLORS.length;
        if (randIndex < 0) {
            randIndex += PLANET_COLORS.length;
        }
        return PLANET_COLORS[randIndex].getRGB();
    }

    private void drawWireMesh(Mesh mesh, Transform transform, int color) {
        for (int triIndex = 0; triIndex < mesh.getTriangleCount(); triIndex++) {
            Triangle tri = mesh.getTriangle(triIndex);
            tri.verts[0] = Transform.multiply(transform, tri.verts[0]);
            tri.verts[1] = Transform.multiply(transform, tri.verts[1]);
            tri.verts[2] = Transform.multiply(transform, tri.verts[2]);

            tri = projectTriangleToScreenSpace(tri);

            drawLine(tri.verts[0], tri.verts[1], color);
            drawLine(tri.verts[1], tri.verts[2], color);
            drawLine(tri.verts[2], tri.verts[0], color);
        }
    }

    // MODIFIES: this
    // EFFECTS: renders a given mesh
    private void shadeMesh(Mesh mesh, Transform transform) {
        for (int triIndex = 0; triIndex < mesh.getTriangleCount(); triIndex++) {
            Triangle tri = mesh.getTriangle(triIndex);
            tri.verts[0] = Transform.multiply(transform, tri.verts[0]);
            tri.verts[1] = Transform.multiply(transform, tri.verts[1]);
            tri.verts[2] = Transform.multiply(transform, tri.verts[2]);

            tri = projectTriangleToScreenSpace(tri);

            shadeTriangle(tri);
        }
    }

    // MODIFIES: this
    // EFFECTS: renders a given triangle
    private void shadeTriangle(Triangle tri) {
        // NOTE:
        // the standard procedure for rendering an arbitrary triangle is to split it in
        // the middle, and render the flattop/flatbottom parts of it each

        Triangle sortedTri = sortTriangleByHeight(tri);
        Triangle[] cutTris = cutSortedTriangle(sortedTri);

        shadeTriangleFlatBottom(cutTris[0]);
        shadeTriangleFlatTop(cutTris[1]);
    }

    // MODIFIES: this
    // EFFECTS: renders a triangle with a flat bottom
    private void shadeTriangleFlatBottom(Triangle flatBotTri) {
        // NOTE: the verticies of the tri are as follows:
        // verts[0] -> top pointy
        // verts[1] -> left bottom vertex
        // verts[2] -> right bottom vertex

        float dyBottomToTop = flatBotTri.verts[0].getY() - flatBotTri.verts[1].getY();
        if (dyBottomToTop <= 0.0f) {
            return;
        }

        float dxLeftToTop = flatBotTri.verts[0].getX() - flatBotTri.verts[1].getX();
        float dxRightToTop = flatBotTri.verts[0].getX() - flatBotTri.verts[2].getX();

        float invSlopeLeftToTop = dxLeftToTop / dyBottomToTop;
        float invSlopeRightToTop = dxRightToTop / dyBottomToTop;

        float startY = flatBotTri.verts[1].getY();
        startY = Math.max(0, startY);

        float endY = flatBotTri.verts[0].getY();
        endY = Math.min(endY, (float) bufferSize - 1);

        for (float drawY = startY; drawY <= endY; drawY += 1.0f) {
            float travelledY = drawY - flatBotTri.verts[2].getY();

            float startX = flatBotTri.verts[1].getX() + (travelledY * invSlopeLeftToTop);
            startX = Math.max(0, startX);

            float endX = flatBotTri.verts[2].getX() + (travelledY * invSlopeRightToTop);
            endX = Math.min(endX, (float) bufferSize - 1);

            for (float drawX = startX; drawX <= endX; drawX += 1.0f) {
                drawFragment(new Vector3(drawX, drawY, 0.0f), 0xFFFFFFFF);
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: renders a triangle with a flat top
    private void shadeTriangleFlatTop(Triangle flatBotTri) {
        // NOTE: the verticies of the tri are as follows:
        // verts[0] -> top left
        // verts[1] -> top right
        // verts[2] -> bottom pointy

        float dyBottomToTop = flatBotTri.verts[0].getY() - flatBotTri.verts[2].getY();
        if (dyBottomToTop <= 0.0f) {
            return;
        }

        float dxBottomToLeft = flatBotTri.verts[0].getX() - flatBotTri.verts[2].getX();
        float dxBottomToRight = flatBotTri.verts[1].getX() - flatBotTri.verts[2].getX();

        float invSlopeBottomToLeft = dxBottomToLeft / dyBottomToTop;
        float invSlopeBottomToRight = dxBottomToRight / dyBottomToTop;

        float startY = flatBotTri.verts[2].getY();
        startY = Math.max(0, startY);

        float endY = flatBotTri.verts[0].getY();
        endY = Math.min(endY, (float) bufferSize - 1);

        for (float drawY = startY; drawY <= endY; drawY += 1.0f) {
            float travelledY = drawY - flatBotTri.verts[2].getY();

            float startX = flatBotTri.verts[2].getX() + (travelledY * invSlopeBottomToLeft);
            startX = Math.max(0, startX);

            float endX = flatBotTri.verts[2].getX() + (travelledY * invSlopeBottomToRight);
            endX = Math.min(endX, (float) bufferSize - 1);

            for (float drawX = startX; drawX <= endX; drawX += 1.0f) {
                drawFragment(new Vector3(drawX, drawY, 0.0f), 0xFFFFFFFF);
            }
        }

    }

    // EFFECTS: cuts a sorted triangle in half along the middle vertex and returns
    // each piece, where the top verticies are sorted from left to right
    private Triangle[] cutSortedTriangle(Triangle sortedTri) {
        float dxLowToHigh = sortedTri.verts[0].getX() - sortedTri.verts[2].getX();
        float dyLowToHigh = sortedTri.verts[0].getY() - sortedTri.verts[2].getY();
        float invSlopeLowToHigh = dxLowToHigh / dyLowToHigh;
        float distLowToMid = (sortedTri.verts[1].getY() - sortedTri.verts[2].getY());
        float middleX = sortedTri.verts[2].getX() + distLowToMid * invSlopeLowToHigh;

        // NOTE: depth interpolation will be done with respect to the original sorted
        // tri, so the depth values of each cut tri doesn't actually matter
        Vector3 middleVert = new Vector3(middleX, sortedTri.verts[1].getY(), 0.0f);

        // NOTE: construct triangle such that
        // verts[0] -> top pointy
        // verts[1] -> bottom left
        // verts[2] -> bottom right
        Triangle topTriFlatBottom = new Triangle(sortedTri);
        topTriFlatBottom.verts[2] = new Vector3(middleVert);
        if (topTriFlatBottom.verts[1].getX() > topTriFlatBottom.verts[2].getX()) {
            Vector3 tempVert = topTriFlatBottom.verts[1];
            topTriFlatBottom.verts[1] = topTriFlatBottom.verts[2];
            topTriFlatBottom.verts[2] = tempVert;
        }

        // NOTE: constructs a triangle such that
        // verts[0] -> top left
        // verts[1] -> top right
        // verts[2] -> bottom pointy
        Triangle bottomTriFlatTop = new Triangle(sortedTri);
        bottomTriFlatTop.verts[0] = new Vector3(middleVert);
        if (bottomTriFlatTop.verts[0].getX() > bottomTriFlatTop.verts[1].getX()) {
            Vector3 tempVert = bottomTriFlatTop.verts[0];
            bottomTriFlatTop.verts[0] = bottomTriFlatTop.verts[1];
            bottomTriFlatTop.verts[1] = tempVert;
        }

        return new Triangle[] { topTriFlatBottom, bottomTriFlatTop };
    }

    // EFFECTS: returns a new triangle which is the original triangle with the
    // verticies sorted by height, such that 0 is the highest and 2 is the lowest
    private Triangle sortTriangleByHeight(Triangle toSort) {
        Triangle sorted = new Triangle(toSort);
        Vector3 tempVert = null;
        Vector3 tempUV = null;

        if (sorted.verts[0].getY() < sorted.verts[1].getY()) {
            tempVert = sorted.verts[0];
            tempUV = sorted.uvs[0];
            sorted.verts[0] = sorted.verts[1];
            sorted.verts[1] = tempVert;
            sorted.uvs[0] = sorted.uvs[1];
            sorted.uvs[1] = tempUV;
        }
        if (sorted.verts[1].getY() < sorted.verts[2].getY()) {
            tempVert = sorted.verts[1];
            tempUV = sorted.uvs[1];
            sorted.verts[1] = sorted.verts[2];
            sorted.verts[2] = tempVert;
            sorted.uvs[1] = sorted.uvs[2];
            sorted.uvs[2] = tempUV;
        }
        if (sorted.verts[0].getY() < sorted.verts[1].getY()) {
            tempVert = sorted.verts[0];
            tempUV = sorted.uvs[0];
            sorted.verts[0] = sorted.verts[1];
            sorted.verts[1] = tempVert;
            sorted.uvs[0] = sorted.uvs[1];
            sorted.uvs[1] = tempUV;
        }

        return sorted;
    }

    // EFFECTS: creates a new triangle which has been projected into screenspace
    // coordinates
    private Triangle projectTriangleToScreenSpace(Triangle triangle) {
        Triangle projTri = new Triangle(triangle);
        projTri.verts[0] = projectVectorToScreenSpace(projTri.verts[0]);
        projTri.verts[1] = projectVectorToScreenSpace(projTri.verts[1]);
        projTri.verts[2] = projectVectorToScreenSpace(projTri.verts[2]);
        return projTri;
    }

    // EFFECTS: projects a "worldspace" Vector3 into screenspace coordinates
    private Vector3 projectVectorToScreenSpace(Vector3 point) {
        // NOTE: despite facing down the -Z axis, we dont want X and Y axis to be
        // inverted, so we take the Abs of the Z
        float posX = point.getX() / Math.abs(point.getZ());
        float posY = point.getY() / Math.abs(point.getZ());
        // NOTE: this transforms a point from [-1, 1] to [0, bufferSize]
        posX = ((posX + 1.0f) * 0.5f) * (float) bufferSize;
        posY = ((posY + 1.0f) * 0.5f) * (float) bufferSize;
        return new Vector3(posX, posY, point.getZ());
    }

    private void drawLine(Vector3 from, Vector3 to, int color) {
        float deltaX = to.getX() - from.getX();
        float deltaY = to.getY() - from.getY();
        float dist = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        float drawX = from.getX();
        float drawY = from.getY();
        for (float step = 0; step <= dist; step++) {
            float depth = from.getZ() + (to.getZ() - from.getZ()) * (1.0f - step / dist);
            drawFragment(new Vector3(drawX, drawY, depth), color);
            drawX += deltaX / dist;
            drawY += deltaY / dist;
        }
    }

    private void drawFragment(Vector3 position, int color) {
        int posX = (int) (position.getX() + 0.5f);
        int posY = (int) (position.getY() + 0.5f);

        if (posX < 0 || posX >= bufferSize || posY < 0 || posY >= bufferSize) {
            return;
        }

        // depth test
        float depth = depthBuffer[getBufferIndex(posX, posY)];
        if (depth >= position.getZ()) {
            return;
        }

        colorBuffer[getBufferIndex(posX, posY)] = color;
        depthBuffer[getBufferIndex(posX, posY)] = position.getZ();
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
