/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.controls.XMLProperty;
import org.opensourcephysics.controls.XMLTreeChooser;
import org.opensourcephysics.controls.XMLTreePanel;
import org.opensourcephysics.display.axes.DrawableAxes;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.Job;
import org.opensourcephysics.tools.LocalJob;
import org.opensourcephysics.tools.SnapshotTool;
import org.opensourcephysics.tools.Tool;
import org.opensourcephysics.tools.VideoTool;

import javajs.async.AsyncDialog;

/**
 *  Drawing Frame: a frame that contains a drawing panel.
 *
 * @author     Wolfgang Christian
 * @created    July 16, 2004
 * @version    1.1
 */
public class DrawingFrame extends OSPFrame implements ClipboardOwner {
  // protected static String resourcesPath = "/org/opensourcephysics/resources/display/";
  // protected static String defaultToolsFileName = "drawing_tools.xml";
  protected JMenu fileMenu, editMenu;
  protected JMenuItem copyItem, pasteItem, replaceItem;
  protected DrawingPanel drawingPanel;
  protected final static int MENU_SHORTCUT_KEY_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
  protected Window customInspector; // optional custom inspector for this frame
  protected Tool reply;

  /**
   * DrawingFrame constructor that creates a default DrawingPanel.
   *
   * The default DrawingPanel is an InteractivePanel.
   */
  public DrawingFrame() {
    this(DisplayRes.getString("DrawingFrame.DefaultTitle"), new InteractivePanel()); //$NON-NLS-1$
  }

  /**
   *  DrawingFrame constructor specifying the DrawingPanel that will be placed
   *  in the center of the content pane.
   *
   * @param  drawingPanel
   */
  public DrawingFrame(DrawingPanel drawingPanel) {
    this(DisplayRes.getString("DrawingFrame.DefaultTitle"), drawingPanel); //$NON-NLS-1$
  }

  /**
   *  DrawingFrame constructor specifying the title and the DrawingPanel that
   *  will be placed in the center of the content pane.
   *
   * @param  title
   * @param  _drawingPanel
   */
  public DrawingFrame(String title, DrawingPanel _drawingPanel) {
    super(title);
    // forces dependence on the DatasetTool class;
    // add or remove the next line if you do not want the DatasetTool in your jar
//     org.opensourcephysics.tools.DataTool.loadClass = true;
    drawingPanel = _drawingPanel;
    if(drawingPanel!=null) {
      getContentPane().add(drawingPanel, BorderLayout.CENTER);
    }
    getContentPane().add(buttonPanel, BorderLayout.SOUTH); // buttons are added using addButton method.
    pack();
    if(!OSPRuntime.appletMode) {
      createMenuBar();
    }
    // responds to data from the Data Tool
    reply = new Tool() {
      public void send(Job job, Tool replyTo) throws RemoteException {
        XMLControlElement control = new XMLControlElement();
        try {
          control.readXML(job.getXML());
        } catch(RemoteException ex) {}
        ArrayList<?> datasets = drawingPanel.getObjectOfClass(Dataset.class);
        Iterator<?> it = control.getObjects(Dataset.class).iterator();
        while(it.hasNext()) {
          Dataset newData = (Dataset) it.next();
          int id = newData.getID();
          for(int i = 0, n = datasets.size(); i<n; i++) {
            if(((Dataset) datasets.get(i)).getID()==id) {
              XMLControl xml = new XMLControlElement(newData);      // convert the source to xml
              Dataset.getLoader().loadObject(xml, datasets.get(i)); // copy the data to the destination
              break;
            }
          }
        }
        drawingPanel.repaint();
      }

    };
  }

  /**
   * Renders the drawing panel if the frame is showing and not iconified.
   */
  public void render() {
    if(isIconified()||!isShowing()) {
      return;
    }
    if(drawingPanel!=null) {
      drawingPanel.render();
    } else {
      repaint();
    }
  }

  /**
   * Invalidates image buffers if a drawing panel buffered.
   */
  public void invalidateImage() {
    if(drawingPanel!=null) {
      drawingPanel.invalidateImage();
    }
  }

  /**
   *  Gets the drawing panel.
   *
   * @return    the drawingPanel
   */
  public DrawingPanel getDrawingPanel() {
    return drawingPanel;
  }

  /**
   *  Sets the label for the X (horizontal) axis.
   *
   * @param  label  the label
   */
  public void setXLabel(String label) {
    if(drawingPanel instanceof PlottingPanel) {
      ((PlottingPanel) drawingPanel).setXLabel(label);
    }
  }

  /**
   *  Sets the label for the Y (vertical) axis.
   *
   * @param  label  the label
   */
  public void setYLabel(String label) {
    if(drawingPanel instanceof PlottingPanel) {
      ((PlottingPanel) drawingPanel).setYLabel(label);
    }
  }

