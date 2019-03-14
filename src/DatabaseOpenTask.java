package harmonia;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.*;
import javax.swing.*;

public class DatabaseOpenTask extends SwingWorker<Database, Object> {

    public static final String DONE_SUCCESS = "DatabaseOpenTaskDoneSuccess";
    public static final String DONE_FAILURE = "DatabaseOpenTaskDoneFailure";

    private Database db;
    private Component parent;
    private ActionListener callback;
    private boolean success;

    private Map<Integer, Song> songLookup;
    private Map<Integer, SongSet> songSetLookup;
    private Map<Integer, Service> serviceLookup;

    private DateFormat dateFormatter;

    public DatabaseOpenTask(Database db, ActionListener callback, Component parent) {
        this.db = db;
        this.parent = parent;
        this.callback = callback;
        this.success = false;
        this.songLookup = new HashMap<Integer, Song>();
        this.songSetLookup = new HashMap<Integer, SongSet>();
        this.serviceLookup = new HashMap<Integer, Service>();
        this.dateFormatter = DateFormat.getDateTimeInstance();
    }

    public Database doInBackground() {
        ProgressMonitor monitor = new ProgressMonitor(parent, 
                "Opening " + db.name, "",
                0, 100);
        monitor.setProgress(0);
        int openedItems = 0, totalItems = 0;
        int nSongs, nAttachments, nSongSets;
        int nDeletedItems, nServices, nProperties;
        int i;
        try {
            // initialize main input stream
            DataInputStream dis = new DataInputStream(
                new FileInputStream(db.path));

            // read basic database information
            db.name = dis.readUTF();
            nSongs = dis.readInt();
            nAttachments = dis.readInt();
            nSongSets = dis.readInt();
            nServices = dis.readInt();
            nDeletedItems = dis.readInt();
            nProperties = dis.readInt();
            totalItems = nSongs + nAttachments + nSongSets + 
                nServices + nDeletedItems + nProperties;
            monitor.setMaximum(totalItems);

            // read compressed block
            int dataSize = dis.readInt();
            byte[] data = new byte[dataSize];
            dis.read(data, 0, dataSize);
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            DataInputStream cdis = new DataInputStream(
                    new GZIPInputStream(bis));

            // extract database data from block
            db.allSongs = new Song[nSongs];
            for (i=0; i<nSongs; i++) {
                db.allSongs[i] = readSong(cdis);
                db.nextSongID = Math.max(db.nextSongID, db.allSongs[i].id+1);
                monitor.setProgress(++openedItems);
            }
            Arrays.sort(db.allSongs);
            for (i=0; i<nAttachments; i++) {
                Attachment a = readAttachment(cdis);
                db.attachments.put(a.key, a);
                db.nextAttachmentID = Math.max(db.nextAttachmentID, a.id+1);
                monitor.setProgress(++openedItems);
            }
            db.allSongSets = new SongSet[nSongSets];
            for (i=0; i<nSongSets; i++) {
                db.allSongSets[i] = readSongSet(cdis);
                db.nextSongSetID = Math.max(db.nextSongSetID, db.allSongSets[i].id+1);
                monitor.setProgress(++openedItems);
            }
            db.allServices = new Service[nServices];
            for (i=0; i<nServices; i++) {
                db.allServices[i] = readService(cdis);
                db.nextServiceID = Math.max(db.nextServiceID, db.allServices[i].id+1);
                monitor.setProgress(++openedItems);
            }
            // TODO: read deleted items
            // TODO: read properties

            // close input streams
            cdis.close();
            dis.close();
        } catch (IOException ex) {
            System.out.println("ERROR opening: " + ex.getMessage());
        }
        if (!isCancelled()) {
            success = true;
        }
        return db;
    }

