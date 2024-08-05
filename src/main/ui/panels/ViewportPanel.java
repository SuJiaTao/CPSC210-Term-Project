package ui.panels;

import ui.*;
import model.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import ui.engine.RenderEngine;

// Viewport panel which is used to host the 3D view of the simulation
public class ViewportPanel extends JPanel implements ActionListener, Tickable {
    private static final float SPLIT_WEIGHT = 0.0f;
    private static final int VIEWPORT_RESOLUTION = 350;

    private JButton startButton;
    private JButton stopButton;
    private JButton resetButton;
    private JButton resetCameraButton;
    private JSlider timeScaleSlider;
    private JLabel timeElapsedLabel;
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

        JPanel simControlPanel = new JPanel(new FlowLayout());
        startButton = new JButton("Start");
        startButton.addActionListener(this);
        simControlPanel.add(startButton);

        stopButton = new JButton("Stop");
        stopButton.addActionListener(this);
        simControlPanel.add(stopButton);

        resetButton = new JButton("Reset");
        resetButton.addActionListener(this);
        simControlPanel.add(resetButton);

        resetCameraButton = new JButton("Reset Camera");
        resetCameraButton.addActionListener(this);
        simControlPanel.add(resetCameraButton);

        timeScaleSlider = new JSlider((int) SimulatorState.TIMESCALE_MIN, (int) SimulatorState.TIMESCALE_MAX,
                (int) SimulatorState.getInstance().getTimeScale());
        simControlPanel.add(timeScaleSlider);

        timeElapsedLabel = new JLabel();
        simControlPanel.add(timeElapsedLabel);

        viewport = new ActualViewport(this);
        renderEngine = new RenderEngine(viewport, VIEWPORT_RESOLUTION);

        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, simControlPanel, viewport);
        splitter.setResizeWeight(SPLIT_WEIGHT);
        splitter.setEnabled(false);

        add(splitter);
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
        startButton.setEnabled(hasPlanets);
        stopButton.setEnabled(hasPlanets);
    }
}
