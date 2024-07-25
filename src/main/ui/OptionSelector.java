package ui;

import java.util.*;
import com.googlecode.lanterna.input.*;
import ui.exceptions.OptionAlreadyExistsException;
import ui.exceptions.OptionDoesntExistException;

public class OptionSelector<T> {
    private List<T> optionList;
    private int selectedIndex;
    private T selectedObject;
    private KeyStroke selectFwd;
    private KeyStroke selectBack;

    // EFFECTS: creates an option selector with the relevant parameters
    public OptionSelector(List<T> optionList, KeyStroke selectFwd, KeyStroke selectBck) {
        this.optionList = optionList;
        this.selectFwd = selectFwd;
        this.selectBack = selectBck;
        this.selectedIndex = -1;
        this.selectedObject = null;
        ensureSelectedObjectIsReasonable();
    }

    public List<T> getOptions() {
        return optionList;
    }

    public T getSelectedObject() {
        ensureSelectedObjectIsReasonable();
        return selectedObject;
    }

    public void setSelectedObject(T object) {
        if (!optionList.contains(object)) {
            throw new OptionDoesntExistException();
        }
        selectedIndex = optionList.indexOf(object);
        selectedObject = object;
    }

    // EFFECTS: returns whether getSelectedObject is null
    public boolean noSelectedObject() {
        return (getSelectedObject() == null);
    }

    // EFFECTS: ensures that selectedObject is a valid value and updates the current
    // selected object based on the latest input
    public void cycleObjectSelection(KeyStroke lastKeyStroke) {
        ensureSelectedObjectIsReasonable();
        updateSelectedObject(lastKeyStroke);
    }

    // EFFECTS: ensure that selectedObject is within the optionList, if not, the
    // first element in the list. if the list is empty, then null
    private void ensureSelectedObjectIsReasonable() {
        if (optionList.contains(selectedObject)) {
            return;
        }
        if (optionList.isEmpty()) {
            selectedObject = null;
            selectedIndex = -1;
            return;
        }

        selectedIndex = Math.max(0, Math.min(selectedIndex, optionList.size() - 1));
        selectedObject = optionList.get(selectedIndex);
    }

    // EFFECTS: cycles selectedObject based on the last keystroke
    private void updateSelectedObject(KeyStroke lastKeyStroke) {
        if (lastKeyStroke == null || selectedObject == null) {
            return;
        }
        int objIndex = optionList.indexOf(selectedObject);
        if (objIndex == -1) {
            throw new IllegalStateException(); // this should NEVER happen
        }

        if (lastKeyStroke.equals(selectFwd)) {
            objIndex++;
        }
        if (lastKeyStroke.equals(selectBack)) {
            objIndex--;
        }

        objIndex %= optionList.size();
        if (objIndex < 0) {
            objIndex += optionList.size();
        }

        selectedObject = optionList.get(objIndex);
        selectedIndex = objIndex;
    }
}
