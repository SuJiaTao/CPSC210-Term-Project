package ui.engine;

import model.*;

// Represents a single triangle with vertex and uv data
public class Triangle {
    public Vector3 verts[];
    public Vector3 uvs[];

    // EFFECTS: initializes all verts and uv arrays as empty
    public Triangle() {
        verts = new Vector3[3];
        uvs = new Vector3[3];
    }

    // EFFECTS: initializes all verts and uv arrays with copies of the values of the
    // original triangle
    public Triangle(Triangle original) {
        verts = new Vector3[3];
        uvs = new Vector3[3];
        verts[0] = new Vector3(original.verts[0]);
        verts[1] = new Vector3(original.verts[1]);
        verts[2] = new Vector3(original.verts[2]);
        uvs[0] = new Vector3(original.uvs[0]);
        uvs[1] = new Vector3(original.uvs[1]);
        uvs[2] = new Vector3(original.uvs[2]);
    }
}
