package harmonia;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.border.*;

public class SongSetEditor extends JDialog implements ActionListener {

    private SongSetEditorPanel editPanel;
    private JButton exitButton;

    public SongSetEditor(Frame owner, Database db, SongSet set) {
        super(owner, App.APP_NAME, true);
        editPanel = new SongSetEditorPanel(this, set, db);
        buildGUI();
        updateTitle();
        pack();
        setSize(600,750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void buildGUI() {

        JPanel mainButtonPanel = new JPanel();
        exitButton = new JButton("Close");
        exitButton.addActionListener(this);
        mainButtonPanel.add(exitButton);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(editPanel, BorderLayout.CENTER);
        mainPanel.add(mainButtonPanel, BorderLayout.SOUTH);
        getContentPane().add(mainPanel);
    }

    public void updateTitle() {
        setTitle(App.APP_NAME + " - \"" + editPanel.getSongSet().name + "\"");
    }

    public SongSet getSongSet() {
        return editPanel.getSongSet();
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == editPanel) {
            updateTitle();
        } else if (evt.getSource() == exitButton) {
            dispose();
        }
    }

}

