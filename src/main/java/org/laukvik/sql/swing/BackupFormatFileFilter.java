package org.laukvik.sql.swing;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by morten on 13.10.2015.
 */
public class BackupFormatFileFilter implements FilenameFilter {


    public final static String EXTENSION = ".csv";

    @Override
    public boolean accept(File dir, String name) {
        if (name == null){
            return false;
        }
        return name.toLowerCase().endsWith(EXTENSION);
    }
}
