package harmonia;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.event.*;

public class DatabaseChooser extends JDialog 
    implements ActionListener, ListSelectionListener, MouseListener {

    private class DatabaseFilenameFilter implements FilenameFilter {
        private static final String POSTFIX = "." + App.APP_DB_EXT;
        public boolean accept(File dir, String name) {
            return name.endsWith(POSTFIX);
        }
    }

    public static Database showDialog() {
        return showDialog(null);
    }

    public static Database showDialog(Frame owner) {
        Database ret = null;
        DatabaseChooser chooser = new DatabaseChooser(owner);
        chooser.setVisible(true);
        ret = chooser.getSelectedDB();
        return ret;
    }

    private JList databaseList;
    private JLabel lastModifiedLabel;
    private JButton newButton;
    private JButton openButton;
    private JButton cancelButton;
    private Database selected;
    private SwingWorker<Database, Object> currentTask;

    public DatabaseChooser(Frame owner) {
        super(owner, App.APP_NAME + " " + App.APP_VERSION, true);
        selected = null;
        currentTask = null;
        buildGUI();
        pack();
        setSize(400,300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        refreshList();
    }

    private void buildGUI() {
        databaseList = new JList();
        databaseList.addListSelectionListener(this);
        databaseList.addMouseListener(this);

        lastModifiedLabel = new JLabel("<html> <br> </html>");

        JPanel installedPanel = new JPanel();
        installedPanel.setLayout(new BorderLayout());
        installedPanel.add(new JLabel("Installed databases:"), BorderLayout.NORTH);
        installedPanel.add(new JScrollPane(databaseList), BorderLayout.CENTER);
        installedPanel.add(lastModifiedLabel, BorderLayout.SOUTH);
        installedPanel.setBorder(BorderFactory.createEmptyBorder(20,20,10,20));

        newButton = new JButton("New");
        newButton.addActionListener(this);
        openButton = new JButton("Open");
        openButton.addActionListener(this);
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(newButton);
        buttonPanel.add(openButton);
        buttonPanel.add(cancelButton);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(installedPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        getContentPane().add(mainPanel);
    }

    public void refreshList() {
        File dataPath = new File(Util.getDataPath());
        File[] dbs = dataPath.listFiles(new DatabaseFilenameFilter());
        int i;
        java.util.List<Database> list = new ArrayList<Database>();
        for (i=0; i<dbs.length; i++) {
            list.add(new Database(dbs[i]));
        }
        Collections.sort(list);
        DefaultListModel model = new DefaultListModel();
        for (i=0; i<list.size(); i++) {
            model.addElement(list.get(i));
        }
        databaseList.setModel(model);
    }

    public Database getSelectedDB() {
        return selected;
    }

    public void createNewDatabase() {
        String name = JOptionPane.showInputDialog(this,
                "Enter database name:");
        if (name != null) {
            String fn = Util.sanitizeFilename(name);
            File dbPath = new File(Util.getDataPath() + File.separator + 
                    fn + "." + App.APP_DB_EXT);
            if (dbPath.exists()) {
                JOptionPane.showMessageDialog(null, "Database Creation Error",
                        "There is already a database file with the same filename.", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            File attachmentPath = new File(Util.getDataPath() + File.separator +
                    fn + App.APP_ATTACH_EXT);
            if (attachmentPath.exists()) {
                JOptionPane.showMessageDialog(null, "Database Creation Error",
                        "There is already a database attachment folder with the same filename.", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                dbPath.createNewFile();
                attachmentPath.mkdirs();
                Database newDB = new Database(dbPath, attachmentPath);
                newDB.name = name;
                currentTask = new DatabaseSaveXMLTask(newDB, this, this);
                currentTask.execute();
                newButton.setEnabled(false);
                openButton.setEnabled(false);
                selected = newDB;
                dispose();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Database Creation Error",
                        "Cannot create database.", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void openSelectedDatabase() {
        selected = (Database)databaseList.getSelectedValue();
        if (selected != null && currentTask == null) {
            currentTask = new DatabaseOpenXMLTask(selected, this, this);
            currentTask.execute();
            newButton.setEnabled(false);
            openButton.setEnabled(false);
        }
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == newButton) {
            createNewDatabase();
        } else if (evt.getSource() == openButton) {
            openSelectedDatabase();
        } else if (evt.getSource() == currentTask) {
            if (evt.getActionCommand().equals(DatabaseOpenXMLTask.DONE_SUCCESS)) {
                dispose();
            } else if (evt.getActionCommand().equals(DatabaseOpenXMLTask.DONE_FAILURE)) {
                System.out.println("failure!");
                newButton.setEnabled(true);
                openButton.setEnabled(true);
                currentTask = null;
                selected = null;
            } else if (evt.getActionCommand().equals(DatabaseSaveXMLTask.DONE_SUCCESS) ||
                       evt.getActionCommand().equals(DatabaseSaveXMLTask.DONE_FAILURE)) {
                newButton.setEnabled(true);
                openButton.setEnabled(true);
                currentTask = null;
                refreshList();
            }
        } else if (evt.getSource() == cancelButton) {
            selected = null;
            if (currentTask != null) {
                currentTask.cancel(true);
            }
            dispose();
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if (databaseList.getSelectedIndex() != -1) {
            selected = (Database)databaseList.getSelectedValue();
            lastModifiedLabel.setText("<html>" + selected.allSongs.length + " song(s), " +
                    selected.allSongSets.length + " set(s), " +
                    selected.allServices.length + " service(s)" +
                    "<br>Last Modified: " + selected.lastModified.toString() + "</html>");
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && e.getSource() == databaseList &&
                databaseList.getSelectedIndex() != -1) {
            openSelectedDatabase();
        }
    }

    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }

}

