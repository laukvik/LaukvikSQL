package org.laukvik.sql.cmd;

import org.laukvik.sql.Analyzer;
import org.laukvik.sql.DatabaseConnection;
import org.laukvik.sql.SQL;
import org.laukvik.sql.ddl.Function;

/**
 *
 *
 */
public class ListSystemFunctions extends SqlCommand {

    public ListSystemFunctions() {
        super("system", "displays all system functions");
    }

    @Override
    public int run(DatabaseConnection db, String value) {
        Analyzer a = new Analyzer();
        for (Function f : a.listSystemFunctions(db)){
            System.out.println(f.getName());
        }
        return 0;
    }
}
