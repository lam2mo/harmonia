package harmonia;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.swing.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

class DatabaseXMLHandler extends DefaultHandler {

    public static final String HEADERS_ONLY_READ_DONE = "DatabaseXMLHeaderOnlyReadDone";

    private Database mainDB;
    private ProgressMonitor pmon;
    private boolean brief;

    private Map<Integer, Song> songLookup;
    private Map<Integer, SongSet> songSetLookup;

    private int openedItems, totalItems;
    private int nSongs, nSongSets, nServices;
    private int nDeletedItems, nProperties;

    private String temp, tempKey, tempValue;
    private Song tempSong;
    private Attachment tempAttachment;
    private SongSet tempSongSet;
    private SongSetItem tempSongSetItem;
    private Service tempService;

    private DateFormat dateFormatter;

    public static void openXML(Database db, boolean headersOnly, ProgressMonitor monitor) 
            throws IOException {
		try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
            DatabaseXMLHandler dxh = new DatabaseXMLHandler(db, headersOnly, monitor);
            sp.parse(db.path, dxh);
		} catch (SAXException ex) {
            if (!ex.getMessage().equals(HEADERS_ONLY_READ_DONE)) {
                throw new IOException(ex.getMessage(), ex);
            }
		} catch (ParserConfigurationException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
            throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
        }
    }
        
    public DatabaseXMLHandler(Database db, boolean headersOnly, ProgressMonitor monitor) {
        mainDB = db;
        brief = headersOnly;
        pmon = monitor;
        songLookup = new HashMap<Integer, Song>();
        songSetLookup = new HashMap<Integer, SongSet>();
        openedItems = 0;
        totalItems = 0;
        nSongs = 0;
        nSongSets = 0;
        nServices = 0;
        nDeletedItems = 0;
        nProperties = 0;
        temp = "";
        tempSong = null;
        tempSongSet = null;
        tempSongSetItem = null;
        tempService = null;
        dateFormatter = DateFormat.getDateTimeInstance();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        temp = "";

        if (qName.equalsIgnoreCase("database")) {
            initDB(attributes);
        } else if (qName.equalsIgnoreCase("song")) {
            tempSong = new Song(getAttributeInt(attributes, "id"),
                                getAttribute(attributes, "title"));
        } else if (qName.equalsIgnoreCase("attachment")) {
            tempAttachment = new Attachment(mainDB,
                                getAttributeInt(attributes, "id"),
                                getAttribute(attributes, "key"),
                                Attachment.String2DataType(getAttribute(attributes, "datatype")),
                                Attachment.String2FileType(getAttribute(attributes, "filetype"))
                                );
        } else if (qName.equalsIgnoreCase("set")) {
            tempSongSet = new SongSet(getAttributeInt(attributes, "id"),
                                getAttribute(attributes, "name"));
        } else if (qName.equalsIgnoreCase("item")) {
            tempSongSetItem = new SongSetItem(getAttributeInt(attributes, "songid"));
            tempSongSetItem.song = songLookup.get(new Integer(tempSongSetItem.songID));
        } else if (qName.equalsIgnoreCase("service")) {
            tempService = new Service(getAttributeInt(attributes, "id"),
                                getAttributeDate(attributes, "date"));
        } else if (qName.equalsIgnoreCase("property")) {
            tempKey = getAttribute(attributes, "key");
            tempValue = getAttribute(attributes, "value");
        }
    }

    private void initDB(Attributes attributes) throws SAXException {
        mainDB.allSongs = new Song[getAttributeInt(attributes, "numSongs")];
        mainDB.allSongSets = new SongSet[getAttributeInt(attributes, "numSongSets")];
        mainDB.allServices = new Service[getAttributeInt(attributes, "numServices")];

        openedItems = 0;
        totalItems = getAttributeInt(attributes, "totalItems");
        if (pmon != null) {
            pmon.setMaximum(totalItems);
        }

        mainDB.name = getAttribute(attributes, "name");
        mainDB.lastModified = getAttributeDate(attributes, "lastModified");

        if (brief) {
            throw new SAXException(HEADERS_ONLY_READ_DONE);
        }
    }

    private String getAttribute(Attributes attributes, String key) throws SAXException {
        String value = attributes.getValue(key);
        if (value == null) {
            throw new SAXException ("Missing attribute \"" + key + "\"");
        }
        return value;
    }

    private int getAttributeInt(Attributes attributes, String key) throws SAXException {
        String value = getAttribute(attributes, key);
        int ivalue = 0;
        try {
            ivalue = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new SAXException ("Unable to parse integer from attribute \"" + key + "\"");
        }
        return ivalue;
    }

    private Date getAttributeDate(Attributes attributes, String key) throws SAXException {
        String value = getAttribute(attributes, key);
        Date dvalue = null;
        try {
            dvalue = dateFormatter.parse(value);
        } catch (ParseException ex) {
            throw new SAXException ("Unable to parse date from attribute \"" + key + "\"");
        }
        return dvalue;
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        temp = new String(ch, start, length);
    }

    public int toInt(String str) {
        return Integer.parseInt(str);
    }

    public Date toDate(String str) {
        Date d = null;
        try {
            d = dateFormatter.parse(str);
        } catch (ParseException ex) {
            d = new Date();
        }
        return d;
    }

    public byte[] toByteArray(String str) {
        int i;
        byte[] data = new byte[str.length()];
        for (i=0; i<str.length(); i++) {
            data[i] = Byte.parseByte(str.substring(i,i+1), 16);
        }

        // for testing
        /*
         *System.out.println(str);
         *StringBuffer hex = new StringBuffer();
         *for (i=0; i<data.length; i++) {
         *    hex.append(Integer.toHexString(data[i]));
         *}
         *System.out.println(hex.toString());
         */

        return data;
    }

    public SongSetItem[] appendSongItem(SongSetItem[] songs, SongSetItem item) {
        SongSetItem[] newSongs = new SongSetItem[songs.length+1];
        int i;
        for (i=0; i<songs.length; i++) {
            newSongs[i] = songs[i];
        }
        newSongs[newSongs.length-1] = item;
        return newSongs;
    }

    public SongSet[] appendSongSet(SongSet[] sets, SongSet set) {
        SongSet[] newSets = new SongSet[sets.length+1];
        int i;
        for (i=0; i<sets.length; i++) {
            newSets[i] = sets[i];
        }
        newSets[newSets.length-1] = set;
        return newSets;
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("database")) {
            finalizeDB();

        } else if (tempSong != null && qName.equalsIgnoreCase("alias")) {
            tempSong.aliases = Util.appendToStringArray(tempSong.aliases, temp);
        } else if (tempSong != null && qName.equalsIgnoreCase("author")) {
            tempSong.authors = Util.appendToStringArray(tempSong.authors, temp);
        } else if (tempSong != null && qName.equalsIgnoreCase("year")) {
            tempSong.year = toInt(temp);
        } else if (tempSong != null && qName.equalsIgnoreCase("copyright")) {
            tempSong.copyright = temp;
        } else if (tempSong != null && qName.equalsIgnoreCase("key")) {
            tempSong.key = new Key(temp);
        } else if (tempSong != null && qName.equalsIgnoreCase("length")) {
            tempSong.length = toInt(temp);
        } else if (tempSong != null && qName.equalsIgnoreCase("tempo")) {
            tempSong.tempo = toInt(temp);
        } else if (tempSong != null && qName.equalsIgnoreCase("meter")) {
            String[] parts = temp.split(" ");
            if (parts.length == 2) {
                tempSong.meter[0] = toInt(parts[0]);
                tempSong.meter[1] = toInt(parts[1]);
            }
        } else if (tempSong != null && qName.equalsIgnoreCase("added")) {
            tempSong.added = toDate(temp);
        } else if (tempSong != null && qName.equalsIgnoreCase("comment")) {
            tempSong.comment = temp;
        } else if (tempSong != null && qName.equalsIgnoreCase("tag")) {
            tempSong.tags = Util.appendToStringArray(tempSong.tags, temp);
        } else if (tempSong != null && qName.equalsIgnoreCase("property")) {
            tempSong.properties.put(tempKey, tempValue);
        } else if (tempSong != null && qName.equalsIgnoreCase("attachmentkey")) {
            tempSong.attachmentKeys = Util.appendToStringArray(tempSong.attachmentKeys, temp);
        } else if (tempSong != null && qName.equalsIgnoreCase("song")) {
            songLookup.put(new Integer(tempSong.id), tempSong);
            mainDB.allSongs[nSongs] = tempSong;
            mainDB.nextSongID = Math.max(mainDB.nextSongID, tempSong.id+1);
            nSongs++;
            tempSong = null;

        } else if (tempAttachment != null && qName.equalsIgnoreCase("added")) {
            tempAttachment.added = toDate(temp);
        } else if (tempAttachment != null && qName.equalsIgnoreCase("comment")) {
            tempAttachment.comment = temp;
        } else if (tempAttachment != null && qName.equalsIgnoreCase("size")) {
            tempAttachment.size = toInt(temp);
        } else if (tempAttachment != null && qName.equalsIgnoreCase("hash")) {
            tempAttachment.hash = toByteArray(temp);
        } else if (tempAttachment != null && qName.equalsIgnoreCase("data")) {
            tempAttachment.setBytes(toByteArray(temp));
        } else if (tempAttachment != null && qName.equalsIgnoreCase("attachment")) {
            mainDB.attachments.put(tempAttachment.key, tempAttachment);
            mainDB.nextAttachmentID = Math.max(mainDB.nextAttachmentID, tempAttachment.id+1);
            tempAttachment = null;

        } else if (tempSongSetItem != null && tempSongSet != null && qName.equalsIgnoreCase("title")) {
            tempSongSetItem.title = temp;
        } else if (tempSongSetItem != null && tempSongSet != null && qName.equalsIgnoreCase("key")) {
            tempSongSetItem.key = new Key(temp);
        } else if (tempSongSetItem != null && tempSongSet != null && qName.equalsIgnoreCase("repeats")) {
            tempSongSetItem.repeats = toInt(temp);
        } else if (tempSongSetItem != null && tempSongSet != null && qName.equalsIgnoreCase("comment")) {
            tempSongSetItem.comment = temp;
        } else if (tempSongSetItem != null && tempSongSet != null && qName.equalsIgnoreCase("item")) {
            tempSongSet.songs = appendSongItem(tempSongSet.songs, tempSongSetItem);
            tempSongSetItem = null;

        } else if (tempSongSet != null && tempSongSetItem == null && qName.equalsIgnoreCase("created")) {
            tempSongSet.created = toDate(temp);
        } else if (tempSongSet != null && tempSongSetItem == null && qName.equalsIgnoreCase("comment")) {
            tempSongSet.comment = temp;
        } else if (tempSongSet != null && tempSongSetItem == null && qName.equalsIgnoreCase("set")) {
            songSetLookup.put(new Integer(tempSongSet.id), tempSongSet);
            mainDB.allSongSets[nSongSets] = tempSongSet;
            mainDB.nextSongSetID = Math.max(mainDB.nextSongSetID, tempSongSet.id+1);
            nSongSets++;
            tempSongSet = null;

        } else if (tempService != null && qName.equalsIgnoreCase("venue")) {
            tempService.venue = temp;
        } else if (tempService != null && qName.equalsIgnoreCase("comment")) {
            tempService.comment = temp;
        } else if (tempService != null && qName.equalsIgnoreCase("setid")) {
            tempService.sets = appendSongSet(tempService.sets, 
                    songSetLookup.get(new Integer(toInt(temp))));
        } else if (tempService != null && qName.equalsIgnoreCase("service")) {
            mainDB.allServices[nServices] = tempService;
            mainDB.nextServiceID = Math.max(mainDB.nextServiceID, tempService.id+1);
            nServices++;
            tempService = null;
        }
       
        openedItems++;
        if (pmon != null) {
            pmon.setProgress(openedItems);
        }
    }

    private void finalizeDB() throws SAXException {
        Arrays.sort(mainDB.allSongs);
        Arrays.sort(mainDB.allSongSets);
    }


}

