package harmonia;

import java.text.*;
import java.util.*;

public class Service implements Comparable {

    public int id;
    public Date date;

    public String venue;
    public String comment;
    public SongSet[] sets;

    private DateFormat dateFormatter;

    public Service(int id, Date date) {
        this.id = id;
        this.date = date;
        this.venue = "";
        this.comment = "";
        this.sets = new SongSet[0];
        this.dateFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
    }

    public boolean containsSong(Song s) {
        SongSet ss;
        SongSetItem ssi;
        boolean found = false;
        int i, j;
        for (i=0; !found && i<sets.length; i++) {
            ss = sets[i];
            for (j=0; !found && j<ss.songs.length; j++) {
                ssi = ss.songs[j];
                if (ssi.songID == s.id) {
                    found = true;
                }
            }
        }
        return found;
    }

    public boolean addSong(Song s) {
        boolean success = false;
        int i;
        if (containsSong(s)) {
            success = true;
        } else {
            if (sets.length > 0) {
                SongSet set = sets[sets.length-1];
                SongSetItem ssi = new SongSetItem(s);
                SongSetItem[] newSSIs = new SongSetItem[set.songs.length+1];
                for (i=0; i<set.songs.length; i++) {
                    newSSIs[i] = set.songs[i];
                }
                newSSIs[newSSIs.length-1] = ssi;
                set.songs = newSSIs;
                success = true;
            }
        }
        return success;
    }

    public boolean removeSong(Song s) {
        boolean found;
        int i, j, k, idx;
        for (i=0; i<sets.length; i++) {
            do {
                found = false;
                idx = -1;
                for (j=0; j<sets[i].songs.length; j++) {
                    if (sets[i].songs[j].songID == s.id) {
                        found = true;
                        idx = j;
                        break;
                    }
                }
                if (found && idx >= 0) {
                    SongSetItem[] newSSIs = new SongSetItem[sets[i].songs.length-1];
                    for (k=0; k<idx; k++) {
                        newSSIs[k] = sets[i].songs[k];
                    }
                    for (; k<newSSIs.length; k++) {
                        newSSIs[k] = sets[i].songs[k+1];
                    }
                    sets[i].songs = newSSIs;
                }
            } while (found);
        }
        return true;
    }

    public int compareTo(Object other) {
        if (other instanceof Service) {
            return date.compareTo(((Service)other).date);
            //return id - ((Service)other).id;
            //name.compareToIgnoreCase(((SongSet)other).name);
        } else {
            return 0;
        }
    }

    public String toString() {
        return dateFormatter.format(date);
    }

}

