package ui.engine;

import model.*;
import java.util.*;

import ui.SimulatorState;
import ui.Tickable;
import java.awt.event.*;

// The virtural camera controller for the 3D viewer
public class CameraController implements Tickable, KeyListener, MouseListener {
    private static final Vector3 INITIAL_POSTION = new Vector3(0, 0, 30.0f);

    private static final float MAX_VELOCITY = 300.0f;
    private static final float MAX_VELOCITY_SHIFT_FACTOR = 7.5f;
    private static final float MAX_VELOCITY_CTRL_FACTOR = 0.15f;
    private static final float ACCELERATION = 1000.0f;
    private static final float ACCELERATION_SHIFT_FACTOR = 10.0f;
    private static final float ACCELERATION_CTRL_FACTOR = 0.2f;
    private static final float DRAG = 0.97f;

    private static final float CAMERA_DECOLLIDE_FACTOR = 1.1f;

    private static final float MAX_ANGULAR_VELOCITY = 90.0f;
    private static final float ANGULAR_ACCELERATION = 700.0f;
    private static final float ANGULAR_DRAG = 0.98f;

    private static final float PITCH_RANGE = 85.0f;

    private static final float PLANET_JUMP_PULLBACK_FACTOR = 5.0f;

    private RenderEngine parent;

    private Set<Integer> keysDown;

    private long lastTickNanoseconds;
    private Vector3 position;
    private Vector3 velocity;

    private float yaw;
    private float yawVelocity;
    private float pitch;
    private float pitchVelocity;

    private Transform viewTransform;

    // EFFECTS: initializes self to listen for user inputs and initializes self to
    // default camera coordinates
    public CameraController(RenderEngine parent) {
        this.parent = parent;
        this.parent.getPanel().addKeyListener(this);
        this.parent.getPanel().addMouseListener(this);

        keysDown = new HashSet<>();
        resetCamera();

        lastTickNanoseconds = System.nanoTime();
    }

    // MODIFIES: this
    // EFFECTS: initializes camera positioning related parameters to their default
    // values
    public void resetCamera() {
        position = new Vector3(INITIAL_POSTION);
        velocity = new Vector3();
        yaw = 0.0f;
        yawVelocity = 0.0f;
        pitch = 0.0f;
        pitchVelocity = 0.0f;
        viewTransform = new Transform();
    }

    // EFFECTS: ignores
    @Override
    public void keyTyped(KeyEvent e) {
        // do nothing
    }

    // MODIFIES: this
    // EFFECTS: adds the keys pressed to the current set of keys down
    @Override
    public void keyPressed(KeyEvent e) {
        keysDown.add(e.getKeyCode());
    }

    // MODIFIES: this
    // EFFECTS: removes the key reelased from the current set of keys down
    @Override
    public void keyReleased(KeyEvent e) {
        keysDown.remove(Integer.valueOf(e.getKeyCode()));
    }

    // REQUIRES: planet is NOT null
    // MODIFIES: this
    // EFFECTS: jumps camera to be looking directly towards the target planet
    public void jumpToPlanet(Planet planet) {
        Transform cameraRotation = Transform.multiply(Transform.rotationX(pitch), Transform.rotationY(yaw));
        Vector3 pullbackPos = Transform.multiply(cameraRotation,
                new Vector3(0.0f, 0.0f, planet.getRadius() * PLANET_JUMP_PULLBACK_FACTOR));
        position = Vector3.add(planet.getPosition(), pullbackPos);
    }

    // MODIFES: this
    // EFFECTS: updates all camera inputs and de-intersects camera with all planets
    @Override
    public void tick() {
        long deltaTimeNanoseconds = System.nanoTime() - lastTickNanoseconds;
        float deltaTimeSeconds = (float) deltaTimeNanoseconds / 1000000000.0f;

        // NOTE:
        // key release callbacks will not go through if the panel suddenly loses focus
        if (!parent.getPanel().isFocusOwner()) {
            keysDown.clear();
        }

        handleInputs(deltaTimeSeconds);

        Transform velRotation = Transform.multiply(Transform.rotationX(pitch), Transform.rotationY(yaw));
        float maxVelActual = MAX_VELOCITY;
        if (keysDown.contains(KeyEvent.VK_SHIFT)) {
            maxVelActual *= MAX_VELOCITY_SHIFT_FACTOR;
        }
        if (keysDown.contains(KeyEvent.VK_CONTROL)) {
            maxVelActual *= MAX_VELOCITY_CTRL_FACTOR;
        }
        velocity = clampVector(velocity, maxVelActual);

        velocity = Vector3.multiply(velocity, (float) Math.pow((1.0f - DRAG), deltaTimeSeconds));
        Vector3 velActual = Transform.multiply(velRotation, velocity);
        position = Vector3.add(position, Vector3.multiply(velActual, deltaTimeSeconds));

        handleCameraCollisions();

        yawVelocity = Math.max(Math.min(yawVelocity, MAX_ANGULAR_VELOCITY), -MAX_ANGULAR_VELOCITY);
        yaw += yawVelocity * deltaTimeSeconds;
        yawVelocity *= Math.pow((1.0f - ANGULAR_DRAG), deltaTimeSeconds);

        pitchVelocity = Math.max(Math.min(pitchVelocity, MAX_ANGULAR_VELOCITY), -MAX_ANGULAR_VELOCITY);
        pitch += pitchVelocity * deltaTimeSeconds;
        pitch = Math.max(Math.min(pitch, PITCH_RANGE), -PITCH_RANGE);
        pitchVelocity *= Math.pow((1.0f - ANGULAR_DRAG), deltaTimeSeconds);

        viewTransform = Transform.translation(Vector3.multiply(position, -1.0f));
        viewTransform = Transform.multiply(viewTransform, Transform.rotationY(-yaw));
        viewTransform = Transform.multiply(viewTransform, Transform.rotationX(-pitch));
        parent.setViewTransform(viewTransform);

        lastTickNanoseconds = System.nanoTime();
    }

