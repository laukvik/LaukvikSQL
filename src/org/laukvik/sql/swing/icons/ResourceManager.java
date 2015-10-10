package org.laukvik.sql.swing.icons;

import javax.swing.*;
import java.net.URL;

/**
 *
 *
 */
public class ResourceManager {

    public static Icon getIcon(String filename) {
        try{
            return new ImageIcon(ResourceManager.class.getResource( filename));
        }catch (Exception e){
            return new ImageIcon();
        }

    }

}
