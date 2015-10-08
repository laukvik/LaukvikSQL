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
package org.laukvik.sql.ddl;

/**
 *
 * @author Morten Laukvik <morten@laukvik.no>
 */
public abstract class Column {

    /**
     * -7	BIT -6	TINYINT -5	BIGINT -4	LONGVARBINARY -3	VARBINARY -2	BINARY -1
     * LONGVARCHAR 0	NULL 1	CHAR 2	NUMERIC 3	DECIMAL 4	INTEGER 5	SMALLINT 6
     * FLOAT 7	REAL 8	DOUBLE 12	VARCHAR 91	DATE 92	TIME 93	TIMESTAMP 1111 OTHER
     */
    private String name;
    private boolean allowNulls;
    private Table table;
    private int size;

    public Column(String name) {
        this.name = name;
    }

    public static Column parse(int columnType, String name) {
        switch (columnType) {
            case -7:
                return new BitColumn(name);
            case -6:
                return new TinyIntColumn(name);
            case -5:
                return new BigIntColumn(name);
            case -4:
                return new LongVarBinaryColumn(name);
            case -3:
                return new VarBinaryColumn(name);
            case -2:
                return new BinaryColumn(name);
            case -1:
                return new LongVarCharColumn(name);
            case 1:
                return new CharColumn(name);
            case 2:
                return new NumericColumn(name);
            case 3:
                return new DecimalColumn(name);
            case 4:
                return new IntegerColumn(name);
            case 5:
                return new SmallIntColumn(name);
            case 6:
                return new FloatColumn(name);
            case 7:
                return new RealColumn(name);
            case 8:
                return new DoubleColumn(name);
            case 12:
                return new VarCharColumn(name);
            case 91:
                return new DateColumn(name);
            case 92:
                return new TimeColumn(name);
            case 93:
                return new TimestampColumn(name);
            case 1111:
                return new OtherColumn(name);
        }
        throw new IllegalArgumentException("ColumnType: " + columnType);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public boolean isAllowNulls() {
        return allowNulls;
    }

    public void setAllowNulls(boolean allowNulls) {
        this.allowNulls = allowNulls;
    }

    public String getColumnName() {
        String name = this.getClass().getSimpleName();
        return name.substring(0, name.length() - "Column".length()).toUpperCase();
    }

    public String getDDL() {
        return getColumnName() + " (" + size + ")" + (allowNulls ? "" : " NOT NULL");
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

}
