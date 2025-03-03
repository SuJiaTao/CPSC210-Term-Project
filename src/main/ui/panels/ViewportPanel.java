package ui.panels;

import ui.*;
import model.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import ui.engine.RenderEngine;

// Viewport panel which is used to host the 3D view of the simulation
public class ViewportPanel extends JPanel implements ActionListener, Tickable {
    private static final float SPLIT_WEIGHT_TOP = 0.0f;
    private static final float SPLIT_WEIGHT_BOTTOM = 1.0f;
    private static final int VIEWPORT_RESOLUTION = 350;

    private JButton startButton;
    private JButton stopButton;
    private JButton resetButton;
    private JButton resetCameraButton;
    private JLabel timeElapsedLabel;
    private JSlider timeScaleSlider;
    private RenderEngine renderEngine;

    // Represents the internal class which actually holds the viewport framebuffer
    private class ActualViewport extends JPanel {
        private ViewportPanel parent;

        // EFFECTS: allows the panel to be focusable
        public ActualViewport(ViewportPanel parent) {
            this.parent = parent;
            setFocusable(true);
            setRequestFocusEnabled(true);
        }

        // EFFECTS: paints the current frame of the render engine into itself
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            parent.renderEngine.drawCurrentFrame(g);
        }
    }

    ActualViewport viewport;

    // EFFECTS: initializes all UI elements and render engine
    public ViewportPanel() {
        setLayout(new BorderLayout());

        JPanel topSimControlPanel = new JPanel(new FlowLayout());
        startButton = new JButton("Start");
        startButton.addActionListener(this);
        topSimControlPanel.add(startButton);

        stopButton = new JButton("Stop");
        stopButton.addActionListener(this);
        topSimControlPanel.add(stopButton);

        resetButton = new JButton("Reset");
        resetButton.addActionListener(this);
        topSimControlPanel.add(resetButton);

        resetCameraButton = new JButton("Reset Camera");
        resetCameraButton.addActionListener(this);
        topSimControlPanel.add(resetCameraButton);

        timeElapsedLabel = new JLabel();
        topSimControlPanel.add(timeElapsedLabel);

        viewport = new ActualViewport(this);
        renderEngine = new RenderEngine(viewport, VIEWPORT_RESOLUTION);

        JSplitPane topSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSimControlPanel, viewport);
        topSplitter.setResizeWeight(SPLIT_WEIGHT_TOP);
        topSplitter.setEnabled(false);

        JPanel bottomSimControlPanel = new JPanel(new FlowLayout());

        JLabel timeScaleLabel = new JLabel("Simulation Timescale:");
        bottomSimControlPanel.add(timeScaleLabel);

        timeScaleSlider = new JSlider((int) SimulatorState.TIMESCALE_MIN, (int) SimulatorState.TIMESCALE_MAX,
                (int) SimulatorState.getInstance().getTimeScale());
        timeScaleSlider.setMajorTickSpacing(4);
        timeScaleSlider.setPaintTicks(true);
        timeScaleSlider.setPaintLabels(true);
        bottomSimControlPanel.add(timeScaleSlider);

        JSplitPane bottomSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplitter, bottomSimControlPanel);
        bottomSplitter.setResizeWeight(SPLIT_WEIGHT_BOTTOM);
        bottomSplitter.setEnabled(false);

        add(bottomSplitter);
    }

    public RenderEngine getRenderEngine() {
        return renderEngine;
    }

    // MODIFIES: this
    // EFFECTS: handles actionevents, locks simulation state as it directly modifies
    // it
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        SimulatorState.getInstance().lock();

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
        if (actionEvent.getSource() == resetCameraButton) {
            renderEngine.getCameraController().resetCamera();
        }

        SimulatorState.getInstance().unlock();
    }

    // MODIFIES: this
    // EFFECTS: updates this and relevant sub-components
    @Override
    public void tick() {
        handleButtonsUsability();

        SimulatorState simState = SimulatorState.getInstance();
        simState.setTimeScale(timeScaleSlider.getValue());
        float timeElapsed = simState.getSimulation().getTimeElapsed();
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
        boolean isRunning = SimulatorState.getInstance().getIsRunning();
        startButton.setEnabled(hasPlanets && !isRunning);
        stopButton.setEnabled(hasPlanets && isRunning);
    }
}
