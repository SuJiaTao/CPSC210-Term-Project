package ui.panels;

import persistence.SimulationReadWriter;
import ui.SimulatorState;
import ui.SimulatorUtils;
import ui.Tickable;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import java.util.*;
import java.util.List;

public class SavedEditorPanel extends JPanel implements ActionListener, Tickable {
    private SavedListPanel parent;
    private JTextField renameField;
    private JButton loadButton;
    private JButton saveButton;
    private JButton newButton;
    private JButton deleteButton;

    public SavedEditorPanel(SavedListPanel parent) {
        this.parent = parent;
        setLayout(new BorderLayout());

        JLabel editorTitleLabel = SimulatorUtils.makeTitleLabel("Edit Save");
        add(editorTitleLabel, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridBagLayout());

        renameField = SimulatorUtils.initAndAddPropertyEditField(infoPanel, this, "Save Name:", 0);
        loadButton = new JButton("Load");
        infoPanel.add(loadButton, SimulatorUtils.makeGbConstraints(1, 1, 1));
        saveButton = new JButton("Save");
        infoPanel.add(saveButton, SimulatorUtils.makeGbConstraints(2, 1, 1));

        newButton = new JButton("Create New Save");
        infoPanel.add(newButton, SimulatorUtils.makeGbConstraints(1, 2, 2));
        deleteButton = new JButton("Delete Save");
        infoPanel.add(deleteButton, SimulatorUtils.makeGbConstraints(1, 3, 2));

        add(infoPanel, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == renameField) {
            handleRenameSave();
        }
        if (actionEvent.getSource() instanceof JButton) {

        }
    }

    // MODIFIES: this
    // EFFECTS: handles renaming the simulation. renames the simulation if the name
    // is valid, and the name doesnt already exist
    private void handleRenameSave() {
        String selectedSaveName = parent.swingList.getSelectedValue();
        if (!SimulatorUtils.checkIfValidName(selectedSaveName)) {
            return;
        }
        if (parent.swingList.getSelectedValuesList().contains(selectedSaveName)) {
            return;
        }

    }

    // MODIFIES: this
    // EFFECTS: updates self and all sub-components
    @Override
    public void tick() {
        String selectedSaveName = parent.swingList.getSelectedValue();
        handleShouldPanelsBeEditable(selectedSaveName);
        handleRenameFieldText(selectedSaveName);
    }

    // MODIFIES: this
    // EFFECTS: handles the text in the rename filed
    private void handleRenameFieldText(String selectedSave) {
        if (selectedSave == null) {
            renameField.setText("");
        } else {
            renameField.setText(selectedSave);
        }
    }

    // MODIFIES: this
    // EFFECTS: handles setting which buttons/fields can be edited
    private void handleShouldPanelsBeEditable(String selectedSave) {
        boolean hasSelected = (selectedSave != null);
        renameField.setEditable(hasSelected);
        loadButton.setEnabled(hasSelected);
        saveButton.setEnabled(hasSelected);
        deleteButton.setEnabled(hasSelected);
    }
}
