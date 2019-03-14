package harmonia;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;

public class ImporterEPCPraise implements Importer {

    private static final String MAIN_URL = "http://www.worshiparchive.com/A-Z/";
    private static final String SONG_ROOT_URL = "http://www.worshiparchive.com/song/";

    private Pattern songPattern;
    private Pattern songSetItemPattern;
    private Pattern authorPattern;
    private Pattern copyright1Pattern;
    private Pattern copyright2Pattern;
    private Pattern copyright3Pattern;

    private File rootEPCDir;
    private String chordDir;
    private String lyricDir;
    private String slideDir;
    private String songlistDir;
    private Database mainDB;

    private Map<String,String> songKeys;
    private Map<String,Song> songLookup;
    private String[] songTitles;
    private BufferedReader reader;
    private int curSong;
    private String curTitle;

    public ImporterEPCPraise() {
        songPattern = Pattern.compile(
                "\"(.+?)\",\"(.+?)\"",
                Pattern.DOTALL);
        songSetItemPattern = Pattern.compile(
                "^\"(.+?)\"$",
                Pattern.DOTALL);
        authorPattern = Pattern.compile(
                "\\\\i\\\\fs16\\\\cgrid0 \".+?\".*?\\\\par ([a-zA-Z,. &]+)",
                Pattern.DOTALL);
        copyright1Pattern = Pattern.compile(
                "(\\((?:c|C)\\) ([0-9]+) [a-zA-Z0-9.,&'!/ -]+)",
                Pattern.DOTALL);
        copyright2Pattern = Pattern.compile(
                "CCLI\\s*(?:\\#\\s*)?([0-9]+)",
                Pattern.DOTALL);
        copyright3Pattern = Pattern.compile(
                "(Public (D|d)omain)",
                Pattern.DOTALL);
        mainDB = null;
        songKeys = new HashMap<String,String>();
        songLookup = new HashMap<String,Song>();
        songTitles = new String[0];
        curSong = 0;
        curTitle = "";
    }

