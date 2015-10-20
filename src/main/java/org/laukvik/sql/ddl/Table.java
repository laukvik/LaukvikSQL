/*
 * Copyright (C) 2014 morten
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
package org.laukvik.sql.ddl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author morten
 */
public class Table implements Sqlable {

    private final String name;
    private final List<Column> columns;
    private Schema schema;

    public Table(String name) {
        if (name == null || name.trim().isEmpty()){
            throw new IllegalArgumentException("Table name cant be null or empty!");
        }
        this.name = name;
        columns = new ArrayList<>();
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setPrimaryKey(String columnName, boolean isEnabled) {
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public void addColumn(Column c) {
        c.setTable(this);
        columns.add(c);
    }

    public String getInsertSQL( ResultSet rs ) throws SQLException{
        StringBuilder b = new StringBuilder();
        b.append("INSERT INTO "+ name +"(");
        // Names
        int x=0;
        for (Column c: columns){
            b.append(x > 0 ? "," : "");
            b.append(c.getName());
            x++;
        }
        b.append(") VALUES (");
        // Values
        int z = 0;
        for (Column c : columns){
            b.append(z > 0 ? "," : "");

            Object value = rs.getObject(z + 1);

            if (rs.wasNull()){
                b.append("NULL");

            } else {
                b.append( c.getFormatted(value ) );
            }


            z++;
        }
        b.append(");");
        return b.toString();
    }

    public String getSelectTable(){
        return "SELECT * FROM " + name + " ORDER BY " + getColumns().get(0).getName() + " ASC";
    }

    public String getDDL() {
        StringBuilder b = new StringBuilder();
        b.append("CREATE TABLE ");
        b.append(name);
        b.append(" (");
        int x = 0;
        for (Column c : columns) {
            b.append(x > 0 ? "," : "");
            b.append("\n\t");
            b.append(c.getName());
            b.append("\t");
            b.append(c.getDDL());
            /*
            if (c.getForeignKey() == null){

            } else {
                b.append(" REFERENCES ");
                b.append( c.getForeignKey().getTable() );
                b.append("(");
                b.append( c.getForeignKey().getColumn() );
                b.append(")");
            }
            */
            /*if (c.getDefaultValue() != null){
                b.append(" DEFAULT '");
                b.append( c.getDefaultValue());
                b.append("'");
            }*/
            if (c.isAutoIncrement()){
                //b.append(" AUTOINCREMENT");
            }
            x++;
        }
        x=0;

        List<Column> primaryKeys = findPrimaryKeys();
        if (primaryKeys.isEmpty()){

        } else {
            b.append(",\n\tPRIMARY KEY(");
            for (Column c : primaryKeys) {
                if (x == 0){
                } else {
                    b.append(",");
                }
                b.append(c.getName());
                x++;
            }
            b.append(")");
        }




        b.append("\n");
        b.append(");");
        b.append("\n");
        return b.toString();
    }

    public List<Column> findPrimaryKeys(){
        List<Column> cols = new ArrayList<>();
        for (Column c : columns){
            if (c.isPrimaryKey()){
                cols.add(c);
            }
        }
        return cols;
    }

    public Column findColumnByName(String name) {
        for (Column c : columns){
            if (c.getName().equalsIgnoreCase(name)){
                return c;
            }
        }
        return null;
    }
}
