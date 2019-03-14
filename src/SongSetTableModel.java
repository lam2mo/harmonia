package harmonia;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class SongSetTableModel extends AbstractTableModel {

    private SongSet mainSet;

    public SongSetTableModel(SongSet set) {
        mainSet = set;
    }

    public int getRowCount() {
        return mainSet.songs.length;
    }

    public int getColumnCount() {
        return 5;
    }

    public String getColumnName(int column) {
        String ret = "";
        switch (column) {
            case 0: ret = "Title"; break;
            case 1: ret = "Key"; break;
            case 2: ret = "Repeats"; break;
            case 3: ret = "Comment"; break;
        }
        return ret;
    }

    public Class<?> getColumnClass(int columnIndex) {
        Class ret = Object.class;
        switch (columnIndex) {
            case 0: ret = String.class; break;
            case 1: ret = Key.class; break;
            case 2: ret = String.class; break;
            case 3: ret = String.class; break;
        }
        return ret;
    }

    public Object getValueAt(int row, int column) {
        SongSetItem item = mainSet.songs[row];
        Object ret = null;
        switch (column) {
            case 0: ret = item.title + (item.song == null ? " (deleted)" : ""); break;
            case 1: ret = item.key; break;
            case 2: ret = Integer.toString(item.repeats); break;
            case 3: ret = item.comment; break;
        }
        return ret;
    }

    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public void setValueAt(Object aValue, int row, int column) {
        SongSetItem item = mainSet.songs[row];
        switch (column) {
            case 0: item.title = (String)aValue; break;
            case 1: item.key = (Key)aValue; break;
            case 2: item.repeats = (new Integer((String)aValue)).intValue(); break;
            case 3: item.comment = (String)aValue; break;
        }
    }

}


