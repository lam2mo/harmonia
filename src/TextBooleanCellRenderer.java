package harmonia;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class TextBooleanCellRenderer extends DefaultTableCellRenderer {

    public TextBooleanCellRenderer () {
        super();
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, 
            boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        ((JLabel)c).setText(((Boolean)value).booleanValue() ? "X" : "");
        return c;
    }

}

