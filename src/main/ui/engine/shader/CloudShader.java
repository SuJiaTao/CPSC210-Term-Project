package ui.engine.shader;

import model.*;
import java.util.*;
import java.awt.image.*;

// Shader which renders semi-transparent clouds based on the red channel of a alpha mask
public class CloudShader extends AbstractShader {
    private static final Random RANDOM = new Random();
    private BufferedImage alphaMask;
    private float maxAlpha;

    // EFFECTS: saves the specified texture as an alpha mask
    public CloudShader(BufferedImage texture, float maxAlpha) {
        this.alphaMask = texture;
        this.maxAlpha = maxAlpha;
    }

    // EFFECTS: checks the red channel of the texture alpha mask and discards the
    // fragment based on noise accordingly
    @Override
    public int shade(Vector3 weights, Vector3 uv) {
        int alphaWeight = super.sample(alphaMask, uv);
        if (RANDOM.nextInt(0xFF) > (int) (maxAlpha * (alphaWeight & 0xFF))) {
            return CULL_FRAGMENT;
        }
        return 0xFFFFFFFF;
    }
}
