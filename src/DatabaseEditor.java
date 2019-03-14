package harmonia;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;

public class DatabaseEditor extends JFrame implements ActionListener, MouseListener {

    private Database mainDB;

    private StringEditorPanel namePanel;

    private JLabel songLabel;
    private JList simpleSongList;
    private JButton addSongButton;
    private JButton editSongButton;
    private JButton removeSongButton;

    private JLabel setLabel;
    private JList simpleSetList;
    private JButton addSetButton;
    private JButton editSetButton;
    private JButton removeSetButton;
    
    private JLabel serviceLabel;
    private JList simpleServiceList;
    private JButton addServiceButton;
    private JButton editServiceButton;
    private JButton removeServiceButton;

    private JButton importButton;
    private JButton songByServiceButton;
    private JButton exitButton;

    private Song selectedSong;
    private SongSet selectedSet;
    private Service selectedService;

    private DateFormat dateFormatter;

    private SwingWorker<Database, Object> currentTask;

    public DatabaseEditor(Database db) {
        super();
        mainDB = db;
        selectedSong = null;
        selectedSet = null;
        selectedService = null;
        dateFormatter = DateFormat.getDateTimeInstance();
        dateFormatter.setLenient(true);
        currentTask = null;
        buildGUI();
        pack();
        setSize(500,600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        updateTitle();
        refreshSimpleSongList();
        refreshSimpleSetList();
        refreshSimpleServiceList();
        simpleSongList.requestFocus();
    }

    private void buildGUI() {

        namePanel = new StringEditorPanel(this, "Database name:", mainDB.name, 30, 1);

        songLabel = new JLabel("All songs: (" + mainDB.allSongs.length + " total)");
        simpleSongList = new JList();
        simpleSongList.addMouseListener(this);

        addSongButton = new JButton("Add");
        addSongButton.addActionListener(this);
        addSongButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        editSongButton = new JButton("Edit");
        editSongButton.addActionListener(this);
        editSongButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeSongButton = new JButton("Remove");
        removeSongButton.addActionListener(this);
        removeSongButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel simpleSongButtonPanel = new JPanel();
        simpleSongButtonPanel.setLayout(new BoxLayout(simpleSongButtonPanel, BoxLayout.PAGE_AXIS));
        simpleSongButtonPanel.add(addSongButton);
        simpleSongButtonPanel.add(editSongButton);
        simpleSongButtonPanel.add(removeSongButton);
        simpleSongButtonPanel.add(Box.createVerticalGlue());

        JPanel simpleSongListPanel = new JPanel();
        simpleSongListPanel.setLayout(new BorderLayout());
        simpleSongListPanel.add(songLabel, BorderLayout.NORTH);
        simpleSongListPanel.add(new JScrollPane(simpleSongList), BorderLayout.CENTER);
        simpleSongListPanel.add(simpleSongButtonPanel, BorderLayout.EAST);
        simpleSongListPanel.setBorder(BorderFactory.createEmptyBorder(20,20,10,20));

        setLabel = new JLabel("All sets: (" + mainDB.allSongSets.length + " total)");
        simpleSetList = new JList();
        simpleSetList.addMouseListener(this);

        addSetButton = new JButton("Add");
        addSetButton.addActionListener(this);
        addSetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        editSetButton = new JButton("Edit");
        editSetButton.addActionListener(this);
        editSetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeSetButton = new JButton("Remove");
        removeSetButton.addActionListener(this);
        removeSetButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel simpleSetButtonPanel = new JPanel();
        simpleSetButtonPanel.setLayout(new BoxLayout(simpleSetButtonPanel, BoxLayout.PAGE_AXIS));
        simpleSetButtonPanel.add(addSetButton);
        simpleSetButtonPanel.add(editSetButton);
        simpleSetButtonPanel.add(removeSetButton);
        simpleSetButtonPanel.add(Box.createVerticalGlue());

        JPanel simpleSetListPanel = new JPanel();
        simpleSetListPanel.setLayout(new BorderLayout());
        simpleSetListPanel.add(setLabel, BorderLayout.NORTH);
        simpleSetListPanel.add(new JScrollPane(simpleSetList), BorderLayout.CENTER);
        simpleSetListPanel.add(simpleSetButtonPanel, BorderLayout.EAST);
        simpleSetListPanel.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));

