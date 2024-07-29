package ui.panels;

import model.*;
import ui.SimulatorState;
import ui.SimulatorUtils;
import ui.Tickable;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// Viewport panel which is used to host the 3D view of the simulation
public class ViewportPanel extends JPanel implements ActionListener, Tickable {
    private JButton startButton;
    private JButton stopButton;
    private JButton resetButton;
    private JLabel timeElapsedLabel;

    public ViewportPanel() {
        setLayout(new FlowLayout());

        startButton = new JButton("Start");
        startButton.addActionListener(this);
        add(startButton);

        stopButton = new JButton("Stop");
        stopButton.addActionListener(this);
        add(stopButton);

        resetButton = new JButton("Reset");
        resetButton.addActionListener(this);
        add(resetButton);

        timeElapsedLabel = new JLabel();
        add(timeElapsedLabel);
    }

    // MODIFIES: this
    // EFFECTS: handles actionevents
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == startButton) {
            SimulatorState.getInstance().setIsRunning(true);
        }
        if (actionEvent.getSource() == stopButton) {
            SimulatorState.getInstance().setIsRunning(false);
        }
        if (actionEvent.getSource() == resetButton) {
            SimulatorState.getInstance().setIsRunning(false);
            Simulation newSim = new Simulation();
            SimulatorUtils.transferSimData(SimulatorState.getInstance().getSimulation(), newSim);
        }
    }

    // MODIFIES: this
    // EFFECTS: updates this and relevant sub-components
    @Override
    public void tick() {
        float timeElapsed = SimulatorState.getInstance().getSimulation().getTimeElapsed();
        timeElapsedLabel.setText(String.format("Time Elapsed: %03.3fs", timeElapsed));
    }
}
