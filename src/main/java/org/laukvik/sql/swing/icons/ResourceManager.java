package org.laukvik.sql.swing.icons;

import org.laukvik.sql.swing.TreeModel;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

/**
 *
 *
 */
public class ResourceManager {

    public static boolean isRetina() {
        return true;
    }

    public static boolean isMac() {
        return (System.getProperty("os.name").toLowerCase().startsWith("mac os"));
    }

    /**
     *
     * @param key
     * @return
     * @see KeyEvent.VK_C
     */
    public static KeyStroke getKeyStroke(int key) {
        if (isMac()) {
            return KeyStroke.getKeyStroke(key, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        } else {
            return KeyStroke.getKeyStroke(key, KeyEvent.CTRL_DOWN_MASK);
        }
    }

    public static Icon getIcon(String filename) {
        try {
            return new ImageIcon(ResourceManager.class.getClassLoader().getResource(filename));
        } catch (Exception e) {
            return new ImageIcon();
        }
    }

    public static File getResource(String filename) {
        ClassLoader classLoader = TreeModel.class.getClassLoader();
        return new File(classLoader.getResource(filename).getFile());
    }

}
