package org.laukvik.sql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by morten on 14.10.2015.
 */
public class Importer {

    private final static Logger LOG = Logger.getLogger(Importer.class.getName());
    private DatabaseConnection databaseConnection;

    public Importer(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public void importDatabase(File file){
        LOG.info("Importing database to file: " + file.getAbsolutePath() );
        System.out.println("Importing database to file: " + file.getAbsolutePath() );
        StringBuilder b = new StringBuilder();
        byte [] buffer = new byte[2048];
        try(FileInputStream in = new FileInputStream( file )) {
            in.read(buffer);
            b.append(buffer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
