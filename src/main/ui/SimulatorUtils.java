package ui;

import model.Vector3;

// There are a handful of miscellanious parsing and calculating methods that would 
// otherwise bloat UI or simple logic code, which is kept here instead
public class SimulatorUtils {
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
