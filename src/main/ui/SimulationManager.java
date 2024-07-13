package ui;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import model.*;

// Represents the current state of user-interface to managing simulations
public class SimulationManager {
    private static final int TERMINAL_WIDTH = 90;
    private static final int TERMINAL_HEIGHT = 35;
    private static final int FRAME_TIME_DELAY_MSEC = 15;
    private static final int EDITOR_TOP = 0;
    private static final int EDITOR_BOT = 32;
    private static final int EDITOR_LEFT = 0;
    private static final int EDITOR_RIGHT = 40;
    private static final int PLANETLIST_ENTIRES = 20;

    private ConsoleOutputRedirectStream errRedirect;
    private ConsoleOutputRedirectStream outRedirect;

    private TerminalScreen screen;
    private KeyStroke lastUserKey;
    private String simulationState;

    private Simulation simulation;
    private Planet selectedPlanet;

    // EFFECTS: initialize simulation, init graphical/user input, redirect
    // sterr+stdout, and set simulation state to the opening screen
    public SimulationManager() throws Exception {
        errRedirect = new ConsoleOutputRedirectStream(System.err);
        System.setErr(errRedirect);
        outRedirect = new ConsoleOutputRedirectStream(System.out);
        System.setOut(outRedirect);

        DefaultTerminalFactory termFactory = new DefaultTerminalFactory();
        termFactory.setInitialTerminalSize(new TerminalSize(TERMINAL_WIDTH, TERMINAL_HEIGHT));
        screen = termFactory.createScreen();

        checkIfObtainedDesiredTerminalSize();
        screen.startScreen();

        simulation = new Simulation();
        addAndSelectNewPlanet(); // simulation starts with ONE new planet
    }

    // EFFECTS: prints an error to stderr if failed to construct screen of desired
    // size
    private void checkIfObtainedDesiredTerminalSize() {
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
            try {
                handleSimulationState();
            } catch (Exception errMsg) {
                System.err.print(errMsg.toString());
            }

            handleInterfaceGraphics();

            spinWaitMiliseconds(FRAME_TIME_DELAY_MSEC);
        }
    }

    // MODIFIES: this
    // EFFECTS: draws all required UI visuals
    public void handleInterfaceGraphics() throws Exception {
        screen.clear();
        screen.setCursorPosition(new TerminalPosition(0, 0));
        // TODO: add main draw logic here
        drawPlanetListEditor();
        drawErrAndMessageText();
        screen.setCursorPosition(new TerminalPosition(screen.getTerminalSize().getColumns() - 1, 0));
        screen.refresh();
    }

    // MODIFIES: this
    // EFFECTS: draws left-side planet editor
    public void drawPlanetListEditor() {
        TextGraphics gfx = screen.newTextGraphics();
        gfx.setForegroundColor(TextColor.ANSI.WHITE);

        // DRAW EDITOR SURROUNDING BOX
        gfx.drawLine(EDITOR_LEFT, EDITOR_TOP, EDITOR_RIGHT, EDITOR_TOP, '+'); // TOP
        gfx.drawLine(EDITOR_LEFT, EDITOR_TOP, EDITOR_LEFT, EDITOR_BOT, '+'); // LEFT
        gfx.drawLine(EDITOR_RIGHT, EDITOR_TOP, EDITOR_RIGHT, EDITOR_BOT, '+'); // RIGHT
        gfx.drawLine(EDITOR_LEFT, EDITOR_BOT, EDITOR_RIGHT, EDITOR_BOT, '+'); // BOTTOM

        drawPlanetList(gfx);
        drawPlanetEditMenu(gfx);
    }

    // MODIFIES: this
    // EFFECTS: draws planet editor planet list
    public void drawPlanetList(TextGraphics gfx) {
        // title and border
        gfx.putString(new TerminalPosition(EDITOR_LEFT + 1, EDITOR_TOP + 1), "PLANET LIST");
        gfx.drawLine(EDITOR_LEFT, EDITOR_TOP + 2, EDITOR_RIGHT, EDITOR_TOP + 2, '+');
        int listStartIndex = simulation.getPlanets().indexOf(selectedPlanet);
        for (int i = 0; i < PLANETLIST_ENTIRES; i++) {
            int indexActual = listStartIndex + i;
            if (indexActual >= simulation.getPlanets().size()) {
                break;
            }
            gfx.putString(EDITOR_LEFT + 3, EDITOR_TOP + 3 + i, simulation.getPlanets().get(indexActual).getName());
        }
    }

    // MODIFIES: this
    // EFFECTS: draws planet edit menu
    public void drawPlanetEditMenu(TextGraphics gfx) {
        // title and border
        int borderHeight = EDITOR_TOP + PLANETLIST_ENTIRES;
        gfx.drawLine(EDITOR_LEFT, borderHeight, EDITOR_RIGHT, borderHeight, '+');
        gfx.putString(EDITOR_LEFT + 1, borderHeight + 1, "EDIT PLANET: " + selectedPlanet.getName());

    }

    // EFFECTS: draws stdout and stederr to the bottom of the screen
    public void drawErrAndMessageText() {
        TextGraphics textWriter = screen.newTextGraphics();
        textWriter.setForegroundColor(TextColor.ANSI.WHITE);
        textWriter.putString(0, TERMINAL_HEIGHT - 2, "Message:\t" + outRedirect.getStringToDisplay());
        textWriter.setForegroundColor(TextColor.ANSI.RED);
        textWriter.putString(0, TERMINAL_HEIGHT - 1, "Last Error:\t" + errRedirect.getStringToDisplay());
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
        lastUserKey = screen.pollInput();
    }

    // MODIFIES: this
    // EFFECTS: adds new planet to the simulation and selects it
    public void addAndSelectNewPlanet() {
        Planet newPlanet = new Planet("New Planet", 1.0f);
        simulation.addPlanet(newPlanet);
        selectedPlanet = newPlanet;
    }
}
