/*
 * Copyright (C) 2015 Morten Laukvik <morten@laukvik.no>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.laukvik.sql.swing;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.laukvik.sql.ddl.Column;
import org.laukvik.sql.ddl.Table;

/**
 *
 * @author Morten Laukvik <morten@laukvik.no>
 */
public class TableDefinitionTableModel implements TableModel {

    private final Table table;
    private final List<TableModelListener> listeners;

    public TableDefinitionTableModel(Table table) {
        this.table = table;
        listeners = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return table.getColumns().size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Null";
            case 1:
                return "Name";
            case 2:
                return "Type";
            case 3:
                return "Size";
        }
        return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 3) {
            return Integer.class;
        } else if (columnIndex == 0) {
            return Boolean.class;
        }
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Column c = table.getColumns().get(rowIndex);
        switch (columnIndex) {
            case 0:
                return c.isAllowNulls();
            case 1:
                return c.getName();
            case 2:
                return c.getColumnName();
            case 3:
                return c.getSize();
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }

}
