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
    private static final int REFRESH_DELAY_MSEC = 5;

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

    private static final int VIEWPORT_TOP = 0;
    private static final int VIEWPORT_BOT = 32;
    private static final int VIEWPORT_LEFT = EDITOR_RIGHT + 1;
    private static final int VIEWPORT_RIGHT = TERMINAL_WIDTH - 1;

    private static final int EDIT_PROP_MAX_INPUT_LEN = EDITOR_RIGHT - EDITOR_LEFT - 3;

    private ConsoleOutputRedirectStream errRedirect;
    private ConsoleOutputRedirectStream outRedirect;

    private TerminalScreen screen;
    private KeyStroke lastUserKey;

    private Simulation simulation;
    private float lastDeltaTimeSeconds;
    private boolean simulationIsRunning;

    private int newPlanetSuffix;
    private Planet selectedPlanet;
    private int listViewOffset;
    private boolean editingSelectedPlanet;
    private boolean editingSelectedProperty;
    private int selectedProperty;
    private String userInputString;

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
        simulationIsRunning = false;
        lastDeltaTimeSeconds = 0.0f;

        // simulation starts with planet
        addAndSelectNewPlanet();
        editingSelectedPlanet = false;
        editingSelectedProperty = false;
        userInputString = "";
        listViewOffset = 0;
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
            long startNanoTime = System.nanoTime();

            screen.setCursorPosition(new TerminalPosition(0, 0));
            clearTerminal();

            try {
                handleUserInput();
                handleSimulationState();
                drawPlanetListEditor();
                drawSimulationViewPort();
            } catch (Exception errMsg) {
                System.err.print(errMsg.toString());
            }

            drawErrAndMessageText();

            screen.setCursorPosition(new TerminalPosition(screen.getTerminalSize().getColumns() - 1, 0));
            screen.refresh();

            long endNanoTime = System.nanoTime();
            lastDeltaTimeSeconds = (float) (endNanoTime - startNanoTime) / 1000000000.0f;
            spinWaitMiliseconds(REFRESH_DELAY_MSEC);
        }
    }

    // EFFECTS: completely clears the terminal
    public void clearTerminal() {
        TextGraphics gfx = screen.newTextGraphics();
        gfx.fillRectangle(new TerminalPosition(0, 0), screen.getTerminalSize(), ' ');
    }

    // MODIFIES: this
    // EFFECTS: draws right-side 3D viewport
    public void drawSimulationViewPort() {
        TextGraphics gfx = screen.newTextGraphics();
        setTextGraphicsToViewMode(gfx);

        // DRAW VIEWPORT SURROUNDING BOX
        gfx.drawLine(VIEWPORT_LEFT, VIEWPORT_TOP, VIEWPORT_RIGHT, VIEWPORT_TOP, 'X');
        gfx.drawLine(VIEWPORT_LEFT, VIEWPORT_BOT, VIEWPORT_RIGHT, VIEWPORT_BOT, 'X');
        gfx.drawLine(VIEWPORT_LEFT, VIEWPORT_TOP, VIEWPORT_LEFT, VIEWPORT_BOT, 'X');
        gfx.drawLine(VIEWPORT_RIGHT, VIEWPORT_TOP, VIEWPORT_RIGHT, VIEWPORT_BOT, 'X');

        gfx.drawLine(VIEWPORT_LEFT, VIEWPORT_TOP + 2, VIEWPORT_RIGHT, VIEWPORT_TOP + 2, 'X');
        String viewportTitle = "Simulation: ";
        if (simulationIsRunning) {
            viewportTitle += "Running";
        } else {
            viewportTitle += "Stopped";
        }
        viewportTitle += String.format(" | Time Elapsed: %.2fs", simulation.getTimeElapsed());
        gfx.putString(VIEWPORT_LEFT + 2, VIEWPORT_TOP + 1, viewportTitle);
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
        gfx.putString(new TerminalPosition(EDITOR_LEFT + 2, EDITOR_TOP + 1), "PLANET LIST");
        gfx.drawLine(EDITOR_LEFT, EDITOR_TOP + 2, EDITOR_RIGHT, EDITOR_TOP + 2, '+');

        List<Planet> planetList = simulation.getPlanets();

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
    public void drawPlanetInfo(TextGraphics gfx) {
        setTextGraphicsToViewMode(gfx);

        // title and border
        gfx.drawLine(EDITOR_LEFT, PLANETINFO_TOP, EDITOR_RIGHT, PLANETINFO_TOP, '+');

        String actionPrefix = "";
        if (editingSelectedPlanet) {
            actionPrefix = "EDIT";
        } else {
            actionPrefix = "VIEW";
        }
        gfx.putString(EDITOR_LEFT + 2, PLANETINFO_TOP + 1, actionPrefix + " PLANET: ");

        drawPlanetProperties(gfx);
        drawPropertyEditingInputBox(gfx);
    }

    // MODIFIES: this
    // EFFECTS: draws GUI for value editing input handler
    public void drawPropertyEditingInputBox(TextGraphics gfx) {
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
    public void drawPlanetProperties(TextGraphics gfx) {
        setTextGraphicsToViewMode(gfx);
        String[] propertyStrings = new String[EDIT_PROP_CYCLE_MOD];
        propertyStrings[0] = "Name: " + selectedPlanet.getName();
        propertyStrings[1] = "Pos: " + selectedPlanet.getPosition().toString();
        propertyStrings[2] = "Vel: " + selectedPlanet.getVelocity().toString();
        propertyStrings[3] = "Radius: " + selectedPlanet.getRadius();
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
        if (editingSelectedPlanet) {
            simulationIsRunning = false;
        }
        if (simulationIsRunning) {
            simulation.progressBySeconds(lastDeltaTimeSeconds);
        }
    }

    // MODIFIES: this
    // EFFECTS: handles all user input
    public void handleUserInput() throws Exception {
        lastUserKey = screen.pollInput();
        if (lastUserKey == null) {
            return;
        }

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
    // EFFECTS: handles pausing/unpausing of the simulation
    public void handleSimulationPauseAndUnpause() {
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
    public void handleEditPlanetProperty() {
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
    public boolean handleUserInputSubmissionAttempt() {
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

    // EFFECTS: attempts to update the planet name
    public boolean tryApplyNewName() {
        if (userInputString.length() > 0) {
            selectedPlanet.setName(userInputString);
            return true;
        }
        return false;
    }

    // EFFECTS: attempts to update the planet radius
    public boolean tryApplyNewRadius() {
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
    public Vector3 tryParseVectorFromInputString() {
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
    public void handlePlanetAddAndRemove() {
        if (lastUserKey.getKeyType() != KeyType.Character) {
            return;
        }

        Character lastChar = lastUserKey.getCharacter();
        if (lastChar == '+' || lastChar == '=') {
            addAndSelectNewPlanet();
        }
        if (lastChar == '-' || lastChar == '_') {
            if (simulation.getPlanets().size() == 1) {
                return;
            }
            handleRemoveSelectedPlanet();
        }
    }

    // REQUIRES: there are more than one planets remaining
    // MODFIES: this
    // EFFECTS: handles the updating of selectedPlanet when the current selected
    // planet is removed
    public void handleRemoveSelectedPlanet() {
        int selectedIndex = simulation.getPlanets().indexOf(selectedPlanet);
        simulation.getPlanets().remove(selectedPlanet);
        if (selectedIndex >= simulation.getPlanets().size()) {
            selectedIndex = simulation.getPlanets().size() - 1;
        }

        selectedPlanet = simulation.getPlanets().get(selectedIndex);
    }

    // MODIFIES: this
    // EFFECTS: manages planet editing cycle behavior
    public void handleCyclePlanetProperty() {
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
            selectedProperty = EDIT_PROP_NAME;
            return;
        }

        selectedIndex = Math.max(0, Math.min(selectedIndex, simulation.getPlanets().size() - 1));
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
