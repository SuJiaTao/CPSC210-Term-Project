package ui.engine;

import model.*;
import java.util.*;
import ui.Tickable;
import java.awt.event.*;

public class CameraController implements Tickable, KeyListener, MouseListener {
    private static final Vector3 INITIAL_POSTION = new Vector3(0, 0, 30.0f);

    private static final float MAX_VELOCITY = 15.0f;
    private static final float ACCELERATION = 75.0f;
    private static final float DRAG = 0.97f;

    private static final float MAX_ANGULAR_VELOCITY = 90.0f;
    private static final float ANGULAR_ACCELERATION = 700.0f;
    private static final float ANGULAR_DRAG = 0.98f;

    private static final float PITCH_RANGE = 85.0f;

    private RenderEngine parent;

    private java.util.List<Integer> keysDown;

    private long lastTickNanoseconds;
    private Vector3 position;
    private Vector3 velocity;

    private float yaw;
    private float yawVelocity;
    private float pitch;
    private float pitchVelocity;

    private Transform viewTransform;

    public CameraController(RenderEngine parent) {
        this.parent = parent;
        this.parent.getPanel().addKeyListener(this);
        this.parent.getPanel().addMouseListener(this);

        keysDown = new ArrayList<>();
        resetCamera();

        lastTickNanoseconds = System.nanoTime();
    }

    public void resetCamera() {
        position = new Vector3(INITIAL_POSTION);
        velocity = new Vector3();
        yaw = 0.0f;
        yawVelocity = 0.0f;
        pitch = 0.0f;
        pitchVelocity = 0.0f;
        viewTransform = new Transform();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // do nothing
    }

    @Override
    public void keyPressed(KeyEvent e) {
        synchronized (keysDown) {
            if (!keysDown.contains(e.getKeyCode())) {
                keysDown.add(e.getKeyCode());
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        synchronized (keysDown) {
            if (keysDown.contains(e.getKeyCode())) {
                keysDown.remove(Integer.valueOf(e.getKeyCode()));
            }
        }
    }

    @Override
    public void tick() {
        long deltaTimeNanoseconds = System.nanoTime() - lastTickNanoseconds;
        float deltaTimeSeconds = (float) deltaTimeNanoseconds / 1000000000.0f;

        handleInputs(deltaTimeSeconds);

        Transform velRotation = Transform.multiply(Transform.rotationX(pitch), Transform.rotationY(yaw));
        velocity = clampVector(velocity, MAX_VELOCITY);
        velocity = Vector3.multiply(velocity, (float) Math.pow((1.0f - DRAG), deltaTimeSeconds));
        Vector3 velActual = Transform.multiply(velRotation, velocity);
        position = Vector3.add(position, Vector3.multiply(velActual, deltaTimeSeconds));

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

    private void handleInputs(float deltaTime) {
        synchronized (keysDown) {
            if (keysDown.contains(KeyEvent.VK_W)) {
                velocity = Vector3.add(velocity, new Vector3(0, 0, -ACCELERATION * deltaTime));
            }
            if (keysDown.contains(KeyEvent.VK_S)) {
                velocity = Vector3.add(velocity, new Vector3(0, 0, ACCELERATION * deltaTime));
            }
            if (keysDown.contains(KeyEvent.VK_A)) {
                velocity = Vector3.add(velocity, new Vector3(-ACCELERATION * deltaTime, 0, 0));
            }
            if (keysDown.contains(KeyEvent.VK_D)) {
                velocity = Vector3.add(velocity, new Vector3(ACCELERATION * deltaTime, 0, 0));
            }

            if (keysDown.contains(KeyEvent.VK_LEFT)) {
                yawVelocity -= ANGULAR_ACCELERATION * deltaTime;
            }
            if (keysDown.contains(KeyEvent.VK_RIGHT)) {
                yawVelocity += ANGULAR_ACCELERATION * deltaTime;
            }

            if (keysDown.contains(KeyEvent.VK_UP)) {
                pitchVelocity += ANGULAR_ACCELERATION * deltaTime;
            }
            if (keysDown.contains(KeyEvent.VK_DOWN)) {
                pitchVelocity -= ANGULAR_ACCELERATION * deltaTime;
            }
        }
    }

    private static Vector3 clampVector(Vector3 original, float bound) {
        if (original.magnitude() < bound) {
            return original;
        }
        return Vector3.multiply(Vector3.normalize(original), bound);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        parent.getPanel().requestFocusInWindow();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // ignore
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // ignore
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // ignore
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // ignore
    }

}
