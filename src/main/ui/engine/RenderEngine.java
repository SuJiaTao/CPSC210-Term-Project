package ui.engine;

import ui.*;
import model.*;
import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import ui.engine.shader.*;
import java.util.ArrayList;
import java.util.concurrent.locks.*;

// Hosts the rendering logic code for ViewportPanel, functions similarly to ui.legacy's ViewportEngine class
public class RenderEngine implements Tickable {
    public static final float CLIPPING_PLANE_DEPTH = -0.1f;
    private static final int COLOR_CLEAR_VALUE = 0xFF000000;
    private static final float DEPTH_CLEAR_VALUE = Float.NEGATIVE_INFINITY;
    private static final float VIEWPORT_SCALE_FACTOR = 0.97f;
    private static final float SELECTOR_SCALE = 1.15f;
    private static final Mesh DEBUG_MESH = Mesh.loadMeshByFileName(Mesh.MESH_DEBUG_NAME);
    private static final Mesh PLANET_MESH = Mesh.loadMeshByFileName(Mesh.MESH_UVSPHERE_NAME);
    private static final Mesh PLANET_SELECTOR_MESH = Mesh.loadMeshByFileName(Mesh.MESH_ICOSPHERE_NAME);
    private static final BufferedImage TEXTURE_DEBUG = SimulatorUtils.loadImage("debug.jpg");
    private static final BufferedImage TEXTURE_EARTH = SimulatorUtils.loadImage("special/earth.jpg");
    private static final BufferedImage TEXTURE_PAUL = SimulatorUtils.loadImage("special/paul.jpg");
    private static final BufferedImage TEXTURE_UNIVERSE = SimulatorUtils.loadImage("special/universe.png");
    private static final float UNIVERSE_SCALE = 500000.0f;
    private static final BufferedImage TEXTURE_SUN = SimulatorUtils.loadImage("sun.jpg");
    private static final BufferedImage[] TEXTURE_ROCKY_PLANETS = {
            SimulatorUtils.loadImage("rockyA.jpg"),
            SimulatorUtils.loadImage("rockyB.jpg"),
            SimulatorUtils.loadImage("rockyC.jpg"),
            SimulatorUtils.loadImage("rockyD.jpg"),
            SimulatorUtils.loadImage("rockyE.jpg"),
            SimulatorUtils.loadImage("rockyF.jpg"),
    };
    private static final BufferedImage[] TEXTURE_GASGIANT_PLANETS = {
            SimulatorUtils.loadImage("gasA.jpg"),
            SimulatorUtils.loadImage("gasB.jpg"),
            SimulatorUtils.loadImage("gasC.jpg"),
            SimulatorUtils.loadImage("gasD.jpg"),
            SimulatorUtils.loadImage("gasE.jpg"),
            SimulatorUtils.loadImage("gasF.jpg")
    };
    private static final float PLANET_SPIN_MAX = 500.0f;
    private static final float TRAIL_UPDATE_FACTOR = 0.2f;
    private static final float TRAIL_UPDATE_MINDISTANCE = 5.0f;
    private static final float DRAWLINE_EPSILON = 0.0001f;

    private int bufferSize;
    private float[] depthBuffer;
    private int[] colorBuffer;
    private BufferedImage image;
    private ReentrantLock imageSync;

    private SimulatorState simState;
    private JPanel parent;

    private Transform viewTransform;
    private CameraController cameraController;

    private Planet lastSelectedPlanet;
    private ArrayList<Vector3> selectedPlanetTrail;

