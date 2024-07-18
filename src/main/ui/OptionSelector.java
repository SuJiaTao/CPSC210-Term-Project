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

public class OptionSelector<T> {
    private List<T> optionList;
    private T selectedObject;
    private KeyStroke selectFwd;
    private KeyStroke selectBack;

    // EFFECTS: creates an option selector with the relevant parameters
    public OptionSelector(List<T> optionList, KeyStroke selectFwd, KeyStroke selectBck) {
        this.optionList = optionList;
        this.selectFwd = selectFwd;
        this.selectBack = selectBck;
        this.selectedObject = null;
        ensureSelectedObjectIsReasonable();
    }

    public T getSelectedObject() {
        return selectedObject;
    }

    // EFFECTS: returns whether getSelectedObject is null
    public boolean noSelectedObject() {
        return (getSelectedObject() == null);
    }

    // EFFECTS: ensures that selectedObject is a valid value and updates the current
    // selected object based on the latest input
    public void update(KeyStroke lastKeyStroke) {
        ensureSelectedObjectIsReasonable();
        updateSelectedObject(lastKeyStroke);
    }

    // EFFECTS: ensure that selectedObject is within the optionList, if not, the
    // first element in the list. if the list is empty, then null
    private void ensureSelectedObjectIsReasonable() {
        if (optionList.contains(selectedObject)) {
            return;
        }
        if (optionList.size() > 0) {
            selectedObject = optionList.get(0);
            return;
        }
        selectedObject = null;
    }

    // EFFECTS: cycles selectedObject based on the last keystroke
    public void updateSelectedObject(KeyStroke lastKeyStroke) {
        if (lastKeyStroke == null || selectedObject == null) {
            return;
        }
        int objIndex = optionList.indexOf(selectedObject);
        assert (objIndex != -1); // this should NEVER happen

        if (lastKeyStroke.equals(selectFwd)) {
            objIndex--;
        }
        if (lastKeyStroke.equals(selectBack)) {
            objIndex++;
        }

        objIndex %= optionList.size();
        if (objIndex < 0) {
            objIndex += optionList.size();
        }

        selectedObject = optionList.get(objIndex);
    }
}
