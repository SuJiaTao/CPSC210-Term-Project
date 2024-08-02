package ui;

import model.*;
import java.awt.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// There are a handful of miscellanious parsing, calculating, and UI methods that would 
// bloat otherwise unrelated code, so it is kept here instead
public class SimulatorUtils {
    private static final String[] NEW_PLANET_NAMES = { "Kepler", "Earth", "Solaris", "Tatoonie", "Furball", "X",
            "Atlas", "Gemini", "Spongey", "Arrakis", "Paul", "Trapist", "Proxima", "Mundley", "Bongcloud", "Euclid",
            "Hades", "Jupiter", "Nguyen", "Draper", "Randy", "Draconis", "Cancri", "Awohali", "Vytis", "Igsael",
            "Chura", "Maskita", "Nanron", "Ugaris", "Yvaga", "Youssef", "Lebnitz", "Doodski", "Phobos", "WASP",
            "Mitski", "Cupid", "Demeter", "Saturn", "Sputnik", "Quix", "Pontus" };
    private static final int NEW_PLANET_SUFFIX_MAX = 1000;

    public enum PlanetType {
        Star, GasGiant, Rocky, Asteroid
    }

    private static final float PLANET_STAR_MAXRADIUS = 160.0f;
    private static final float PLANET_STAR_MINRADIUS = 110.0f;
    private static final float PLANET_GASGIANT_MAXRADIUS = 60.0f;
    private static final float PLANET_GASGIANT_MINRADIUS = 30.5f;
    private static final float PLANET_ROCKY_MAXRADIUS = 13.0f;
    private static final float PLANET_ROCKY_MINRADIUS = 4.0f;

    private static final float GASGIANT_ORBIT_MINMULTIPLE = 15.0f;
    private static final float GASGIANT_ORBIT_MAXMULTIPLE = 65.0f;
    private static final float ROCKY_ORBIT_MINMULTIPLE = 5.0f;
    private static final float ROCKY_ORBIT_MAXMULTIPLE = 20.0f;
    private static final float PLANET_ORBIT_ROTVARIANCE = 20.0f;
    private static final float PLANET_ORBIT_VELVARIANCE = 0.25f;

    private static final float NEWEWST_STAR_PUSHBACK_FACTOR = 15.0f;

    private static final int MAX_GASGIANT_TO_STAR_RATIO = 12;
    private static final int MAX_ROCKY_TO_GASGIANT_RATIO = 2;

    private static final int EDIT_FIELD_COLUMNS = 20;
    private static final String IMAGE_PATH = "./data/image/";
    private static final Random RANDOM = new Random();

    // EFFECTS: loads image
    public static BufferedImage loadImage(String imgName) {
        try {
            return ImageIO.read(new File(IMAGE_PATH + imgName));
        } catch (IOException err) {
            throw new IllegalStateException(); // shouldnt happen
        }
    }

    // EFFECTS: generates a possible name of a new planet
    public static String generateNewPlanetName() {
        String name = NEW_PLANET_NAMES[RANDOM.nextInt(NEW_PLANET_NAMES.length)];
        String numberSuffix = String.format("%03d", RANDOM.nextInt(NEW_PLANET_SUFFIX_MAX));
        return name + "-" + numberSuffix;
    }

    // EFFECTS: returns the type of a given planet
    public static PlanetType getPlanetType(Planet planet) {
        float radius = planet.getRadius();
        if (radius >= PLANET_STAR_MINRADIUS) {
            return PlanetType.Star;
        }
        if (radius >= PLANET_GASGIANT_MINRADIUS) {
            return PlanetType.GasGiant;
        }
        if (radius >= PLANET_ROCKY_MINRADIUS) {
            return PlanetType.Rocky;
        }
        return PlanetType.Asteroid;
    }

    // EFFECTS: creates a new planet based on the existing planets and returns it
    public static Planet createNewPlanet() {
        SimulatorState simState = SimulatorState.getInstance();

        simState.lock();

        java.util.List<Planet> planetList = simState.getSimulation().getPlanets();
        ArrayList<Planet> starPlanets = new ArrayList<>();
        ArrayList<Planet> gasPlanets = new ArrayList<>();
        ArrayList<Planet> rockyPlanets = new ArrayList<>();
        for (Planet planet : planetList) {
            switch (getPlanetType(planet)) {
                case Star:
                    starPlanets.add(planet);
                    break;
                case GasGiant:
                    gasPlanets.add(planet);
                    break;
                case Rocky:
                    rockyPlanets.add(planet);
                    break;
                default:
                    break;
            }
        }

        if (starPlanets.size() == 0) {
            simState.unlock();
            Planet newestStar = createNewStar();
            newestStar.setPosition(new Vector3(0, 0, -newestStar.getRadius() * NEWEWST_STAR_PUSHBACK_FACTOR));
            return newestStar;
        }
        if ((gasPlanets.size() != 0) && (rockyPlanets.size() < gasPlanets.size() * MAX_ROCKY_TO_GASGIANT_RATIO)) {
            Planet toOrbit = gasPlanets.get(RANDOM.nextInt(gasPlanets.size()));
            float newRadius = randomFloatInRangeGaussian(PLANET_ROCKY_MINRADIUS, PLANET_ROCKY_MAXRADIUS);
            Planet newRocky = new Planet(generateNewPlanetName(), newRadius);
            setPlanetToOrbit(newRocky, toOrbit, ROCKY_ORBIT_MINMULTIPLE, ROCKY_ORBIT_MAXMULTIPLE);

            simState.unlock();
            return newRocky;
        }
        if (gasPlanets.size() < starPlanets.size() * MAX_GASGIANT_TO_STAR_RATIO) {
            Planet toOrbit = starPlanets.get(RANDOM.nextInt(starPlanets.size()));
            float newRadius = randomFloatInRangeGaussian(PLANET_GASGIANT_MINRADIUS, PLANET_GASGIANT_MAXRADIUS);
            Planet newGasPlanet = new Planet(generateNewPlanetName(), newRadius);
            setPlanetToOrbit(newGasPlanet, toOrbit, GASGIANT_ORBIT_MINMULTIPLE, GASGIANT_ORBIT_MAXMULTIPLE);

            simState.unlock();
            return newGasPlanet;
        }

        simState.unlock();
        return createNewStar();
    }

