package ui.panels;

import model.*;
import ui.SimulatorState;
import ui.SimulatorUtils;
import ui.Tickable;
import ui.engine.RenderEngine;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// Viewport panel which is used to host the 3D view of the simulation
public class ViewportPanel extends JPanel implements ActionListener, Tickable {
    private static final float SPLIT_WEIGHT = 0.0f;
    private static final int VIEWPORT_RESOLUTION = 512;

    private JButton startButton;
    private JButton stopButton;
    private JButton resetButton;
    private JLabel timeElapsedLabel;
    private RenderEngine renderEngine;

    private class ActualViewport extends JPanel {
        private ViewportPanel parent;

        public ActualViewport(ViewportPanel parent) {
            this.parent = parent;
            setFocusable(true);
            setRequestFocusEnabled(true);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            parent.renderEngine.drawCurrentFrame(g);
        }
    }

    ActualViewport viewport;

    public ViewportPanel() {
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel(new FlowLayout());
        startButton = new JButton("Start");
        startButton.addActionListener(this);
        controlPanel.add(startButton);

        stopButton = new JButton("Stop");
        stopButton.addActionListener(this);
        controlPanel.add(stopButton);

        resetButton = new JButton("Reset");
        resetButton.addActionListener(this);
        controlPanel.add(resetButton);

        timeElapsedLabel = new JLabel();
        controlPanel.add(timeElapsedLabel);

        viewport = new ActualViewport(this);
        renderEngine = new RenderEngine(viewport, VIEWPORT_RESOLUTION);

        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, controlPanel, viewport);
        splitter.setResizeWeight(SPLIT_WEIGHT);
        splitter.setEnabled(false);

        add(splitter);
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
        handleButtonsUsability();

        float timeElapsed = SimulatorState.getInstance().getSimulation().getTimeElapsed();
        timeElapsedLabel.setText(String.format("Time Elapsed: %03.3fs", timeElapsed));

        renderEngine.tick();

        // FORCE viewport to update
        handleActualViewportBorderVisuals();
        viewport.repaint();
    }

    // MODIFIES: this
    // EFFECTS: handles the visual border behavior of the actual viewport based on
    // whether its focused or not
    private void handleActualViewportBorderVisuals() {
        if (viewport.isFocusOwner()) {
            viewport.setBorder(BorderFactory.createLoweredBevelBorder());
        } else {
            viewport.setBorder(BorderFactory.createRaisedBevelBorder());
        }
    }

    // MODIFIES: this
    // EFFECTS: handles whether the start, stop and reset buttons can be used
    private void handleButtonsUsability() {
        boolean hasPlanets = (SimulatorState.getInstance().getSimulation().getPlanets().size() > 0);
        startButton.setEnabled(hasPlanets);
        stopButton.setEnabled(hasPlanets);
    }
}
