package harmonia;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;

public class SongSetChooser extends JDialog implements ActionListener, MouseListener {

    public static SongSet showDialog(Database db) {
        return showDialog(null, db);
    }

    public static SongSet showDialog(Dialog owner, Database db) {
        SongSet ret = null;
        SongSetChooser chooser = new SongSetChooser(owner, db);
        chooser.setVisible(true);
        ret = chooser.getSelectedSongSet();
        return ret;
    }

    private Database mainDB;

    private JList songSetList;
    private JButton okButton;
    private JButton cancelButton;
    private SongSet selected;

    public SongSetChooser(Dialog owner, Database mainDB) {
        super(owner, "Choose a set", true);
        this.mainDB = mainDB;
        this.selected = null;
        buildGUI();
        pack();
        setSize(400,300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        refreshList();
    }

    private void buildGUI() {
        songSetList = new JList();
        songSetList.addMouseListener(this);

        JPanel songSetPanel = new JPanel();
        songSetPanel.setLayout(new BorderLayout());
        songSetPanel.add(new JLabel("Sets:"), BorderLayout.NORTH);
        songSetPanel.add(new JScrollPane(songSetList), BorderLayout.CENTER);
        songSetPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        okButton = new JButton("OK");
        okButton.addActionListener(this);
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(songSetPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        getContentPane().add(mainPanel);
    }

    public void refreshList() {
        DefaultListModel model = new DefaultListModel();
        for (int i=0; i<mainDB.allSongSets.length; i++) {
            model.addElement(mainDB.allSongSets[i]);
        }
        songSetList.setModel(model);
    }

    public SongSet getSelectedSongSet() {
        return selected;
    }

    public void selectSongSet() {
        selected = (SongSet)songSetList.getSelectedValue();
        if (selected != null) {
            okButton.setEnabled(false);
            cancelButton.setEnabled(false);
        }
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == okButton) {
            selectSongSet();
            dispose();
        } else if (evt.getSource() == cancelButton) {
            selected = null;
            dispose();
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && e.getSource() == songSetList &&
                songSetList.getSelectedIndex() != -1) {
            selectSongSet();
            dispose();
        }
    }

    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }

}

