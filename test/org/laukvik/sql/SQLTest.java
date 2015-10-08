package org.laukvik.sql;

import org.junit.Test;

/**
 * Created by morten on 08.10.2015.
 */
public class SQLTest {

    @Test
    public void shouldListTables(){
        SQL.main( new String[] {"-tables","default"} );
    }

    @Test
    public void shouldListViews(){
        SQL.main( new String[] {"-views","default"} );
    }

    @Test
    public void shouldListFunctions(){
        SQL.main( new String[] {"-functions","default"} );
    }

    @Test
    public void shouldOpenAppWithoutDatabase(){
        SQL.main( new String[] {} );
    }

    @Test
    public void shouldOpenAppWithDatabase(){
        SQL.main( new String[] {"default"} );
    }

    @Test
    public void shouldNotFindDatabase(){
        SQL.main( new String[] {"illegal-database"} );
    }

}
