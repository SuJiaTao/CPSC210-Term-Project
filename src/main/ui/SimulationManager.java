package ui;

import java.util.*;

import javax.swing.JFrame;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.*;
import com.googlecode.lanterna.input.*;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;

import exceptions.PlanetDoesntExistException;
import model.*;

// Represents the current state of user-interface to managing simulations
public class SimulationManager {
    private static final int TERMINAL_WIDTH = 100;
    private static final int TERMINAL_HEIGHT = 35;
    private static final int REFRESH_DELAY_MSEC = 10;

    private static final int TITLE_SCREEN_CENTER_X = TERMINAL_WIDTH / 2;
    private static final int TITLE_SCREEN_CENTER_Y = TERMINAL_HEIGHT / 2;
    private static final int TITLE_SCREEN_WIDTH = 70;
    private static final int TITLE_SCREEN_HEIGHT = 16;
    private static final int TITLE_SCREEN_LEFT = TITLE_SCREEN_CENTER_X - (TITLE_SCREEN_WIDTH / 2);
    private static final int TITLE_SCREEN_TOP = TITLE_SCREEN_CENTER_Y - (TITLE_SCREEN_HEIGHT / 2);

    private static final int EDITOR_TOP = 0;
    private static final int EDITOR_BOT = 32;
    private static final int EDITOR_LEFT = 0;
    private static final int EDITOR_RIGHT = 40;
    private static final int EDITORLIST_ENTIRES = 20;
    private static final int PLANETINFO_TOP = EDITOR_TOP + EDITORLIST_ENTIRES + 3;

    private static final int EDIT_PROP_NAME = 0;
    private static final int EDIT_PROP_POSITION = 1;
    private static final int EDIT_PROP_VELOCITY = 2;
    private static final int EDIT_PROP_RADIUS = 3;
    private static final int EDIT_PROP_CYCLE_MOD = EDIT_PROP_RADIUS + 1;

    private static final int VP_FRAME_TOP = 0;
    private static final int VP_FRAME_BOT = 32;
    private static final int VP_FRAME_LEFT = EDITOR_RIGHT + 1;
    private static final int VP_FRAME_RIGHT = TERMINAL_WIDTH - 1;
    private static final int VIEWPORT_TOP = VP_FRAME_TOP + 3;
    private static final int VIEWPORT_BOT = VP_FRAME_BOT;
    private static final int VIEWPORT_LEFT = VP_FRAME_LEFT + 1;
    private static final int VIEWPORT_RIGHT = VP_FRAME_RIGHT;
    private static final int VIEWPORT_PIX_WIDTH = (VIEWPORT_RIGHT - VIEWPORT_LEFT) / 2;
    private static final int VIEWPORT_PIX_HEIGHT = VIEWPORT_BOT - VIEWPORT_TOP;

    private static final int EDIT_PROP_MAX_INPUT_LEN = EDITOR_RIGHT - EDITOR_LEFT - 3;

    private static final String[] NEW_PLANET_NAMES = { "Kepler", "Earth", "Solaris", "Tatoonie", "Furball", "X",
            "Atlas", "Gemini", "Spongey", "Arrakis", "Trapist", "Proxima", "Mundley", "Bongcloud", "Euclid", "Hades",
            "Jupiter", "Draper", "Draconis", "Cancri", "Awohali", "Vytis", "Igsael", "Chura", "Maskita", "Nanron",
            "Ugaris", "Yvaga", "Lebnitz", "Doodski", "Phobos", "WASP" };
    private static final int NEW_PLANET_SUFFIX_MAX = 1000;
    private static final float NEW_PLANET_INITIAL_POS_BOUND = 30.0f;
    private static final float NEW_PLANET_INITIAL_VEL_BOUND = 1.5f;
    private static final float NEW_PLANET_MIN_RAD = 0.5f;
    private static final float NEW_PLANET_MAX_RAD = 1.5f;

    private static final float HIGH_DELTA_K = 0.5f;
    private static final float LOW_M_FACTOR_BOUNDARY = 1.2f;
    private static final float MED_M_FACTOR_BOUNDARY = 3.5f;

    private static final int EDITOR_VIEW_LIST_PLANETS = 0;
    private static final int EDITOR_VIEW_LIST_COLLISIONS = 1;
    private static final int EDITOR_VIEW_LIST_CYCLE_MOD = EDITOR_VIEW_LIST_COLLISIONS + 1;

    private ConsoleOutputRedirectStream errRedirect;
    private ConsoleOutputRedirectStream outRedirect;

    private TerminalScreen screen;
    private KeyStroke lastUserKey;

    private Simulation simulation;
    private float lastDeltaTimeSeconds;
    private boolean simulationIsRunning;

    private int editorViewListSelection;

    private Planet selectedPlanet;
    private int plntListViewOffset;
    private boolean editingSelectedPlanet;
    private boolean editingSelectedProperty;
    private int selectedProperty;
    private String userInputString;

    private Collision selectedCollision;
    private int colListViewOffset;

    private ViewportEngine viewport;

    private boolean onTitleScreen;

    // EFFECTS: initialize simulation, init graphical/user input, redirect
    // sterr+stdout, and set simulation state to the opening screen
    public SimulationManager() throws Exception {
        initOutputStreams();
        initScreen();
        initSimulationVariables();
        initEditorVariables();

        viewport = new ViewportEngine(Math.min(VIEWPORT_PIX_WIDTH, VIEWPORT_PIX_HEIGHT), simulation);
        onTitleScreen = true;
    }

