package persistence;

import model.*;
import model.exceptions.PlanetDoesntExistException;

import org.json.*;
import java.util.*;

public class JsonConverter {
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

    public static final String SIM_KEY_TIME_ELAPSED = "TimeElapsed";
    public static final String SIM_KEY_PLANETS_INSIM = "PlanetsInSim";
    public static final String SIM_KEY_PLANETS_HISTORIC = "PlanetsHistoric";
    public static final String SIM_KEY_COLLISIONS = "Collisions";

    private JsonConverter() {
        // instantiation not allowed
    }

    // EFFECTS: converts a Vector3 into a JSONObject
    public static JSONObject vector3ToJsonObject(Vector3 vector3) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(VECTOR3_KEY_X, Float.toString(vector3.getX()));
        jsonObject.put(VECTOR3_KEY_Y, Float.toString(vector3.getY()));
        jsonObject.put(VECTOR3_KEY_Z, Float.toString(vector3.getZ()));
        return jsonObject;
    }

    // EFFECTS: converts a JSONObject into a Vector3
    public static Vector3 jsonObjectToVector3(JSONObject jsonObject) {
        float x = Float.parseFloat(jsonObject.getString(VECTOR3_KEY_X));
        float y = Float.parseFloat(jsonObject.getString(VECTOR3_KEY_Y));
        float z = Float.parseFloat(jsonObject.getString(VECTOR3_KEY_Z));
        return new Vector3(x, y, z);
    }

    // EFFECTS: converts a planet to a JSONObject
    public static JSONObject planetToJsonObject(Planet planet) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(PLANET_KEY_NAME, planet.getName());
        jsonObject.put(PLANET_KEY_POSITION, vector3ToJsonObject(planet.getPosition()));
        jsonObject.put(PLANET_KEY_VELOCITY, vector3ToJsonObject(planet.getVelocity()));
        jsonObject.put(PLANET_KEY_RADIUS, Float.toString(planet.getRadius()));
        return jsonObject;
    }

    // EFFFECTS: converts a JSONObject into a planet
    public static Planet jsonObjectToPlanet(JSONObject jsonObject) {
        String name = jsonObject.getString(PLANET_KEY_NAME);
        Vector3 position = jsonObjectToVector3(jsonObject.getJSONObject(PLANET_KEY_POSITION));
        Vector3 velocity = jsonObjectToVector3(jsonObject.getJSONObject(PLANET_KEY_VELOCITY));
        float radius = Float.parseFloat(jsonObject.getString(PLANET_KEY_RADIUS));
        return new Planet(name, position, velocity, radius);
    }

    // EFFECTS: creates a JSONObject which represents a reference to a planet within
    // the parent simulation
    public static JSONObject planetReferenceToJsonObject(Planet planetRef, Simulation parent) {
        JSONObject jsonObject = new JSONObject();

        int index = -1;
        String planetTypeValue = "";
        if (parent.getPlanets().contains(planetRef)) {
            planetTypeValue = PLANETREF_VALUE_TYPE_INSIM;
            index = parent.getPlanets().indexOf(planetRef);
        } else if (parent.getHistoricPlanets().contains(planetRef)) {
            planetTypeValue = PLANETREF_VALUE_TYPE_HISTORIC;
            index = parent.getHistoricPlanets().indexOf(planetRef);
        } else {
            throw new PlanetDoesntExistException();
        }

        jsonObject.put(PLANETREF_KEY_TYPE, planetTypeValue);
        jsonObject.put(PLANETREF_KEY_INDEX, Integer.toString(index));
        return jsonObject;
    }

    // EFFECTS: given a JSONObject representing a reference to a planet in the
    // parent simulation, return that planet, if it can be found. Throws a
    // JSONException otherwise
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
                throw new JSONException("invalid jsonObject value for key: " + PLANETREF_KEY_TYPE);
        }

        int index = Integer.parseInt(jsonObject.getString(PLANETREF_KEY_INDEX));
        if (index >= planetList.size()) {
            throw new JSONException("invalid jsonObject value for key: " + PLANETREF_KEY_TYPE);
        }
        return planetList.get(index);
    }

    // EFFECTS: converts a collision to a JSONObject, using references to planets in
    // the parent simulation
    public static JSONObject collisionToJsonObject(Collision collision, Simulation parent) {
        JSONObject jsonObject = new JSONObject();
        Planet planet1 = collision.getPlanetsInvolved().get(0);
        Planet planet2 = collision.getPlanetsInvolved().get(1);
        jsonObject.put(COLLISION_KEY_PLANETREF1, planetReferenceToJsonObject(planet1, parent));
        jsonObject.put(COLLISION_KEY_PLANETREF2, planetReferenceToJsonObject(planet2, parent));
        jsonObject.put(COLLISION_KEY_TIMEOCCOURED, Float.toString(collision.getCollisionTime()));
        return jsonObject;
    }

    // EFFECTS: converts a JSONObject into a collision, with references to planets
    // in the parent simulation
    public static Collision jsonObjectToCollision(JSONObject jsonObject, Simulation parent) {
        Planet planet1 = jsonObjectToPlanetReference(jsonObject.getJSONObject(COLLISION_KEY_PLANETREF1), parent);
        Planet planet2 = jsonObjectToPlanetReference(jsonObject.getJSONObject(COLLISION_KEY_PLANETREF2), parent);
        float timeOccoured = Float.parseFloat(jsonObject.getString(COLLISION_KEY_TIMEOCCOURED));
        return new Collision(planet1, planet2, timeOccoured);
    }

    // EFFECTS: converts a list of planets into a JSONArray
    private static JSONArray planetListToJsonArray(List<Planet> planetList) {
        JSONArray jsonArray = new JSONArray();
        for (Planet planet : planetList) {
            jsonArray.put(planetToJsonObject(planet));
        }
        return jsonArray;
    }

    // EFFECTS: converts a list of collisions into a JSONArray
    private static JSONArray collisionListToJsonArray(List<Collision> collisionlist, Simulation parent) {
        JSONArray jsonArray = new JSONArray();
        for (Collision collision : collisionlist) {
            jsonArray.put(collisionToJsonObject(collision, parent));
        }
        return jsonArray;
    }

    // EFFECTS: converts a simulation to a JSONObject
    public static JSONObject simulationToJsonObject(Simulation simulation) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(SIM_KEY_TIME_ELAPSED, Float.toString(simulation.getTimeElapsed()));
        jsonObject.put(SIM_KEY_PLANETS_INSIM, planetListToJsonArray(simulation.getPlanets()));
        jsonObject.put(SIM_KEY_PLANETS_HISTORIC, planetListToJsonArray(simulation.getHistoricPlanets()));
        jsonObject.put(SIM_KEY_COLLISIONS, collisionListToJsonArray(simulation.getCollisions(), simulation));
        return jsonObject;
    }

    // EFFECTS: converts a JSONObject into a simulation
    public static Simulation jsonObjectToSimulation(JSONObject jsonObject) {
        Simulation simulation = new Simulation();

        float timeElapsed = Float.parseFloat(jsonObject.getString(SIM_KEY_TIME_ELAPSED));
        simulation.setTimeElapsed(timeElapsed);

        for (Object jsonPlanet : jsonObject.getJSONArray(SIM_KEY_PLANETS_INSIM)) {
            simulation.addPlanet(jsonObjectToPlanet((JSONObject) jsonPlanet));
        }

        for (Object jsonHistoricPlanet : jsonObject.getJSONArray(SIM_KEY_PLANETS_HISTORIC)) {
            simulation.addHistoricPlanet(jsonObjectToPlanet((JSONObject) jsonHistoricPlanet));
        }

        for (Object jsonCollision : jsonObject.getJSONArray(SIM_KEY_COLLISIONS)) {
            simulation.addCollision(jsonObjectToCollision((JSONObject) jsonCollision, simulation));
        }

        return simulation;
    }
}
