package harmonia;

import java.util.*;

public class SongSet implements Comparable {

    public int id;
    public String name;

    public Date created;
    public String comment;
    public SongSetItem[] songs;

    public SongSet(int id, String name) {
        this.id = id;
        this.name = name;
        this.created = new Date();
        this.comment = "";
        this.songs = new SongSetItem[0];
    }

    public int compareTo(Object other) {
        if (other instanceof SongSet) {
            return ((SongSet)other).id - id;
            //name.compareToIgnoreCase(((SongSet)other).name);
        } else {
            return 0;
        }
    }

    public String toString() {
        return name + " (" + songs.length + ")";
    }

}

