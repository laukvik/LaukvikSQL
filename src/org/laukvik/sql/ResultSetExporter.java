package org.laukvik.sql;

import org.laukvik.csv.CsvWriter;
import org.laukvik.csv.MetaData;
import org.laukvik.sql.ddl.DatabaseConnection;
import org.laukvik.sql.ddl.Schema;
import org.laukvik.sql.ddl.Table;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 *
 *
 */
public class ResultSetExporter {

    private final Logger LOG = Logger.getLogger(ResultSetExporter.class.getName());

    private DatabaseConnection databaseConnection;
    private ResultSet rs;

    public ResultSetExporter(DatabaseConnection databaseConnection ){
        this.databaseConnection = databaseConnection;
    }

    public void exportTables( File directory ) throws IOException, SQLException, DatabaseConnectionNotFoundException {
        LOG.info("Exporting all tables to " + directory.getParent());
        SQL sql = new SQL();
        sql.openConnectionByName("default");

        Schema s = sql.getSchema();
        for (Table t : s.getTables()){
            //LOG.info("Table: " + t.getName());
            File file = new File( directory.getAbsolutePath(), t.getName() + ".csv" );
            export(t, file);
        }
    }

    public void export( Table table, File file ) throws FileNotFoundException {
        LOG.info("Exporting table '" + table + "' to file " + file.getAbsolutePath() );
        try{
            Connection conn = databaseConnection.getConnection();
            OutputStream out = new FileOutputStream(file);
            CsvWriter writer = new CsvWriter(out);
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM " + table.getName() );

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
            rs.close();
            conn.close();

        } catch (IOException e){
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
