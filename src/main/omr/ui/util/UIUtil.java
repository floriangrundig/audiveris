//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                                           U I U t i l                                          //
//                                                                                                //
//------------------------------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">
//  Copyright © Hervé Bitteur and others 2000-2014. All rights reserved.
//  This software is released under the GNU General Public License.
//  Goto http://kenai.com/projects/audiveris to report bugs or suggestions.
//------------------------------------------------------------------------------------------------//
// </editor-fold>
package omr.ui.util;

import omr.WellKnowns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Container;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

/**
 * Class {@code UIUtil} gathers utilities related to User Interface
 *
 * @author Hervé Bitteur and Brenton Partridge
 */
public abstract class UIUtil
{
    //~ Static fields/initializers -----------------------------------------------------------------

    private static final Logger logger = LoggerFactory.getLogger(UIUtil.class);

    /**
     * Customized border for tool buttons, to use consistently in all UI
     * components.
     */
    private static Border toolBorder;

    public static final WindowListener closeWindow = new WindowAdapter()
    {
        @Override
        public void windowClosing (WindowEvent e)
        {
            e.getWindow().dispose();
        }
    };

    //~ Methods ------------------------------------------------------------------------------------
    //------------------//
    // directoryChooser //
    //------------------//
    /**
     * Let the user select a directory.
     *
     * @param parent   the parent component for the dialog
     * @param startDir the starting directory
     * @param title    specific dialog title if any, null otherwise
     * @return the chosen directory, or null
     */
    public static File directoryChooser (Component parent,
                                         File startDir,
                                         String title)
    {
        //        String oldMacProperty = System.getProperty("apple.awt.fileDialogForDirectories", "false");
        //        System.setProperty("apple.awt.fileDialogForDirectories", "true");
        OmrFileFilter filter = new OmrFileFilter("Directories", new String[]{})
        {
            @Override
            public boolean accept (File f)
            {
                return (f.isDirectory());
            }
        };

        final JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES); // FILES needed if dir doesn't exist
        fc.addChoosableFileFilter(filter); // To display directories list
        fc.setFileFilter(filter); // To display this list by default
        ///fc.setAcceptAllFileFilterUsed(false); // Don't display the AllFiles list

        if (title != null) {
            fc.setDialogTitle(title);
        }

        ///
        if (startDir != null) {
            // Pre-select the proposed directory
            File parentDir = startDir.getParentFile();
            fc.setCurrentDirectory(parentDir);
            fc.setSelectedFile(startDir);
        }

        final int result = fc.showSaveDialog(parent);