        serviceLabel = new JLabel("All services: (" + mainDB.allServices.length + " total)");
        simpleServiceList = new JList();
        simpleServiceList.addMouseListener(this);

        addServiceButton = new JButton("Add");
        addServiceButton.addActionListener(this);
        addServiceButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        editServiceButton = new JButton("Edit");
        editServiceButton.addActionListener(this);
        editServiceButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeServiceButton = new JButton("Remove");
        removeServiceButton.addActionListener(this);
        removeServiceButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel simpleServiceButtonPanel = new JPanel();
        simpleServiceButtonPanel.setLayout(new BoxLayout(simpleServiceButtonPanel, BoxLayout.PAGE_AXIS));
        simpleServiceButtonPanel.add(addServiceButton);
        simpleServiceButtonPanel.add(editServiceButton);
        simpleServiceButtonPanel.add(removeServiceButton);
        simpleServiceButtonPanel.add(Box.createVerticalGlue());

        JPanel simpleServiceListPanel = new JPanel();
        simpleServiceListPanel.setLayout(new BorderLayout());
        simpleServiceListPanel.add(serviceLabel, BorderLayout.NORTH);
        simpleServiceListPanel.add(new JScrollPane(simpleServiceList), BorderLayout.CENTER);
        simpleServiceListPanel.add(simpleServiceButtonPanel, BorderLayout.EAST);
        simpleServiceListPanel.setBorder(BorderFactory.createEmptyBorder(10,20,20,20));

        importButton = new JButton("Import Songs");
        importButton.addActionListener(this);
        songByServiceButton = new JButton("Table View");
        songByServiceButton.addActionListener(this);
        exitButton = new JButton("Exit");
        exitButton.addActionListener(this);

        JPanel mainContentsPanel = new JPanel();
        mainContentsPanel.setLayout(new BoxLayout(mainContentsPanel, BoxLayout.PAGE_AXIS));
        mainContentsPanel.add(namePanel);
        mainContentsPanel.add(simpleSongListPanel);
        //mainContentsPanel.add(simpleSetListPanel);
        mainContentsPanel.add(simpleServiceListPanel);

