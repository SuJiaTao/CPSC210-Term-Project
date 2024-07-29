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
    private static final int EDIT_FIELD_COLUMNS = 20;

    private PlanetListPanel parent;
    private JTextField nameEditField;
    private JTextField posEditField;
    private JTextField velEditField;
    private JTextField radEditField;
    private JButton addPlanetButton;
    private JButton removePlaneButton;

    public PlanetEditorPanel(PlanetListPanel parent) {
        super(new GridBagLayout());

        this.parent = parent;
        JLabel editorTitleLabel = new JLabel("Edit Planet");
        editorTitleLabel.setHorizontalAlignment(JLabel.CENTER);
        editorTitleLabel.setFont(new Font(editorTitleLabel.getFont().getName(),
                Font.BOLD, 15));

        add(editorTitleLabel, createConstraints(0, 0, 3));

        nameEditField = initAndAddPropertyEditField("Name: ", 1);
        posEditField = initAndAddPropertyEditField("Position: ", 2);
        velEditField = initAndAddPropertyEditField("Velocity: ", 3);
        radEditField = initAndAddPropertyEditField("Radius: ", 4);

        addPlanetButton = new JButton("Add New");
        addPlanetButton.addActionListener(this);
        add(addPlanetButton, createConstraints(1, 5, 1));

        removePlaneButton = new JButton("Remove");
        removePlaneButton.addActionListener(this);
        add(removePlaneButton, createConstraints(2, 5, 1));
    }

    // MODIFIES: this
    // EFFECTS: adds a label and textfield, returns the textfield
    public JTextField initAndAddPropertyEditField(String title, int height) {
        add(new JLabel(title, JLabel.RIGHT), createConstraints(0, height, 1));
        JTextField textField = new JTextField(EDIT_FIELD_COLUMNS);
        textField.addActionListener(this);
        add(textField, createConstraints(1, height, 2));
        return textField;

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
            if (SimulatorUtils.checkIfValidPlanetName(fieldSrc.getText())) {
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
        Planet selPlanet = parent.getSwingList().getSelectedValue();
        handleShouldFieldsBeEditable(simState, selPlanet);
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
    // EFFECTS: handles whether the editfields should be editable
    private void handleShouldFieldsBeEditable(SimulatorState simState, Planet selPlanet) {
        boolean isNotRunning = !simState.getIsRunning();
        boolean isPlanetSelected = (selPlanet != null);

        boolean canEdit = (isNotRunning && isPlanetSelected);

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