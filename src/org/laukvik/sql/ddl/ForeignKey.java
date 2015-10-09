package org.laukvik.sql.ddl;

/**
 * Created by morten on 09.10.2015.
 */
public class ForeignKey {

    private String table;
    private String column;

    public ForeignKey(String table, String column){
        this.column = column;
    }


}
