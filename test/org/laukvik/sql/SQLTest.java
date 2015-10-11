package org.laukvik.sql;

import org.junit.Test;
import org.laukvik.sql.ddl.DatabaseConnection;
import org.laukvik.sql.ddl.Schema;

import java.util.List;

/**
 * Created by morten on 08.10.2015.
 */
public class SQLTest {

    @Test
    public void shouldFindNamedConnections() throws DatabaseConnectionNotFoundException {
        SQL sql = new SQL();
        List<DatabaseConnection> conns = SQL.findDatabaseConnections();
        for (DatabaseConnection c : conns){
            System.out.println(c.getName());
        }
    }

    @Test
    public void shouldFindSchema() throws DatabaseConnectionNotFoundException {
        SQL sql = new SQL();

        System.out.println();
        //SQL.findSchema();
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
