package ui.engine;

import model.*;
import ui.SimulatorGUI;
import ui.SimulatorState;
import ui.Tickable;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.concurrent.locks.*;

public abstract class AbstractShader {
    public static final int CULL_FRAGMENT = 0x00000000;

    public abstract int shade(Vector3 weights, Vector3 uv);

    // EFFECTS: samples a buffered image, while wrapping the UVs around
    protected static int sample(BufferedImage image, Vector3 uv) {
        int sampleU = (int) (uv.getX() * (image.getWidth() - 1) + 0.5f);
        int sampleV = (int) (uv.getY() * (image.getHeight() - 1) + 0.5f);
        sampleU %= image.getWidth();
        if (sampleU < 0) {
            sampleU += image.getWidth();
        }
        sampleV %= image.getHeight();
        if (sampleV < 0) {
            sampleV += image.getHeight();
        }
        return image.getRGB(sampleU, sampleV);
    }
}
