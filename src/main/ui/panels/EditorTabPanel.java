package ui.panels;

import model.*;
import ui.Tickable;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

// Tab panel which is used to cycle through different lists of objects
public class EditorTabPanel extends JTabbedPane implements Tickable {
    private static final String PLANET_LIST_NAME = "Planet List";
    private static final String COLLISION_LIST_NAME = "Collision List";
    private static final String SAVE_LIST_NAME = "Saved Simulations";

    private PlanetListPanel planetListPanel;
    private CollisionListPanel collisionListPanel;
    private JPanel saveListPanel;

    public EditorTabPanel() {
        planetListPanel = new PlanetListPanel();
        collisionListPanel = new CollisionListPanel();
        // TODO: implement colListPanel and saveListPanel
        addTab(PLANET_LIST_NAME, planetListPanel);
        addTab(COLLISION_LIST_NAME, collisionListPanel);
        addTab(SAVE_LIST_NAME, new JPanel());

        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    public PlanetListPanel getPlanetListPanel() {
        return planetListPanel;
    }

    // TODO: write other getters

    // MODIFIES: this
    // EFFECTS: updates this and relevant sub-components
    @Override
    public void tick() {
        planetListPanel.tick();
        collisionListPanel.tick();
    }
}