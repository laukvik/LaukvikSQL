package org.laukvik.sql.swing;


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class SqlTableHeaderRenderer extends JLabel implements TableCellRenderer {

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int rowIndex, int vColIndex) {
        setText(value.toString());
        setToolTipText((String) value);
        setHorizontalAlignment(CENTER);
        setBorder(BorderFactory.createEtchedBorder(1));
        return this;
    }

}
