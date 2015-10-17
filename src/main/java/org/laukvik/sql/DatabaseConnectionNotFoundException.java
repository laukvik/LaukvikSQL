package org.laukvik.sql;

/**
 * Created by morten on 07.10.2015.
 */
public class DatabaseConnectionNotFoundException extends Exception {

    public DatabaseConnectionNotFoundException(String name){
        super("Could not find configuration file for the named connection '" + name + "'!");
    }
}