        //        System.setProperty("apple.awt.fileDialogForDirectories", oldMacProperty);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile();
        } else {
            return null;
        }
    }

    //---------------//
    // enableActions //
    //---------------//
    /**
     * Given a list of actions, set all these actions (whether they
     * derive from AbstractAction or AbstractButton) enabled or not,
     * according to the bool parameter provided.
     *
     * @param actions list of actions to enable/disable as a whole
     * @param bool    true for enable, false for disable
     */
    public static void enableActions (Collection<?> actions,
                                      boolean bool)
    {
        for (Iterator<?> it = actions.iterator(); it.hasNext();) {
            Object next = it.next();

            if (next instanceof AbstractAction) {
                ((AbstractAction) next).setEnabled(bool);
            } else if (next instanceof AbstractButton) {
                ((AbstractButton) next).setEnabled(bool);
            } else {
                logger.warn("Neither Button nor Action : {}", next);
            }
        }
    }

    //-------------//
    // fileChooser //
    //-------------//
    /**
     * A replacement for standard JFileChooser, to allow better look and feel on the Mac
     * platform.
     *
     * @param save      true for a SAVE dialog, false for a LOAD dialog
     * @param parent    the parent component for the dialog, if any
     * @param startFile default file, or just default directory, or null
     * @param filter    a filter to by applied on files
     * @return the selected file, or null
     */
    public static File fileChooser (boolean save,
                                    Component parent,
                                    File startFile,
                                    OmrFileFilter filter)
    {
        return fileChooser(save, parent, startFile, filter, null);
    }

    //-------------//
    // fileChooser //
    //-------------//
    /**
     * A replacement for standard JFileChooser, to allow better look and feel on the Mac
     * platform.
     *
     * @param save      true for a SAVE dialog, false for a LOAD dialog
     * @param parent    the parent component for the dialog, if any
     * @param startFile default file, or just default directory, or null
     * @param filter    a filter to by applied on files
     * @param title     a specific dialog title or null
     * @return the selected file, or null
     */
    public static File fileChooser (boolean save,
                                    Component parent,
                                    File startFile,
                                    OmrFileFilter filter,
                                    String title)
    {
        File file = null;

        if (WellKnowns.MAC_OS_X) {
            if ((parent == null) && (omr.OMR.getGui() != null)) {
                parent = omr.OMR.getGui().getFrame();
            }

            Component parentFrame = parent;

            while (parentFrame.getParent() != null) {
                parentFrame = parentFrame.getParent();
            }

            try {
                final FileDialog fd = new FileDialog((Frame) parentFrame);

                if (startFile != null) {
                    fd.setDirectory(
                            startFile.isDirectory() ? startFile.getPath() : startFile.getParent());
                }

                fd.setMode(save ? FileDialog.SAVE : FileDialog.LOAD);
                fd.setFilenameFilter(filter);

                if (title == null) {
                    title = save ? "Saving: " : "Loading: ";
                    title += filter.getDescription();
                }

                fd.setTitle(title);
                fd.setVisible(true);

                String fileName = fd.getFile();
                String dir = fd.getDirectory();

                if ((dir != null) && (fileName != null)) {
                    String fullName = dir + WellKnowns.FILE_SEPARATOR + fileName;
                    file = new File(fullName);
                }
            } catch (ClassCastException e) {
                logger.warn("no ancestor is Frame");
            }
        } else {
            ///final JFileChooser fc = new JFileChooser();
            // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6317789
            final JFileChooser fc = new JFileChooser()
            {
                @Override
                public void updateUI ()
                {
                    putClientProperty("FileChooser.useShellFolder", false);
                    super.updateUI();
                }
            };

            // Pre-select the directory, and potentially the file to save to
            if (startFile != null) {
                if (startFile.isDirectory()) {
                    fc.setCurrentDirectory(startFile);
                } else {
                    File parentFile = startFile.getParentFile();
                    fc.setCurrentDirectory(parentFile);
                    fc.setSelectedFile(startFile);
                }
            }

            fc.addChoosableFileFilter(filter);
            fc.setFileFilter(filter);

            if (title != null) {
                fc.setDialogTitle(title);
            }

            int result = save ? fc.showSaveDialog(parent) : fc.showOpenDialog(parent);

            if (result == JFileChooser.APPROVE_OPTION) {
                file = fc.getSelectedFile();
            }
        }

        return file;
    }

    //---------------//
    // getToolBorder //
    //---------------//
    /**
     * Report a standard tool border entity, which is a blank border.
     *
     * @return the standard tool border
     */
    public static Border getToolBorder ()
    {
        if (toolBorder == null) {
            toolBorder = BorderFactory.createEmptyBorder(1, 2, 1, 2);
        }

        return toolBorder;
    }

    //-------------//
    // insertTitle //
    //-------------//
    /**
     * Insert a pseudo-item, to be used as a menu title.
     *
     * @param menu the containing menu
     * @param text the title text
     */
    public static void insertTitle (JMenu menu,
                                    String text)
    {
        JMenuItem title = new JMenuItem(text);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setEnabled(false);
        menu.add(title);
        menu.addSeparator();
    }

    //-------------------//
    // setAbsoluteStroke //
    //-------------------//
    /**
     * Whatever the current scaling of a graphic context, set the stroke to the desired
     * absolute width, and return the saved stroke for later restore.
     *
     * @param g     the current graphics context
     * @param width the absolute stroke width desired
     * @return the previous stroke
     */
    public static Stroke setAbsoluteStroke (Graphics g,
                                            float width)
    {
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform AT = g2.getTransform();
        double ratio = AT.getScaleX();
        Stroke oldStroke = g2.getStroke();
        Stroke stroke = new BasicStroke(width / (float) ratio);
        g2.setStroke(stroke);

        return oldStroke;
    }

    //-----------------//
    // suppressBorders //
    //-----------------//
    /**
     * Browse the whole hierarchy of components and nullify their borders.
     *
     * @param container top of hierarchy
     */
    public static void suppressBorders (Container container)
    {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JComponent) {
                suppressBorders((JComponent) comp);
            } else if (comp instanceof Container) {
                suppressBorders((Container) comp);
            }
        }
    }

    //-----------------//
    // suppressBorders //
    //-----------------//
    /**
     * Nullify the border of this JComponent, as well as its subcomponents.
     *
     * @param comp top of hierarchy
     */
    public static void suppressBorders (JComponent comp)
    {
        if (!(comp instanceof JButton) && !(comp instanceof JToggleButton)) {
            comp.setBorder(null);
        }

        suppressBorders((Container) comp);
    }
}