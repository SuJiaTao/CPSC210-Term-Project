package ui.panels;

import ui.Tickable;
import javax.swing.*;

// Tab panel which is used to cycle through different lists of objects
public class EditorTabPanel extends JTabbedPane implements Tickable {
    private static final String PLANET_LIST_NAME = "Planet List";
    private static final String COLLISION_LIST_NAME = "Collision List";
    private static final String SAVE_LIST_NAME = "Saved Simulations";

    private PlanetListPanel planetListPanel;
    private CollisionListPanel collisionListPanel;
    private SavedListPanel savedListPanel;

    public EditorTabPanel() {
        planetListPanel = new PlanetListPanel();
        collisionListPanel = new CollisionListPanel();
        savedListPanel = new SavedListPanel();

        addTab(PLANET_LIST_NAME, planetListPanel);
        addTab(COLLISION_LIST_NAME, collisionListPanel);
        addTab(SAVE_LIST_NAME, savedListPanel);

        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    public PlanetListPanel getPlanetListPanel() {
        return planetListPanel;
    }

    // MODIFIES: this
    // EFFECTS: updates this and relevant sub-components
    @Override
    public void tick() {
        planetListPanel.tick();
        collisionListPanel.tick();
        savedListPanel.tick();
    }
}