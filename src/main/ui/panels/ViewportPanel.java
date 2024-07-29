package ui.panels;

import model.*;
import ui.SimulatorState;
import ui.Tickable;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

// Viewport panel which is used to host the 3D view of the simulation
public class ViewportPanel extends JPanel implements ActionListener, Tickable {
    private static final String TOGGLE_BUTTON_TEXT_RUNNING = "Pause Simulation";
    private static final String TOGGLE_BUTTON_TEXT_PAUSED = "Unpause Simulation";

    private JButton toggleRunButton;

    public ViewportPanel() {
        setLayout(new FlowLayout());
        toggleRunButton = new JButton();
        toggleRunButton.addActionListener(this);
        add(toggleRunButton);
    }

    // MODIFIES: this
    // EFFECTS: handles actionevents
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == toggleRunButton) {
            SimulatorState.getInstance().setIsRunning(!SimulatorState.getInstance().getIsRunning());
        }
    }

    // MODIFIES: this
    // EFFECTS: updates this and relevant sub-components
    @Override
    public void tick() {
        if (SimulatorState.getInstance().getIsRunning()) {
            toggleRunButton.setText(TOGGLE_BUTTON_TEXT_RUNNING);
        } else {
            toggleRunButton.setText(TOGGLE_BUTTON_TEXT_PAUSED);
        }
    }
}
