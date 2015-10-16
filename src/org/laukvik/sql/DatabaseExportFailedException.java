package org.laukvik.sql;

/**
 * Created by morten on 14.10.2015.
 */
public class DatabaseExportFailedException extends Exception{

    public DatabaseExportFailedException(Exception e, DatabaseConnection db) {
        super("Failed to exportTableCSV database: " + db.getFilename());
        setStackTrace(e.getStackTrace());
    }
}
