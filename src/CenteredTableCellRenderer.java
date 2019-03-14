package harmonia;

import javax.swing.*;
import javax.swing.table.*;

public class CenteredTableCellRenderer extends DefaultTableCellRenderer {

    public CenteredTableCellRenderer () {
        super();
        setHorizontalAlignment(SwingConstants.CENTER);
    }

}

