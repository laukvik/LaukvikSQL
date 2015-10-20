package org.laukvik.sql;

import org.laukvik.csv.CSV;
import org.laukvik.csv.CsvWriter;
import org.laukvik.csv.MetaData;
import org.laukvik.csv.Row;
import org.laukvik.sql.ddl.Column;
import org.laukvik.sql.ddl.Schema;
import org.laukvik.sql.ddl.Table;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 *
 */
public class Exporter {

    private final Logger LOG = Logger.getLogger(Exporter.class.getName());
    private DatabaseConnection databaseConnection;

    public Exporter(DatabaseConnection databaseConnection){
        this.databaseConnection = databaseConnection;
    }

    /**
     * Exports all table
     *
     * @param directory
     * @throws IOException
     * @throws SQLException
     * @throws DatabaseConnectionNotFoundException
     */
    public void backupCSV(File directory) throws IOException, SQLException, DatabaseConnectionNotFoundException {
        LOG.info("Creating CSV backup to " + directory.getAbsolutePath());

        Analyzer a = new Analyzer();
        Schema s = a.findSchema(null,databaseConnection);
        int max = s.getTables().size();
        int counter = 0;
        for (Table t : s.getTables()){
            counter++;
            LOG.fine(counter + "/" + max + " " + t.getName());
            System.out.println(counter + "/" + max + " " + t.getName());
            File dataFile = new File( directory.getAbsolutePath(), t.getName() + ".csv" );
            exportTableCSV(t, dataFile);

            File metaFile = new File( directory.getAbsolutePath(), t.getName() + ".meta.csv" );
            Exporter.createMetaData( t, metaFile );
        }
    }

    /**
     * Exports table data to file
     *
     * @param table
     * @param file
     * @throws FileNotFoundException
     */
    public void exportTableCSV(Table table, File file) throws FileNotFoundException {
        LOG.info("Exporting table '" + table + "' to file " + file.getAbsolutePath() );
        try(
            Connection conn = databaseConnection.getConnection();
            OutputStream out = new FileOutputStream(file);
            CsvWriter writer = new CsvWriter(out);
            ResultSet rs = conn.createStatement().executeQuery( table.getSelectTable() );
        ){
            int columnCount = rs.getMetaData().getColumnCount();
            LOG.fine("Found " + columnCount + " columns in table " + table.getName());
            System.out.println("Found " + columnCount + " columns in table " + table.getName());

            MetaData md = new MetaData();
            for (int x=0; x<columnCount; x++){
                String column = rs.getMetaData().getColumnName(x + 1);
                LOG.fine("Column: " + column);
                md.addColumn( column );
            }
            writer.writeMetaData(md);
            int rowCounter = 0;
            while(rs.next()){
                rowCounter++;
                LOG.fine("Row: " + rowCounter);
                System.out.println(".");
                String [] values = new String [ columnCount ];
                for (int x=0; x<columnCount; x++){
                    Object o = rs.getObject(x+1);
                    values[ x ] = o == null ? "" :  o.toString();
                }
                writer.writeRow(values);
            }


        } catch (IOException e){
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void exportScript(Schema schema, File file){
        LOG.info("Exporting database to file: " + file.getAbsolutePath() );
        try(FileOutputStream out = new FileOutputStream( file )) {
            List<Table> tables = schema.getTables();
            for (int z=0; z<tables.size(); z++){
                Table table = tables.get(z);
                System.out.print(table.getName() + ":");
                out.write( table.getDDL().getBytes() );
                try (Connection conn = databaseConnection.getConnection();
                     ResultSet rs = conn.createStatement().executeQuery( table.getSelectTable() )) {
                    int cols = rs.getMetaData().getColumnCount();
                    while (rs.next()) {
                        out.write( table.getInsertSQL(rs).getBytes() );
                        out.write("\n".getBytes());
                    }
                    System.out.print("#");
                } catch (Exception e) {
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

    /**
     * Exports all tables to System.out
     *
     * @param schemaName
     * @throws DatabaseExportFailedException
     * @throws IOException
     */
    public void exportDatabaseScripts(String schemaName) throws DatabaseExportFailedException, IOException {
        Analyzer a = new Analyzer();
        Schema schema = a.findSchema(schemaName,databaseConnection);
        List<Table> tables = schema.getTables();
        for (int z=0; z<tables.size(); z++){
            Table table = tables.get(z);
            System.out.println(table.getDDL() );
            try (
                    Connection conn = databaseConnection.getConnection();
                    ResultSet rs = conn.createStatement().executeQuery( table.getSelectTable() )
                )
            {
                int cols = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    System.out.println(table.getInsertSQL(rs));
                }
            } catch (SQLException e) {
                throw new DatabaseExportFailedException(e,databaseConnection);
            } catch (IOException e) {
                throw new DatabaseExportFailedException(e,databaseConnection);
            }
        }
    }

    public void listQuery(String query){
        try (
                Connection conn = databaseConnection.getConnection();
                ResultSet rs = conn.createStatement().executeQuery(query)
        ) {
            int cols = rs.getMetaData().getColumnCount();
            for (int x=0; x<cols; x++){
                System.out.print( x > 0 ? "," : "" );
                System.out.print(rs.getMetaData().getColumnLabel(x+1));
            }
            while (rs.next()) {
                for (int x=0; x<cols; x++){
                    System.out.print( x > 0 ? "," : "" );
                    System.out.print(rs.getObject(x + 1));
                }
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
    public void backup(String... tables){
        Analyzer a = new Analyzer();
        try {
            Schema s = a.findDefaultSchema(databaseConnection);
            for (Table t : s.getTables()){
            }
            Table t = s.getTables().get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    /**
     *
     * @param table
     * @param file
     */
    public static void createMetaData(Table table, File file) throws IOException {
        CSV csv = Exporter.createMetaDataCSV(table);
        csv.write(file);
    }

    public static CSV createMetaDataCSV(Table table){
        CSV csv = new CSV();
        csv.addColumn("column");
        csv.addColumn("type");
        csv.addColumn("size");
        csv.addColumn("allowNulls");
        csv.addColumn("primaryKey");
        csv.addColumn("foreignKey");
        csv.addColumn("autoIncrement");
        csv.addColumn("autoGenerated");
        csv.addColumn("default");
        csv.addColumn("comments");
        csv.addColumn("format");

        for (Column c : table.getColumns()){
            // New row
            Row r = csv.createRow();
            // Setting values
            r.setValue( 0, c.getName());
            r.setValue( 1, c.getType() );
            r.setValue( 2, c.getSize() );
            r.setValue( 3, c.isAllowNulls() );
            r.setValue( 4, c.isPrimaryKey() );
            r.setValue( 5, (c.getForeignKey() == null ? "" : c.getForeignKey().getDDL()) );
            r.setValue( 6, c.isAutoIncrement() );
            r.setValue( 7, c.isAutoGenerated() );
            r.setValue( 8, c.getDefaultValue() );
            r.setValue( 9, c.getComments() );
            r.setValue(10, c.getFormat());
        }
        return csv;
    }

}