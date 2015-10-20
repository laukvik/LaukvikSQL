/*
 * Copyright (C) 2014 morten
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.laukvik.sql;

import java.util.logging.Logger;
import org.laukvik.sql.cmd.*;

/**
 *
 */
public class SQL {

    private static final Logger LOG = Logger.getLogger(SQL.class.getName());

    public static void main(String[] args) {

        try {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            LOG.warning("Could not set system look and feel.");
        }
        CommandManager mgr = new CommandManager("sql");
        mgr.add( new ListConnections());
        mgr.add( new App() );
        mgr.add( new Backup() );
        mgr.add( new ListDateFunctions() );
        mgr.add( new DisplayFunction() );
        mgr.add( new ExportScripts() );
        mgr.add( new Import() );
        mgr.add( new ListTables() );
        mgr.add( new ListUserFunctions() );
        mgr.add( new ListViews() );
        mgr.add( new ListNumericFunctions() );
        mgr.add( new Query() );
        mgr.add( new Restore() );
        mgr.add( new ListStringFunctions() );
        mgr.add( new ListSystemFunctions() );
        mgr.add( new ExportTableDDL() );
        mgr.add( new ExportTable() );
        int status = mgr.run(args);
        System.exit(status);
    }

}