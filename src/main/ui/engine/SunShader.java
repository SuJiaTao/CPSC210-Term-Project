package ui.engine;

import model.*;
import java.util.*;
import java.awt.image.*;

// Sunshader which jitters around a texture to give a firey effect
public class SunShader extends TextureShader {
    private static final Random RANDOM = new Random();
    private static final float WIGGLE_FACTOR = 10.0f;
    private final float uvWiggle;

    // EFFECTS: initializes the texture and uv wiggle factor
    public SunShader(BufferedImage texture) {
        super(texture);
        uvWiggle = WIGGLE_FACTOR / (float) texture.getWidth();
    }

    // EFFECTS: jiggles the uv based on noise and returns the texture with the
    // jiggled uv
    @Override
    public int shade(Vector3 weights, Vector3 uv) {
        float uvWiggleU = RANDOM.nextFloat() * uvWiggle;
        float uvWiggleV = RANDOM.nextFloat() * uvWiggle;
        uv = Vector3.add(new Vector3(uvWiggleU, uvWiggleV, 0.0f), uv);
        return super.shade(weights, uv);
    }
}
