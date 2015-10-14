package org.laukvik.sql;

/**
 * Created by morten on 07.10.2015.
 */
public class DatabaseConnectionInvalidException extends Exception {

    public DatabaseConnectionInvalidException(String name){
        super("The configuration file for the named connection '" + name + "' is invalid.");
    }
}
