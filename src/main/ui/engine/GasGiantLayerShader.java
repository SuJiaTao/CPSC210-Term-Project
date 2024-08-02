package ui.engine;

import model.*;
import java.util.*;
import java.awt.image.*;

public class GasGiantLayerShader extends TextureShader {
    private static final Random RANDOM = new Random();
    private static final float SKEW_VERTICAL_FACTOR = 0.15f;
    private final float uvSkew;
    private final float alpha;

    public GasGiantLayerShader(BufferedImage texture, float skewFactor, float alpha) {
        super(texture);
        this.alpha = alpha;
        uvSkew = skewFactor / (float) texture.getWidth();
    }

    @Override
    public int shade(Vector3 weights, Vector3 uvs) {
        if (RANDOM.nextFloat() > alpha) {
            return CULL_FRAGMENT;
        }
        float skew = uvSkew * RANDOM.nextFloat();
        float skewVert = SKEW_VERTICAL_FACTOR * uvSkew * RANDOM.nextFloat();
        uvs = Vector3.add(uvs, new Vector3(skew, skewVert, 0.0f));
        return super.shade(weights, uvs);
    }
}
