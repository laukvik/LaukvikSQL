package org.laukvik.sql;

import org.junit.Test;
import org.laukvik.sql.ddl.IntegerColumn;

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
