package ui;

import model.*;
import java.util.*;

// There are a handful of miscellanious parsing and calculating methods that would 
// otherwise bloat UI or simple logic code, which is kept here instead
public class SimulatorUtils {
    private static final String[] NEW_PLANET_NAMES = { "Kepler", "Earth", "Solaris", "Tatoonie", "Furball", "X",
            "Atlas", "Gemini", "Spongey", "Arrakis", "Paul", "Trapist", "Proxima", "Mundley", "Bongcloud", "Euclid",
            "Hades", "Jupiter", "Draper", "Randy", "Draconis", "Cancri", "Awohali", "Vytis", "Igsael", "Chura",
            "Maskita", "Nanron", "Ugaris", "Yvaga", "Youssef", "Lebnitz", "Doodski", "Phobos", "WASP", "Mitski" };
    private static final int NEW_PLANET_SUFFIX_MAX = 1000;
    private static final float NEW_PLANET_INITIAL_POS_BOUND = 30.0f;
    private static final float NEW_PLANET_INITIAL_VEL_BOUND = 2.0f;
    private static final float NEW_PLANET_MIN_RAD = 0.5f;
    private static final float NEW_PLANET_MAX_RAD = 1.5f;

    // EFFECTS: creates a new random planet and returns it
    public static Planet createNewPlanet() {
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

        return new Planet(name + "-" + numberSuffix, newPos, newVel, scale);
    }

    // MODIFIES: simDestination
    // EFFECTS: copies all values of simSource to simDestination
    public static void transferSimData(Simulation simDestination, Simulation simSource) {
        simDestination.setTimeElapsed(simSource.getTimeElapsed());

        simDestination.getPlanets().clear();
        simDestination.getPlanets().addAll(simSource.getPlanets());

        simDestination.getHistoricPlanets().clear();
        simDestination.getHistoricPlanets().addAll(simSource.getHistoricPlanets());

        simDestination.getCollisions().clear();
        simDestination.getCollisions().addAll(simSource.getCollisions());
    }

    // EFFECTS: checks whether string is valid planet name
    public static boolean checkIfValidPlanetName(String str) {
        return (str.length() > 0 && str.charAt(0) != ' ');
    }

    // EFFECTS: attempts to parse a string that reperesents a Vector3, returns null
    // if it fails
    public static Vector3 tryParseVector3(String str) {
        String[] strComponents = str.split(" ");

        try {
            float valX = Float.parseFloat(strComponents[0]);
            float valY = Float.parseFloat(strComponents[1]);
            float valZ = Float.parseFloat(strComponents[2]);
            return new Vector3(valX, valY, valZ);
        } catch (Exception exp) {
            return null;
        }
    }

    // EFFECTS: attempts to parse a string that represents a float, returns null if
    // it fails
    public static Float tryParseFloat(String str) {
        try {
            return Float.parseFloat(str);
        } catch (Exception exp) {
            return null;
        }
    }

    // EFFECTS: converts the Vector3.toString() string into the appropriately
    // parseable one for the UI
    public static String convertVectorStringToParseable(String vecString) {
        return vecString.replaceAll("[()]", "");
    }
}
