package ui.panels;

import model.*;
import ui.SimulatorState;
import javax.swing.*;

// Planet list view panel which is used to view and edit planets
public class PlanetListPanel extends AbstractListPanel<Planet> {
    private PlanetEditorPanel planetEditorPanel;

    // EFFECTS: constructs the list editor with the SimulationManager's planet list
    public PlanetListPanel() {
        super(SimulatorState.getInstance().getSimulation().getPlanets());
    }

    // EFFECTS: returns the PlanetEditorPanel class
    @Override
    protected JPanel initEditorPanel() {
        planetEditorPanel = new PlanetEditorPanel(this);
        return planetEditorPanel;
    }

    // MODIFIES: this
    // EFFECTS: updates itself and all relevant sub-components
    public void tick() {
        super.tick();
        planetEditorPanel.tick();
    }

}