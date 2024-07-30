package ui.engine;

import model.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.ImageIO;

public class TextureShader extends AbstractShader {
    private static final String IMAGE_PATH = "./data/image/";
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

    public static BufferedImage loadImage(String imgName) {
        try {
            return ImageIO.read(new File(IMAGE_PATH + imgName));
        } catch (IOException err) {
            throw new IllegalStateException(); // shouldnt happen
        }
    }
}
