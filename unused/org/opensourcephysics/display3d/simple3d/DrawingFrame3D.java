/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.simple3d;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.RemoteException;
import java.util.Iterator;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.controls.XMLTreeChooser;
import org.opensourcephysics.controls.XMLTreePanel;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display3d.core.CameraInspector;
import org.opensourcephysics.tools.ExportTool;
import org.opensourcephysics.tools.LocalJob;
import org.opensourcephysics.tools.SnapshotTool;
import org.opensourcephysics.tools.Tool;
import org.opensourcephysics.tools.VideoTool;

/**
 * DrawingFrame: a frame that contains a generic drawing panel.
 * @author     Francisco Esquembre
 * @author     Adapted from Wolfgang Christian
 * @version    March 2005
 */
public class DrawingFrame3D extends OSPFrame implements ClipboardOwner, org.opensourcephysics.display3d.core.DrawingFrame3D {
  protected JMenu fileMenu, editMenu;
  protected JMenuItem copyItem, pasteItem, replaceItem;
  protected JMenu visualMenu, displayMenu, decorationMenu, cursorMenu;
  protected JMenuItem displayPerspectiveItem, displayNoPerspectiveItem, displayXYItem, displayXZItem, displayYZItem;
  protected JMenuItem decorationCubeItem, decorationNoneItem, decorationAxesItem;
  protected JMenuItem cursorNoneItem, cursorCubeItem, cursorXYZItem, cursorCrosshairItem;
  protected JMenuItem zoomToFitItem, resetCameraItem, cameraItem, lightItem;
  protected JFrame cameraInspectorFrame, lightInspectorFrame;
  protected JMenuBar menuBar = new JMenuBar();
  protected org.opensourcephysics.display3d.core.DrawingPanel3D drawingPanel;
  protected final static int MENU_SHORTCUT_KEY_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

  /**
   *  Default DrawingFrame constructor
   */
  public DrawingFrame3D() {
    this(DisplayRes.getString("DrawingFrame.DefaultTitle"), null); //$NON-NLS-1$
  }

  /**
   *  DrawingFrame constructor specifying the DrawingPanel that will be placed
   *  in the center of the content pane.
   * @param  drawingPanel
   */
  public DrawingFrame3D(DrawingPanel3D drawingPanel) {
    this(DisplayRes.getString("DrawingFrame.DefaultTitle"), drawingPanel); //$NON-NLS-1$
  }

  /**
   *  DrawingFrame constructor specifying the title and the DrawingPanel that
   *  will be placed in the center of the content pane.
   *
   * @param  title
   * @param  _drawingPanel
   */
  public DrawingFrame3D(String title, DrawingPanel3D _drawingPanel) {
    super(title);
    drawingPanel = _drawingPanel;
    if(drawingPanel!=null) {
      getContentPane().add((JPanel) drawingPanel, BorderLayout.CENTER);
    }
    pack();
    if(!OSPRuntime.appletMode) {
      createMenuBar();
    }
    setAnimated(true); // simulations will automatically render this frame after "doStep."
    setEnabledPaste(true);
    setEnabledReplace(true);
  }

  /**
   * Renders the drawing panel if the frame is showing and not iconified.
   */
  public void render() {
    drawingPanel.render();
  }

  /**
 * Shows a message in a yellow text box in the lower right hand corner.
 *
 * @param msg
 */
  public void setMessage(String msg) {
    ((org.opensourcephysics.display3d.simple3d.DrawingPanel3D) drawingPanel).setMessage(msg); // the default message box
  }

  /**
   * Shows a message in a yellow text box.
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
    ((org.opensourcephysics.display3d.simple3d.DrawingPanel3D) drawingPanel).setMessage(msg, location);
  }

  /**
   *  Gets the drawing panel.
   *
   * @return    the drawingPanel
   */
  public org.opensourcephysics.display3d.core.DrawingPanel3D getDrawingPanel3D() {
    return drawingPanel;
  }

