package org.laukvik.sql.cmd;

import org.laukvik.sql.Analyzer;
import org.laukvik.sql.DatabaseConnection;
import org.laukvik.sql.SQL;
import org.laukvik.sql.ddl.View;

public class ListViews extends SqlCommand {

    public ListViews() {
        super("views", "displays all views");
    }

    @Override
    public int run(DatabaseConnection db, String value) {
        Analyzer a = new Analyzer();
        for (View v : a.findViews(null, db)){
            System.out.println(v.getName());
        }
        return 0;
    }
}
