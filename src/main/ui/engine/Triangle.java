package ui.engine;

import model.*;

// Represents a single triangle with vertex and uv data
public class Triangle {
    public Vector3 verts[];
    public Vector3 uvs[];

    public Triangle() {
        verts = new Vector3[3];
        uvs = new Vector3[3];
    }
}
