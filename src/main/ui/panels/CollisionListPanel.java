package ui.panels;

import model.*;
import javax.swing.*;
import ui.SimulatorState;

// JPanel which contains all the UI for the list of collisions
public class CollisionListPanel extends AbstractListPanel<Collision> {
    private CollisionEditorPanel collisionEditorPanel;

    // EFFECTS: initializes the list to represent SimulatorState's collision list
    public CollisionListPanel() {
        super(SimulatorState.getInstance().getSimulation().getCollisions());
    }

    // EFFECTS: returns the PlanetEditorPanel class
    @Override
    protected JPanel initEditorPanel() {
        collisionEditorPanel = new CollisionEditorPanel(this);
        return collisionEditorPanel;
    }

    // MODIFIES: this
    // EFFECTS: updates the collision editor panel
    @Override
    public void tick() {
        super.tick();
        collisionEditorPanel.tick();
    }
}