    private static float getOrbitalVelocity(Planet planet, float radius) {
        float circularVel = (float) Math.sqrt(planet.getMass() * Simulation.GRAVITATIONAL_CONSTANT / radius);
        return circularVel
                * randomFloatInRange(1.0f - PLANET_ORBIT_VELVARIANCE, 1.0f + PLANET_ORBIT_VELVARIANCE);
    }

    private static void setPlanetToOrbit(Planet orbiter, Planet orbitee, float minMultiple, float maxMultiple) {
        float orbitRadius = randomFloatInRange(orbitee.getRadius() * minMultiple, orbitee.getRadius() * maxMultiple);
        Vector3 orbitRotation = new Vector3(
                randomFloatInRangeGaussian(-PLANET_ORBIT_ROTVARIANCE, PLANET_ORBIT_ROTVARIANCE),
                randomFloatInRange(-360.0f, 360.0f),
                randomFloatInRangeGaussian(-PLANET_ORBIT_ROTVARIANCE, PLANET_ORBIT_ROTVARIANCE));
        Transform rotationTransform = Transform.rotation(orbitRotation);
        Vector3 orbitPosOrigin = Transform.multiply(rotationTransform, new Vector3(orbitRadius, 0, 0));
        Vector3 orbitVelocity = Transform.multiply(rotationTransform,
                new Vector3(0, 0, getOrbitalVelocity(orbitee, orbitRadius)));
        orbiter.setPosition(Vector3.add(orbitee.getPosition(), orbitPosOrigin));
        orbiter.setVelocity(Vector3.add(orbitee.getVelocity(), orbitVelocity));
    }

    private static Planet createNewStar() {
        float newRadius = randomFloatInRangeGaussian(PLANET_STAR_MINRADIUS, PLANET_STAR_MAXRADIUS);
        Vector3 newPos = new Vector3(
                randomFloatInRange(-newRadius * GASGIANT_ORBIT_MINMULTIPLE, newRadius * GASGIANT_ORBIT_MINMULTIPLE),
                randomFloatInRange(-newRadius * GASGIANT_ORBIT_MINMULTIPLE, newRadius * GASGIANT_ORBIT_MINMULTIPLE),
                randomFloatInRange(-newRadius * GASGIANT_ORBIT_MINMULTIPLE, newRadius * GASGIANT_ORBIT_MINMULTIPLE));
        return new Planet(generateNewPlanetName(), newPos, new Vector3(), newRadius);
    }

    // REQUIRES: min <= max
    // EFFECTS: returns a random float between a given range
    private static float randomFloatInRange(float min, float max) {
        return min + (RANDOM.nextFloat() * (max - min));
    }

    // REQUIRES: min <= max
    // EFFECTS: returns a random float between a given range, with gauss
    // distribution
    private static float randomFloatInRangeGaussian(float min, float max) {
        float mean = (min + max) / 2.0f;
        float deviation = (max - min) / 6.0f;
        return mean + deviation * (float) RANDOM.nextGaussian();
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

    // EFFECTS: creates a "Title" JLabel and returns it
    public static JLabel makeTitleLabel(String message) {
        JLabel title = new JLabel(message);
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setVerticalAlignment(JLabel.CENTER);
        title.setFont(new Font(title.getFont().getName(),
                Font.BOLD, 15));
        return title;
    }

    // EFFECTS: creates GridBagConstraints at the specific row and column, with the
    // specified with and with some padding around it
    public static GridBagConstraints makeGbConstraints(int gx, int gy, int width) {
        GridBagConstraints gbConst = new GridBagConstraints();
        gbConst.fill = GridBagConstraints.BOTH;
        gbConst.gridx = gx;
        gbConst.gridy = gy;
        gbConst.gridwidth = width;
        gbConst.weightx = 0.5;
        gbConst.insets = new Insets(1, 5, 1, 5);
        return gbConst;
    }

    // EFFECTS: makes a property field which is labelled on the right, adds it to
    // the parent, adds the listener to it, and returns it
    public static JTextField initAndAddPropertyEditField(JPanel parent, ActionListener listener, String title,
            int height) {
        parent.add(new JLabel(title, JLabel.RIGHT), SimulatorUtils.makeGbConstraints(0, height, 1));
        JTextField textField = new JTextField(EDIT_FIELD_COLUMNS);
        if (listener != null) {
            textField.addActionListener(listener);
        }
        parent.add(textField, SimulatorUtils.makeGbConstraints(1, height, 2));
        return textField;
    }

    // EFFECTS: checks whether string is valid name
    public static boolean checkIfValidName(String str) {
        return (str != null && str.length() > 0 && str.charAt(0) != ' ');
    }

    // EFFECTS: attempts to parse a string that reperesents a Vector3, returns null
    // if it fails
    public static Vector3 tryParseVector3(String str) {
        String[] strComponents = str.trim().split(" ");

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
