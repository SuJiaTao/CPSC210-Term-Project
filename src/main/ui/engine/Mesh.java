package ui.engine;

import model.*;
import java.util.*;

// Mesh object for containing info on object geometry
public class Mesh {
    private static final int INDICIE_ELEMENTS_PER_TRI = 6;

    private Vector3 verts[];
    private Vector3 uvs[];
    private int[] indicies;

    public static Mesh createSphereMesh() {

    }

    // EFFECTS: initializes a mesh based on the given objects
    private Mesh(Vector3 verts[], Vector3 uvs[], int[] indicies) {
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
}
