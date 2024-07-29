package ui.panels;

import model.*;
import ui.Tickable;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

// Viewport panel which is used to host the 3D view of the simulation
public class ViewportPanel extends JPanel implements Tickable {
    public ViewportPanel() {
        super();
    }

    // MODIFIES: this
    // EFFECTS: updates this and relevant sub-components
    @Override
    public void tick() {
        // stub
    }
}
