package harmonia;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.border.*;

public class SongEditor extends JDialog implements ActionListener, ItemListener {

    private Database mainDB;
    private Song mainSong;

    private StringEditorPanel titlePanel;
    private StringArrayEditorPanel aliasPanel;
    private StringArrayEditorPanel authorPanel;
    private IntegerEditorPanel yearPanel;
    private StringEditorPanel copyrightPanel;
    private StringEditorPanel keyPanel;
    private IntegerEditorPanel meter0Panel;
    private IntegerEditorPanel meter1Panel;
    private IntegerEditorPanel lengthPanel;
    private IntegerEditorPanel tempoPanel;
    private StringEditorPanel commentPanel;
    private StringArrayEditorPanel tagPanel;
    private JComboBox attachmentCombo;
    private JButton addAttachmentButton;
    private JButton replaceAttachmentButton;
    private JButton printAttachmentButton;
    private JButton removeAttachmentButton;
    private JPanel attachmentViewer;
    private JTextArea tempPlainText;
    private JEditorPane tempRichText;
    private JButton exitButton;

    public SongEditor(Frame owner, Database db, Song song) {
        super(owner, App.APP_NAME, false);
        mainDB = db;
        mainSong = song;
        buildGUI();
        updateTitle();
        updateAttachmentCombo();
        pack();
        setSize(800,700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void buildGUI() {

        titlePanel = new StringEditorPanel(this, 
                "Main title:", mainSong.title);

        aliasPanel = new StringArrayEditorPanel(this,
                "Alternate title(s)", "alternate title", 
                mainSong.aliases, 2, true, false);
        authorPanel = new StringArrayEditorPanel(this,
                "Author(s)", "author", mainSong.authors, 2);

        yearPanel = new IntegerEditorPanel(this, 
                "Year:", mainSong.year, 5);
        copyrightPanel = new StringEditorPanel(this, 
                "Copyright:", mainSong.copyright);

        JPanel keyMeterPanel = new JPanel();
        keyPanel = new StringEditorPanel(this, 
                "Key:", mainSong.key.toString(), 5, 1);
        meter0Panel = new IntegerEditorPanel(this, 
                null, mainSong.meter[0], 2);
        meter1Panel = new IntegerEditorPanel(this, 
                null, mainSong.meter[1], 2);
        JPanel meterPanel = new JPanel();
        meterPanel.add(new JLabel("Meter:"));
        meterPanel.add(meter0Panel);
        meterPanel.add(new JLabel("/"));
        meterPanel.add(meter1Panel);
        keyMeterPanel.setLayout(new BoxLayout(keyMeterPanel, BoxLayout.LINE_AXIS));
        keyMeterPanel.add(keyPanel);
        keyMeterPanel.add(meterPanel);

        JPanel lengthTempoPanel = new JPanel();
        lengthPanel = new IntegerEditorPanel(this, 
                "Length (sec):", mainSong.length, 4);
        tempoPanel = new IntegerEditorPanel(this, 
                "Tempo (bpm):", mainSong.tempo, 4);
        lengthTempoPanel.setLayout(new BoxLayout(lengthTempoPanel, BoxLayout.LINE_AXIS));
        lengthTempoPanel.add(lengthPanel);
        lengthTempoPanel.add(tempoPanel);

        commentPanel = new StringEditorPanel(this, 
                "Comments:", mainSong.comment, 20, 4);
        tagPanel = new StringArrayEditorPanel(this,
                "Tag(s)", "tag", mainSong.tags);
        // TODO: properties

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.PAGE_AXIS));
        if (App.APP_DEBUG_SHOW_IDS) {
            settingsPanel.add(new JLabel("ID: " + mainSong.id));
        }
        settingsPanel.add(titlePanel);
        settingsPanel.add(aliasPanel);
        settingsPanel.add(authorPanel);
        settingsPanel.add(yearPanel);
        settingsPanel.add(copyrightPanel);
        settingsPanel.add(keyMeterPanel);
        settingsPanel.add(lengthTempoPanel);
        settingsPanel.add(commentPanel);
        settingsPanel.add(tagPanel);
        // TODO: properties
        JLabel addedLabel = new JLabel("Added to database on " +
                DateFormat.getDateInstance().format(mainSong.added));
        addedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        settingsPanel.add(addedLabel);

        JPanel mainButtonPanel = new JPanel();
        exitButton = new JButton("Close");
        exitButton.addActionListener(this);
        mainButtonPanel.add(exitButton);

        JPanel attachmentChooserPanel = new JPanel();
        attachmentChooserPanel.add(new JLabel("Attachment(s):"));
        attachmentCombo = new JComboBox();
        attachmentCombo.setPreferredSize(new Dimension(200,25));
        attachmentCombo.addItemListener(this);
        attachmentChooserPanel.add(attachmentCombo);

        JPanel attachmentButtonPanel = new JPanel();
        addAttachmentButton = new JButton("Add");
        addAttachmentButton.addActionListener(this);
        attachmentButtonPanel.add(addAttachmentButton);
        replaceAttachmentButton = new JButton("Edit");
        replaceAttachmentButton.addActionListener(this);
        attachmentButtonPanel.add(replaceAttachmentButton);
        printAttachmentButton = new JButton("Print");
        printAttachmentButton.addActionListener(this);
        attachmentButtonPanel.add(printAttachmentButton);
        removeAttachmentButton = new JButton("Remove");
        removeAttachmentButton.addActionListener(this);
        attachmentButtonPanel.add(removeAttachmentButton);

        JPanel attachmentControlPanel = new JPanel();
        attachmentControlPanel.setLayout(new BoxLayout(attachmentControlPanel, BoxLayout.PAGE_AXIS));
        attachmentControlPanel.add(attachmentChooserPanel);
        attachmentControlPanel.add(attachmentButtonPanel);

        JPanel attachmentWrapper = new JPanel();
        attachmentViewer = new JPanel();
        attachmentViewer.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        attachmentViewer.setBackground(Color.WHITE);
        attachmentWrapper.setLayout(new BorderLayout());
        attachmentWrapper.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        attachmentWrapper.add(attachmentViewer, BorderLayout.CENTER);

        JPanel attachmentsPanel = new JPanel();
        attachmentsPanel.setLayout(new BorderLayout());
        attachmentsPanel.add(attachmentControlPanel, BorderLayout.NORTH);
        attachmentsPanel.add(attachmentWrapper, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(settingsPanel, BorderLayout.WEST);
        mainPanel.add(attachmentsPanel, BorderLayout.CENTER);
        mainPanel.add(mainButtonPanel, BorderLayout.SOUTH);
        getContentPane().add(mainPanel);
    }

    public void updateTitle() {
        setTitle(App.APP_NAME + " - \"" + mainSong.title + "\"");
    }

    public void updateAttachmentCombo() {
        int i;
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (i=0; i<mainSong.attachmentKeys.length; i++) {
            String key = mainSong.attachmentKeys[i];
            Attachment file = mainDB.attachments.get(key);
            model.addElement(file);
        }
        attachmentCombo.setModel(model);
        updateAttachmentPreview();
    }

    public void updateAttachmentPreview() {
        attachmentViewer.removeAll();
        Attachment citem = (Attachment)attachmentCombo.getSelectedItem();
        if (citem != null) {
            if (citem.fileType == Attachment.FileType.PLAINTEXT) {
                citem.openFromFile();
                tempPlainText = new JTextArea();
                tempPlainText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
                tempPlainText.setText(new String(citem.data));
                tempPlainText.setEditable(false);
                tempPlainText.setSelectionStart(0);
                tempPlainText.setSelectionEnd(0);
                JScrollPane scrollPanel = new JScrollPane(tempPlainText);
                attachmentViewer.setLayout(new BorderLayout());
                attachmentViewer.add(scrollPanel, BorderLayout.CENTER);
            } else if (citem.fileType == Attachment.FileType.RICHTEXT) {
                citem.openFromFile();
                tempRichText = new JEditorPane();
                tempRichText.setContentType("text/rtf");
                tempRichText.setText(new String(citem.data));
                tempRichText.setEditable(false);
                tempRichText.setSelectionStart(0);
                tempRichText.setSelectionEnd(0);
                JScrollPane scrollPanel = new JScrollPane(tempRichText);
                attachmentViewer.setLayout(new BorderLayout());
                attachmentViewer.add(scrollPanel, BorderLayout.CENTER);
            }
        }
        attachmentViewer.validate();
        attachmentViewer.repaint();
    }

    public void addAttachment() {
        AttachmentEditor editor = new AttachmentEditor(this, mainDB, mainSong);
        editor.setVisible(true);
        Attachment a = editor.getAttachment();
        if (a != null) {
            a.saveToFile();
            String[] newAttachmentKeys = new String[mainSong.attachmentKeys.length+1];
            int i;
            for (i=0; i<mainSong.attachmentKeys.length; i++) {
                newAttachmentKeys[i] = mainSong.attachmentKeys[i];
            }
            newAttachmentKeys[newAttachmentKeys.length-1] = a.key;
            mainSong.attachmentKeys = newAttachmentKeys;
            updateAttachmentCombo();
            attachmentCombo.setSelectedIndex(newAttachmentKeys.length-1);
            updateAttachmentPreview();
        }
    }

    public void replaceAttachment() {
        int cidx = attachmentCombo.getSelectedIndex();
        if (cidx < 0) {
            return;
        }
        Attachment citem = (Attachment)attachmentCombo.getSelectedItem();
        AttachmentEditor editor = new AttachmentEditor(this, mainDB, mainSong, citem);
        editor.setVisible(true);
        Attachment a = editor.getAttachment();
        if (a != null) {
            a.saveToFile();
            mainSong.attachmentKeys[cidx] = a.key;
            updateAttachmentCombo();
            attachmentCombo.setSelectedIndex(cidx);
            updateAttachmentPreview();
        }
    }

    public void printAttachment() {
        int cidx = attachmentCombo.getSelectedIndex();
        if (cidx < 0) {
            return;
        }
        Attachment citem = (Attachment)attachmentCombo.getSelectedItem();
        try {
            switch (citem.fileType) {
                case PLAINTEXT: tempPlainText.print(); break;
                case RICHTEXT:  tempRichText.print(); break;
                default:        break;
            }
        } catch (PrinterException ex) {
            ex.printStackTrace();
        }
    }

    public void removeAttachment() {
        int cidx = attachmentCombo.getSelectedIndex();
        if (cidx < 0) {
            return;
        }
        Attachment citem = (Attachment)attachmentCombo.getSelectedItem();
        int response = JOptionPane.showConfirmDialog(this,
                "Are you sure you wish to remove attachment " + 
                "\"" + citem.toString() + "\"?", "Remove attachment",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response == JOptionPane.YES_OPTION) {
            String[] newAttachmentKeys = new String[mainSong.attachmentKeys.length-1];
            int idx = 0;
            while (idx < cidx) {
                newAttachmentKeys[idx] = mainSong.attachmentKeys[idx];
                idx++;
            }
            while (idx < newAttachmentKeys.length) {
                newAttachmentKeys[idx] = mainSong.attachmentKeys[idx+1];
                idx++;
            }
            mainSong.attachmentKeys = newAttachmentKeys;
            mainDB.attachments.remove(citem.key);
            updateAttachmentCombo();
        }
    }

    public Song getSong() {
        return mainSong;
    }

    public void itemStateChanged(ItemEvent evt) {
        updateAttachmentPreview();
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == titlePanel) {
            mainSong.title = titlePanel.getText();
            updateTitle();
        } else if (evt.getSource() == aliasPanel) {
            mainSong.aliases = aliasPanel.getArray();
        } else if (evt.getSource() == authorPanel) {
            mainSong.authors = authorPanel.getArray();
        } else if (evt.getSource() == yearPanel) {
            mainSong.year = yearPanel.getValue();
        } else if (evt.getSource() == copyrightPanel) {
            mainSong.copyright = copyrightPanel.getText();
        } else if (evt.getSource() == keyPanel) {
            mainSong.key = new Key(keyPanel.getText());
        } else if (evt.getSource() == meter0Panel) {
            mainSong.meter[0] = meter0Panel.getValue();
        } else if (evt.getSource() == meter1Panel) {
            mainSong.meter[1] = meter1Panel.getValue();
        } else if (evt.getSource() == lengthPanel) {
            mainSong.length = lengthPanel.getValue();
        } else if (evt.getSource() == tempoPanel) {
            mainSong.tempo = tempoPanel.getValue();
        } else if (evt.getSource() == commentPanel) {
            mainSong.comment = commentPanel.getText();
        } else if (evt.getSource() == tagPanel) {
            mainSong.tags = tagPanel.getArray();
        } else if (evt.getSource() == addAttachmentButton) {
            addAttachment();
        } else if (evt.getSource() == replaceAttachmentButton) {
            replaceAttachment();
        } else if (evt.getSource() == printAttachmentButton) {
            printAttachment();
        } else if (evt.getSource() == removeAttachmentButton) {
            removeAttachment();
        } else if (evt.getSource() == exitButton) {
            dispose();
        }
    }

}

