package ui.engine;

import java.util.Arrays;

import model.*;

// Represents a single triangle with vertex and uv data
public class Triangle {
    public Vector3 verts[];
    public Vector3 uvs[];

    public Triangle() {
        verts = new Vector3[3];
        uvs = new Vector3[3];
    }

    public Triangle(Triangle original) {
        verts = Arrays.copyOf(original.verts, 3);
        uvs = Arrays.copyOf(original.uvs, 3);
    }
}
