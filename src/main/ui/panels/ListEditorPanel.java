package ui.panels;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

// Abstract List panel which is used to view and edit elements in a list
public abstract class ListEditorPanel<T> extends JPanel {
    public static final double VERTICAL_SPLIT_FACTOR = 0.7;
    protected java.util.List<T> objList;
    private JList<String> list;
    private JScrollPane listScroller;
    private JComponent editorPanel;

    // EFFECTS: initializes list to be empty and listScroller to contain list, calls
    // on user defined initialization of editorpanel, and then packs the components
    public ListEditorPanel(java.util.List<T> listData) {
        // setLayout(new BorderLayout());

        this.objList = listData;
        list = new JList<>();
        listScroller = new JScrollPane(list);
        editorPanel = initEditorPanel();

        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listScroller, editorPanel);
        splitter.setDividerLocation(VERTICAL_SPLIT_FACTOR);
        splitter.setEnabled(false);

        add(splitter);

        updateListData();
    }

    protected JList<String> getList() {
        return list;
    }

    protected JComponent getEditorPanel() {
        return editorPanel;
    }

    // EFFECTS: expected that the user defines a means to initialize the editor
    // panel in this method, and returns it
    protected abstract JComponent initEditorPanel();

    // EFFECTS: expected that the user defines a means to conver their objectList
    // into an array of string
    protected abstract String[] convertListToStrings();

    // EFFECTS: updates the current JList list data
    public void updateListData() {
        list.setListData(convertListToStrings());
    }
}