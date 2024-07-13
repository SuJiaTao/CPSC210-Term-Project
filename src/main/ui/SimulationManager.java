package ui;

import java.awt.Dimension;
import java.util.*;
import com.googlecode.lanterna.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.*;

// Represents the current state of user-interface to managing simulations
public class SimulationManager {
    private static final int TERMINAL_WIDTH = 90;
    private static final int TERMINAL_HEIGHT = 35;
    private List<Simulation> simulations;
    private Screen screen;
    private KeyStroke lastUserKey;

    // EFFECTS: initializes all internal variables
    public SimulationManager() throws Exception {
        simulations = new ArrayList<>();

        DefaultTerminalFactory termFactory = new DefaultTerminalFactory();
        termFactory.setInitialTerminalSize(new TerminalSize(TERMINAL_WIDTH, TERMINAL_HEIGHT));
        screen = termFactory.createScreen();
        ensureDesiredTerminalSize();

    }

    // EFFECTS: throws an exception if failed to construct screen of desired size
    private void ensureDesiredTerminalSize() throws Exception {
        TerminalSize termSize = screen.getTerminalSize();
        if (termSize.getRows() != TERMINAL_WIDTH || termSize.getColumns() != TERMINAL_HEIGHT) {
            String formatStr = "Failed to create terminal of desired size: (%d, %d)";
            String errMessage = String.format(formatStr, TERMINAL_WIDTH, TERMINAL_HEIGHT);
            throw new Exception(errMessage);
        }
    }

    // MODIFIES: this
    // EFFECTS: execute main input/rendering loop
    public void mainLoop() throws Exception {
        while (true) {
            drawUiGraphics();
        }
    }

    // MODIFIES: this
    // EFFECTS: draw all UI graphics
    public void drawUiGraphics() throws Exception {
        screen.clear();
        screen.setCursorPosition(null);
        screen.refresh();
    }

}
