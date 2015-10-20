package org.laukvik.sql;

import org.junit.Assert;
import org.junit.Test;
import org.laukvik.sql.ddl.ForeignKey;

/**
 * Created by morten on 17.10.2015.
 */
public class ForeignKeyTest {

    @Test
    public void shouldParse(){
        ForeignKey key = new ForeignKey("Customer", "customer_id");
        ForeignKey k1 = ForeignKey.parse("Customer(customer_id)");
        Assert.assertEquals(key, k1);
    }

    @Test
    public void shouldNotParse(){
        Assert.assertEquals("Missing (",null, ForeignKey.parse("Customercustomer_id)"));
        Assert.assertEquals("Missing )",null, ForeignKey.parse("Customer(customer_id"));
        Assert.assertEquals("Missing ( and )",null, ForeignKey.parse("Customer"));
        Assert.assertEquals("Only ()",null, ForeignKey.parse("()"));
        Assert.assertEquals("Only ( )",null, ForeignKey.parse("( )"));
        Assert.assertEquals("Empty",null, ForeignKey.parse("  "));
        Assert.assertEquals("Empty",null, ForeignKey.parse(""));
        Assert.assertEquals("Null",null, ForeignKey.parse(null));
    }

}
