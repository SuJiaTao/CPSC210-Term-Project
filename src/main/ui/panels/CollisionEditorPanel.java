package ui.panels;

import model.*;
import java.awt.*;
import ui.Tickable;
import javax.swing.*;
import ui.SimulatorUtils;

// JPanel which contains all the UI for the collision list information viewer
public class CollisionEditorPanel extends JPanel implements Tickable {
    private CollisionListPanel parent;
    private JTextField planetsInvolvedLabel;
    private JTextField collisionTimeLabel;

    // EFFECTS: initializes all UI elements
    public CollisionEditorPanel(CollisionListPanel parent) {
        super(new BorderLayout());
        this.parent = parent;

        JLabel editorTitleLabel = SimulatorUtils.makeTitleLabel("Collision Info");
        add(editorTitleLabel, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridBagLayout());

        planetsInvolvedLabel = SimulatorUtils.initAndAddPropertyEditField(infoPanel, null, "Planets Involved:", 0);
        collisionTimeLabel = SimulatorUtils.initAndAddPropertyEditField(infoPanel, null, "Collision Time:", 1);
        planetsInvolvedLabel.setEditable(false);
        collisionTimeLabel.setEditable(false);

        add(infoPanel, BorderLayout.CENTER);
    }

    // MODIFIES: this
    // EFFECTS: updates all UI elements to reflect the information for the currently
    // selected collision
    @Override
    public void tick() {
        Collision selectedCol = parent.getSwingList().getSelectedValue();
        if (selectedCol == null) {
            planetsInvolvedLabel.setText("");
            collisionTimeLabel.setText("");
        } else {
            Planet planet1 = selectedCol.getPlanetsInvolved().get(0);
            Planet planet2 = selectedCol.getPlanetsInvolved().get(1);
            planetsInvolvedLabel.setText(planet1.getName() + ", " + planet2.getName());
            collisionTimeLabel.setText("" + selectedCol.getCollisionTime() + "s");
        }
    }
}
