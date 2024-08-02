package ui.engine;

import model.*;
import java.util.*;
import java.awt.image.*;

public class SunShader extends TextureShader {
    private static final Random RANDOM = new Random();
    private static final float WIGGLE_FACTOR = 10.5f;
    private final float uvWiggle;

    public SunShader(BufferedImage texture) {
        super(texture);
        uvWiggle = WIGGLE_FACTOR / (float) texture.getWidth();
    }

    @Override
    public int shade(Vector3 weights, Vector3 uv) {
        float uvWiggleU = RANDOM.nextFloat() * uvWiggle;
        float uvWiggleV = RANDOM.nextFloat() * uvWiggle;
        uv = Vector3.add(new Vector3(uvWiggleU, uvWiggleV, 0.0f), uv);
        return super.shade(weights, uv);
    }
}
