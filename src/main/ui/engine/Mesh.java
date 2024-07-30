package ui.engine;

import model.*;
import ui.SimulatorUtils;

import java.io.File;
import java.util.*;

// Mesh object for containing info on object geometry
public class Mesh {
    private static final String MESH_PATH = "./data/mesh/";
    private static final int INDICIE_ELEMENTS_PER_TRI = 6;

    private Vector3[] verts;
    private Vector3[] uvs;
    private int[] indicies;

    public static Mesh getSphereMesh() {
        return loadMeshFromObjFile("sphere.obj");
    }

    // EFFECTS: initializes a mesh based on the given objects
    private Mesh(Vector3[] verts, Vector3[] uvs, int[] indicies) {
        this.verts = Arrays.copyOf(verts, verts.length);
        this.uvs = Arrays.copyOf(uvs, verts.length);
        this.indicies = Arrays.copyOf(indicies, verts.length);
    }

    public int getTriangleCount() {
        return indicies.length / 3;
    }

    public Triangle getTriangle(int triangle) {
        Triangle tri = new Triangle();

        tri.verts[0] = verts[indicies[(triangle * INDICIE_ELEMENTS_PER_TRI) + 0]];
        tri.verts[1] = verts[indicies[(triangle * INDICIE_ELEMENTS_PER_TRI) + 2]];
        tri.verts[2] = verts[indicies[(triangle * INDICIE_ELEMENTS_PER_TRI) + 4]];

        tri.uvs[0] = uvs[indicies[(triangle * INDICIE_ELEMENTS_PER_TRI) + 1]];
        tri.uvs[1] = uvs[indicies[(triangle * INDICIE_ELEMENTS_PER_TRI) + 3]];
        tri.uvs[2] = uvs[indicies[(triangle * INDICIE_ELEMENTS_PER_TRI) + 5]];

        return tri;
    }

    private static Mesh loadMeshFromObjFile(String fileName) {
        File modelFile = new File(MESH_PATH + fileName);
        if (!modelFile.isFile()) {
            throw new IllegalStateException(); // shouldnt happen
        }

        Scanner scanner = null;
        try {
            scanner = new Scanner(modelFile);
        } catch (Exception e) {
            scanner.close();
            throw new IllegalStateException(); // shouldnt happen
        }

        ArrayList<Vector3> vertList = new ArrayList<>();
        ArrayList<Vector3> uvList = new ArrayList<>();
        ArrayList<Integer> indexList = new ArrayList<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] lines = line.split(" ");

            if (line.length() < 1) {
                continue;
            }

            switch (lines[0]) {
                case "v":
                    vertList.add(SimulatorUtils.tryParseVector3(line.substring(lines[0].length())));
                    break;

                case "vt":
                    uvList.add(SimulatorUtils.tryParseVector3(line.substring(lines[0].length()) + " 0.0"));
                    break;

                case "f":
                    // NOTE:
                    // it is assumed that the obj file is exported WITHOUT normals.
                    String[] faceVertexAttribs0 = lines[1].split("/");
                    indexList.add(Integer.parseInt(faceVertexAttribs0[0]));
                    indexList.add(Integer.parseInt(faceVertexAttribs0[1]));

                    String[] faceVertexAttribs1 = lines[2].split("/");
                    indexList.add(Integer.parseInt(faceVertexAttribs1[0]));
                    indexList.add(Integer.parseInt(faceVertexAttribs1[1]));

                    String[] faceVertexAttribs2 = lines[3].split("/");
                    indexList.add(Integer.parseInt(faceVertexAttribs2[0]));
                    indexList.add(Integer.parseInt(faceVertexAttribs2[1]));
                    break;

                default:
                    break;
            }
        }

        scanner.close();

        int[] indicieArray = new int[indexList.size()];
        for (int i = 0; i < indicieArray.length; i++) {
            indicieArray[i] = indexList.get(i);
        }
        Vector3[] vertexArray = new Vector3[vertList.size()];
        for (int i = 0; i < vertexArray.length; i++) {
            vertexArray[i] = vertList.get(i);
        }
        Vector3[] uvArray = new Vector3[vertList.size()];
        for (int i = 0; i < vertexArray.length; i++) {
            uvArray[i] = uvList.get(i);
        }
        return new Mesh(vertexArray, uvArray, indicieArray);
    }
}
