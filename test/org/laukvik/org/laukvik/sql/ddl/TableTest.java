package org.laukvik.org.laukvik.sql.ddl;

import org.junit.Test;
import org.laukvik.sql.ddl.Table;

/**
 * Created by morten on 16.10.2015.
 */
public class TableTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithNullName(){
        Table t = new Table(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithEmptyName(){
        Table t = new Table("");
    }

}
