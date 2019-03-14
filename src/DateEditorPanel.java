package harmonia;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import com.michaelbaranov.microba.common.*;
import com.michaelbaranov.microba.calendar.*;

public class DateEditorPanel extends JPanel implements ActionListener, CommitListener, DocumentListener {

    public static final String ACTION_UPDATE = "date-updated";

    private ActionListener callback;
    private String caption;
    private Date currentDate;
    private DateFormat timeFormatter;

    private DatePicker datePicker;
    private JFormattedTextField timeField;

    public DateEditorPanel(ActionListener callback, String caption) {
        this(callback, caption, null);
    }

    public DateEditorPanel(ActionListener callback, String caption, 
            Date date) {
        this.callback = callback;
        this.caption = caption;
        this.currentDate = date;
        this.timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT);
        this.timeFormatter.setLenient(true);

        buildGUI();
    }

    public void buildGUI() {
        datePicker = new DatePicker();
        datePicker.setKeepTime(false);
        datePicker.setShowNoneButton(false);
        datePicker.setPreferredSize(new Dimension(160, 20));
        datePicker.addActionListener(this);
        datePicker.addCommitListener(this);

        timeField = new JFormattedTextField(DateFormat.getTimeInstance(DateFormat.SHORT));
        timeField.setColumns(6);
        timeField.addActionListener(this);
        timeField.getDocument().addDocumentListener(this);

        if (currentDate != null) {
            try {
                datePicker.setDate(currentDate);
            } catch (PropertyVetoException ex) { }
        }
        if (currentDate != null) {
            timeField.setText(timeFormatter.format(currentDate));
        }

        add(new JLabel(caption));
        add(datePicker);
        add(timeField);
    }

    public void setDate(Date date) {
        this.currentDate = date;
        try {
            datePicker.setDate(date);
        } catch (PropertyVetoException ex) { }
        timeField.setText(timeFormatter.format(date));
    }

    public Date getDate() {
        updateSelectedDate();
        return currentDate;
    }

    public void notifyCallback() {
        if (callback != null) {
            callback.actionPerformed(new ActionEvent(this,
                    ActionEvent.ACTION_PERFORMED, ACTION_UPDATE));
        }
    }

    public void updateSelectedDate() {
        Calendar fullCal = Calendar.getInstance();
        fullCal.setTime(datePicker.getDate());
        Calendar timeCal = Calendar.getInstance();
        Date timeDate = null;
        try {
            timeDate = timeFormatter.parse(timeField.getText());
        } catch (ParseException ex) {
            timeDate = null;
        }
        if (timeDate != null) {
            timeCal.setTime(timeDate);
            fullCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
            fullCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
            fullCal.set(Calendar.SECOND, 0);
        }
        currentDate = fullCal.getTime();
    }

    public void actionPerformed(ActionEvent e) {
        updateSelectedDate();
        notifyCallback();
    }

    public void commit(CommitEvent e) {
        updateSelectedDate();
        notifyCallback();
    }

    public void changedUpdate(DocumentEvent e) {
        updateSelectedDate();
        notifyCallback();
    }

    public void insertUpdate(DocumentEvent e) {
        updateSelectedDate();
        notifyCallback();
    }

    public void removeUpdate(DocumentEvent e) {
        updateSelectedDate();
        notifyCallback();
    }
}

