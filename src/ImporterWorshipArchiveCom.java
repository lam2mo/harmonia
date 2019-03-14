package harmonia;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

public class ImporterWorshipArchiveCom implements Importer {

    private static final String MAIN_URL = "http://www.worshiparchive.com/A-Z/";
    private static final String SONG_ROOT_URL = "http://www.worshiparchive.com/song/";

    private Pattern songPattern;
    private Pattern authorPattern;
    private Pattern keyPattern;
    private Pattern chordsPattern;
    private Pattern yearPattern;
    private Pattern ccliPattern;

    private Database mainDB;

    private Map<String,String> songTitles;
    private String[] songKeys;
    private BufferedReader reader;
    private int curSong;
    private String curTitle;

    public ImporterWorshipArchiveCom() {
        songPattern = Pattern.compile(
                "<span class=\"title\"><a href=\"\\/song\\/(.+?)\">(.+?)<\\/a><\\/span>",
                Pattern.DOTALL);
        authorPattern = Pattern.compile(
                "<a href=\"/authors/(?:.+?)\">(.+?)<\\/a>",
                Pattern.DOTALL);
        keyPattern = Pattern.compile(
                "transpose\\(\\{ key: '(.+?)' \\}",
                Pattern.DOTALL);
        chordsPattern = Pattern.compile(
                "<pre id=\"song\">(.+?)<\\/pre>",
                Pattern.DOTALL);
        yearPattern = Pattern.compile(
                "&copy;([0-9]+)",
                Pattern.DOTALL);
        ccliPattern = Pattern.compile(
                "(CCLI #[0-9]+)",
                Pattern.DOTALL);
        mainDB = null;
        songTitles = new HashMap<String,String>();
        songKeys = new String[0];
        curSong = 0;
        curTitle = "";
    }

    public String getHTML(String url) {
        StringBuffer html = new StringBuffer();
        try {
            reader = new BufferedReader(
                    new InputStreamReader(
                        (new URL(url)).openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                html.append(line + "\n");
            }
            reader.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String str = html.toString();
        str = str.replaceAll("&#0?39;", "'");
        return str;

    }

    public void init(Database db) {
        mainDB = db;

        String mainHTML = getHTML(MAIN_URL);
        Matcher m = songPattern.matcher(mainHTML);

        while (m.find()) {
            songTitles.put(m.group(1), m.group(2));
        }

        songKeys = songTitles.keySet().toArray(songKeys);
        curSong = 0;
    }

    public int estimateSongsRemaining() {
        // TODO: fix this (must adjust by number already read)
        return songKeys.length;
    }

    public String getNote() {
        return curTitle;
    }
    
    public Song readSong() {
        if (curSong >= songKeys.length) {
            return null;
        }

        String key = songKeys[curSong];
        String title = songTitles.get(key);
        StringBuffer authorLine = new StringBuffer();
        Song s = new Song(curSong, title);
        curTitle = title;

        String songHTML = getHTML(SONG_ROOT_URL + key);

        Matcher m;

        List<String> authors = new ArrayList<String>();
        m = authorPattern.matcher(songHTML);
        while (m.find()) {
            if (authorLine.length() > 0) {
                authorLine.append(", ");
            }
            authorLine.append(m.group(1));
            authors.add(m.group(1));
            s.authors = new String[1];
            s.authors[0] = m.group(1);
        }
        s.authors = new String[authors.size()];
        s.authors = authors.toArray(s.authors);

        m = keyPattern.matcher(songHTML);
        if (m.find()) {
            s.key = new Key(m.group(1));
        }
        m = chordsPattern.matcher(songHTML);
        if (m.find()) {
            Attachment a = mainDB.addNewAttachment(key,
                    Attachment.DataType.CHORDS,
                    Attachment.FileType.PLAINTEXT);
            String text = title.toUpperCase() + Util.getLineSeparator() + 
                authorLine.toString() + Util.getLineSeparator() +
                Util.getLineSeparator() + m.group(1).trim();
            a.setBytes(text.getBytes());
            a.saveToFile();
            s.attachmentKeys = new String[1];
            s.attachmentKeys[0] = a.key;
        }
        m = yearPattern.matcher(songHTML);
        if (m.find()) {
            s.year = Integer.parseInt(m.group(1));
        }
        m = ccliPattern.matcher(songHTML);
        if (m.find()) {
            s.copyright = "Copyright (C) " + s.year + " " + m.group(1);
        }
        
        curSong++;
        return s;
    }
    
    public void cleanup() {
    }

    public String toString() {
        return "Worship Archive";
    }

    public static void main(String[] args) {
        Database db = new Database(new File(Util.getDataPath() + File.separator +  "test_db.mdb"));
        Song s;
        ImporterWorshipArchiveCom imp = new ImporterWorshipArchiveCom();
        imp.init(db);
        while ((s = imp.readSong()) != null) {
            System.out.println(s.toString() + " (" + s.year + ")");
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

