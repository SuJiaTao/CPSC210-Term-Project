package persistence;

import model.*;
import java.io.*;
import org.json.*;
import java.util.*;

public class SimulationReadWriter {
    public static final String SAVE_PATH = "./data/";
    public static final int TAB_SPACES = 4;

    public static File fileFromFileTitle(String fileTitle) {
        File file = new File(SAVE_PATH + fileTitle + ".json");
        file.getParentFile().mkdirs();
        return file;
    }

    public static void writeSimulation(Simulation simulation, String fileTitle)
            throws IOException, FileNotFoundException {
        JSONObject jsonSimulation = JsonConverter.simulationToJsonObject(simulation);
        File writeFile = fileFromFileTitle(fileTitle);
        if (!writeFile.isFile()) {
            writeFile.createNewFile();
        }

        PrintWriter writeStream = new PrintWriter(writeFile);
        writeStream.print(jsonSimulation.toString(TAB_SPACES));
        writeStream.flush();
        writeStream.close();
    }

    public static Simulation readSimulation(String fileTitle) throws FileNotFoundException {
        File readFile = fileFromFileTitle(fileTitle);
        if (!readFile.isFile()) {
            throw new FileNotFoundException();
        }

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
