package harmonia;

import java.awt.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class SongByServiceTableModel extends AbstractTableModel {

    public static class KeyFilter extends RowFilter<SongByServiceTableModel, Object> {
        private Key mainKey;
        public KeyFilter(Key key) {
            mainKey = key;
        }
        public boolean include(RowFilter.Entry<? extends SongByServiceTableModel, ? extends Object> entry) {
            Key k = ((Song)entry.getValue(0)).key;
            return k.compareTo(mainKey) == 0;
        }
    }

    public static class TagFilter extends RowFilter<SongByServiceTableModel, Object> {
        private String mainTag;
        public TagFilter(String tag) {
            mainTag = tag;
        }
        public boolean include(RowFilter.Entry<? extends SongByServiceTableModel, ? extends Object> entry) {
            Song s = (Song)entry.getValue(0);
            int i;
            for (i=0; i<s.tags.length; i++) {
                if (s.tags[i].equalsIgnoreCase(mainTag)) {
                    return true;
                }
            }
            return false;
        }
    }

    private Database mainDB;
    private Service[] allServices;

    private int fixedColumns;
    private DateFormat dateFormatter;

    public SongByServiceTableModel(Database db, Service[] services) {
        mainDB = db;
        allServices = services;
        fixedColumns = 4;
        dateFormatter = new SimpleDateFormat("M/d");
    }

    public int getRowCount() {
        return mainDB.allSongs.length;
    }

    public int getFixedColumnCount() {
        return fixedColumns;
    }

    public int getColumnCount() {
        return allServices.length+fixedColumns;
    }

    public String getColumnName(int column) {
        String ret = "";
        if (column == 0) {
            ret = "Song Title";
        } else if (column == 1) {
            ret = "Key";
        } else if (column == 2) {
            ret = "Tempo";
        } else if (column == 3) {
            ret = "Repeats";
        } else {
            ret = dateFormatter.format(allServices[column-fixedColumns].date);
        }
        return ret;
    }
    
    public Class<?> getColumnClass(int column) {
        Class ret = Object.class;
        if (column == 0) {
            ret = Song.class;
        } else if (column == 1) {
            ret = Key.class;
        } else if (column == 2) {
            ret = Integer.class;
        } else if (column == 3) {
            ret = Integer.class;
        } else {
            ret = Boolean.class;
        }
        return ret;
    }

    public int countRowRepeats(int row) {
        Song song = mainDB.allSongs[row];
        Service tempService;
        int col, count = 0;
        for (col = 0; col < allServices.length; col++) {
            tempService = allServices[col];
            if (tempService.containsSong(song)) {
                count++;
            }
        }
        return count;
    }

    public Object getValueAt(int row, int column) {
        Song song = mainDB.allSongs[row];
        Service service = null;
        Object ret = null;
        if (column == 0) {
            ret = song;
        } else if (column == 1) {
            ret = song.key;
        } else if (column == 2) {
            ret = new Integer(song.tempo);
        } else if (column == 3) {
            ret = new Integer(countRowRepeats(row));
        } else {
            service = allServices[column-fixedColumns];
            return new Boolean(service.containsSong(song));
        }
        return ret;
    }

    public boolean isCellEditable(int row, int column) {
        if (column >= fixedColumns) {
            return true;
        } else {
            return false;
        }
    }

    public void setValueAt(Object aValue, int row, int column) {
        if (column >= fixedColumns) {
            boolean b = ((Boolean)aValue).booleanValue();
            Service service = allServices[column-fixedColumns];
            Song song = mainDB.allSongs[row];
            if (b) {
                //System.out.println("add \"" + song.title + "\" to service \"" + service.toString() + "\"");
                if (service.sets.length == 0) {
                    service.sets = new SongSet[1];
                    service.sets[0] = mainDB.addNewSongSet(service.toString());
                }
                service.addSong(song);
            } else {
                //System.out.println("remove \"" + song.title + "\" from service \"" + service.toString() + "\"");
                service.removeSong(song);
            }
            mainDB.saveToFile();
            fireTableRowsUpdated(row, row);
        }
    }

}

