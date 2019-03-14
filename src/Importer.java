package harmonia;

public interface Importer {

    public void init(Database db);

    public int estimateSongsRemaining();
    public Song readSong();

    public String getNote();

    public void cleanup();

    public String toString();

}

