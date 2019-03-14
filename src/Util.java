package harmonia;

import java.io.*;
import javax.swing.*;
import java.util.regex.*;

public class Util {

    public static String getLineSeparator() {
        return System.getProperty("line.separator");
    }

    public static String getCurrentPath() {
        return System.getProperty("user.dir");
    }

    public static String getProgramPath() {
        return System.getProperty("user.dir");
    }

    public static String getDataDirName() {
        return "Harmonia";
    }

    private static String dataPath = null;

    public static String getDataPath() {
        if (dataPath == null) {
            dataPath = (new JFileChooser()).getFileSystemView().getDefaultDirectory().getAbsolutePath() +
                    File.separator + getDataDirName();
        }
        return dataPath;
    }

    public static String getShortName(String path) {
        int start = path.lastIndexOf('/');
        if (start == -1) {
            start = path.lastIndexOf('\\');
        }
        if (start == -1) {
            start = 0;
        } else {
            start = start + 1;
        }
        int end = path.lastIndexOf('.');
        if (end == -1) {
            end = path.length()-1;
        }
        return path.substring(start, end);
    }

    public static String getExtension(String name) {
        String ext = null;
        int i = name.lastIndexOf('.');
        if (i > 0 &&  i < name.length() - 1) {
            ext = name.substring(i+1).toLowerCase();
        }
        return ext;
    }

    public static String sanitizeFilename(String name) {
        return sanitizeFilename(name, false);
    }

    public static String sanitizeFilename(String name, boolean deleteSpaces) {
        StringBuffer sanitized = new StringBuffer();
        name = name.toLowerCase();
        int i;
        char c;
        boolean skipWhitespace = false;
        for (i=0; i<name.length(); i++) {
            c = name.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                sanitized.append(c);
                skipWhitespace = false;
            } else if (c == '_') {
                sanitized.append(c);
                skipWhitespace = false;
            } else if (Character.isWhitespace(c) && !skipWhitespace) {
                if (!deleteSpaces) {
                    sanitized.append('_');
                }
                skipWhitespace = true;
            } else {
                // ignore
            }
        }
        return sanitized.toString();
    }

    public static String extractViaRegex(String regex, int group, String text) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        String str = null;
        if (m.find()) {
            str = m.group(group);
        }
        return str;
    }

    public static String[] appendToStringArray(String[] array, String str) {
        String[] newArray = new String[array.length+1];
        int i;
        for (i=0; i<array.length; i++) {
            newArray[i] = array[i];
        }
        newArray[newArray.length-1] = str;
        return newArray;
    }
    
}

