package org.laukvik.sql;

/**
 * Created by morten on 19.10.2015.
 */
public class DatabaseReadOnlyException extends Exception {

    public DatabaseReadOnlyException(String connection) {
        super("Connection with name " + connection + " is readonly!");
    }
}