  /**
   *  Adds the drawing panel to the the frame. The panel is added to the center
   *  of the frame's content pane.
   *
   * @param  _drawingPanel
   */
  public void setDrawingPanel3D(org.opensourcephysics.display3d.core.DrawingPanel3D _drawingPanel) {
    if(drawingPanel!=null) { // remove the old drawing panel.
      getContentPane().remove((JPanel) drawingPanel);
    }
    drawingPanel = _drawingPanel;
    if(drawingPanel!=null) {
      getContentPane().add((JPanel) drawingPanel, BorderLayout.CENTER);
    }
    pack();
  }

  /**
   * Getting the pointer to the real JFrame in it
   * @return JFrame
   */
  public javax.swing.JFrame getJFrame() {
    return this;
  }

  /**
   * Enables the paste edit menu item.
   * @param enable boolean
   */
  public void setEnabledPaste(boolean enable) {
    pasteItem.setEnabled(enable);
  }

  /**
   * Paste action
   *
   */
  protected void pasteAction() {
    try {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      Transferable data = clipboard.getContents(null);
      XMLControlElement control = new XMLControlElement();
      control.readXML((String) data.getTransferData(DataFlavor.stringFlavor));
      // get Drawables using an xml tree chooser
      XMLTreeChooser chooser = new XMLTreeChooser(DisplayRes.getString("DrawingFrame3D.XMLChooser.Title"), //$NON-NLS-1$
        DisplayRes.getString("DrawingFrame3D.XMLChooser.Message"),                                         //$NON-NLS-1$
          this);
      java.util.List<?> props = chooser.choose(control, Element.class);
      if(!props.isEmpty()) {
        Iterator<?> it = props.iterator();
        while(it.hasNext()) {
          XMLControl prop = (XMLControl) it.next();
          Element element = (Element) prop.loadObject(null);
          System.out.println("Adding element "+element);                                                   //$NON-NLS-1$
          drawingPanel.addElement(element);
        }
      }
      if(drawingPanel!=null) {
        drawingPanel.repaint();
      }
    } catch(UnsupportedFlavorException ex) {}
    catch(IOException ex) {}
    catch(HeadlessException ex) {}
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
   */
  public void replaceAction() {
    drawingPanel.removeAllElements();
    pasteAction();
  }

  /**
   * Copies objects found in the specified xml control.
   */
  protected void copyAction() {
    XMLControlElement control = new XMLControlElement(DrawingFrame3D.this);
    control.saveObject(null);
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

  /**
   * Whether this implementation supports LightInspectors
   * @return
   */
  protected boolean supportsLightInspectors() {
    return false;
  }

  /**
   * Dummy creator of a LightInspector. To be overwritten by packages that offer light inspectors
   * @param drawingPanel
   * @return
   */
  protected JFrame createLightInspectorFrame(org.opensourcephysics.display3d.core.DrawingPanel3D drawingPanel) {
    return null;
  }

  /**
   * Creates a standard DrawingFrame menu bar and adds it to the frame.
   */
  private void createMenuBar() {
    fileMenu = new JMenu(DisplayRes.getString("DrawingFrame.File_menu_item")); //$NON-NLS-1$
    JMenuItem printItem = new JMenuItem(DisplayRes.getString("DrawingFrame.Print_menu_item")); //$NON-NLS-1$
    if(!org.opensourcephysics.js.JSUtil.isJS)  printItem.setAccelerator(KeyStroke.getKeyStroke('P', MENU_SHORTCUT_KEY_MASK));
    if(!org.opensourcephysics.js.JSUtil.isJS) printItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintable((Printable) drawingPanel);
        if(printerJob.printDialog()) {
          try {
            printerJob.print();
          } catch(PrinterException pe) {
            JOptionPane.showMessageDialog(DrawingFrame3D.this, DisplayRes.getString("DrawingFrame.PrintErrorMessage"), //$NON-NLS-1$
              DisplayRes.getString("DrawingFrame.Error"), //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE);
          }
        }
      }

    });
    JMenuItem saveXMLItem = new JMenuItem(DisplayRes.getString("DrawingFrame.SaveXML_menu_item")); //$NON-NLS-1$
    saveXMLItem.setAccelerator(KeyStroke.getKeyStroke('S', MENU_SHORTCUT_KEY_MASK));
    saveXMLItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveXML();
      }

    });
    JMenuItem exportItem = new JMenuItem(DisplayRes.getString("DrawingFrame.Export_menu_item")); //$NON-NLS-1$
    exportItem.setAccelerator(KeyStroke.getKeyStroke('E', MENU_SHORTCUT_KEY_MASK));
    exportItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          ExportTool.getTool().send(new LocalJob(drawingPanel), null);
        } catch(RemoteException ex) {}
      }

    });
    JMenuItem saveAsPSItem = new JMenuItem(DisplayRes.getString("DrawingFrame.SaveFrameAsEPS_menu_item")); //$NON-NLS-1$
    saveAsPSItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // GUIUtils.saveImage(drawingPanel.getComponent(), "eps", DrawingFrame3D.this);
        GUIUtils.saveImage((JPanel) drawingPanel.getComponent(), "eps", DrawingFrame3D.this); //$NON-NLS-1$
      }

    });
    JMenuItem inspectItem = new JMenuItem(DisplayRes.getString("DrawingFrame.InspectMenuItem")); //$NON-NLS-1$
    inspectItem.setAccelerator(KeyStroke.getKeyStroke('I', MENU_SHORTCUT_KEY_MASK));
    inspectItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        inspectXML(); // cannot use a static method here because of run-time binding
      }

    });
    if(!org.opensourcephysics.js.JSUtil.isJS)fileMenu.add(printItem);
    fileMenu.add(saveXMLItem);
    if(!org.opensourcephysics.js.JSUtil.isJS)fileMenu.add(exportItem);
    if(!org.opensourcephysics.js.JSUtil.isJS)fileMenu.add(saveAsPSItem);
    fileMenu.add(inspectItem);
    menuBar.add(fileMenu);
    editMenu = new JMenu(DisplayRes.getString("DrawingFrame.Edit_menu_title")); //$NON-NLS-1$
    menuBar.add(editMenu);
    copyItem = new JMenuItem(DisplayRes.getString("DrawingFrame.Copy_menu_item")); //$NON-NLS-1$
    copyItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        copyAction();
      }

    });
    editMenu.add(copyItem);
    pasteItem = new JMenuItem(DisplayRes.getString("DrawingFrame.Paste_menu_item")); //$NON-NLS-1$
    pasteItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pasteAction();
      }

    });
    pasteItem.setEnabled(false); // not supported yet
    editMenu.add(pasteItem);
    replaceItem = new JMenuItem(DisplayRes.getString("DrawingFrame.Replace_menu_item")); //$NON-NLS-1$
    replaceItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        replaceAction();
      }

    });
    replaceItem.setEnabled(false); // not supported yet
    editMenu.add(replaceItem);
    setJMenuBar(menuBar);
    cameraItem = new JMenuItem(DisplayRes.getString("DrawingFrame3D.Camera_menu_item")); //$NON-NLS-1$
    cameraItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(drawingPanel!=null) {
          if(cameraInspectorFrame==null) {
            cameraInspectorFrame = CameraInspector.createFrame(drawingPanel);
          }
          cameraInspectorFrame.setVisible(true);
        }
      }

    });
    if(supportsLightInspectors()) {
      lightItem = new JMenuItem(DisplayRes.getString("DrawingFrame3D.Light_menu_item")); //$NON-NLS-1$
      lightItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if(drawingPanel!=null) {
            if(lightInspectorFrame==null) {
              lightInspectorFrame = createLightInspectorFrame(drawingPanel);
            }
            lightInspectorFrame.setVisible(true);
          }
        }

      });
    }
    decorationMenu = new JMenu(DisplayRes.getString("DrawingFrame3D.Decoration_menu"));                  //$NON-NLS-1$
    decorationNoneItem = new JMenuItem(DisplayRes.getString("DrawingFrame3D.DecorationNone_menu_item")); //$NON-NLS-1$
    decorationNoneItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(drawingPanel!=null) {
          drawingPanel.getVisualizationHints().setDecorationType(org.opensourcephysics.display3d.core.VisualizationHints.DECORATION_NONE);
          drawingPanel.repaint();
        }
      }

    });
    decorationMenu.add(decorationNoneItem);
    decorationCubeItem = new JMenuItem(DisplayRes.getString("DrawingFrame3D.DecorationCube_menu_item")); //$NON-NLS-1$
    decorationCubeItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(drawingPanel!=null) {
          drawingPanel.getVisualizationHints().setDecorationType(org.opensourcephysics.display3d.core.VisualizationHints.DECORATION_CUBE);
          drawingPanel.repaint();
        }
      }

    });
    decorationMenu.add(decorationCubeItem);
    decorationAxesItem = new JMenuItem(DisplayRes.getString("DrawingFrame3D.DecorationAxes_menu_item")); //$NON-NLS-1$
    decorationAxesItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(drawingPanel!=null) {
          drawingPanel.getVisualizationHints().setDecorationType(org.opensourcephysics.display3d.core.VisualizationHints.DECORATION_AXES);
          drawingPanel.repaint();
        }
      }

    });
    decorationMenu.add(decorationAxesItem);
    cursorMenu = new JMenu(DisplayRes.getString("DrawingFrame3D.Cursor_menu"));                  //$NON-NLS-1$
    cursorNoneItem = new JMenuItem(DisplayRes.getString("DrawingFrame3D.CursorNone_menu_item")); //$NON-NLS-1$
    cursorNoneItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(drawingPanel!=null) {
          drawingPanel.getVisualizationHints().setCursorType(org.opensourcephysics.display3d.core.VisualizationHints.CURSOR_NONE);
          drawingPanel.repaint();
        }
      }

    });
    cursorMenu.add(cursorNoneItem);
    cursorCubeItem = new JMenuItem(DisplayRes.getString("DrawingFrame3D.CursorCube_menu_item")); //$NON-NLS-1$
    cursorCubeItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(drawingPanel!=null) {
          drawingPanel.getVisualizationHints().setCursorType(org.opensourcephysics.display3d.core.VisualizationHints.CURSOR_CUBE);
          drawingPanel.repaint();
        }
      }

    });
    cursorMenu.add(cursorCubeItem);
    cursorXYZItem = new JMenuItem(DisplayRes.getString("DrawingFrame3D.CursorXYZ_menu_item")); //$NON-NLS-1$
    cursorXYZItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(drawingPanel!=null) {
          drawingPanel.getVisualizationHints().setCursorType(org.opensourcephysics.display3d.core.VisualizationHints.CURSOR_XYZ);
          drawingPanel.repaint();
        }
      }

    });
    cursorMenu.add(cursorXYZItem);
    cursorCrosshairItem = new JMenuItem(DisplayRes.getString("DrawingFrame3D.CursorCrosshair_menu_item")); //$NON-NLS-1$
    cursorCrosshairItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(drawingPanel!=null) {
          drawingPanel.getVisualizationHints().setCursorType(org.opensourcephysics.display3d.core.VisualizationHints.CURSOR_CROSSHAIR);
          drawingPanel.repaint();
        }
      }

    });
    cursorMenu.add(cursorCrosshairItem);
    zoomToFitItem = new JMenuItem(DisplayRes.getString("DrawingFrame3D.ZoomToFit_menu_item")); //$NON-NLS-1$
    zoomToFitItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(drawingPanel!=null) {
          drawingPanel.zoomToFit();
          drawingPanel.repaint();
        }
      }

    });
    visualMenu = new JMenu(DisplayRes.getString("DrawingFrame3D.Visual_menu")); //$NON-NLS-1$
    visualMenu.add(cameraItem);
    if(supportsLightInspectors()) {
      visualMenu.add(lightItem);
    }
    visualMenu.add(decorationMenu);
    visualMenu.add(cursorMenu);
    visualMenu.add(zoomToFitItem);
    menuBar.add(visualMenu);
    loadToolsMenu();
    JMenu helpMenu = new JMenu(DisplayRes.getString("DrawingFrame.Help_menu_item")); //$NON-NLS-1$
    menuBar.add(helpMenu);
    JMenuItem aboutItem = new JMenuItem(DisplayRes.getString("DrawingFrame.AboutOSP_menu_item")); //$NON-NLS-1$
    aboutItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        OSPRuntime.showAboutDialog(DrawingFrame3D.this);
      }

    });
    helpMenu.add(aboutItem);
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
    JMenuItem snapshotItem = new JMenuItem(DisplayRes.getString("DisplayPanel.Snapshot_menu_item")); //$NON-NLS-1$
    toolsMenu.add(snapshotItem);
    snapshotItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SnapshotTool tool = SnapshotTool.getTool();
        if(drawingPanel!=null) {
          tool.saveImage(null, drawingPanel.getComponent());
        } else {
          tool.saveImage(null, getContentPane());
        }
      }

    });
    // create video capture menu item
    JMenuItem videoItem = new JMenuItem(DisplayRes.getString("DrawingFrame.MenuItem.Capture")); //$NON-NLS-1$
    if(false) toolsMenu.add(videoItem); // video capture not supported.
    Class<?> videoToolClass = null;
    if(false&&OSPRuntime.loadVideoTool) { // video capture not supported.
      try {
        videoToolClass = Class.forName("org.opensourcephysics.tools.VideoCaptureTool"); //$NON-NLS-1$
      } catch(ClassNotFoundException ex) {
        OSPRuntime.loadVideoTool = false;
        videoItem.setEnabled(false);
        OSPLog.finest("Cannot instantiate video capture tool class:\n"+ex.toString());  //$NON-NLS-1$
      }
    }
    final Class<?> finalVideoToolClass = videoToolClass; // class must be final for action listener
    videoItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(drawingPanel.getVideoTool()==null) {
          try {
            Method m = finalVideoToolClass.getMethod("getTool", (Class[]) null); //$NON-NLS-1$
            Tool tool = (Tool) m.invoke(null, (Object[]) null);                  // tool is a VideoTool
            drawingPanel.setVideoTool((VideoTool) tool);
            ((VideoTool) tool).setVisible(true);
            ((VideoTool) tool).clear();
          } catch(Exception ex) {
        	OSPLog.warning("Video capture not supported");
          }
        } else {
          drawingPanel.getVideoTool().setVisible(true);
        }
      }

    });
    return toolsMenu;
  }

  /**
   * Gets a menu with the given name from the menu bar.  Returns null if menu item does not exist.
   *
   * @param menuName String
   * @return JMenu
   */
  public JMenu getMenuItem(String menuName) {
    menuName = menuName.trim();
    JMenu menu = null;
    for(int i = 0; i<menuBar.getMenuCount(); i++) {
      JMenu next = menuBar.getMenu(i);
      if(next.getText().equals(menuName)) {
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
  public JMenu removeMenuItem(String menuName) {
    menuName = menuName.trim();
    JMenu menu = null;
    for(int i = 0; i<menuBar.getMenuCount(); i++) {
      JMenu next = menuBar.getMenu(i);
      if(next.getText().equals(menuName)) {
        menu = next;
        menuBar.remove(i);
        break;
      }
    }
    return menu;
  }

  /**
   * Inspects the drawing frame by using an xml document tree.
   */
  public void inspectXML() {
    XMLControlElement xml = null;
    try {
      // if drawingPanel provides an xml loader, inspect the drawingPanel
      Method method = drawingPanel.getClass().getMethod("getLoader", (java.lang.Class[]) null); //$NON-NLS-1$
      if((method!=null)&&Modifier.isStatic(method.getModifiers())) {
        xml = new XMLControlElement(drawingPanel);
      }
    } catch(NoSuchMethodException ex) {
      // this drawing panel cannot be inspected
      return;
    }
    // display a TreePanel in a modal dialog
    JDialog dialog = new JDialog((java.awt.Frame) null, true);
    XMLTreePanel treePanel = new XMLTreePanel(xml);
    dialog.setTitle("XML Inspector");
    dialog.setContentPane(treePanel);
    dialog.setSize(new Dimension(600, 300));
    dialog.setVisible(true);
  }

  public void saveXML() {
    JFileChooser chooser = OSPRuntime.getChooser();
    int result = chooser.showSaveDialog(null);
    if(result==JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      // check to see if file already exists
      if(file.exists()) {
        int selected = JOptionPane.showConfirmDialog(null, DisplayRes.getString("DrawingFrame.ReplaceExisting_message")+file.getName() //$NON-NLS-1$
          +DisplayRes.getString("DrawingFrame.QuestionMark"),                                     //$NON-NLS-1$
            DisplayRes.getString("DrawingFrame.ReplaceFile_option_title"),                        //$NON-NLS-1$
              JOptionPane.YES_NO_CANCEL_OPTION);
        if(selected!=JOptionPane.YES_OPTION) {
          return;
        }
      }
      String fileName = XML.getRelativePath(file.getAbsolutePath());
      if((fileName==null)||fileName.trim().equals("")) {                                          //$NON-NLS-1$
        return;
      }
      int i = fileName.toLowerCase().lastIndexOf(".xml");                                         //$NON-NLS-1$
      if(i!=fileName.length()-4) {
        fileName += ".xml";                                                                       //$NON-NLS-1$
      }
      try {
        // if drawingPanel provides an xml loader, save the drawingPanel
        Method method = drawingPanel.getClass().getMethod("getLoader", (java.lang.Class[]) null); //$NON-NLS-1$
        if((method!=null)&&Modifier.isStatic(method.getModifiers())) {
          XMLControl xml = new XMLControlElement(drawingPanel);
          xml.write(fileName);
        }
      } catch(NoSuchMethodException ex) {
        // this drawingPanel cannot be saved
        return;
      }
    }
  }

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------
  public static XML.ObjectLoader getLoader() {
    return new org.opensourcephysics.display3d.core.DrawingFrame3D.Loader();
  }

}
/*
//CJB
private static final GridBagConstraints gbc;

static {
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.CENTER;
}


private JPanel wrapInBackgroundImage(JComponent component,Icon backgroundIcon,
                          int verticalAlignment, int horizontalAlignment) {

   // make the passed in swing component transparent
   component.setOpaque(false);

   // create wrapper JPanel
   JPanel backgroundPanel = new JPanel(new GridBagLayout());

   // add the passed in swing component first to ensure that it is in front
   backgroundPanel.add(component, gbc);

   // create a label to paint the background image
   JLabel backgroundImage = new JLabel(backgroundIcon);

   // set minimum and preferred sizes so that the size of the image
   backgroundImage.setPreferredSize(new Dimension(1,1));
   backgroundImage.setMinimumSize(new Dimension(1,1));

   // align the image as specified.
   backgroundImage.setVerticalAlignment(verticalAlignment);
   backgroundImage.setHorizontalAlignment(horizontalAlignment);

   // add the background label
   backgroundPanel.add(backgroundImage, gbc);

   return backgroundPanel;

}

public void setBackground(String name){
    //Image Transformation (Escale Factor)
    ImageIcon image = null;
    image = new ImageIcon(this.getClass().getClassLoader().getResource(name));
    this.add(wrapInBackgroundImage((JPanel)this.getComponent(),image, JLabel.TOP, JLabel.LEADING));
 }
 //CJB*/

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

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
