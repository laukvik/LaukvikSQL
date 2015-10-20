package org.laukvik.sql.cmd;

import org.laukvik.sql.DatabaseConnection;
import org.laukvik.sql.DatabaseReadOnlyException;
import org.laukvik.sql.Importer;
import org.laukvik.sql.SQL;

/**
 *
 *
 */
public class Import extends SqlCommand {

    public Import() {
        super("import", "Imports the database from file");
    }

    @Override
    public int run(DatabaseConnection db, String value) {
        try {
            Importer importer = new Importer(db);
            return SUCCESS;
        } catch (DatabaseReadOnlyException e) {
            e.printStackTrace();
        }

        return EXCEPTION;
    }
}
