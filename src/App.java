package harmonia;

import java.beans.*;
import java.io.*;
import java.util.concurrent.*;
import javax.swing.*;

public class App {

    public static final String APP_NAME = "Harmonia";
    public static final String APP_VERSION = "0.0.1";
    public static final String APP_DB_EXT = "xml";
    public static final String APP_ATTACH_EXT = "_files";
    public static final String APP_ATTACH_FILE_EXT = "dat";

    public static final boolean APP_DEBUG_SHOW_IDS = false;

    public static final Importer[] allImporters = {
        new ImporterEPCPraise(),
        new ImporterWorshipArchiveCom()
    };

    public static boolean initApp() {
        File dataPath = new File(Util.getDataPath());
        if (!dataPath.exists()) {
            dataPath.mkdirs();
        }
        if (!dataPath.exists() || !dataPath.isDirectory()) {
            JOptionPane.showMessageDialog(null, "Initialization Error",
                    "Cannot find or create data folder.", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        if (!initApp()) {
            System.exit(-1);
        }
        Database mainDB = DatabaseChooser.showDialog();
        if (mainDB != null) {
            DatabaseEditor editor = new DatabaseEditor(mainDB);
            editor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            editor.setVisible(true);
        }
    }

}

