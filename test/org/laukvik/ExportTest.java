package org.laukvik;

import org.junit.Test;
import org.laukvik.sql.DatabaseConnectionNotFoundException;
import org.laukvik.sql.ResultSetExporter;
import org.laukvik.sql.SQL;
import org.laukvik.sql.ddl.DatabaseConnection;
import org.laukvik.sql.ddl.Schema;
import org.laukvik.sql.ddl.Table;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by morten on 13.10.2015.
 */
public class ExportTest {


    @Test
    public void shouldExportToFile() throws DatabaseConnectionNotFoundException, IOException, SQLException {
        SQL sql = new SQL();
        sql.openConnectionByName("default");
        Table t = new Table("Activity");
        DatabaseConnection db = sql.getDatabaseConnection();
        ResultSetExporter exporter = new ResultSetExporter(db);
        exporter.export( t, new File("/Users/morten/Desktop/Activity.csv") );
    }

}
