package org.laukvik.sql;

import org.laukvik.sql.ddl.*;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by morten on 14.10.2015.
 */
public class Analyzer {

    private final static Logger LOG = Logger.getLogger(Analyzer.class.getName());

    public Analyzer() {
    }

    /**
     * Find all schemas in database
     *
     * @return
     */
    public List<Schema> findSchemas( DatabaseConnection db ) throws IOException {
        List<Schema> list = new ArrayList<>();
        try {
            DatabaseMetaData dbmd = db.getConnection().getMetaData();
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

    public Schema findDefaultSchema(DatabaseConnection db) throws IOException {
        return findSchema(null,db);
    }

    /**
     * Finds all details in schema
     *
     * @param schemaName
     * @param db
     * @return
     */
    public Schema findSchema(String schemaName, DatabaseConnection db) throws IOException {
        Schema schema = new Schema(schemaName);
        for (Table t : findTables(schemaName,db)){
            schema.addTable(t);
        }
        for (View v : findViews(schemaName,db)){
            schema.addView(v);
        }
        for (Function f : findUserFunctions(schemaName, db)){
            schema.addFunction(f);
        }

        for (Function f : listSystemFunctions(db)){
            schema.addSystemFunction(f);
        }
        for (Function f : listStringFunctions(db)){
            schema.addStringFunction(f);
        }
        for (Function f : listTimeDateFunctions(db)){
            schema.addTimeFunction(f);
        }
        for (Function f : listNumbericFunctions(db)){
            schema.addNumericFunction(f);
        }
        return schema;
    }



    /**
     * Finds all tables in database
     *
     * @param schema
     * @param db
     * @return
     */
    public List<Table> findTables(String schema,DatabaseConnection db) {
        //LOG.finest("Finding tables...");
        List<Table> tables = new ArrayList<>();
        if (db == null){
            return tables;
        }
        //
        try (Connection conn = db.getConnection()){

            /**
             * Find tables
             *
             */
            String[] types = {"TABLE"};
            try (ResultSet rs = conn.getMetaData().getTables(null, null, "%", types);) {
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
                ResultSet rs = conn.getMetaData().getColumns(null, null, t.getName(), null);
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
                rs.close();
            }

            /**
             * Foreign Keys
             *
             */
            for (Table t : tables){
                ResultSet rs = conn.getMetaData().getImportedKeys(null, null, t.getName());
                while (rs.next()) {
                    LOG.finest("Looking for foreign key for table '" + t.getName() + "': " + rs.getString("FKTABLE_NAME")+"."+rs.getString("FKCOLUMN_NAME") + " " + rs.getString("PKTABLE_NAME")+"."+rs.getString("PKCOLUMN_NAME"));
                    Column c = t.findColumnByName(rs.getString("FKCOLUMN_NAME"));
                    if (c == null){

                    } else {
                        c.setForeignKey( new ForeignKey( rs.getString("PKTABLE_NAME"), rs.getString("PKCOLUMN_NAME")));
                    }
                }
                rs.close();
            }

            // Primary keys
            for (Table t : tables) {
                ResultSet rs = conn.getMetaData().getPrimaryKeys(null, null, t.getName());
                while (rs.next()) {
                    LOG.fine("PrimaryKey: " + rs.getString(3) + " " + rs.getString(4));
                    Column c = t.findColumnByName(rs.getString(4));
                    if (c != null){
                        c.setPrimaryKey(true);
                    }
                    //System.out.println("Column: " + rs.getString(1) + " " + rs.getString(2) + " " + rs.getString(3) + " " + rs.getString(4) + " " + rs.getString(5) + " " + rs.getString(6) + " ");
                }
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        return tables;
    }

    /**
     * Find all user functions
     *
     * @param schema
     * @param db
     * @return
     */
    public List<Function> findUserFunctions(String schema,DatabaseConnection db) {
        List<Function> list = new ArrayList<>();
        try (Connection conn = db.getConnection()){
            DatabaseMetaData dbmd = conn.getMetaData();
            String catalog = null;
            ResultSet rs = dbmd.getFunctions(catalog, schema, "%");
            while (rs.next()) {
                LOG.info("Function: " + rs.getString(1));
                list.add(new Function(rs.getString(1)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Finds all views
     *
     * @param schema
     * @param db
     * @return
     */
    public List<View> findViews(String schema,DatabaseConnection db) {
        List<View> list = new ArrayList<>();
        try (Connection conn = db.getConnection()){
            DatabaseMetaData dbmd = conn.getMetaData();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }


    /**
     * Finds all String functions
     *
     * @return
     */
    public List<Function> listStringFunctions(DatabaseConnection db) {
        List<Function> items = new ArrayList<>();
        try (Connection conn = db.getConnection()) {
            DatabaseMetaData dbmd = conn.getMetaData();
            String[] arr = dbmd.getStringFunctions().split(",\\s*");
            for (String f : arr) {
                items.add(new Function(f));
            }
        } catch (SQLException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Finds all numeric functions
     *
     * @param db
     * @return
     */
    public List<Function> listNumbericFunctions(DatabaseConnection db) {
        List<Function> items = new ArrayList<>();
        try (Connection conn = db.getConnection()) {
            DatabaseMetaData dbmd = conn.getMetaData();
            String[] arr = dbmd.getNumericFunctions().split(",\\s*");
            for (String f : arr) {
                items.add(new Function(f));
            }
        } catch (SQLException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Finds all system functions
     *
     * @param db
     * @return
     */
    public List<Function> listSystemFunctions(DatabaseConnection db) {
        List<Function> items = new ArrayList<>();
        try (Connection conn = db.getConnection()){
            DatabaseMetaData dbmd = conn.getMetaData();
            String[] arr = dbmd.getSystemFunctions().split(",\\s*");
            for (String f : arr) {
                items.add(new Function(f));
            }
        } catch (SQLException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Find all time and date functions
     * @param db
     * @return
     */
    public List<Function> listTimeDateFunctions(DatabaseConnection db) {
        List<Function> items = new ArrayList<>();
        try (Connection conn = db.getConnection()) {
            DatabaseMetaData dbmd = conn.getMetaData();
            String[] arr = dbmd.getTimeDateFunctions().split(",\\s*");
            for (String f : arr) {
                items.add(new Function(f));
            }
        } catch (SQLException e) {

        } catch (IOException e) {
            e.printStackTrace();
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
    public Function displayFunction(Function function,DatabaseConnection db) throws SQLException, IOException {
        // Gets the database metadata
        DatabaseMetaData mtdt = db.getConnection().getMetaData();
        //System.out.println(mtdt.getProcedureTerm());
        //ResultSet rs = mtdt.getProcedures(conn.getCatalog(), "%", function.getFilename());

        ResultSet rs = mtdt.getProcedureColumns( null, null, function.getName(), null);
            /* Iterate all columns */
        while (rs.next()) {
            FunctionParameter p = new FunctionParameter(rs.getString("COLUMN_NAME"));
            p.setComments(rs.getString("REMARKS"));
            //System.out.println("\t" + p.getFilename() + " " + p.getDataType() + " " + p.getRemarks());
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
}