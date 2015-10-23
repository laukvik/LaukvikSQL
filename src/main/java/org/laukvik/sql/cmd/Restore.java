package org.laukvik.sql.cmd;

import org.laukvik.sql.DatabaseConnection;
import org.laukvik.sql.DatabaseReadOnlyException;
import org.laukvik.sql.Importer;

import java.io.File;
import java.nio.charset.Charset;

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
                /* @todo - Add support for encoding */
                imp.importDirectory(directory, Charset.forName("iso-8859-1"));
                return SUCCESS;
            } catch (DatabaseReadOnlyException e) {
                System.out.println("Connection is read only!");
            }
        }
        return EXCEPTION;
    }

}