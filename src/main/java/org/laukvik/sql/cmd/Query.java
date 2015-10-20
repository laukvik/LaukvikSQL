package org.laukvik.sql.cmd;

import org.laukvik.sql.DatabaseConnection;
import org.laukvik.sql.Exporter;
import org.laukvik.sql.SQL;

/**
 * Lists all connections
 *
 */
public class Query extends SqlCommand {

    public Query() {
        super("query", "SQL", "displays all registered connections");
    }

    @Override
    public int run(DatabaseConnection db, String value) {
        Exporter exporter = new Exporter(db);
        exporter.listQuery(value);
        return SUCCESS;
    }
}
