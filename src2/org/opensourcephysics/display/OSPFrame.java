/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.ToolsRes;

/**
 * OSPFrame is the base class for Open Source Physics JFrames such as DrawingFrame and DataTableFrame.
 *
 * Copyright:    Copyright (c) 2002
 * @author       Wolfgang Christian
 * @version 1.1
 */
public class OSPFrame extends JFrame implements Hidable, AppFrame {
  /** Location of OSP icon. */
  static final String OSP_ICON_FILE = "/org/opensourcephysics/resources/controls/images/osp_icon.gif"; //$NON-NLS-1$
  // value is set in static block
  protected ArrayList<JButton> customButtons = new ArrayList<JButton>();                               // list of custom buttons, custom buttons are enabled when the animation is stopped
  static int topx = 10;
  static int topy = 100;

  /** Set to <I>true</I> if a simulation should automatically render this frame after every animation step. */
  protected boolean animated = false;

  /** Set to <I>true</I> if a simulation should automatically clear the data when it is initialized. */
  protected boolean autoclear = false;

  /** Set <I>true</I> if the Frame's defaultCloseOperation has been changed by Launcher. */
  private volatile boolean wishesToExit = false;

  /** The thread group that created this object. */
  public ThreadGroup constructorThreadGroup = Thread.currentThread().getThreadGroup();
  protected boolean keepHidden = false;
  protected BufferStrategy strategy;
  protected JPanel buttonPanel = new JPanel();
  protected Collection<JFrame> childFrames = new ArrayList<JFrame>();

  /**
   * Gets a file chooser that is the same for all OSPFrames.
   *
   * @deprecated  use <code>OSPRuntime.getChooser()<\code>.
   * @return the chooser
   */
  public static JFileChooser getChooser() {
    return OSPRuntime.getChooser();
  }

  /**
   * OSPFrame constructor with a title.
   * @param title
   */
  public OSPFrame(String title) {
    super(TeXParser.parseTeX(title));
    if(OSPRuntime.appletMode) {
      keepHidden = true;
    }
    buttonPanel.setVisible(false);
    setLocation(topx, topy);
    Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    topx = Math.min(topx+20, (int) d.getWidth()-100);
    topy = Math.min(topy+20, (int) d.getHeight()-100);
    setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    // Changes font size to current level
    setFontLevel(FontSizer.getLevel());
    FontSizer.addPropertyChangeListener("level", new PropertyChangeListener() { //$NON-NLS-1$
      public void propertyChange(PropertyChangeEvent e) {
        int level = ((Integer) e.getNewValue()).intValue();
        setFontLevel(level);
      }

    });
    ToolsRes.addPropertyChangeListener("locale", new PropertyChangeListener() { //$NON-NLS-1$
      public void propertyChange(PropertyChangeEvent e) {
        refreshGUI();
      }

    });
    try {
      URL url = OSPFrame.class.getResource(OSP_ICON_FILE);
      ImageIcon icon = new ImageIcon(url);
      setIconImage(icon.getImage());
      //setIconImage(ResourceLoader.getImage(OSPRuntime.OSP_ICON_FILE));
    } catch(Exception ex) {
      // image not found  
    }
    addWindowListener(new WindowAdapter() {
      /**
       * Closes and disposes child windows when this window is about to be closed.
       */
      public void windowClosed(WindowEvent e) {
        disposeChildWindows();
      }

    });
  }

  void disposeChildWindows() {
    //if(OSPRuntime.applet!=null) return;  // applets do not have a main window so return
    Iterator<JFrame> it = childFrames.iterator();
    while(it.hasNext()) {
      JFrame f = it.next();
      if(!f.isDisplayable()) {
        continue; // frame has already been disposed
      }
      if(f instanceof OSPFrame) {
        ((OSPFrame) f).setKeepHidden(true);
      } else {
        f.setVisible(false);
      }
      f.dispose();
    }
    childFrames.clear();
  }

  /**
   * OSPFrame constructor.
   */
  public OSPFrame() {
    this("Open Source Physics"); //$NON-NLS-1$
  }

  /**
   * OSPFrame constructor with a new content pane.
   * @param contentPane
   */
  public OSPFrame(Container contentPane) {
    this();
    setContentPane(contentPane);
  }

  /**
   * Sets the title for this frame to the specified string after converting TeX math symbols to characters.
   * @param title the title to be displayed in the frame's border.
   *              A <code>null</code> value
   *              is treated as an empty string, "".
   * @see      #getTitle
   */
  public void setTitle(String title) {
    super.setTitle(TeXParser.parseTeX(title));
  }

  /**
   * Adds a child frame that depends on this frame.
   * Child frames are closed when this frame is closed.
   *
   * @param frame JFrame
   */
  public void addChildFrame(JFrame frame) {
    if((frame==null)||!frame.isDisplayable()) {
      return;
    }
    childFrames.add(frame);
  }

  /**
   * Clears all frames from the child frame list.
   */
  public void clearChildFrames() {
    childFrames.clear();
  }

  /**
   * Gets a copy of the ChildFrames collection.
   * @return Collection
   */
  public Collection<JFrame> getChildFrames() {
    return new ArrayList<JFrame>(childFrames);
  }

