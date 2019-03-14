package harmonia;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.text.rtf.*;

public class AttachmentEditor extends JDialog implements ActionListener {

    // TODO: refresh RTF display when focus is acquired
    // TODO: handle tab switching (switch file types & delete old files?)

    private static final String[] DATATYPE_CHOICES = { "Lyrics", "Slides",
        "Chords", "Lead", "Sheet", "Recording", "Sample", "Other" };
    private static final String[] FILETYPE_CHOICES = { "Plain text", "Rich text", 
        "HTML", "PDF", "PPT", "Image", "Other" };

    private static final int TAB_PLAIN_TEXT = 0;
    private static final int TAB_RICH_TEXT = 1;
    private static final int TAB_FILE = 2;

    private Database mainDB;
    private Song mainSong;
    private Attachment prevAttachment;

    private boolean openedInExternalEditor;
    private JTabbedPane tabPanel;
    private StringEditorPanel textPanel;
    private JEditorPane editorPanel;
    private JButton fileButton;
    private byte[] attachmentData;
    private JPanel previewPanel;
    private JComboBox dataTypeCombo;
    private JComboBox fileTypeCombo;
    private StringEditorPanel commentPanel;
    private JButton okButton;
    private JButton cancelButton;

    private boolean canceled = true;

    public AttachmentEditor(Dialog owner, Database db, Song song, Attachment attachment) {
        this(owner, db, song);
        prevAttachment = attachment;
        tabPanel.setSelectedIndex(TAB_FILE);
        attachmentData = attachment.data;
        openedInExternalEditor = false;
        switch (attachment.dataType) {
            case LYRICS:    dataTypeCombo.setSelectedIndex(0); break;
            case SLIDES:    dataTypeCombo.setSelectedIndex(1); break;
            case CHORDS:    dataTypeCombo.setSelectedIndex(2); break;
            case LEAD:      dataTypeCombo.setSelectedIndex(3); break;
            case SHEET:     dataTypeCombo.setSelectedIndex(4); break;
            case RECORDING: dataTypeCombo.setSelectedIndex(5); break;
            case SAMPLE:    dataTypeCombo.setSelectedIndex(6); break;
            case UNKNOWN:   dataTypeCombo.setSelectedIndex(7); break;
        }
        switch (attachment.fileType) {
            case PLAINTEXT: fileTypeCombo.setSelectedIndex(0);
                            attachment.openFromFile();
                            textPanel.setText(new String(attachment.data));
                            tabPanel.setSelectedIndex(TAB_PLAIN_TEXT);
                            break;
            case RICHTEXT:  fileTypeCombo.setSelectedIndex(1);
                            attachment.openFromFile();
                            editorPanel.setText(new String(attachment.data));
                            editorPanel.setEditable(false);
                            tabPanel.setSelectedIndex(TAB_RICH_TEXT);
                            try {
                                Desktop.getDesktop().open(attachment.getFile());
                                openedInExternalEditor = true;
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                editorPanel.setEditable(true);
                            }
                            break;
            case HTML:      fileTypeCombo.setSelectedIndex(2); break;
            case PDF:       fileTypeCombo.setSelectedIndex(3); break;
            case PPT:       fileTypeCombo.setSelectedIndex(4);
                            try {
                                Desktop.getDesktop().open(attachment.getFile());
                                openedInExternalEditor = true;
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                editorPanel.setEditable(true);
                            }
                            break;
            case IMAGE:     fileTypeCombo.setSelectedIndex(5); break;
            case UNKNOWN:   fileTypeCombo.setSelectedIndex(6); break;
        }
        commentPanel.setText(attachment.comment);
    }

