package ui.engine;

import model.*;
import java.util.*;
import java.awt.image.*;

public class TextureShader extends AbstractShader {

    private static final Random RANDOM = new Random();
    private BufferedImage texture;
    private float alpha;

    public TextureShader(BufferedImage texture, float alpha) {
        this.texture = texture;
        this.alpha = alpha;
    }

    @Override
    public int shade(Vector3 weights, Vector3 uv) {
        int texSample = sample(texture, uv);
        if (RANDOM.nextFloat() > alpha) {
            return CULL_FRAGMENT;
        }
        return texSample;
    }
}
