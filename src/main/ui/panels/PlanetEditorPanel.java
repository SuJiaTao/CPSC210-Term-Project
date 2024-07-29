package ui.panels;

import model.*;
import ui.SimulatorState;
import ui.Tickable;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

// Planet editor panel used to edit plant properties
public class PlanetEditorPanel extends JPanel implements ActionListener, Tickable {
    private static final int EDIT_FIELD_COLUMNS = 20;

    private PlanetListPanel parent;
    private JTextField nameEditField;
    private JTextField posEditField;
    private JTextField velEditField;
    private JTextField radEditField;

    public PlanetEditorPanel(PlanetListPanel parent) {
        super(new GridBagLayout());

        this.parent = parent;
        JLabel editorTitleLabel = new JLabel("Edit Planet");
        editorTitleLabel.setHorizontalAlignment(JLabel.CENTER);
        editorTitleLabel.setFont(new Font(editorTitleLabel.getFont().getName(),
                Font.BOLD, 15));

        add(editorTitleLabel, createConstraints(0, 0, 3));

        add(new JLabel("Name: ", JLabel.RIGHT), createConstraints(0, 1, 1));
        nameEditField = createTextField();
        nameEditField.addActionListener(this);
        add(nameEditField, createConstraints(1, 1, 2));

        add(new JLabel("Position: ", JLabel.RIGHT), createConstraints(0, 2, 1));
        posEditField = createTextField();
        posEditField.addActionListener(this);
        add(posEditField, createConstraints(1, 2, 2));

        add(new JLabel("Velocity: ", JLabel.RIGHT), createConstraints(0, 3, 1));
        velEditField = createTextField();
        velEditField.addActionListener(this);
        add(velEditField, createConstraints(1, 3, 2));

        add(new JLabel("Radius: ", JLabel.RIGHT), createConstraints(0, 4, 1));
        radEditField = createTextField();
        radEditField.addActionListener(this);
        add(radEditField, createConstraints(1, 4, 2));
    }

    public JTextField getNameEditField() {
        return nameEditField;
    }

    public JTextField getPosEditField() {
        return posEditField;
    }

    public JTextField getVelEditField() {
        return velEditField;
    }

    public JTextField getRadEditField() {
        return radEditField;
    }

    // MODIFIES: this
    // EFFECTS: handles when the user submits something to a text field
    public void actionPerformed(ActionEvent actionEvent) {
        // NOTE: ignore if nothing selected
        Planet selectedPlanet = parent.getSwingList().getSelectedValue();
        if (selectedPlanet == null) {
            return;
        }

        JTextField sourceField = (JTextField) actionEvent.getSource();
        if (sourceField == nameEditField) {

        }
    }

    // MODIFIES: this, SimulatorState instance
    // EFFECTS: handles renaming of currently selected planet name
    private void handleRenamePlanet(Planet planet, JTextField textField) {
        if (textField.getText().length() == 0 || textField.getText().charAt(0) == ' ') {
            textField.setText(planet.getName());
        } else {
            planet.setName(textField.getText());
        }
    }

    // EFFECTS: creates a JTextField with EDIT_FIELD_COLUMNS columns and adds itself
    // as a listener
    private JTextField createTextField() {
        JTextField field = new JTextField(EDIT_FIELD_COLUMNS);
        field.addActionListener(this);
        return field;
    }

    // EFFECTS: creates GridBagConstraints at the specific row and column, with the
    // specified with and with some padding around it
    private GridBagConstraints createConstraints(int gx, int gy, int width) {
        GridBagConstraints gbConst = new GridBagConstraints();
        gbConst.fill = GridBagConstraints.BOTH;
        gbConst.gridx = gx;
        gbConst.gridy = gy;
        gbConst.gridwidth = width;
        gbConst.weightx = 0.5;
        gbConst.insets = new Insets(1, 5, 1, 5);
        return gbConst;
    }

    // MODIFIES: this
    // EFFECTS: updates itself and all relevant sub-components
    public void tick() {
        SimulatorState simState = SimulatorState.getInstance();
        handleShouldFieldsBeEditable(simState);
    }

    // MODIFIES: this
    // EFFECTS: handles whether the editfields should be editable
    private void handleShouldFieldsBeEditable(SimulatorState simState) {
        boolean isNotRunning = !simState.getIsRunning();
        nameEditField.setEditable(isNotRunning);
        posEditField.setEditable(isNotRunning);
        velEditField.setEditable(isNotRunning);
        radEditField.setEditable(isNotRunning);
    }
}