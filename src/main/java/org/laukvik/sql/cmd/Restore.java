package org.laukvik.sql.cmd;

import org.laukvik.sql.DatabaseConnection;
import org.laukvik.sql.DatabaseReadOnlyException;
import org.laukvik.sql.Importer;
import org.laukvik.sql.SQL;
import org.laukvik.sql.swing.BackupFormatFileFilter;

import java.io.File;

/**
 *
 *
 */
public class Restore extends SqlCommand {

    public Restore() {
        super("restore", "directory", "Restores database from the specified directory");
    }

    @Override
    public int run(DatabaseConnection db, String value) {
        File directory = new File(value);
        if (directory.exists()){

            try {
                Importer imp = new Importer(db);
                imp.importDirectory(directory);
                //restore(db,directory);
                return SUCCESS;
            } catch (DatabaseReadOnlyException e) {
                e.printStackTrace();
            }
        } else {

        }
        return EXCEPTION;
    }


}
