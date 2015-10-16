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
import java.sql.SQLException;
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
            /* Show usage */
            SQL.listUsage();
            System.exit(1);
        } else if (args.length == 1){
            String option = args[0];

            if (option.equalsIgnoreCase("-help")) {
                /*  Assume connection argument and open graphical application */
                SQL.listUsage();

            } else if (option.equalsIgnoreCase("-list")){
                /* List all connections */
                SQL.listConnections();
            } else {
                SQL.listUsage();
            }
        } else  if (args.length == 2){
            // Find parameters
            String namedConnection = args[0];
            String option = args[1];
            try {
                // Find named connection before continuing
                DatabaseConnection db = DatabaseConnection.read(namedConnection);
                // Check arguments
                if (option.equalsIgnoreCase("-tables")){
                    Analyzer a = new Analyzer();
                    SQL.listTables( db );

                } else if (option.equalsIgnoreCase("-functions")){
                    SQL.listFunctions( db );

                } else if (option.equalsIgnoreCase("-views")){
                    SQL.listViews( db.getSchema(), db);

                } else if (option.equalsIgnoreCase("-exportTableCSV")){

                    Exporter exporter = new Exporter(db);
                    try {
                        exporter.export2( db.getSchema() );
                    } catch (DatabaseExportFailedException e) {
                        System.out.println(e.getMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else if (option.startsWith("-function=")){
                    String functionName = option.split("=")[1];
                    SQL.displayFunction(functionName,db);

                } else if (option.startsWith("-exportTableCSV=")){
                    String filename = option.split("=")[1];
                    SQL.exportFile(db, new File(filename));

                } else if (option.startsWith("-import=")){
                    String filename = args[0].split("=")[1];
                    Importer importer = new Importer(db);
                    importer.importDatabase(new File(filename));

                } else if (option.startsWith("-query=")){
                    SQL.listQuery(db, option.split("=")[1]);

                } else if (option.startsWith("-app")){
                    SQL.openApplication(db);

                } else if (option.startsWith("-system")){
                    SQL.listSystemFunctions(db);

                } else if (option.startsWith("-string")){
                    SQL.listStringFunctions(db);

                } else if (option.startsWith("-date")){
                    SQL.listDateTimeFunctions(db);

                } else if (option.startsWith("-numeric")){
                    SQL.listNumericFunctions(db);

                } else if (option.startsWith("-backup")){
                    SQL.backup(db);

                } else if (option.startsWith("-restore")){
                    SQL.restore(db);

                } else {
                    SQL.listUsage();
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

    /**
     *
     * Export
     *
     * exportTableCSV=csv
     * exportTableCSV=sql
     * exportTableCSV=table1,table2
     *
     * -backup=table1,table2
     *
     */
    public static void listUsage(){
        System.out.println("Usage: sql connection [option]");
        System.out.println("  -list              displays all connections registered");
        System.out.println("  -tables            displays all tables");
        System.out.println("  -functions         displays all user functions");
        System.out.println("  -numeric           displays all numeric functions");
        System.out.println("  -string            displays all string functions");
        System.out.println("  -date              displays all user functions");
        System.out.println("  -system            displays all system functions");
        System.out.println("  -function=<value>  display details about function");
        System.out.println("  -views             displays all views");
        System.out.println("  -query=<value>     runs the query and displays the results");
        System.out.println("  -exportTableCSV            creates scripts used to import in other databases");
        System.out.println("  -exportTableCSV=file       creates scripts file used to import in other databases");
        System.out.println("  -import=file       runs scrips from file");
    }

    public static void exportFile(DatabaseConnection db, File file) {
        Exporter exporter = new Exporter(db);
    }

    public static void displayFunction(String functionName, DatabaseConnection db) {
        Analyzer a = new Analyzer();
        Function f = new Function( functionName );
        try {
            f = a.findFunctionDetails(f,db);
            System.out.println( f.getDetails() );
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            displayFunction(f);
        }
    }

    private static void displayFunction(Function f){
        System.out.print(f.getName());
        System.out.print(",");
        System.out.print("[");
        int x=0;
        for (FunctionParameter fp : f.getParameters()){
            if (x>0){
                System.out.print(",");
            }
            System.out.print( fp.getName());
            x++;
        }

        System.out.print("]");
        System.out.println();
    }

    public static void listSystemFunctions(DatabaseConnection db){
        Analyzer a = new Analyzer();
        for (Function f : a.listSystemFunctions(db)){
            displayFunction(f);
        }
    }

    public static void listStringFunctions(DatabaseConnection db){
        Analyzer a = new Analyzer();
        for (Function f : a.listStringFunctions(db)){
            displayFunction(f);
        }
    }

    public static void listDateTimeFunctions(DatabaseConnection db){
        Analyzer a = new Analyzer();
        for (Function f : a.listTimeDateFunctions(db)){
            displayFunction(f);
        }
    }

    public static void listNumericFunctions(DatabaseConnection db){
        Analyzer a = new Analyzer();
        for (Function f : a.listNumbericFunctions(db)){
            displayFunction(f);
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

    public static void backup(DatabaseConnection db, String... tables){
        Exporter exporter = new Exporter(db);
        exporter.backup(tables);
    }

    public static void restore(DatabaseConnection db, String... tables) {
        Importer imp = new Importer(db);
        try {
            if (tables == null){

            }
            imp.importCSV(null, "Activity");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
