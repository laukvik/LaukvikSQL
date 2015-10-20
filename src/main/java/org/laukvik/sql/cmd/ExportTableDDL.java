package org.laukvik.sql.cmd;

import org.laukvik.sql.Analyzer;
import org.laukvik.sql.DatabaseConnection;
import org.laukvik.sql.DatabaseExportFailedException;
import org.laukvik.sql.Exporter;
import org.laukvik.sql.ddl.Schema;
import org.laukvik.sql.ddl.Table;

import java.io.IOException;

/**
 *
 *
 */
public class ExportTableDDL extends SqlCommand {

    public ExportTableDDL() {
        super("tabledef", "Exports the table definition");
    }

    @Override
    public int run(DatabaseConnection db, String value) {

        Analyzer a = new Analyzer();
        try {
            Schema s = a.findSchema(null,db);
            for(Table t : s.getTables()){
                System.out.println( t.getDDL() );
            }

        } catch (IOException e) {
            e.printStackTrace();
            return EXCEPTION;
        }

        return SUCCESS;
    }
}