  /**
   * Gets the ICONIFIED flag for this frame.
   *
   * @return boolean true if frame is iconified; false otherwise
   */
  public boolean isIconified() {
    return(getExtendedState()&ICONIFIED)==1;
  }

  /**
   * Invalidates image buffers if a drawing panel is buffered.
   */
  public void invalidateImage() {
    // default does nothing
  }

  /**
   * Sets the font level.
   *
   * @param level the level
   */
  protected void setFontLevel(int level) {
    try {
    	FontSizer.setFonts(getJMenuBar(), level);
        //FontSizer.setFonts(getContentPane(), level);   //WC: Bug in JS
    }catch(Exception ex) {
     	System.err.println("Err: OSPFrame line 220.");
     }
  }

  /**
   * Reads the animated property.
   *
   * @return boolean
   */
  public boolean isAnimated() {
    return animated;
  }

  /**
   * Sets the animated property.
   *
   * @param animated
   */
  public void setAnimated(boolean animated) {
    this.animated = animated;
  }

  /**
   * Reads the animated property.
   *
   * @return boolean
   */
  public boolean isAutoclear() {
    return autoclear;
  }

  /**
   * Sets the autoclear property.
   *
   * @param autoclear
   */
  public void setAutoclear(boolean autoclear) {
    this.autoclear = autoclear;
  }

  /**
   * Adds a Display menu to the menu bar.
   *
   * The default method does nothing.
   * Override this method to create a menu item that is appropriate for the frame.
   */
  protected JMenu loadDisplayMenu() {
    return null;
  }

  /**
   * Adds a Tools menu to the menu bar.
   *
   * The default method does nothing.
   * Override this method to create a menu item that is appropriate for the frame.
   */
  protected JMenu loadToolsMenu() {
    return null;
  }

  /**
   * Clears data from drawing objects within this frame.
   *
   * The default method does nothing.
   * Override this method to select the object(s) and the data to be cleared.
   */
  public void clearData() {}

  /**
   * Clears data and repaints the drawing panel within this frame.
   *
   * The default method does nothing.
   * Override this method to clear and repaint objects that have data.
   */
  public void clearDataAndRepaint() {}

  public void setSize(int width, int height) {
    super.setSize(width, height);
    validate();
  }

  /**
   * Shows the frame on the screen if the keep hidden flag is false.
   *
   * @deprecated
   */
  public void show() {
    if(!keepHidden) {
      super.show();
    }
  }

  /**
   * Disposes all resources.
   */
  public void dispose() {
    keepHidden = true;
    this.clearData();
    disposeChildWindows();
    super.dispose();
  }

  /**
   * Shows or hides this component depending on the value of parameter
   * <code>b</code> and the <code>keepHidden</code> flag.
   *
   * OSP Applets often keep windows hidden.
   *
   * @param b
   */
  public void setVisible(boolean b) {
    if(!keepHidden) {
      boolean shouldRender = (!isVisible())&&animated; // render animated frames when made visible
      super.setVisible(b);
      if(shouldRender) {
        render();
      }
    }
  }

  /**
   * Sets the keepHidden flag.
   *
   * @param _keepHidden
   */
  public void setKeepHidden(boolean _keepHidden) {
    keepHidden = _keepHidden;
    if(keepHidden) {
      super.setVisible(false);
    }
  }

  /**
   * Reads the keepHidden flag.
   *
   */
  public boolean isKeepHidden() {
    return keepHidden;
  }

  /**
   * Gets the ThreadGroup that constructed this frame.
   *
   * @return the ThreadGroup
   */
  public ThreadGroup getConstructorThreadGroup() {
    return constructorThreadGroup;
  }

  /**
   * Creates a BufferStrategy based on the capabilites of the hardware.
   */
  public void createBufferStrategy() {
    createBufferStrategy(2);
    strategy = this.getBufferStrategy();
  }

  /**
   * Shows (repaints) the frame useing the current BufferStrategy.
   */
  public void bufferStrategyShow() {
    if((strategy)==null) {
      createBufferStrategy();
    }
    if(isIconified()||!isShowing()) {
      return;
    }
    Graphics g = strategy.getDrawGraphics();
    paintComponents(g);
    g.dispose();
    strategy.show();
  }

  /**
   * Renders the frame.  Subclass this method to render the contents of this frame in the calling thread.
   */
  public void render() {}

  /**
   * Gets a menu with the given name from the menu bar.  Returns null if menu item does not exist.
   *
   * @param menuName String
   * @return JMenu
   */
  public JMenu getMenu(String menuName) {
    JMenuBar menuBar = getJMenuBar();
    if(menuBar==null) {
      return null;
    }
    menuName = menuName.trim();
    JMenu menu = null;
    for(int i = 0; i<menuBar.getMenuCount(); i++) {
      JMenu next = menuBar.getMenu(i);
      if(next.getText().trim().equals(menuName)) {
        menu = next;
        break;
      }
    }
    return menu;
  }

