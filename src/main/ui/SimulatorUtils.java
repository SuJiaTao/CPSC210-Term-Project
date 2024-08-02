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
    public static final String[] NEW_PLANET_NAMES = { "Kepler", "Earth", "Solaris", "Tatoonie", "Furball", "X",
            "Atlas", "Gemini", "Spongey", "Arrakis", "Paul", "Trapist", "Proxima", "Mundley", "Bongcloud", "Euclid",
            "Hades", "Jupiter", "Nguyen", "Draper", "Randy", "Draconis", "Cancri", "Awohali", "Vytis", "Igsael",
            "Chura", "Maskita", "Nanron", "Ugaris", "Yvaga", "Youssef", "Lebnitz", "Doodski", "Phobos", "WASP",
            "Mitski", "Cupid", "Demeter", "Saturn", "Sputnik", "Quix", "Pontus" };
    public static final int NEW_PLANET_SUFFIX_MAX = 1000;

    public enum PlanetVisualType {
        Star,
        GasGiant,
        Rocky,
        Asteroid
    };

    public static final float PLANET_STAR_MINRADIUS = 64.0f;
    public static final float PLANET_GASGIANT_MINRADIUS = 16.0f;
    public static final float PLANET_ROCKY_MINRADIUS = 4.0f;

    private static final int EDIT_FIELD_COLUMNS = 20;
    private static final String IMAGE_PATH = "./data/image/";
    private static final Random RANDOM = new Random();

    // EFFECTS: returns the visual type of a given planet
    public static PlanetVisualType getPlanetVisualType(Planet planet) {
        float radius = planet.getRadius();
        if (radius >= PLANET_STAR_MINRADIUS) {
            return PlanetVisualType.Star;
        }
        if (radius >= PLANET_GASGIANT_MINRADIUS) {
            return PlanetVisualType.GasGiant;
        }
        if (radius >= PLANET_ROCKY_MINRADIUS) {
            return PlanetVisualType.Rocky;
        }
        return PlanetVisualType.Asteroid;
    }

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

    // EFFECTS: creates a new random planet and returns it
    public static Planet createNewPlanet() {
        float posX = (RANDOM.nextFloat() - 0.5f) * NEW_PLANET_INITIAL_POS_BOUND;
        float posY = (RANDOM.nextFloat() - 0.5f) * NEW_PLANET_INITIAL_POS_BOUND;
        float posZ = (RANDOM.nextFloat() - 0.5f) * NEW_PLANET_INITIAL_POS_BOUND;
        Vector3 newPos = new Vector3(posX, posY, posZ);

        float velX = (RANDOM.nextFloat() - 0.5f) * NEW_PLANET_INITIAL_VEL_BOUND;
        float velY = (RANDOM.nextFloat() - 0.5f) * NEW_PLANET_INITIAL_VEL_BOUND;
        float velZ = (RANDOM.nextFloat() - 0.5f) * NEW_PLANET_INITIAL_VEL_BOUND;
        Vector3 newVel = new Vector3(velX, velY, velZ);

        float scale = NEW_PLANET_MIN_RAD + RANDOM.nextFloat() * (NEW_PLANET_MAX_RAD - NEW_PLANET_MIN_RAD);

        return new Planet(generateNewPlanetName(), newPos, newVel, scale);
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