  /**
   *  Converts to polar coordinates.
   *
   * @param plotTitle String
   * @param deltaR double
   */
  public void setPolar(String plotTitle, double deltaR) {
    if(drawingPanel instanceof PlottingPanel) {
      ((PlottingPanel) drawingPanel).setPolar(plotTitle, deltaR);
    }
  }

  /**
   *  Converts to cartesian coordinates.
   *
   *
   * @param xLabel String
   * @param yLabel String
   * @param plotTitle String
   */
  public void setCartesian(String xLabel, String yLabel, String plotTitle) {
    if(drawingPanel instanceof PlottingPanel) {
      ((PlottingPanel) drawingPanel).setCartesian(xLabel, yLabel, plotTitle);
    }
  }

  /**
   * Limits the xmin and xmax values during autoscaling so that the mininimum value
   * will be no greater than the floor and the maximum value will be no
   * smaller than the ceil.
   *
   * Setting a floor or ceil value to <code>Double.NaN<\code> will disable that limit.
   *
   * @param floor the xfloor value
   * @param ceil the xceil value
   */
  public void limitAutoscaleX(double floor, double ceil) {
    if(drawingPanel!=null) drawingPanel.limitAutoscaleX(floor, ceil);
  }

  /**
   * Limits ymin and ymax values during autoscaling so that the mininimum value
   * will be no greater than the floor and the maximum value will be no
   * smaller than the ceil.
   *
   * Setting a floor or ceil value to <code>Double.NaN<\code> will disable that limit.
   *
   * @param floor the yfloor value
   * @param ceil the yceil value
   */
  public void limitAutoscaleY(double floor, double ceil) {
	  if(drawingPanel!=null) drawingPanel.limitAutoscaleY(floor, ceil);
  }

  /**
   * Autoscale the drawing panel's x axis using min and max values.
   * from measurable objects.
   * @param autoscale
   */
  public void setAutoscaleX(boolean autoscale) {
    if(drawingPanel!=null) {
      drawingPanel.setAutoscaleX(autoscale);
    }
  }

  /**
   * Determines if the panel's x axis autoscale property is true.
   * @return <code>true<\code> if autoscaled.
   */
  public boolean isAutoscaleX() {
    if(drawingPanel!=null) {
      return drawingPanel.isAutoscaleX();
    }
    return false;
  }

  /**
   * Autoscale the y axis using min and max values.
   * from measurable objects.
   * @param autoscale
   */
  public void setAutoscaleY(boolean autoscale) {
    if(drawingPanel!=null) {
      drawingPanel.setAutoscaleY(autoscale);
    }
  }

  /**
   * Determines if the y axis autoscale property is true.
   * @return <code>true<\code> if autoscaled.
   */
  public boolean isAutoscaleY() {
    if(drawingPanel!=null) {
      return drawingPanel.isAutoscaleY();
    }
    return false;
  }

  /**
   * Sets the aspect ratio for horizontal to vertical to unity when <code>true<\code>.
   * @param isSquare boolean
   */
  public void setSquareAspect(boolean isSquare) {
    if(drawingPanel!=null) {
      drawingPanel.setSquareAspect(isSquare);
    }
  }

  /**
   * Sets Cartesian axes to log scale.
   *
   * @param  logX
   * @param  logY
   */
  public void setLogScale(boolean logX, boolean logY) {
    if((drawingPanel!=null)&&(drawingPanel instanceof PlottingPanel)) {
      ((PlottingPanel) drawingPanel).setLogScale(logX, logY);
    }
  }

  /**
   * Sets the scale using pixels per unit.
   *
   * @param enable boolean enable fixed pixels per unit
   * @param xPixPerUnit double
   * @param yPixPerUnit double
   */
  public void setPixelsPerUnit(boolean enable, double xPixPerUnit, double yPixPerUnit) {
	  if(drawingPanel!=null)drawingPanel.setPixelsPerUnit(enable, xPixPerUnit, yPixPerUnit);
  }

  /**
   * Sets the drawing panel's preferred scale.
   * @param xmin
   * @param xmax
   * @param ymin
   * @param ymax
   */
  public void setPreferredMinMax(double xmin, double xmax, double ymin, double ymax) {
    if(drawingPanel!=null) {
      drawingPanel.setPreferredMinMax(xmin, xmax, ymin, ymax);
    }
  }

  /**
   * Sets the drawing panel's preferred scale in the vertical direction.
   * @param ymin
   * @param ymax
   */
  public void setPreferredMinMaxY(double ymin, double ymax) {
    if(drawingPanel!=null) {
      drawingPanel.setPreferredMinMaxY(ymin, ymax);
    }
  }

  /**
   * Sets the drawing panel's preferred scale in the horizontal direction.
   * @param xmin the minimum value
   * @param xmax the maximum value
   */
  public void setPreferredMinMaxX(double xmin, double xmax) {
    if(drawingPanel!=null) {
      drawingPanel.setPreferredMinMaxX(xmin, xmax);
    }
  }

