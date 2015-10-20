package org.laukvik.sql.ddl;

/**
 * Created by morten on 09.10.2015.
 */
public class ForeignKey {

    private String table;
    private String column;

    private ForeignKey(){
    }

    public ForeignKey(String table, String column){
        this.table = table;
        this.column = column;
    }

    public static ForeignKey parse( String value ){
        if (value == null || value.trim().isEmpty()){
            return null;
        }
        int firstIndex = value.indexOf("(");
        int lastIndex = value.indexOf(")", firstIndex);

        if (firstIndex == -1 || lastIndex == -1){
            return null;
        }

        String table = value.substring(0, firstIndex);
        String column = value.substring(firstIndex+1,lastIndex);

        if (table.trim().isEmpty()){
            return null;
        }
        if (column.trim().isEmpty()){
            return null;
        }

        ForeignKey fk = new ForeignKey();
        fk.table = table;
        fk.column = column;
        return fk;
    }

    public String getDDL(){
        return table + "(" + column + ")";
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ForeignKey that = (ForeignKey) o;

        if (table != null ? !table.equals(that.table) : that.table != null) return false;
        return !(column != null ? !column.equals(that.column) : that.column != null);

    }

    @Override
    public int hashCode() {
        int result = table != null ? table.hashCode() : 0;
        result = 31 * result + (column != null ? column.hashCode() : 0);
        return result;
    }
}
