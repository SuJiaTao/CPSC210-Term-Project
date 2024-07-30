package ui.panels;

import persistence.SimulationReadWriter;
import ui.SimulatorState;
import ui.SimulatorUtils;
import ui.Tickable;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import model.Simulation;

import java.util.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
        loadButton.addActionListener(this);
        infoPanel.add(loadButton, SimulatorUtils.makeGbConstraints(1, 1, 1));

        saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        infoPanel.add(saveButton, SimulatorUtils.makeGbConstraints(2, 1, 1));

        newButton = new JButton("Create New Save");
        newButton.addActionListener(this);
        infoPanel.add(newButton, SimulatorUtils.makeGbConstraints(1, 2, 2));

        deleteButton = new JButton("Delete Save");
        deleteButton.addActionListener(this);
        infoPanel.add(deleteButton, SimulatorUtils.makeGbConstraints(1, 3, 2));

        add(infoPanel, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == renameField) {
            handleRenameSave();
        }
        if (actionEvent.getSource() instanceof JButton) {
            handleButtonPressed((JButton) actionEvent.getSource());
        }
    }

    // MODIFIES: this
    // EFFECTS: handles all button combinations, locks the simulation state as it
    // directly modifies it
    private void handleButtonPressed(JButton source) {
        SimulatorState simState = SimulatorState.getInstance();
        String selectedSaveName = parent.swingList.getSelectedValue();

        simState.lock();

        if (source == loadButton) {
            simState.setIsRunning(false);
            try {
                Simulation loadedSim = SimulationReadWriter.readSimulation(selectedSaveName);
                SimulatorUtils.transferSimData(simState.getSimulation(), loadedSim);
            } catch (Exception exp) {
                // nothing we can really do
            }
        }

        if (source == saveButton) {
            handleSaveSimulation(simState, selectedSaveName);
        }

        if (source == deleteButton) {
            File toDeleteFile = SimulationReadWriter.fileFromFileTitle(selectedSaveName);
            toDeleteFile.delete();
        }

        if (source == newButton) {
            DateFormat dateFormat = new SimpleDateFormat("ddMMyy_HHmmssSS");
            String newSimName = "Sim_" + dateFormat.format(new Date());
            handleSaveSimulation(simState, newSimName);
        }

        simState.unlock();
    }

    // EFFECTS: ensure the simulation is paused while writing it to the specified
    // file location
    private void handleSaveSimulation(SimulatorState simState, String fileDest) {
        boolean wasRunning = simState.getIsRunning();
        simState.setIsRunning(false);
        try {
            SimulationReadWriter.writeSimulation(simState.getSimulation(), fileDest);
        } catch (Exception exp) {
            // not much we can do
        }
        simState.setIsRunning(wasRunning);
    }

    // MODIFIES: this
    // EFFECTS: handles renaming the simulation. renames the simulation if the name
    // is valid, and the name doesnt already exist
    private void handleRenameSave() {
        System.out.println("reached");
        String selectedSaveName = parent.swingList.getSelectedValue();
        String newSaveName = renameField.getText();

        if (!SimulatorUtils.checkIfValidName(newSaveName)) {
            return;
        }
        if (parent.swingList.getSelectedValuesList().contains(newSaveName)) {
            return;
        }

        File renamedFile = SimulationReadWriter.fileFromFileTitle(newSaveName);
        File oldFile = SimulationReadWriter.fileFromFileTitle(selectedSaveName);
        oldFile.renameTo(renamedFile);
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
            if (!renameField.hasFocus()) {
                renameField.setText(selectedSave);
            }
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
