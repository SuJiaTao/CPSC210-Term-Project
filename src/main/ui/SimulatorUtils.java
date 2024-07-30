package ui;

import model.*;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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

    private static final int EDIT_FIELD_COLUMNS = 20;

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
