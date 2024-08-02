package ui.engine;

import model.*;
import java.util.*;
import java.awt.image.*;

public class TextureShader extends AbstractShader {

    private static final Random RANDOM = new Random();
    private BufferedImage texture;

    public TextureShader(BufferedImage texture) {
        this.texture = texture;
    }

    @Override
    public int shade(Vector3 weights, Vector3 uv) {
        int texSample = sample(texture, uv);
        return texSample;
    }
}
