package persistence;

import model.*;
import org.json.*;
import java.nio.*;
import java.nio.file.*;

public class SimulationReadWriter {
    public static final String SAVE_PATH = "./data/";

    public static final String VECTOR3_KEY_X = "x";
    public static final String VECTOR3_KEY_Y = "y";
    public static final String VECTOR3_KEY_Z = "z";

    public static final String PLANET_KEY_NAME = "Name";
    public static final String PLANET_KEY_POSITION = "Positon";
    public static final String PLANET_KEY_VELOCITY = "Velocity";
    public static final String PLANET_KEY_RADIUS = "Radius";

    public static JSONObject convertVector3ToJsonObject(Vector3 vector3) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.append(VECTOR3_KEY_X, Float.toString(vector3.getX()));
        jsonObject.append(VECTOR3_KEY_Y, Float.toString(vector3.getY()));
        jsonObject.append(VECTOR3_KEY_Z, Float.toString(vector3.getZ()));
        return jsonObject;
    }

    public static Vector3 convertJsonObjectToVector3(JSONObject jsonObject) {
        float x = Float.parseFloat(jsonObject.getString(VECTOR3_KEY_X));
        float y = Float.parseFloat(jsonObject.getString(VECTOR3_KEY_Y));
        float z = Float.parseFloat(jsonObject.getString(VECTOR3_KEY_Z));
        return new Vector3(x, y, z);
    }

    public static JSONObject convertPlanetToJsonObject(Planet planet) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.append(PLANET_KEY_NAME, planet.getName());
        jsonObject.append(PLANET_KEY_POSITION, convertVector3ToJsonObject(planet.getPosition()));
        jsonObject.append(PLANET_KEY_VELOCITY, convertVector3ToJsonObject(planet.getVelocity()));
        jsonObject.append(PLANET_KEY_RADIUS, Float.toString(planet.getRadius()));
        return jsonObject;
    }

    public static Planet convertJsonObjectToPlanet(JSONObject jsonObject) {
        String name = jsonObject.getString(PLANET_KEY_NAME);
        Vector3 position = convertJsonObjectToVector3(jsonObject.getJSONObject(PLANET_KEY_POSITION));
        Vector3 velocity = convertJsonObjectToVector3(jsonObject.getJSONObject(PLANET_KEY_VELOCITY));
        float radius = Float.parseFloat(jsonObject.getString(PLANET_KEY_RADIUS));
        return new Planet(name, position, velocity, radius);
    }
}
