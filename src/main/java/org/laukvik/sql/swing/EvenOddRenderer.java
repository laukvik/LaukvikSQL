/*
 * Copyright (C) 2015 Morten Laukvik <morten@laukvik.no>
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
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Morten Laukvik <morten@laukvik.no>
 */
public class EvenOddRenderer implements TableCellRenderer {

    public static final DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();

    final static Color ODD = new Color(255, 255, 255);
    final static Color EVEN = new Color(245, 245, 245);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        Component renderer = DEFAULT_RENDERER.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
        ((JLabel) renderer).setOpaque(true);
        Color foreground, background;
        if (isSelected) {
//            foreground = Color.yellow;
//            background = Color.green;
        } else {
            if (row % 2 == 0) {
//                foreground = ODD;
//                background = Color.white;
                renderer.setBackground(ODD);

            } else {
                renderer.setBackground(EVEN);
//            renderer.setBackground(background);
            }
        }
//        renderer.setForeground(foreground);
//        renderer.setBackground(background);
        return renderer;
    }
}
