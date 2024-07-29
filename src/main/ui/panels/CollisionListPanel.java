package ui.panels;

import model.*;
import ui.SimulatorState;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class CollisionListPanel extends AbstractListPanel<Collision> {
    private CollisionEditorPanel collisionEditorPanel;

    public CollisionListPanel() {
        super(SimulatorState.getInstance().getSimulation().getCollisions());
    }

    // EFFECTS: returns the PlanetEditorPanel class
    @Override
    protected JPanel initEditorPanel() {
        collisionEditorPanel = new CollisionEditorPanel();
        return collisionEditorPanel;
    }
}
