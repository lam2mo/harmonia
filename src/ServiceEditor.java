package harmonia;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class ServiceEditor extends JDialog implements ActionListener, ListSelectionListener {

    private Database mainDB;
    private Service mainService;

    private DateFormat dateFormatter;

    private DateEditorPanel datePanel;
    private StringEditorPanel venuePanel;
    private StringEditorPanel commentPanel;
    private JList songSetList;
    private JButton newSongSetButton;
    private JButton addSongSetButton;
    private JButton removeSongSetButton;
    private JButton moveUpSongSetButton;
    private JButton moveDownSongSetButton;
    private JPanel songSetContainerPanel;
    private SongSetEditorPanel songSetPanel;

    private JButton exitButton;

    public ServiceEditor(Frame owner, Database db, Service service) {
        super(owner, App.APP_NAME, false);
        mainDB = db;
        mainService = service;
        dateFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        buildGUI();
        updateTitle();
        updateSongSetList();
        if (mainService.sets.length > 0) {
            songSetList.setSelectedIndex(0);
            updateSongSetEditorPanel();
        }
        pack();
        setSize(800,800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void buildGUI() {

        datePanel = new DateEditorPanel(this,
                "Date/time:", mainService.date);
        venuePanel = new StringEditorPanel(this, 
                "Location/venue:", mainService.venue);
        commentPanel = new StringEditorPanel(this,
                "Service comments:", mainService.comment, 30,3);
        JPanel serviceSettingPanel = new JPanel();
        serviceSettingPanel.setLayout(new BoxLayout(serviceSettingPanel, BoxLayout.PAGE_AXIS));
        if (App.APP_DEBUG_SHOW_IDS) {
            serviceSettingPanel.add(new JLabel("ID: " + mainService.id));
        }
        serviceSettingPanel.add(datePanel);
        serviceSettingPanel.add(venuePanel);
        serviceSettingPanel.add(commentPanel);
        serviceSettingPanel.add(Box.createVerticalGlue());

        songSetList = new JList();
        songSetList.addListSelectionListener(this);
        JScrollPane songSetScroll = new JScrollPane(songSetList);
        JPanel songSetScrollPanel = new JPanel();
        songSetScrollPanel.setLayout(new BorderLayout());
        songSetScrollPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        songSetScrollPanel.add(songSetScroll, BorderLayout.CENTER);
        
        newSongSetButton = new JButton("Add new set");
        newSongSetButton.addActionListener(this);
        newSongSetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addSongSetButton = new JButton("Add existing set");
        addSongSetButton.addActionListener(this);
        addSongSetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeSongSetButton =  new JButton("Remove set");
        removeSongSetButton.addActionListener(this);
        removeSongSetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        moveUpSongSetButton =  new JButton("Move up");
        moveUpSongSetButton.addActionListener(this);
        moveUpSongSetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        moveDownSongSetButton =  new JButton("Move down");
        moveDownSongSetButton.addActionListener(this);
        moveDownSongSetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel songButtonPanel = new JPanel();
        songButtonPanel.setLayout(new BoxLayout(songButtonPanel, BoxLayout.PAGE_AXIS));
        songButtonPanel.add(newSongSetButton);
        songButtonPanel.add(addSongSetButton);
        songButtonPanel.add(removeSongSetButton);
        songButtonPanel.add(Box.createRigidArea(new Dimension(1,5)));
        songButtonPanel.add(moveUpSongSetButton);
        songButtonPanel.add(moveDownSongSetButton);
        songButtonPanel.add(Box.createVerticalGlue());

        JPanel songSetPanel = new JPanel();
        songSetPanel.setLayout(new BorderLayout());
        songSetPanel.add(new JLabel("Sets:"), BorderLayout.NORTH);
        songSetPanel.add(songSetScrollPanel, BorderLayout.CENTER);
        songSetPanel.add(songButtonPanel, BorderLayout.EAST);
        songSetPanel.setBorder(BorderFactory.createEmptyBorder(15,5,5,10));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(serviceSettingPanel, BorderLayout.WEST);
        topPanel.add(songSetPanel, BorderLayout.CENTER);

        songSetContainerPanel = new JPanel();
        songSetContainerPanel.setLayout(new BorderLayout());

        JPanel mainButtonPanel = new JPanel();
        exitButton = new JButton("Close");
        exitButton.addActionListener(this);
        mainButtonPanel.add(exitButton);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(songSetContainerPanel, BorderLayout.CENTER);
        mainPanel.add(mainButtonPanel, BorderLayout.SOUTH);
        getContentPane().add(mainPanel);
    }

    public void updateTitle() {
        setTitle(App.APP_NAME + " - \"" + dateFormatter.format(mainService.date) + "\"");
    }

    public void updateSongSetList() {
        DefaultListModel model = new DefaultListModel();
        int i;
        for (i=0; i<mainService.sets.length; i++) {
            model.addElement(mainService.sets[i]);
        }
        songSetList.setModel(model);
    }

    public void updateSongSetEditorPanel() {
        songSetContainerPanel.removeAll();
        int cidx = songSetList.getSelectedIndex();
        if (cidx >= 0) {
            SongSet citem = mainService.sets[cidx];
            if (citem.id >= 0) {
                songSetPanel = new SongSetEditorPanel(this, citem, mainDB);
                songSetContainerPanel.add(songSetPanel, BorderLayout.CENTER);
            }
        }
        songSetContainerPanel.revalidate();
        songSetContainerPanel.repaint();
    }

    public void addSongSetHelper(SongSet newSet) {
        SongSet[] newSets = new SongSet[mainService.sets.length+1];
        int idx = 0;
        while (idx < mainService.sets.length) {
            newSets[idx] = mainService.sets[idx];
            idx++;
        }
        newSets[idx] = newSet;
        mainService.sets = newSets;
        updateSongSetList();
        songSetList.setSelectedIndex(mainService.sets.length-1);
        updateSongSetEditorPanel();
    }

    public void addNewSongSet() {
        String name = JOptionPane.showInputDialog(this,
                "Enter set name:", dateFormatter.format(mainService.date));
        if (name != null) {
            addSongSetHelper(mainDB.addNewSongSet(name));
        }
    }

    public void addExistingSongSet() {
        SongSet set = SongSetChooser.showDialog(this, mainDB);
        if (set != null) {
            addSongSetHelper(set);
        }
    }

    public void removeSongSet() {
        int cidx = songSetList.getSelectedIndex();
        if (cidx < 0) {
            return;
        }
        SongSet citem = mainService.sets[cidx];
        int response = JOptionPane.showConfirmDialog(this,
                "Are you sure you wish to remove set " + 
                "\"" + citem.name + "\" from the service?", "Remove set",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response == JOptionPane.YES_OPTION) {
            SongSet[] newSets = new SongSet[mainService.sets.length-1];
            int idx = 0;
            while (idx < cidx) {
                newSets[idx] = mainService.sets[idx];
                idx++;
            }
            while (idx < newSets.length) {
                newSets[idx] = mainService.sets[idx+1];
                idx++;
            }
            mainService.sets = newSets;
            updateSongSetList();
        }
    }

    public void moveUpSongSet() {
        int cidx = songSetList.getSelectedIndex();
        if (cidx > 0) {
            SongSet tmp = mainService.sets[cidx];
            mainService.sets[cidx] = mainService.sets[cidx-1];
            mainService.sets[cidx-1] = tmp;
            updateSongSetList();
            songSetList.setSelectedIndex(cidx-1);
        }
    }
    
    public void moveDownSongSet() {
        int cidx = songSetList.getSelectedIndex();
        if (cidx >= 0 && cidx < mainService.sets.length-1) {
            SongSet tmp = mainService.sets[cidx];
            mainService.sets[cidx] = mainService.sets[cidx+1];
            mainService.sets[cidx+1] = tmp;
            updateSongSetList();
            songSetList.setSelectedIndex(cidx+1);
        }
    }

    public Service getService() {
        return mainService;
    }

    public void valueChanged(ListSelectionEvent evt) {
        updateSongSetEditorPanel();
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == datePanel) {
            mainService.date = datePanel.getDate();
            updateTitle();
        } else if (evt.getSource() == venuePanel) {
            mainService.venue = venuePanel.getText();
        } else if (evt.getSource() == commentPanel) {
            mainService.comment = commentPanel.getText();
        } else if (evt.getSource() == newSongSetButton) {
            addNewSongSet();
        } else if (evt.getSource() == addSongSetButton) {
            addExistingSongSet();
        } else if (evt.getSource() == removeSongSetButton) {
            removeSongSet();
        } else if (evt.getSource() == moveUpSongSetButton) {
            moveUpSongSet();
        } else if (evt.getSource() == moveDownSongSetButton) {
            moveDownSongSet();
        } else if (evt.getSource() == exitButton) {
            dispose();
        }
    }

}

