package org.laukvik.org.laukvik.sql.ddl;

import org.junit.Test;
import org.laukvik.sql.ddl.IntegerColumn;
import org.laukvik.sql.ddl.Table;

/**
 * Created by morten on 16.10.2015.
 */
public class IntegerColumnTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithEmptyName(){
        new IntegerColumn("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithNullName(){
        new IntegerColumn(null);
    }

}
