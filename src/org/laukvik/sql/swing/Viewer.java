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

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;

import org.laukvik.csv.CSV;
import org.laukvik.csv.CsvWriter;
import org.laukvik.sql.DatabaseConnectionNotFoundException;
import org.laukvik.sql.ResultSetExporter;
import org.laukvik.sql.SQL;
import org.laukvik.sql.ddl.DatabaseConnection;
import org.laukvik.sql.ddl.Function;
import org.laukvik.sql.ddl.Schema;
import org.laukvik.sql.ddl.Sqlable;
import org.laukvik.sql.ddl.Table;
import org.laukvik.sql.ddl.View;
import org.laukvik.sql.swing.icons.ResourceManager;

/**
 *
 * @author morten
 */
public class Viewer extends javax.swing.JFrame implements ConnectionDialogListener {

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
    private ConnectionDialog connectionPanel;

    /**
     * Creates new form SQL
     */
    public Viewer(SQL sql) {
        super();
        emptyPanel = new JPanel();

        initComponents();

        newMenuItem.setAccelerator(ResourceManager.getKeyStroke(KeyEvent.VK_N));
        openMenuItem.setAccelerator(ResourceManager.getKeyStroke(KeyEvent.VK_O));
        saveMenuItem.setAccelerator(ResourceManager.getKeyStroke(KeyEvent.VK_S));
        quitMenuItem.setAccelerator(ResourceManager.getKeyStroke(KeyEvent.VK_Q));

        importMenuItem.setAccelerator(ResourceManager.getKeyStroke(KeyEvent.VK_I));
        exportMenuItem.setAccelerator(ResourceManager.getKeyStroke(KeyEvent.VK_E));


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
        jToolBar1.setVisible(false);
        setSize(Toolkit.getDefaultToolkit().getScreenSize());

    }

    public void setSQL(SQL sql) {
        this.sql = sql;
        treeModel = new TreeModel(sql);
        tree.setCellRenderer(treeModel);
        tree.setModel(treeModel);

        diagramPanel.removeAll();
        if (sql.getSchema() == null) {
            setTitle("");
        } else {
            for (Table t : sql.getSchema().getTables()) {
                diagramPanel.addTable(t);
            }
            tree.setSelectionPath(new TreePath(treeModel.getRoot()));
            setTitle(sql.getDatabaseConnection().getName());
        }

        diagramPanel.autoLayout();

    }

    public void openDiagram() {
        mainSplitPane.setRightComponent(diagramScroll);
        mainSplitPane.setDividerLocation(DEFAULT_TREE_WIDTH);
    }

    public void openFunction(Function function) {
        LOG.info("Function: " + function.getName());
        try {
            sql.displayFunction(function);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void openView(View view) {
        LOG.info("View: " + view.getName());
    }

    public void openTable(Table t) {
        LOG.info("Table: " + t.getName());
        mainSplitPane.setRightComponent(tableSplitPane);
        mainSplitPane.setDividerLocation(DEFAULT_TREE_WIDTH);
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
            /* Table items */
            Table t = (Table) sqlable;
            openTable(t);

        } else if (sqlable instanceof Function) {
            /* Function items */
        } else {
            /* Other items */
            openDiagram();
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
        newMenuItem = new javax.swing.JMenuItem();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        importMenuItem = new javax.swing.JMenuItem();
        exportMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        quitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        viewQueryMenuItem = new javax.swing.JCheckBoxMenuItem();
        viewDDLMenuItem = new javax.swing.JCheckBoxMenuItem();
        helpMenu = new javax.swing.JMenu();
        contentsMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        mainSplitPane.setBorder(null);
        mainSplitPane.setDividerLocation(200);

        tree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                treeValueChanged(evt);
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

        newMenuItem.setText("New");
        newMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(newMenuItem);

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
        fileMenu.add(jSeparator2);

        importMenuItem.setText("Import");
        importMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(importMenuItem);

        exportMenuItem.setText("Export");
        exportMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exportMenuItem);
        fileMenu.add(jSeparator1);

        quitMenuItem.setMnemonic('x');
        quitMenuItem.setText("Quit");
        quitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(quitMenuItem);

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

        viewMenu.setText("View");

        viewQueryMenuItem.setText("Query");
        viewMenu.add(viewQueryMenuItem);

        viewDDLMenuItem.setText("Table definition");
        viewMenu.add(viewDDLMenuItem);

        menuBar.add(viewMenu);

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

    private void quitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_quitMenuItemActionPerformed

    private void treeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeValueChanged
        if (evt.getPath() != null) {
            Object o = evt.getPath().getLastPathComponent();

            if (o instanceof Schema) {
                openDiagram();

            } else if (o instanceof Table) {
                openTable((Table) o);

            } else if (o instanceof View) {
                openView((View) o);

            } else if (o instanceof Function) {
                openFunction((Function) o);

            } else {

            }
        }

    }//GEN-LAST:event_treeValueChanged

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        LOG.info("openMenu");

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

    private void importMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importMenuItemActionPerformed
        LOG.info("importMenuItem: ");
    }//GEN-LAST:event_importMenuItemActionPerformed

