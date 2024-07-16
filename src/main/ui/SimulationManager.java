package ui;

import java.util.*;

import javax.swing.JFrame;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.*;
import com.googlecode.lanterna.input.*;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;

import exceptions.PlanetDoesntExistInSimulationException;
import model.*;

// Represents the current state of user-interface to managing simulations
public class SimulationManager {
    private static final int TERMINAL_WIDTH = 90;
    private static final int TERMINAL_HEIGHT = 35;
    private static final int REFRESH_DELAY_MSEC = 10;

    private static final int EDITOR_TOP = 0;
    private static final int EDITOR_BOT = 32;
    private static final int EDITOR_LEFT = 0;
    private static final int EDITOR_RIGHT = 40;
    private static final int PLANETLIST_ENTIRES = 20;
    private static final int PLANETINFO_TOP = EDITOR_TOP + PLANETLIST_ENTIRES + 3;

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
    private static final float NEW_PLANET_POS_OFFSET_BOUND = 30.0f;
    private static final float NEW_PLANET_MIN_RAD = 0.5f;
    private static final float NEW_PLANET_MAX_RAD = 1.5f;

    private static final float HIGH_DELTA_K = 0.5f;
    private static final float LOW_M_FACTOR_BOUNDARY = 1.2f;
    private static final float MED_M_FACTOR_BOUNDARY = 3.5f;

    private ConsoleOutputRedirectStream errRedirect;
    private ConsoleOutputRedirectStream outRedirect;

    private TerminalScreen screen;
    private KeyStroke lastUserKey;

    private Simulation simulation;
    private float lastDeltaTimeSeconds;
    private boolean simulationIsRunning;

    private Planet selectedPlanet;
    private int listViewOffset;
    private boolean editingSelectedPlanet;
    private boolean editingSelectedProperty;
    private int selectedProperty;
    private String userInputString;

    private ViewportEngine viewport;

    // EFFECTS: initialize simulation, init graphical/user input, redirect
    // sterr+stdout, and set simulation state to the opening screen
    public SimulationManager() throws Exception {
        initOutputStreams();
        initScreen();
        initSimulationVariables();
        initEditorVariables();

        viewport = new ViewportEngine(Math.min(VIEWPORT_PIX_WIDTH, VIEWPORT_PIX_HEIGHT), simulation);
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
            swingFrame.setTitle("n-Body Simulation");
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
        editingSelectedPlanet = false;
        editingSelectedProperty = false;
        userInputString = "";
        listViewOffset = 0;
    }

    // MODIFIES: this
    // EFFECTS: execute main input/rendering loop
    public void mainLoop() throws Exception {
        while (true) {
            long startNanoTime = System.nanoTime();

            screen.setCursorPosition(new TerminalPosition(0, 0));
            clearTerminal();

            handleEverythingAndiMeanEverything();

            screen.setCursorPosition(new TerminalPosition(screen.getTerminalSize().getColumns() - 1, 0));
            screen.refresh();

            long endNanoTime = System.nanoTime();
            lastDeltaTimeSeconds = (float) (endNanoTime - startNanoTime) / 1000000000.0f;
            spinWaitMiliseconds((int) Math.max(0, REFRESH_DELAY_MSEC - (endNanoTime - startNanoTime) / 1000));
        }
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
            drawPlanetListEditor();
            drawSimulationViewPort();
        } catch (Exception exception) {
            printException(exception);
        }
        drawErrAndMessageText();
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
        System.err.print(method + " threw " + exception.getClass().getSimpleName());
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
                if (viewport.getPlanetMaskValue(x, y) == selectedPlanet) {
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
    // EFFECTS: draws left-side planet editor
    private void drawPlanetListEditor() {
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
        listViewOffset = Math.max(listViewOffset, planetList.indexOf(selectedPlanet) - PLANETLIST_ENTIRES + 1);
        listViewOffset = Math.min(listViewOffset, planetList.indexOf(selectedPlanet));

        for (int i = 0; i < PLANETLIST_ENTIRES; i++) {
            int indexActual = listViewOffset + i;
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

        float deltaV = Vector3.sub(planetA.getVelocity(), planetB.getVelocity()).magnitude();

        float kineticA = 0.5f * planetA.getMass() * deltaV * deltaV;
        float kineticB = 0.5f * planetB.getMass() * deltaV * deltaV;

        float deltaK = Math.abs(kineticA - kineticB) / (kineticA + kineticB);
        if (deltaK >= HIGH_DELTA_K) {
            handleCollideHighDeltaK(planetA, planetB);
        } else {
            handleCollideLowDeltaK(planetA, planetB);
        }
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

    // MODIFIES: this
    // EFFECTS: handles all user input
    private void handleUserInput() throws Exception {
        lastUserKey = screen.pollInput();
        if (lastUserKey == null) {
            return;
        }

        handleShouldQuit();
        handleSimulationPauseAndUnpause();

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
            throw new PlanetDoesntExistInSimulationException();
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
        float posX = (rand.nextFloat() - 0.5f) * NEW_PLANET_POS_OFFSET_BOUND;
        float posY = (rand.nextFloat() - 0.5f) * NEW_PLANET_POS_OFFSET_BOUND;
        float posZ = (rand.nextFloat() - 0.5f) * NEW_PLANET_POS_OFFSET_BOUND;
        float scale = NEW_PLANET_MIN_RAD + rand.nextFloat() * (NEW_PLANET_MAX_RAD - NEW_PLANET_MIN_RAD);

        Planet newPlanet = new Planet(name + "-" + numberSuffix, new Vector3(posX, posY, posZ), new Vector3(), scale);
        simulation.addPlanet(newPlanet);
        selectedPlanet = newPlanet;
    }
}
