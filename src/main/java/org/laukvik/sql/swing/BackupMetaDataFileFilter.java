package org.laukvik.sql.swing;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 *
 */
public class BackupMetaDataFileFilter implements FilenameFilter {


    public final static String EXTENSION = ".meta.csv";

    @Override
    public boolean accept(File dir, String name) {
        if (name == null){
            return false;
        }
        return name.toLowerCase().endsWith(EXTENSION);
    }

    public static String getName( File file ){
        if (file == null){
            return null;
        } else if(file.getName().toLowerCase().endsWith(EXTENSION)){
            return file.getName().substring(0, file.getName().length() - EXTENSION.length() );
        } else {
            return null;
        }
    }
}
