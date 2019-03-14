package harmonia;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class StringEditorPanel extends JPanel implements DocumentListener {

    public static final String ACTION_UPDATE = "string-updated";

    private ActionListener callback;
    private String caption;
    private int columns;
    private int rows;
    private String text;

    private JLabel captionText;
    private JTextComponent textField;

    public StringEditorPanel(ActionListener callback, String caption, 
            String text) {
        this(callback, caption, text, 20, 1);
    }

    public StringEditorPanel(ActionListener callback, String caption, 
            String text, int columns, int rows) {
        this.callback = callback;
        this.caption = caption;
        this.text = text;
        this.columns = columns;
        this.rows = rows;

        buildGUI();
    }

    public void buildGUI() {
        captionText = new JLabel();
        if (rows > 1) {
            setLayout(new BorderLayout());
            if (caption != null) {
                add(new JLabel(caption), BorderLayout.NORTH);
            }
            textField = new JTextArea(text, rows, columns);
            ((JTextArea)textField).setLineWrap(true);
            ((JTextArea)textField).setWrapStyleWord(true);
            add(new JScrollPane(textField), BorderLayout.CENTER);
            setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
        } else {
            if (caption != null) {
                captionText.setText(caption);
                add(captionText);
            }
            textField = new JTextField(text, columns);
            add(textField);
        }
        textField.getDocument().addDocumentListener(this);
    }

    public void setEditorFont(Font f) {
        textField.setFont(f);
    }

    public void setText(String text) {
        this.text = text;
        textField.getDocument().removeDocumentListener(this);
        textField.setText(text);
        textField.getDocument().addDocumentListener(this);
    }

    public void setEnabled(boolean enabled) {
        captionText.setEnabled(enabled);
        textField.setEnabled(enabled);
    }

    public String getText() {
        return text;
    }

    public void notifyCallback() {
        if (callback != null) {
            callback.actionPerformed(new ActionEvent(this,
                    ActionEvent.ACTION_PERFORMED, ACTION_UPDATE));
        }
    }

    public void changedUpdate(DocumentEvent e) {
        text = textField.getText();
        notifyCallback();
    }

    public void insertUpdate(DocumentEvent e) {
        text = textField.getText();
        notifyCallback();
    }

    public void removeUpdate(DocumentEvent e) {
        text = textField.getText();
        notifyCallback();
    }

}