  /**
   * Removes a menu with the given name from the menu bar and returns the removed item.
   * Returns null if menu item does not exist.
   *
   * @param menuName String
   * @return JMenu
   */
  public JMenu removeMenu(String menuName) {
    JMenuBar menuBar = getJMenuBar();
    if(menuBar==null) {
      return null;
    }
    menuName = menuName.trim();
    JMenu menu = null;
    for(int i = 0; i<menuBar.getMenuCount(); i++) {
      JMenu next = menuBar.getMenu(i);
      if(next.getText().trim().equals(menuName)) {
        menu = next;
        menuBar.remove(i);
        break;
      }
    }
    return menu;
  }

  /**
   * Removes a menu item with the given name from the menu bar and returns the removed item.
   * Returns null if menu item does not exist.
   *
   * @param menuName String
   * @return JMenu
   */
  public JMenuItem removeMenuItem(String menuName, String itemName) {
    JMenu menu = getMenu(menuName);
    if(menu==null) {
      return null;
    }
    itemName = itemName.trim();
    JMenuItem item = null;
    for(int i = 0; i<menu.getItemCount(); i++) {
      JMenuItem next = menu.getItem(i);
      if(next.getText().trim().equals(itemName)) {
        item = next;
        menu.remove(i);
        break;
      }
    }
    return item;
  }

  /*
   * Creates a menu in the menu bar from the given XML document.
   * @param xmlMenu name of the xml file with menu data
   */
  public void parseXMLMenu(String xmlMenu) {
    parseXMLMenu(xmlMenu, null);
  }

  public void parseXMLMenu(String xmlMenu, Class<?> type) {
    System.out.println("The parseXMLMenu method has been disabled to reduce the size OSP jar files."); //$NON-NLS-1$
  }

  /**
   * Refreshes the user interface in response to display changes such as Language.
   */
  protected void refreshGUI() {
    Iterator<JButton> it = customButtons.iterator();
    while(it.hasNext()) {
      TranslatableButton b = (TranslatableButton) it.next();
      b.refreshGUI();
    }
    buttonPanel.validate();
  }

  /**
   *  Adds a custom button to the control's frame.
   *
   * @param  methodName   the name of the method; the method has no parameters
   * @param  text         the button's text label
   * @param  toolTipText  the button's tool tip text
   * @param  target       the target for the method
   * @return              the custom button
   */
  public JButton addButton(String methodName, String text, String toolTipText, final Object target) {
    TranslatableButton b = new TranslatableButton(text, toolTipText, target);
    // changed to add translation tools for strings by D Brown 2007-10-17
    if(OSPRuntime.getTranslator()!=null) {
      text = OSPRuntime.getTranslator().getProperty(target.getClass(), "custom_button."+text, text);                      //$NON-NLS-1$
      toolTipText = OSPRuntime.getTranslator().getProperty(target.getClass(), "custom_button."+toolTipText, toolTipText); //$NON-NLS-1$
    }
    b.setText(text);
    b.setToolTipText(toolTipText);
    Class<?>[] parameters = {};
    try {   
      final java.lang.reflect.Method m = target.getClass().getMethod(methodName, parameters); 
      b.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Object[] args = {};
          try {
            m.invoke(target, args);
          } catch(IllegalAccessException iae) {
            System.err.println(iae);
          } catch(java.lang.reflect.InvocationTargetException ite) {
            System.err.println(ite);
          }
        }

      });
      buttonPanel.setVisible(true);
      buttonPanel.add(b);
      validate();
      pack();
    } catch(NoSuchMethodException nsme) {
      System.err.println("Error adding custom button "+text+". The method "+methodName+"() does not exist."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    customButtons.add(b);
    return b;
  }

  class TranslatableButton extends JButton {
    String text, tip;
    Object target;

    /**
     * Constructor TranslatableButton
     * @param text
     * @param tip
     * @param target
     */
    public TranslatableButton(String text, String tip, Object target) {
      this.text = text;
      this.tip = tip;
      this.target = target;
    }

    void refreshGUI() {
      if(OSPRuntime.getTranslator()!=null) {
        setText(OSPRuntime.getTranslator().getProperty(target.getClass(), "custom_button."+text, text));      //$NON-NLS-1$
        setToolTipText(OSPRuntime.getTranslator().getProperty(target.getClass(), "custom_button."+tip, tip)); //$NON-NLS-1$
      }
    }

  }

  /**
   * Overrides JFrame method. This converts EXIT_ON_CLOSE to DISPOSE_ON_CLOSE
   * and sets the wishesToExit flag.
   *
   * @param  operation the operation
   */
  public void setDefaultCloseOperation(int operation) {
    if((operation==JFrame.EXIT_ON_CLOSE)&&OSPRuntime.launchingInSingleVM) {
      operation = WindowConstants.DISPOSE_ON_CLOSE;
      wishesToExit = true;
    }
    try {
      super.setDefaultCloseOperation(operation);
    } catch(Exception ex) {
      // cannot set the default close operation for java applets frames in Java 1.6
    }
  }

  /**
   * Returns true if this frame wishes to exit.
   * Launcher uses this to identify control frames.
   *
   * @return true if this frame wishes to exit
   */
  public boolean wishesToExit() {
    return wishesToExit;
  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2017  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
