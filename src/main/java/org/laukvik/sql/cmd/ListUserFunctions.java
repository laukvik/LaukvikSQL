package org.laukvik.sql.cmd;

import org.laukvik.sql.Analyzer;
import org.laukvik.sql.DatabaseConnection;
import org.laukvik.sql.SQL;
import org.laukvik.sql.ddl.Function;

/**
 * Lists all user functions
 *
 */
public class ListUserFunctions extends SqlCommand {

    public ListUserFunctions() {
        super("functions", "lists all user functions");
    }

    @Override
    public int run(DatabaseConnection db, String value) {
        Analyzer a = new Analyzer();
        for (Function f : a.findUserFunctions(db.getSchema(), db)){
            System.out.println( f.getName() );
        }
        return 0;
    }
}
