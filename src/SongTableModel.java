package harmonia;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class SongTableModel extends AbstractTableModel {

    private SongStub[] allSongs;

    public SongTableModel(Song[] allSongs) {
        java.util.List<SongStub> stubs = new ArrayList<SongStub>();
        int i, j;
        for (i=0; i<allSongs.length; i++) {
            stubs.add(new SongStub(allSongs[i], allSongs[i].title));
            for (j=0; j<allSongs[i].aliases.length; j++) {
                stubs.add(new SongStub(allSongs[i], allSongs[i].aliases[j]));
            }

        }
        this.allSongs = stubs.toArray(new SongStub[0]);
        Arrays.sort(this.allSongs);
    }

    public Song getSong(int row) {
        return allSongs[row].song;
    }

    public int getRowCount() {
        return allSongs.length;
    }

    public int getColumnCount() {
        return 2;
    }

    public String getColumnName(int column) {
        String ret = "";
        switch (column) {
            case 0: ret = "Title"; break;
            case 1: ret = "Key"; break;
        }
        return ret;
    }

    public Class<?> getColumnClass(int columnIndex) {
        Class ret = Object.class;
        switch (columnIndex) {
            case 0: ret = String.class; break;
            case 1: ret = Key.class; break;
        }
        return ret;
    }

    public Object getValueAt(int row, int column) {
        SongStub item = allSongs[row];
        Object ret = null;
        switch (column) {
            case 0: ret = item.title; break;
            case 1: ret = item.song.key; break;
        }
        return ret;
    }

    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public void setValueAt(Object aValue, int row, int column) {
        SongStub item = allSongs[row];
        switch (column) {
            case 0: item.title = (String)aValue; break;
            case 1: item.song.key = (Key)aValue; break;
        }
    }

}


