package ui;

import java.util.*;
import javax.swing.JFrame;
import javax.swing.text.html.Option;

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
    public static final int REFRESH_DELAY_MSEC = 10;
    private static final int EDIT_PROP_MAX_INPUT_LEN = SimulationGraphics.EDITOR_RIGHT - SimulationGraphics.EDITOR_LEFT
            - 3;
    private static final String[] NEW_PLANET_NAMES = { "Kepler", "Earth", "Solaris", "Tatoonie", "Furball", "X",
            "Atlas", "Gemini", "Spongey", "Arrakis", "Trapist", "Proxima", "Mundley", "Bongcloud", "Euclid", "Hades",
            "Jupiter", "Draper", "Draconis", "Cancri", "Awohali", "Vytis", "Igsael", "Chura", "Maskita", "Nanron",
            "Ugaris", "Yvaga", "Lebnitz", "Doodski", "Phobos", "WASP" };
    private static final int NEW_PLANET_SUFFIX_MAX = 1000;
    private static final float NEW_PLANET_INITIAL_POS_BOUND = 30.0f;
    private static final float NEW_PLANET_INITIAL_VEL_BOUND = 2.0f;
    private static final float NEW_PLANET_MIN_RAD = 0.5f;
    private static final float NEW_PLANET_MAX_RAD = 1.5f;

    private static final float HIGH_DELTA_K = 0.5f;
    private static final float LOW_M_FACTOR_BOUNDARY = 1.2f;
    private static final float MED_M_FACTOR_BOUNDARY = 3.5f;

    private static final KeyStroke KS_ARROWUP = new KeyStroke(KeyType.ArrowUp);
    private static final KeyStroke KS_ARROWDOWN = new KeyStroke(KeyType.ArrowDown);
    private static final KeyStroke KS_ARROWLEFT = new KeyStroke(KeyType.ArrowLeft);
    private static final KeyStroke KS_ARROWRIGHT = new KeyStroke(KeyType.ArrowRight);

    public static final String EDITOR_OPTION_PLANETS = "EditPlanets";
    public static final String EDITOR_OPTION_COLLISIONS = "EditCollision";

    public static final String PROP_OPTION_NAME = "PropName";
    public static final String PROP_OPTION_POS = "PropPosition";
    public static final String PROP_OPTION_VEL = "PropVelocity";
    public static final String PROP_OPTION_RAD = "PropRadius";

    private ConsoleOutputRedirectStream errRedirect;
    private ConsoleOutputRedirectStream outRedirect;

    private KeyStroke lastUserKey;

    private Simulation simulation;
    private float lastDeltaTimeSeconds;
    private boolean simulationIsRunning;

    private boolean editingSelectedPlanet;
    private boolean editingSelectedProperty;
    private String userInputString;

    private boolean onTitleScreen;

    private SimulationGraphics simGraphics;

    private OptionSelector<Planet> planetSelector;
    private OptionSelector<String> propertySelector;
    private OptionSelector<Collision> collisionSelector;
    private OptionSelector<String> editorSelector;

    // EFFECTS: initialize simulation, init graphical/user input, redirect
    // sterr+stdout, and set simulation state to the opening screen
    public SimulationManager() throws Exception {
        initOutputStreams();
        simGraphics = new SimulationGraphics(this);
        initSimulationVariables();
        initEditorVariables();

        onTitleScreen = true;
    }

    // EFFECTS: setup output streams
    private void initOutputStreams() {
        errRedirect = new ConsoleOutputRedirectStream(System.err);
        System.setErr(errRedirect);
        outRedirect = new ConsoleOutputRedirectStream(System.out);
        System.setOut(outRedirect);
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
        planetSelector = new OptionSelector<>(simulation.getPlanets(), KS_ARROWDOWN, KS_ARROWUP);

        String propOptions[] = { PROP_OPTION_NAME, PROP_OPTION_POS, PROP_OPTION_VEL, PROP_OPTION_RAD };
        propertySelector = new OptionSelector<>(Arrays.asList(propOptions), KS_ARROWDOWN, KS_ARROWUP);

        collisionSelector = new OptionSelector<>(simulation.getCollisions(), KS_ARROWDOWN, KS_ARROWUP);

        String editorOptions[] = { EDITOR_OPTION_PLANETS, EDITOR_OPTION_PLANETS };
        editorSelector = new OptionSelector<>(Arrays.asList(editorOptions), KS_ARROWRIGHT, KS_ARROWLEFT);

        editingSelectedPlanet = false;
        editingSelectedProperty = false;
        userInputString = "";
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public Planet getSelectedPlanet() {
        return planetSelector.getSelectedObject();
    }

    public String getSelectedProperty() {
        return propertySelector.getSelectedObject();
    }

    public Collision getSelectedCollision() {
        return collisionSelector.getSelectedObject();
    }

    public String getSelectedEditorView() {
        return editorSelector.getSelectedObject();
    }

    // MODIFIES: this
    // EFFECTS: execute main input/rendering loop
    public void mainLoop() throws Exception {
        while (true) {
            long startNanoTime = System.nanoTime();
            simGraphics.clear();

            if (onTitleScreen) {
                handleTitleScreen();
            } else {
                handleEverythingAndiMeanEverything();
            }

            simGraphics.refresh();

            long endNanoTime = System.nanoTime();
            lastDeltaTimeSeconds = (float) (endNanoTime - startNanoTime) / 1000000000.0f;
            spinWaitMiliseconds((int) Math.max(0, REFRESH_DELAY_MSEC - (endNanoTime - startNanoTime) / 1000));
        }
    }

    // MODIFIES: this
    // EFFECTS: handles simple titlescreen logic
    private void handleTitleScreen() throws Exception {
        simGraphics.drawTitleScreen();

        KeyStroke nextKey = simGraphics.getScreen().pollInput();
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
    // EFFECTS: handles all input and graphics
    private void handleEverythingAndiMeanEverything() {
        try {
            handleUserInput();
            handleSimulationState();

            // NOTE: the handling of the simulation and user input can cause it such that
            // there is no planets left, which is a special state which must be recognised
            // for when rendering, so drawing must be done last, after this is accounted for
            simGraphics.drawEditorView();
            simGraphics.drawSimulationViewPort();
        } catch (Exception exception) {
            printException(exception);
        }

        simGraphics.drawErrAndMessageText(outRedirect, errRedirect);
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
            case EDITOR_OPTION_PLANETS:
                handleEditorViewPlanetUserInput();
                break;

            case EDITOR_OPTION_COLLISIONS:
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
