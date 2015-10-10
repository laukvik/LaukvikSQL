package org.laukvik.sql.swing;

import javax.swing.*;

/**
 * 
 *
 */
public class ResourceManager {

    public static Icon getIcon(String filename) {
        return new javax.swing.ImageIcon(ResourceManager.class.getResource("/org/laukvik/sql/swing/icons/" + filename));
    }

}
