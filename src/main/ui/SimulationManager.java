package ui;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

// Represents the current state of user-interface to managing simulations
public class SimulationManager {
    private static final int TERMINAL_WIDTH = 90;
    private static final int TERMINAL_HEIGHT = 35;

    // Internal printstream to handle System.out/err since javaw has decided to
    // taketh it away from me );
    private class ConsoleRedirect extends PrintStream {
        private String stringToDisplay;

        // EFFECTS: redirects a given outputstream to super, enable always flush
        public ConsoleRedirect(OutputStream outputStream) {
            // NOTE:
            // i can't lie, I am not familiar enough with the JDK's stream related classes
            // and methods to completely understand what this constructor is doing, but
            // making any modification to it will cause the program to crash, so I will opt
            // to keep it like this >.<
            super(outputStream, true);
            stringToDisplay = "null";
        }

        public String getStringToDisplay() {
            return stringToDisplay;
        }

        // EFFECTS: updates latest display string
        @Override
        public void print(String toPrint) {
            internalPrint(toPrint);
        }

        // EFFECTS: updates latest display string
        @Override
        public void println(String toPrint) {
            internalPrint(toPrint);
        }

        // EFFECTS: updates latest display string
        @Override
        public void print(Object toPrint) {
            internalPrint(toPrint.toString());
        }

        // EFFECTS: updates latest display string
        private void internalPrint(String toPrint) {
            stringToDisplay = toPrint;
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
    private static final String MENU_BROWSE_PLANETS = "BrowsePlanets";
    private static final String MENU_EDIT_PLANET = "EditPlanet";
    private static final String MENU_ADD_PLANET = "AddPlanet";
    private String menuState;

    // SIMULATION VIEW VARIABLES

    // EFFECTS: initialize simulation, init graphical/user input, redirect
    // sterr+stdout, and set simulation state to the opening screen
    public SimulationManager() throws Exception {
        simulation = new Simulation();

        errRedirect = new ConsoleRedirect(System.err);
        System.setErr(errRedirect);
        outRedirect = new ConsoleRedirect(System.out);
        System.setOut(outRedirect);

        DefaultTerminalFactory termFactory = new DefaultTerminalFactory();
        termFactory.setInitialTerminalSize(new TerminalSize(TERMINAL_WIDTH, TERMINAL_HEIGHT));
        screen = termFactory.createScreen();

        ensureDesiredTerminalSize();
        screen.startScreen();

        simulationState = SIM_STATE_OPENING_SCREEN;
        menuState = MENU_BROWSE_PLANETS;
    }

    // EFFECTS: prints an error to sterr if failed to construct screen of desired
    // size
    private void ensureDesiredTerminalSize() throws Exception {
        TerminalSize termSize = screen.getTerminalSize();
        int widthActual = termSize.getColumns();
        int heightActual = termSize.getRows();
        if (heightActual != TERMINAL_HEIGHT || widthActual != TERMINAL_WIDTH) {
            String formatStr = "Failed to create terminal of desired size: (%d, %d), instead got (%d %d)";
            String errMessage = String.format(formatStr, TERMINAL_WIDTH, TERMINAL_HEIGHT, widthActual, heightActual);
            System.err.print(errMessage);
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

            long startWaitTime = System.nanoTime();
            spinWaitMiliseconds(15);
            long endWaitTime = System.nanoTime();
            System.out.println("deltaTime: " + (endWaitTime - startWaitTime) / 1000);
        }
    }

    public void drawErrAndOut() {
        TextGraphics textWriter = screen.newTextGraphics();
        textWriter.setForegroundColor(TextColor.ANSI.WHITE);
        textWriter.putString(0, TERMINAL_HEIGHT - 2, "out: " + outRedirect.getStringToDisplay());
        textWriter.setForegroundColor(TextColor.ANSI.RED);
        textWriter.putString(0, TERMINAL_HEIGHT - 1, "err: " + errRedirect.getStringToDisplay());
    }

    // EFFECTS: waits for miliseconds via spin
    public void spinWaitMiliseconds(int waitMilliseconds) {
        // NOTE: hi, you are probably wondering why I'm not using Thread.sleep()
        // right now. The issue with Thread.Sleep is that internally it is generally not
        // precice enough. At least on windows, Thread.sleep() has a granularity of
        // about ~16msec which really isn't good enough for my purposes. The only way to
        // do this the way I want is to go into a spin so here it is :3
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
                System.exit(0);
                break;
            default:
                System.err.println("entered unknown simulation state");
        }
    }

    // MODIFIES: this
    // EFFECTS: handles ui/input logic for opening screen
    public void handleOpeningScreenTick() {
        drawOpeningScreenGraphic();

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
    // EFFECTS: draws opening screen graphic to terminal
    public void drawOpeningScreenGraphic() {
        TextGraphics graphics = screen.newTextGraphics();
        graphics.setForegroundColor(TextColor.ANSI.WHITE);
        graphics.drawRectangle(new TerminalPosition(2, 1), new TerminalSize(50, 10), '+');
        graphics.putString(5, 3, "UI Controls:");
        graphics.putString(7, 4, "- Press Space to change selection");
        graphics.putString(7, 5, "- Press Enter to confirm selection");
        graphics.putString(7, 6, "- Press Escape to go back");
        graphics.putString(7, 7, "- Press Q to quit");
        graphics.putString(5, 8, "Press Space to continue");
    }

    // MODIFIES: this
    // EFFECTS: handles ui/input logic for menu
    public void handleMenuTick() {
        drawMenuGraphic();
    }

    // MODIFIES: this
    // EFFECTS: draws the menu graphic to terminal
    public void drawMenuGraphic() {
        TextGraphics graphics = screen.newTextGraphics();
        graphics.setForegroundColor(TextColor.ANSI.WHITE);
        drawPlanetViewer(graphics);
    }

    public void drawPlanetViewer(TextGraphics graphics) {
        graphics.drawRectangle(new TerminalPosition(1, 1), new TerminalSize(32, TERMINAL_HEIGHT - 5), '+');
        graphics.putString(new TerminalPosition(4, 2), "Planet List");
        graphics.drawLine(1, 3, 32, 3, '+');
    }

    // MODIFIES: this
    // EFFECTS: handles ui/input logic for simulation viewer
    public void handleSimViewTick() {

    }
}