  /**
   * Clears data and repaints the drawing panel within this frame.
   */
  public void clearDataAndRepaint() {
    clearData();
    if(drawingPanel!=null)drawingPanel.repaint();
  }

  /**
   * Clears Drawable objects added by the user from this frame.
   */
  public void clearDrawables() {
	  if(drawingPanel!=null)drawingPanel.clear(); // removes all drawables
  }

  /**
   * Adds a drawable object to the frame's drawing panel.
   * @param drawable
   */
  public synchronized void addDrawable(Drawable drawable) {
    if(drawingPanel!=null) {
      drawingPanel.addDrawable(drawable);
    }
  }

  /**
   * Replaces a Drawable object with another Drawable.
   *
   * @param oldDrawable Drawable
   * @param newDrawable Drawable
   */
  public synchronized void replaceDrawable(Drawable oldDrawable, Drawable newDrawable) {
    if(drawingPanel!=null) {
      drawingPanel.replaceDrawable(oldDrawable, newDrawable);
    }
  }

  /**
   * Removes a drawable object to the frame's drawing panel.
   * @param drawable
   */
  public synchronized void removeDrawable(Drawable drawable) {
    if(drawingPanel!=null) {
      drawingPanel.removeDrawable(drawable);
    }
  }

  /**
   * Shows a message in a yellow text box in the lower right hand corner.
   *
   * @param msg
   */
  public void setMessage(String msg) {
    if(drawingPanel!=null)drawingPanel.setMessage(msg);
  }

  /**
   * Shows a message in a yellow text box at the given location.
   *
   * location 0=bottom left
   * location 1=bottom right
   * location 2=top right
   * location 3=top left
   *
   * @param msg
   * @param location
   */
  public void setMessage(String msg, int location) {
    if(drawingPanel!=null) drawingPanel.setMessage(msg, location);
  }

  /**
   * Gets objects of a specific class from the drawing panel.
   *
   * Assignable subclasses are NOT returned.  Interfaces CANNOT be specified.
   * The same objects will be in the drawable list and the cloned list.
   *
   * @param c the class of the object
   *
   * @return the list
   */
  public synchronized <T extends Drawable> ArrayList<T> getObjectOfClass(Class<T> c) {
    if(drawingPanel!=null) {
      return drawingPanel.getObjectOfClass(c);
    }
    return null;
  }

  /**
   * Gets Drawable previously objects added by the user.
   *
   * @return the list
   */
  public synchronized ArrayList<Drawable> getDrawables() {
    if(drawingPanel!=null) {
      return drawingPanel.getDrawables();
    }
    return new ArrayList<Drawable>(); // return an empty list
  }

  public DrawableAxes getAxes() {
    if(drawingPanel instanceof PlottingPanel) {
      return((PlottingPanel) drawingPanel).getAxes();
    }
    return null;
  }

  /**
   * Gets Drawable objects added by the user of an assignable type. The list contains
   * objects that are assignable from the class or interface.
   *
   * @param c the type of Drawable object
   *
   * @return the cloned list
   *
   * @see #getObjectOfClass(Class c)
   */
  public synchronized <T extends Drawable> ArrayList<T> getDrawables(Class<T> c) {
    if(drawingPanel!=null) {
      return drawingPanel.getDrawables(c);
    }
    return new ArrayList<T>(); // return an empty list
  }

  /**
   * Removes all objects of the given class from the drawable list.
   *
   * Assignable subclasses are NOT removed.  Interfaces CANNOT be specified.
   *
   * @param c the class
   */
  public synchronized <T extends Drawable> void removeObjectsOfClass(Class<T> c) {
	  if(drawingPanel!=null) drawingPanel.removeObjectsOfClass(c);
  }

  /**
   * Sets the interactive mouse handler if the drawing panel is an interactive panel.
   *
   * Throws an invalid cast exception if the panel is not of the correct type.
   *
   * @param handler the mouse handler
   */
  public void setInteractiveMouseHandler(InteractiveMouseHandler handler) {
    ((InteractivePanel) drawingPanel).setInteractiveMouseHandler(handler);
  }

  /**
   *  Adds the drawing panel to the the frame. The panel is added to the center
   *  of the frame's content pane.
   *
   * @param  _drawingPanel
   */
  public void setDrawingPanel(DrawingPanel _drawingPanel) {
    if(drawingPanel!=null) { // remove the old drawing panel.
      getContentPane().remove(drawingPanel);
    }
    drawingPanel = _drawingPanel;
    if(drawingPanel!=null) {
      getContentPane().add(drawingPanel, BorderLayout.CENTER);
    }
  }