    public Song readSong(DataInputStream is) throws IOException {
        Song s = new Song(is.readInt(), is.readUTF());
        s.aliases = readStringArray(is);
        s.authors = readStringArray(is);
        s.year = is.readInt();
        s.copyright = is.readUTF();
        s.key = new Key(is.readUTF());
        s.length = is.readInt();
        s.tempo = is.readInt();
        s.meter[0] = is.readInt();
        s.meter[1] = is.readInt();
        s.added = readUTFDate(is);
        s.comment = is.readUTF();
        s.tags = readStringArray(is);
        s.properties = readPropertyList(is);
        s.attachmentKeys = readStringArray(is);
        songLookup.put(new Integer(s.id), s);
        return s;
    }

    public Attachment readAttachment(DataInputStream is) throws IOException {
        int id = is.readInt();
        String key = is.readUTF();
        Attachment.DataType dtype = Attachment.String2DataType(is.readUTF());
        Attachment.FileType ftype = Attachment.String2FileType(is.readUTF());
        Attachment a = new Attachment(db, id, key, dtype, ftype);
        a.added = readUTFDate(is);
        a.comment = is.readUTF();
        a.size = is.readInt();
        a.hash = readByteArray(is);
        a.cached = is.readBoolean();
        if (a.cached) {
            a.data = readByteArray(is);
        }
        return a;
    }

    public SongSet readSongSet(DataInputStream is) throws IOException {
        SongSet set = new SongSet(is.readInt(), is.readUTF());
        set.created = readUTFDate(is);
        set.comment = is.readUTF();
        set.songs = new SongSetItem[is.readInt()];
        for (int i=0; i<set.songs.length; i++) {
            set.songs[i] = readSongSetItem(is);
        }
        songSetLookup.put(new Integer(set.id), set);
        return set;
    }

    public Service readService(DataInputStream is) throws IOException {
        Service s = new Service(is.readInt(), readUTFDate(is));
        s.venue = is.readUTF();
        s.comment = is.readUTF();
        s.sets = new SongSet[is.readInt()];
        for (int i=0; i<s.sets.length; i++) {
            SongSet set = songSetLookup.get(new Integer(is.readInt()));
            if (set == null) {
                set = new SongSet(-1, "(deleted)");
            }
            s.sets[i] = set;
        }
        return s;
    }

    public SongSetItem readSongSetItem(DataInputStream is) throws IOException {
        int sid = is.readInt();
        Song song = songLookup.get(new Integer(sid));
        SongSetItem item = new SongSetItem(sid);
        if (song != null) {
            item.song = song;
        }
        item.title = is.readUTF();
        item.key = new Key(is.readUTF());
        item.repeats = is.readInt();
        item.comment = is.readUTF();
        return item;
    }

    public String[] readStringArray(DataInputStream is) throws IOException {
        int i, length;
        length = is.readInt();
        String[] arr = new String[length];
        for (i=0; i<length; i++) {
            arr[i] = is.readUTF();
        }
        return arr;
    }

    public Map<String,String> readPropertyList(DataInputStream is) throws IOException {
        Map<String,String> props = new HashMap<String,String>();
        int i, length;
        String key, value;
        length = is.readInt();
        for (i=0; i<length; i++) {
            key = is.readUTF();
            value = is.readUTF();
            props.put(key, value);
        }
        return props;
    }

    public Date readUTFDate(DataInputStream is) throws IOException {
        Date d = null;
        try {
            d = dateFormatter.parse(is.readUTF());
        } catch (ParseException ex) {
            d = new Date();
        }
        return d;
    }

    public byte[] readByteArray(DataInputStream is) throws IOException {
        int size = is.readInt();
        byte[] data = new byte[size];
        is.read(data, 0, size);
        return data;
    }

    public void done() {
        if (callback != null) {
            if (success) {
                callback.actionPerformed(new ActionEvent(this, 
                            ActionEvent.ACTION_PERFORMED, DONE_SUCCESS));
            } else {
                callback.actionPerformed(new ActionEvent(this, 
                            ActionEvent.ACTION_PERFORMED, DONE_FAILURE));
            }
        }
    }

}

