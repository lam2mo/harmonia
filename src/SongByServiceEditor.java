package harmonia;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class SongByServiceEditor extends JFrame 
    implements ActionListener, DocumentListener, ItemListener, MouseListener {
    
    private Database mainDB;

    private Song selectedSong;

    private JTextField titleFilter;
    private JComboBox keyFilter;
    private JComboBox tagFilter;
    private JButton clearFilterButton;
    private DateEditorPanel startDatePanel;
    private DateEditorPanel endDatePanel;

    private JTable mainTable;
    private SongByServiceTableModel mainModel;
    private TableRowSorter mainSorter;
    private TableCellRenderer centeredRenderer;
    private TableCellRenderer booleanRenderer;
    private TableCellEditor booleanEditor;

    private JButton refreshButton;
    private JButton exitButton;

    private SwingWorker<Database, Object> currentTask;

    public SongByServiceEditor(Database db) {
        super(App.APP_NAME + " - " + db.name);
        mainDB = db;
        buildGUI();
        updateTitle();
        updateTable();
        clearFilters();
        pack();
        setSize(800,600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void buildGUI() {

        titleFilter = new JTextField(10);
        titleFilter.getDocument().addDocumentListener(this);
        keyFilter = new JComboBox(Key.ALL_KEYS);
        keyFilter.addItemListener(this);
        keyFilter.setPreferredSize(new Dimension(100, 5));
        tagFilter = new JComboBox(extractAllTags());
        tagFilter.addItemListener(this);
        tagFilter.setPreferredSize(new Dimension(200, 5));
        clearFilterButton = new JButton("Clear");
        clearFilterButton.addActionListener(this);

        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.LINE_AXIS));
        filterPanel.add(new JLabel("Filter by title (regex):  "));
        filterPanel.add(titleFilter);
        filterPanel.add(Box.createRigidArea(new Dimension(20,5)));
        filterPanel.add(new JLabel("Filter by key:  "));
        filterPanel.add(keyFilter);
        filterPanel.add(Box.createRigidArea(new Dimension(20,5)));
        filterPanel.add(new JLabel("Filter by tag:  "));
        filterPanel.add(tagFilter);
        filterPanel.add(Box.createRigidArea(new Dimension(20,5)));
        filterPanel.add(clearFilterButton);

        startDatePanel = new DateEditorPanel(this, "Start date:  ", findStartDate());
        endDatePanel = new DateEditorPanel(this, "End date:  ", findEndDate());

        JPanel calendarPanel = new JPanel();
        calendarPanel.setLayout(new BoxLayout(calendarPanel, BoxLayout.LINE_AXIS));
        calendarPanel.add(startDatePanel);
        calendarPanel.add(Box.createRigidArea(new Dimension(20,5)));
        calendarPanel.add(endDatePanel);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(0,0,8,0));
        topPanel.add(filterPanel);
        topPanel.add(calendarPanel);

        mainTable = new JTable() {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component returnComp = super.prepareRenderer(renderer, row, column);
                Color baseColor = Color.WHITE;
                Color alternateColor = new Color(235,235,235);
                if (!returnComp.getBackground().equals(getSelectionBackground())){
                    Color bg = (row % 2 == 0 ? baseColor : alternateColor);
                    returnComp.setBackground(bg);
                    bg = null;
                }
                return returnComp;
            }
        };
        mainTable.setIntercellSpacing(new Dimension(0,1));
        mainTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mainTable.addMouseListener(this);
        JScrollPane mainScroll = new JScrollPane(mainTable);

        JPanel tablePanel = new JPanel();
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        tablePanel.setLayout(new BorderLayout());
        tablePanel.add(topPanel, BorderLayout.NORTH);
        tablePanel.add(mainScroll, BorderLayout.CENTER);

        centeredRenderer = new CenteredTableCellRenderer();
        booleanRenderer = new TextBooleanCellRenderer();
        booleanEditor = new TextBooleanCellEditor();

        JPanel mainButtonPanel = new JPanel();
        mainButtonPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(this);
        exitButton = new JButton("Close");
        exitButton.addActionListener(this);
        mainButtonPanel.add(new JLabel("Double-click a cell to add/remove a song in a particular service"));
        mainButtonPanel.add(Box.createRigidArea(new Dimension(20,5)));
        mainButtonPanel.add(refreshButton);
        mainButtonPanel.add(exitButton);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(mainButtonPanel, BorderLayout.SOUTH);
        getContentPane().add(mainPanel);
    }

    public void updateTitle() {
        setTitle(App.APP_NAME + " - \"" + mainDB.name + "\"");
    }

    public void updateTable() {
        int cidx = mainTable.getSelectedRow();
        if (mainSorter != null && cidx >= 0) {
            cidx = mainSorter.convertRowIndexToModel(cidx);
        }
        int i;
        java.util.List<Service> services = new ArrayList<Service>();
        Date start = startDatePanel.getDate();
        Date end = endDatePanel.getDate();
        Date d;
        for (i=0; i<mainDB.allServices.length; i++) {
            d = mainDB.allServices[i].date;
            if (d.compareTo(start) >= 0 && d.compareTo(end) <= 0) {
                services.add(mainDB.allServices[i]);
            }
        }
        mainModel = new SongByServiceTableModel(mainDB, services.toArray(new Service[0]));
        mainSorter = new TableRowSorter<SongByServiceTableModel>(mainModel);
        mainTable.setModel(mainModel);
        mainTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        mainTable.setRowSorter(mainSorter);
        mainTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        mainTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        mainTable.getColumnModel().getColumn(1).setCellRenderer(centeredRenderer);
        mainTable.getColumnModel().getColumn(2).setPreferredWidth(65);
        mainTable.getColumnModel().getColumn(2).setCellRenderer(centeredRenderer);
        mainTable.getColumnModel().getColumn(3).setPreferredWidth(65);
        mainTable.getColumnModel().getColumn(3).setCellRenderer(centeredRenderer);
        for (i=mainModel.getFixedColumnCount(); i<mainTable.getColumnModel().getColumnCount(); i++) {
            mainTable.getColumnModel().getColumn(i).setPreferredWidth(45);
            mainTable.getColumnModel().getColumn(i).setCellRenderer(booleanRenderer);
            mainTable.getColumnModel().getColumn(i).setCellEditor(booleanEditor);
        }
        updateFilters();
        if (cidx >= 0) {
            cidx = mainSorter.convertRowIndexToView(cidx);
            mainTable.setRowSelectionInterval(cidx, cidx);
        }
    }

    public void updateFilters() {
        RowFilter<SongByServiceTableModel, Object> rf;
        java.util.List<RowFilter<SongByServiceTableModel, Object> > filters = new
            ArrayList<RowFilter<SongByServiceTableModel, Object> > ();
        rf = buildTitleFilter();
        if (rf != null) {
            filters.add(rf);
        }
        rf = buildKeyFilter();
        if (rf != null) {
            filters.add(rf);
        }
        rf = buildTagFilter();
        if (rf != null) {
            filters.add(rf);
        }
        if (filters.size() > 0) {
            mainSorter.setRowFilter(RowFilter.andFilter(filters));
        } else {
            mainSorter.setRowFilter(null);
        }
    }

    public RowFilter<SongByServiceTableModel, Object> buildTitleFilter() {
        RowFilter<SongByServiceTableModel, Object> rf = null;
        try {
            rf = RowFilter.regexFilter("(?i)" + titleFilter.getText(), 0);
        } catch (PatternSyntaxException e) { }
        return rf;
    }

    public RowFilter<SongByServiceTableModel, Object> buildKeyFilter() {
        RowFilter<SongByServiceTableModel, Object> rf = null;
        int cidx = keyFilter.getSelectedIndex();
        if (cidx >= 0) {
            rf = new SongByServiceTableModel.KeyFilter((Key)keyFilter.getSelectedItem());
        }
        return rf;
    }
    
    public RowFilter<SongByServiceTableModel, Object> buildTagFilter() {
        RowFilter<SongByServiceTableModel, Object> rf = null;
        int cidx = tagFilter.getSelectedIndex();
        if (cidx >= 0) {
            rf = new SongByServiceTableModel.TagFilter((String)tagFilter.getSelectedItem());
        }
        return rf;
    }

    public void clearFilters() {
        titleFilter.setText("");
        keyFilter.setSelectedIndex(-1);
        tagFilter.setSelectedIndex(-1);
        updateFilters();
    }

    public String[] extractAllTags() {
        SortedSet<String> tags = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        int i, j;
        for (i=0; i<mainDB.allSongs.length; i++) {
            for (j=0; j<mainDB.allSongs[i].tags.length; j++) {
                tags.add(mainDB.allSongs[i].tags[j]);
            }
        }
        return tags.toArray(new String[0]);
    }

    public Date findStartDate() {
        Date d = new Date();
        int i;
        for (i=0; i<mainDB.allServices.length; i++) {
            if (d == null || mainDB.allServices[i].date.before(d)) {
                d = mainDB.allServices[i].date;
            }
        }
        return d;
    }

    public Date findEndDate() {
        Date d = new Date();
        int i;
        for (i=0; i<mainDB.allServices.length; i++) {
            if (d == null || mainDB.allServices[i].date.after(d)) {
                d = mainDB.allServices[i].date;
            }
        }
        return d;
    }

    public void saveDatabase() {
        mainDB.lastModified = new Date();
        currentTask = new DatabaseSaveXMLTask(mainDB, this, this);
        currentTask.execute();
    }

    public void editSong() {
        int cidx = mainTable.getSelectedRow();
        if (cidx < 0) {
            return;
        }
        selectedSong = mainDB.allSongs[mainSorter.convertRowIndexToModel(cidx)];
        if (selectedSong != null) {
            SongEditor editor = new SongEditor(this, mainDB, selectedSong);
            editor.setVisible(true);
            saveDatabase();
            updateTable();
        }
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == clearFilterButton) {
            clearFilters();
        } if (evt.getSource() == refreshButton ||
              evt.getSource() == startDatePanel ||
              evt.getSource() == endDatePanel) {
            updateTable();
        } else if (evt.getSource() == exitButton) {
            dispose();
        }
    }

    public void changedUpdate(DocumentEvent e) {
        updateFilters();
    }

    public void insertUpdate(DocumentEvent e) {
        updateFilters();
    }

    public void removeUpdate(DocumentEvent e) {
        updateFilters();
    }

    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == keyFilter) {
            updateFilters();
        } else if (e.getSource() == tagFilter) {
            updateFilters();
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && e.getSource() == mainTable &&
                mainTable.getSelectedRow() != -1 &&
                mainTable.getSelectedColumn() < mainModel.getFixedColumnCount()) {
            editSong();
        }
    }

    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }

}

