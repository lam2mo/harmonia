package harmonia;

import java.io.*;
import java.math.*;
import java.security.*;
import java.util.*;

public class Attachment {

    public enum DataType {
        UNKNOWN,
        LYRICS,
        SLIDES,
        CHORDS,
        LEAD,
        SHEET,
        RECORDING,
        SAMPLE
    }

    public enum FileType {
        UNKNOWN,
        PLAINTEXT,
        RICHTEXT,
        HTML,
        PDF,
        PPT,
        IMAGE
    }

    public static DataType String2DataType(String str) {
        DataType dtype = DataType.UNKNOWN;
        str = str.toLowerCase();
        if (str.contains("lyric")) {
            dtype = DataType.LYRICS;
        } else if (str.contains("slide")) {
            dtype = DataType.SLIDES;
        } else if (str.contains("overhead")) {
            dtype = DataType.SLIDES;
        } else if (str.contains("chord")) {
            dtype = DataType.CHORDS;
        } else if (str.contains("lead")) {
            dtype = DataType.LEAD;
        } else if (str.contains("sheet")) {
            dtype = DataType.SHEET;
        } else if (str.contains("recording")) {
            dtype = DataType.RECORDING;
        } else if (str.contains("sample")) {
            dtype = DataType.SAMPLE;
        }
        return dtype;
    }

    public static FileType String2FileType(String str) {
        FileType ftype = FileType.UNKNOWN;
        str = str.toLowerCase();
        if (str.contains("plain")) {
            ftype = FileType.PLAINTEXT;
        } else if (str.contains("rich")) {
            ftype = FileType.RICHTEXT;
        } else if (str.contains("html")) {
            ftype = FileType.HTML;
        } else if (str.contains("pdf")) {
            ftype = FileType.PDF;
        } else if (str.contains("ppt")) {
            ftype = FileType.PPT;
        } else if (str.contains("image")) {
            ftype = FileType.IMAGE;
        }
        return ftype;
    }

    public static String DataType2String(DataType dtype) {
        String str = "";
        switch (dtype) {
            case LYRICS:    str = "Lyrics";    break;
            case SLIDES:    str = "Slides";    break;
            case CHORDS:    str = "Chords";    break;
            case LEAD:      str = "Lead";      break;
            case SHEET:     str = "Sheet";     break;
            case RECORDING: str = "Recording"; break;
            case SAMPLE:    str = "Sample";    break;
            default:        str = "Other";     break;
        }
        return str;
    }

    public static String FileType2String(FileType ftype) {
        String str = "";
        switch (ftype) {
            case PLAINTEXT: str = "(plain text)";     break;
            case RICHTEXT:  str = "(rich text)";      break;
            case HTML:      str = "(HTML)";           break;
            case PDF:       str = "(PDF)";            break;
            case PPT:       str = "(PPT)";            break;
            case IMAGE:     str = "(image)";          break;
            default:        str = "(unknown format)"; break;
        }
        return str;
    }

    public static String FileType2Extension(FileType ftype) {
        String str = "";
        switch (ftype) {
            case PLAINTEXT: str = "txt";  break;
            case RICHTEXT:  str = "rtf";  break;
            case HTML:      str = "htm";  break;
            case PDF:       str = "pdf";  break;
            case PPT:       str = "ppt";  break;
            case IMAGE:     str = "png";  break;
            default:        str = "dat";  break;
        }
        return str;
    }

    private Database mainDB;

    public int id;
    public String key;

    public DataType dataType;
    public FileType fileType;
    public Date added;
    public String comment;
    public boolean cached;
    public int size;
    public byte[] hash;
    public byte[] data;

    public Attachment(Database db, int id, String key, DataType dtype, FileType ftype) {
        mainDB = db;
        this.id = id;
        this.key = key;
        this.dataType = dtype;
        this.fileType = ftype;
        this.added = new Date();
        this.comment = "";
        this.cached = false;
        this.size = 0;
        this.hash = new byte[0];
        this.data = new byte[0];
    }

    public void setBytes(byte[] data) {
        this.data = data;
        this.size = data.length;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data);
            this.hash = md.digest();
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        cached = true;
    }

    public File getFile() {
        return new File(mainDB.attachmentPath.getAbsolutePath() + 
                File.separator + key + "." + FileType2Extension(fileType));
    }

    public void saveToFile() {
        if (!cached) {
            System.out.println("WARNING: saving uncached attachment!");
        }
        try {
            File path = getFile();
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(data);
            fos.close();

            //System.out.println("saving attachment:");
            //System.out.println(mainDB.attachmentPath.getAbsolutePath() + 
                    //File.separator + key + "." + App.APP_ATTACH_FILE_EXT);
            //System.out.println("size=" + size);
            //BigInteger bi = new BigInteger(1, hash);
            //System.out.println("hash=" + String.format("%x", bi));
            //System.out.println(new String(data));
        } catch (IOException ex) {
            System.out.println("ERROR saving attachment: " + ex.getMessage());
        }
    }

    public void openFromFile() {
        try {
            File path = getFile();
            FileInputStream fis = new FileInputStream(path);
            data = new byte[size];
            fis.read(data);
            fis.close();
            cached = true;

            //System.out.println("reading attachment:");
            //System.out.println(mainDB.attachmentPath.getAbsolutePath() + 
                    //File.separator + key + "." + App.APP_ATTACH_FILE_EXT);
            //System.out.println("size=" + size);
            //BigInteger bi = new BigInteger(1, hash);
            //System.out.println("hash=" + String.format("%x", bi));
            //System.out.println(new String(data));
        } catch (IOException ex) {
            System.out.println("ERROR opening attachment: " + ex.getMessage());
        }
    }

    public String toString() {
        return DataType2String(dataType) + " - " + FileType2String(fileType);
    }

}

