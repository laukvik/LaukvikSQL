/*
 * Copyright (C) 2014 morten
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.laukvik.sql;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.laukvik.sql.ddl.*;
import org.laukvik.sql.swing.DatabaseConnectionFileFilter;
import org.laukvik.sql.swing.Viewer;

/**
 * Command line app
 *
 * Database
 * - Catalog
 *   - Schema
 * @author Morten Laukvik
 */
public class SQL {

    private final static Logger LOG = Logger.getLogger(SQL.class.getName());

    public static void main(String[] args) {
        if (args.length == 0) {
            /* Assume graphical application with no arguments */
            SQL sql = new SQL();
            SQL.openApplication( null );

        } else if (args.length == 1){
            String action = args[0];

            if (action.equalsIgnoreCase("-help")) {
                /*  Assume connection argument and open graphical application */
                SQL.listUsage();

            } else if (action.equalsIgnoreCase("-list")){
                /* List all connections */
                SQL.listConnections();
            } else {

                String namedConnection = action;
                DatabaseConnection db = null;
                try {
                    db = DatabaseConnection.read(namedConnection);
                    SQL.openApplication(db);
                } catch (DatabaseConnectionNotFoundException e) {
                    System.out.println( e.getMessage() );
                } catch (DatabaseConnectionInvalidException e) {
                    System.out.println(e.getMessage());
                }
            }
        } else  if (args.length == 2){
            // Find parameters
            String action = args[0];
            String namedConnection = args[1];
            try {
                // Find named connection before continuing
                DatabaseConnection db = DatabaseConnection.read(namedConnection);
                // Check arguments
                if (action.equalsIgnoreCase("-tables")){
                    Analyzer a = new Analyzer();

                    SQL.listTables( db );

                } else if (action.equalsIgnoreCase("-functions")){

                    SQL.listFunctions( db );

                } else if (action.equalsIgnoreCase("-views")){

                    SQL.listViews( db.getSchema(), db);

                } else if (action.equalsIgnoreCase("-export")){

                    Exporter exporter = new Exporter(db);
                    try {
                        exporter.export2( db.getSchema() );
                    } catch (DatabaseExportFailedException e) {
                        System.out.println(e.getMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else if (action.startsWith("-export=")){

                    String filename = action.split("=")[1];

                    SQL.exportFile(db, new File(filename));

                } else if (action.startsWith("-import=")){

                    String filename = args[0].split("=")[1];
                    Importer importer = new Importer(db);
                    importer.importDatabase(new File(filename));

                } else if (action.startsWith("-query=")){

                    SQL.listQuery(db, action.split("=")[1]);
                }
            } catch (DatabaseConnectionInvalidException e) {
                System.out.println( e.getMessage() );
            } catch (DatabaseConnectionNotFoundException e) {
                System.out.println(e.getMessage());
            }
        } else {
            listUsage();
        }
    }

    public static void listUsage(){
        System.out.println("Usage: sql <option> connection");
        System.out.println("  -list              displays all connections registered");
        System.out.println("  -tables            displays all tables");
        System.out.println("  -functions         displays all user functions");
        System.out.println("  -views             displays all views");
        System.out.println("  -system            displays all system functions");
        System.out.println("  -query=<COMMAND>   runs the query and displays the results");
        System.out.println("  -export            creates scripts used to import in other databases");
        System.out.println("  -export=file       creates scripts file used to import in other databases");
        System.out.println("  -import=file       runs scrips from file");
    }

    public static void exportFile(DatabaseConnection db, File file) {
        Exporter exporter = new Exporter(db);
    }

    public static void listQuery(DatabaseConnection db, String query) {
        Exporter exporter = new Exporter(db);
        exporter.listQuery(query);
    }

    public static void listTables(DatabaseConnection db){
        Analyzer a = new Analyzer();
        for (Table t : a.findTables(db.getSchema(), db)){
            System.out.println(t.getName());
        }
    }

    public static void listFunctions(DatabaseConnection db){
        Analyzer a = new Analyzer();
        for (Function f : a.findUserFunctions(db.getSchema(), db )){
            System.out.println(f.getName());
        }
    }

    public static void listViews(String schema, DatabaseConnection db){
        Analyzer a = new Analyzer();
        for (View v : a.findViews(schema, db)){
            System.out.println(v.getName());
        }
    }

    public static void listConnections(){
        for (DatabaseConnection c : SQL.findDatabaseConnections()){
            System.out.println(c.getFilename());
        }
    }

    public static File getLibraryHome() {
        File home = new File(System.getProperty("user.home"), "Library");
        if (!home.exists()){
            home.mkdir();
        }
        return home;
    }

    public static File getConnectionsHome() {
        File home = new File(getLibraryHome(), "org.laukvik.sql");
        if (!home.exists()){
            home.mkdir();
        }
        return home;
    }

    public static List<DatabaseConnection> findDatabaseConnections() {
        File home = getConnectionsHome();
        List<DatabaseConnection> items = new ArrayList<>();
        for (File f : home.listFiles(new DatabaseConnectionFileFilter())) {
            DatabaseConnection db = new DatabaseConnection();
            String name = f.getName();
            db.setFilename(name.substring(0, name.length() - ".properties".length()));
            items.add(db);
        }
        return items;
    }

    /**
     * Opens a new Graphical application with the specified database connection
     *
     * @param db
     */
    public static void openApplication( final DatabaseConnection db ){
        LOG.info("Opening Swing application for database '" + (db == null ? "" : db.getFilename()) + "'");
        try {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            LOG.warning("Could not set system look and feel.");
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Viewer v = new Viewer();
                if (db != null){
                    v.setDatabaseConnection(db);
                }
                v.setVisible(true);

            }
        });
    }

}
