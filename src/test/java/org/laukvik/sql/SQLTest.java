package org.laukvik.sql;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class SQLTest {

    @Test
    public void shouldFindNamedConnections() throws DatabaseConnectionNotFoundException {
        List<DatabaseConnection> conns = Analyzer.findDatabaseConnections();
        Assert.assertEquals(4,conns.size());
        System.out.println("Connections: " + conns.size());
        for (DatabaseConnection c : conns){
            System.out.println(c.getFilename());
        }
    }

    @Test
    public void shouldFail() throws DatabaseConnectionNotFoundException {

    }

    /*
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
*/
}
