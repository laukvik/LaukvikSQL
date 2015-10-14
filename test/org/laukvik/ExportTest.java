package org.laukvik;

import org.junit.Test;
import org.laukvik.sql.*;
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
        Analyzer a = new Analyzer();
        /*
        a.findSchema(db.get)
        sql.openConnectionByName("default");
        Table t = new Table("Activity");
        DatabaseConnection db = sql.getDatabaseConnection();
        Exporter exporter = new Exporter(db);
        exporter.export( t, new File("/Users/morten/Desktop/Activity.csv") );
        */
    }

}