    public AttachmentEditor(Dialog owner, Database db, Song song) {
        super(owner, "Choose attachment", true);
        mainDB = db;
        mainSong = song;
        buildGUI();
        updatePreview();
        pack();
        setSize(500,700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public void buildGUI() {

        JPanel plainTextPanel = new JPanel();
        textPanel = new StringEditorPanel(this, "", "", 50, 40);
        textPanel.setEditorFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        plainTextPanel.setLayout(new BorderLayout());
        plainTextPanel.add(new JScrollPane(textPanel), BorderLayout.CENTER);

        JPanel richTextPanel = new JPanel();
        editorPanel = new JEditorPane();
        editorPanel.setContentType("text/rtf");
        //editorPanel.setEditorKit(new RTFEditorKit());
        richTextPanel.setLayout(new BorderLayout());
        richTextPanel.add(new JScrollPane(editorPanel), BorderLayout.CENTER);

        fileButton = new JButton("Import file");
        fileButton.addActionListener(this);

        previewPanel = new JPanel();
        previewPanel.setLayout(new BorderLayout());

        JPanel filePanel = new JPanel();
        filePanel.add(fileButton, BorderLayout.NORTH);
        filePanel.add(previewPanel, BorderLayout.CENTER);

        tabPanel = new JTabbedPane();
        tabPanel.setBorder(BorderFactory.createEmptyBorder(8,8,0,8));
        tabPanel.addTab("Enter/paste plain text", plainTextPanel);
        tabPanel.addTab("Enter/paste rich text (RTF)", richTextPanel);
        tabPanel.addTab("Attach a file", filePanel);

        JPanel dataFileTypePanel = new JPanel();
        dataFileTypePanel.setLayout(new BoxLayout(dataFileTypePanel, BoxLayout.LINE_AXIS));
        dataFileTypePanel.setBorder(BorderFactory.createEmptyBorder(0,8,0,8));
        dataFileTypePanel.add(new JLabel("Data type:"));
        dataTypeCombo = new JComboBox(new DefaultComboBoxModel(DATATYPE_CHOICES));
        dataFileTypePanel.add(dataTypeCombo);
        dataFileTypePanel.add(Box.createRigidArea(new Dimension(8,0)));
        dataFileTypePanel.add(new JLabel("File type:"));
        fileTypeCombo = new JComboBox(new DefaultComboBoxModel(FILETYPE_CHOICES));
        dataFileTypePanel.add(fileTypeCombo);

        commentPanel = new StringEditorPanel(this,
                "Comments:", "", 30, 4);

        JPanel commonPanel = new JPanel();
        commonPanel.setLayout(new BoxLayout(commonPanel, BoxLayout.PAGE_AXIS));
        commonPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(8,8,0,8),
                    BorderFactory.createTitledBorder("Common properties")));
        commonPanel.add(dataFileTypePanel);
        commonPanel.add(commentPanel);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(tabPanel, BorderLayout.CENTER);
        topPanel.add(commonPanel, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel();
        okButton = new JButton("OK");
        okButton.addActionListener(this);
        bottomPanel.add(okButton);
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        bottomPanel.add(cancelButton);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        getContentPane().add(mainPanel);
    }

    public Attachment getAttachment() {
        Attachment a = null;
        if (!canceled) {
            if (prevAttachment == null) {
                a = mainDB.addNewAttachment(mainSong.title, 
                        Attachment.String2DataType((String)dataTypeCombo.getSelectedItem()),
                        Attachment.String2FileType((String)fileTypeCombo.getSelectedItem()));
            } else {
                a = prevAttachment;
                a.dataType = Attachment.String2DataType((String)dataTypeCombo.getSelectedItem());
                a.fileType = Attachment.String2FileType((String)fileTypeCombo.getSelectedItem());
            }
            a.comment = commentPanel.getText();
            int idx = tabPanel.getSelectedIndex();
            switch (idx) {
                case TAB_PLAIN_TEXT: // plain text
                    a.setBytes(textPanel.getText().getBytes());
                    break;
                case TAB_RICH_TEXT: // rich text
                    if (!openedInExternalEditor) {
                        try {
                            ByteArrayOutputStream fout = new ByteArrayOutputStream();
                            RTFEditorKit kit = new RTFEditorKit();
                            Document doc = editorPanel.getDocument();
                            kit.write(fout, doc, doc.getStartPosition().getOffset(), doc.getLength());
                            fout.close();
                            a.setBytes(fout.toByteArray());
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (BadLocationException ex) {
                            ex.printStackTrace();
                        }
                    }
                    break;
                case TAB_FILE: // file
                    if (!openedInExternalEditor) {
                        a.setBytes(attachmentData);
                    }
                    break;
            }
        }
        return a;
    }

    public void updatePreview() {
        previewPanel.removeAll();
        if (attachmentData != null) {
            JTextArea rawData = new JTextArea(new String(attachmentData), 50, 20);
            rawData.setLineWrap(true);
            JScrollPane rawScroll = new JScrollPane(rawData);
            //switch (Attachment.String2FileType((String)fileTypeCombo.getSelectedItem())) {
                //case PLAINTEXT: break;
                //case RICHTEXT:  break;
                //case HTML:      break;
                //case PDF:       break;
                //case IMAGE:     break;
                //case UNKNOWN:   break;
            //}
            previewPanel.add(rawScroll, BorderLayout.CENTER);
        }
        previewPanel.revalidate();
    }

    public void importFile() {
        JFileChooser fc = new JFileChooser();
        int response = fc.showOpenDialog(this);
        if (response == JFileChooser.APPROVE_OPTION) {
            try {
                File path = fc.getSelectedFile();
                FileInputStream fis = new FileInputStream(path);
                attachmentData = new byte[(int)path.length()];
                fis.read(attachmentData);
                fis.close();
                String ext = Util.getExtension(path.getAbsolutePath());
                if (ext.equalsIgnoreCase("pdf")) {
                    fileTypeCombo.setSelectedIndex(3);
                }
                updatePreview();
            } catch (IOException ex) {
                System.out.println("ERROR opening attachment: " + ex.getMessage());
            }
        }
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == okButton) {
            canceled = false;
            dispose();
        } else if (evt.getSource() == cancelButton) {
            dispose();
        } else if (evt.getSource() == fileButton) {
            importFile();
        }
    }

}

