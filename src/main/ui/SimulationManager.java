package ui;

import java.awt.Dimension;
import java.util.*;
import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.*;

// Represents the current state of user-interface to managing simulations
public class SimulationManager {
    private static final int TERMINAL_WIDTH = 90;
    private static final int TERMINAL_HEIGHT = 35;
    private static final String SIM_STATE_OPENING_SCREEN = "OpeningScreen";
    private static final String SIM_STATE_MENU = "Menu";
    private static final String SIM_STATE_VIEWSIM = "ViewSimulation";
    private static final String SIM_STATE_QUIT = "Quit";

    private List<Simulation> simulations;
    private Screen screen;
    private KeyStroke lastUserKey;
    private String simulationState;

    // EFFECTS: initialize simulation list, graphical/user input, and set simulation
    // state to the opening screen
    public SimulationManager() throws Exception {
        simulations = new ArrayList<>();

        DefaultTerminalFactory termFactory = new DefaultTerminalFactory();
        termFactory.setInitialTerminalSize(new TerminalSize(TERMINAL_WIDTH, TERMINAL_HEIGHT));
        screen = termFactory.createScreen();
        ensureDesiredTerminalSize();

        simulationState = SIM_STATE_OPENING_SCREEN;
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
        screen.startScreen();
        while (true) {
            screen.clear();
            lastUserKey = screen.pollInput();
            screen.setCursorPosition(new TerminalPosition(0, 0));

            handleSimulationState();

            screen.refresh();
            screen.setCursorPosition(new TerminalPosition(screen.getTerminalSize().getColumns() - 1, 0));
        }
    }

    // MODIFIES: this
    // EFFECTS: runs the appropriate handler function based on the simulation state
    public void handleSimulationState() throws Exception {
        switch (simulationState) {
            case SIM_STATE_OPENING_SCREEN:
                // handleOpeningScreenTick();
                // break;
            case SIM_STATE_MENU:
                // handleMenuTick();
                // break;
            case SIM_STATE_VIEWSIM:
                // handleSimViewTick();
                // break;
            case SIM_STATE_QUIT:
                // System.exit(0); // TODO: make less harsh
                // break;
            default:
                throw new Exception("Entered unknown simulationState: " + simulationState);
        }
    }

    // MODIFIES: this
    // EFFECTS: handles ui/input logic for opening screen
    public void handleOpeningScreenTick() {
        TextGraphics textWriter = screen.newTextGraphics();
        textWriter.setForegroundColor(TextColor.ANSI.WHITE);
        textWriter.putString(5, 3, "UI Controls:");
        textWriter.putString(7, 4, "B:");
    }

    // ODIFIES: his

    // EFFECTS: handles ui/input logic for menu
    public void handleMenuTick() {

    }

    // MODIFIES: this
    // EFFECTS: handles ui/input logic for simulation viewer
    public void handleSimViewTick() {

    }
}
