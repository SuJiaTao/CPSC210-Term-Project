package ui;

import java.util.*;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import model.*;

// Represents the current state of user-interface to managing simulations
public class SimulationManager {
    private static final int TERMINAL_WIDTH = 90;
    private static final int TERMINAL_HEIGHT = 35;
    private static final int FRAME_TIME_DELAY_MSEC = 50;

    private static final int EDITOR_TOP = 0;
    private static final int EDITOR_BOT = 32;
    private static final int EDITOR_LEFT = 0;
    private static final int EDITOR_RIGHT = 40;
    private static final int PLANETLIST_ENTIRES = 20;

    private static final int PLANETEDIT_NAME = 0;
    private static final int PLANETEDIT_POSITION = 1;
    private static final int PLANETEDIT_VELOCITY = 2;
    private static final int PLANETEDIT_RADIUS = 3;
    private static final int PLANETEDIT_CYCLE_MOD = PLANETEDIT_RADIUS;

    private ConsoleOutputRedirectStream errRedirect;
    private ConsoleOutputRedirectStream outRedirect;

    private TerminalScreen screen;
    private KeyStroke lastUserKey;

    private Simulation simulation;
    private int newPlanetSuffix;

    private Planet selectedPlanet;
    private boolean editingSelectedPlanet;
    private int planetEditMode;

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
        newPlanetSuffix = 0;

        // simulation starts with planet
        addAndSelectNewPlanet();
        editingSelectedPlanet = false;
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
                handleUserInput();
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
        setTextGraphicsToViewMode(gfx);

        // DRAW EDITOR SURROUNDING BOX
        gfx.drawLine(EDITOR_LEFT, EDITOR_TOP, EDITOR_RIGHT, EDITOR_TOP, '+'); // TOP
        gfx.drawLine(EDITOR_LEFT, EDITOR_TOP, EDITOR_LEFT, EDITOR_BOT, '+'); // LEFT
        gfx.drawLine(EDITOR_RIGHT, EDITOR_TOP, EDITOR_RIGHT, EDITOR_BOT, '+'); // RIGHT
        gfx.drawLine(EDITOR_LEFT, EDITOR_BOT, EDITOR_RIGHT, EDITOR_BOT, '+'); // BOTTOM

