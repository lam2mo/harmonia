package harmonia;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class SongSetEditorPanel extends JPanel implements ActionListener, ListSelectionListener, MouseListener {

    public static final String ACTION_UPDATE = "songset-updated";

    private ActionListener callback;
    private SongSet mainSet;
    private Database mainDB;
    private SongSetItem selectedItem;

    private StringEditorPanel namePanel;
    private StringEditorPanel commentPanel;
    private SongSetTableModel songItemModel;
    private JTable songItemTable;
    private JButton addButton;
    private JPanel songItemPanel;
    private JLabel itemOrigTitleLabel;
    private StringEditorPanel itemTitlePanel;
    private JLabel itemOrigKeyLabel;
    private StringEditorPanel itemKeyPanel;
    private IntegerEditorPanel itemRepeatsPanel;
    private StringEditorPanel itemCommentPanel;
    private JPanel previewPanel;
    private JLabel previewPlaceholder;
    private JButton moveUpButton;
    private JButton moveDownButton;
    private JButton removeButton;
    private JButton assembleButton;

    public SongSetEditorPanel(ActionListener callback, SongSet set, Database db) {
        this.callback = callback;
        this.mainSet = set;
        this.mainDB = db;
        this.selectedItem = null;
        buildGUI();
        updateItemControls();
        if (set.songs.length > 0) {
            songItemTable.getSelectionModel().setSelectionInterval(0,0);
            updateItemControls();
        }
    }

    public void buildGUI() {

        namePanel = new StringEditorPanel(this, "Set name:", mainSet.name);
        JLabel createdLabel = new JLabel("Created on " + DateFormat.getDateInstance().format(mainSet.created));
        createdLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        commentPanel = new StringEditorPanel(this, "Set comments:", mainSet.comment, 20, 3);

        JPanel nameCreatedPanel = new JPanel();
        nameCreatedPanel.setLayout(new BoxLayout(nameCreatedPanel, BoxLayout.PAGE_AXIS));
        if (App.APP_DEBUG_SHOW_IDS) {
            nameCreatedPanel.add(new JLabel("ID: " + mainSet.id));
        }
        nameCreatedPanel.add(namePanel);
        nameCreatedPanel.add(createdLabel);

        addButton = new JButton("Add New Song");
        addButton.addActionListener(this);
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        assembleButton = new JButton("Assemble Set");
        assembleButton.addActionListener(this);
        assembleButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel topPanel = new JPanel();
        topPanel.add(nameCreatedPanel);
        topPanel.add(Box.createRigidArea(new Dimension(30,10)));
        topPanel.add(addButton);
        topPanel.add(assembleButton);

        JPanel songSetSettingsPanel = new JPanel();
        songSetSettingsPanel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        songSetSettingsPanel.setLayout(new BoxLayout(songSetSettingsPanel, BoxLayout.PAGE_AXIS));
        songSetSettingsPanel.add(topPanel);
        songSetSettingsPanel.add(commentPanel);

        songItemModel = new SongSetTableModel(mainSet);
        songItemTable = new JTable();
        songItemTable.addMouseListener(this);
        songItemTable.setModel(songItemModel);
        songItemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        songItemTable.getSelectionModel().addListSelectionListener(this);
        setTableColumnWidths();
        JScrollPane scrollPane = new JScrollPane(songItemTable);
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());
        listPanel.add(scrollPane, BorderLayout.CENTER);
        listPanel.setBorder(BorderFactory.createEmptyBorder(0,8,8,0));

        itemOrigTitleLabel = new JLabel("");
        itemTitlePanel = new StringEditorPanel(this, "Title:", "", 30, 1);
        itemOrigKeyLabel = new JLabel("");
        itemKeyPanel = new StringEditorPanel(this, "Key:", "", 5, 1);
        itemRepeatsPanel = new IntegerEditorPanel(this, "Repeats:", 0, 4);
        itemCommentPanel = new StringEditorPanel(this, "Comment:", "", 30, 1);

        removeButton = new JButton("Remove");
        removeButton.addActionListener(this);
        moveUpButton = new JButton("Move Up");
        moveUpButton.addActionListener(this);
        moveDownButton = new JButton("Move Down");
        moveDownButton.addActionListener(this);

        JPanel itemKeyRepeatPanel = new JPanel();
        itemKeyRepeatPanel.add(itemOrigKeyLabel);
        itemKeyRepeatPanel.add(itemKeyPanel);
        itemKeyRepeatPanel.add(itemRepeatsPanel);

        JPanel itemButtonPanel = new JPanel();
        itemButtonPanel.add(removeButton);
        itemButtonPanel.add(moveUpButton);
        itemButtonPanel.add(moveDownButton);

        songItemPanel = new JPanel();
        songItemPanel.setBorder(BorderFactory.createTitledBorder(""));
        songItemPanel.setLayout(new BoxLayout(songItemPanel, BoxLayout.PAGE_AXIS));
        songItemPanel.add(itemOrigTitleLabel);
        songItemPanel.add(itemTitlePanel);
        songItemPanel.add(itemKeyRepeatPanel);
        songItemPanel.add(itemButtonPanel);
        songItemPanel.add(itemCommentPanel);

        previewPlaceholder = new JLabel("(preview)");
        previewPlaceholder.setHorizontalAlignment(SwingConstants.CENTER);
        previewPlaceholder.setAlignmentY(Component.CENTER_ALIGNMENT);

        previewPanel = new JPanel();
        previewPanel.setLayout(new BorderLayout());
        previewPanel.add(previewPlaceholder, BorderLayout.CENTER);
        previewPanel.setPreferredSize(new Dimension(180,200));
        previewPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(8,8,8,16),
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1)));
        //JPanel buttonPanel = new JPanel();
        //buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
        //buttonPanel.add(Box.createVerticalGlue());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(songItemPanel, BorderLayout.CENTER);
        //bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(previewPanel, BorderLayout.WEST);

        setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
        setLayout(new BorderLayout());
        add(songSetSettingsPanel, BorderLayout.NORTH);
        add(listPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void setTableColumnWidths() {
        int width = 800;
        songItemTable.getColumnModel().getColumn(0).setPreferredWidth(width/4);
        songItemTable.getColumnModel().getColumn(1).setPreferredWidth(width/8);
        songItemTable.getColumnModel().getColumn(2).setPreferredWidth(width/8);
        songItemTable.getColumnModel().getColumn(3).setPreferredWidth(width/2);
    }

    public SongSet getSongSet() {
        return mainSet;
    }

    public Window getOwnerWindow() {
        Window owner = null;
        Container parent = getTopLevelAncestor();
        if (parent instanceof Window) {
            owner = (Window)parent;
        }
        return owner;
    }

    public Frame getOwnerFrame() {
        Frame owner = null;
        Container parent = getTopLevelAncestor();
        if (parent instanceof Frame) {
            owner = (Frame)parent;
        }
        return owner;
    }

    public void addItem() {
        Song song = SongChooser.showDialog(getOwnerWindow(),
                "Choose a song:", "Add Song", mainDB.allSongs);
        if (song != null) {
            SongSetItem item = new SongSetItem(song);
            SongSetItem[] newSongs = new SongSetItem[mainSet.songs.length+1];
            int idx = 0;
            while (idx < mainSet.songs.length) {
                newSongs[idx] = mainSet.songs[idx];
                idx++;
            }
            newSongs[idx] = item;
            mainSet.songs = newSongs;
            songItemModel.fireTableRowsInserted(idx,idx);
            notifyCallback();
        }
    }

    public void editItem() {
        int cidx = songItemTable.getSelectedRow();
        if (cidx < 0) {
            return;
        }
        selectedItem = mainSet.songs[cidx];

        if (selectedItem.song != null) {
            SongEditor editor = new SongEditor(getOwnerFrame(), mainDB, selectedItem.song);
            editor.setVisible(true);
            notifyCallback();
            songItemModel.fireTableRowsUpdated(cidx,cidx);
        } else {
            JOptionPane.showMessageDialog(getOwnerFrame(), 
                    "This song was deleted from the database and is no longer available.", 
                    "Deleted song", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void removeItemHelper(int cidx) {
        SongSetItem[] newArray = new SongSetItem[mainSet.songs.length-1];
        int idx = 0;
        while (idx < cidx) {
            newArray[idx] = mainSet.songs[idx];
            idx++;
        }
        while (idx < newArray.length) {
            newArray[idx] = mainSet.songs[idx+1];
            idx++;
        }
        mainSet.songs = newArray;
    }

    public void removeItem() {
        int cidx = songItemTable.getSelectedRow();
        if (cidx < 0) {
            return;
        }
        removeItemHelper(cidx);
        selectedItem = null;
        updateItemControls();
        songItemModel.fireTableRowsDeleted(cidx,cidx);
        notifyCallback();
    }

    public void moveItemUp() {
        int cidx = songItemTable.getSelectedRow();
        if (cidx > 0) {
            SongSetItem tmp = mainSet.songs[cidx];
            mainSet.songs[cidx] = mainSet.songs[cidx-1];
            mainSet.songs[cidx-1] = tmp;
            songItemModel.fireTableRowsUpdated(cidx-1,cidx);
            songItemTable.getSelectionModel().setSelectionInterval(cidx-1,cidx-1);
            notifyCallback();
        }
    }

    public void moveItemDown() {
        int cidx = songItemTable.getSelectedRow();
        if (cidx >= 0 && cidx < mainSet.songs.length-1) {
            SongSetItem tmp = mainSet.songs[cidx];
            mainSet.songs[cidx] = mainSet.songs[cidx+1];
            mainSet.songs[cidx+1] = tmp;
            songItemModel.fireTableRowsUpdated(cidx,cidx+1);
            songItemTable.getSelectionModel().setSelectionInterval(cidx+1,cidx+1);
            notifyCallback();
        }
    }

    public void assemble() {
        String[] ASSEMBLE_OPTIONS = {
            "Song list (plain text)",
            "All chords (plain text)",
            "All lyrics (plain text)",
            "All chords (rich text)",
            "All lyrics (rich text)"
        };
        Object choice = JOptionPane.showInputDialog(this,
                "What would you like to assemble?",
                "Assemble Set", JOptionPane.PLAIN_MESSAGE, null,
                ASSEMBLE_OPTIONS, ASSEMBLE_OPTIONS[0]);
        if (choice == null) {
            return;
        }
        if (choice.equals(ASSEMBLE_OPTIONS[0])) {
            assembleTitleKeyCommentList();
        } else if (choice.equals(ASSEMBLE_OPTIONS[1])) {
            assemblePlainTextAttachments(Attachment.DataType.CHORDS);
        } else if (choice.equals(ASSEMBLE_OPTIONS[2])) {
            assemblePlainTextAttachments(Attachment.DataType.LYRICS);
        } else if (choice.equals(ASSEMBLE_OPTIONS[3])) {
            assembleRichTextAttachments(Attachment.DataType.CHORDS);
        } else if (choice.equals(ASSEMBLE_OPTIONS[4])) {
            assembleRichTextAttachments(Attachment.DataType.LYRICS);
        }
    }

    public void assembleTitleKeyCommentList() {
        StringBuffer text = new StringBuffer();
        SongSetItem ssi;
        int i;
        for (i=0; i<mainSet.songs.length; i++) {
            ssi = mainSet.songs[i];
            text.append(ssi.title);
            text.append(" (");
            text.append(ssi.key.toString());
            text.append(")");
            if (ssi.comment.length() > 0) {
                text.append("    (");
                text.append(ssi.comment);
                text.append(")");
            }
            text.append(Util.getLineSeparator());
        }
        PrintDialog.showPlainTextDialog(getOwnerFrame(),
                App.APP_NAME + " - " + mainSet.name,
                text.toString());
    }

    public void assemblePlainTextAttachments(Attachment.DataType dt) {
        StringBuffer text = new StringBuffer();
        String[] aKeys;
        Attachment a;
        int i, j;
        int totalAttachments = 0;
        for (i=0; i<mainSet.songs.length; i++) {
            aKeys = mainSet.songs[i].song.attachmentKeys;
            for (j=0; j<aKeys.length; j++) {
                a = mainDB.attachments.get(aKeys[j]);
                if (a != null && a.dataType == dt
                        && a.fileType == Attachment.FileType.PLAINTEXT) {
                    if (!a.cached) {
                        a.openFromFile();
                    }
                    if (totalAttachments > 0) {
                        text.append(Util.getLineSeparator());
                        text.append(Util.getLineSeparator());
                        text.append(Util.getLineSeparator());
                        text.append(Util.getLineSeparator());
                    }
                    text.append(new String(a.data));
                    totalAttachments++;
                    break;
                }
            }
        }
        PrintDialog.showPlainTextDialog(getOwnerFrame(),
                App.APP_NAME + " - \"" + mainSet.name + "\"",
                text.toString(), true);
    }

    public void assembleRichTextAttachments(Attachment.DataType dt) {
        StringBuffer text = new StringBuffer();
        String[] aKeys;
        Attachment a;
        int i, j;
        int totalAttachments = 0;
        for (i=0; i<mainSet.songs.length; i++) {
            aKeys = mainSet.songs[i].song.attachmentKeys;
            for (j=0; j<aKeys.length; j++) {
                a = mainDB.attachments.get(aKeys[j]);
                // TODO: also append plain text files?
                if (a != null && a.dataType == dt
                        && a.fileType == Attachment.FileType.RICHTEXT) {
                    if (!a.cached) {
                        a.openFromFile();
                    }
                    if (totalAttachments > 0) {
                        text.append(Util.getLineSeparator());
                        text.append(Util.getLineSeparator());
                        text.append(Util.getLineSeparator());
                        text.append(Util.getLineSeparator());
                    }
                    text.append(new String(a.data));
                    totalAttachments++;
                    break;
                }
            }
        }
        PrintDialog.showRichTextDialog(getOwnerFrame(),
                App.APP_NAME + " - \"" + mainSet.name + "\"",
                text.toString());
    }

    public void updateItemControls() {
        if (selectedItem != null) {
            songItemPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(10,0,0,0),
                        BorderFactory.createTitledBorder(" Song details: ")));
                        //BorderFactory.createTitledBorder(" " + selectedItem.title + " ")));
            if (selectedItem.song != null) {
                itemOrigTitleLabel.setText("Original title:  " + selectedItem.song.title);
            } else {
                itemOrigTitleLabel.setText("(song unavailable)");
            }
            itemOrigTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            itemTitlePanel.setText(selectedItem.title);
            if (selectedItem.song != null) {
                itemOrigKeyLabel.setText("Original Key:  " + selectedItem.song.key.toString() + 
                        "       ");
            } else {
                itemOrigKeyLabel.setText("");
            }
            itemKeyPanel.setText(selectedItem.key.toString());
            itemRepeatsPanel.setValue(selectedItem.repeats);
            itemCommentPanel.setText(selectedItem.comment);
            previewPanel.removeAll();
            java.util.List<Component> previews = new ArrayList<Component>();
            if (selectedItem.song.attachmentKeys.length > 0) {
                Attachment a = mainDB.attachments.get(selectedItem.song.attachmentKeys[0]);
                if (a.fileType == Attachment.FileType.PLAINTEXT) {
                    if (!a.cached) {
                        a.openFromFile();
                    }
                    JTextArea previewText = new JTextArea(new String(a.data));
                    previewText.setLineWrap(true);
                    previewText.setWrapStyleWord(true);
                    previewText.setEditable(false);
                    if (a.dataType == Attachment.DataType.CHORDS) {
                        previewText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 4));
                    } else {
                        previewText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 4));
                    }
                    previews.add(new JScrollPane(previewText));
                } /* TODO: fix to zoom properly
                else if (a.fileType == Attachment.FileType.RICHTEXT) {
                    if (!a.cached) {
                        a.openFromFile();
                    }
                    JEditorPane previewEditor = new JEditorPane();
                    previewEditor.setContentType("text/rtf");
                    previewEditor.setText(new String(a.data));
                    previewEditor.setEditable(false);
                    previews.add(new JScrollPane(previewEditor));
                } */
            }
            if (previews.size() == 1) {
                previewPanel.add(previews.get(0), BorderLayout.CENTER);
            } else if (previews.size() > 1) {
                // TODO: tabbed interface?
                previewPanel.add(previews.get(0), BorderLayout.CENTER);
            } else {
                previewPanel.add(previewPlaceholder, BorderLayout.CENTER);
            }
            previewPanel.invalidate();
            enableItemControls();
        } else {
            songItemPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(10,0,0,0),
                        BorderFactory.createTitledBorder("")));
            previewPanel.removeAll();
            previewPanel.add(previewPlaceholder, BorderLayout.CENTER);
            previewPanel.invalidate();
            disableItemControls();
        }
    }

    public void enableItemControls() {
        itemTitlePanel.setEnabled(true);
        itemOrigKeyLabel.setEnabled(true);
        itemKeyPanel.setEnabled(true);
        itemRepeatsPanel.setEnabled(true);
        itemCommentPanel.setEnabled(true);
        moveUpButton.setEnabled(true);
        moveDownButton.setEnabled(true);
        removeButton.setEnabled(true);
    }

    public void disableItemControls() {
        itemTitlePanel.setEnabled(false);
        itemOrigKeyLabel.setEnabled(false);
        itemKeyPanel.setEnabled(false);
        itemRepeatsPanel.setEnabled(false);
        itemCommentPanel.setEnabled(false);
        moveUpButton.setEnabled(false);
        moveDownButton.setEnabled(false);
        removeButton.setEnabled(false);
    }

    public void notifyCallback() {
        callback.actionPerformed(new ActionEvent(this,
                ActionEvent.ACTION_PERFORMED, ACTION_UPDATE));
    }

    public void actionPerformed(ActionEvent evt) {
        int cidx = songItemTable.getSelectedRow();
        if (evt.getSource() == namePanel) {
            mainSet.name = namePanel.getText();
            notifyCallback();
        } else if (evt.getSource() == commentPanel) {
            mainSet.comment = commentPanel.getText();
            notifyCallback();
        } else if (evt.getSource() == itemTitlePanel) {
            selectedItem.title = itemTitlePanel.getText();
            songItemModel.fireTableRowsUpdated(cidx,cidx);
            notifyCallback();
        } else if (evt.getSource() == itemKeyPanel) {
            selectedItem.key = new Key(itemKeyPanel.getText());
            songItemModel.fireTableRowsUpdated(cidx,cidx);
            notifyCallback();
        } else if (evt.getSource() == itemRepeatsPanel) {
            selectedItem.repeats = itemRepeatsPanel.getValue();
            songItemModel.fireTableRowsUpdated(cidx,cidx);
            notifyCallback();
        } else if (evt.getSource() == itemCommentPanel) {
            selectedItem.comment = itemCommentPanel.getText();
            songItemModel.fireTableRowsUpdated(cidx,cidx);
            notifyCallback();
        } else if (evt.getSource() == addButton) {
            addItem();
        } else if (evt.getSource() == assembleButton) {
            assemble();
        } else if (evt.getSource() == moveUpButton) {
            moveItemUp();
        } else if (evt.getSource() == moveDownButton) {
            moveItemDown();
        } else if (evt.getSource() == removeButton) {
            removeItem();
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if (songItemTable.getSelectedRow() != -1) {
            selectedItem = mainSet.songs[songItemTable.getSelectedRow()];
            updateItemControls();
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && e.getSource() == songItemTable &&
                songItemTable.getSelectedRow() != -1) {
            editItem();
        }
    }

    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }

}

