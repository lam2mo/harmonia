package harmonia;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class TextBooleanCellEditor extends JPanel implements TableCellEditor, MouseListener {

    private static final JTable defaultTable = new JTable();

    private JLabel label;
    private boolean value;

    Set<CellEditorListener> listeners;

    public TextBooleanCellEditor() {
        super();
        label = new JLabel();
        label.addMouseListener(this);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        setBackground(defaultTable.getSelectionBackground());
        label.setForeground(defaultTable.getSelectionForeground());
        setLayout(new BorderLayout());
        add(label, BorderLayout.CENTER);
        addMouseListener(this);
        listeners = new HashSet<CellEditorListener>();
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.value = ((Boolean)value).booleanValue();
        label.setText(this.value ? "X" : "");
        return this;
    }

    public void addCellEditorListener(CellEditorListener l) {
        listeners.add(l);
    }

    public void cancelCellEditing() {
        fireEditingCanceled();
    }

    public Object getCellEditorValue() {
        return new Boolean(value);
    }

    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    public void removeCellEditorListener(CellEditorListener l) {
        listeners.remove(l);
    }

    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }

    public void fireEditingCanceled() {
        for (CellEditorListener l : listeners) {
            l.editingCanceled(new ChangeEvent(this));
        }
    }

    public void fireEditingStopped() {
        for (CellEditorListener l : listeners) {
            l.editingStopped(new ChangeEvent(this));
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            value = !value;
            fireEditingStopped();
        }
    }

    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }

}

