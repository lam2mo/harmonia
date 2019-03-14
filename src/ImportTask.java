package harmonia;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;

public class ImportTask extends SwingWorker<Database, Object> {

    public static final String DONE_SUCCESS = "ImportTaskDoneSuccess";
    public static final String DONE_FAILURE = "ImportTaskDoneFailure";

    private Database db;
    private Importer imp;
    private Component parent;
    private ActionListener callback;
    private boolean success;

    public ImportTask(Database db, Importer imp, ActionListener callback, Component parent) {
        this.db = db;
        this.imp = imp;
        this.parent = parent;
        this.callback = callback;
        this.success = false;
    }

    public Database doInBackground() {
        ProgressMonitor monitor = new ProgressMonitor(parent, 
                "Importing " + imp.toString() + " into " + db.name + " ...", "",
                0, 100);
        monitor.setProgress(0);
        int importedItems = 0, totalItems = 0;

        imp.init(db);
        totalItems = imp.estimateSongsRemaining();
        monitor.setMaximum(totalItems);

        Song s = null;
        while ((s = imp.readSong()) != null) {
            db.addNewSong(s);
            monitor.setProgress(++importedItems);
            monitor.setNote(imp.getNote());
        }

        imp.cleanup();
        monitor.setProgress(monitor.getMaximum());

        if (!isCancelled()) {
            success = true;
        }

        return db;
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

