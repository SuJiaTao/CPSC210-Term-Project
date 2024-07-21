package persistence;

import model.*;
import java.io.*;
import org.json.*;
import java.util.*;

public class SimulationReadWriter {
    public static final String SAVE_PATH = "./data/";
    public static final int TAB_SPACES = 4;

    private static File fileFromFileTitle(String fileTitle) throws FileNotFoundException {
        File file = new File(SAVE_PATH + fileTitle + ".json");
        if (!file.isFile()) {
            throw new FileNotFoundException();
        }
        return file;
    }

    public static void writeSimulation(Simulation simulation, String fileTitle) throws FileNotFoundException {
        JSONObject jsonSimulation = JsonConverter.simulationToJsonObject(simulation);
        File writeFile = fileFromFileTitle(fileTitle);

        PrintStream writeStream = new PrintStream(writeFile);
        writeStream.print(jsonSimulation.toString(TAB_SPACES));
        writeStream.flush();
        writeStream.close();
    }

    public static Simulation readSimulation(String fileTitle) throws FileNotFoundException {
        File readFile = fileFromFileTitle(fileTitle);

        String jsonStringBuffer = "";
        Scanner readScanner = new Scanner(readFile);
        while (readScanner.hasNextLine()) {
            jsonStringBuffer += readScanner.nextLine();
        }
        readScanner.close();

        JSONObject jsonSimObject = new JSONObject(jsonStringBuffer);
        return JsonConverter.jsonObjectToSimulation(jsonSimObject);
    }
}
