package ui.engine;

import model.*;

public class LineShader extends AbstractShader {
    private int color;

    public LineShader(int color) {
        this.color = color;
    }

    @Override
    public int shade(Vector3 weights, Vector3 uv) {
        return color;
    }
}
