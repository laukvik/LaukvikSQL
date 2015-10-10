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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.laukvik.sql.SQL;
import org.laukvik.sql.ddl.Table;

/**
 *
 * @author Morten Laukvik <morten@laukvik.no>
 */
public class ResultSetTableModel implements TableModel {

    private final Table table;
    private final SQL sql;
    private final List<TableModelListener> listeners;
    private ResultSet rs;
    private int rowIndex;
    private int maxRows;
    private static final Logger LOG = Logger.getLogger(ResultSetTableModel.class.getName());

    public ResultSetTableModel(Table table, SQL sql) {
        this.table = table;
        this.sql = sql;
        this.listeners = new ArrayList<>();
        Connection c = sql.getConnection();
        Statement st;
        try {
            st = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
            try {
                rowIndex = 0;
                maxRows = 0;
                rs = st.executeQuery("SELECT count(*) FROM " + table.getName());

                if (rs.next()) {
                    maxRows = rs.getInt(1);
                    LOG.log(Level.FINE, "Found {0} in table {1}", new Object[]{maxRows, table.getName()});
                } else {
                    LOG.log(Level.FINE, "Could not find table {0}", table.getName());
                }

                rs = st.executeQuery("SELECT * FROM " + table.getName() + "");
                rs.next();
                rowIndex = 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ResultSetTableModel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public int getRowCount() {
        return maxRows;
    }

    @Override
    public int getColumnCount() {
        try {
            return rs.getMetaData().getColumnCount();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        try {
            return rs.getMetaData().getColumnLabel(columnIndex + 1);
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        try {
            return rs.getMetaData().getColumnClassName(columnIndex + 1).getClass();
        } catch (SQLException ex) {
            return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            if (this.rowIndex == rowIndex) {
                return rs.getObject(columnIndex + 1);
            } else {
                try {
                    if (rowIndex == 0){
                        rs.first();
                    } else {
                        rs.absolute(rowIndex);
                    }
                    this.rowIndex = rowIndex;
                    return rs.getObject(columnIndex + 1);
                } catch (SQLException e) {
                    System.err.println("Row: " + rowIndex + " Column: " + columnIndex + " Message: " + e.getMessage());
                }

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
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
