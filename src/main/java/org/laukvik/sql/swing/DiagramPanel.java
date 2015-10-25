package org.laukvik.sql.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;

import org.laukvik.csv.CSV;
import org.laukvik.sql.ddl.Column;
import org.laukvik.sql.ddl.ForeignKey;
import org.laukvik.sql.ddl.Table;
import org.laukvik.sql.swing.icons.ResourceManager;

public class DiagramPanel extends JPanel implements MouseListener, MouseMotionListener {

    private final Logger LOG = Logger.getLogger(DiagramPanel.class.getName());

    private Color BACKGROUND = new Color(242, 242, 242);
    private Color TABLE_OUTLINE = new Color(160, 160, 160);
    private Color TABLE_HEADER_BACKGROUND = new Color(240, 156, 49);
    private Color TABLE_HEADER_FOREGROUND = new Color(255,255, 255);
    private Color TABLE_TEXT = new Color(0,0,0);
    private Color TABLE_FILL = new Color(255, 255, 255);
    private Color FOREIGNKEY = new Color(170, 170, 170);
    final static Icon PKICON = ResourceManager.getIcon("pk_decorate.gif");
    final static Icon FKICON = ResourceManager.getIcon("pkfk_decorate.gif");
    final static int rowHeight = 20;
    final static int headerHeight = 20;
    final static int tableWidth = 150;
    final static int padding = 0;

    private List<Table> tables;
    private List<Point> locations;
    private List<Column> foreignKeys;
    private File file;

    public DiagramPanel() {
        super();
        this.file = null;
        setAutoscrolls(true);
        setBackground(BACKGROUND);
        tables = new ArrayList<>();
        locations = new ArrayList<>();
        foreignKeys = new ArrayList<>();
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void removeTables() {
        tables.clear();
        locations.clear();
        fireTablesChanged();
        repaint();
    }

    /**
     * Adds a table
     *
     * @param table
     */
    public void addTable(Table table) {
        LOG.fine("Added table: " + table );
        tables.add(table);
        locations.add(new Point(0, 0));
        fireTablesChanged();
    }

    /**
     * Creates a grid locations for all tables
     *
     */
    public void autoLayout(int panelWidth){
        for (int index = 0; index < tables.size(); index++) {
            int blockWidth = tableWidth + padding;
            int blockHeight = 200;
            int tablesPrRow = panelWidth / blockWidth;
            locations.get(index).setLocation((index % tablesPrRow) * blockWidth, (index / tablesPrRow) * blockHeight);
        }
        setSize(calculateSize());
    }

    public void removeTable(Table table) {
        int index = tables.indexOf(table);
        tables.remove(index);
        locations.remove(index);
        fireTablesChanged();
    }

    public void fireTablesChanged() {
        setPreferredSize(calculateSize());
        setSize(calculateSize());
        findForeignKeys();
    }

    /**
     *
     *
     * @param point
     * @param table
     */
    public void setTableLocation(Point point, Table table) {
        LOG.fine("Setting location for table " + table + " to " + point);
        int index = tables.indexOf(table);
        if (index == -1) {
            LOG.finest("Table " + table.getName() + " was not found!");
        } else {
            locations.get(index).x = point.x;
            locations.get(index).y = point.y;
        }
        fireTablesChanged();
    }

    private Column findPrimaryKey(ForeignKey fk) {
        for (Table t : tables) {
            if (t.getName().equalsIgnoreCase(fk.getTable())) {
                for (Column c : t.getColumns()) {
                    if (c.getName().equalsIgnoreCase(fk.getColumn())) {
                        return c;
                    }
                }
            }
        }
        return null;
    }

    private void findForeignKeys() {
         foreignKeys.clear();
         for (Table t : tables) {
             for (Column c : t.getColumns()) {
                 if (c.getForeignKey() != null) {
                     Column primaryKey = findPrimaryKey(c.getForeignKey());
                     foreignKeys.add(c);
                 }
             }
         }
    }

    public int getIndex(Table table) {
        return tables.indexOf(table);
    }

    private Column findColumnTarget(ForeignKey fk) {
        for (Table t : tables) {
            if (t.getName().equalsIgnoreCase(fk.getTable())) {
                Column c = t.findColumnByName(fk.getColumn());
                return c;
            }
        }
        return null;
    }

    /**
     * Paints the foreign key line connecting the two tables
     *
     * @param column
     * @param g
     */
    private void paintForeignKey(Column column, Graphics g) {
        int tableIndex = getIndex(column.getTable());
        if (column.getForeignKey() == null) {
            // No foreign key
        } else {
            Column pk = findColumnTarget(column.getForeignKey());

            if (pk == null) {
                // Should never happen
            } else {
                int endTableIndex = getIndex(pk.getTable());

                Point start = new Point(locations.get(tableIndex));
                start.y += column.index() * rowHeight + headerHeight + (rowHeight / 2);
                start.x += tableWidth;

                Point end = new Point(locations.get(endTableIndex));
                end.y += pk.index() * rowHeight + headerHeight + (rowHeight / 2);

                paintForeignKeyLine(g, start, end );
            }
        }
    }

    /**
     * Paints a foreignKey line between two points
     *
     * @param g
     * @param start
     * @param end
     */
    private void paintForeignKeyLine(Graphics g, Point start, Point end ){
        g.setColor(FOREIGNKEY);
        g.drawLine(start.x, start.y, end.x, end.y);
    }

    /**
     * Paints the whole component both tables, icons and foreign key connectors
     *
     */
    public void paint(Graphics g) {
        /* Turn anti-aliasing on to smooth corners and lines */
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //g2.setBackground(BACKGROUND);
        //g2.clearRect(0,0,getWidth(),getHeight());

        /* Paint the foreign key lines */
        for (Column fk : foreignKeys) {
            paintForeignKey(fk, g);
        }

        /* Paint each table */
        for (Table t : tables) {
            paintTable(t, g);
        }
    }

    public Dimension calculateSize() {
        int rightMost = 0;
        int bottomMost = 0;
        int x = 0;
        for (Table t : tables) {
            Rectangle r = getRectangle(t);
            if (r.x + r.width > rightMost) {
                rightMost = r.x + r.width;
            }
            if (r.y + r.height > bottomMost) {
                bottomMost = r.y + r.height;
            }
            x++;
        }
        return new Dimension(rightMost, bottomMost);
    }

    private Rectangle getRectangle(Table table) {
        return new Rectangle(locations.get(tables.indexOf(table)), new Dimension(tableWidth, (table.getColumns().size() + 1) * rowHeight));
    }

    private void paintTable(Table table, Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(1));

        Point p = locations.get(tables.indexOf(table));
        int x = p.x;
        int y = p.y;
        int height = rowHeight * (table.getColumns().size() + 1);
        g.setColor(TABLE_OUTLINE);
        g.fillRoundRect(x, y, tableWidth, height, 10, 10);
        g.setColor(TABLE_FILL);
        g.fillRoundRect(x + 1, y + 1, tableWidth - 2, height - 2, 10, 10);

        /* Draw table name centered */
        int textWidth = getFontMetrics(getFont()).stringWidth(table.getName());
        int textHeight = getFontMetrics(getFont()).getHeight();

        g.setColor(TABLE_HEADER_BACKGROUND);
        g.fillRoundRect(x + 1, y + 1, tableWidth - 2, 20, 10, 10);
        g.fillRect(x + 1, y + 12, tableWidth - 2, 10);

        g.setColor(TABLE_HEADER_FOREGROUND);
        g.drawString(table.getName(), p.x + ((150 - textWidth) / 2), p.y + textHeight);

        /* Paint columns */
        g.setColor(TABLE_TEXT);
        for (int n = 0; n < table.getColumns().size(); n++) {
            Column c = table.getColumns().get(n);
            if (c.isPrimaryKey()) {
                PKICON.paintIcon(this, g, x + 5, y + 20 + n * rowHeight);
            }
            g.drawString(c.getName(), x + 25, y + 35 + n * rowHeight);

            if (c.getForeignKey() != null) {
                FKICON.paintIcon(this, g, x + 130, y + 20 + n * rowHeight);
            }
        }
    }


