package ui.engine.shader;

import model.*;

import java.awt.image.*;

// Simple texture shader which always samples the color from a specified texture
public class TextureShader extends AbstractShader {
    private BufferedImage texture;

    // EFFECTS: saves the specified texture for later
    public TextureShader(BufferedImage texture) {
        this.texture = texture;
    }

    // EFFECTS: returns the color of the specified texture at the given UV
    @Override
    public int shade(Vector3 weights, Vector3 uv) {
        int texSample = sample(texture, uv);
        return texSample;
    }
}