    public String readTextFile(File f) {
        StringBuffer text = new StringBuffer();
        try {
            reader = new BufferedReader(
                    new FileReader(f));
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line + "\n");
            }
            reader.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String str = text.toString();
        return str;
    }

    public byte[] readByteFile(File f) throws IOException {
        byte[] data = new byte[0];
        try {
            FileInputStream fis = new FileInputStream(f);
            data = new byte[(int)f.length()];
            fis.read(data);
            fis.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public void init(Database db) {
        mainDB = db;

        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int ret = jfc.showOpenDialog(null);
        if (ret == JFileChooser.APPROVE_OPTION) {
            rootEPCDir = jfc.getSelectedFile();
            //rootEPCDir = new File("/Users/lam/Projects/EPCPraise");
            chordDir = rootEPCDir.getAbsolutePath() +
                File.separator + "chords";
            lyricDir = rootEPCDir.getAbsolutePath() +
                File.separator + "plain";
            slideDir = rootEPCDir.getAbsolutePath() +
                File.separator + "Praise Song Overheads";
            songlistDir = rootEPCDir.getAbsolutePath() +
                File.separator + "songlists";
            File songList = new File(rootEPCDir.getAbsolutePath() +
                    File.separator + "songs.dat");
            String mainText = readTextFile(songList);
            Matcher m = songPattern.matcher(mainText);

            while (m.find()) {
                songKeys.put(m.group(1), m.group(2));
            }
        }

        songTitles = songKeys.keySet().toArray(songTitles);
        curSong = 0;
    }

    public int estimateSongsRemaining() {
        return songTitles.length - curSong;
    }

    public String getNote() {
        return curTitle;
    }
    
    public Song readSong() {
        if (curSong >= songTitles.length) {
            return null;
        }

        Attachment a;
        byte[] data;
        String author, temp, text = "";
        Matcher m;
        int i;

        String title = songTitles[curSong];
        String key = Util.sanitizeFilename(title);
        Song s = new Song(curSong, title);
        s.key = new Key(songKeys.get(title));

        curTitle = title;

        // TODO: try to find files via heuristic
        
        try {
            a = mainDB.addNewAttachment(key,
                    Attachment.DataType.CHORDS,
                    Attachment.FileType.RICHTEXT);
            data = readByteFile(new File(chordDir +
                        File.separator + title + ".rtf"));
            text = new String(data);
            a.setBytes(data);
            a.saveToFile();
            s.attachmentKeys = Util.appendToStringArray(s.attachmentKeys, a.key);
        } catch (IOException ex) {
            System.out.println("Couldn't find chords for \"" + title + "\"");
        }

        m = authorPattern.matcher(text);
        if (m.find()) {
            String[] names = m.group(1).split("&");
            for (i=0; i<names.length; i++) {
                temp = names[i].trim().toLowerCase();
                if (!temp.contains("unknown") &&
                    !temp.contains("ccli")) {
                    author = names[i].trim();
                    String splice[] = author.split(",");
                    if (splice.length >= 2) {
                        author = splice[1].trim() + " " + splice[0].trim();
                    }
                    s.authors = Util.appendToStringArray(s.authors, author);
                }
            }
        }
        m = copyright1Pattern.matcher(text);
        if (m.find()) {
           s.copyright = m.group(1);
           try {
               s.year = Integer.parseInt(m.group(2));
           } catch (NumberFormatException ex) { }
        } else {
           m = copyright2Pattern.matcher(text);
           if (m.find()) {
              s.copyright = "CCLI #" + m.group(1);
           } else {
              m = copyright3Pattern.matcher(text);
              if (m.find()) {
                 s.copyright = m.group(1);
              }
           }
        }

        try {
            a = mainDB.addNewAttachment(key,
                    Attachment.DataType.LYRICS,
                    Attachment.FileType.RICHTEXT);
            data = readByteFile(new File(lyricDir +
                        File.separator + title + ".rtf"));
            a.setBytes(data);
            a.saveToFile();
            s.attachmentKeys = Util.appendToStringArray(s.attachmentKeys, a.key);
        } catch (IOException ex) {
            System.out.println("Couldn't find lyrics for \"" + title + "\"");
        }
        
        try {
            a = mainDB.addNewAttachment(key,
                    Attachment.DataType.SLIDES,
                    Attachment.FileType.PPT);
            data = readByteFile(new File(slideDir +
                        File.separator + title + ".ppt"));
            a.setBytes(data);
            a.saveToFile();
            s.attachmentKeys = Util.appendToStringArray(s.attachmentKeys, a.key);
        } catch (IOException ex) {
            System.out.println("Couldn't find slides for \"" + title + "\"");
        }

        songLookup.put(title, s);
        
        curSong++;
        return s;
    }
    
    public void cleanup() {
        // read song lists
        DateFormat fmt = new SimpleDateFormat("yy-MM-dd");
        File listDir = new File(songlistDir);
        File[] files = listDir.listFiles();
        String label, line, title;
        Song song;
        Matcher m;
        int i;
        for (i=0; i<files.length; i++) {
            if (Util.getExtension(files[i].getAbsolutePath()).equals("sls")) {
                label = Util.getShortName(files[i].getAbsolutePath());
                Date d = new Date();
                try {
                    d = fmt.parse(label);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    cal.set(Calendar.HOUR_OF_DAY, 11);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    Service srv = mainDB.addNewService(cal.getTime());
                    SongSet set = mainDB.addNewSongSet(srv.toString());
                    srv.sets = new SongSet[1];
                    srv.sets[0] = set;
                    try {
                        BufferedReader reader = new BufferedReader(
                                new FileReader(files[i]));
                        while ((line = reader.readLine()) != null) {
                            m = songSetItemPattern.matcher(line);
                            if (m.find()) {
                                title = m.group(1);
                                song = songLookup.get(title);
                                if (song != null) {
                                    srv.addSong(song);
                                }
                            }
                        }
                        reader.close();
                    } catch (FileNotFoundException ex) { }
                      catch (IOException ex) { }
                } catch (ParseException ex) { }
            }
        }
    }

    public String toString() {
        return "EPCPraise";
    }

    public static void main(String[] args) {
        Database db = new Database(new File(Util.getDataPath() + File.separator +  "test_db.mdb"));
        Song s;
        ImporterEPCPraise imp = new ImporterEPCPraise();
        imp.init(db);
        while ((s = imp.readSong()) != null) {
            System.out.println(s.toString() + " (" + s.year + ") [" + s.key.toString() + "]");
            System.out.println(s.copyright);
            for (int i=0; i<s.authors.length; i++) {
                System.out.println(s.authors[i]);
            }
            if (s.attachmentKeys.length > 0) {
                Attachment a = db.attachments.get(s.attachmentKeys[0]);
                System.out.println(new String(a.data));
            }
        }
        imp.cleanup();
    }

}

