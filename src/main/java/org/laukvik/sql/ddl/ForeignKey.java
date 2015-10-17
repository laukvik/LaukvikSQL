package org.laukvik.sql.ddl;

/**
 * Created by morten on 09.10.2015.
 */
public class ForeignKey {

    private String table;
    private String column;

    public ForeignKey(String table, String column){
        this.table = table;
        this.column = column;
    }

    private ForeignKey(){

    }

    public static ForeignKey parse( String value ){
        if (value == null || value.trim().isEmpty()){
            return null;
        }
        if (value.contains("(") || value.contains(")")){
            return null;
        } else {
            ForeignKey fk = new ForeignKey();
            fk.table = value.split("\\(")[0];
            fk.column = value.split("\\(")[1].split("\\)")[0];
            return fk;
        }
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }
}
