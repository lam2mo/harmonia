package harmonia;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import com.michaelbaranov.microba.calendar.*;

public class DateChooser extends JDialog implements ActionListener {

    DateEditorPanel datePanel;

    private JButton okButton;
    private JButton cancelButton;

    private String label;
    private Date currentDate;

    public DateChooser(Frame owner, String label, String title) {
        super(owner, title, true);
        this.label = label;
        this.currentDate = null;
        buildGUI();
        pack();
        setSize(320, 140);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public void buildGUI() {

        Calendar defaultDate = Calendar.getInstance();
        defaultDate.set(Calendar.HOUR_OF_DAY, 11);
        defaultDate.set(Calendar.MINUTE, 0);
        defaultDate.set(Calendar.SECOND, 0);
        defaultDate.set(Calendar.MILLISECOND, 0);
        datePanel = new DateEditorPanel(this, label, defaultDate.getTime());
        datePanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

        JPanel buttonPanel = new JPanel();
        okButton = new JButton("OK");
        okButton.addActionListener(this);
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(datePanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        getContentPane().add(mainPanel);
    }

    public Date getDate() {
        return currentDate;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == okButton) {
            currentDate = datePanel.getDate();
            dispose();
        } else if (e.getSource() == cancelButton) {
            currentDate = null;
            dispose();
        }
    }

}

