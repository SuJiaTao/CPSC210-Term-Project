package ui.panels;

import model.*;
import javax.swing.*;
import ui.SimulatorState;

// Planet list view panel which is used to view and edit planets
public class PlanetListPanel extends AbstractListPanel<Planet> {
    private PlanetEditorPanel planetEditorPanel;

    // EFFECTS: constructs the list editor with the SimulationManager's planet list
    public PlanetListPanel() {
        super(SimulatorState.getInstance().getSimulation().getPlanets());
    }

    // MODIFIES: this
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

    // EFFECTS: returns the currently selected planet
    public Planet getSelectedPlanet() {
        return super.swingList.getSelectedValue();
    }

}