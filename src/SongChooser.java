package harmonia;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.event.*;

public class SongChooser extends JDialog implements ActionListener, ListSelectionListener, MouseListener {

    public static Song showDialog(String prompt, String caption, Song[] songs) {
        return showDialog(null, prompt, caption, songs);
    }

    public static Song showDialog(Window owner, String prompt, String caption, Song[] songs) {
        SongChooser chooser = new SongChooser(owner, prompt, caption, songs);
        chooser.setVisible(true);
        return chooser.getSelectedSong();
    }

    private Song[] songs;
    private JTable songTable;
    private SongTableModel songModel;
    private JLabel promptLabel;
    private JButton okButton;
    private JButton cancelButton;
    private Song selected;

    public SongChooser(Window owner, String prompt, String caption, Song[] songs) {
        super(owner, caption, Dialog.ModalityType.APPLICATION_MODAL);
        this.songs = songs;
        selected = null;
        buildGUI();
        promptLabel.setText(prompt);
        pack();
        setSize(400,500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void buildGUI() {
        songTable = new JTable();
        songModel = new SongTableModel(songs);
        songTable.addMouseListener(this);
        songTable.setModel(songModel);
        songTable.getSelectionModel().addListSelectionListener(this);

        promptLabel = new JLabel();

        JPanel songPanel = new JPanel();
        songPanel.setLayout(new BorderLayout());
        songPanel.add(promptLabel, BorderLayout.NORTH);
        songPanel.add(new JScrollPane(songTable), BorderLayout.CENTER);
        songPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        okButton = new JButton("OK");
        okButton.addActionListener(this);
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(songPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        getContentPane().add(mainPanel);
    }

    public Song getSelectedSong() {
        return selected;
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == okButton) {
            dispose();
        } else if (evt.getSource() == cancelButton) {
            selected = null;
            dispose();
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if (songTable.getSelectedRow() != -1) {
            selected = songModel.getSong(songTable.getSelectedRow());
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && e.getSource() == songTable &&
                songTable.getSelectedRow() != -1) {
            dispose();
        }
    }

    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }

}

