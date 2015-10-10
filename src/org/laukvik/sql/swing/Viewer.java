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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.TableColumn;
import org.laukvik.sql.SQL;
import org.laukvik.sql.ddl.Function;
import org.laukvik.sql.ddl.Sqlable;
import org.laukvik.sql.ddl.Table;

/**
 *
 * @author morten
 */
public class Viewer extends javax.swing.JFrame {

    private final static Logger LOG = Logger.getLogger(Viewer.class.getName());
    private final int DEFAULT_DDL_WIDTH = 300;
    private final int DEFAULT_QUERY_HEIGHT = 100;
    private final int DEFAULT_DIVIDER_SIZE;
    private final int DEFAULT_TREE_WIDTH = 250;

    private SQL sql = null;
    private TreeModel treeModel;
    private JPanel emptyPanel;
    private DiagramPanel diagramPanel;
    private JScrollPane diagramScroll;

    /**
     * Creates new form SQL
     */
    public Viewer(SQL sql) {
        super();
        emptyPanel = new JPanel();
        initComponents();

        diagramPanel = new DiagramPanel();
        diagramScroll = new JScrollPane(diagramPanel);

        tree.setBackground(new Color(217, 226, 239));

        tableDDL.setDefaultRenderer(Object.class, new EvenOddRenderer());
        tableDDL.setDefaultRenderer(Number.class, new EvenOddRenderer());
        tableDDL.setRowHeight(24);
        tableDDL.getTableHeader().setPreferredSize(new Dimension(100, 30));
        tableDDL.getTableHeader().setBackground(UIManager.getColor("Label.background"));
        tableDDL.getTableHeader().setDefaultRenderer(new SqlTableHeaderRenderer());

        resultTable.setDefaultRenderer(Object.class, new EvenOddRenderer());
        resultTable.setDefaultRenderer(Number.class, new EvenOddRenderer());
        resultTable.setRowHeight(24);
        resultTable.getTableHeader().setPreferredSize(new Dimension(100, 30));
        resultTable.getTableHeader().setBackground(UIManager.getColor("Label.background"));
        resultTable.getTableHeader().setDefaultRenderer(new SqlTableHeaderRenderer());

        setSQL(sql);
        Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension s70 = new Dimension(Math.round(s.width * 0.7f), Math.round(s.height * 0.7f));
        setSize(s70);
        setLocationRelativeTo(null);
        DEFAULT_DIVIDER_SIZE = tableSplitPane.getDividerSize();
        setQueryPanelVisible(false);
        setDefinitionPanelVisible(false);
    }

    public void setSQL(SQL sql) {
        this.sql = sql;
        treeModel = new TreeModel(sql);
        tree.setCellRenderer(treeModel);
        tree.setModel(treeModel);


        diagramPanel.removeAll();
        for (Table t : sql.findTables()){
            diagramPanel.addTable(t);
        }
        diagramPanel.autoLayout();
    }

    public void openTable(Table t){
        // Open query
        queryPane.setText("SELECT * FROM " + t.getName());
        // Run query
        ResultSetTableModel resultModel = new ResultSetTableModel(t, sql);

        resultTable.setModel(resultModel);
        // Open table definition
        TableDefinitionTableModel model = new TableDefinitionTableModel(t);
        tableDDL.setModel(model);
        // Set column widths
        TableColumn tc0 = tableDDL.getColumnModel().getColumn(0);
        tc0.setMinWidth(32);
        tc0.setMaxWidth(32);

        TableColumn tc1 = tableDDL.getColumnModel().getColumn(1);
        tc1.setPreferredWidth(200);

        TableColumn tc2 = tableDDL.getColumnModel().getColumn(2);
        tc2.setPreferredWidth(100);

        TableColumn tc3 = tableDDL.getColumnModel().getColumn(3);
        tc3.setPreferredWidth(40);
    }

