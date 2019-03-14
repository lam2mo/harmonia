package harmonia;

import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.*;
import javax.swing.*;
import javax.swing.event.*;

public class Database implements Comparable {

    public File path;
    public File attachmentPath;

    public String name;
    public Date lastModified;
    public Song[] allSongs;
    public SongSet[] allSongSets;
    public Service[] allServices;
    public Object[] deletedItems;
    public Map<String,Attachment> attachments;
    public Map<String,String> properties;

    public int nextSongID;
    public int nextAttachmentID;
    public int nextSongSetID;
    public int nextServiceID;

    public Database (File path) {
        this(path, new File(path.getParent() + File.separator +
                    Util.getShortName(path.getName()) + App.APP_ATTACH_EXT));
    }

    public Database (File path, File attachmentPath) {
        this.path = path;
        this.attachmentPath = attachmentPath;
        allSongs = new Song[0];
        allSongSets = new SongSet[0];
        allServices = new Service[0];
        deletedItems = new Object[0];
        nextSongID = 0;
        nextAttachmentID = 0;
        nextSongSetID = 0;
        nextServiceID = 0;
        attachments = new TreeMap<String,Attachment>();
        properties = new TreeMap<String,String>();

        this.name = Util.getShortName(path.getName());
        this.lastModified = new Date();

        // read basic data, like name, from file if it exists already
        if (path.exists() && path.length() > 0) {
            try {
                DatabaseXMLHandler.openXML(this, true, null);
            } catch (IOException ex) {
                ex.printStackTrace();
            }    

            /*
            try {
                DataInputStream dis = new DataInputStream(
                    new FileInputStream(path));
                name = dis.readUTF();
                int nSongs = dis.readInt();
                int nAttachments = dis.readInt();
                int nSets = dis.readInt();
                int nServices = dis.readInt();
                int nProperties = dis.readInt();
                dis.close();
            } catch (IOException ex) {
                System.out.println("ERROR listing: " + ex.getMessage());
                // ignore
            }
            */
        }
    }

    public void saveToFile() {
        lastModified = new Date();
        SwingWorker<Database, Object> task = new DatabaseSaveXMLTask(this, null, null);
        task.execute();
    }

    public SongStub[] getAllSongs() {
        java.util.List<SongStub> songs = new ArrayList<SongStub>();
        Song s;
        int i, j;
        for (i=0; i<allSongs.length; i++) {
            s = allSongs[i];
            songs.add(new SongStub(s, s.title));
            for (j=0; j<s.aliases.length; j++) {
                songs.add(new SongStub(s, s.aliases[j]));
            }
        }
        return songs.toArray(new SongStub[0]);
    }
            

    public Song addNewSong(String title) {
        Song newSong = new Song(0, title);
        addNewSong(newSong);
        return newSong;
    }

    public void addNewSong(Song newSong) {
        newSong.id = nextSongID++; // assign unique ID
        Song[] newAllSongs = new Song[allSongs.length+1];
        int idx = 0;
        while (idx < allSongs.length &&
                allSongs[idx].title.compareTo(newSong.title) <= 0) {
            newAllSongs[idx] = allSongs[idx];
            idx++;
        }
        newAllSongs[idx] = newSong;
        idx++;
        while (idx < newAllSongs.length) {
            newAllSongs[idx] = allSongs[idx-1];
            idx++;
        }
        allSongs = newAllSongs;
        Arrays.sort(allSongs);
    }

    public void removeSong(Song song) {
        int idx = 0;
        while (allSongs[idx] != song) {
            idx++;
        }
        if (idx >= allSongs.length) {
            return;
        }
        Song[] newAllSongs = new Song[allSongs.length-1];
        int i = 0;
        while (i < idx) {
            newAllSongs[i] = allSongs[i];
            i++;
        }
        while (i < newAllSongs.length) {
            newAllSongs[i] = allSongs[i+1];
            i++;
        }
        allSongs = newAllSongs;
    }

    public Attachment addNewAttachment(String baseKey, Attachment.DataType dtype, Attachment.FileType ftype) {
        String key = Util.sanitizeFilename(baseKey) + "_" +
            Util.sanitizeFilename(Attachment.DataType2String(dtype), true) + "_" + 
            Util.sanitizeFilename(Attachment.FileType2String(ftype), true);
        while (attachments.containsKey(key)) {
            key += "x";
        }
        Attachment a = new Attachment(this, nextAttachmentID++, key, dtype, ftype);
        attachments.put(key, a);
        return a;
    }

    public SongSet addNewSongSet(String name) {
        SongSet newSongSet = new SongSet(nextSongSetID++, name);
        addNewSongSet(newSongSet);
        return newSongSet;
    }
    
    public void addNewSongSet(SongSet newSongSet) {
        SongSet[] newAllSongSets = new SongSet[allSongSets.length+1];
        int idx = 0;
        while (idx < allSongSets.length) {
            newAllSongSets[idx] = allSongSets[idx];
            idx++;
        }
        newAllSongSets[idx] = newSongSet;
        allSongSets = newAllSongSets;
    }
    
    public void removeSongSet(SongSet set) {
        int idx = 0;
        while (allSongSets[idx] != set) {
            idx++;
        }
        if (idx >= allSongSets.length) {
            return;
        }
        SongSet[] newAllSongSets = new SongSet[allSongSets.length-1];
        int i = 0;
        while (i < idx) {
            newAllSongSets[i] = allSongSets[i];
            i++;
        }
        while (i < newAllSongSets.length) {
            newAllSongSets[i] = allSongSets[i+1];
            i++;
        }
        allSongSets = newAllSongSets;
    }

    public Service addNewService(Date date) {
        Service newService = new Service(nextServiceID++, date);
        Service[] newAllServices = new Service[allServices.length+1];
        int idx = 0;
        while (idx < allServices.length &&
                allServices[idx].date.compareTo(date) <= 0) {
            newAllServices[idx] = allServices[idx];
            idx++;
        }
        newAllServices[idx] = newService;
        idx++;
        while (idx < newAllServices.length) {
            newAllServices[idx] = allServices[idx-1];
            idx++;
        }
        allServices = newAllServices;
        Arrays.sort(allServices);
        return newService;
    }

    public void removeService(Service song) {
        int idx = 0;
        while (allServices[idx] != song) {
            idx++;
        }
        if (idx >= allServices.length) {
            return;
        }
        Service[] newAllServices = new Service[allServices.length-1];
        int i = 0;
        while (i < idx) {
            newAllServices[i] = allServices[i];
            i++;
        }
        while (i < newAllServices.length) {
            newAllServices[i] = allServices[i+1];
            i++;
        }
        allServices = newAllServices;
    }

    public int compareTo(Object db) {
        if (db instanceof Database) {
            return name.compareTo(((Database)db).name);
        } else {
            return 0;
        }
    }

    public String toString() {
        return name;
    }

}

