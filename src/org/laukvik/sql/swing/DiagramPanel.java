package org.laukvik.sql.swing;


import java.awt.BasicStroke;
import java.awt.BorderLayout;
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
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.laukvik.sql.ddl.*;
import org.laukvik.sql.swing.ResourceManager;

public class DiagramPanel extends JPanel implements MouseListener, MouseMotionListener {

    private static final long serialVersionUID = 1L;

    List<Table> tables;
    List<Point> locations;

    final static Color OUTLINE_COLOR = Color.black;
    final static Color HEADER_BACKGROUND = new Color(240, 160, 40);
    final static Color HEADER_FOREGROUND = Color.white;
    final static Color TEXT_COLOR = Color.BLACK;
    final static Color FILL = Color.WHITE;
    final static Color LINK = Color.BLACK;

    final static Icon PKICON = ResourceManager.getIcon("pk_decorate.gif");
    final static Icon FKICON = ResourceManager.getIcon("pkfk_decorate.gif");

    List<Column> foreignKeys;
    final static int rowHeight = 20;
    final static int headerHeight = 20;
    final static int tableWidth = 150;
    boolean tablesChanged = false;
    File file;

    public DiagramPanel() {
        super();
        setAutoscrolls(true);
        setBackground(Color.white);
        tables = new ArrayList<Table>();
        locations = new ArrayList<Point>();
        foreignKeys = new ArrayList<Column>();
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void loadLocations(File file) throws FileNotFoundException, IOException {
        this.file = file;
        read(file);
    }

    public void removeAll() {
        tables.clear();
        locations.clear();
        fireTablesChanged();
        repaint();
    }

    /**
     *
     * @param table
     */
    public void addTable(Table table) {
        int index = tables.size();
        tables.add(table);

        int panelWidth   = 700;
        int tableWidth   = 150;
        int padding      = 20;

        int tablesPrRow  = panelWidth / tableWidth;

        //locations.add(new Point((index % tablesPrRow)*200, (index / tablesPrRow)*200 ));
        locations.add(new Point(0,0));
        fireTablesChanged();
    }

    public void autoLayout(){
        for (int index=0; index<tables.size(); index++){
            int panelWidth   = getWidth();
            int tableWidth   = 150;
            int padding      = 20;

            int tablesPrRow  = panelWidth / tableWidth;

            locations.get(index).setLocation((index % tablesPrRow)*200, (index / tablesPrRow)*200);
            //locations.add(new Point((index % tablesPrRow)*200, (index / tablesPrRow)*200 ));
        }
    }

    public void removeTable(Table table) {
        int index = tables.indexOf(table);
        tables.remove(index);
        locations.remove(index);
        fireTablesChanged();
    }

    public void fireTablesChanged() {
        tablesChanged = true;
        setPreferredSize(calculateSize());
        setSize(calculateSize());
    }

    public void setLocation(Point point, Table table) {
        int index = tables.indexOf(table);
        if (index == -1) {
            System.err.println("Table " + table.getName() + " not found!");
        } else {
            locations.get(index).x = point.x;
            locations.get(index).y = point.y;
        }
        fireTablesChanged();
    }

    public Column findPrimaryKey(ForeignKey fk) {
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

    public void findForeignKeys() {
        /*
        foreignKeys.clear();
        for (Table t : tables) {
//			System.out.println( "Checking table " + t.getName() );
            for (Column c : t.getColumns()) {
//				System.out.println( "Checking column " + c.getName() );
                if (c.getForeignKey() != null) {
//					System.out.println( "Found link: " );
                    Column primaryKey = findPrimaryKey(c.foreignKey);
                    c.foreignKey.setColumnTarget(primaryKey);
                    foreignKeys.add(c);
                }
            }
        }
        */
    }

    public int getIndex(Table table) {
        return tables.indexOf(table);
    }


    public Column findColumnTarget( ForeignKey fk ){
        for (Table t : tables){
            if (t.getName().equalsIgnoreCase(fk.getTable())){
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
    public void paintForeignKey(Column column, Graphics g) {
        int tableIndex = getIndex(column.getTable());
        if (column.getForeignKey() == null){
            // No foreign key
        } else {
            Column pk = findColumnTarget(column.getForeignKey());
            if (pk == null){
                // Should never happen
            } else {
                int endTableIndex = getIndex(pk.getTable());

                Point start = new Point(locations.get(tableIndex));
                start.y += column.index() * rowHeight + headerHeight + (rowHeight / 2);
                start.x += tableWidth;

                Point end = new Point(locations.get(endTableIndex));
                end.y += pk.index() * rowHeight + headerHeight + (rowHeight / 2);

                g.setColor(Color.BLACK);
                g.drawLine(start.x, start.y, end.x, end.y);
            }
        }
    }

    /**
     * Paints the whole component both tables, icons and foreign key connectors
     *
     */
    public void paint(Graphics g) {
        if (tablesChanged) {
            findForeignKeys();
            tablesChanged = false;
        }

        /* Turn anti-aliasing on to smooth corners and lines */
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        /* Paint each table */
        for (Table t : tables) {
            paintTable(t, g);
        }

        /* Paint the foreign key lines */
        for (Column fk : foreignKeys) {
            paintForeignKey(fk, g);
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

    public Rectangle getRectangle(Table table) {
        return new Rectangle(locations.get(tables.indexOf(table)), new Dimension(tableWidth, (table.getColumns().size() + 1) * rowHeight));
    }

    public void paintTable(Table table, Graphics g) {

        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(1));

        Point p = locations.get(tables.indexOf(table));
        int x = p.x;
        int y = p.y;
        int height = rowHeight * (table.getColumns().size() + 1);
        g.setColor(OUTLINE_COLOR);
        g.fillRoundRect(x, y, tableWidth, height, 10, 10);
        g.setColor(FILL);
        g.fillRoundRect(x + 1, y + 1, tableWidth - 2, height - 2, 10, 10);

        /* Draw table name centered */
        int textWidth = getFontMetrics(getFont()).stringWidth(table.getName());
        int textHeight = getFontMetrics(getFont()).getHeight();

        g.setColor(HEADER_BACKGROUND);
        g.fillRoundRect(x + 1, y + 1, tableWidth - 2, 20, 10, 10);
        g.fillRect(x + 1, y + 12, tableWidth - 2, 10);

        g.setColor(HEADER_FOREGROUND);
        g.drawString(table.getName(), p.x + ((150 - textWidth) / 2), p.y + textHeight);

        /* Paint columns */
        g.setColor(TEXT_COLOR);
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
     * @param args
     */
    public static void main(String[] args) {
        // Employee
        Table employee = new Table("Employee");

        Column eID = new IntegerColumn("employeeID");
        eID.setPrimaryKey(true);
        eID.setAllowNulls(true);
        employee.addColumn(eID);

        employee.addColumn(new VarCharColumn("firstName"));
        employee.addColumn(new VarCharColumn("lastName"));
        employee.addColumn(new VarCharColumn("email"));

        IntegerColumn employeeCompanyID = new IntegerColumn("companyID");
        employeeCompanyID.setForeignKey(new ForeignKey("Company", "companyID"));

        IntegerColumn employeeDepartmentID = new IntegerColumn("departmentID");
        employeeDepartmentID.setForeignKey(new ForeignKey("Department", "departmentID"));

        // Company
        Table company = new Table("Company");
        Column cID = new IntegerColumn("companyID");
        cID.setPrimaryKey(true);
        company.addColumn(cID);
        company.addColumn(new VarCharColumn("name"));

        // Department
        Table department = new Table("Department");
        Column dID = new IntegerColumn("departmentID");
        dID.setPrimaryKey(true);
        dID.setAllowNulls(false);
        department.addColumn(dID);
        department.addColumn(new VarCharColumn("name"));
        department.addColumn(new VarCharColumn("contact"));


        IntegerColumn companyID = new IntegerColumn("companyID");
        ForeignKey contactFK = new ForeignKey("Company", "companyID");
        companyID.setForeignKey(contactFK);


        DiagramPanel panel = new DiagramPanel();
        panel.addTable(employee);
        panel.addTable(company);
        panel.addTable(department);

        panel.setLocation(new Point(50, 50), employee);
        panel.setLocation(new Point(300, 50), company);
        panel.setLocation(new Point(300, 300), department);

        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(panel), BorderLayout.CENTER);
        frame.setSize(500, 500);
        frame.setVisible(true);

        panel.repaint();

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
//		System.out.println( "mouseEntered: " );
    }

    public void mouseExited(MouseEvent e) {
//		System.out.println( "mouseExited: " );
    }

    public void mousePressed(MouseEvent e) {
//		System.out.println( "mousePressed: " + e.getX() + "," + e.getY()  );
    }

    public void mouseReleased(MouseEvent e) {
//		System.out.println( "mouseReleased: " + e.getX() + "," + e.getY()  );
        if (isDragging) {
            endPoint = e.getPoint();
        }
        isDragging = false;

        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void mouseDragged(MouseEvent e) {
//		System.out.println( "mouseDragged: " + e.getX() + "," + e.getY()  );
        if (tableDrag != null) {
            isDragging = true;
            Point p = new Point(e.getPoint());
            p.x -= minusX;
            p.y -= minusY;
            setLocation(p, tableDrag);

            setPreferredSize(calculateSize());
            repaint();
        }

    }

    public void mouseMoved(MouseEvent e) {
//		System.out.print( "mouseMoved: " + e.getX() + "," + e.getY()  );

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
//				System.out.println( "mouseOver: " + t.getName() );
                return;
            }
        }
        tableDrag = null;
        startPoint = null;
        endPoint = null;
        try {
            write(file);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }


    public void write(File file) throws IOException {
        /*
        CSV csv = new CSV();
        csv.foundHeaders(new String[]{"x", "y", "table"});
        for (int y = 0; y < tables.size(); y++) {
            Point p = locations.get(y);
            Table t = tables.get(y);
            csv.foundRow(y, new String[]{p.x + "", p.y + "", t.getName()});
        }
        csv.write(file);
        */
    }

    public void read(File file) throws FileNotFoundException, IOException {
        /*
        CSV csv = new CSV();
        csv.parse(file);
        for (int y = 0; y < csv.getRowCount(); y++) {
            try {
                String table = csv.getCell(2, y);
                Table t = getTable(table);
                if (t == null) {
                } else {
                    int px = Integer.parseInt(csv.getCell(0, y));
                    int py = Integer.parseInt(csv.getCell(1, y));
                    setLocation(new Point(px, py), t);
                }
            } catch (CSVRowNotFoundException e) {
                e.printStackTrace();
            } catch (CSVColumnNotFoundException e) {
                e.printStackTrace();
            }
        }
        fireTablesChanged();
        */
    }

    public Table getTable(String table) {
        for (Table t : tables) {
            if (t.getName().equalsIgnoreCase(table)) {
                return t;
            }
        }
        return null;
    }

}