        drawPlanetList(gfx);
        drawPlanetInfo(gfx);
    }

    // MODIFIES: this
    // EFFECTS: draws planet editor planet list
    public void drawPlanetList(TextGraphics gfx) {
        // title and border
        gfx.putString(new TerminalPosition(EDITOR_LEFT + 1, EDITOR_TOP + 1), "PLANET LIST");
        gfx.drawLine(EDITOR_LEFT, EDITOR_TOP + 2, EDITOR_RIGHT, EDITOR_TOP + 2, '+');

        List<Planet> planetList = simulation.getPlanets();

        int listStartIndex = Math.max(0, planetList.indexOf(selectedPlanet) - PLANETLIST_ENTIRES);
        for (int i = 0; i < PLANETLIST_ENTIRES; i++) {
            int indexActual = listStartIndex + i;
            if (indexActual >= planetList.size()) {
                break;
            }
            if (indexActual == planetList.indexOf(selectedPlanet)) {
                setTextGraphicsToHoverMode(gfx);
                if (editingSelectedPlanet) {
                    setTextGraphicsToSelectMode(gfx);
                }
            } else {
                setTextGraphicsToViewMode(gfx);
            }
            gfx.putString(EDITOR_LEFT + 1, EDITOR_TOP + 3 + i, planetList.get(indexActual).getName());
        }
    }

    // MODIFIES: this
    // EFFECTS: draws planet info viewer
    public void drawPlanetInfo(TextGraphics gfx) {
        setTextGraphicsToViewMode(gfx);

        // title and border
        int borderHeight = EDITOR_TOP + PLANETLIST_ENTIRES;
        gfx.drawLine(EDITOR_LEFT, borderHeight, EDITOR_RIGHT, borderHeight, '+');

        if (editingSelectedPlanet) {
            gfx.putString(EDITOR_LEFT + 1, borderHeight + 1, "EDIT PLANET: " + selectedPlanet.getName());
        } else {
            gfx.putString(EDITOR_LEFT + 1, borderHeight + 1, "VIEW PLANET: " + selectedPlanet.getName());
        }

    }

    // EFFECTS: sets Textgraphics to "hover" appearance
    public void setTextGraphicsToHoverMode(TextGraphics gfx) {
        gfx.setBackgroundColor(TextColor.ANSI.WHITE);
        gfx.setForegroundColor(TextColor.ANSI.BLACK);
    }

    // EFFECTS: sets Textgraphics to "selection" appearance
    public void setTextGraphicsToSelectMode(TextGraphics gfx) {
        gfx.setBackgroundColor(TextColor.ANSI.BLUE);
        gfx.setForegroundColor(TextColor.ANSI.WHITE);
    }

    // EFFECTS: sets Textgraphics to "view" appearance
    public void setTextGraphicsToViewMode(TextGraphics gfx) {
        gfx.setBackgroundColor(TextColor.ANSI.BLACK);
        gfx.setForegroundColor(TextColor.ANSI.WHITE);
    }

    // EFFECTS: draws stdout and stederr to the bottom of the screen
    public void drawErrAndMessageText() {
        TextGraphics textWriter = screen.newTextGraphics();
        textWriter.setBackgroundColor(TextColor.ANSI.BLACK);

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

    }

    // MODIFIES: this
    // EFFECTS: handles all user input
    public void handleUserInput() throws Exception {
        lastUserKey = screen.pollInput();
        if (lastUserKey == null) {
            return;
        }
        if (editingSelectedPlanet) {
            handleEditPlanet();
        } else {
            handleCycleSelectedPlanet();
        }
    }

    // MODIFIES: this
    // EFFECTS: manages planet editing behavior
    public void handleEditPlanet() {
        if (lastUserKey.getKeyType() == KeyType.Escape) {
            editingSelectedPlanet = false;
            return;
        }
        if (lastUserKey.getKeyType() == KeyType.ArrowUp) {
            planetEditMode--;
        }
        if (lastUserKey.getKeyType() == KeyType.ArrowDown) {
            planetEditMode++;
        }
        // NOTE: ensure positive modulous
        planetEditMode %= PLANETEDIT_CYCLE_MOD;
        if (planetEditMode < 0) {
            planetEditMode += PLANETEDIT_CYCLE_MOD;
        }
    }

    // MODIFIES: this
    // EFFECTS: cycles the selected planet based on the arrow keys, or selects if
    // detected enter key
    public void handleCycleSelectedPlanet() {
        int selectedIndex = simulation.getPlanets().indexOf(selectedPlanet);
        if (lastUserKey.getKeyType() == KeyType.ArrowUp) {
            selectedIndex--;
        }
        if (lastUserKey.getKeyType() == KeyType.ArrowDown) {
            selectedIndex++;
        }
        if (lastUserKey.getKeyType() == KeyType.Enter) {
            editingSelectedPlanet = true;
            planetEditMode = PLANETEDIT_NAME;
            return;
        }

        // NOTE: ensure a positive modulous as the % operator can produce negative
        // integers
        selectedIndex %= simulation.getPlanets().size();
        if (selectedIndex < 0) {
            selectedIndex += simulation.getPlanets().size();
        }
        selectedPlanet = simulation.getPlanets().get(selectedIndex);
    }

    // MODIFIES: this
    // EFFECTS: adds new planet to the simulation and selects it
    public void addAndSelectNewPlanet() {
        Planet newPlanet = new Planet("New Planet " + newPlanetSuffix, 1.0f);
        simulation.addPlanet(newPlanet);
        selectedPlanet = newPlanet;
        newPlanetSuffix++;
    }
}