  /**
   * Sets the interior background color for the current drawing panel.
   * The interior of a PlottingaPanel is the area inside the axes where is displayed.
   * The interior of a DrawingPanel is the entire panel.
   */
  public void setInteriorBackground(Color color) {
    if(drawingPanel instanceof PlottingPanel) {
      ((PlottingPanel) drawingPanel).getAxes().setInteriorBackground(color);
    } else {
    	 if(drawingPanel!=null) drawingPanel.setBackground(color);
    }
  }

  /**
   *  This is a hack to fix a bug when the reload button is pressed in browsers
   *  running JDK 1.4.
   *
   * @param  g
   */
  public void paint(Graphics g) {
    if(!OSPRuntime.appletMode) {
      super.paint(g);
      return;
    }
    try {
      super.paint(g);
    } catch(Exception ex) {
      System.err.println("OSPFrame paint error: "+ex.toString()); //$NON-NLS-1$
      System.err.println("Title: "+this.getTitle());              //$NON-NLS-1$
    }
  }

  /**
   * Enables the paste edit menu item.
   * @param enable boolean
   */
  public void setEnabledPaste(boolean enable) {
    pasteItem.setEnabled(enable);
  }

  /**
   * Pastes drawables found in the specified xml control.
   *
   * @param control the xml control
   */
  protected void pasteAction(XMLControlElement control) {
	System.err.println("DrawingFrame Line 568");
    // get Drawables using an xml tree chooser
    XMLTreeChooser chooser = new XMLTreeChooser(DisplayRes.getString("DrawingFrame.SelectDrawables_chooser_title"), DisplayRes.getString("DrawingFrame.SelectDrawables_chooser_message"), this); //$NON-NLS-1$ //$NON-NLS-2$
    java.util.List<XMLProperty> props = chooser.choose(control, Drawable.class);
    if(!props.isEmpty()) {
      Iterator<XMLProperty> it = props.iterator();
      while(it.hasNext()) {
        XMLControl prop = (XMLControl) it.next();
        Drawable drawable = (Drawable) prop.loadObject(null);
        addDrawable(drawable);
      }
    }
    drawingPanel.repaint();
  }

  /**
   * Enables the replace edit menu item.
   * @param enable boolean
   */
  public void setEnabledReplace(boolean enable) {
    replaceItem.setEnabled(enable);
  }

  /**
   * Replaces the drawables with the drawables found in the specified XML control.
   * @param control XMLControlElement
   */
  public void replaceAction(XMLControlElement control) {
    clearDrawables();
    pasteAction(control);
  }

  /**
   * Copies objects found in the specified xml control.
   *
   * @param control the xml control
   */
  protected void copyAction(XMLControlElement control) {
    StringSelection data = new StringSelection(control.toXML());
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(data, this);
  }

  /**
   * Implementation of ClipboardOwner interface.
   *
   * Override this method to receive notification that data copied to the clipboard has changed.
   *
   * @param clipboard Clipboard
   * @param contents Transferable
   */
  public void lostOwnership(Clipboard clipboard, Transferable contents) {}

  /**
   * Enables the copy edit menu item.
   * @param enable boolean
   */
  public void setEnabledCopy(boolean enable) {
    copyItem.setEnabled(enable);
  }

  protected void refreshGUI() {
    createMenuBar();
    addMenuItems();
    pack();
  }

  /**
   * Adds Views menu items on the menu bar.
   * Override this method to add custom menu items.
   */
  protected void addMenuItems() {}

