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

import model.*;
import model.exceptions.PlanetDoesntExistException;

public class SimulationGraphics {
    public static final int TERMINAL_WIDTH = 100;
    public static final int TERMINAL_HEIGHT = 35;

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

    private int plntListViewOffset;
    private int colListViewOffset;

    public SimulationGraphics(SimulationManager manager) throws IOException {
        this.manager = manager;

        DefaultTerminalFactory termFactory = new DefaultTerminalFactory();
        termFactory.setInitialTerminalSize(new TerminalSize(TERMINAL_WIDTH, TERMINAL_HEIGHT));
        screen = termFactory.createScreen();

        checkIfObtainedDesiredTerminalSize();
        tryAndSetupWindowFrame();

        int viewportSize = Math.max(VIEWPORT_PIX_WIDTH, VIEWPORT_PIX_HEIGHT);
        viewport = new ViewportEngine(viewportSize, manager);

        plntListViewOffset = 0;
        colListViewOffset = 0;
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

    public TerminalScreen getScreen() {
        return screen;
    }

    // MODIFIES: this
    // EFFECTS: clears the terminal and hides the cursor
    public void clear() {
        screen.setCursorPosition(null);
        TextGraphics gfx = screen.newTextGraphics();
        gfx.fillRectangle(new TerminalPosition(0, 0), screen.getTerminalSize(), ' ');
    }

    // MODIFIES: this
    // EFFECTS: refreshes the terminal window
    public void refresh() {
        try {
            screen.refresh();
        } catch (IOException except) {
            // DO NOTHING, IT'S OK!
        }
    }

    // MODIFIES: this
    // EFFECTS: draws title screen text
    public void drawTitleScreen() {
        TextGraphics gfx = screen.newTextGraphics();
        TerminalPosition borderTopLeft = new TerminalPosition(TITLE_SCREEN_LEFT, TITLE_SCREEN_TOP);
        TerminalSize borderSize = new TerminalSize(TITLE_SCREEN_WIDTH, TITLE_SCREEN_HEIGHT);
        gfx.drawRectangle(borderTopLeft, borderSize, '+');
        drawTitleScreenCenteredText(gfx, "N-BODY SIMULATOR", 0);
        drawTitleScreenCenteredText(gfx, "Bailey JT Brown 2024", 1);
        drawTitleScreenleftText(gfx, " Controls:", 3);
        drawTitleScreenleftText(gfx, "  - Left/Right to cycle between Planet/Collision list view", 4);
        drawTitleScreenleftText(gfx, "  - Plus/Minus to add/remove planets", 5);
        drawTitleScreenleftText(gfx, "  - Up/Down to cycle between elements", 6);
        drawTitleScreenleftText(gfx, "  - Enter to select item", 7);
        drawTitleScreenleftText(gfx, "  - Escape to unselect item", 8);
        drawTitleScreenleftText(gfx, "  - Space to pause/unpause simulation", 9);
        drawTitleScreenleftText(gfx, "  - Q or close window to quit", 10);
        drawTitleScreenCenteredText(gfx, " Press Space to continue", TITLE_SCREEN_HEIGHT - 3);
    }

    // MODIFIES: this
    // EFFECTS: draws centered text within the titlescreen box
    private void drawTitleScreenCenteredText(TextGraphics gfx, String text, int line) {
        int posX = TITLE_SCREEN_CENTER_X - (text.length() / 2);
        int posY = TITLE_SCREEN_TOP + line + 1;
        gfx.putString(posX, posY, text);
    }

    // MODIFIES: this
    // EFFECTS: draws left-aligned text within the titlescreen box
    private void drawTitleScreenleftText(TextGraphics gfx, String text, int line) {
        int posX = TITLE_SCREEN_LEFT + 1;
        int posY = TITLE_SCREEN_TOP + line + 1;
        gfx.putString(posX, posY, text);
    }

    // REQUIRES: editorViewListSelection must be a valid value
    // MODIFIES: this
    // EFFECTS: draws the appropriate visuals to the left-side editor
    public void drawEditorView() {
        switch (manager.getSelectedEditorView()) {
            case SimulationManager.EDITOR_OPTION_PLANETS:
                drawPlanetListEditor();
                break;
            case SimulationManager.EDITOR_OPTION_COLLISIONS:
                drawCollisionListEditor();
                break;
            default:
                break;
        }
    }

    // MODIFIES: this
    // EFFECTS: draws right-side 3D viewport
    public void drawSimulationViewPort() {
        TextGraphics gfx = screen.newTextGraphics();
        drawViewportFrame(gfx);
        drawViewportView(gfx);
    }

    // MODIFIES: this
    // EFFECTS: draw the viewport buffers to the terminal
    private void drawViewportView(TextGraphics gfx) {
        // TODO: this is a hackjob that will be fixed once I can use a proper GUI
        // library
        viewport.update();
        int vpOffsetX = (viewport.getSize() - VIEWPORT_PIX_WIDTH) / 2;
        int vpOffsetY = (viewport.getSize() - VIEWPORT_PIX_HEIGHT) / 2;
        int anchorLeft = VIEWPORT_LEFT + vpOffsetX;
        int anchorTop = VIEWPORT_TOP - vpOffsetY;
        for (int x = 0; x < viewport.getSize(); x++) {
            for (int y = 0; y < viewport.getSize(); y++) {
                Planet selectedPlanet = manager.getSelectedPlanet();
                if (viewport.getPlanetMaskValue(x, y) == selectedPlanet && selectedPlanet != null) {
                    setTextGraphicsToHoverMode(gfx);
                    if (manager.isEditingSelectedPlanet()) {
                        setTextGraphicsToSelectMode(gfx);
                    }
                } else {
                    setTextGraphicsToViewMode(gfx);
                }
                char fbChar = (char) viewport.getFrameBufferValue(x, y);
                gfx.setCharacter(anchorLeft + (x * 2) + 0, anchorTop + y, fbChar);
                gfx.setCharacter(anchorLeft + (x * 2) + 1, anchorTop + y, fbChar);
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: draws the viewport frame
    private void drawViewportFrame(TextGraphics gfx) {
        setTextGraphicsToViewMode(gfx);

        // DRAW VIEWPORT SURROUNDING BOX
        gfx.drawLine(VP_FRAME_LEFT, VP_FRAME_TOP, VP_FRAME_RIGHT, VP_FRAME_TOP, 'X');
        gfx.drawLine(VP_FRAME_LEFT, VP_FRAME_BOT, VP_FRAME_RIGHT, VP_FRAME_BOT, 'X');
        gfx.drawLine(VP_FRAME_LEFT, VP_FRAME_TOP, VP_FRAME_LEFT, VP_FRAME_BOT, 'X');
        gfx.drawLine(VP_FRAME_RIGHT, VP_FRAME_TOP, VP_FRAME_RIGHT, VP_FRAME_BOT, 'X');

        gfx.drawLine(VP_FRAME_LEFT, VP_FRAME_TOP + 2, VP_FRAME_RIGHT, VP_FRAME_TOP + 2, 'X');
        String viewportTitle = "Simulation ";
        if (manager.isSimulationRunning()) {
            viewportTitle += "Running";
        } else {
            viewportTitle += "Stopped";
        }
        viewportTitle += String.format(" | Time Elapsed: %.2fs", manager.getSimulation().getTimeElapsed());
        gfx.putString(VP_FRAME_LEFT + 2, VP_FRAME_TOP + 1, viewportTitle);
    }

    // MODIFIES: this
    // EFFECTS: draws the collision list editor
    public void drawCollisionListEditor() {
        TextGraphics gfx = screen.newTextGraphics();
        setTextGraphicsToViewMode(gfx);

        drawEditorFrame(gfx);
        drawCollisionList(gfx);
    }

    // MODIFIES: this
    // EFFECTS: draw the list of collisions
    private void drawCollisionList(TextGraphics gfx) {
        gfx.putString(new TerminalPosition(EDITOR_LEFT + 2, EDITOR_TOP + 1), "COLLISION LIST");
        gfx.drawLine(EDITOR_LEFT, EDITOR_TOP + 2, EDITOR_RIGHT, EDITOR_TOP + 2, '+');

        List<Collision> colList = manager.getSimulation().getCollisions();
        if (colList.size() == 0) {
            gfx.putString(EDITOR_LEFT + 1, EDITOR_TOP + 3, " No collisions yet!");
            return;
        }

        drawCollisionListEntries(gfx, colList);
        drawCollisionInfo(gfx);
    }

    // REQUIRES: selectedCollision to exist in colList
    // MODIFIES: this
    // EFFECTS: draw the entries of collisions
    private void drawCollisionListEntries(TextGraphics gfx, List<Collision> colList) {
        Collision selectedCollision = manager.getSelectedCollision();

        colListViewOffset = Math.max(colListViewOffset,
                colList.indexOf(selectedCollision) - EDITORLIST_ENTIRES + 1);
        colListViewOffset = Math.min(colListViewOffset, colList.indexOf(selectedCollision));

        for (int i = 0; i < EDITORLIST_ENTIRES; i++) {
            int indexActual = colListViewOffset + i;
            if (indexActual >= colList.size()) {
                break;
            }

            if (indexActual == colList.indexOf(selectedCollision)) {
                setTextGraphicsToHoverMode(gfx);
            } else {
                setTextGraphicsToViewMode(gfx);
            }

            Collision col = colList.get(indexActual);
            List<Planet> involved = col.getPlanetsInvolved();
            String colName = "";
            colName += involved.get(0).getName().charAt(0);
            colName += "/";
            colName += involved.get(1).getName().charAt(0);
            colName += String.format("-%.3f", col.getCollisionTime());
            String entryString = (1 + indexActual) + ". Collision " + colName;
            gfx.putString(EDITOR_LEFT + 1, EDITOR_TOP + 3 + i, entryString);
        }
    }

    // REQUIRES: selectedCollision must be in collision list and NOT null
    // MODIFIES: this
    // EFFECTS: draws collision info viewer
    private void drawCollisionInfo(TextGraphics gfx) {
        setTextGraphicsToViewMode(gfx);
        Collision selectedCollision = manager.getSelectedCollision();

        // title and border
        gfx.drawLine(EDITOR_LEFT, PLANETINFO_TOP, EDITOR_RIGHT, PLANETINFO_TOP, '+');

        if (selectedCollision == null) {
            gfx.putString(EDITOR_LEFT + 2, PLANETINFO_TOP + 1, "No collision selected");
            return;
        }
        gfx.putString(EDITOR_LEFT + 2, PLANETINFO_TOP + 1, "COLLISION INFO: ");
        gfx.putString(EDITOR_LEFT + 3, PLANETINFO_TOP + 2, "Planets Involved:");
        String involvedString = "";
        involvedString += selectedCollision.getPlanetsInvolved().get(0).getName();
        involvedString += ", ";
        involvedString += selectedCollision.getPlanetsInvolved().get(1).getName();
        gfx.putString(EDITOR_LEFT + 5, PLANETINFO_TOP + 3, involvedString);
        String timeOccouredString = String.format("Time Occoured: %.3fs", selectedCollision.getCollisionTime());
        gfx.putString(EDITOR_LEFT + 3, PLANETINFO_TOP + 4, timeOccouredString);
    }

    // MODIFIES: this
    // EFFECTS: draws left-side planet editor
    public void drawPlanetListEditor() {
        TextGraphics gfx = screen.newTextGraphics();
        setTextGraphicsToViewMode(gfx);

        drawEditorFrame(gfx);

        drawPlanetList(gfx);
        drawPlanetInfo(gfx);
    }

    // MODIFIES: this
    // EFFECTS: draw the left-side editor frame
    private void drawEditorFrame(TextGraphics gfx) {
        gfx.drawLine(EDITOR_LEFT, EDITOR_TOP, EDITOR_RIGHT, EDITOR_TOP, '+'); // TOP
        gfx.drawLine(EDITOR_LEFT, EDITOR_TOP, EDITOR_LEFT, EDITOR_BOT, '+'); // LEFT
        gfx.drawLine(EDITOR_RIGHT, EDITOR_TOP, EDITOR_RIGHT, EDITOR_BOT, '+'); // RIGHT
        gfx.drawLine(EDITOR_LEFT, EDITOR_BOT, EDITOR_RIGHT, EDITOR_BOT, '+'); // BOTTOM
    }

    // MODIFIES: this
    // EFFECTS: draws planet editor planet list
    private void drawPlanetList(TextGraphics gfx) {
        // title and border
        gfx.putString(new TerminalPosition(EDITOR_LEFT + 2, EDITOR_TOP + 1), "PLANET LIST");
        gfx.drawLine(EDITOR_LEFT, EDITOR_TOP + 2, EDITOR_RIGHT, EDITOR_TOP + 2, '+');

        List<Planet> planetList = manager.getSimulation().getPlanets();
        if (planetList.size() == 0) {
            gfx.putString(EDITOR_LEFT + 1, EDITOR_TOP + 3, " Press '+' to add a planet");
            return;
        }

        drawPlanetListEntries(gfx, planetList);
    }

    // MODIFIES: this
    // EFFECTS: draws entries of the planet list
    private void drawPlanetListEntries(TextGraphics gfx, List<Planet> planetList) {
        Planet selectedPlanet = manager.getSelectedPlanet();

        plntListViewOffset = Math.max(plntListViewOffset,
                planetList.indexOf(selectedPlanet) - EDITORLIST_ENTIRES + 1);
        plntListViewOffset = Math.min(plntListViewOffset, planetList.indexOf(selectedPlanet));

        for (int i = 0; i < EDITORLIST_ENTIRES; i++) {
            int indexActual = plntListViewOffset + i;
            if (indexActual >= planetList.size()) {
                break;
            }
            if (indexActual == planetList.indexOf(selectedPlanet)) {
                setTextGraphicsToHoverMode(gfx);
                if (manager.isEditingSelectedPlanet()) {
                    setTextGraphicsToSelectMode(gfx);
                }
            } else {
                setTextGraphicsToViewMode(gfx);
            }
            String entryString = "" + (indexActual + 1) + ". " + planetList.get(indexActual).getName();
            gfx.putString(EDITOR_LEFT + 1, EDITOR_TOP + 3 + i, entryString);
        }
    }

    // MODIFIES: this
    // EFFECTS: draws planet info viewer
    private void drawPlanetInfo(TextGraphics gfx) {
        setTextGraphicsToViewMode(gfx);
        Planet selectedPlanet = manager.getSelectedPlanet();

        // title and border
        gfx.drawLine(EDITOR_LEFT, PLANETINFO_TOP, EDITOR_RIGHT, PLANETINFO_TOP, '+');

        if (selectedPlanet == null) {
            gfx.putString(EDITOR_LEFT + 2, PLANETINFO_TOP + 1, "No planet selected");
            return;
        }

        String actionPrefix = "";
        if (manager.isEditingSelectedPlanet()) {
            actionPrefix = "EDIT";
        } else {
            actionPrefix = "VIEW";
        }
        gfx.putString(EDITOR_LEFT + 2, PLANETINFO_TOP + 1, actionPrefix + " PLANET ");

        drawPlanetProperties(gfx);
        drawPropertyEditingInputBox(gfx);
    }

    // MODIFIES: this
    // EFFECTS: draws GUI for value editing input handler
    private void drawPropertyEditingInputBox(TextGraphics gfx) {
        if (!manager.isEditingSelectedProperty()) {
            return;
        }
        setTextGraphicsToViewMode(gfx);
        gfx.drawLine(EDITOR_LEFT, EDITOR_BOT - 3, EDITOR_RIGHT, EDITOR_BOT - 3, '+');
        gfx.putString(EDITOR_LEFT + 1, EDITOR_BOT - 2, "Input Value:");
        setTextGraphicsToSelectMode(gfx);
        gfx.putString(EDITOR_LEFT + 2, EDITOR_BOT - 1, manager.getUserInputString());
    }

    // MODIFIES: this
    // EFFECTS: draws planet property editor/viewer
    private void drawPlanetProperties(TextGraphics gfx) {
        setTextGraphicsToViewMode(gfx);

        Planet selectedPlanet = manager.getSelectedPlanet();

        int yOffset = 0;
        for (String property : SimulationManager.PROP_OPTIONS) {
            String propertyString = "";
            switch (property) {
                case SimulationManager.PROP_OPTION_NAME:
                    propertyString += "Name: " + selectedPlanet.getName();
                    break;

                case SimulationManager.PROP_OPTION_POS:
                    propertyString += "Pos: " + selectedPlanet.getPosition().toString();
                    break;

                case SimulationManager.PROP_OPTION_VEL:
                    propertyString += "Vel: " + selectedPlanet.getVelocity().toString();
                    break;

                case SimulationManager.PROP_OPTION_RAD:
                    propertyString += "Radius: " + String.format("%.2f", selectedPlanet.getRadius());
                    break;

                default:
                    assert false; // THIS SHOULD NEVER HAPPEN
                    break;
            }

            if (manager.isEditingSelectedPlanet() && manager.getSelectedProperty().equals(property)) {
                setTextGraphicsToHoverMode(gfx);
                if (manager.isEditingSelectedProperty()) {
                    setTextGraphicsToSelectMode(gfx);
                }
            } else {
                setTextGraphicsToViewMode(gfx);
            }
            gfx.putString(EDITOR_LEFT + 3, PLANETINFO_TOP + 2 + yOffset, propertyString);
            yOffset++;
        }
    }

    // EFFECTS: sets Textgraphics to "hover" appearance
    private void setTextGraphicsToHoverMode(TextGraphics gfx) {
        gfx.setBackgroundColor(TextColor.ANSI.WHITE);
        gfx.setForegroundColor(TextColor.ANSI.BLACK);
    }

    // EFFECTS: sets Textgraphics to "selection" appearance
    private void setTextGraphicsToSelectMode(TextGraphics gfx) {
        gfx.setBackgroundColor(TextColor.ANSI.BLUE);
        gfx.setForegroundColor(TextColor.ANSI.WHITE);
    }

    // EFFECTS: sets Textgraphics to "view" appearance
    private void setTextGraphicsToViewMode(TextGraphics gfx) {
        gfx.setBackgroundColor(TextColor.ANSI.BLACK);
        gfx.setForegroundColor(TextColor.ANSI.WHITE);
    }

    // EFFECTS: draws stdout and stederr to the bottom of the screen
    public void drawErrAndMessageText(ConsoleOutputRedirectStream out, ConsoleOutputRedirectStream err) {
        TextGraphics textWriter = screen.newTextGraphics();
        textWriter.setBackgroundColor(TextColor.ANSI.BLACK);

        textWriter.setForegroundColor(TextColor.ANSI.WHITE);
        textWriter.putString(0, TERMINAL_HEIGHT - 2, "LastOut: " + out.getStringToDisplay());
        textWriter.setForegroundColor(TextColor.ANSI.RED);
        textWriter.putString(0, TERMINAL_HEIGHT - 1, "LastErr: " + err.getStringToDisplay());
    }
}
