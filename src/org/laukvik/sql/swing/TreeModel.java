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
package org.laukvik.sql.swing;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import org.laukvik.sql.SQL;
import org.laukvik.sql.ddl.Function;
import org.laukvik.sql.ddl.Table;
import org.laukvik.sql.ddl.View;

/**
 * A model for the database connection and a renderer for tree nodes
 *
 * @author morten
 */
public class TreeModel extends DefaultTreeCellRenderer implements javax.swing.tree.TreeModel {

    private static final Logger LOG = Logger.getLogger(TreeModel.class.getName());
    private final List<TreeModelListener> listeners;
    private SQL sql;
    private List root;
    private List<Table> tables;
    private List<View> views;
    private List<Function> functions;


    public TreeModel(SQL sql) {
        super();
        listeners = new ArrayList<>();
        root = new ArrayList<>();
        setSQL(sql);
    }

    public void setSQL(SQL sql) {
        this.sql = sql;
        root = new ArrayList<>();
        if (sql == null){
            tables = new ArrayList<>();
            views = new ArrayList<>();
            functions = new ArrayList<>();
        } else {
            tables = sql.findTables();
            views = sql.findViews();
            functions = sql.findUserFunctions();
        }
        root.add(tables);
        root.add(views);
        root.add(functions);
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        LOG.finest("getChild: " + parent + " index: " + index);
        if (parent == root){
            return root.get(index);

        } else if (parent == tables){
            return tables.get(index);

        } if (parent == functions){
            return functions.get(index);

        } else if (parent == views){
            return views.get(index);

        } else {
            return null;
        }
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent == root){
            return root.size();

        } else if (parent == tables){
            return tables.size();

        } if (parent == functions){
            return functions.size();

        } else if (parent == views){
            return views.size();

        } else {
            return -1;
        }
    }

    @Override
    public int getIndexOfChild(Object parent, Object node) {
        LOG.info("getIndexOfChild: " + parent + " child: " + node);

        if (parent == tables){
            return tables.indexOf( (Table)node );

        } if (parent == functions){
            return functions.indexOf((Function) node);

        } else if (parent == views) {
            return views.indexOf((View) node);

        } else if (parent == root){
            return root.indexOf(node);

        } else {
            LOG.warning("Unkown: " + parent);
            return 0;
        }
    }

    @Override
    public boolean isLeaf(Object node) {


        if (node == root) {
            LOG.info("Root: is not leaf" );
            return false;

        } else if (node == tables){
            LOG.info("Tables: is not leaf" );
            return false;

        } if (node == functions){
            LOG.info("Functions: is not leaf" );
            return false;

        } else if (node == views){
            LOG.info("Views: is not leaf" );
            return false;

        } else {
            LOG.warning(node + ": is not leaf");
            return true;
        }
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        for (TreeModelListener l : listeners) {
            l.treeNodesChanged( new TreeModelEvent( this,path ));
        }
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        setBackground(sel ? getBackgroundSelectionColor() : tree.getBackground());

        setOpaque(true);

        if (value == root) {
            setText("Rot");

        } else if (value == tables) {
            setText("Tabeller");

        } else if (value == views) {
            setText("Views");

        } else if (value == functions) {
            setText("Functions");
        }

        return this;
    }

}