    // MODIFIES: this
    // EFFECTS: de-intersects the camera with all planets
    private void handleCameraCollisions() {
        SimulatorState simState = SimulatorState.getInstance();
        simState.lock();

        for (Planet planet : simState.getSimulation().getPlanets()) {
            Vector3 displacement = Vector3.sub(position, planet.getPosition());
            float distance = displacement.magnitude();
            float boundRadius = Math.abs(RenderEngine.CLIPPING_PLANE_DEPTH * CAMERA_DECOLLIDE_FACTOR)
                    + planet.getRadius();
            if (distance < boundRadius) {
                float pushbackDist = boundRadius - distance;
                Vector3 pushbackVector = Vector3.multiply(Vector3.normalize(displacement), pushbackDist);
                position = Vector3.add(position, pushbackVector);
            }
        }

        simState.unlock();
    }

    // MODIFIES: this
    // EFFECTS: handles the camera velocity/angular velocity based on the user
    // inputs, synchronized with the keys currently down
    private void handleInputs(float deltaTime) {
        synchronized (keysDown) {
            float accelActual = ACCELERATION;
            if (keysDown.contains(KeyEvent.VK_SHIFT)) {
                accelActual *= ACCELERATION_SHIFT_FACTOR;
            }
            if (keysDown.contains(KeyEvent.VK_CONTROL)) {
                accelActual *= ACCELERATION_CTRL_FACTOR;
            }
            if (keysDown.contains(KeyEvent.VK_W)) {
                velocity = Vector3.add(velocity, new Vector3(0, 0, -accelActual * deltaTime));
            }
            if (keysDown.contains(KeyEvent.VK_S)) {
                velocity = Vector3.add(velocity, new Vector3(0, 0, accelActual * deltaTime));
            }
            if (keysDown.contains(KeyEvent.VK_A)) {
                velocity = Vector3.add(velocity, new Vector3(-accelActual * deltaTime, 0, 0));
            }
            if (keysDown.contains(KeyEvent.VK_D)) {
                velocity = Vector3.add(velocity, new Vector3(accelActual * deltaTime, 0, 0));
            }

            if (keysDown.contains(KeyEvent.VK_LEFT)) {
                yawVelocity -= ANGULAR_ACCELERATION * deltaTime;
            }
            if (keysDown.contains(KeyEvent.VK_RIGHT)) {
                yawVelocity += ANGULAR_ACCELERATION * deltaTime;
            }

            if (keysDown.contains(KeyEvent.VK_UP)) {
                pitchVelocity -= ANGULAR_ACCELERATION * deltaTime;
            }
            if (keysDown.contains(KeyEvent.VK_DOWN)) {
                pitchVelocity += ANGULAR_ACCELERATION * deltaTime;
            }
        }
    }

    // REQUIRES: bound >= 0
    // EFFECTS: clamps a vector's magnitude to the given bound while preserving its
    // direction
    private static Vector3 clampVector(Vector3 original, float bound) {
        if (original.magnitude() < bound) {
            return original;
        }
        return Vector3.multiply(Vector3.normalize(original), bound);
    }

    // MODIFIES: this
    // EFFECTS: when the mouse is clicked on parent, requests the focus to parent
    @Override
    public void mouseClicked(MouseEvent e) {
        parent.getPanel().requestFocusInWindow();
    }

    // EFFECTS: ignored
    @Override
    public void mousePressed(MouseEvent e) {
        // ignore
    }

    // EFFECTS: ignored
    @Override
    public void mouseReleased(MouseEvent e) {
        // ignore
    }

    // EFFECTS: ignored
    @Override
    public void mouseEntered(MouseEvent e) {
        // ignore
    }

    // EFFECTS: ignored
    @Override
    public void mouseExited(MouseEvent e) {
        // ignore
    }

}