    /**
     * DRAG N DROP STUFF *
     */
    Point startPoint, endPoint;
    boolean isDragging = false;
    Table tableDrag;
    int minusX, minusY;

    public void mouseClicked(MouseEvent evt) {
        if (evt.getClickCount() == 2) {
            try {
                write(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
        LOG.finest("mouseReleased: " + e.getX() + "," + e.getY());
        if (isDragging) {
            endPoint = e.getPoint();
            try {
                write(file);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        isDragging = false;
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void mouseDragged(MouseEvent e) {
		LOG.finest( "mouseDragged: " + e.getX() + "," + e.getY()  );
        if (tableDrag != null) {
            isDragging = true;
            Point p = new Point(e.getPoint());
            p.x -= minusX;
            p.y -= minusY;
            setTableLocation(p, tableDrag);
            setPreferredSize(calculateSize());
            repaint();
        }
    }

    /**
     * Mouse moved event listener
     *
     * @param e
     */
    public void mouseMoved(MouseEvent e) {
        LOG.finest("mouseMoved: " + e.getX() + "," + e.getY());
        for (int x = 0; x < locations.size(); x++) {
            Point p = locations.get(x);
            Table t = tables.get(x);
            Rectangle r = new Rectangle(p.x, p.y, tableWidth, rowHeight);
            if (r.contains(e.getPoint())) {
                tableDrag = t;
                startPoint = e.getPoint();
                minusX = startPoint.x - p.x;
                minusY = startPoint.y - p.y;
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				LOG.finest( "mouseOver: " + t.getName() );
                return;
            }
        }
        tableDrag = null;
        startPoint = null;
        endPoint = null;

        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Writes the diagram to file - the name and location of each table
     *
     * @param file
     * @throws IOException
     */
    public void write(File file) throws IOException {
        LOG.fine("Writing diagram to file " + file.getAbsolutePath());
        CSV csv = new CSV();
        csv.addColumn("table");
        csv.addColumn("x");
        csv.addColumn("y");
        for (int y=0; y<tables.size(); y++){
            Point p = locations.get(y);
            Table t = tables.get(y);
            csv.addRow( t.getName(), p.x +"", p.y+"");
        }
        csv.write(file);
        this.file = file;
    }

    /**
     * Reads diagram from file - the name and location of each table
     *
     * @param file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void read(File file) throws Exception {
        LOG.fine("Reading diagram from file " + file.getAbsolutePath());
        if (!file.exists()){
            this.file = file;
        } else {
            CSV csv = new CSV( file );
            for (int n=0; n<csv.getRowCount(); n++){
                String table = csv.getRow(n).getString("table");
                int x = csv.getRow(n).getInteger("x");
                int y = csv.getRow(n).getInteger("y");
                Table t = findTableByName(table);
                LOG.info("Table: " + table + " " + x + "/" + y);
                if (t != null){
                    setTableLocation( new Point(x,y), t);
                }
            }
            this.file = file;
            fireTablesChanged();
        }
    }

    /**
     * Finds a table by its name
     *
     * @param name
     * @return
     */
    public Table findTableByName(String name) {
        for (Table t : tables){
            if (t.getName().equalsIgnoreCase(name)){
                return t;
            }
        }
        return null;
    }

}
