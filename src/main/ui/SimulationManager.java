package ui;

import model.*;
import java.util.*;
import java.io.IOException;
import com.googlecode.lanterna.input.*;

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
    public static final String EDITOR_OPTION_SAVELOAD = "EditSaveLoad";
    public static final String[] EDITOR_OPTIONS = { EDITOR_OPTION_PLANETS, EDITOR_OPTION_COLLISIONS,
            EDITOR_OPTION_SAVELOAD };

    public static final String PROP_OPTION_NAME = "PropName";
    public static final String PROP_OPTION_POS = "PropPosition";
    public static final String PROP_OPTION_VEL = "PropVelocity";
    public static final String PROP_OPTION_RAD = "PropRadius";
    public static final String[] PROP_OPTIONS = { PROP_OPTION_NAME, PROP_OPTION_POS, PROP_OPTION_VEL, PROP_OPTION_RAD };

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
    private OptionSelector<String> savedSimSelector;

    // EFFECTS: initialize simulation, init graphical/user input, redirect
    // sterr+stdout, and set simulation state to the opening screen
    public SimulationManager() throws Exception {
        initOutputStreams();
        simGraphics = new SimulationGraphics(this);
        initSimulationVariables();
        initEditorSelectors();

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
    }

    // EFFECTS: sets up editor selection related variables
    private void initEditorSelectors() {
        planetSelector = new OptionSelector<>(simulation.getPlanets(), KS_ARROWDOWN, KS_ARROWUP);
        propertySelector = new OptionSelector<>(Arrays.asList(PROP_OPTIONS), KS_ARROWDOWN, KS_ARROWUP);
        collisionSelector = new OptionSelector<>(simulation.getCollisions(), KS_ARROWDOWN, KS_ARROWUP);
        editorSelector = new OptionSelector<>(Arrays.asList(EDITOR_OPTIONS), KS_ARROWRIGHT, KS_ARROWLEFT);
        savedSimSelector = new OptionSelector<>(new ArrayList<String>(), KS_ARROWDOWN, KS_ARROWUP);

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

    public boolean isEditingSelectedPlanet() {
        return editingSelectedPlanet;
    }

    public boolean isEditingSelectedProperty() {
        return editingSelectedProperty;
    }

    public boolean isSimulationRunning() {
        return simulationIsRunning;
    }

    public String getUserInputString() {
        return userInputString;
    }

    // MODIFIES: this
    // EFFECTS: execute main input/rendering loop
    public void mainLoop() {
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
    private void handleTitleScreen() {
        simGraphics.drawTitleScreen();

        KeyStroke nextKey = null;
        try {
            nextKey = simGraphics.getScreen().pollInput();
        } catch (IOException e) {
            // DO NOTHING
        }

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
            simGraphics.drawEverything();
            handleUserInput();
            handleSimulationState();
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

        // NOTE: gets the "highest" element within a decently releavent class as to get
        // around nested error checking in the JDK
        for (int i = 0; i < callStack.length; i++) {
            if (isExceptionFromAppropriateClass(callStack[i])) {
                elemOfInterest = callStack[i];
                break;
            }
        }

        String className = elemOfInterest.getClassName();
        String method = elemOfInterest.getMethodName();
        int lineNum = elemOfInterest.getLineNumber();
        String excepNameAndMsg = exception.getClass().getSimpleName();
        System.err.print(className + "." + method + " line " + lineNum + " threw " + excepNameAndMsg);
    }

    // EFFECTS: returns whether the exception is from an appropriate class to view
    // in the error message
    private boolean isExceptionFromAppropriateClass(StackTraceElement elem) {
        if (elem.getClassName().equals(SimulationManager.class.getName())) {
            return true;
        }
        if (elem.getClassName().equals(SimulationGraphics.class.getName())) {
            return true;
        }
        if (elem.getClassName().equals(OptionSelector.class.getName())) {
            return true;
        }
        if (elem.getClassName().equals(ViewportEngine.class.getName())) {
            return true;
        }
        return false;
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
        if (planetSelector.getOptions().size() == 0) {
            simulationIsRunning = false;
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
        lastUserKey = simGraphics.getScreen().pollInput();
        if (lastUserKey == null) {
            return;
        }

        handleShouldQuit();
        handleSimulationPauseAndUnpause();
        editorSelector.cycleObjectSelection(lastUserKey);

        switch (editorSelector.getSelectedObject()) {
            case EDITOR_OPTION_PLANETS:
                handlePlanetViewUserInput();
                break;

            case EDITOR_OPTION_COLLISIONS:
                collisionSelector.cycleObjectSelection(lastUserKey);
                break;

            case EDITOR_OPTION_SAVELOAD:
                // TODO: implement
                break;

            default:
                assert false; // THIS SHOULD NEVER HAPPEN
                break;
        }
    }

    // MODIFES: this
    // EFFECTS: handles inputs when editor is viewing planet list
    private void handlePlanetViewUserInput() {
        if (editingSelectedPlanet) {
            if (editingSelectedProperty) {
                handleEditPlanetProperty();
                return;
            }

            propertySelector.cycleObjectSelection(lastUserKey);
            if (lastUserKey.getKeyType() == KeyType.Enter) {
                editingSelectedProperty = true;
            }
            if (lastUserKey.getKeyType() == KeyType.Escape) {
                editingSelectedPlanet = false;
            }

        } else {
            handlePlanetAddAndRemove();
            planetSelector.cycleObjectSelection(lastUserKey);

            if (lastUserKey.getKeyType() == KeyType.Enter) {
                editingSelectedPlanet = true;
            }
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
            userInputString = "";
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
            if (handleUserApplyPlanetEditAttempt()) {
                userInputString = "";
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
    private boolean handleUserApplyPlanetEditAttempt() {
        switch (propertySelector.getSelectedObject()) {
            case PROP_OPTION_NAME:
                return tryApplyNewName();

            case PROP_OPTION_POS:
                Vector3 newPos = tryParseVectorFromInputString();
                if (newPos == null) {
                    return false;
                }
                planetSelector.getSelectedObject().setPosition(newPos);
                return true;

            case PROP_OPTION_VEL:
                Vector3 newVel = tryParseVectorFromInputString();
                if (newVel == null) {
                    return false;
                }
                planetSelector.getSelectedObject().setVelocity(newVel);
                return true;

            case PROP_OPTION_RAD:
                return tryApplyNewRadius();

            default:
                assert false; // THIS SHOULD NEVER HAPPEN
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

        planetSelector.getSelectedObject().setName(userInputString);
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
        planetSelector.getSelectedObject().setRadius(newRadius);
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
        if (planetSelector.noSelectedObject()) {
            return;
        }

        Planet selectedPlanet = planetSelector.getSelectedObject();
        simulation.removePlanet(selectedPlanet);
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
        planetSelector.setSelectedObject(newPlanet);
    }
}
