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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.laukvik.sql.ddl.Column;
import org.laukvik.sql.ddl.DatabaseConnection;
import org.laukvik.sql.ddl.Function;
import org.laukvik.sql.ddl.Schema;
import org.laukvik.sql.ddl.Table;
import org.laukvik.sql.ddl.View;
import org.laukvik.sql.swing.DatabaseConnectionFileFilter;
import org.laukvik.sql.swing.Viewer;

/**
 * Helper class for database access
 *
 *
 * @author Morten Laukvik
 */
public class SQL {

    private final static Logger LOG = Logger.getLogger(SQL.class.getName());

    private Connection connection;
    private DatabaseConnection db;

    public SQL(){
        this.db = new DatabaseConnection();
    }

    public SQL(DatabaseConnection db) throws SQLException, IOException {
        this.db = db;
        connection = getConnection(db);
    }

    public void close(){
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Connection getConnection(DatabaseConnection db) throws SQLException, IOException {
        /* Read settings file */
        Properties p = new Properties();
        File f = new File( SQL.getConnectionsHome(),  db.getName() + ".properties");
        LOG.fine("Loading settings file " + f.getAbsolutePath());
        p.load(new FileInputStream(f));
        db.setUrl(p.getProperty("url"));
        db.setUser(p.getProperty("user"));
        db.setPassword(p.getProperty("password"));
        LOG.fine("URL: " + db.getUrl());
        LOG.fine("User: " + db.getUser());
        //LOG.info("Password: " + db.getPassword());
        Connection connection = DriverManager.getConnection(db.getUrl(), db.getUser(), db.getPassword());
        return connection;
    }

    public static void main(String[] args) {
        //args = new String[]{ "-query=\"SELECT * FROM Article\"","default"};

        if (args.length == 0) {
            /* Assume graphical application with no arguments */
            SQL.openApplication( new SQL() );

        } else if (args.length == 1){

            if (args[0].equalsIgnoreCase("-help")) {
                /*  Assume connection argument and open graphical application */
                SQL.listUsage();

            } else if (args[0].equalsIgnoreCase("-list")){
                /* List all connections */
                SQL.listConnections();
            } else {
                DatabaseConnection db = null;
                try {
                    db = SQL.findConnectionByName(args[0]);
                    SQL.openApplication(new SQL(db));
                } catch (DatabaseConnectionNotFoundException e) {
                    System.out.println("Can't find database connection with name '" + args[0] + "'.");
                } catch (SQLException e) {
                    System.out.println("Could not connect to database connection with name '" + args[0] + "'.");
                } catch (IOException e) {
                    System.out.println("Could not read connection settings with name '" + args[0] + "'.");
                }
            }
        } else  if (args.length == 2){
            DatabaseConnection db = null;
            try {
                db = SQL.findConnectionByName(args[1]);
                if (args[0].equalsIgnoreCase("-tables")){
                    SQL sql = new SQL(db);
                    sql.listTables();
                } else if (args[0].equalsIgnoreCase("-functions")){
                    SQL sql = new SQL(db);
                    sql.listFunctions();
                } else if (args[0].equalsIgnoreCase("-views")){
                    SQL sql = new SQL(db);
                    sql.listViews();
                } else if (args[0].equalsIgnoreCase("-export")){
                    SQL sql = new SQL(db);
                    sql.export();
                } else if (args[0].startsWith("-query=")){
                    SQL sql = new SQL(db);
                    sql.listQuery(args[0].split("=")[1]);
                }
            } catch (DatabaseConnectionNotFoundException e) {
                System.out.println("Can't find database connection with name '" + args[1] + "'.");
            } catch (SQLException e) {
                System.out.println("Could not connect to database connection with name '" + args[1] + "'.");
            } catch (IOException e) {
                System.out.println("Could not read connection settings with name '" + args[1] + "'.");
            }

        } else {
            listUsage();
        }
    }

    public static DatabaseConnection findConnectionByName( String name ) throws DatabaseConnectionNotFoundException {
        for (DatabaseConnection c : findDatabaseConnections()){
            LOG.fine("Found defined connection: " + c.getName());
            if (c.getName().equalsIgnoreCase(name )){
                return c;
            }
        }
        throw new DatabaseConnectionNotFoundException(name);
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
        System.out.println("  -exportfile        creates scripts file used to import in other databases");
    }

    public void exportFile( File file ){
        try(FileOutputStream out = new FileOutputStream( file )) {
            List<Table> tables = findTables();
            for (int z=0; z<tables.size(); z++){
                Table table = tables.get(z);
                out.write( table.getDDL().getBytes() );
                try (ResultSet rs = getConnection().createStatement().executeQuery("SELECT * FROM " + table.getName() )) {
                    int cols = rs.getMetaData().getColumnCount();
                    while (rs.next()) {
                        out.write( table.getInsertSQL(rs).getBytes() );

                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void export(){
        List<Table> tables = findTables();
        for (int z=0; z<tables.size(); z++){
            Table table = tables.get(z);



            System.out.println(table.getDDL() );
            try (ResultSet rs = getConnection().createStatement().executeQuery("SELECT * FROM " + table.getName() )) {
                int cols = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    //System.out.println(table.getInsertSQL(rs));

                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }

    public void listQuery(String query){
        try (ResultSet rs = getConnection().createStatement().executeQuery(query)) {
            int cols = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                for (int x=0; x<cols; x++){
                    System.out.print( x > 0 ? "," : "" );
                    System.out.print(rs.getObject(x + 1));
                }
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void listTables(){
        for (Table t : findTables()){
            System.out.println(t.getName());
        }
    }

    public void listFunctions(){
        for (Function f : findUserFunctions()){
            System.out.println(f.getName());
        }
    }

    public void listViews(){
        for (View v : findViews()){
            System.out.println(v.getName());
        }
    }

    public static void listConnections(){
        for (DatabaseConnection c : SQL.findDatabaseConnections()){
            System.out.println(c.getName());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public static File getConnectionsHome() {
        File home = new File(System.getProperty("user.home"), "org.laukvik.sql");
        LOG.finer("Connections home: " + home.getAbsolutePath());
        home.mkdir();
        return home;
    }

    public static List<DatabaseConnection> findDatabaseConnections() {
        File home = getConnectionsHome();
        List<DatabaseConnection> items = new ArrayList<>();
        for (File f : home.listFiles(new DatabaseConnectionFileFilter())) {
            DatabaseConnection db = new DatabaseConnection();
            String name = f.getName();
            db.setName( name.substring(0,name.length()-".properties".length()) );
            items.add(db);
        }
        return items;
    }

    public List<Table> findTables() {
        //LOG.finest("Finding tables...");
        List<Table> tables = new ArrayList<>();
        try {
            DatabaseMetaData md = getConnection().getMetaData();
            String[] types = {"TABLE"};
            try (ResultSet rs = md.getTables(null, null, "%", types);) {
                while (rs.next()) {
                    Table t = new Table(rs.getString(3));
                    LOG.fine("Table: " + t.getName());
                    tables.add(t);
                }
            } catch (SQLException e) {
            }
            // Column definitions
            for (Table t : tables) {
                ResultSet rs = md.getColumns(null, null, t.getName(), null);
                while (rs.next()) {
                    String columnName = rs.getString(4);
                    int columnType = rs.getInt(5);
                    int size = rs.getInt(7);
                    Column c = Column.parse(columnType, columnName);
                    c.setSize(size);

                    c.setAllowNulls(rs.getInt("NULLABLE") == 1);
                    t.addColumn(c);

                    //LOG.info("Column: " + t.getName() );
                    //System.out.println("Column: " + rs.getString(1) + " " + rs.getString(2) + " " + rs.getString(3) + " " + rs.getString(4) + " " + rs.getString(5) + " " + rs.getString(6) + " " + rs.getString(7) + " " + rs.getString(8));
                }
            }
            // Attributes
            //for (Table t : tables) {
            {
                ResultSet rs = md.getAttributes(null,null,null,null);
                while (rs.next()) {
                    System.out.println("Attribute: " + rs.getString(1) + " " + rs.getString(2) + " " + rs.getString(3) + " " + rs.getString(4) + " " + rs.getString(5) + " " + rs.getString(6) + " " + rs.getString(7) + " " + rs.getString(8));
                }
            }
//            }



            // Primary keys
            for (Table t : tables) {
                ResultSet rs = md.getPrimaryKeys(null, null, t.getName());
                while (rs.next()) {
                    t.setPrimaryKey(rs.getString(3), true);
                    //System.out.println("Column: " + rs.getString(1) + " " + rs.getString(2) + " " + rs.getString(3) + " " + rs.getString(4) + " " + rs.getString(5) + " " + rs.getString(6) + " ");
                }
                //System.out.println(t.getDDL());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    public List<Schema> findSchemas() {
        List<Schema> list = new ArrayList<>();
        try {
            DatabaseMetaData dbmd = getConnection().getMetaData();

            try (ResultSet rs = dbmd.getSchemas()) {
                while (rs.next()) {
                    list.add(new Schema(rs.getString(3)));
                }
            } catch (SQLException e) {
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Function> findUserFunctions() {
        List<Function> list = new ArrayList<>();
        try {
            DatabaseMetaData dbmd = getConnection().getMetaData();
            String catalog = null;
            ResultSet rs = dbmd.getFunctions(catalog, null, "%");
            while (rs.next()) {
                LOG.info("Function: " + rs.getString(1));
                list.add(new Function(rs.getString(1)));
            }
            //list.add(new Function("asd"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<View> findViews() {
        List<View> list = new ArrayList<>();
        try {
            DatabaseMetaData dbmd = getConnection().getMetaData();
            String[] types = {"VIEW"};
            try (ResultSet rs = dbmd.getTables(null, null, "%", types);) {
                while (rs.next()) {
                    list.add(new View(rs.getString(3)));
                }
            } catch (SQLException e) {
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Returns all String functions
     *
     * @return
     */
    public List<Function> listStringFunctions() {
        List<Function> items = new ArrayList<>();
        try {
            DatabaseMetaData dbmd = getConnection().getMetaData();
            String[] arr = dbmd.getStringFunctions().split(",\\s*");
            for (String f : arr) {
                items.add(new Function(f));
            }
        } catch (SQLException e) {

        }
        return items;
    }

    public List<Function> listNumbericFunctions() {
        List<Function> items = new ArrayList<>();
        try {
            DatabaseMetaData dbmd = getConnection().getMetaData();
            String[] arr = dbmd.getNumericFunctions().split(",\\s*");
            for (String f : arr) {
                items.add(new Function(f));
            }
        } catch (SQLException e) {

        }
        return items;
    }

    public List<Function> listSystemFunctions() {
        List<Function> items = new ArrayList<>();
        try {
            DatabaseMetaData dbmd = getConnection().getMetaData();
            String[] arr = dbmd.getSystemFunctions().split(",\\s*");
            for (String f : arr) {
                items.add(new Function(f));
            }
        } catch (SQLException e) {

        }
        return items;
    }

    public List<Function> listTimeDateFunctions() {
        List<Function> items = new ArrayList<>();
        try {
            DatabaseMetaData dbmd = getConnection().getMetaData();
            String[] arr = dbmd.getTimeDateFunctions().split(",\\s*");
            for (String f : arr) {
                items.add(new Function(f));
            }
        } catch (SQLException e) {

        }
        return items;
    }

    public Function displayFunction(Function function) throws SQLException {
        // Gets the database metadata
        Connection conn = getConnection();
        DatabaseMetaData mtdt = conn.getMetaData();
        System.out.println(mtdt.getProcedureTerm());
        ResultSet rs = mtdt.getProcedures(conn.getCatalog(), "%", function.getName());

        ResultSetMetaData rsmd = rs.getMetaData();
        int numCols = rsmd.getColumnCount();
        for (int i = 1; i <= numCols; i++) {
            if (i > 1) {
                System.out.print(", ");
            }
            System.out.print(rsmd.getColumnLabel(i));
        }
        System.out.println("");
        while (rs.next()) {
            for (int i = 1; i <= numCols; i++) {
                if (i > 1) {
                    System.out.print(", ");
                }
                System.out.print(rs.getString(i));
            }
            System.out.println("");
        }
        return function;
    }

    /**
     * Opens a new Graphical application with the specified database connection
     *
     * @param sql
     */
    public static void openApplication( SQL sql ){
        try {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            Logger.getLogger(SQL.class.getName()).log(Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Viewer(sql).setVisible(true);
            }
        });
    }

}
