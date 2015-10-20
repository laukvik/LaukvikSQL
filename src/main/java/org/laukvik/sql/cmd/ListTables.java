package org.laukvik.sql.cmd;

import org.laukvik.sql.Analyzer;
import org.laukvik.sql.DatabaseConnection;
import org.laukvik.sql.SQL;
import org.laukvik.sql.ddl.Function;
import org.laukvik.sql.ddl.Table;

/**
 *
 *
 */
public class ListTables extends SqlCommand {

    public ListTables() {
        super("tables", "displays all tables");
    }

    @Override
    public int run(DatabaseConnection db, String value) {
        Analyzer a = new Analyzer();
        for (Table t : a.findTables(db.getSchema(), db)){
            System.out.println(t.getName());
        }
        return 0;
    }
}