  /**
   * Creates a standard DrawingFrame menu bar and adds it to the frame.
   */
  private void createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    fileMenu = new JMenu(DisplayRes.getString("DrawingFrame.File_menu_item")); //$NON-NLS-1$
    JMenu printMenu = new JMenu(DisplayRes.getString("DrawingFrame.Print_menu_title"));        //$NON-NLS-1$
    JMenuItem printItem = new JMenuItem(DisplayRes.getString("DrawingFrame.Print_menu_item")); //$NON-NLS-1$
    printMenu.add(printItem);
    printItem.setAccelerator(KeyStroke.getKeyStroke('P', MENU_SHORTCUT_KEY_MASK));
    printItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        PrintUtils.printComponent(drawingPanel);
      }

    });
    JMenuItem printFrameItem = new JMenuItem(DisplayRes.getString("DrawingFrame.PrintFrame_menu_item")); //$NON-NLS-1$
    printMenu.add(printFrameItem);
    printFrameItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        PrintUtils.printComponent(DrawingFrame.this);
      }

    });
    JMenuItem saveXMLItem = new JMenuItem(DisplayRes.getString("DrawingFrame.SaveXML_menu_item")); //$NON-NLS-1$
    saveXMLItem.setAccelerator(KeyStroke.getKeyStroke('S', MENU_SHORTCUT_KEY_MASK));
    saveXMLItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveXML();
      }

    });
    //ExportTool menu item
    /*
    JMenuItem exportItem = new JMenuItem(DisplayRes.getString("DrawingFrame.Export_menu_item")); //$NON-NLS-1$
    exportItem.setAccelerator(KeyStroke.getKeyStroke('E', MENU_SHORTCUT_KEY_MASK));
    exportItem.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
          try {
             ExportTool.getTool().send(new LocalJob(drawingPanel), null);
          } catch(RemoteException ex) {}
       }
    });*/
    // create export tool menu item if the tool exists in classpath
    JMenuItem exportItem = new JMenuItem(DisplayRes.getString("DrawingFrame.Export_menu_item")); //$NON-NLS-1$
    exportItem.setAccelerator(KeyStroke.getKeyStroke('E', MENU_SHORTCUT_KEY_MASK));
    Class<?> exportTool = null;
    if(OSPRuntime.loadExportTool) {
      try {
        exportTool = Class.forName("org.opensourcephysics.tools.ExportTool");        //$NON-NLS-1$
      } catch(Exception ex) {
        OSPRuntime.loadExportTool = false;
        OSPLog.finest("Cannot instantiate data export tool class:\n"+ex.toString()); //$NON-NLS-1$
        exportItem.setEnabled(false);
      }
    }
    final Class<?> tool = exportTool;
    exportItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          Method m = tool.getMethod("getTool", (Class[]) null); //$NON-NLS-1$
          Tool tool = (Tool) m.invoke(null, (Object[]) null);
          tool.send(new LocalJob(drawingPanel), reply);
        } catch(Exception ex) {}
      }

    });
    //Save menu item
    JMenu saveImage = new JMenu(DisplayRes.getString("DrawingFrame.SaveImage_menu_title"));      //$NON-NLS-1$
    JMenuItem epsMenuItem = new JMenuItem(DisplayRes.getString("DrawingFrame.EPS_menu_item"));   //$NON-NLS-1$
    JMenuItem jpegMenuItem = new JMenuItem(DisplayRes.getString("DrawingFrame.JPEG_menu_item")); //$NON-NLS-1$
    JMenuItem pngMenuItem = new JMenuItem(DisplayRes.getString("DrawingFrame.PNG_menu_item"));   //$NON-NLS-1$
    saveImage.add(epsMenuItem);
    saveImage.add(jpegMenuItem);
    saveImage.add(pngMenuItem);
    epsMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        GUIUtils.saveImage(drawingPanel, "eps", DrawingFrame.this); //$NON-NLS-1$
      }

    });
    jpegMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        GUIUtils.saveImage(drawingPanel, "jpeg", DrawingFrame.this); //$NON-NLS-1$
      }

    });
    pngMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        GUIUtils.saveImage(drawingPanel, "png", DrawingFrame.this); //$NON-NLS-1$
      }

    });
    JMenuItem inspectItem = new JMenuItem(DisplayRes.getString("DrawingFrame.InspectMenuItem")); //$NON-NLS-1$
    inspectItem.setAccelerator(KeyStroke.getKeyStroke('I', MENU_SHORTCUT_KEY_MASK));
    inspectItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        inspectXML(); // cannot use a static method here because of run-time binding
      }

    });
    if(OSPRuntime.applet==null) {
      if(!org.opensourcephysics.js.JSUtil.isJS)fileMenu.add(printMenu);
      fileMenu.add(saveXMLItem);
      if(!org.opensourcephysics.js.JSUtil.isJS)fileMenu.add(exportItem);
      if(!org.opensourcephysics.js.JSUtil.isJS)fileMenu.add(saveImage);
    }
    fileMenu.add(inspectItem);
    menuBar.add(fileMenu);
    // edit menu
    editMenu = new JMenu(DisplayRes.getString("DrawingFrame.Edit_menu_title")); //$NON-NLS-1$
    menuBar.add(editMenu);
    copyItem = new JMenuItem(DisplayRes.getString("DrawingFrame.Copy_menu_item")); //$NON-NLS-1$
    copyItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        XMLControlElement control = new XMLControlElement(DrawingFrame.this);
        control.saveObject(null);
        copyAction(control);
      }

    });
    editMenu.add(copyItem);
    pasteItem = new JMenuItem(DisplayRes.getString("DrawingFrame.Paste_menu_item")); //$NON-NLS-1$
    pasteItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
          Transferable data = clipboard.getContents(null);
          XMLControlElement control = new XMLControlElement();
          control.readXML((String) data.getTransferData(DataFlavor.stringFlavor));
          pasteAction(control);
        } catch(UnsupportedFlavorException ex) {}
        catch(IOException ex) {}
        catch(HeadlessException ex) {}
      }

    });
    pasteItem.setEnabled(false); // not supported yet
    editMenu.add(pasteItem);
    replaceItem = new JMenuItem(DisplayRes.getString("DrawingFrame.Replace_menu_item")); //$NON-NLS-1$
    replaceItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
          Transferable data = clipboard.getContents(null);
          XMLControlElement control = new XMLControlElement();
          control.readXML((String) data.getTransferData(DataFlavor.stringFlavor));
          replaceAction(control);
        } catch(UnsupportedFlavorException ex) {}
        catch(IOException ex) {}
        catch(HeadlessException ex) {}
      }

    });
    replaceItem.setEnabled(false); // not supported yet
    editMenu.add(replaceItem);
    setJMenuBar(menuBar);
    // additonal menus
    loadDisplayMenu();
    loadToolsMenu();
    //help menu
    JMenu helpMenu = new JMenu(DisplayRes.getString("DrawingFrame.Help_menu_item")); //$NON-NLS-1$
    menuBar.add(helpMenu);
    JMenuItem aboutItem = new JMenuItem(DisplayRes.getString("DrawingFrame.AboutOSP_menu_item")); //$NON-NLS-1$
    aboutItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        OSPRuntime.showAboutDialog(DrawingFrame.this);
      }

    });
    helpMenu.add(aboutItem);
  }

  /**
   * Adds a Display menu to the menu bar.
   */
  protected JMenu loadDisplayMenu() {
    JMenuBar menuBar = getJMenuBar();
    if(menuBar==null) {
      return null;
    }
    JMenu displayMenu = new JMenu(DisplayRes.getString("DrawingFrame.Display_menu_title")); //$NON-NLS-1$
    menuBar.add(displayMenu);
    JMenu fontMenu = new JMenu(DisplayRes.getString("DrawingFrame.Font_menu_title")); //$NON-NLS-1$
    displayMenu.add(fontMenu);
    JMenuItem sizeUpItem = new JMenuItem(DisplayRes.getString("DrawingFrame.IncreaseFontSize_menu_item")); //$NON-NLS-1$
    sizeUpItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        FontSizer.levelUp();
      }

    });
    fontMenu.add(sizeUpItem);
    final JMenuItem sizeDownItem = new JMenuItem(DisplayRes.getString("DrawingFrame.DecreaseFontSize_menu_item")); //$NON-NLS-1$
    sizeDownItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        FontSizer.levelDown();
      }

    });
    fontMenu.add(sizeDownItem);
    fontMenu.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        sizeDownItem.setEnabled(FontSizer.getLevel()>0);
      }

    });
    JMenu aliasMenu = new JMenu(DisplayRes.getString("DrawingFrame.AntiAlias_menu_title")); //$NON-NLS-1$
    displayMenu.add(aliasMenu);
    final JCheckBoxMenuItem textAliasItem = new JCheckBoxMenuItem(DisplayRes.getString("DrawingFrame.Text_checkbox_label"), false); //$NON-NLS-1$
    textAliasItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        drawingPanel.antialiasTextOn = textAliasItem.isSelected();
        drawingPanel.repaint();
      }

    });
    aliasMenu.add(textAliasItem);
    final JCheckBoxMenuItem shapeAliasItem = new JCheckBoxMenuItem(DisplayRes.getString("DrawingFrame.Drawing_textbox_label"), false); //$NON-NLS-1$
    shapeAliasItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        drawingPanel.antialiasShapeOn = shapeAliasItem.isSelected();
        drawingPanel.repaint();
      }

    });
    aliasMenu.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        textAliasItem.setSelected(drawingPanel.antialiasTextOn);
        shapeAliasItem.setSelected(drawingPanel.antialiasShapeOn);
      }

    });
    aliasMenu.add(shapeAliasItem);
    menuBar.add(displayMenu);
    return displayMenu;
  }

  /**
   * Adds a Tools menu to the menu bar.
   */
  protected JMenu loadToolsMenu() {
	if(org.opensourcephysics.js.JSUtil.isJS) {  // external tools not supported in JavaScript.
		  return null;
	}
    JMenuBar menuBar = getJMenuBar();
    if(menuBar==null) {
      return null;
    }
    // create Tools menu item
    JMenu toolsMenu = new JMenu(DisplayRes.getString("DrawingFrame.Tools_menu_title")); //$NON-NLS-1$
    menuBar.add(toolsMenu);
    // create Data Tool menu item if the tool exists in the classpath
    JMenuItem datasetItem = new JMenuItem(DisplayRes.getString("DrawingFrame.DatasetTool_menu_item")); //$NON-NLS-1$
    toolsMenu.add(datasetItem);
    Class<?> datasetToolClass = null;
    if(OSPRuntime.loadDataTool) {
      try {
        datasetToolClass = Class.forName("org.opensourcephysics.tools.DataTool");      //$NON-NLS-1$
      } catch(Exception ex) {
        OSPLog.finest("Cannot instantiate data analysis tool class:\n"+ex.toString()); //$NON-NLS-1$
        OSPRuntime.loadDataTool = false;
        datasetItem.setEnabled(false);
      }
    }
    final Class<?> finalDatasetToolClass = datasetToolClass; // class must be final for action listener
    datasetItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          Method m = finalDatasetToolClass.getMethod("getTool", (Class[]) null); //$NON-NLS-1$
          Tool tool = (Tool) m.invoke(null, (Object[]) null);
          tool.send(new LocalJob(drawingPanel), reply);
          if(tool instanceof OSPFrame) {
            ((OSPFrame) tool).setKeepHidden(false);
          }
          ((JFrame) tool).setVisible(true);
        } catch(Exception ex) {}
      }

    });
    // create Fourier Tool menu item if the tool exists in the classpath
    JMenuItem fourierToolItem = new JMenuItem(DisplayRes.getString("DrawingFrame.FourierTool_menu_item")); //$NON-NLS-1$
    toolsMenu.add(fourierToolItem);
    Class<?> fourierToolClass = null;
    if(OSPRuntime.loadFourierTool) {
      try {
        //fourierToolClass = Class.forName("org.opensourcephysics.tools.FourierTool");      //$NON-NLS-1$
      } catch(Exception ex) {
        OSPLog.finest("Cannot instantiate Fourier analysis tool class:\n"+ex.toString()); //$NON-NLS-1$
        OSPRuntime.loadFourierTool = false;
        fourierToolItem.setEnabled(false);
      }
    }
    final Class<?> finalFourierToolClass = fourierToolClass; // class must be final for action listener
    fourierToolItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          Method m = finalFourierToolClass.getMethod("getTool", (Class[]) null); //$NON-NLS-1$
          Tool tool = (Tool) m.invoke(null, (Object[]) null);
          tool.send(new LocalJob(drawingPanel), reply);
          if(tool instanceof OSPFrame) {
            ((OSPFrame) tool).setKeepHidden(false);
          }
          ((JFrame) tool).setVisible(true);
        } catch(Exception ex) {}
      }

    });
    // create snapshot menu item
    JMenuItem snapshotItem = new JMenuItem(DisplayRes.getString("DisplayPanel.Snapshot_menu_item")); //$NON-NLS-1$
    if(OSPRuntime.applet==null) {
      toolsMenu.add(snapshotItem);
    }
    snapshotItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SnapshotTool tool = SnapshotTool.getTool();
        if(drawingPanel!=null) {
          tool.saveImage(null, drawingPanel);
        } else {
          tool.saveImage(null, getContentPane());
        }
      }

    });
    // create video capture menu item
    JMenuItem videoItem = new JMenuItem(DisplayRes.getString("DrawingFrame.MenuItem.Capture")); //$NON-NLS-1$
    if(OSPRuntime.applet==null && false) { // video capture no longer supported.
      toolsMenu.add(videoItem);
    }
    Class<?> videoToolClass = null;
    if(false&& OSPRuntime.loadVideoTool) {  // video capture no longer supported.
      try {
        videoToolClass = Class.forName("org.opensourcephysics.tools.VideoCaptureTool"); //$NON-NLS-1$
      } catch(Exception ex) {
        OSPRuntime.loadVideoTool = false;
        OSPLog.finest("Cannot instantiate video capture tool class:\n"+ex.toString());  //$NON-NLS-1$
        videoItem.setEnabled(false);
      }
    }
    final Class<?> finalVideoToolClass = videoToolClass; // class must be final for action listener
    videoItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(drawingPanel.getVideoTool()==null) {
          try {
            Method m = finalVideoToolClass.getMethod("getTool", (Class[]) null);             //$NON-NLS-1$
            Tool tool = (Tool) m.invoke(null, (Object[]) null);                              // tool is a VideoTool
            drawingPanel.setVideoTool((VideoTool) tool);
            ((VideoTool) tool).setVisible(true);
            ((VideoTool) tool).clear();
          } catch(Exception ex) {
            OSPLog.finest("Cannot perform action to get video tool class:\n"+ex.toString()); //$NON-NLS-1$
          }
        } else {
          drawingPanel.getVideoTool().setVisible(true);
        }
      }

    });
    return toolsMenu;
  }

  /**
   * Sets a custom  properties inspector window.
   *
   * @param w the new inspector window
   */
  public void setCustomInspector(Window w) {
    if(customInspector!=null) {
      customInspector.setVisible(false); // hide the current inspector window
    }
    customInspector = w;
  }

  /**
   * Inspects the drawing frame by using an xml document tree.
   */
  public void inspectXML() {
	System.err.println("DrawingFrame Line 1018: customInspector="+customInspector);
    if(customInspector!=null) {
      customInspector.setVisible(true);
      return;
    }
    XMLControlElement xml = null;
    try {
      // if drawingPanel provides an xml loader, inspect the drawingPanel
      Method method = drawingPanel.getClass().getMethod("getLoader", (Class[]) null); //$NON-NLS-1$
      if((method!=null)&&Modifier.isStatic(method.getModifiers())) {
        xml = new XMLControlElement(drawingPanel);
      }
    } catch(NoSuchMethodException ex) {       // this drawing panel cannot be inspected
        System.err.println("DrawingFram line 1039 NoSuchMethodException xml="+xml);
      return;
    }
    // display a TreePanel in a modal dialog
    XMLTreePanel treePanel = new XMLTreePanel(xml);
    JDialog dialog = new JDialog((java.awt.Frame) null, true);
    dialog.setTitle("XML Inspector");
    System.err.println("Debug: DrawingFrame Line 1042");
    dialog.setContentPane(treePanel);  // WC: JS bug here!
    System.err.println("Debug: DrawingFrame Line 1444");
    dialog.setSize(new Dimension(600, 300));
    dialog.setVisible(true);
  }

  public void saveXML() {
    JFileChooser chooser = OSPRuntime.getChooser();
    if(chooser==null) {
        return;
     }
     String oldTitle = chooser.getDialogTitle();
     chooser.setDialogTitle("Save XML Data");
     int result = -1;
     try {
     	result = chooser.showSaveDialog(null);
     } catch (Throwable e) {
     	System.err.println("InterruptedException in saveXML()().");
     	e.printStackTrace();
     }
     chooser.setDialogTitle(oldTitle);
     if(result==JFileChooser.APPROVE_OPTION) {
        File file = chooser.getSelectedFile();
        // check to see if file already exists
        org.opensourcephysics.display.OSPRuntime.chooserDir = chooser.getCurrentDirectory().toString();
        String fileName = file.getAbsolutePath();
        // String fileName = XML.getRelativePath(file.getAbsolutePath());
        if((fileName==null)||fileName.trim().equals("")) {
           return;
        }
        int i = fileName.toLowerCase().lastIndexOf(".xml");
        if(i!=fileName.length()-4) {
           fileName += ".xml";
           file = new File(fileName);
        }
        if(/** @j2sNative false && */file.exists()) {
            int selected = JOptionPane.showConfirmDialog(null, "Replace existing "+file.getName()+"?", "Replace File",
               JOptionPane.YES_NO_CANCEL_OPTION);
            if(selected!=JOptionPane.YES_OPTION) {
               return;
            }
         }
        XMLControl xml = new XMLControlElement(drawingPanel);
        xml.write(fileName);
     }
  }

  /**
   * Returns an XML.ObjectLoader to save and load data for this program.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new DrawingFrameLoader();
  }

  static protected class DrawingFrameLoader implements XML.ObjectLoader {
    /**
     * createObject
     *
     * @param control XMLControl
     * @return Object
     */
    public Object createObject(XMLControl control) {
      DrawingFrame frame = new DrawingFrame();
      frame.setTitle(control.getString("title"));                                    //$NON-NLS-1$
      frame.setLocation(control.getInt("location x"), control.getInt("location y")); //$NON-NLS-1$ //$NON-NLS-2$
      frame.setSize(control.getInt("width"), control.getInt("height"));              //$NON-NLS-1$ //$NON-NLS-2$
      if(control.getBoolean("showing")) { //$NON-NLS-1$
        frame.setVisible(true);
      }
      return frame;
    }

    /**
     * Save data object's data in the control.
     *
     * @param control XMLControl
     * @param obj Object
     */
    public void saveObject(XMLControl control, Object obj) {
      DrawingFrame frame = (DrawingFrame) obj;
      control.setValue("title", frame.getTitle());                //$NON-NLS-1$
      control.setValue("showing", frame.isShowing());             //$NON-NLS-1$
      control.setValue("location x", frame.getLocation().x);      //$NON-NLS-1$
      control.setValue("location y", frame.getLocation().y);      //$NON-NLS-1$
      control.setValue("width", frame.getSize().width);           //$NON-NLS-1$
      control.setValue("height", frame.getSize().height);         //$NON-NLS-1$
      control.setValue("drawing panel", frame.getDrawingPanel()); //$NON-NLS-1$
    }

    /**
     * Loads the object with data from the control.
     *
     * @param control XMLControl
     * @param obj Object
     * @return Object
     */
    public Object loadObject(XMLControl control, Object obj) {
    	System.err.println("1136");
      DrawingFrame frame = ((DrawingFrame) obj);
      DrawingPanel panel = frame.getDrawingPanel();
      panel.clear();
      XMLControl panelControl = control.getChildControl("drawing panel"); //$NON-NLS-1$
      panelControl.loadObject(panel);
      panel.repaint();
      frame.setTitle(control.getString("title"));                                    //$NON-NLS-1$
      frame.setLocation(control.getInt("location x"), control.getInt("location y")); //$NON-NLS-1$ //$NON-NLS-2$
      frame.setSize(control.getInt("width"), control.getInt("height"));              //$NON-NLS-1$ //$NON-NLS-2$
      if(control.getBoolean("showing")) { //$NON-NLS-1$
        frame.setVisible(true);
      }
      return obj;
    }

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
