package org.laukvik.sql;


import org.laukvik.csv.CSV;
import org.laukvik.csv.CsvReader;
import org.laukvik.csv.ParseException;
import org.laukvik.csv.Row;
import org.laukvik.sql.ddl.*;
import org.laukvik.sql.swing.BackupMetaDataFileFilter;

import java.io.*;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 *
 *
 */
public class Importer {

    private final static Logger LOG = Logger.getLogger(Importer.class.getName());
    private DatabaseConnection db;

    public Importer(DatabaseConnection databaseConnection) throws DatabaseReadOnlyException {
        this.db = databaseConnection;
        if (db.isReadOnly()){
            throw new DatabaseReadOnlyException(db.getFilename());
        }
    }

    /**
     * Restores all data found in directory
     *
     * @param directory
     * @throws DatabaseReadOnlyException
     */
    public void importDirectory( File directory, Charset charset) throws DatabaseReadOnlyException {
        LOG.fine("Importing directory " + directory.getAbsolutePath());
        BackupMetaDataFileFilter ff = new BackupMetaDataFileFilter();
        List<Table> tables = new ArrayList<>();
        List<File> datas = new ArrayList<>();
        System.out.print("Finding files to import: ");
        try {
            File [] files = directory.listFiles(ff);
            int x = 0;
            for (File f : files) {
                x++;
                // Get name of file without extension
                String tableName = BackupMetaDataFileFilter.getName(f);
                //
                LOG.fine("Importing table " + tableName);
                //
                File meta = new File(directory.getAbsolutePath(), tableName + ".meta.csv");
                Table t = Importer.readTableMetadata(tableName,meta);
                // Create
                File data = new File(directory.getAbsolutePath(), tableName + ".csv");

                if (data.exists()){
                    tables.add(t);
                    datas.add(data);
                } else {
                    System.err.println("Missing data file for " + tableName + "!");
                }
            }
            System.out.print(tables.size());
            System.out.print("(");
            for (int y=0; y<tables.size();y++){
                System.out.print( (y>0 ? "," : "") + tables.get(y).getName());
            }
            System.out.print(")");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Pre installation
        System.out.println("Running pre-installation scripts...");
        for (int x=0; x<tables.size(); x++){
            // Find table
            Table table = tables.get(x);
            boolean wasCreated = preInstall(table);
            System.out.println( (x+1) + "/" + tables.size() + " " + table.getName() + " PreInstall: " + (wasCreated ? "Ok":"Failed") );
        }

        // Installation
        System.out.println("Installing data from files...");
        for (int x=0; x<tables.size(); x++){
            // Find table
            Table table = tables.get(x);
            File data = datas.get(x);
            try {
                installFromCsvFile(table, data, charset);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Post installation
        System.out.println("Running post-installation autonumber scripts...");
        for (int x=0; x<tables.size(); x++){
            // Find table
            Table table = tables.get(x);
            File data = datas.get(x);
            boolean wasCreated = postInstallAutoNumber(table);
            System.out.println( (x+1) + "/" + tables.size() + " " + table.getName() + " - AutoNumber: " + (wasCreated ? "Ok":"Failed") );
        }

        System.out.println("Running post-installation foreignKey scripts...");
        for (int x=0; x<tables.size(); x++){
            // Find table
            Table table = tables.get(x);
            File data = datas.get(x);
            boolean wasCreated = postInstallForeignKey(table);
            System.out.println( (x+1) + "/" + tables.size() + " " + table.getName() + " - ForeignKey: " + (wasCreated ? "Ok":"Failed") );
        }

    }

    /**
     * Run required SQL scripts before installing data
     *
     *
     * @param table
     */
    public boolean preInstall( Table table ){
        boolean successful = false;
        try(
                Connection conn = db.getConnection();
                Statement st = conn.createStatement();
        ){
            int results = st.executeUpdate(table.getDDL());
            LOG.fine("Created table " + table + " with " + table.getColumns().size() + " columns.");
            successful = true;
        } catch (Exception e) {
            LOG.fine("Failed to create table " + table + "! (" + e.getMessage() + ")");
            e.printStackTrace();
            successful = true;
        }
        return successful;
    }

    /**
     * Run required SQL scripts after installing data
     *
     * @param t
     * @return
     */
    public boolean postInstallAutoNumber(Table t){
        if (t.isPostInstallRequired()){
            try(
                    Connection conn = db.getConnection();
                    Statement st = conn.createStatement();
            ){
                for (String q : t.getPostAutoNumberScript(db)){
                    try{
                        st.executeUpdate( q );
                    } catch (Exception e){
                        System.out.println( t.getName() +": AutoNumber failed! Script: " + q + " Error: " + e.getMessage());
                        return false;
                    }
                }
                return true;
            } catch (Exception e){
                return false;
            }
        } else {
            return true;
        }
    }

    public boolean postInstallForeignKey(Table t){
        if (t.isPostInstallRequired()){
            try(
                Connection conn = db.getConnection();
                Statement st = conn.createStatement();
            ){
                for (String q : t.getPostConstraintScript(db)){
                    try{
                        st.executeUpdate( q );
                        System.out.println("Updated FK: " + q );
                    } catch (Exception e){
                        System.out.println( t.getName() +": Constraint failed! Script: " + q + " Error: " + e.getMessage());
                        return false;
                    }
                }
                return true;
            } catch (Exception e){
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * Imports the specified table into the database. Both a metadata file and
     * the data file must be found in order to import.
     *
     * @throws Exception
     */
    public long installFromCsvFile(Table t, File data, Charset charset) throws Exception {
        LOG.fine("Reading data file " + data.getAbsolutePath());
        InputStream is = new FileInputStream(data);
        CsvReader r = new CsvReader( is, charset );
        long updateCount = 0;
        long failedCount = 0;
        long totalCount  = 0;
        try(
                Connection conn = db.getConnection();
                Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                /** @todo - Add schema when importing */
                ResultSet rs = st.executeQuery("SELECT * FROM " + t.getName() );
        ){
            int n = 1;
            rs.next();
            while(r.hasNext()){
                totalCount++;
                Row row = r.getRow();
                try{
                    // Prepare new row
                    rs.moveToInsertRow();

                    // Fill data
                    for (int x=0; x<t.getColumns().size(); x++){
                        // Get column definition
                        Column c = t.getColumns().get(x);
                        int columnIndex = x+1;

                            //

                            if (c instanceof BigIntColumn) {
                                Integer value = row.getInteger(x);
                                if (value != null) {
                                    rs.updateInt(columnIndex, value);
                                }
                            } else if (c instanceof BinaryColumn){
                                /* @todo - Implement BinaryColumn support */


                            } else if (c instanceof BooleanColumn){
                                Boolean value = row.getBoolean(x);
                                if (value != null){
                                    rs.updateBoolean(columnIndex, value);
                                }


                            } else if (c instanceof CharColumn){
                                /* @todo - Implement CharColumn support */

                            } else if (c instanceof DateColumn) {
                                String value = row.getString(x);
                                if (value == null || value.trim().isEmpty()) {
                                    rs.updateNull(columnIndex);
                                } else {
                                    DateColumn dc = (DateColumn) c;
                                    SimpleDateFormat format = new SimpleDateFormat(dc.getFormat());
                                    Date date = format.parse(value);
                                    rs.updateDate(columnIndex, new java.sql.Date(date.getTime()));
                                }

                            } else if (c instanceof DecimalColumn){
                                /* @todo - Implement DecimalColumn support */

                            } else if (c instanceof DoubleColumn){
                                Double value = row.getDouble(x);
                                if (value != null){
                                    rs.updateDouble(columnIndex, value);
                                }

                            } else if (c instanceof FloatColumn){
                                Float value = row.getFloat(x);
                                if (value != null){
                                    rs.updateFloat(columnIndex, value);
                                }


                            } else if (c instanceof IntegerColumn){
                                Integer value = row.getInteger(x);
                                if (value != null){
                                    rs.updateInt(columnIndex, value);
                                }

                            } else if (c instanceof LongVarBinaryColumn){
                                /* @todo - Implement LongVarBinaryColumn support */

                            } else if (c instanceof LongVarCharColumn){
                                String s = row.getString(columnIndex);
                                if (s != null){
                                    rs.updateString(columnIndex, s);
                                }

                            } else if (c instanceof NumericColumn){
                                /* @todo - Implement NumericColumn support */

                            } else if (c instanceof OtherColumn){
                                /* @todo - Implement OtherColumn support */

                            } else if (c instanceof RealColumn){
                                /* @todo - Implement RealColumn support */

                            } else if (c instanceof SmallIntColumn){
                                /* @todo - Implement SmallIntColumn support */
                                short s = 0;
                                rs.updateShort(columnIndex,s);

                            } else if (c instanceof TimeColumn){
                                /* @todo - Implement TimeColumn support */

                            } else if (c instanceof TimestampColumn){
                                String value = row.getString(x);
                                if (value == null || value.trim().isEmpty()){

                                } else {
                                    TimestampColumn dc = (TimestampColumn)c;
                                    SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ); // 2011-10-13 16:26:45.0
                                    Date date = format.parse( value );
                                    rs.updateTimestamp(columnIndex, new java.sql.Timestamp(date.getTime()));
                                }

                            } else if (c instanceof TinyIntColumn){
                                /* @todo - Implement TinyIntColumn support */

                            } else if (c instanceof VarBinaryColumn){
                                /* @todo - Implement VarBinaryColumn support */

                            } else if (c instanceof VarCharColumn){
                                String value = row.getString(x);
                                rs.updateString(columnIndex, value);

                            }
                        }


                    // Add new row
                    rs.insertRow();

                    updateCount++;
                    System.out.println( t.getName() + ": #" + n + " imported. " + row.getString(0));
                } catch(SQLException e2){
                    failedCount++;
                    System.out.println(t.getName() + ": #" + n + " failed: " + e2.getMessage());
                }
                n++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(t.getName() + ": Imported " + updateCount + "/" + totalCount + "." );
        return updateCount;
    }


    /**
     * Reads Table definition persisted in a file
     *
     * @param table
     * @param file
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public static Table readTableMetadata(String table, File file) throws ParseException, IOException {
        Table t = new Table( table );
        CSV csv = new CSV( file );
        for (int y=0; y<csv.getRowCount(); y++){
            //
            Row r = csv.getRow(y);
            //
            Column c = Column.parse( r.getInteger("type"), r.getString("column"));
            // Set properties
            c.setAllowNulls( r.getBoolean("allowNulls") );
            c.setPrimaryKey( r.getBoolean("primaryKey") );
            c.setForeignKey(  ForeignKey.parse(r.getString("foreignKey")));
            c.setSize( r.getInteger("size") );
            c.setAutoIncrement( r.getBoolean("autoIncrement") );
            c.setAutoGenerated(r.getBoolean("autoGenerated"));
            c.setComments(r.getString("comments"));
            c.setDefaultValue(r.getString("default"));
            c.setFormat(r.getString("format"));
            t.addColumn(c);
        }
        return t;
    }


}
