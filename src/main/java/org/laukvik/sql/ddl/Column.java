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


    public final static int TYPE_BIT                = -7;
    public final static int TYPE_TINYINT            = -6;
    public final static int TYPE_BIGINT             = -5;
    public final static int TYPE_LONGVARBINARY      = -4;
    public final static int TYPE_VARBINARY          = -3;
    public final static int TYPE_BINARY             = -2;
    public final static int TYPE_LONGVARCHAR        = -1;

    public final static int TYPE_CHAR               = 1;
    public final static int TYPE_NUMERIC            = 2;
    public final static int TYPE_DECIMAL            = 3;
    public final static int TYPE_INTEGER            = 4;
    public final static int TYPE_SMALLINT           = 5;
    public final static int TYPE_FLOAT              = 6;
    public final static int TYPE_REAL               = 7;
    public final static int TYPE_DOUBLE             = 8;
    public final static int TYPE_VARCHAR            = 12;

    public final static int TYPE_DATE               = 91;
    public final static int TYPE_TIME               = 92;
    public final static int TYPE_TIMESTAMP          = 93;
    public final static int TYPE_OTHER              = 1111;


    private String name;
    private boolean allowNulls;
    private Table table;
    private int size;
    private String comments;
    private boolean primaryKey;
    private ForeignKey foreignKey;
    private boolean autoIncrement;
    private boolean autoGenerated;
    private String defaultValue;
    private String format;


    public Column(String name) {
        if (name == null || name.trim().isEmpty()){
            throw new IllegalArgumentException("Column name cant be null or empty!");
        }
        this.name = name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        if (defaultValue == null){

        } else if (defaultValue.isEmpty()){
            this.defaultValue = null;
        }
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public boolean isAutoGenerated() {
        return autoGenerated;
    }

    public void setAutoGenerated(boolean autoGenerated) {
        this.autoGenerated = autoGenerated;
    }

    public ForeignKey getForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(ForeignKey foreignKey) {
        this.foreignKey = foreignKey;
    }

    public String getComments() {
        return comments;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public abstract int getType();

    public static Column parse(int columnType, String name) {
        switch (columnType) {
            case TYPE_BIT:
                return new BooleanColumn(name);
            case TYPE_TINYINT:
                return new TinyIntColumn(name);
            case TYPE_BIGINT:
                return new BigIntColumn(name);
            case TYPE_LONGVARBINARY:
                return new LongVarBinaryColumn(name);
            case TYPE_VARBINARY:
                return new VarBinaryColumn(name);
            case TYPE_BINARY:
                return new BinaryColumn(name);
            case TYPE_LONGVARCHAR:
                return new LongVarCharColumn(name);
            case TYPE_CHAR:
                return new CharColumn(name);
            case TYPE_NUMERIC:
                return new NumericColumn(name);
            case TYPE_DECIMAL:
                return new DecimalColumn(name);
            case TYPE_INTEGER:
                return new IntegerColumn(name);
            case TYPE_SMALLINT:
                return new SmallIntColumn(name);
            case TYPE_FLOAT:
                return new FloatColumn(name);
            case TYPE_REAL:
                return new RealColumn(name);
            case TYPE_DOUBLE:
                return new DoubleColumn(name);
            case TYPE_VARCHAR:
                return new VarCharColumn(name);
            case TYPE_DATE:
                return new DateColumn(name);
            case TYPE_TIME:
                return new TimeColumn(name);
            case TYPE_TIMESTAMP:
                return new TimestampColumn(name);
            case TYPE_OTHER:
                return new OtherColumn(name);
        }
        throw new IllegalArgumentException("ColumnType: " + columnType);
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the name of the column
     *
     * @return
     */
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
        return getColumnName() + "" + (allowNulls ? " NULL" : " NOT NULL");
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Returns the SQL formatted value of the object
     *
     * @param value
     * @return
     */
    public String getFormatted( Object value ){
        return value.toString();
    }


    public int index(){
        return table.getColumns().indexOf(this);
    }
}
