package ui.panels;

import java.util.*;
import java.io.File;
import java.util.List;
import persistence.SimulationReadWriter;

// Contains all the UI elements to represent the current list of saved simulations
public class SavedListPanel extends AbstractListPanel<String> {
    private SavedEditorPanel savedEditorPanel;

    // EFFECTS: initializes itself to record the list of contents in a new list of
    // empty strings
    public SavedListPanel() {
        super(new ArrayList<String>());
    }

    // MODIFIES: this
    // EFFECTS: instantiates a savedEditorPanel and returns it
    @Override
    public SavedEditorPanel initEditorPanel() {
        savedEditorPanel = new SavedEditorPanel(this);
        return savedEditorPanel;
    }

    // MODIFIES: this, super
    // EFFECTS: updates strings of save filenames
    @Override
    public void tick() {
        super.tick();

        // NOTE:
        // this is a direct rip from ui.legacy.SimulationManager, with some mild
        // modifications
        File saveDir = new File(SimulationReadWriter.SAVE_PATH);
        File[] subFiles = saveDir.listFiles();

        // NOTE:
        // this is a simply horrible way of doing this but oh well
        List<String> newFileNames = new ArrayList<String>(subFiles.length);
        for (File subFile : subFiles) {
            if (subFile.isDirectory() || !subFile.getName().endsWith(SimulationReadWriter.FILE_SUFFIX)) {
                continue;
            }

            String subFileName = subFile.getName();
            subFileName = subFileName.substring(0, subFileName.lastIndexOf("."));
            newFileNames.add(subFileName);
        }

        List<String> savedSimOptions = super.getListData();

        // remove everything thats gone
        // NOTE: this must be done in two passes as you cant remove element while
        // iterating over the colection
        List<String> toRemove = new ArrayList<String>(savedSimOptions.size());
        for (String oldFileName : savedSimOptions) {
            if (!newFileNames.contains(oldFileName)) {
                toRemove.add(oldFileName);
            }
        }
        for (String toRemoveName : toRemove) {
            savedSimOptions.remove(toRemoveName);
        }

        // add everything not already there
        for (String newFileName : newFileNames) {
            if (!savedSimOptions.contains(newFileName)) {
                savedSimOptions.add(newFileName);
            }
        }

        savedEditorPanel.tick();
    }
}