        JPanel mainButtonPanel = new JPanel();
        mainButtonPanel.add(importButton);
        mainButtonPanel.add(songByServiceButton);
        mainButtonPanel.add(exitButton);
        mainButtonPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(mainContentsPanel, BorderLayout.CENTER);
        mainPanel.add(mainButtonPanel, BorderLayout.SOUTH);
        getContentPane().add(mainPanel);
    }

    public void updateTitle() {
        setTitle(App.APP_NAME + " - " + mainDB.name);
    }

    public void refreshSimpleSongList() {
        DefaultListModel model = new DefaultListModel();
        SongStub[] stubs = mainDB.getAllSongs();
        Arrays.sort(stubs);
        int i;
        for (i=0; i<stubs.length; i++) {
            model.addElement(stubs[i]);
        }
        simpleSongList.setModel(model);
        songLabel.setText("All songs: (" + stubs.length + " total)");
    }

    public void refreshSimpleSetList() {
        DefaultListModel model = new DefaultListModel();
        int i;
        for (i=0; i<mainDB.allSongSets.length; i++) {
            model.addElement(mainDB.allSongSets[i]);
        }
        simpleSetList.setModel(model);
        setLabel.setText("All sets: (" + mainDB.allSongSets.length + " total)");
    }

    public void refreshSimpleServiceList() {
        DefaultListModel model = new DefaultListModel();
        int i;
        for (i=0; i<mainDB.allServices.length; i++) {
            model.addElement(mainDB.allServices[i]);
        }
        simpleServiceList.setModel(model);
        serviceLabel.setText("All services: (" + mainDB.allServices.length + " total)");
    }

    public void addSong() {
        String title = JOptionPane.showInputDialog(this,
                "Enter song title:");
        if (title != null) {
            Song newSong = mainDB.addNewSong(title);
            SongEditor editor = new SongEditor(this, mainDB, newSong);
            editor.setVisible(true);
            saveDatabase();
            refreshAll();
        }
    }

    public void editSong() {
        selectedSong = ((SongStub)simpleSongList.getSelectedValue()).song;
        if (selectedSong != null) {
            SongEditor editor = new SongEditor(this, mainDB, selectedSong);
            editor.setVisible(true);
            saveDatabase();
            refreshAll();
        }
    }

    public void removeSong() {
        int idx = simpleSongList.getSelectedIndex();
        if (idx >= 0) {
            Song song = ((SongStub)simpleSongList.getSelectedValue()).song;
            int response = JOptionPane.showConfirmDialog(this,
                    "Are you sure you wish to remove song " + 
                    "\"" + song.title + "\"?", "Remove song",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                mainDB.removeSong(song);
                saveDatabase();
                refreshSimpleSongList();
            }
        }
    }

    public void addSet() {
        String name = JOptionPane.showInputDialog(this,
                "Enter set name:");
        if (name != null) {
            SongSet newSet = mainDB.addNewSongSet(name);
            SongSetEditor editor = new SongSetEditor(this, mainDB, newSet);
            editor.setVisible(true);
            saveDatabase();
            refreshAll();
        }
    }

    public void editSet() {
        selectedSet = (SongSet)simpleSetList.getSelectedValue();
        if (selectedSet != null) {
            SongSetEditor editor = new SongSetEditor(this, mainDB, selectedSet);
            editor.setVisible(true);
            Arrays.sort(mainDB.allSongSets);
            saveDatabase();
            refreshAll();
        }
    }

    public void removeSet() {
        int idx = simpleSetList.getSelectedIndex();
        if (idx >= 0) {
            int response = JOptionPane.showConfirmDialog(this,
                    "Are you sure you wish to remove set " + 
                    "\"" + mainDB.allSongSets[idx].name + "\"?", "Remove set",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                SongSet set = (SongSet)simpleSetList.getSelectedValue();
                mainDB.removeSongSet(set);
                saveDatabase();
                refreshSimpleSetList();
            }
        }
    }

    public void addService() {
        Date date = null;
        DateChooser dateDialog = new DateChooser(this, "Enter service date and time:", "New service");
        dateDialog.setVisible(true);
        date = dateDialog.getDate();
        if (date != null) {
            Service newService = mainDB.addNewService(date);
            ServiceEditor editor = new ServiceEditor(this, mainDB, newService);
            editor.setVisible(true);
            saveDatabase();
            refreshAll();
        }
    }

    public void editService() {
        selectedService = (Service)simpleServiceList.getSelectedValue();
        if (selectedService != null) {
            ServiceEditor editor = new ServiceEditor(this, mainDB, selectedService);
            editor.setVisible(true);
            saveDatabase();
            refreshAll();
        }
    }

    public void removeService() {
        int idx = simpleServiceList.getSelectedIndex();
        if (idx >= 0) {
            Service song = (Service)simpleServiceList.getSelectedValue();
            int response = JOptionPane.showConfirmDialog(this,
                    "Are you sure you wish to remove the service on " + 
                    "\"" + dateFormatter.format(song.date) + "\"?", "Remove service",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                mainDB.removeService(song);
                saveDatabase();
                refreshSimpleServiceList();
            }
        }
    }

    public void doImport() {
        Importer imp = (Importer)JOptionPane.showInputDialog(this,
                "Choose an importer:", "Import", JOptionPane.PLAIN_MESSAGE,
                null, App.allImporters, App.allImporters[0]);
        if (imp != null) {
            currentTask = new ImportTask(mainDB, imp, this, this);
            disableButtons();
            currentTask.execute();
        }
    }

    public void doSongByService() {
        SongByServiceEditor sbsEditor = new SongByServiceEditor(mainDB);
        sbsEditor.setVisible(true);
    }

    public void refreshAll() {
        refreshSimpleSongList();
        refreshSimpleSetList();
        refreshSimpleServiceList();
    }

    public void enableButtons() {
        addSongButton.setEnabled(true);
        editSongButton.setEnabled(true);
        removeSongButton.setEnabled(true);
        addSetButton.setEnabled(true);
        editSetButton.setEnabled(true);
        removeSetButton.setEnabled(true);
        addServiceButton.setEnabled(true);
        editServiceButton.setEnabled(true);
        removeServiceButton.setEnabled(true);
        exitButton.setEnabled(true);
    }

    public void disableButtons() {
        addSongButton.setEnabled(false);
        editSongButton.setEnabled(false);
        removeSongButton.setEnabled(false);
        addSetButton.setEnabled(false);
        editSetButton.setEnabled(false);
        removeSetButton.setEnabled(false);
        addServiceButton.setEnabled(true);
        editServiceButton.setEnabled(true);
        removeServiceButton.setEnabled(true);
        exitButton.setEnabled(false);
    }

    public void saveDatabase() {
        mainDB.lastModified = new Date();
        currentTask = new DatabaseSaveXMLTask(mainDB, this, this);
        disableButtons();
        currentTask.execute();
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == namePanel) {
            mainDB.name = namePanel.getText();
            saveDatabase();
            updateTitle();
        } else if (evt.getSource() == addSongButton) {
            addSong();
        } else if (evt.getSource() == editSongButton) {
            editSong();
        } else if (evt.getSource() == removeSongButton) {
            removeSong();
        } else if (evt.getSource() == addSetButton) {
            addSet();
        } else if (evt.getSource() == editSetButton) {
            editSet();
        } else if (evt.getSource() == removeSetButton) {
            removeSet();
        } else if (evt.getSource() == addServiceButton) {
            addService();
        } else if (evt.getSource() == editServiceButton) {
            editService();
        } else if (evt.getSource() == removeServiceButton) {
            removeService();
        } else if (evt.getSource() == currentTask) {
            if (evt.getActionCommand().equals(DatabaseOpenTask.DONE_SUCCESS)) {
                dispose();
            } else if (evt.getActionCommand().equals(DatabaseOpenTask.DONE_FAILURE)) {
                enableButtons();
                currentTask = null;
            } else if (evt.getActionCommand().equals(DatabaseSaveXMLTask.DONE_SUCCESS) ||
                       evt.getActionCommand().equals(DatabaseSaveXMLTask.DONE_FAILURE)) {
                enableButtons();
                currentTask = null;
            } else if (evt.getActionCommand().equals(ImportTask.DONE_SUCCESS) ||
                       evt.getActionCommand().equals(ImportTask.DONE_FAILURE)) {
                enableButtons();
                currentTask = null;
                saveDatabase();
                refreshAll();
            }
        } else if (evt.getSource() == importButton) {
            doImport();
        } else if (evt.getSource() == songByServiceButton) {
            doSongByService();
        } else if (evt.getSource() == exitButton) {
            dispose();
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && e.getSource() == simpleSongList &&
                simpleSongList.getSelectedIndex() != -1) {
            editSong();
        } else if (e.getClickCount() == 2 && e.getSource() == simpleSetList &&
                simpleSetList.getSelectedIndex() != -1) {
            editSet();
        } else if (e.getClickCount() == 2 && e.getSource() == simpleServiceList &&
                simpleServiceList.getSelectedIndex() != -1) {
            editService();
        }
    }

    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }

}

