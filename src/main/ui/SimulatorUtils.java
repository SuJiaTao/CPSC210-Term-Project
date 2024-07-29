package ui;

import model.Vector3;

// There are a handful of miscellanious parsing and calculating methods that would 
// otherwise bloat UI or simple logic code, which is kept here instead
public class SimulatorUtils {
    // EFFECTS: attempts to parse a string that reperesents a Vector3, returns null
    // if it fails
    public static Vector3 tryParseVector3(String str) {
        String[] strComponents = str.split(" ");
        if (strComponents.length != 3) {
            return null;
        }

        try {
            float valX = Float.parseFloat(strComponents[0]);
            float valY = Float.parseFloat(strComponents[0]);
            float valZ = Float.parseFloat(strComponents[0]);
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
}