    public void openSQL(Sqlable sqlable) {
        if (sqlable instanceof Table) {
            Table t = (Table) sqlable;
            openTable(t);
            mainSplitPane.setRightComponent( tableSplitPane );
            mainSplitPane.setDividerLocation( DEFAULT_TREE_WIDTH );
        } else if (sqlable instanceof Function){
        } else {
            mainSplitPane.setRightComponent( diagramScroll );
            mainSplitPane.setDividerLocation( DEFAULT_TREE_WIDTH );
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainSplitPane = new javax.swing.JSplitPane();
        treeScrollPane = new javax.swing.JScrollPane();
        tree = new javax.swing.JTree();
        tableSplitPane = new javax.swing.JSplitPane();
        queryAndResultSplitPane = new javax.swing.JSplitPane();
        jScrollPaneQuery = new javax.swing.JScrollPane();
        queryPane = new javax.swing.JTextPane();
        tableScroll = new javax.swing.JScrollPane();
        resultTable = new javax.swing.JTable();
        ddlSplitPane = new javax.swing.JScrollPane();
        tableDDL = new javax.swing.JTable();
        jToolBar1 = new javax.swing.JToolBar();
        toggleDDL = new javax.swing.JToggleButton();
        toggleQuery = new javax.swing.JToggleButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        contentsMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        mainSplitPane.setBorder(null);
        mainSplitPane.setDividerLocation(DEFAULT_TREE_WIDTH);

        tree.setRootVisible(true);
        tree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTree1ValueChanged(evt);
            }
        });
        treeScrollPane.setViewportView(tree);

        mainSplitPane.setLeftComponent(treeScrollPane);

        tableSplitPane.setBorder(null);
        tableSplitPane.setDividerLocation(400);
        tableSplitPane.setResizeWeight(1.0);

        queryAndResultSplitPane.setBorder(null);
        queryAndResultSplitPane.setDividerLocation(0);
        queryAndResultSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jScrollPaneQuery.setViewportView(queryPane);

        queryAndResultSplitPane.setLeftComponent(jScrollPaneQuery);

        tableScroll.setBorder(null);

        resultTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        resultTable.setGridColor(new java.awt.Color(230, 230, 230));
        resultTable.setShowGrid(true);
        tableScroll.setViewportView(resultTable);

        queryAndResultSplitPane.setRightComponent(tableScroll);

        tableSplitPane.setLeftComponent(queryAndResultSplitPane);

        tableDDL.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        ddlSplitPane.setViewportView(tableDDL);

        tableSplitPane.setRightComponent(ddlSplitPane);

        mainSplitPane.setRightComponent(tableSplitPane);

        getContentPane().add(mainSplitPane, java.awt.BorderLayout.CENTER);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        toggleDDL.setText("DDL");
        toggleDDL.setFocusable(false);
        toggleDDL.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toggleDDL.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toggleDDL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleDDLActionPerformed(evt);
            }
        });
        jToolBar1.add(toggleDDL);

        toggleQuery.setText("Query");
        toggleQuery.setFocusable(false);
        toggleQuery.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toggleQuery.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toggleQuery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleQueryActionPerformed(evt);
            }
        });
        jToolBar1.add(toggleQuery);

        getContentPane().add(jToolBar1, java.awt.BorderLayout.PAGE_START);

        fileMenu.setMnemonic('f');
        fileMenu.setText("File");

        openMenuItem.setMnemonic('o');
        openMenuItem.setText("Open");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);

        saveMenuItem.setMnemonic('s');
        saveMenuItem.setText("Save");
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setMnemonic('a');
        saveAsMenuItem.setText("Save As ...");
        saveAsMenuItem.setDisplayedMnemonicIndex(5);
        fileMenu.add(saveAsMenuItem);

        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setMnemonic('e');
        editMenu.setText("Edit");

        cutMenuItem.setMnemonic('t');
        cutMenuItem.setText("Cut");
        editMenu.add(cutMenuItem);

        copyMenuItem.setMnemonic('y');
        copyMenuItem.setText("Copy");
        editMenu.add(copyMenuItem);

        pasteMenuItem.setMnemonic('p');
        pasteMenuItem.setText("Paste");
        editMenu.add(pasteMenuItem);

        deleteMenuItem.setMnemonic('d');
        deleteMenuItem.setText("Delete");
        editMenu.add(deleteMenuItem);

        menuBar.add(editMenu);

        helpMenu.setMnemonic('h');
        helpMenu.setText("Help");

        contentsMenuItem.setMnemonic('c');
        contentsMenuItem.setText("Contents");
        helpMenu.add(contentsMenuItem);

        aboutMenuItem.setMnemonic('a');
        aboutMenuItem.setText("About");
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void jTree1ValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTree1ValueChanged

        if (evt.getPath() != null) {
            Object o = evt.getPath().getLastPathComponent();
            if (o instanceof Sqlable) {
                Sqlable s = (Sqlable) o;
                openSQL(s);
            } else {
                System.err.println("ROT?");
                openSQL(null);
            }
        }

    }//GEN-LAST:event_jTree1ValueChanged

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_openMenuItemActionPerformed

    public void setDefinitionPanelVisible(boolean isVisible) {
        if (isVisible) {
            tableSplitPane.setDividerLocation(tableSplitPane.getWidth() - DEFAULT_DDL_WIDTH);
            tableSplitPane.setDividerSize(DEFAULT_DIVIDER_SIZE);
            tableSplitPane.setRightComponent(ddlSplitPane);
        } else {
            tableSplitPane.setDividerLocation(1.0);
            tableSplitPane.setDividerSize(0);
            tableSplitPane.setRightComponent(null);
        }
    }

    private void toggleDDLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleDDLActionPerformed
        setDefinitionPanelVisible(tableSplitPane.getDividerLocation() > tableSplitPane.getWidth() - DEFAULT_DDL_WIDTH);

    }//GEN-LAST:event_toggleDDLActionPerformed

    public void setQueryPanelVisible(boolean isVisible) {
        if (isVisible) {
            queryAndResultSplitPane.setDividerLocation(DEFAULT_QUERY_HEIGHT);
            queryAndResultSplitPane.setDividerSize(DEFAULT_DIVIDER_SIZE);
        } else {
            queryAndResultSplitPane.setDividerLocation(0);
            queryAndResultSplitPane.setDividerSize(0);
        }
    }

    private void toggleQueryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleQueryActionPerformed
        setQueryPanelVisible(queryAndResultSplitPane.getDividerLocation() == 0);
    }//GEN-LAST:event_toggleQueryActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem contentsMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JScrollPane ddlSplitPane;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JScrollPane jScrollPaneQuery;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JTree tree;
    private javax.swing.JSplitPane mainSplitPane;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JSplitPane queryAndResultSplitPane;
    private javax.swing.JTextPane queryPane;
    private javax.swing.JTable resultTable;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JTable tableDDL;
    private javax.swing.JScrollPane tableScroll;
    private javax.swing.JSplitPane tableSplitPane;
    private javax.swing.JToggleButton toggleDDL;
    private javax.swing.JToggleButton toggleQuery;
    private javax.swing.JScrollPane treeScrollPane;
    // End of variables declaration//GEN-END:variables

}
