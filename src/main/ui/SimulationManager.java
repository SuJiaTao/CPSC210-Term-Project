package ui;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import model.Simulation;

// Represents the current state of user-interface to managing simulations
public class SimulationManager {
    private static final int TERMINAL_WIDTH = 90;
    private static final int TERMINAL_HEIGHT = 35;

    private ConsoleOutputRedirectStream errRedirect;
    private ConsoleOutputRedirectStream outRedirect;
    private Simulation simulation;
    private TerminalScreen screen;
    private KeyStroke lastUserKey;
    private String simulationState;

    // EFFECTS: initialize simulation, init graphical/user input, redirect
    // sterr+stdout, and set simulation state to the opening screen
    public SimulationManager() throws Exception {
        simulation = new Simulation();

        errRedirect = new ConsoleOutputRedirectStream(System.err);
        System.setErr(errRedirect);
        outRedirect = new ConsoleOutputRedirectStream(System.out);
        System.setOut(outRedirect);

        DefaultTerminalFactory termFactory = new DefaultTerminalFactory();
        termFactory.setInitialTerminalSize(new TerminalSize(TERMINAL_WIDTH, TERMINAL_HEIGHT));
        screen = termFactory.createScreen();

        checkDesiredTerminalSize();
        screen.startScreen();

    }

    // EFFECTS: prints an error to sterr if failed to construct screen of desired
    // size
    private void checkDesiredTerminalSize() {
        TerminalSize termSize = screen.getTerminalSize();
        int widthActual = termSize.getColumns();
        int heightActual = termSize.getRows();
        if (heightActual != TERMINAL_HEIGHT || widthActual != TERMINAL_WIDTH) {
            String formatStr = "Failed to create terminal of desired size: (%d, %d), instead got (%d %d)";
            String errMessage = String.format(formatStr, TERMINAL_WIDTH, TERMINAL_HEIGHT, widthActual, heightActual);
            System.err.print(errMessage);
        }
    }

    // MODIFIES: this
    // EFFECTS: execute main input/rendering loop
    public void mainLoop() throws Exception {
        while (true) {
            screen.clear();
            lastUserKey = screen.pollInput();

            screen.setCursorPosition(new TerminalPosition(0, 0));
            try {
                handleSimulationState();
            } catch (Exception errMsg) {
                System.err.print(errMsg.toString());
            }

            drawErrAndOut();

            screen.refresh();
            screen.setCursorPosition(new TerminalPosition(screen.getTerminalSize().getColumns() - 1, 0));

            spinWaitMiliseconds(15);
        }
    }

    public void drawErrAndOut() {
        TextGraphics textWriter = screen.newTextGraphics();
        textWriter.setForegroundColor(TextColor.ANSI.WHITE);
        textWriter.putString(0, TERMINAL_HEIGHT - 2, "out: " + outRedirect.getStringToDisplay());
        textWriter.setForegroundColor(TextColor.ANSI.RED);
        textWriter.putString(0, TERMINAL_HEIGHT - 1, "err: " + errRedirect.getStringToDisplay());
    }

    // EFFECTS: waits for miliseconds via spin
    public void spinWaitMiliseconds(int waitMilliseconds) {
        // NOTE: hi, you are probably wondering why I'm not using Thread.sleep()
        // right now. The issue with Thread.Sleep is that internally it is generally not
        // precice enough. At least on windows, Thread.sleep() has a granularity of
        // about ~16msec which really isn't good enough for my purposes. The only way to
        // do this the way I want is to go into a spin so here it is :3
        long startTime = System.nanoTime();
        while (((System.nanoTime() - startTime) / 1000) <= waitMilliseconds) {
            // wait
        }
    }

    // MODIFIES: this
    // EFFECTS: runs the appropriate handler function based on the simulation state
    public void handleSimulationState() throws Exception {

    }

    // MODIFIES: this
    // EFFECTS: draws opening screen graphic to terminal
    public void drawOpeningScreenGraphic() {
        TextGraphics graphics = screen.newTextGraphics();
        graphics.setForegroundColor(TextColor.ANSI.WHITE);
        graphics.drawRectangle(new TerminalPosition(2, 1), new TerminalSize(50, 10), '+');
        graphics.putString(5, 3, "UI Controls:");
        graphics.putString(7, 4, "- Press Space to change selection");
        graphics.putString(7, 5, "- Press Enter to confirm selection");
        graphics.putString(7, 6, "- Press Escape to go back");
        graphics.putString(7, 7, "- Press Q to quit");
        graphics.putString(5, 8, "Press Space to continue");
    }
}
