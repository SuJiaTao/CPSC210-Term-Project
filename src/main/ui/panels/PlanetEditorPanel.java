package ui.panels;

import model.*;
import ui.SimulatorState;
import ui.SimulatorUtils;
import ui.Tickable;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// Planet editor panel used to edit plant properties
public class PlanetEditorPanel extends JPanel implements ActionListener, Tickable {
    private PlanetListPanel parent;
    private JTextField nameEditField;
    private JTextField posEditField;
    private JTextField velEditField;
    private JTextField radEditField;
    private JButton addPlanetButton;
    private JButton removePlaneButton;

    public PlanetEditorPanel(PlanetListPanel parent) {
        super(new BorderLayout());

        this.parent = parent;

        JLabel editorTitleLabel = SimulatorUtils.makeTitleLabel("Edit Planet");
        add(editorTitleLabel, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridBagLayout());

        nameEditField = SimulatorUtils.initAndAddPropertyEditField(infoPanel, this, "Name: ", 0);
        posEditField = SimulatorUtils.initAndAddPropertyEditField(infoPanel, this, "Position: ", 1);
        velEditField = SimulatorUtils.initAndAddPropertyEditField(infoPanel, this, "Velocity: ", 2);
        radEditField = SimulatorUtils.initAndAddPropertyEditField(infoPanel, this, "Radius: ", 3);

        addPlanetButton = new JButton("Add New");
        addPlanetButton.addActionListener(this);
        infoPanel.add(addPlanetButton, SimulatorUtils.makeGbConstraints(1, 5, 1));

        removePlaneButton = new JButton("Remove");
        removePlaneButton.addActionListener(this);
        infoPanel.add(removePlaneButton, SimulatorUtils.makeGbConstraints(2, 5, 1));

        add(infoPanel, BorderLayout.CENTER);
    }

    // MODIFIES: this
    // EFFECTS: handles all actionevents
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() instanceof JTextField) {
            handleTextFieldSubmit((JTextField) actionEvent.getSource());
        }

        if (actionEvent.getSource() instanceof JButton) {
            handleButtonPressed((JButton) actionEvent.getSource());
        }
    }

    // MODIFIES: this
    // EFFECTS: handles if something is submitted to a text field
    public void handleTextFieldSubmit(JTextField fieldSrc) {
        if (fieldSrc == nameEditField) {
            if (SimulatorUtils.checkIfValidName(fieldSrc.getText())) {
                getSelectedPlanet().setName(fieldSrc.getText());
            }
        }
        if (fieldSrc == posEditField) {
            Vector3 newPos = SimulatorUtils.tryParseVector3(fieldSrc.getText());
            if (newPos != null) {
                getSelectedPlanet().setPosition(newPos);
            }
        }
        if (fieldSrc == velEditField) {
            Vector3 newVel = SimulatorUtils.tryParseVector3(fieldSrc.getText());
            if (newVel != null) {
                getSelectedPlanet().setVelocity(newVel);
            }
        }
        if (fieldSrc == radEditField) {
            Float newRad = SimulatorUtils.tryParseFloat(fieldSrc.getText());
            if (newRad != null) {
                getSelectedPlanet().setRadius(newRad);
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: handles if a button was clicked
    public void handleButtonPressed(JButton buttonSrc) {
        if (buttonSrc == addPlanetButton) {
            Planet newPlanet = SimulatorUtils.createNewPlanet();
            SimulatorState.getInstance().getSimulation().addPlanet(newPlanet);
            parent.getSwingList().setSelectedValue(newPlanet, true);
        }
        if (buttonSrc == removePlaneButton && getSelectedPlanet() != null) {
            SimulatorState.getInstance().getSimulation().removePlanet(getSelectedPlanet());
            if (getSelectedPlanet() == null) {
                parent.getSwingList().setSelectedIndex(parent.getSwingList().getModel().getSize() - 1);
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: updates itself and all relevant sub-components
    public void tick() {
        SimulatorState simState = SimulatorState.getInstance();
        Planet selPlanet = parent.getSwingList().getSelectedValue();
        handleShouldPanelsBeEditable(simState, selPlanet);
        handleEditFieldText(simState, selPlanet);
    }

    // MODIFIES: this
    // EFFECTS: sets the text of each editing field
    private void handleEditFieldText(SimulatorState simState, Planet selPlanet) {
        if (selPlanet == null) {
            nameEditField.setText("");
            posEditField.setText("");
            velEditField.setText("");
            radEditField.setText("");
            return;
        }
        if (!nameEditField.isFocusOwner()) {
            nameEditField.setText(selPlanet.getName());
        }
        if (!posEditField.isFocusOwner()) {
            String posString = selPlanet.getPosition().toString();
            posEditField.setText(SimulatorUtils.convertVectorStringToParseable(posString));
        }
        if (!velEditField.isFocusOwner()) {
            String velString = selPlanet.getVelocity().toString();
            velEditField.setText(SimulatorUtils.convertVectorStringToParseable(velString));
        }
        if (!radEditField.isFocusOwner()) {
            radEditField.setText(Float.toString(selPlanet.getRadius()));
        }
    }

    // MODIFIES: this
    // EFFECTS: handles whether the editfields and buttons should be editable
    private void handleShouldPanelsBeEditable(SimulatorState simState, Planet selPlanet) {
        boolean isNotRunning = !simState.getIsRunning();
        boolean isPlanetSelected = (selPlanet != null);

        boolean canEdit = (isNotRunning && isPlanetSelected);

        removePlaneButton.setEnabled(canEdit);
        nameEditField.setEditable(canEdit);
        posEditField.setEditable(canEdit);
        velEditField.setEditable(canEdit);
        radEditField.setEditable(canEdit);
    }

    // MODIFIES: this
    // EFFECTS: gets the currently selected planet
    private Planet getSelectedPlanet() {
        return parent.getSwingList().getSelectedValue();
    }

}