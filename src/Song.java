package harmonia;

import java.util.*;

public class Song implements Comparable {

    public int id;

    public String title;
    public String[] aliases;
    public String[] authors;
    public int year;
    public String copyright;

    public Key key;
    public int length;
    public int tempo;
    public int[] meter;
    
    public Date added;
    public String comment;
    public String[] tags;
    public Map<String,String> properties;

    public String[] attachmentKeys;

    public Song(int id, String title) {
        Date now = new Date();
        this.id = id;
        this.title = title;
        this.aliases = new String[0];
        this.authors = new String[0];
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        this.year = cal.get(Calendar.YEAR);
        this.copyright = "";
        this.key = Key.C;
        this.length = 0;
        this.tempo = 120;
        this.meter = new int[2];
        this.meter[0] = 4; this.meter[1] = 4;
        this.added = now;
        this.comment = "";
        this.tags = new String[0];
        this.properties = new HashMap<String,String>();
        this.attachmentKeys = new String[0];
    }

    public int compareTo(Object other) {
        int val = 0;
        if (other instanceof Song) {
            val = title.compareToIgnoreCase(((Song)other).title);
            if (val == 0) {
                val = id - ((Song)other).id;
            }
        }
        return val;
    }

    public String toString() {
        //return title + " (" + key.toString() + ")";
        return title;
    }

}