    private void exportMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportMenuItemActionPerformed
        LOG.info("exportMenuItem: ");
        FileDialog dialog = new FileDialog(this);
        dialog.setLocationRelativeTo(null);
        dialog.setFilenameFilter( new BackupFormatFileFilter() );




        TreePath path = tree.getSelectionPath();
        if (path == null){

        } else {
            Object o = path.getLastPathComponent();
            LOG.info("export: pathComponent=" + o);
            if (o instanceof Table){
                Table t = (Table)o;
                dialog.setTitle("Export table to file");
                dialog.setFile( t.getName() + BackupFormatFileFilter.EXTENSION);
                dialog.setMode( FileDialog.SAVE );
                dialog.setVisible(true);
                if (dialog.getName().isEmpty()){
                    JOptionPane.showMessageDialog(this,"You didnt specify a filename!");
                } else {
                    File file = new File(dialog.getDirectory(),dialog.getFile());
                    ResultSetExporter exporter = new ResultSetExporter( sql.getDatabaseConnection() );
                    try {
                        exporter.export(t, file);
                        JOptionPane.showMessageDialog(this,"Exported: " + file.getAbsolutePath());

                    } catch (FileNotFoundException e) {
                        JOptionPane.showMessageDialog(this,e.getMessage());
                        e.printStackTrace();
                    }
                }

            } else if (o instanceof Schema){
                Schema s = (Schema)o;
                System.setProperty("apple.awt.fileDialogForDirectories", "true");
                dialog.setTitle("Export all tables to file");
                dialog.setMode( FileDialog.SAVE );
                dialog.setVisible(true);

                /*
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
                fc.showSaveDialog(this);
*/

                if (dialog.getName().isEmpty()){
                    JOptionPane.showMessageDialog(this,"You didn't specify a filename!");
                } else if (dialog.getDirectory() == null){

                } else {

                    File file = new File(dialog.getDirectory(),dialog.getFile());
                    file.mkdir();
                    ResultSetExporter exporter = new ResultSetExporter( sql.getDatabaseConnection() );

                    try {
                        exporter.exportTables(file);
                        JOptionPane.showMessageDialog(this,"Exported: " + file.getAbsolutePath());
                    } catch (FileNotFoundException e) {
                        JOptionPane.showMessageDialog(this,e.getMessage());
                        e.printStackTrace();
                    } catch (SQLException e) {
                        JOptionPane.showMessageDialog(this,e.getMessage());
                        e.printStackTrace();
                    } catch (DatabaseConnectionNotFoundException e) {
                        JOptionPane.showMessageDialog(this,e.getMessage());
                        e.printStackTrace();
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(this,e.getMessage());
                        e.printStackTrace();
                    }

                }
                System.setProperty("apple.awt.fileDialogForDirectories", "false");
            }
        }



    }//GEN-LAST:event_exportMenuItemActionPerformed

    private DatabaseConnection unsavedConnection;

    private void newMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newMenuItemActionPerformed
        unsavedConnection = new DatabaseConnection();
        unsavedConnection.setServer("localhost");
        connectionPanel = new ConnectionDialog(unsavedConnection, this);
        connectionPanel.setSize(440, 350);
        connectionPanel.setLocationRelativeTo(null);
        connectionPanel.setModal(true);
        connectionPanel.setVisible(true);


    }//GEN-LAST:event_newMenuItemActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem contentsMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JScrollPane ddlSplitPane;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exportMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem importMenuItem;
    private javax.swing.JScrollPane jScrollPaneQuery;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JSplitPane mainSplitPane;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JSplitPane queryAndResultSplitPane;
    private javax.swing.JTextPane queryPane;
    private javax.swing.JMenuItem quitMenuItem;
    private javax.swing.JTable resultTable;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JTable tableDDL;
    private javax.swing.JScrollPane tableScroll;
    private javax.swing.JSplitPane tableSplitPane;
    private javax.swing.JToggleButton toggleDDL;
    private javax.swing.JToggleButton toggleQuery;
    private javax.swing.JTree tree;
    private javax.swing.JScrollPane treeScrollPane;
    private javax.swing.JCheckBoxMenuItem viewDDLMenuItem;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JCheckBoxMenuItem viewQueryMenuItem;
    // End of variables declaration//GEN-END:variables

    @Override
    public void accepted(DatabaseConnection connection) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void denied() {

    }

    @Override
    public boolean canConnect(DatabaseConnection connection) {
        return sql.getConnection() != null;
    }

}
