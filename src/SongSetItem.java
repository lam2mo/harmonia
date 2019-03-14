package harmonia;

public class SongSetItem {

    public int songID;
    public Song song;

    public String title;
    public Key key;
    public int repeats;
    public String comment;

    public SongSetItem(int id) {
        this.songID = id;
        this.song = null;
        this.title = "(untitled)";
        this.key = Key.C;
        this.repeats = 1;
        this.comment = "";
    }

    public SongSetItem(Song song) {
        this.songID = song.id;
        this.song = song;
        this.title = song.title;
        this.key = song.key;
        this.repeats = 1;
        this.comment = song.comment;
    }

}

