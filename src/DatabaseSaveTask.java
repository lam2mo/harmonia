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

public class DatabaseSaveTask extends SwingWorker<Database, Object> {

    public static final String DONE_SUCCESS = "DatabaseSaveTaskDoneSuccess";
    public static final String DONE_FAILURE = "DatabaseSaveTaskDoneFailure";

    private Database db;
    private Component parent;
    private ActionListener callback;
    private boolean success;

    private DateFormat dateFormatter;

    public DatabaseSaveTask(Database db, ActionListener callback, Component parent) {
        this.db = db;
        this.parent = parent;
        this.callback = callback;
        this.success = false;
        this.dateFormatter = DateFormat.getDateTimeInstance();
    }

    public Database doInBackground() {
        ProgressMonitor monitor = new ProgressMonitor(parent, 
                "Saving " + db.name, "",
                0, 100);
        monitor.setProgress(0);
        int savedItems = 0, totalItems = 0;
        int nSongs, nAttachments, nSongSets;
        int nServices, nDeletedItems, nProperties;
        int i;
        try {
            // initialize main output stream
            DataOutputStream dos = new DataOutputStream(
                new FileOutputStream(db.path));

            // write basic database information
            dos.writeUTF(db.name);
            nSongs = db.allSongs.length;
            dos.writeInt(nSongs);
            nAttachments = db.attachments.size();
            dos.writeInt(nAttachments);
            nSongSets = db.allSongSets.length;
            dos.writeInt(nSongSets);
            nServices = db.allServices.length;
            dos.writeInt(nServices);
            nDeletedItems = db.deletedItems.length;
            dos.writeInt(nDeletedItems);
            nProperties = db.properties.size();
            dos.writeInt(nProperties);
            totalItems = nSongs + nAttachments + nSongSets + 
                nServices + nDeletedItems + nProperties;
            monitor.setMaximum(totalItems);

            // write database data into compressed block
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream cdos = new DataOutputStream(
                    new GZIPOutputStream(bos));
            for (i=0; i<nSongs; i++) {
                writeSong(db.allSongs[i], cdos);
                monitor.setProgress(++savedItems);
            }
            for (Attachment a : db.attachments.values()) {
                writeAttachment(a, cdos);
                monitor.setProgress(++savedItems);
            }
            for (i=0; i<nSongSets; i++) {
                writeSongSet(db.allSongSets[i], cdos);
                monitor.setProgress(++savedItems);
            }
            for (i=0; i<nServices; i++) {
                writeService(db.allServices[i], cdos);
                monitor.setProgress(++savedItems);
            }
            // TODO: write deleted items
            // TODO: write properties

            // finalize compressed data block
            cdos.close();
            byte[] data = bos.toByteArray();
            dos.writeInt(data.length);
            dos.write(data, 0, data.length);

            // finalize main output stream
            dos.close();
        } catch (IOException ex) {
            System.out.println("ERROR saving: " + ex.getMessage());
        }
        if (!isCancelled()) {
            success = true;
        }
        return db;
    }

    public void writeSong(Song s, DataOutputStream os) throws IOException {
        os.writeInt(s.id);
        os.writeUTF(s.title);
        writeStringArray(s.aliases, os);
        writeStringArray(s.authors, os);
        os.writeInt(s.year);
        os.writeUTF(s.copyright);
        os.writeUTF(s.key.toString());
        os.writeInt(s.length);
        os.writeInt(s.tempo);
        os.writeInt(s.meter[0]);
        os.writeInt(s.meter[1]);
        writeUTFDate(s.added, os);
        os.writeUTF(s.comment);
        writeStringArray(s.tags, os);
        writePropertyList(s.properties, os);
        writeStringArray(s.attachmentKeys, os);
    }

    public void writeAttachment(Attachment a, DataOutputStream os) throws IOException {
        os.writeInt(a.id);
        os.writeUTF(a.key);
        os.writeUTF(Attachment.DataType2String(a.dataType));
        os.writeUTF(Attachment.FileType2String(a.fileType));
        writeUTFDate(a.added, os);
        os.writeUTF(a.comment);
        os.writeInt(a.size);
        writeByteArray(a.hash, os);
        os.writeBoolean(false);
    }

    public void writeSongSet(SongSet set, DataOutputStream os) throws IOException {
        os.writeInt(set.id);
        os.writeUTF(set.name);
        writeUTFDate(set.created, os);
        os.writeUTF(set.comment);
        os.writeInt(set.songs.length);
        for (int i=0; i<set.songs.length; i++) {
            writeSongSetItem(set.songs[i], os);
        }
    }

    public void writeService(Service s, DataOutputStream os) throws IOException {
        os.writeInt(s.id);
        writeUTFDate(s.date, os);
        os.writeUTF(s.venue);
        os.writeUTF(s.comment);
        os.writeInt(s.sets.length);
        for (int i=0; i<s.sets.length; i++) {
            os.writeInt(s.sets[i].id);
        }
    }

    public void writeSongSetItem(SongSetItem item, DataOutputStream os) throws IOException {
        os.writeInt(item.songID);
        os.writeUTF(item.title);
        os.writeUTF(item.key.toString());
        os.writeInt(item.repeats);
        os.writeUTF(item.comment);
    }

    public void writeStringArray(String[] arr, DataOutputStream os) throws IOException {
        int i;
        os.writeInt(arr.length);
        for (i=0; i<arr.length; i++) {
            os.writeUTF(arr[i]);
        }
    }

    public void writePropertyList(Map<String,String> props, DataOutputStream os) throws IOException {
        int i;
        os.writeInt(props.size());
        for (Map.Entry<String,String> entry : props.entrySet()) {
            os.writeUTF(entry.getKey());
            os.writeUTF(entry.getValue());
        }
    }

    public void writeUTFDate(Date d, DataOutputStream os) throws IOException {
        os.writeUTF(dateFormatter.format(d));
    }

    public void writeByteArray(byte[] data, DataOutputStream os) throws IOException {
        os.writeInt(data.length);
        os.write(data, 0, data.length);
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

