package harmonia;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;

public class DatabaseOpenXMLTask extends SwingWorker<Database, Object> {

    public static final String DONE_SUCCESS = "DatabaseOpenXMLTaskDoneSuccess";
    public static final String DONE_FAILURE = "DatabaseOpenXMLTaskDoneFailure";

    private Database db;
    private Component parent;
    private ActionListener callback;
    private boolean success;

    private int openedItems, totalItems;

    public DatabaseOpenXMLTask(Database db, ActionListener callback, Component parent) {
        this.db = db;
        this.parent = parent;
        this.callback = callback;
        this.success = false;
    }

    public Database doInBackground() {
        ProgressMonitor monitor = new ProgressMonitor(parent, 
                "Opening " + db.name, "",
                0, 100);
        monitor.setProgress(0);

        try {
            DatabaseXMLHandler.openXML(db, false, monitor);
		} catch (IOException ex) {
            System.out.println(ex.getMessage());
			ex.printStackTrace();
		}    

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

