package ui.panels;

import model.*;
import ui.SimulatorState;
import ui.SimulatorUtils;
import ui.Tickable;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class CollisionEditorPanel extends JPanel implements Tickable {
    private static final int TEXT_FIELD_COLUMNS = 20;

    private CollisionListPanel parent;
    private JTextField planetsInvolvedLabel;
    private JTextField collisionTimeLabel;

    public CollisionEditorPanel(CollisionListPanel parent) {
        super(new GridBagLayout());

        this.parent = parent;
        JLabel editorTitleLabel = new JLabel("Collision Info");
        editorTitleLabel.setHorizontalAlignment(JLabel.CENTER);
        editorTitleLabel.setFont(new Font(editorTitleLabel.getFont().getName(),
                Font.BOLD, 15));
        add(editorTitleLabel, createConstraints(0, 0, 3));

        add(new JLabel("Planets Involved:", JLabel.RIGHT), createConstraints(0, 1, 1));
        planetsInvolvedLabel = new JTextField(TEXT_FIELD_COLUMNS);
        planetsInvolvedLabel.setEditable(false);
        add(planetsInvolvedLabel, createConstraints(1, 1, 2));

        add(new JLabel("Time Occoured:", JLabel.RIGHT), createConstraints(0, 2, 1));
        collisionTimeLabel = new JTextField(TEXT_FIELD_COLUMNS);
        collisionTimeLabel.setEditable(false);
        add(collisionTimeLabel, createConstraints(1, 2, 2));
    }

    private GridBagConstraints createConstraints(int gx, int gy, int width) {
        GridBagConstraints gbConst = new GridBagConstraints();
        gbConst.fill = GridBagConstraints.BOTH;
        gbConst.gridx = gx;
        gbConst.gridy = gy;
        gbConst.weightx = width;
        gbConst.weightx = 0.5;
        gbConst.insets = new Insets(1, 10, 1, 10);
        return gbConst;
    }

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
