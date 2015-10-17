package org.laukvik.sql;

import org.junit.Test;
import org.laukvik.sql.ddl.Column;
import org.laukvik.sql.ddl.IntegerColumn;
import org.laukvik.sql.ddl.Table;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by morten on 13.10.2015.
 */
public class ExporterTest {

    public static File getTestFolder() {
        ClassLoader classLoader = org.laukvik.sql.ImporterTest.class.getClassLoader();
        return new File(classLoader.getResource("").getFile());
    }

    public static File getResource(String filename) {
        ClassLoader classLoader = org.laukvik.sql.ImporterTest.class.getClassLoader();
        return new File(classLoader.getResource(filename).getFile());
    }

    @Test
    public void shouldExportToFile() throws DatabaseConnectionNotFoundException, IOException, SQLException {
        Analyzer a = new Analyzer();
        /*
        a.findSchema(db.get)
        sql.openConnectionByName("default");
        Table t = new Table("Activity");
        DatabaseConnection db = sql.getDatabaseConnection();
        Exporter exporter = new Exporter(db);
        exporter.exportTableCSV( t, new File("/Users/morten/Desktop/Activity.csv") );
        */
    }

    @Test
    public void shouldWriteMetaData() throws IOException {
        Table t  = new Table("Activity");
        {
            Column c = new IntegerColumn("id");
            c.setPrimaryKey(true);
            c.setAutoIncrement(true);
            c.setAllowNulls(false);
            t.addColumn(c);
        }

        Exporter exp = new Exporter(null);
        exp.createMetaData(t, new File("/Users/morten/Desktop/Activity.meta.csv"));
    }

}