    // EFFECTS: initializes the framebuffer to be size x size dimensions,
    // initializes a cameracontroller and viewtransfrom
    public RenderEngine(JPanel parent, int size) {
        this.parent = parent;
        parent.setFocusable(true);

        simState = SimulatorState.getInstance();

        bufferSize = size;
        depthBuffer = new float[size * size];
        image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        imageSync = new ReentrantLock();

        // NOTE:
        // this is a real hack of voodo magic to allow me to directly acess the internal
        // data of a buffered image object. the alternative would be to create a new
        // buffered image every frame which would be horribly slow
        colorBuffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        viewTransform = new Transform();
        cameraController = new CameraController(this);

        lastSelectedPlanet = null;
        selectedPlanetTrail = new ArrayList<>();
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

    // EFFECTS: synchronously draws the current state of the framebuffer to the
    // given Graphics object
    public void drawCurrentFrame(Graphics gfx) {
        Rectangle bounds = gfx.getClipBounds();
        int imageSize = (int) ((float) Math.min(bounds.width, bounds.height) * VIEWPORT_SCALE_FACTOR);
        int offsetX = (int) ((double) (bounds.width - imageSize) * 0.5);
        int offsetY = (int) ((double) (bounds.height - imageSize) * 0.5);

        imageSync.lock();
        gfx.drawImage(image, offsetX, offsetY, imageSize, imageSize, null);
        imageSync.unlock();
    }

    // MODIFIES: this
    // EFFECTS: synchronously updates the camera controller, clears the framebuffer,
    // and draws all graphics
    @Override
    public void tick() {
        simState.lock();
        imageSync.lock();
        cameraController.tick();

        clearBuffers();
        for (Planet planet : simState.getSimulation().getPlanets()) {
            drawPlanet(planet);
        }

        drawSelectedPlanetTrail();
        drawUniverse();

        imageSync.unlock();
        simState.unlock();
    }

    // MODIFIES: this
    // EFFECTS: draws a massive universe skybox which tracks to the camera
    private void drawUniverse() {
        Transform uniTransform = Transform.scale(new Vector3(UNIVERSE_SCALE, UNIVERSE_SCALE, UNIVERSE_SCALE));
        uniTransform = Transform.multiply(uniTransform, viewTransform);
        Vector3 cameraOffset = Transform.extractTranslation(viewTransform);
        uniTransform = Transform.multiply(uniTransform, Transform.translation(Vector3.multiply(cameraOffset, -1.0f)));
        TextureShader shader = new TextureShader(TEXTURE_UNIVERSE);
        shadeMesh(shader, PLANET_MESH, uniTransform);
    }

    // MODIFIES: this
    // EFFECTS: ensures that the trail is following the correct planet, adds new
    // verticies to the trail when appropriate, and draws the trail
    private void drawSelectedPlanetTrail() {
        if (SimulatorGUI.getInstance().getSelectedPlanet() != lastSelectedPlanet) {
            selectedPlanetTrail.clear();
            lastSelectedPlanet = SimulatorGUI.getInstance().getSelectedPlanet();
        }

        if (lastSelectedPlanet == null) {
            return;
        }

        if (selectedPlanetTrail.size() == 0) {
            selectedPlanetTrail.add(lastSelectedPlanet.getPosition());
        }

        int lastIndex = selectedPlanetTrail.size() - 1;
        Vector3 deltaPos = Vector3.sub(selectedPlanetTrail.get(lastIndex), lastSelectedPlanet.getPosition());
        float planetVelMag = lastSelectedPlanet.getVelocity().magnitude();
        if (deltaPos.magnitude() > planetVelMag * TRAIL_UPDATE_FACTOR
                && deltaPos.magnitude() >= TRAIL_UPDATE_MINDISTANCE) {
            selectedPlanetTrail.add(lastSelectedPlanet.getPosition());
        }

        selectedPlanetTrail.add(lastSelectedPlanet.getPosition());
        for (int i = 0; i < selectedPlanetTrail.size() - 1; i++) {
            Vector3 linePosI = Transform.multiply(viewTransform, selectedPlanetTrail.get(i));
            Vector3 linePosF = Transform.multiply(viewTransform, selectedPlanetTrail.get(i + 1));
            drawLine(new LineShader(0xFFFFFFFF), linePosI, linePosF);
        }
        selectedPlanetTrail.remove(selectedPlanetTrail.size() - 1);
    }

    // MODIFIES: this
    // EFFECTS: generates a transformation matrix for the current planet, draws an
    // outline if the planet is selected, selects the appropriate shader for the
    // planet, and then renders the planet to the framebuffer
    private void drawPlanet(Planet planet) {
        Vector3 planetScale = new Vector3(planet.getRadius(), planet.getRadius(), planet.getRadius());
        Transform planetTransform = Transform.transform(planet.getPosition(), new Vector3(), planetScale);
        Transform meshTransform = Transform.multiply(planetTransform, viewTransform);

        if (planet == SimulatorGUI.getInstance().getSelectedPlanet()) {
            Vector3 scaleVector = new Vector3(SELECTOR_SCALE, SELECTOR_SCALE, SELECTOR_SCALE);
            drawWireMesh(PLANET_SELECTOR_MESH, Transform.multiply(Transform.scale(scaleVector), meshTransform),
                    0xFFFFFFFF);
        }

        AbstractShader shader = null;
        int planetSeed = Math.abs(planet.getName().hashCode());
        float planeSpinSpeed = getPlanetSpinRate(planet);
        Transform planetSpin = Transform
                .rotationY(planeSpinSpeed * SimulatorState.getInstance().getSimulation().getTimeElapsed());
        switch (SimulatorUtils.getPlanetType(planet)) {
            case Star:
                drawPlanetSun(planet, Transform.multiply(planetSpin, meshTransform));
                break;

            case GasGiant:
                drawPlanetGasGiant(planetSeed, planet, Transform.multiply(planetSpin, meshTransform));
                break;

            case Rocky:
                drawPlanetRocky(planetSeed, planet, Transform.multiply(planetSpin, meshTransform));
                break;

            default:
                // NOTE:
                // this should not be reached ever
                shader = new TextureShader(TEXTURE_DEBUG);
                shadeMesh(shader, DEBUG_MESH, meshTransform);
        }
    }

    // MODIFIES: this
    // EFFECTS: selects the sun shader and renders the planet with it
    private void drawPlanetSun(Planet planet, Transform transform) {
        AbstractShader shader = new SunShader(TEXTURE_SUN);
        if (planet.getName().equals("Paul")) {
            shader = new TextureShader(TEXTURE_PAUL);
        }
        shadeMesh(shader, PLANET_MESH, transform);
    }

    // MODIFIES: this
    // EFFECTS: chooses a random gas giant texture based on the planet's name,
    // initializes the appropriate shaders, and renders the planet in layers
    private void drawPlanetGasGiant(int planetSeed, Planet planet, Transform transform) {
        planetSeed %= TEXTURE_GASGIANT_PLANETS.length;
        BufferedImage texture = TEXTURE_GASGIANT_PLANETS[planetSeed];

        GasGiantLayerShader layer0 = new GasGiantLayerShader(texture, 5.0f, 1.0f);
        shadeMesh(layer0, PLANET_MESH, Transform.multiply(Transform.scale(uniformScaleVector(1.0f)), transform));

        GasGiantLayerShader layer1 = new GasGiantLayerShader(texture, 25.0f, 0.6f);
        shadeMesh(layer1, PLANET_MESH, Transform.multiply(Transform.scale(uniformScaleVector(1.02f)), transform));

        GasGiantLayerShader layer2 = new GasGiantLayerShader(texture, 50.0f, 0.3f);
        shadeMesh(layer2, PLANET_MESH, Transform.multiply(Transform.scale(uniformScaleVector(1.05f)), transform));
    }

    // MODIFIES: this
    // EFFECTS: chooses a random rocky texture and cloud texture based on the
    // planets name, inits the appropriate shaders, and then renders the planet in
    // layers
    private void drawPlanetRocky(int planetSeed, Planet planet, Transform transform) {
        int groundSeed = planetSeed % TEXTURE_ROCKY_PLANETS.length;
        BufferedImage groundTexture = TEXTURE_ROCKY_PLANETS[groundSeed];
        if (planet.getName().equals("Earth")) {
            groundTexture = TEXTURE_EARTH;
        }

        TextureShader groundShader = new TextureShader(groundTexture);
        shadeMesh(groundShader, PLANET_MESH, transform);
    }

    // EFFECTS: creates a vector with all components set to scale
    private Vector3 uniformScaleVector(float scale) {
        return new Vector3(scale, scale, scale);
    }

    // EFFECTS: returns a random planet angular velocity based on its name and
    // radius
    private float getPlanetSpinRate(Planet planet) {
        int seed = Math.abs(planet.getName().hashCode());
        float norm = ((float) (seed & 0xFFFFFF) / (float) 0xFFFFFF);
        return norm * (PLANET_SPIN_MAX / Math.max(1.0f, planet.getRadius()));
    }

    // MODIFIES: this
    // EFFECTS: draws a wireframe of the specified mesh with the given color,
    // transformed by the specified matrix
    private void drawWireMesh(Mesh mesh, Transform transform, int color) {
        for (int triIndex = 0; triIndex < mesh.getTriangleCount(); triIndex++) {
            Triangle tri = mesh.getTriangle(triIndex);
            tri.verts[0] = Transform.multiply(transform, tri.verts[0]);
            tri.verts[1] = Transform.multiply(transform, tri.verts[1]);
            tri.verts[2] = Transform.multiply(transform, tri.verts[2]);
            LineShader shader = new LineShader(color);
            drawLine(shader, tri.verts[0], tri.verts[1]);
            drawLine(shader, tri.verts[1], tri.verts[2]);
            drawLine(shader, tri.verts[2], tri.verts[0]);
        }
    }

    // MODIFIES: this
    // EFFECTS: renders a given mesh
    private void shadeMesh(AbstractShader shader, Mesh mesh, Transform transform) {
        for (int triIndex = 0; triIndex < mesh.getTriangleCount(); triIndex++) {
            Triangle tri = mesh.getTriangle(triIndex);
            tri.verts[0] = Transform.multiply(transform, tri.verts[0]);
            tri.verts[1] = Transform.multiply(transform, tri.verts[1]);
            tri.verts[2] = Transform.multiply(transform, tri.verts[2]);
            shadeTriangle(shader, tri);
        }
    }

    // MODIFIES: this
    // EFFECTS: renders a given triangle
    private void shadeTriangle(AbstractShader shader, Triangle tri) {
        Triangle[] clippedTris = clipTriangle(tri);

        for (Triangle clippedTri : clippedTris) {
            clippedTri = projectTriangleToScreenSpace(clippedTri);

            // NOTE:
            // the standard procedure for rendering an arbitrary triangle is to split it in
            // the middle, and render the flattop/flatbottom parts of it each
            Triangle sortedTri = sortTriangleByHeight(clippedTri);
            Triangle[] cutTris = cutSortedTriangle(sortedTri);

            shadeTriangleFlatBottom(shader, cutTris[0], sortedTri);
            shadeTriangleFlatTop(shader, cutTris[1], sortedTri);
        }
    }

    // MODIFIES: this
    // EFFECTS: renders a triangle with a flat bottom
    private void shadeTriangleFlatBottom(AbstractShader shader, Triangle flatBotTri, Triangle target) {
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
        endY = Math.min(endY, (float) bufferSize);

        if (!(Float.isFinite(startY) && Float.isFinite(endY))) {
            return; // sanity check
        }

        for (float drawY = startY; drawY <= endY; drawY += 1.0f) {
            float travelledY = drawY - flatBotTri.verts[2].getY();

            float startX = flatBotTri.verts[1].getX() + (travelledY * invSlopeLeftToTop);
            startX = Math.max(0, startX);

            float endX = flatBotTri.verts[2].getX() + (travelledY * invSlopeRightToTop);
            endX = Math.min(endX, (float) bufferSize);

            if (!(Float.isFinite(startX) && Float.isFinite(endX))) {
                return; // sanity check
            }

            for (float drawX = startX; drawX <= endX; drawX += 1.0f) {
                prepareAndDrawFragment(shader, new Vector3(drawX, drawY, 0.0f), target);
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: interpolates the input values for the given fragment based on the
    // current fragment's position with respect to the original triangle, and the
    // draws the fragment
    private void prepareAndDrawFragment(AbstractShader shader, Vector3 fragPos, Triangle target) {
        Vector3 attribWeights = generateAttribWeightings(fragPos, target);

        float[] texUVals = { target.uvs[0].getX(), target.uvs[1].getX(), target.uvs[2].getX() };
        float texU = interpolateAttrib(texUVals, attribWeights, target);

        float[] texVVals = { target.uvs[0].getY(), target.uvs[1].getY(), target.uvs[2].getY() };
        float texV = interpolateAttrib(texVVals, attribWeights, target);

        fragPos = new Vector3(fragPos.getX(), fragPos.getY(), interpolateDepth(attribWeights, target));
        int color = shader.shade(attribWeights, new Vector3(texU, texV, 0.0f));
        if (color == AbstractShader.CULL_FRAGMENT) {
            return;
        }
        drawFragment(fragPos, 0xFF000000 | color);
    }

    // EFFECTS: interpolates the depth properly
    private float interpolateDepth(Vector3 weights, Triangle target) {
        float w0 = weights.getX() / target.verts[0].getZ();
        float w1 = weights.getY() / target.verts[1].getZ();
        float w2 = weights.getZ() / target.verts[2].getZ();
        return 1.0f / (w0 + w1 + w2);
    }

    // EFFECTS: interpolates an attbribute depth-correctly based on a given
    // weighting
    private float interpolateAttrib(float[] valArray, Vector3 weight, Triangle parent) {
        float w0 = weight.getX() / parent.verts[0].getZ();
        float w1 = weight.getY() / parent.verts[1].getZ();
        float w2 = weight.getZ() / parent.verts[2].getZ();
        return (valArray[0] * w0 + valArray[1] * w1 + valArray[2] * w2) / (w0 + w1 + w2);
    }

    // Refer to:
    // https://gamedev.stackexchange.com/questions/23743/whats-the-most-efficient-way-to-find-barycentric-coordinates
    // EFFECTS: generates a weighting for the vertex attributes of the target
    // triangle for this fragment
    private Vector3 generateAttribWeightings(Vector3 fragPos, Triangle target) {
        Vector3 vert0 = target.verts[0];
        Vector3 vert1 = target.verts[1];
        Vector3 vert2 = target.verts[2];
        float det = (vert1.getY() - vert2.getY()) * (vert0.getX() - vert2.getX())
                + (vert2.getX() - vert1.getX()) * (vert0.getY() - vert2.getY());
        float weight1 = (vert1.getY() - vert2.getY()) * (fragPos.getX() - vert2.getX())
                + (vert2.getX() - vert1.getX()) * (fragPos.getY() - vert2.getY());
        weight1 /= det;
        float weight2 = (vert2.getY() - vert0.getY()) * (fragPos.getX() - vert2.getX())
                + (vert0.getX() - vert2.getX()) * (fragPos.getY() - vert2.getY());
        weight2 /= det;
        float weight3 = 1.0f - weight2 - weight1;
        return new Vector3(weight1, weight2, weight3);
    }

    // MODIFIES: this
    // EFFECTS: renders a triangle with a flat top
    private void shadeTriangleFlatTop(AbstractShader shader, Triangle flatBotTri, Triangle target) {
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
        endY = Math.min(endY, (float) bufferSize);

        for (float drawY = startY; drawY <= endY; drawY += 1.0f) {
            float travelledY = drawY - flatBotTri.verts[2].getY();

            float startX = flatBotTri.verts[2].getX() + (travelledY * invSlopeBottomToLeft);
            startX = Math.max(0, startX);

            float endX = flatBotTri.verts[2].getX() + (travelledY * invSlopeBottomToRight);
            endX = Math.min(endX, (float) bufferSize);

            for (float drawX = startX; drawX <= endX; drawX += 1.0f) {
                prepareAndDrawFragment(shader, new Vector3(drawX, drawY, 0.0f), target);
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

    // EFFECTS: cuts up a given triangle such that none of its verticies go past the
    // clipping plane (this is highly non-trivial), and returns the generated
    // triangles for when it has been clipped
    private Triangle[] clipTriangle(Triangle tri) {
        ArrayList<Integer> vertsBehind = new ArrayList<>();
        ArrayList<Integer> vertsBefore = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            if (tri.verts[i].getZ() > CLIPPING_PLANE_DEPTH) {
                vertsBehind.add(i);
            } else {
                vertsBefore.add(i);
            }
        }
        if (vertsBehind.size() == 3) {
            return new Triangle[0]; // nothing to draw
        }
        if (vertsBehind.size() == 0) {
            return new Triangle[] { tri }; // original triangle was fine
        }
        if (vertsBehind.size() == 1) {
            return clipTriangleCase1(vertsBehind.get(0), vertsBefore.get(0), vertsBefore.get(1), tri);
        }
        if (vertsBehind.size() == 2) {
            return clipTriangleCase2(vertsBefore.get(0), vertsBehind.get(0), vertsBehind.get(1), tri);
        }

        // should never reach here
        throw new IllegalStateException();
    }

    // EFFECTS: clips a triangle in the case that only one of the inidices is behind
    // the clipping plane
    private Triangle[] clipTriangleCase1(int behind, int beforeL, int beforeR, Triangle original) {
        // interpolate between beforeL to behind
        float factorLToB = getClippingFactor(original.verts[beforeL], original.verts[behind]);
        Vector3 vertexLToB = interpolateVector3(original.verts[beforeL], original.verts[behind], factorLToB);
        Vector3 uvLToB = interpolateVector3(original.uvs[beforeL], original.uvs[behind], factorLToB);

        // interpolate between beforeR to behind
        float factorRToB = getClippingFactor(original.verts[beforeR], original.verts[behind]);
        Vector3 vertexRToB = interpolateVector3(original.verts[beforeR], original.verts[behind], factorRToB);
        Vector3 uvRToB = interpolateVector3(original.uvs[beforeR], original.uvs[behind], factorRToB);

        // generate two new triangles, (L, LtoB, RtoB) and (L, R, RtoB)
        Triangle triLeft = new Triangle();
        triLeft.verts[0] = new Vector3(original.verts[beforeL]);
        triLeft.uvs[0] = new Vector3(original.uvs[beforeL]);
        triLeft.verts[1] = new Vector3(vertexLToB);
        triLeft.uvs[1] = new Vector3(uvLToB);
        triLeft.verts[2] = new Vector3(vertexRToB);
        triLeft.uvs[2] = new Vector3(uvRToB);

        Triangle triRight = new Triangle();
        triRight.verts[0] = new Vector3(original.verts[beforeL]);
        triRight.uvs[0] = new Vector3(original.uvs[beforeL]);
        triRight.verts[1] = new Vector3(original.verts[beforeR]);
        triRight.uvs[1] = new Vector3(original.uvs[beforeR]);
        triRight.verts[2] = new Vector3(vertexRToB);
        triRight.uvs[2] = new Vector3(uvRToB);

        return new Triangle[] { triLeft, triRight };
    }

    // EFFECTS: clips a triangle in the case that two of the verticies are behind
    // the clipping plane
    private Triangle[] clipTriangleCase2(int before, int behindL, int behindR, Triangle original) {
        float factorBToL = getClippingFactor(original.verts[before], original.verts[behindL]);
        Vector3 vertexBToL = interpolateVector3(original.verts[before], original.verts[behindL], factorBToL);
        Vector3 uvBToL = interpolateVector3(original.uvs[before], original.uvs[behindL], factorBToL);

        float factorBToR = getClippingFactor(original.verts[before], original.verts[behindR]);
        Vector3 vertexBToR = interpolateVector3(original.verts[before], original.verts[behindR], factorBToR);
        Vector3 uvBToR = interpolateVector3(original.uvs[before], original.uvs[behindR], factorBToR);

        // construct a triangle (B, BtoL, BtoR)
        Triangle clipped = new Triangle();
        clipped.verts[0] = new Vector3(original.verts[before]);
        clipped.uvs[0] = new Vector3(original.uvs[before]);
        clipped.verts[1] = new Vector3(vertexBToL);
        clipped.uvs[1] = new Vector3(uvBToL);
        clipped.verts[2] = new Vector3(vertexBToR);
        clipped.uvs[2] = new Vector3(uvBToR);

        return new Triangle[] { clipped };
    }

    // REQUIRES: behind to be behind the clipping plane and before to be before the
    // clipping plane
    // EFFECTS: gets the interpolation factor from before to behind that places a
    // vertex right on the clipping plane
    private float getClippingFactor(Vector3 before, Vector3 behind) {
        return (CLIPPING_PLANE_DEPTH - before.getZ()) / (behind.getZ() - before.getZ());
    }

    // EFFECTS: interpolates a given vector3 from a to b based on an interpolation
    // factor
    private Vector3 interpolateVector3(Vector3 a, Vector3 b, float factor) {
        return Vector3.add(Vector3.multiply(a, 1.0f - factor), Vector3.multiply(b, factor));
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

    // MODIFIES: this
    // EFFECTS: draws a 3D line with the specified shader, with proper camera
    // clipping
    private void drawLine(AbstractShader shader, Vector3 from, Vector3 to) {
        // order from furthest to closest to camera
        if (from.getZ() > to.getZ()) {
            Vector3 temp = from;
            from = to;
            to = temp;
        }

        if (from.getZ() >= CLIPPING_PLANE_DEPTH && to.getZ() >= CLIPPING_PLANE_DEPTH) {
            return;
        }

        if (to.getZ() >= CLIPPING_PLANE_DEPTH) {
            float factor = getClippingFactor(from, to);
            to = interpolateVector3(from, to, factor);
        }

        Vector3 projectedFrom = projectVectorToScreenSpace(from);
        Vector3 projectedTo = projectVectorToScreenSpace(to);

        drawLineScreenspace(shader, projectedFrom, projectedTo);
    }

    // MODIFIES: this
    // EFFECTS: draws a 2D line to the framebuffer with the given shader
    private void drawLineScreenspace(AbstractShader shader, Vector3 from, Vector3 to) {
        Vector3 left = null;
        Vector3 right = null;
        if (from.getX() > to.getX()) {
            right = from;
            left = to;
        } else {
            left = from;
            right = to;
        }

        Vector3 top = null;
        Vector3 bottom = null;
        if (from.getY() > to.getY()) {
            top = from;
            bottom = to;
        } else {
            bottom = from;
            top = to;
        }
        drawLineScreenspaceXAxis(shader, left, right);
        drawLineScreenspaceYAxis(shader, bottom, top);
    }

    // REQUIRES: left.getX() < right.getX()
    // MODIFIES: this
    // EFFECTS: draws a 2D line to the framebuffer in screenspace, based on the x
    // axis
    private void drawLineScreenspaceXAxis(AbstractShader shader, Vector3 left, Vector3 right) {
        float deltaX = right.getX() - left.getX();
        float deltaY = right.getY() - left.getY();
        if (deltaX <= DRAWLINE_EPSILON) {
            return;
        }
        float slope = deltaY / deltaX;
        float startX = Math.max(0.0f, left.getX());
        float endX = Math.min(bufferSize - 1.0f, right.getX());
        for (float drawX = startX; drawX <= endX; drawX += 1.0f) {
            float drawY = left.getY() + (drawX - left.getX()) * slope;
            float interp = (drawX - left.getX()) / deltaX;
            float depth = left.getZ() * (1.0f - interp) + interp * right.getZ();
            drawFragment(new Vector3(drawX, drawY, depth), shader.shade(new Vector3(), new Vector3()));
        }
    }

    // REQUIRES: bottom.getY() < top.getY()
    // MODIFIES: this
    // EFFECTS: draws a 2D line to the framebuffer in screenspace, based on the x
    // axis
    private void drawLineScreenspaceYAxis(AbstractShader shader, Vector3 bottom, Vector3 top) {
        float deltaX = top.getX() - bottom.getX();
        float deltaY = top.getY() - bottom.getY();
        if (deltaY <= DRAWLINE_EPSILON) {
            return;
        }
        float invSlope = deltaX / deltaY;
        float startY = Math.max(0.0f, bottom.getY());
        float endY = Math.min(bufferSize - 1.0f, top.getY());
        for (float drawY = startY; drawY <= endY; drawY += 1.0f) {
            float drawX = bottom.getX() + (drawY - bottom.getY()) * invSlope;
            float interp = (drawY - bottom.getY()) / deltaY;
            float depth = bottom.getZ() * (1.0f - interp) + interp * top.getZ();
            drawFragment(new Vector3(drawX, drawY, depth), shader.shade(new Vector3(), new Vector3()));
        }
    }

    // MODIFIES: this
    // EFFECTS: draws a single pixel to the framebuffer using the specified color,
    // given that it passes the depth test
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

    // MODIFIES: this
    // EFFECTS: clears the framebuffer and depth buffer
    private void clearBuffers() {
        for (int i = 0; i < bufferSize * bufferSize; i++) {
            colorBuffer[i] = COLOR_CLEAR_VALUE;
            depthBuffer[i] = DEPTH_CLEAR_VALUE;
        }
    }

    // EFFECTS: returns the 1D index for the framebuffer and depth buffer given a 2D
    // (x,y) coordinate
    private int getBufferIndex(int x, int y) {
        return x + (bufferSize * (bufferSize - 1 - y));
    }
}
