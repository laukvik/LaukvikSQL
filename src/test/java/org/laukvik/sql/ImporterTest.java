package org.laukvik.sql;

import org.junit.Assert;
import org.junit.Test;
import org.laukvik.csv.ParseException;
import org.laukvik.sql.ddl.Table;

import java.io.File;
import java.io.IOException;

/**
 * Created by morten on 16.10.2015.
 *
 */
public class ImporterTest {

    public static File getTestFolder() {
        ClassLoader classLoader = org.laukvik.sql.ImporterTest.class.getClassLoader();
        return new File(classLoader.getResource("").getFile());
    }

    public static File getResource(String filename) {
        ClassLoader classLoader = org.laukvik.sql.ImporterTest.class.getClassLoader();
        return new File(classLoader.getResource(filename).getFile());
    }
    @Test
    public void shouldReadMetaData() throws IOException, ParseException {
        Table t = Importer.readTableMetadata("Employee", getResource("employee.meta.csv"));
        Assert.assertEquals("",3,t.getColumns().size());
        //System.out.println( t.getDDL() );
    }

    @Test
    public void read() throws Exception {
        DatabaseConnection db = DatabaseConnection.read("test");
        Importer importer = new Importer(db);
        importer.importCSV( org.laukvik.sql.ImporterTest.getTestFolder(), "employee");
    }

}