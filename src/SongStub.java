package harmonia;

public class SongStub implements Comparable {

    public Song song;
    public String title;

    public SongStub(Song s, String title) {
        this.song = s;
        this.title = title;
    }

    public int compareTo(Object other) {
        int val = 0;
        if (other instanceof SongStub) {
            val = title.compareToIgnoreCase(((SongStub)other).title);
            if (val == 0) {
                val = song.id - ((SongStub)other).song.id;
            }
        }
        return val;
    }

    public String toString() {
        return title;
    }

}

