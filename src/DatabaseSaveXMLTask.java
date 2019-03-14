package harmonia;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.nio.charset.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;

public class DatabaseSaveXMLTask extends SwingWorker<Database, Object> {

    public static final String DONE_SUCCESS = "DatabaseSaveXMLTaskDoneSuccess";
    public static final String DONE_FAILURE = "DatabaseSaveXMLTaskDoneFailure";

    private static final String INDENT = "  ";

    private Database db;
    private Component parent;
    private ActionListener callback;
    private boolean success;

    private PrintWriter pw;
    private DateFormat dateFormatter;

    public DatabaseSaveXMLTask(Database db, ActionListener callback, Component parent) {
        this.db = db;
        this.parent = parent;
        this.callback = callback;
        this.success = false;
        this.pw = null;
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
            /*
             *Map<String,Charset> charsets = Charset.availableCharsets();
             *Charset def = Charset.defaultCharset();
             *for (Map.Entry<String,Charset> cs : charsets.entrySet()) {
             *    System.out.println("CHARSET: " + cs.getKey() + " - " + cs.getValue().name());
             *}
             *System.out.println("DEFAULT: " + def.name());
             */

            // initialize main output stream
            Charset charset = Charset.availableCharsets().get("UTF-8");
            pw = new PrintWriter(new OutputStreamWriter(
                     new FileOutputStream(db.path), charset));
            pw.println("<?xml version=\"1.0\" encoding=\"" + charset.name() + "\"?>");

            // write basic database information
            startOpenTag("database", 0);
            writeAttribute("name", db.name);
            writeAttribute("lastModified", dateToString(db.lastModified));
            nSongs = db.allSongs.length;
            writeAttributeInt("numSongs", nSongs);
            nAttachments = db.attachments.size();
            writeAttributeInt("numAttachments", nAttachments);
            nSongSets = db.allSongSets.length;
            writeAttributeInt("numSongSets", nSongSets);
            nServices = db.allServices.length;
            writeAttributeInt("numServices", nServices);
            nDeletedItems = db.deletedItems.length;
            writeAttributeInt("numDeletedItems", nDeletedItems);
            nProperties = db.properties.size();
            writeAttributeInt("numProperties", nProperties);
            totalItems = nSongs + nAttachments + nSongSets + 
                nServices + nDeletedItems + nProperties;
            writeAttributeInt("totalItems", totalItems);
            monitor.setMaximum(totalItems);
            endOpenTag(true);
 
            // write database data
            for (i=0; i<nSongs; i++) {
                writeSong(db.allSongs[i], 1);
                monitor.setProgress(++savedItems);
            }
            for (Attachment a : db.attachments.values()) {
                writeAttachment(a, 1);
                monitor.setProgress(++savedItems);
            }
            for (i=0; i<nSongSets; i++) {
                writeSongSet(db.allSongSets[i], 1);
                monitor.setProgress(++savedItems);
            }
            for (i=0; i<nServices; i++) {
                writeService(db.allServices[i], 1);
                monitor.setProgress(++savedItems);
            }
            // TODO: write deleted items
            // TODO: write properties
 
            // finalize main output
            closeTag("database", 0);
            pw.close();
        } catch (IOException ex) {
            System.out.println("ERROR saving: " + ex.getMessage());
        }
        if (!isCancelled()) {
            success = true;
        }
        return db;
    }

    public void writeIndentation(int indent) throws IOException {
        int i;
        for (i=0; i<indent; i++) {
            pw.print(INDENT);
        }
    }

    public void startOpenTag(String tag, int indent) throws IOException {
        writeIndentation(indent);
        pw.print('<');
        pw.print(tag);
    }

    public void writeAttribute(String attr, String val) throws IOException {
        pw.print(' ');
        pw.print(attr);
        pw.print("=\"");
        pw.print(val);
        pw.print('\"');
    }

    public void writeAttributeInt(String attr, int val) throws IOException {
        writeAttribute(attr, (new Integer(val)).toString());
    }

    public void endOpenTag(boolean newline) throws IOException {
        pw.print('>');
        if (newline) {
            pw.println();
        }
    }

    public void openTag(String tag, int indent, boolean newline) throws IOException {
        startOpenTag(tag, indent);
        endOpenTag(newline);
    }

    public void closeTag(String tag, int indent) throws IOException {
        writeIndentation(indent);
        pw.print("</");
        pw.print(tag);
        pw.print('>');
        pw.println();
    }

    public void writeSong(Song s, int indent) throws IOException {
        startOpenTag("song", indent);
        writeAttributeInt("id", s.id);
        writeAttribute("title", s.title);
        endOpenTag(true);
        writeStringArray("alias", s.aliases, indent+1);
        writeStringArray("author", s.authors, indent+1);
        writeSimpleElement("year", Integer.toString(s.year), indent+1);
        writeSimpleElementIfNonempty("copyright", s.copyright, indent+1);
        writeSimpleElement("key", s.key.toString(), indent+1);
        writeSimpleElement("length", Integer.toString(s.length), indent+1);
        writeSimpleElement("tempo", Integer.toString(s.tempo), indent+1);
        writeSimpleElement("meter", Integer.toString(s.meter[0]) + " " +
                                    Integer.toString(s.meter[1]), indent+1);
        writeSimpleElement("added", dateToString(s.added), indent+1);
        writeSimpleElementIfNonempty("comment", s.comment, indent+1);
        writeStringArray("tag", s.tags, indent+1);
        writePropertyList(s.properties, indent+1);
        writeStringArray("attachmentkey", s.attachmentKeys, indent+1);
        closeTag("song", indent);
    }

    public void writeAttachment(Attachment a, int indent) throws IOException {
        startOpenTag("attachment", indent);
        writeAttributeInt("id", a.id);
        writeAttribute("key", a.key);
        writeAttribute("datatype", Attachment.DataType2String(a.dataType));
        writeAttribute("filetype", Attachment.FileType2String(a.fileType));
        endOpenTag(true);
        writeSimpleElement("added", dateToString(a.added), indent+1);
        writeSimpleElementIfNonempty("comment", a.comment, indent+1);
        writeSimpleElement("size", Integer.toString(a.size), indent+1);
        // refresh hash
        //a.openFromFile();
        //a.setBytes(a.data);
        writeByteArray("hash", a.hash, indent+1);
        // write actual data
        //writeByteArray("data", a.data, indent+1);
        closeTag("attachment", indent);
    }

    public void writeSongSet(SongSet set, int indent) throws IOException {
        startOpenTag("set", indent);
        writeAttributeInt("id", set.id);
        writeAttribute("name", set.name);
        endOpenTag(true);
        writeSimpleElement("created", dateToString(set.created), indent+1);
        writeSimpleElementIfNonempty("comment", set.comment, indent+1);
        for (int i=0; i<set.songs.length; i++) {
           writeSongSetItem(set.songs[i], indent+1);
        }
        closeTag("set", indent);
    }

    public void writeService(Service s, int indent) throws IOException {
        startOpenTag("service", indent);
        writeAttributeInt("id", s.id);
        writeAttribute("date", dateToString(s.date));
        endOpenTag(true);
        writeSimpleElementIfNonempty("venue", s.venue, indent+1);
        writeSimpleElementIfNonempty("comment", s.comment, indent+1);
        for (int i=0; i<s.sets.length; i++) {
            writeSimpleElement("setid", Integer.toString(s.sets[i].id), indent+1);
        }
        closeTag("service", indent);
    }

    public void writeSongSetItem(SongSetItem item, int indent) throws IOException {
        startOpenTag("item", indent);
        writeAttributeInt("songid", item.songID);
        endOpenTag(true);
        writeSimpleElement("title", item.title, indent+1);
        writeSimpleElement("key", item.key.toString(), indent+1);
        writeSimpleElement("repeats", Integer.toString(item.repeats), indent+1);
        writeSimpleElementIfNonempty("comment", item.comment, indent+1);
        closeTag("item", indent);
    }

    public void writeSimpleElementIfNonempty(String tag, String contents, int indent) throws IOException {
        if (contents.length() > 0) {
            writeSimpleElement(tag, contents, indent);
        }
    }

    public void writeSimpleElement(String tag, String contents, int indent) throws IOException {
        openTag(tag, indent, false);
        pw.print(contents);
        closeTag(tag, 0);
    }

    public void writeStringArray(String tag, String[] arr, int indent) throws IOException {
        int i;
        for (i=0; i<arr.length; i++) {
            openTag(tag, indent, false);
            pw.print(arr[i]);
            closeTag(tag, 0);
        }
    }

    public void writePropertyList(Map<String,String> props, int indent) throws IOException {
        int i;
        for (Map.Entry<String,String> entry : props.entrySet()) {
            openTag("property", indent, false);
            writeAttribute("key", entry.getKey());
            writeAttribute("value", entry.getValue());
            closeTag("property", 0);
        }
    }

    public String dateToString(Date d) throws IOException {
        return dateFormatter.format(d);
    }

    public void writeByteArray(String tag, byte[] data, int indent) throws IOException {
        StringBuffer hex = new StringBuffer();
        int i;
        for (i=0; i<data.length; i++) {
            hex.append(Integer.toHexString(data[i] & 0xF));
        }
        openTag(tag, indent, false);
        pw.print(hex.toString());
        closeTag(tag, 0);
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

