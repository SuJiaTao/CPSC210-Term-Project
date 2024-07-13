package ui;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

import javax.swing.JFrame;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.SwingTerminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;

// Represents the current state of user-interface to managing simulations
public class SimulationManager {
    private static final int TERMINAL_WIDTH = 90;
    private static final int TERMINAL_HEIGHT = 35;

    // Internal printstream to handle System.out/err since javaw has decided to
    // taketh it away from me );
    private class ConsoleRedirect extends PrintStream {
        private String stringToDisplay;

        public ConsoleRedirect(OutputStream outputStream) {
            super(outputStream, true);
            stringToDisplay = "null";
        }

        @Override
        public void print(String toPrint) {
            internalPrint(toPrint);
        }

        @Override
        public void println(String toPrint) {
            internalPrint(toPrint);
        }

        @Override
        public void print(Object toPrint) {
            internalPrint(toPrint.toString());
        }

        private void internalPrint(String toPrint) {
            stringToDisplay = toPrint;
        }

        public String getStringToDisplay() {
            return stringToDisplay;
        }
    }

    // NOTE:
    // this could be better substituted for an enum but we havent covered that yet
    // so I'm not using one
    private static final String SIM_STATE_OPENING_SCREEN = "OpeningScreen";
    private static final String SIM_STATE_MENU = "Menu";
    private static final String SIM_STATE_VIEWSIM = "ViewSimulation";
    private static final String SIM_STATE_QUIT = "Quit";

    private ConsoleRedirect errRedirect;
    private ConsoleRedirect outRedirect;
    private Simulation simulation;
    private TerminalScreen screen;
    private KeyStroke lastUserKey;
    private String simulationState;

    // MENU SELECT VARIABLES

    // SIMULATION VIEW VARIABLES

    // EFFECTS: initialize simulation, graphical/user input, and set simulation
    // state to the opening screen
    public SimulationManager() throws Exception {
        simulation = new Simulation();
        errRedirect = new ConsoleRedirect(System.err);
        System.setErr(errRedirect);
        outRedirect = new ConsoleRedirect(System.out);
        System.setOut(outRedirect);

        DefaultTerminalFactory termFactory = new DefaultTerminalFactory();
        termFactory.setInitialTerminalSize(new TerminalSize(TERMINAL_WIDTH, TERMINAL_HEIGHT));
        screen = termFactory.createScreen();

        // ensureDesiredTerminalSize(); // TODO: fix this mess :(
        screen.startScreen();

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
        while (true) {
            screen.clear();
            lastUserKey = screen.pollInput();

            screen.setCursorPosition(new TerminalPosition(0, 0));
            handleSimulationState();
            drawErrAndOut();

            screen.refresh();
            screen.setCursorPosition(new TerminalPosition(screen.getTerminalSize().getColumns() - 1, 0));

            spinlockWaitMiliseconds(15);
        }
    }

    public void drawErrAndOut() {
        TextGraphics textWriter = screen.newTextGraphics();
        textWriter.setForegroundColor(TextColor.ANSI.WHITE);
        textWriter.putString(0, screen.getTerminalSize().getRows() - 2, "sysout: " + outRedirect.getStringToDisplay());
        textWriter.setForegroundColor(TextColor.ANSI.RED);
        textWriter.putString(0, screen.getTerminalSize().getRows() - 1, "syserr: " + errRedirect.getStringToDisplay());
    }

    // EFFECTS: waits for miliseconds via spinlock
    public void spinlockWaitMiliseconds(int waitMilliseconds) {
        // NOTE: hi, you are probably wondering why I'm not using Thread.sleep()
        // right now the issue with thread.sleep is that internally it is generally not
        // precice enough at least on windows, Thread.sleep() has a granularity of about
        // ~16msec which really isn't good enough for my purposes. The only way to do
        // this the way I want is to go into a spinlock so here it is :3
        long startTime = System.nanoTime();
        while (((System.nanoTime() - startTime) / 1000) <= waitMilliseconds) {
            // wait
        }
    }

    // MODIFIES: this
    // EFFECTS: runs the appropriate handler function based on the simulation state
    public void handleSimulationState() throws Exception {
        switch (simulationState) {
            case SIM_STATE_OPENING_SCREEN:
                handleOpeningScreenTick();
                break;
            case SIM_STATE_MENU:
                handleMenuTick();
                break;
            case SIM_STATE_VIEWSIM:
                handleSimViewTick();
                break;
            case SIM_STATE_QUIT:
                System.exit(0); // TODO: make less harsh
                break;
            default:
                // throw new Exception("Entered unknown simulationState: " + simulationState);
        }
    }

    // MODIFIES: this
    // EFFECTS: handles ui/input logic for opening screen
    public void handleOpeningScreenTick() {
        TextGraphics textWriter = screen.newTextGraphics();
        drawOpeningScreenGraphic(textWriter);

        if (lastUserKey == null) {
            return;
        }

        Character input = lastUserKey.getCharacter();
        if (input == null) {
            return;
        }

        if (input == ' ') {
            simulationState = SIM_STATE_MENU;
        }
        if (Character.toLowerCase(input) == 'q') {
            simulationState = SIM_STATE_QUIT;
        }
    }

    // MODIFIES: this
    // EFFECTS: prints opening screen graphic to terminal
    public void drawOpeningScreenGraphic(TextGraphics textWriter) {
        textWriter.setForegroundColor(TextColor.ANSI.WHITE);
        textWriter.drawRectangle(new TerminalPosition(2, 1), new TerminalSize(50, 10), '+');
        textWriter.putString(5, 3, "UI Controls:");
        textWriter.putString(7, 4, "- Press Space to change selection");
        textWriter.putString(7, 5, "- Press Enter to confirm selection");
        textWriter.putString(7, 6, "- Press Escape to go back");
        textWriter.putString(7, 7, "- Press Q to quit");
        textWriter.putString(5, 8, "Press Space to continue");
    }

    // MODIFIES: this
    // EFFECTS: handles ui/input logic for menu
    public void handleMenuTick() {

    }

    // MODIFIES: this
    // EFFECTS: handles ui/input logic for simulation viewer
    public void handleSimViewTick() {

    }
}
