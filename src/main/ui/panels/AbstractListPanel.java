package ui.panels;

import java.awt.*;
import javax.swing.*;
import ui.Tickable;

// Abstract List panel which is used to view and edit elements in a list
public abstract class AbstractListPanel<T> extends JPanel implements Tickable {
    public static final double SPLIT_WEIGHT = 0.9;

    private InternalListModel listModel;
    protected JList<T> swingList;
    protected JScrollPane listScroller;
    protected JPanel editorPanel;

    // This is simply Swing awfulness. I never intended to write such voodo for this
    // course, but Java has forced my hand. Thanks Swing. To decipher this
    // awfulness, please refer to JList and AbstractListModel documentation.
    private class InternalListModel extends AbstractListModel<T> implements Tickable {
        private java.util.List<T> targetListData;

        // EFFECTS: sets the internal target list to point at listData
        public InternalListModel(java.util.List<T> listData) {
            targetListData = listData;
        }

        public java.util.List<T> getListData() {
            return targetListData;
        }

        // EFFECTS: returns internal list size
        @Override
        public int getSize() {
            return targetListData.size();
        }

        // EFFECTS: fetches item from internal list
        @Override
        public T getElementAt(int index) {
            return targetListData.get(index);
        }

        // EFFECTS: forces an update to the list
        @Override
        public void tick() {
            fireContentsChanged(this, 0, targetListData.size() - 1);
        }
    }

    // EFFECTS: initializes list to be empty and listScroller to contain list, calls
    // on user defined initialization of editorpanel, and then packs the components
    public AbstractListPanel(java.util.List<T> listData) {
        setLayout(new BorderLayout());

        listModel = new InternalListModel(listData);
        swingList = new JList<>(listModel);
        listScroller = new JScrollPane(swingList);
        editorPanel = initEditorPanel();

        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listScroller, editorPanel);
        splitter.setResizeWeight(SPLIT_WEIGHT);
        splitter.setEnabled(false);

        add(splitter);
    }

    public JList<T> getSwingList() {
        return swingList;
    }

    public JPanel getEditorPanel() {
        return editorPanel;
    }

    public java.util.List<T> getListData() {
        return listModel.getListData();
    }

    // EFFECTS: expected that the user defines a means to initialize the editor
    // panel in this method, and returns it
    protected abstract JPanel initEditorPanel();

    // MODIFIES: this
    // EFFECTS: updates the current object
    @Override
    public void tick() {
        listModel.tick();
    }
}
