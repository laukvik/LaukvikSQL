package org.laukvik.sql.cmd;

import org.laukvik.csv.CSV;
import org.laukvik.sql.Analyzer;
import org.laukvik.sql.DatabaseConnection;
import org.laukvik.sql.Exporter;
import org.laukvik.sql.ddl.Schema;
import org.laukvik.sql.ddl.Table;

import java.io.IOException;
import java.util.List;

/**
 *
 *
 */
public class ExportTable extends SqlCommand {

    public ExportTable() {
        super("meta","table", "Exports the table definition");
    }

    @Override
    public int run(DatabaseConnection db, String tableName) {

        Analyzer a = new Analyzer();
        List<Table> tables = a.findTables(null,db);
        Table table = null;
        for (Table t : tables){
            if (t.getName().equalsIgnoreCase(tableName)){
                table = t;
            }
        }

        Exporter exporter = new Exporter( db );
        CSV csv = exporter.createMetaDataCSV(table);


        return SUCCESS;
    }
}
