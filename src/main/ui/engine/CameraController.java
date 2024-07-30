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

public class CameraController implements Tickable, MouseListener, KeyListener {
    private static final Vector3 INITIAL_POSTION = new Vector3(0, 0, 30.0f);

    private RenderEngine parent;

    private java.util.List<Integer> keysDown;

    private Vector3 position;
    private Vector3 velocity;
    private Vector3 rotation;
    private Vector3 angularVelocity;

    public CameraController(RenderEngine parent) {
        this.parent = parent;
        this.parent.getPanel().addMouseListener(this);
        this.parent.getPanel().addKeyListener(this);

        keysDown = new ArrayList<>();

        position = new Vector3(INITIAL_POSTION);
        velocity = new Vector3();
        rotation = new Vector3();
        angularVelocity = new Vector3();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'keyTyped'");
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!keysDown.contains(e.getKeyCode())) {
            keysDown.add(e.getKeyCode());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (keysDown.contains(e.getKeyCode())) {
            keysDown.remove(e.getKeyCode());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseClicked'");
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mousePressed'");
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseReleased'");
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseEntered'");
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseExited'");
    }

    @Override
    public void tick() {

    }

}
