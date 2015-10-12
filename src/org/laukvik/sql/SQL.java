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

import org.laukvik.sql.ddl.*;
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
    private DatabaseConnection databaseConnection;
    private Schema schema;

    public SQL(){
        schema = null;
        connection = null;
        databaseConnection = null;
    }

    public void openConnectionByName(String name) throws DatabaseConnectionNotFoundException, IOException, SQLException {
        setDatabaseConnection(findConnectionByName(name));
    }

    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }

    public void setDatabaseConnection(DatabaseConnection databaseConnection) throws IOException, SQLException {
        this.databaseConnection = databaseConnection;
        connection = getConnection(databaseConnection);
        schema = findSchema("");
    }

    public Schema getSchema() {
        return schema;
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
        args = new String[]{"default"};
        if (args.length == 0) {
            /* Assume graphical application with no arguments */
            SQL sql = new SQL();
            SQL.openApplication( sql );

        } else if (args.length == 1){

            if (args[0].equalsIgnoreCase("-help")) {
                /*  Assume connection argument and open graphical application */
                SQL.listUsage();

            } else if (args[0].equalsIgnoreCase("-list")){
                /* List all connections */
                SQL.listConnections();
            } else {
                SQL sql = new SQL();
                DatabaseConnection db = null;
                try {
                    sql.openConnectionByName(args[0]);
                    SQL.openApplication(sql);
                } catch (DatabaseConnectionNotFoundException e) {
                    System.out.println("Can't find database connection with name '" + args[0] + "'.");
                } catch (SQLException e) {
                    System.out.println("Could not connect to database connection with name '" + args[0] + "'.");
                } catch (IOException e) {
                    System.out.println("Could not read connection settings with name '" + args[0] + "'.");
                }
            }
        } else  if (args.length == 2){
            String action = args[0];
            String namedConnection = args[1];
            SQL sql = new SQL();

            try {
                sql.openConnectionByName(namedConnection);

                if (args[0].equalsIgnoreCase("-tables")){
                    sql.listTables( sql.getSchema() );

                } else if (args[0].equalsIgnoreCase("-functions")){

                    sql.listFunctions(sql.getSchema());

                } else if (args[0].equalsIgnoreCase("-views")){

                    sql.listViews(sql.getSchema());
                } else if (args[0].equalsIgnoreCase("-export")){

                    sql.export(sql.getSchema());

                } else if (args[0].startsWith("-export=")){

                    String filename = args[0].split("=")[1];

                    sql.exportFile(sql.getSchema(), new File(filename));

                } else if (args[0].startsWith("-import=")){

                    String filename = args[0].split("=")[1];
                    sql.importFile(new File(filename));

                } else if (args[0].startsWith("-query=")){

                    sql.listQuery(args[0].split("=")[1]);
                }
            } catch (DatabaseConnectionNotFoundException e) {
                System.err.println("Can't find database connection with name '" + args[1] + "'.");
            } catch (SQLException e) {
                System.err.println("Could not connect to database connection with name '" + args[1] + "'. "  + e.getMessage());
            } catch (IOException e) {
                System.err.println("Could not read connection settings with name '" + args[1] + "'.");
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
        System.out.println("  -export=file       creates scripts file used to import in other databases");
        System.out.println("  -import=file       runs scrips from file");
    }

    /**
     *
     * @param file
     */
    public void importFile( File file ){
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

    public void exportFile( Schema schema, File file ){
        LOG.info("Exporting database to file: " + file.getAbsolutePath() );
        try(FileOutputStream out = new FileOutputStream( file )) {
            List<Table> tables = schema.getTables();
            for (int z=0; z<tables.size(); z++){
                Table table = tables.get(z);
                System.out.print(table.getName() + ":");
                out.write( table.getDDL().getBytes() );
                try (ResultSet rs = getConnection().createStatement().executeQuery("SELECT * FROM " + table.getName() )) {
                    int cols = rs.getMetaData().getColumnCount();
                    while (rs.next()) {
                        out.write( table.getInsertSQL(rs).getBytes() );
                        out.write("\n".getBytes());
                    }
                    System.out.print("#");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                System.out.println();
            }
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void export(Schema schema){
        List<Table> tables = schema.getTables();
        for (int z=0; z<tables.size(); z++){
            Table table = tables.get(z);
            System.out.println(table.getDDL() );
            try (ResultSet rs = getConnection().createStatement().executeQuery("SELECT * FROM " + table.getName() )) {
                int cols = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    System.out.println(table.getInsertSQL(rs));
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


    public void listTables(Schema schema){
        for (Table t : schema.getTables()){
            System.out.println(t.getName());
        }
    }

    public void listFunctions(Schema schema){
        for (Function f : schema.getFunctions()){
            System.out.println(f.getName());
        }
    }

    public void listViews(Schema schema){
        for (View v : schema.getViews()){
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

    public Schema findSchema(String name){
        Schema schema = new Schema(name);
        for (Table t : findTables(schema)){
            schema.addTable(t);
        }
        for (View v : findViews(schema)){
            schema.addView(v);
        }
        for (Function f : findUserFunctions(schema)){
            schema.addFunction(f);
        }

        for (Function f : listSystemFunctions()){
            schema.addSystemFunction(f);
        }
        for (Function f : listStringFunctions()){
            schema.addStringFunction(f);
        }
        for (Function f : listTimeDateFunctions()){
            schema.addTimeFunction(f);
        }
        for (Function f : listNumbericFunctions()){
            schema.addNumericFunction(f);
        }
        return schema;
    }

    /**
     * Find all tables
     *
     * @return
     */
    public List<Table> findTables(Schema schema) {
        //LOG.finest("Finding tables...");
        List<Table> tables = new ArrayList<>();
        try {
            DatabaseMetaData md = getConnection().getMetaData();
            /**
             * Find tables
             *
             */
            String[] types = {"TABLE"};
            try (ResultSet rs = md.getTables(null, null, "%", types);) {
                while (rs.next()) {
                    Table t = new Table(rs.getString(3));
                    LOG.fine("Table: " + t.getName());
                    tables.add(t);
                }
            } catch (SQLException e) {
            }
            /**
             * Column definitions
             *
             */
            for (Table t : tables) {
                ResultSet rs = md.getColumns(null, null, t.getName(), null);
                while (rs.next()) {
                    String columnName = rs.getString(4);
                    int columnType = rs.getInt(5);
                    int size = rs.getInt(7);
                    Column c = Column.parse(columnType, columnName);
                    c.setSize(size);
                    c.setComments( rs.getString("REMARKS"));
                    //c.setAutoGenerated(rs.getBoolean("IS_GENERATEDCOLUMN"));
                    //c.setAutoIncrement(rs.getBoolean("IS_AUTOINCREMENT"));
                    c.setAllowNulls(rs.getInt("NULLABLE") == 1);
                    String defValue = rs.getString("COLUMN_DEF");
                    c.setDefaultValue(rs.wasNull() ? null : defValue);
                    t.addColumn(c);

                    //LOG.info("Column: " + rs.getString(1) + " " + rs.getString(2) + " " + rs.getString(3) + " " + rs.getString(4) + " " + rs.getString(5) + " " + rs.getString(6) + " " + rs.getString(7) + " " + rs.getString(8));
                }
            }

            /**
             * Foreign Keys
             *
             */
            for (Table t : tables){
                ResultSet rs = md.getImportedKeys(null, null, t.getName());
                while (rs.next()) {
                    LOG.finest("Looking for foreign key for table '" + t.getName() + "': " + rs.getString("FKTABLE_NAME")+"."+rs.getString("FKCOLUMN_NAME") + " " + rs.getString("PKTABLE_NAME")+"."+rs.getString("PKCOLUMN_NAME"));
                    Column c = t.findColumnByName(rs.getString("FKCOLUMN_NAME"));
                    if (c == null){

                    } else {
                        c.setForeignKey( new ForeignKey( rs.getString("PKTABLE_NAME"), rs.getString("PKCOLUMN_NAME")));
                    }


                }
            }

            // Primary keys
            for (Table t : tables) {
                ResultSet rs = md.getPrimaryKeys(null, null, t.getName());
                while (rs.next()) {
                    LOG.fine("PrimaryKey: " + rs.getString(3) + " " + rs.getString(4));
                    Column c = t.findColumnByName(rs.getString(4));
                    if (c != null){
                        c.setPrimaryKey(true);
                    }
                    //System.out.println("Column: " + rs.getString(1) + " " + rs.getString(2) + " " + rs.getString(3) + " " + rs.getString(4) + " " + rs.getString(5) + " " + rs.getString(6) + " ");
                }
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

    public List<Function> findUserFunctions(Schema schema) {
        List<Function> list = new ArrayList<>();
        try {
            DatabaseMetaData dbmd = getConnection().getMetaData();
            String catalog = null;
            ResultSet rs = dbmd.getFunctions(catalog, null, "%");
            while (rs.next()) {
                LOG.info("Function: " + rs.getString(1));
                list.add(new Function(rs.getString(1)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<View> findViews(Schema schema) {
        List<View> list = new ArrayList<>();
        try {
            DatabaseMetaData dbmd = getConnection().getMetaData();
            String[] types = {"VIEW"};
            try (ResultSet rs = dbmd.getTables(null, null, "%", types);) {
                while (rs.next()) {
                    list.add(new View(rs.getString(3)));
                }
            } catch (SQLException e) {
                e.printStackTrace();
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

    /**
     *
     * http://docs.oracle.com/javase/7/docs/api/java/sql/DatabaseMetaData.html#getProcedureColumns(java.lang.String,%20java.lang.String,%20java.lang.String,%20java.lang.String)
     *
     * @param function
     * @return
     * @throws SQLException
     */
    public Function displayFunction(Function function) throws SQLException {
        // Gets the database metadata
        Connection conn = getConnection();
        DatabaseMetaData mtdt = conn.getMetaData();
        //System.out.println(mtdt.getProcedureTerm());
        //ResultSet rs = mtdt.getProcedures(conn.getCatalog(), "%", function.getName());

        ResultSet rs = mtdt.getProcedureColumns( null, null, function.getName(), null);
            /* Iterate all columns */
        while (rs.next()) {
            FunctionParameter p = new FunctionParameter(rs.getString("COLUMN_NAME"));
            p.setComments(rs.getString("REMARKS"));
            //System.out.println("\t" + p.getName() + " " + p.getDataType() + " " + p.getRemarks());
            function.addParameter(p);
            System.out.println( p.getName() );
        }
/*
        int numCols = rs.getMetaData().getColumnCount();
        for (int i = 1; i <= numCols; i++) {
            if (i > 1) {
                System.out.print(", ");
            }
            System.out.print(rs.getMetaData().getColumnLabel(i));
        }
        System.out.println("");
        while (rs.next()) {
            function.setComments( rs.getString("REMARKS"));
            for (int i = 1; i <= numCols; i++) {
                if (i > 1) {
                    System.out.print(", ");
                }
                System.out.print(rs.getString(i));
            }
            System.out.println("");
        }

        System.out.println("Comments: " + function.getComments());
*/
        return function;
    }

    /**
     * Opens a new Graphical application with the specified database connection
     *
     * @param sql
     */
    public static void openApplication( final SQL sql ){
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
