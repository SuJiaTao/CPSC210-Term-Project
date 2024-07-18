package ui;

import java.io.IOException;
import java.util.*;
import javax.swing.JFrame;
import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.*;
import com.googlecode.lanterna.input.*;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import exceptions.PlanetDoesntExistException;
import model.*;

public class SimulationGraphics {
    public static final int TERMINAL_WIDTH = 100;
    public static final int TERMINAL_HEIGHT = 35;
    public static final int REFRESH_DELAY_MSEC = 10;

    public static final int TITLE_SCREEN_CENTER_X = TERMINAL_WIDTH / 2;
    public static final int TITLE_SCREEN_CENTER_Y = TERMINAL_HEIGHT / 2;
    public static final int TITLE_SCREEN_WIDTH = 70;
    public static final int TITLE_SCREEN_HEIGHT = 16;
    public static final int TITLE_SCREEN_LEFT = TITLE_SCREEN_CENTER_X - (TITLE_SCREEN_WIDTH / 2);
    public static final int TITLE_SCREEN_TOP = TITLE_SCREEN_CENTER_Y - (TITLE_SCREEN_HEIGHT / 2);

    public static final int EDITOR_TOP = 0;
    public static final int EDITOR_BOT = 32;
    public static final int EDITOR_LEFT = 0;
    public static final int EDITOR_RIGHT = 40;
    public static final int EDITORLIST_ENTIRES = 20;
    public static final int PLANETINFO_TOP = EDITOR_TOP + EDITORLIST_ENTIRES + 3;

    public static final int VP_FRAME_TOP = 0;
    public static final int VP_FRAME_BOT = 32;
    public static final int VP_FRAME_LEFT = EDITOR_RIGHT + 1;
    public static final int VP_FRAME_RIGHT = TERMINAL_WIDTH - 1;
    public static final int VIEWPORT_TOP = VP_FRAME_TOP + 3;
    public static final int VIEWPORT_BOT = VP_FRAME_BOT;
    public static final int VIEWPORT_LEFT = VP_FRAME_LEFT + 1;
    public static final int VIEWPORT_RIGHT = VP_FRAME_RIGHT;
    public static final int VIEWPORT_PIX_WIDTH = (VIEWPORT_RIGHT - VIEWPORT_LEFT) / 2;
    public static final int VIEWPORT_PIX_HEIGHT = VIEWPORT_BOT - VIEWPORT_TOP;

    private SimulationManager manager;
    private TerminalScreen screen;
    private ViewportEngine viewport;

    public SimulationGraphics(SimulationManager manager) throws IOException {
        this.manager = manager;

        DefaultTerminalFactory termFactory = new DefaultTerminalFactory();
        termFactory.setInitialTerminalSize(new TerminalSize(TERMINAL_WIDTH, TERMINAL_HEIGHT));
        screen = termFactory.createScreen();

        checkIfObtainedDesiredTerminalSize();
        tryAndSetupWindowFrame();

        screen.startScreen();
    }

    // EFFECTS: prints an error to stderr if failed to construct screen of desired
    // size
    private void checkIfObtainedDesiredTerminalSize() {
        TerminalSize termSize = screen.getTerminalSize();
        int widthActual = termSize.getColumns();
        int heightActual = termSize.getRows();

        if (heightActual != TERMINAL_HEIGHT || widthActual != TERMINAL_WIDTH) {
            String formatStr = "Failed to create terminal of desired size: (%d, %d), instead got (%d %d)";
            String errMessage = String.format(formatStr, TERMINAL_WIDTH, TERMINAL_HEIGHT, widthActual, heightActual);
            System.err.print(errMessage);
        }
    }

    // EFFECTS: tries to set some additional properties of the current screen and
    // prints an error of unable to
    private void tryAndSetupWindowFrame() {
        try {
            SwingTerminalFrame swingFrame = (SwingTerminalFrame) screen.getTerminal();
            swingFrame.setResizable(false);
            swingFrame.setTitle("N-Body Simulator");
            swingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } catch (Exception excep) {
            System.err.print("Failed to setup window. Error: " + excep.toString());
        }
    }
}
