package org.laukvik.sql;

import org.laukvik.csv.CsvWriter;
import org.laukvik.csv.MetaData;
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

    public void exportTables( File directory ) throws IOException, SQLException, DatabaseConnectionNotFoundException {
        LOG.info("Exporting all tables to " + directory.getParent());
        Analyzer a = new Analyzer();
        Schema s = a.findSchema(null,databaseConnection);
        for (Table t : s.getTables()){
            //LOG.info("Table: " + t.getFilename());
            File file = new File( directory.getAbsolutePath(), t.getName() + ".csv" );
            export(t, file);
        }
    }

    public void export( Table table, File file ) throws FileNotFoundException {
        LOG.info("Exporting table '" + table + "' to file " + file.getAbsolutePath() );
        try(
            Connection conn = databaseConnection.getConnection();
            OutputStream out = new FileOutputStream(file);
            CsvWriter writer = new CsvWriter(out);
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM " + table.getName() );
        ){


            int columnCount = rs.getMetaData().getColumnCount();
            LOG.info("Found " + columnCount + " columns in table " + table.getName());

            MetaData md = new MetaData();
            for (int x=0; x<columnCount; x++){
                String column = rs.getMetaData().getColumnName(x + 1);
                LOG.info("Column: " + column);
                md.addColumn( column );
            }
            writer.writeMetaData(md);
            int rowCounter = 0;
            while(rs.next()){
                rowCounter++;
                LOG.info("Row: " + rowCounter);
                String [] values = new String [ columnCount ];
                for (int x=0; x<columnCount; x++){
                    Object o = rs.getObject(x+1);
                    values[ x ] = o == null ? "" :  o.toString();
                }
                writer.writeRow(values);
            }

            writer.close();

        } catch (IOException e){
            e.printStackTrace();
        } catch (SQLException e) {
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
                try (Connection conn = databaseConnection.getConnection();
                     ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM " + table.getName() )) {
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

    public void export2(String schemaName) throws DatabaseExportFailedException, IOException {
        Analyzer a = new Analyzer();
        Schema schema = a.findSchema(schemaName,databaseConnection);
        List<Table> tables = schema.getTables();
        for (int z=0; z<tables.size(); z++){
            Table table = tables.get(z);
            System.out.println(table.getDDL() );
            try (
                    Connection conn = databaseConnection.getConnection();
                    ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM " + table.getName() )
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

}
