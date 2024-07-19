package ui;

import java.util.*;
import com.googlecode.lanterna.input.*;
import ui.exceptions.OptionAlreadyExistsException;
import ui.exceptions.OptionDoesntExistException;

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

    public List<T> getOptions() {
        return optionList;
    }

    public T getSelectedObject() {
        ensureSelectedObjectIsReasonable();
        return selectedObject;
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
        if (optionList.size() > 0) {
            selectedObject = optionList.get(0);
            return;
        }
        selectedObject = null;
    }

    // EFFECTS: cycles selectedObject based on the last keystroke
    private void updateSelectedObject(KeyStroke lastKeyStroke) {
        if (lastKeyStroke == null || selectedObject == null) {
            return;
        }
        int objIndex = optionList.indexOf(selectedObject);
        assert (objIndex != -1); // this should NEVER happen

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
    }

    // EFFECTS: add the given object to the list and updates selection accordingly
    public void addOptionToSelection(T object, boolean selectAfter) {
        ensureSelectedObjectIsReasonable();

        if (optionList.contains(object)) {
            throw new OptionAlreadyExistsException();
        }

        optionList.add(object);
        if (selectAfter) {
            selectedObject = object;
        }
    }

    // EFFECTS: removes the given object from the list and updates the selection
    // accordingly
    public void removeOptionFromSelection(T object) {
        ensureSelectedObjectIsReasonable();

        if (!optionList.contains(object)) {
            throw new OptionDoesntExistException();
        }

        int oldSelectedIndex = optionList.indexOf(object);
        optionList.remove(oldSelectedIndex);

        if (optionList.size() == 0) {
            selectedObject = null;
            return;
        }

        if (object == selectedObject) {
            int newIndex = Math.min(oldSelectedIndex, optionList.size() - 1);
            selectedObject = optionList.get(newIndex);
        }
    }
}
