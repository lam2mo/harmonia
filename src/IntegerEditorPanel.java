package harmonia;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class IntegerEditorPanel extends JPanel implements ChangeListener, DocumentListener {

    public static final String ACTION_UPDATE = "integer-updated";

    private ActionListener callback;
    private String caption;
    private int columns;
    private int value;

    private JLabel captionText;
    private JFormattedTextField textField;
    private JSpinner spinner;
    private SpinnerNumberModel spinnerModel;

    public IntegerEditorPanel(ActionListener callback, String caption, 
            int value) {
        this(callback, caption, value, 20);
    }

    public IntegerEditorPanel(ActionListener callback, String caption, 
            int value, int columns) {
        this.callback = callback;
        this.caption = caption;
        this.value = value;
        this.columns = columns;

        buildGUI();
    }

    public void buildGUI() {
        captionText = new JLabel();
        if (caption != null) {
            captionText.setText(caption);
            add(captionText);
        }
        NumberFormat formatter = NumberFormat.getIntegerInstance();
        formatter.setGroupingUsed(false);
        textField = new JFormattedTextField(formatter);
        textField.setValue(value);
        textField.setColumns(columns);
        textField.getDocument().addDocumentListener(this);
        spinnerModel = new SpinnerNumberModel(value, 0, 9999, 1);
        spinner = new JSpinner(spinnerModel);
        spinner.addChangeListener(this);
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "###0"));
        //((JFormattedTextField)(spinner.getEditor())).setFormatter(formatter);
        //spinner.setEditor(textField);
        //add(textField);
        add(spinner);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int newValue) {
        value = newValue;
        textField.getDocument().removeDocumentListener(this);
        textField.setValue(value);
        textField.getDocument().addDocumentListener(this);
        spinner.setValue(newValue);
    }

    public void setEnabled(boolean enabled) {
        captionText.setEnabled(enabled);
        textField.setEnabled(enabled);
        spinner.setEnabled(enabled);
    }

    public void notifyCallback() {
        if (callback != null) {
            callback.actionPerformed(new ActionEvent(this,
                    ActionEvent.ACTION_PERFORMED, ACTION_UPDATE));
        }
    }

    public void stateChanged(ChangeEvent e) {
        value = spinnerModel.getNumber().intValue();
        notifyCallback();
    }

    public void changedUpdate(DocumentEvent e) {
        try {
            spinner.commitEdit();
        } catch (ParseException ex) { }
        //try {
            //value = NumberFormat.getIntegerInstance().parse(textField.getText()).intValue();
        //} catch (ParseException ex) { }
        //notifyCallback();
    }

    public void insertUpdate(DocumentEvent e) {
        try {
            spinner.commitEdit();
        } catch (ParseException ex) { }
        //try {
            //value = NumberFormat.getIntegerInstance().parse(textField.getText()).intValue();
        //} catch (ParseException ex) { }
        //notifyCallback();
    }

    public void removeUpdate(DocumentEvent e) {
        try {
            spinner.commitEdit();
        } catch (ParseException ex) { }
        //try {
            //value = NumberFormat.getIntegerInstance().parse(textField.getText()).intValue();
        //} catch (ParseException ex) { }
        //notifyCallback();
    }

}

