package ui.engine;

import model.*;

// Primitive line shader which shades in a flat color
public class LineShader extends AbstractShader {
    private int color;

    // EFFECTS: initializes color
    public LineShader(int color) {
        this.color = color;
    }

    // EFFECTS: returns color provided by the user, regardless of UV
    @Override
    public int shade(Vector3 weights, Vector3 uv) {
        return color;
    }
}
