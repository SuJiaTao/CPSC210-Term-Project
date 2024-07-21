package persistence;

import model.*;
import org.json.*;
import java.nio.*;
import java.nio.file.*;
import java.util.List;

public class JsonConverter {
    public static final String SAVE_PATH = "./data/";

    public static final String VECTOR3_KEY_X = "x";
    public static final String VECTOR3_KEY_Y = "y";
    public static final String VECTOR3_KEY_Z = "z";

    public static final String PLANET_KEY_NAME = "Name";
    public static final String PLANET_KEY_POSITION = "Positon";
    public static final String PLANET_KEY_VELOCITY = "Velocity";
    public static final String PLANET_KEY_RADIUS = "Radius";

    public static final String PLANETREF_KEY_TYPE = "Type";
    public static final String PLANETREF_VALUE_TYPE_INSIM = "InSim";
    public static final String PLANETREF_VALUE_TYPE_HISTORIC = "Historic";
    public static final String PLANETREF_KEY_INDEX = "ListIndex";

    public static final String COLLISION_KEY_PLANETREF1 = "PlanetRef1";
    public static final String COLLISION_KEY_PLANETREF2 = "PlanetRef2";
    public static final String COLLISION_KEY_TIMEOCCOURED = "TimeOccoured";

    public static JSONObject vector3ToJsonObject(Vector3 vector3) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.append(VECTOR3_KEY_X, Float.toString(vector3.getX()));
        jsonObject.append(VECTOR3_KEY_Y, Float.toString(vector3.getY()));
        jsonObject.append(VECTOR3_KEY_Z, Float.toString(vector3.getZ()));
        return jsonObject;
    }

    public static Vector3 jsonObjectToVector3(JSONObject jsonObject) {
        float x = Float.parseFloat(jsonObject.getString(VECTOR3_KEY_X));
        float y = Float.parseFloat(jsonObject.getString(VECTOR3_KEY_Y));
        float z = Float.parseFloat(jsonObject.getString(VECTOR3_KEY_Z));
        return new Vector3(x, y, z);
    }

    public static JSONObject planetToJsonObject(Planet planet) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.append(PLANET_KEY_NAME, planet.getName());
        jsonObject.append(PLANET_KEY_POSITION, vector3ToJsonObject(planet.getPosition()));
        jsonObject.append(PLANET_KEY_VELOCITY, vector3ToJsonObject(planet.getVelocity()));
        jsonObject.append(PLANET_KEY_RADIUS, Float.toString(planet.getRadius()));
        return jsonObject;
    }

    public static Planet jsonObjectToPlanet(JSONObject jsonObject) {
        String name = jsonObject.getString(PLANET_KEY_NAME);
        Vector3 position = jsonObjectToVector3(jsonObject.getJSONObject(PLANET_KEY_POSITION));
        Vector3 velocity = jsonObjectToVector3(jsonObject.getJSONObject(PLANET_KEY_VELOCITY));
        float radius = Float.parseFloat(jsonObject.getString(PLANET_KEY_RADIUS));
        return new Planet(name, position, velocity, radius);
    }

    public static JSONObject planetReferenceToJsonObject(Planet planetRef, Simulation parent) {
        JSONObject jsonObject = new JSONObject();

        int index = -1;
        String planetTypeValue = "";
        if ((index = parent.getPlanets().indexOf(planetRef)) != -1) {
            planetTypeValue = PLANETREF_VALUE_TYPE_INSIM;
        }
        if ((index = parent.getHistoricPlanets().indexOf(planetRef)) != -1) {
            planetTypeValue = PLANETREF_VALUE_TYPE_HISTORIC;
        }
        if (index == -1) {
            assert false; // this should never happen
        }

        jsonObject.append(PLANETREF_KEY_TYPE, planetTypeValue);
        jsonObject.append(PLANETREF_KEY_INDEX, Integer.toString(index));
        return jsonObject;
    }

    public static Planet jsonObjectToPlanetReference(JSONObject jsonObject, Simulation parent) {
        List<Planet> planetList = null;
        switch (jsonObject.getString(PLANETREF_KEY_TYPE)) {
            case PLANETREF_VALUE_TYPE_INSIM:
                planetList = parent.getPlanets();
                break;

            case PLANETREF_VALUE_TYPE_HISTORIC:
                planetList = parent.getHistoricPlanets();
                break;

            default:
                assert false; // THIS SHOULD NEVER HAPPEN
                break;
        }

        int index = Integer.parseInt(jsonObject.getString(PLANETREF_KEY_INDEX));
        return planetList.get(index);
    }

    public static JSONObject collisionToJsonObject(Collision collision, Simulation parent) {
        JSONObject jsonObject = new JSONObject();
        Planet planet1 = collision.getPlanetsInvolved().get(0);
        Planet planet2 = collision.getPlanetsInvolved().get(1);
        jsonObject.append(COLLISION_KEY_PLANETREF1, planetReferenceToJsonObject(planet1, parent));
        jsonObject.append(COLLISION_KEY_PLANETREF2, planetReferenceToJsonObject(planet2, parent));
        jsonObject.append(COLLISION_KEY_TIMEOCCOURED, Float.toString(collision.getCollisionTime()));
        return jsonObject;
    }

    public static Collision jsonObjectToCollision(JSONObject jsonObject, Simulation parent) {
        Planet planet1 = jsonObjectToPlanetReference(jsonObject.getJSONObject(COLLISION_KEY_PLANETREF1), parent);
        Planet planet2 = jsonObjectToPlanetReference(jsonObject.getJSONObject(COLLISION_KEY_PLANETREF2), parent);
        float timeOccoured = Float.parseFloat(jsonObject.getString(COLLISION_KEY_TIMEOCCOURED));
        return new Collision(planet1, planet2, timeOccoured);
    }

}
