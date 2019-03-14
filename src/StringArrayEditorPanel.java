package harmonia;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class StringArrayEditorPanel extends JPanel implements ActionListener, MouseListener {

    public static final String ACTION_UPDATE = "string-array-updated";

    private ActionListener callback;
    private String caption;
    private String tag;
    private String[] array;
    private int rows;

    private JList arrayList;
    private JButton addButton;
    private JButton editButton;
    private JButton removeButton;

    private boolean sorted;
    private boolean confirmRemove;

    public StringArrayEditorPanel(ActionListener callback, String caption, 
            String tag, String[] array) {
        this(callback, caption, tag, array, 4, false, false);
    }

    public StringArrayEditorPanel(ActionListener callback, String caption, 
            String tag, String[] array, int rows) {
        this(callback, caption, tag, array, rows, false, false);
    }

    public StringArrayEditorPanel(ActionListener callback, String caption, 
            String tag, String[] array, int rows,
            boolean sorted, boolean confirmRemove) {
        this.callback = callback;
        this.caption = caption;
        this.tag = tag;
        this.array = array;
        this.rows = rows;
        this.sorted = sorted;
        this.confirmRemove = confirmRemove;

        buildGUI();
    }

    public void buildGUI() {

        arrayList = new JList();
        arrayList.setVisibleRowCount(rows);
        arrayList.addMouseListener(this);
        refreshList();
        JScrollPane scrollPane = new JScrollPane(arrayList);
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());
        listPanel.add(scrollPane, BorderLayout.CENTER);
        listPanel.setBorder(BorderFactory.createEmptyBorder(0,8,8,0));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
        addButton = new JButton("Add");
        addButton.addActionListener(this);
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(addButton);
        editButton = new JButton("Edit");
        editButton.addActionListener(this);
        editButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        //buttonPanel.add(editButton);
        removeButton = new JButton("Remove");
        removeButton.addActionListener(this);
        removeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(removeButton);
        buttonPanel.add(Box.createVerticalGlue());

        setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(4,8,4,8),
                    BorderFactory.createTitledBorder(" " + caption + " ")));
        setLayout(new BorderLayout());
        add(listPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.EAST);
    }

    public void refreshList() {
        DefaultListModel model = new DefaultListModel();
        int i;
        for (i=0; i<array.length; i++) {
            model.addElement(array[i]);
        }
        arrayList.setModel(model);
    }

    public String[] getArray() {
        return array;
    }

    public void setEnabled(boolean enabled) {
        arrayList.setEnabled(enabled);
        addButton.setEnabled(enabled);
        editButton.setEnabled(enabled);
        removeButton.setEnabled(enabled);
    }

    public void addSortedItemHelper(String text) {
        String[] newArray = new String[array.length+1];
        int idx = 0;
            while (idx < array.length &&
                    array[idx].compareTo(text) <= 0) {
                newArray[idx] = array[idx];
                idx++;
            }
        newArray[idx] = text;
        idx++;
            while (idx < newArray.length) {
                newArray[idx] = array[idx-1];
                idx++;
            }
        array = newArray;
    }

    public void addUnsortedItemHelper(String text) {
        String[] newArray = new String[array.length+1];
        int idx = 0;
        while (idx < array.length) {
            newArray[idx] = array[idx];
            idx++;
        }
        newArray[idx] = text;
        array = newArray;
    }

    public void addItem() {
        String text = JOptionPane.showInputDialog(this,
                "Enter " + tag + ":", "Add " + tag,
                JOptionPane.PLAIN_MESSAGE);
        if (text != null) {
            if (sorted) {
                addSortedItemHelper(text);
            } else {
                addUnsortedItemHelper(text);
            }
            refreshList();
            notifyCallback();
        }
    }

    public void editItem() {
        int cidx = arrayList.getSelectedIndex();
        if (cidx < 0) {
            return;
        }
        String citem = (String)arrayList.getSelectedValue();
        String text = (String)JOptionPane.showInputDialog(this,
                "Edit " + tag + ":", "Edit " + tag,
                JOptionPane.PLAIN_MESSAGE, null, null, citem);
        if (text != null) {
            if (sorted) {
                removeItemHelper(cidx);
                addSortedItemHelper(text);
            } else {
                array[cidx] = text;
            }
            refreshList();
            notifyCallback();
        }
    }

    public void removeItemHelper(int cidx) {
        String[] newArray = new String[array.length-1];
        int idx = 0;
        while (idx < cidx) {
            newArray[idx] = array[idx];
            idx++;
        }
        while (idx < newArray.length) {
            newArray[idx] = array[idx+1];
            idx++;
        }
        array = newArray;
    }

    public void removeItem() {
        int cidx = arrayList.getSelectedIndex();
        if (cidx < 0) {
            return;
        }
        String citem = (String)arrayList.getSelectedValue();
        int response = JOptionPane.YES_OPTION;
        if (confirmRemove) {
            response = JOptionPane.showConfirmDialog(this,
                    "Are you sure you wish to remove " + tag + 
                    "\"" + citem + "\"?", "Remove " + tag,
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        }
        if (response == JOptionPane.YES_OPTION) {
            removeItemHelper(cidx);
            refreshList();
            notifyCallback();
        }
    }

    public void notifyCallback() {
        callback.actionPerformed(new ActionEvent(this,
                ActionEvent.ACTION_PERFORMED, ACTION_UPDATE));
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == addButton) {
            addItem();
        } else if (evt.getSource() == editButton) {
            editItem();
        } else if (evt.getSource() == removeButton) {
            removeItem();
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && e.getSource() == arrayList &&
                arrayList.getSelectedIndex() != -1) {
            editItem();
        }
    }

    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }

}

