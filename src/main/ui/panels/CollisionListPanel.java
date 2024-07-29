package ui.panels;

import model.*;
import ui.SimulatorState;
import javax.swing.*;

public class CollisionListPanel extends AbstractListPanel<Collision> {
    private CollisionEditorPanel collisionEditorPanel;

    public CollisionListPanel() {
        super(SimulatorState.getInstance().getSimulation().getCollisions());
    }

    // EFFECTS: returns the PlanetEditorPanel class
    @Override
    protected JPanel initEditorPanel() {
        collisionEditorPanel = new CollisionEditorPanel(this);
        return collisionEditorPanel;
    }

    @Override
    public void tick() {
        super.tick();
        collisionEditorPanel.tick();
    }
}
