package ui.panels;

import model.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

// Planet list view panel which is used to view and edit planets
public class PlanetListPanel extends ListEditorPanel<Planet> {
    // EFFECTS: constructs the list editor with the SimulationManager's planet list
    public PlanetListPanel() {
        super(simState.getSimulation().getPlanets());
    }

    // Planet editor panel used to edit plant properties
    // NOTE: woaw!!! 2-levels of class nesting?? probably bad design
    private class PlanetEditorPanel extends JPanel implements ActionListener {
        private JTextField nameEditField;
        private JTextField posEditField;
        private JTextField velEditField;
        private JTextField radEditField;

        public PlanetEditorPanel() {
            super(new GridBagLayout());

            JLabel editorTitleLabel = new JLabel("Edit Planet");
            editorTitleLabel.setHorizontalAlignment(JLabel.CENTER);
            editorTitleLabel.setFont(new Font(editorTitleLabel.getFont().getName(),
                    Font.BOLD, 15));

            add(editorTitleLabel, createConstraints(0, 0, 3));

            add(new JLabel("Name: ", JLabel.RIGHT), createConstraints(0, 1, 1));
            nameEditField = createTextField();
            add(nameEditField, createConstraints(1, 1, 2));

            add(new JLabel("Position: ", JLabel.RIGHT), createConstraints(0, 2, 1));
            posEditField = createTextField();
            add(posEditField, createConstraints(1, 2, 2));

            add(new JLabel("Velocity: ", JLabel.RIGHT), createConstraints(0, 3, 1));
            velEditField = createTextField();
            add(velEditField, createConstraints(1, 3, 2));

            add(new JLabel("Radius: ", JLabel.RIGHT), createConstraints(0, 4, 1));
            radEditField = createTextField();
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

        // EFFECTS: handles when the user submits something to a text field
        public void actionPerformed(ActionEvent actionEvent) {
            if (!(actionEvent.getSource() instanceof JTextField)) {
                return;
            }

            JTextField sourceField = (JTextField) actionEvent.getSource();
            if (sourceField == nameEditField) {

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
    }

    @Override
    protected JComponent initEditorPanel() {
        return new PlanetEditorPanel();
    }

    @Override
    protected String[] convertListToStrings() {
        String[] names = new String[objList.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = objList.get(i).getName();
        }
        return names;
    }

}