    // EFFECTS: setup output streams
    private void initOutputStreams() {
        errRedirect = new ConsoleOutputRedirectStream(System.err);
        System.setErr(errRedirect);
        outRedirect = new ConsoleOutputRedirectStream(System.out);
        System.setOut(outRedirect);
    }

    // EFFECTS: sets up screen
    private void initScreen() throws Exception {
        DefaultTerminalFactory termFactory = new DefaultTerminalFactory();
        termFactory.setInitialTerminalSize(new TerminalSize(TERMINAL_WIDTH, TERMINAL_HEIGHT));
        screen = termFactory.createScreen();
        checkIfObtainedDesiredTerminalSize();
        tryAndSetupWindowFrame();
        screen.startScreen();
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

    // EFFECTS: tries to set some additional properties of the current screen and
    // prints an error of unable to
    private void tryAndSetupWindowFrame() {
        try {
            SwingTerminalFrame swingFrame = (SwingTerminalFrame) screen.getTerminal();
            swingFrame.setResizable(false);
            swingFrame.setTitle("N-Body Simulator");
            swingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } catch (Exception excep) {
            System.err.print("Failed to setup window. Error: " + excep.toString());
        }
    }

    // EFFECTS: sets up simulation related variables
    private void initSimulationVariables() {
        simulation = new Simulation();
        simulationIsRunning = false;
        lastDeltaTimeSeconds = 0.0f;
        addAndSelectNewPlanet();
    }

    // EFFECTS: sets up editor related variables
    private void initEditorVariables() {
        editorViewListSelection = EDITOR_VIEW_LIST_PLANETS;

        selectedPlanet = null;
        editingSelectedPlanet = false;
        editingSelectedProperty = false;
        userInputString = "";
        plntListViewOffset = 0;

        selectedCollision = null;
        colListViewOffset = 0;
    }

    // MODIFIES: this
    // EFFECTS: execute main input/rendering loop
    public void mainLoop() throws Exception {
        while (true) {
            long startNanoTime = System.nanoTime();

            screen.setCursorPosition(null);
            clearTerminal();

            if (onTitleScreen) {
                handleTitleScreen();
            } else {
                handleEverythingAndiMeanEverything();
            }

            screen.refresh();

            long endNanoTime = System.nanoTime();
            lastDeltaTimeSeconds = (float) (endNanoTime - startNanoTime) / 1000000000.0f;
            spinWaitMiliseconds((int) Math.max(0, REFRESH_DELAY_MSEC - (endNanoTime - startNanoTime) / 1000));
        }
    }

    // MODIFIES: this
    // EFFECTS: handles simple titlescreen logic
    private void handleTitleScreen() throws Exception {
        drawTitleScreen();

        KeyStroke nextKey = screen.pollInput();
        if (nextKey == null) {
            return;
        }
        if (nextKey.getKeyType() != KeyType.Character) {
            return;
        }
        if (nextKey.getCharacter() == ' ') {
            onTitleScreen = false;
        }
    }

    // MODIFIES: this
    // EFFECTS: draws title screen text
    private void drawTitleScreen() {
        TextGraphics gfx = screen.newTextGraphics();
        TerminalPosition borderTopLeft = new TerminalPosition(TITLE_SCREEN_LEFT, TITLE_SCREEN_TOP);
        TerminalSize borderSize = new TerminalSize(TITLE_SCREEN_WIDTH, TITLE_SCREEN_HEIGHT);
        gfx.drawRectangle(borderTopLeft, borderSize, '+');
        drawTitleScreenCenteredText(gfx, "N-BODY SIMULATOR", 0);
        drawTitleScreenCenteredText(gfx, "Bailey JT Brown 2024", 1);
        drawTitleScreenleftText(gfx, " Controls:", 3);
        drawTitleScreenleftText(gfx, "  - Left/Right to cycle between Planet/Collision list view", 4);
        drawTitleScreenleftText(gfx, "  - Plus/Minus to add/remove planets", 5);
        drawTitleScreenleftText(gfx, "  - Up/Down to cycle between elements", 6);
        drawTitleScreenleftText(gfx, "  - Enter to select item", 7);
        drawTitleScreenleftText(gfx, "  - Escape to unselect item", 8);
        drawTitleScreenleftText(gfx, "  - Space to pause/unpause simulation", 9);
        drawTitleScreenleftText(gfx, "  - Q or close window to quit", 10);
        drawTitleScreenCenteredText(gfx, " Press Space to continue", TITLE_SCREEN_HEIGHT - 3);
    }

    // MODIFIES: this
    // EFFECTS: draws centered text within the titlescreen box
    private void drawTitleScreenCenteredText(TextGraphics gfx, String text, int line) {
        int posX = TITLE_SCREEN_CENTER_X - (text.length() / 2);
        int posY = TITLE_SCREEN_TOP + line + 1;
        gfx.putString(posX, posY, text);
    }

    // MODIFIES: this
    // EFFECTS: draws left-aligned text within the titlescreen box
    private void drawTitleScreenleftText(TextGraphics gfx, String text, int line) {
        int posX = TITLE_SCREEN_LEFT + 1;
        int posY = TITLE_SCREEN_TOP + line + 1;
        gfx.putString(posX, posY, text);
    }

    // MODIFIES: this
    // EFFECTS: handles all input and graphics
    private void handleEverythingAndiMeanEverything() {
        try {
            handleUserInput();
            handleSimulationState();

            // NOTE: the handling of the simulation and user input can cause it such that
            // there is no planets left, which is a special state which must be recognised
            // for when rendering, so drawing must be done last, after this is accounted for
            ensureSelectedPlanetIsReasonable();
            ensureSelectedCollsisionIsReasonable();
            drawEditorView();
            drawSimulationViewPort();
        } catch (Exception exception) {
            printException(exception);
        }
        drawErrAndMessageText();
    }

    // REQUIRES: editorViewListSelection must be a valid value
    // MODIFIES: this
    // EFFECTS: draws the appropriate visuals to the left-side editor
    private void drawEditorView() {
        switch (editorViewListSelection) {
            case EDITOR_VIEW_LIST_PLANETS:
                drawPlanetListEditor();
                break;
            case EDITOR_VIEW_LIST_COLLISIONS:
                drawCollisionListEditor();
                break;
            default:
                break;
        }
    }

    // MODIFIES: this
    // EFFECTS: prints the most useful error message given the callstack
    private void printException(Exception exception) {
        StackTraceElement[] callStack = exception.getStackTrace();
        StackTraceElement elemOfInterest = callStack[0]; // DEFAULT: highest

        // NOTE: gets the "highest" element within THIS class as to get around nested
        // error checking in the JDK
        for (int i = 0; i < callStack.length; i++) {
            if (callStack[i].getClassName().equals(this.getClass().getName())) {
                elemOfInterest = callStack[i];
                break;
            }
        }

        String method = elemOfInterest.getMethodName();
        int lineNum = elemOfInterest.getLineNumber();
        System.err.print(method + " line " + lineNum + " threw " + exception.getClass().getSimpleName());
    }

    // MODIFIES: this
    // EFFECTS: ensure that selectedPlanet is a reasonable value
    private void ensureSelectedPlanetIsReasonable() {
        if (!simulation.getPlanets().contains(selectedPlanet)) {
            if (simulation.getPlanets().size() > 0) {
                selectedPlanet = simulation.getPlanets().get(0);
            } else {
                setSimulationNoPlanets();
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: ensure that selectedCollision is a reasonable value
    private void ensureSelectedCollsisionIsReasonable() {
        if (selectedCollision == null) {
            if (simulation.getCollisions().size() > 0) {
                selectedCollision = simulation.getCollisions().get(0);
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: ensure the simulation is in the correct state for there being no
    // planets
    private void setSimulationNoPlanets() {
        selectedPlanet = null;
        simulationIsRunning = false;
        editingSelectedPlanet = false;
        editingSelectedProperty = false;
    }

    // MODIFIES: this
    // EFFECTS: completely clears the terminal
    private void clearTerminal() {
        TextGraphics gfx = screen.newTextGraphics();
        gfx.fillRectangle(new TerminalPosition(0, 0), screen.getTerminalSize(), ' ');
    }

    // MODIFIES: this
    // EFFECTS: draws right-side 3D viewport
    private void drawSimulationViewPort() {
        TextGraphics gfx = screen.newTextGraphics();
        drawViewportFrame(gfx);
        drawViewportView(gfx);
    }

    // MODIFIES: this
    // EFFECTS: draw the viewport buffers to the terminal
    private void drawViewportView(TextGraphics gfx) {
        // TODO: this is a hackjob that will be fixed once I can use a proper GUI
        // library
        viewport.update();
        int vpOffsetX = (viewport.getSize() - VIEWPORT_PIX_WIDTH) / 2;
        int vpOffsetY = (viewport.getSize() - VIEWPORT_PIX_HEIGHT) / 2;
        int anchorLeft = VIEWPORT_LEFT + vpOffsetX;
        int anchorTop = VIEWPORT_TOP - vpOffsetY;
        for (int x = 0; x < viewport.getSize(); x++) {
            for (int y = 0; y < viewport.getSize(); y++) {
                if (viewport.getPlanetMaskValue(x, y) == selectedPlanet && selectedPlanet != null) {
                    setTextGraphicsToHoverMode(gfx);
                    if (editingSelectedPlanet) {
                        setTextGraphicsToSelectMode(gfx);
                    }
                } else {
                    setTextGraphicsToViewMode(gfx);
                }
                char fbChar = (char) viewport.getFrameBufferValue(x, y);
                gfx.setCharacter(anchorLeft + (x * 2) + 0, anchorTop + y, fbChar);
                gfx.setCharacter(anchorLeft + (x * 2) + 1, anchorTop + y, fbChar);
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: draws the viewport frame
    private void drawViewportFrame(TextGraphics gfx) {
        setTextGraphicsToViewMode(gfx);

        // DRAW VIEWPORT SURROUNDING BOX
        gfx.drawLine(VP_FRAME_LEFT, VP_FRAME_TOP, VP_FRAME_RIGHT, VP_FRAME_TOP, 'X');
        gfx.drawLine(VP_FRAME_LEFT, VP_FRAME_BOT, VP_FRAME_RIGHT, VP_FRAME_BOT, 'X');
        gfx.drawLine(VP_FRAME_LEFT, VP_FRAME_TOP, VP_FRAME_LEFT, VP_FRAME_BOT, 'X');
        gfx.drawLine(VP_FRAME_RIGHT, VP_FRAME_TOP, VP_FRAME_RIGHT, VP_FRAME_BOT, 'X');

        gfx.drawLine(VP_FRAME_LEFT, VP_FRAME_TOP + 2, VP_FRAME_RIGHT, VP_FRAME_TOP + 2, 'X');
        String viewportTitle = "Simulation ";
        if (simulationIsRunning) {
            viewportTitle += "Running";
        } else {
            viewportTitle += "Stopped";
        }
        viewportTitle += String.format(" | Time Elapsed: %.2fs", simulation.getTimeElapsed());
        gfx.putString(VP_FRAME_LEFT + 2, VP_FRAME_TOP + 1, viewportTitle);
    }

    // MODIFIES: this
    // EFFECTS: draws the collision list editor
    private void drawCollisionListEditor() {
        TextGraphics gfx = screen.newTextGraphics();
        setTextGraphicsToViewMode(gfx);

        drawEditorFrame(gfx);
        drawCollisionList(gfx);
    }

    // MODIFIES: this
    // EFFECTS: draw the list of collisions
    private void drawCollisionList(TextGraphics gfx) {
        gfx.putString(new TerminalPosition(EDITOR_LEFT + 2, EDITOR_TOP + 1), "COLLISION LIST");
        gfx.drawLine(EDITOR_LEFT, EDITOR_TOP + 2, EDITOR_RIGHT, EDITOR_TOP + 2, '+');

        List<Collision> colList = simulation.getCollisions();
        if (colList.size() == 0) {
            gfx.putString(EDITOR_LEFT + 1, EDITOR_TOP + 3, " No collisions yet!");
            return;
        }

        drawCollisionListEntries(gfx, colList);
        drawCollisionInfo(gfx);
    }

    // REQUIRES: selectedCollision to exist in colList
    // MODIFIES: this
    // EFFECTS: draw the entries of collisions
    private void drawCollisionListEntries(TextGraphics gfx, List<Collision> colList) {
        colListViewOffset = Math.max(colListViewOffset,
                colList.indexOf(selectedCollision) - EDITORLIST_ENTIRES + 1);
        colListViewOffset = Math.min(colListViewOffset, colList.indexOf(selectedCollision));

        for (int i = 0; i < EDITORLIST_ENTIRES; i++) {
            int indexActual = colListViewOffset + i;
            if (indexActual >= colList.size()) {
                break;
            }

            if (indexActual == colList.indexOf(selectedCollision)) {
                setTextGraphicsToHoverMode(gfx);
            } else {
                setTextGraphicsToViewMode(gfx);
            }

            Collision col = colList.get(indexActual);
            List<Planet> involved = col.getPlanetsInvolved();
            String colName = "";
            colName += involved.get(0).getName().charAt(0);
            colName += "/";
            colName += involved.get(1).getName().charAt(0);
            colName += String.format("-%.3f", col.getCollisionTime());
            String entryString = (1 + indexActual) + ". Collision " + colName;
            gfx.putString(EDITOR_LEFT + 1, EDITOR_TOP + 3 + i, entryString);
        }
    }

    // REQUIRES: selectedCollision must be in collision list and NOT null
    // MODIFIES: this
    // EFFECTS: draws collision info viewer
    private void drawCollisionInfo(TextGraphics gfx) {
        setTextGraphicsToViewMode(gfx);

        // title and border
        gfx.drawLine(EDITOR_LEFT, PLANETINFO_TOP, EDITOR_RIGHT, PLANETINFO_TOP, '+');

        if (selectedCollision == null) {
            gfx.putString(EDITOR_LEFT + 2, PLANETINFO_TOP + 1, "No collision selected");
            return;
        }
        gfx.putString(EDITOR_LEFT + 2, PLANETINFO_TOP + 1, "COLLISION INFO: ");
        gfx.putString(EDITOR_LEFT + 3, PLANETINFO_TOP + 2, "Planets Involved:");
        String involvedString = "";
        involvedString += selectedCollision.getPlanetsInvolved().get(0).getName();
        involvedString += ", ";
        involvedString += selectedCollision.getPlanetsInvolved().get(1).getName();
        gfx.putString(EDITOR_LEFT + 5, PLANETINFO_TOP + 3, involvedString);
        String timeOccouredString = String.format("Time Occoured: %.3fs", selectedCollision.getCollisionTime());
        gfx.putString(EDITOR_LEFT + 3, PLANETINFO_TOP + 4, timeOccouredString);
    }

    // MODIFIES: this
    // EFFECTS: draws left-side planet editor
    private void drawPlanetListEditor() {
        TextGraphics gfx = screen.newTextGraphics();
        setTextGraphicsToViewMode(gfx);

        drawEditorFrame(gfx);

        drawPlanetList(gfx);
        drawPlanetInfo(gfx);
    }

    // MODIFIES: this
    // EFFECTS: draw the left-side editor frame
    private void drawEditorFrame(TextGraphics gfx) {
        gfx.drawLine(EDITOR_LEFT, EDITOR_TOP, EDITOR_RIGHT, EDITOR_TOP, '+'); // TOP
        gfx.drawLine(EDITOR_LEFT, EDITOR_TOP, EDITOR_LEFT, EDITOR_BOT, '+'); // LEFT
        gfx.drawLine(EDITOR_RIGHT, EDITOR_TOP, EDITOR_RIGHT, EDITOR_BOT, '+'); // RIGHT
        gfx.drawLine(EDITOR_LEFT, EDITOR_BOT, EDITOR_RIGHT, EDITOR_BOT, '+'); // BOTTOM
    }

    // MODIFIES: this
    // EFFECTS: draws planet editor planet list
    private void drawPlanetList(TextGraphics gfx) {
        // title and border
        gfx.putString(new TerminalPosition(EDITOR_LEFT + 2, EDITOR_TOP + 1), "PLANET LIST");
        gfx.drawLine(EDITOR_LEFT, EDITOR_TOP + 2, EDITOR_RIGHT, EDITOR_TOP + 2, '+');

        List<Planet> planetList = simulation.getPlanets();
        if (planetList.size() == 0) {
            gfx.putString(EDITOR_LEFT + 1, EDITOR_TOP + 3, " Press '+' to add a planet");
            return;
        }

        drawPlanetListEntries(gfx, planetList);
    }

    // MODIFIES: this
    // EFFECTS: draws entries of the planet list
    private void drawPlanetListEntries(TextGraphics gfx, List<Planet> planetList) {
        plntListViewOffset = Math.max(plntListViewOffset,
                planetList.indexOf(selectedPlanet) - EDITORLIST_ENTIRES + 1);
        plntListViewOffset = Math.min(plntListViewOffset, planetList.indexOf(selectedPlanet));

        for (int i = 0; i < EDITORLIST_ENTIRES; i++) {
            int indexActual = plntListViewOffset + i;
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
            String entryString = "" + (indexActual + 1) + ". " + planetList.get(indexActual).getName();
            gfx.putString(EDITOR_LEFT + 1, EDITOR_TOP + 3 + i, entryString);
        }
    }

    // MODIFIES: this
    // EFFECTS: draws planet info viewer
    private void drawPlanetInfo(TextGraphics gfx) {
        setTextGraphicsToViewMode(gfx);

        // title and border
        gfx.drawLine(EDITOR_LEFT, PLANETINFO_TOP, EDITOR_RIGHT, PLANETINFO_TOP, '+');

        if (selectedPlanet == null) {
            gfx.putString(EDITOR_LEFT + 2, PLANETINFO_TOP + 1, "No planet selected");
            return;
        }

        String actionPrefix = "";
        if (editingSelectedPlanet) {
            actionPrefix = "EDIT";
        } else {
            actionPrefix = "VIEW";
        }
        gfx.putString(EDITOR_LEFT + 2, PLANETINFO_TOP + 1, actionPrefix + " PLANET ");

        drawPlanetProperties(gfx);
        drawPropertyEditingInputBox(gfx);
    }

    // MODIFIES: this
    // EFFECTS: draws GUI for value editing input handler
    private void drawPropertyEditingInputBox(TextGraphics gfx) {
        if (!editingSelectedProperty) {
            return;
        }
        setTextGraphicsToViewMode(gfx);
        gfx.drawLine(EDITOR_LEFT, EDITOR_BOT - 3, EDITOR_RIGHT, EDITOR_BOT - 3, '+');
        gfx.putString(EDITOR_LEFT + 1, EDITOR_BOT - 2, "Input Value:");
        setTextGraphicsToSelectMode(gfx);
        gfx.putString(EDITOR_LEFT + 2, EDITOR_BOT - 1, userInputString);
    }

    // MODIFIES: this
    // EFFECTS: draws planet property editor/viewer
    private void drawPlanetProperties(TextGraphics gfx) {
        setTextGraphicsToViewMode(gfx);
        String[] propertyStrings = new String[EDIT_PROP_CYCLE_MOD];
        propertyStrings[0] = "Name: " + selectedPlanet.getName();
        propertyStrings[1] = "Pos: " + selectedPlanet.getPosition().toString();
        propertyStrings[2] = "Vel: " + selectedPlanet.getVelocity().toString();
        propertyStrings[3] = "Radius: " + String.format("%.2f", selectedPlanet.getRadius());
        for (int i = 0; i < propertyStrings.length; i++) {
            if (editingSelectedPlanet && selectedProperty == i) {
                setTextGraphicsToHoverMode(gfx);
                if (editingSelectedProperty) {
                    setTextGraphicsToSelectMode(gfx);
                }
            } else {
                setTextGraphicsToViewMode(gfx);
            }
            gfx.putString(EDITOR_LEFT + 3, PLANETINFO_TOP + 2 + i, propertyStrings[i]);
        }
    }

    // EFFECTS: sets Textgraphics to "hover" appearance
    private void setTextGraphicsToHoverMode(TextGraphics gfx) {
        gfx.setBackgroundColor(TextColor.ANSI.WHITE);
        gfx.setForegroundColor(TextColor.ANSI.BLACK);
    }

    // EFFECTS: sets Textgraphics to "selection" appearance
    private void setTextGraphicsToSelectMode(TextGraphics gfx) {
        gfx.setBackgroundColor(TextColor.ANSI.BLUE);
        gfx.setForegroundColor(TextColor.ANSI.WHITE);
    }

    // EFFECTS: sets Textgraphics to "view" appearance
    private void setTextGraphicsToViewMode(TextGraphics gfx) {
        gfx.setBackgroundColor(TextColor.ANSI.BLACK);
        gfx.setForegroundColor(TextColor.ANSI.WHITE);
    }

    // EFFECTS: draws stdout and stederr to the bottom of the screen
    private void drawErrAndMessageText() {
        TextGraphics textWriter = screen.newTextGraphics();
        textWriter.setBackgroundColor(TextColor.ANSI.BLACK);

        textWriter.setForegroundColor(TextColor.ANSI.WHITE);
        textWriter.putString(0, TERMINAL_HEIGHT - 2, "LastOut: " + outRedirect.getStringToDisplay());
        textWriter.setForegroundColor(TextColor.ANSI.RED);
        textWriter.putString(0, TERMINAL_HEIGHT - 1, "LastErr: " + errRedirect.getStringToDisplay());
    }

    // EFFECTS: waits for miliseconds via spin
    private void spinWaitMiliseconds(int waitMilliseconds) {
        // NOTE: hi, you are probably wondering why I'm not using Thread.sleep()
        // right now. The issue with Thread.Sleep is that internally it is generally not
        // precice enough. At least on windows, Thread.sleep() has a granularity of
        // about ~16msec which really isn't good enough for my purposes. The only way to
        // do this the way I want is to go into a spin so here it is :3
        long startTime = System.nanoTime();
        while (((System.nanoTime() - startTime) / 1000) < waitMilliseconds) {
            // wait
        }
    }

    // MODIFIES: this
    // EFFECTS: runs the appropriate handler function based on the simulation state
    private void handleSimulationState() throws Exception {
        if (editingSelectedPlanet) {
            simulationIsRunning = false;
        }
        if (simulationIsRunning) {
            float latestTime = simulation.getTimeElapsed();
            simulation.progressBySeconds(lastDeltaTimeSeconds);
            handleDebrisForSimuationTick(latestTime);
        }
    }

    // MODIFIES: this
    // EFFECTS: handles debris behavior simulation
    private void handleDebrisForSimuationTick(float latestTime) {
        for (Collision col : simulation.getCollisions()) {
            if (col.getCollisionTime() < latestTime) {
                continue;
            }
            handleDebrisForCollision(col);
        }
    }

    // MODIFIES: this
    // EFFECTS: handles debris behavior for specific collision
    private void handleDebrisForCollision(Collision col) {
        Planet planetA = col.getPlanetsInvolved().get(0);
        Planet planetB = col.getPlanetsInvolved().get(1);

        try {
            simulation.removePlanet(planetA);
        } catch (Exception e) {
            // DO NOTHING
        }

        try {
            simulation.removePlanet(planetB);
        } catch (Exception e) {
            // DO NOTHING
        }
        // NOTE: i will finish implementing this in Phase2/3
        /*
         * float deltaV = Vector3.sub(planetA.getVelocity(),
         * planetB.getVelocity()).magnitude();
         * 
         * float kineticA = 0.5f * planetA.getMass() * deltaV * deltaV;
         * float kineticB = 0.5f * planetB.getMass() * deltaV * deltaV;
         * 
         * float deltaK = Math.abs(kineticA - kineticB) / (kineticA + kineticB);
         * if (deltaK >= HIGH_DELTA_K) {
         * handleCollideHighDeltaK(planetA, planetB);
         * } else {
         * handleCollideLowDeltaK(planetA, planetB);
         * }
         */
    }

    // MODIFIES: this
    // EFFECTS: handles two planets colliding with high deltaK
    private void handleCollideHighDeltaK(Planet bigPlanet, Planet smallPlanet) {
        float bigMass = bigPlanet.getMass();
        float smallMass = smallPlanet.getMass();
        if (bigMass < smallMass) {
            Planet temp = bigPlanet;
            bigPlanet = smallPlanet;
            smallPlanet = temp;
        }
        float massFactor = bigMass / smallMass;
        if (massFactor < LOW_M_FACTOR_BOUNDARY) {
            // low mFactor - both planets break up into pieces
        } else if (LOW_M_FACTOR_BOUNDARY <= massFactor && massFactor <= MED_M_FACTOR_BOUNDARY) {
            // medium mFactor - smaller planet is broken into pieces
        } else {
            // high mFactor - smaller planet is completly absorbed
            simulation.removePlanet(smallPlanet);
        }
    }

    // MODIFIES: this
    // EFFECTS: breaks up planet into pieces

    // MODIFIES: this
    // EFFECTS: handles two planets colliding with low deltaK
    private void handleCollideLowDeltaK(Planet planetA, Planet planetB) {
        // TODO: make actuall functional
        simulation.removePlanet(planetA);
        simulation.removePlanet(planetB);
    }

    // REQUIRES: editorViewListSelection is a legal value
    // MODIFIES: this
    // EFFECTS: handles all user input
    private void handleUserInput() throws Exception {
        lastUserKey = screen.pollInput();
        if (lastUserKey == null) {
            return;
        }

        handleShouldQuit();
        handleSimulationPauseAndUnpause();
        handleEditorCycleListView();

        switch (editorViewListSelection) {
            case EDITOR_VIEW_LIST_PLANETS:
                handleEditorViewPlanetUserInput();
                break;

            case EDITOR_VIEW_LIST_COLLISIONS:
                handleEditorViewCollisionsUserInput();
                break;

            default:
                break;
        }
    }

    // MODIFES: this
    // EFFECTS: handles the cycling of the editor list view
    private void handleEditorCycleListView() {
        if (lastUserKey.getKeyType() == KeyType.ArrowLeft) {
            editorViewListSelection--;
        }
        if (lastUserKey.getKeyType() == KeyType.ArrowRight) {
            editorViewListSelection++;
        }
        editorViewListSelection %= EDITOR_VIEW_LIST_CYCLE_MOD;
        if (editorViewListSelection < 0) {
            editorViewListSelection += EDITOR_VIEW_LIST_CYCLE_MOD;
        }
    }

    // MODIFIES: this
    // EFFECTS: handles inputs when editor is viewing collision list
    private void handleEditorViewCollisionsUserInput() {
        handleCycleSelectedCollision();
    }

    // MODIFIES: this
    // EFFECTS: handles the cycling of the collision selection
    private void handleCycleSelectedCollision() {
        if (selectedCollision == null) {
            return;
        }

        int selectedIndex = simulation.getCollisions().indexOf(selectedCollision);
        if (lastUserKey.getKeyType() == KeyType.ArrowDown) {
            selectedIndex++;
        }
        if (lastUserKey.getKeyType() == KeyType.ArrowUp) {
            selectedIndex--;
        }
        selectedIndex = Math.max(0, selectedIndex);
        selectedIndex = Math.min(selectedIndex, simulation.getCollisions().size() - 1);
        selectedCollision = simulation.getCollisions().get(selectedIndex);
    }

    // MODIFES: this
    // EFFECTS: handles inputs when editor is viewing planet list
    private void handleEditorViewPlanetUserInput() {
        if (editingSelectedPlanet) {
            if (editingSelectedProperty) {
                handleEditPlanetProperty();
            } else {
                handleCyclePlanetProperty();
            }
        } else {
            handlePlanetAddAndRemove();
            handleCycleSelectedPlanet();
        }
    }

    // MODIFIES: this
    // EFFECTS: checks whether the program is in an acceptable state to quit given
    // that the quit key is pressed and quit accordingly
    private void handleShouldQuit() {
        if (editingSelectedProperty) {
            return;
        }
        if (lastUserKey.getCharacter() == null) {
            return;
        }
        Character key = lastUserKey.getCharacter();
        if (Character.toLowerCase(key) == 'q') {
            System.exit(0);
        }
    }

    // MODIFIES: this
    // EFFECTS: handles pausing/unpausing of the simulation
    private void handleSimulationPauseAndUnpause() {
        if (selectedPlanet == null) {
            simulationIsRunning = false;
            return;
        }

        Character key = lastUserKey.getCharacter();
        if (key == null) {
            return;
        }

        if (key == ' ') {
            // toggle
            simulationIsRunning = !simulationIsRunning;
        }
    }

    // MODIFIES: this
    // EFFECTS: manages planet property editing input handling behavior
    private void handleEditPlanetProperty() {
        if (lastUserKey.getKeyType() == KeyType.Escape) {
            editingSelectedProperty = false;
            return;
        }
        if (lastUserKey.getKeyType() == KeyType.Backspace) {
            if (userInputString.length() == 0) {
                return;
            }
            userInputString = userInputString.substring(0, userInputString.length() - 1);
            return;
        }
        if (lastUserKey.getKeyType() == KeyType.Enter) {
            if (handleUserInputSubmissionAttempt()) {
                editingSelectedProperty = false;
            }
            return;
        }
        if (lastUserKey.getCharacter() != null) {
            if (userInputString.length() >= EDIT_PROP_MAX_INPUT_LEN) {
                return;
            }
            userInputString += lastUserKey.getCharacter().toString();
        }
    }

    // MODIFIES: this
    // EFFECTS: attempts to apply user input to replace the selected property, does
    // nothing if invalid input
    private boolean handleUserInputSubmissionAttempt() {
        switch (selectedProperty) {
            case EDIT_PROP_NAME:
                return tryApplyNewName();

            case EDIT_PROP_POSITION:
                Vector3 newPos = tryParseVectorFromInputString();
                if (newPos == null) {
                    return false;
                }
                selectedPlanet.setPosition(newPos);
                return true;

            case EDIT_PROP_VELOCITY:
                Vector3 newVel = tryParseVectorFromInputString();
                if (newVel == null) {
                    return false;
                }
                selectedPlanet.setVelocity(newVel);
                return true;

            case EDIT_PROP_RADIUS:
                return tryApplyNewRadius();

            default:
                System.err.print("attempted to apply input to unknown property: " + selectedProperty);
                return false;
        }
    }

    // REQUIRES: selectedPlanet is not null
    // EFFECTS: attempts to update the planet name
    private boolean tryApplyNewName() {
        if (userInputString.length() == 0) {
            return false;
        }
        if (userInputString.charAt(0) == ' ') {
            return false;
        }

        selectedPlanet.setName(userInputString);
        return true;

    }

    // EFFECTS: attempts to update the planet radius
    private boolean tryApplyNewRadius() {
        float newRadius;
        try {
            newRadius = Float.parseFloat(userInputString);
        } catch (Exception exception) {
            return false;
        }
        selectedPlanet.setRadius(newRadius);
        return true;
    }

    // EFFECTS: attempts to parse a Vector3 out of the user input string, returns
    // null if it fails
    private Vector3 tryParseVectorFromInputString() {
        String[] components = userInputString.split(" ");
        if (components.length != 3) {
            return null;
        }
        float x;
        float y;
        float z;
        try {
            x = Float.parseFloat(components[0]);
            y = Float.parseFloat(components[1]);
            z = Float.parseFloat(components[2]);
            return new Vector3(x, y, z);
        } catch (Exception exception) {
            return null;
        }
    }

    // MODIFIES: this
    // EFFECTS: manages creating and destroying selected planet
    private void handlePlanetAddAndRemove() {
        if (lastUserKey.getKeyType() != KeyType.Character) {
            return;
        }

        Character lastChar = lastUserKey.getCharacter();
        if (lastChar == '+' || lastChar == '=') {
            addAndSelectNewPlanet();
        }
        if (lastChar == '-' || lastChar == '_') {
            handleRemoveSelectedPlanet();
        }
    }

    // MODFIES: this
    // EFFECTS: handles the updating of selectedPlanet when the current selected
    // planet is removed
    private void handleRemoveSelectedPlanet() {
        if (selectedPlanet == null) {
            return;
        }
        if (!simulation.getPlanets().contains(selectedPlanet)) {
            throw new PlanetDoesntExistException();
        }

        int selectedIndex = simulation.getPlanets().indexOf(selectedPlanet);
        simulation.removePlanet(selectedPlanet);
        if (simulation.getPlanets().size() == 0) {
            setSimulationNoPlanets();
            return;
        }
        if (selectedIndex >= simulation.getPlanets().size()) {
            selectedIndex = simulation.getPlanets().size() - 1;
        }

        selectedPlanet = simulation.getPlanets().get(selectedIndex);
    }

    // MODIFIES: this
    // EFFECTS: manages planet editing cycle behavior
    private void handleCyclePlanetProperty() {
        if (lastUserKey.getKeyType() == KeyType.Escape) {
            editingSelectedPlanet = false;
            return;
        }
        if (lastUserKey.getKeyType() == KeyType.ArrowUp) {
            selectedProperty--;
        }
        if (lastUserKey.getKeyType() == KeyType.ArrowDown) {
            selectedProperty++;
        }
        if (lastUserKey.getKeyType() == KeyType.Enter) {
            userInputString = "";
            editingSelectedProperty = true;
            return;
        }
        // NOTE: ensure positive modulous
        selectedProperty %= EDIT_PROP_CYCLE_MOD;
        if (selectedProperty < 0) {
            selectedProperty += EDIT_PROP_CYCLE_MOD;
        }
    }

    // MODIFIES: this
    // EFFECTS: cycles the selected planet based on the arrow keys, or selects if
    // detected enter key
    private void handleCycleSelectedPlanet() {
        if (selectedPlanet == null) {
            return;
        }

        int selectedIndex = simulation.getPlanets().indexOf(selectedPlanet);
        if (lastUserKey.getKeyType() == KeyType.ArrowUp) {
            selectedIndex--;
        }
        if (lastUserKey.getKeyType() == KeyType.ArrowDown) {
            selectedIndex++;
        }
        if (lastUserKey.getKeyType() == KeyType.Enter) {
            editingSelectedPlanet = true;
            selectedProperty = EDIT_PROP_NAME;
            return;
        }

        selectedIndex = Math.max(0, Math.min(selectedIndex, simulation.getPlanets().size() - 1));
        selectedPlanet = simulation.getPlanets().get(selectedIndex);
    }

    // MODIFIES: this
    // EFFECTS: adds new planet to the simulation with some mildly randomized values
    // and selects it
    private void addAndSelectNewPlanet() {

        Random rand = new Random();
        String name = NEW_PLANET_NAMES[rand.nextInt(NEW_PLANET_NAMES.length)];
        String numberSuffix = String.format("%03d", rand.nextInt(NEW_PLANET_SUFFIX_MAX));

        float posX = (rand.nextFloat() - 0.5f) * NEW_PLANET_INITIAL_POS_BOUND;
        float posY = (rand.nextFloat() - 0.5f) * NEW_PLANET_INITIAL_POS_BOUND;
        float posZ = (rand.nextFloat() - 0.5f) * NEW_PLANET_INITIAL_POS_BOUND;
        Vector3 newPos = new Vector3(posX, posY, posZ);

        float velX = (rand.nextFloat() - 0.5f) * NEW_PLANET_INITIAL_VEL_BOUND;
        float velY = (rand.nextFloat() - 0.5f) * NEW_PLANET_INITIAL_VEL_BOUND;
        float velZ = (rand.nextFloat() - 0.5f) * NEW_PLANET_INITIAL_VEL_BOUND;
        Vector3 newVel = new Vector3(velX, velY, velZ);

        float scale = NEW_PLANET_MIN_RAD + rand.nextFloat() * (NEW_PLANET_MAX_RAD - NEW_PLANET_MIN_RAD);

        Planet newPlanet = new Planet(name + "-" + numberSuffix, newPos, newVel, scale);
        simulation.addPlanet(newPlanet);
        selectedPlanet = newPlanet;
    }
}
