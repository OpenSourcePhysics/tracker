/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.SwingPropertyChangeSupport;
import org.opensourcephysics.controls.Animation;
import org.opensourcephysics.controls.Calculation;
import org.opensourcephysics.controls.ControlUtils;
import org.opensourcephysics.controls.MainFrame;
import org.opensourcephysics.controls.OSPApplication;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.controls.XMLTreePanel;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.Hidable;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.PrintUtils;
import org.opensourcephysics.ejs.EjsRes;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.Job;
import org.opensourcephysics.tools.LocalJob;
import org.opensourcephysics.tools.SnapshotTool;
import org.opensourcephysics.tools.Tool;
import org.opensourcephysics.tools.ToolsRes;
import org.opensourcephysics.tools.VideoTool;

/**
 * EjsControlFrame defines an Easy Java Simulations control that is guaranteed to have a
 * parent frame.
 *
 * @author       Wolfgang Christian
 * @version 1.0
 */
public class EjsControlFrame extends ParsedEjsControl implements RootPaneContainer, MainFrame {
  final static int MENU_SHORTCUT_KEY_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
  protected Tool reply;
  protected JMenuItem[] languageItems;
  protected JMenuItem translateItem, snapshotItem, videoItem, clearItem;
  protected JMenu languageMenu;
  EjsFrame mainFrame = new EjsFrame();
  DrawingPanel defaultDrawingPanel;
  protected JFrame messageFrame = new JFrame(EjsRes.getString("EjsControlFrame.Messages_frame_title")); //$NON-NLS-1$
  TextArea messageArea = new TextArea(20, 20);
  Object model;
  JMenuBar menuBar;
  volatile protected XMLControlElement xmlDefault = null;
  protected PropertyChangeSupport support = new SwingPropertyChangeSupport(this);

  /**
   * Constructor EjsControlFrame
   *
   * @param _simulation
   */
  public EjsControlFrame(Object _simulation) {
    this(_simulation, "name=controlFrame;title=Control Frame;location=400,0;layout=border;exit=false; visible=true"); //$NON-NLS-1$
  }

  /**
   * Constructor EjsControlFrame
   *
   * @param _simulation
   * @param param
   */
  public EjsControlFrame(Object _simulation, String param) {
    super(_simulation);
    mainFrame.addChildFrame(messageFrame);
    model = _simulation;
    mainFrame.setName("controlFrame");    //$NON-NLS-1$
    addObject(mainFrame, "Frame", param); //$NON-NLS-1$
    createMenuBar();
    if(OSPRuntime.appletMode) {
      mainFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    }
    messageFrame.getContentPane().add(messageArea);
    messageFrame.setSize(300, 175);
    // responds to data from the DatasetTool
    reply = new Tool() {
      public void send(Job job, Tool replyTo) throws RemoteException {
        if(defaultDrawingPanel==null) {
          return;
        }
        XMLControlElement control = new XMLControlElement();
        try {
          control.readXML(job.getXML());
        } catch(RemoteException ex) {}
        ArrayList<?> datasets = defaultDrawingPanel.getObjectOfClass(Dataset.class);
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
        defaultDrawingPanel.repaint();
      }

    };
  }

  /**
   * Adds a child frame that depends on the main frame.
   * Child frames are closed when this frame is closed.
   *
   * @param mainFrame JFrame
   */
  public void addChildFrame(JFrame child) {
    if((mainFrame==null)||(child==null)) {
      return;
    }
    mainFrame.addChildFrame(child);
  }

  /**
   * Clears the child frames from the main frame.
   */
  public void clearChildFrames() {
    if(mainFrame==null) {
      return;
    }
    mainFrame.clearChildFrames();
  }

  /**
   * Gets a copy of the ChildFrames collection.
   * @return Collection
   */
  public Collection<JFrame> getChildFrames() {
    return mainFrame.getChildFrames();
  }

  /**
   * Gets the OSPFrame that contains the control.
   *
   * @return
   */
  public OSPFrame getMainFrame() {
    return mainFrame;
  }

  /**
   * Gets the OSPFrame that contains the control.  Replaced by getMainFrame to implement MainFrame interface.
   * @deprecated
   * @return
   */
  public OSPFrame getFrame() {
    return mainFrame;
  }

  private void createMenuBar() {
    menuBar = new JMenuBar();
    mainFrame.setJMenuBar(menuBar);
    JMenu fileMenu = new JMenu(EjsRes.getString("EjsControlFrame.File_menu")); //$NON-NLS-1$
    if(OSPRuntime.applet==null) {
      menuBar.add(fileMenu);
    }
    JMenuItem readItem = new JMenuItem(EjsRes.getString("EjsControlFrame.Read_menu_item")); //$NON-NLS-1$
    clearItem = new JMenuItem(EjsRes.getString("EjsControlFrame.Clear_menu_item")); //$NON-NLS-1$
    clearItem.setEnabled(false);
    JMenuItem saveAsItem = new JMenuItem(EjsRes.getString("EjsControlFrame.SaveAs_menu_item"));                  //$NON-NLS-1$
    JMenuItem inspectItem = new JMenuItem(EjsRes.getString("EjsControlFrame.Inspect_menu_item"));                //$NON-NLS-1$
    JMenuItem printFrameItem = new JMenuItem(DisplayRes.getString("DrawingFrame.PrintFrame_menu_item"));         //$NON-NLS-1$
    JMenuItem saveFrameAsEPSItem = new JMenuItem(DisplayRes.getString("DrawingFrame.SaveFrameAsEPS_menu_item")); //$NON-NLS-1$
    JMenu printMenu = new JMenu(DisplayRes.getString("DrawingFrame.Print_menu_title")); //$NON-NLS-1$
    fileMenu.add(readItem);
    fileMenu.add(saveAsItem);
    fileMenu.add(inspectItem);
    fileMenu.add(clearItem);
    fileMenu.add(printMenu);
    printMenu.add(printFrameItem);
    printMenu.add(saveFrameAsEPSItem);
    readItem.setAccelerator(KeyStroke.getKeyStroke('R', MENU_SHORTCUT_KEY_MASK));
    readItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // readParameters(); // cannot use a static method here because of run-time binding
        loadXML((String) null);
        support.firePropertyChange("xmlDefault", null, xmlDefault); //$NON-NLS-1$
        mainFrame.repaint();
      }

    });
    clearItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        clearDefaultXML();
      }

    });
    saveAsItem.setAccelerator(KeyStroke.getKeyStroke('S', MENU_SHORTCUT_KEY_MASK));
    saveAsItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveXML();
      }

    });
    inspectItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        inspectXML(); // cannot use a static method here because of run-time binding
      }

    });
    // print item
    printFrameItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        PrintUtils.printComponent(mainFrame);
      }

    });
    // save as EPS item
    saveFrameAsEPSItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          PrintUtils.saveComponentAsEPS(mainFrame);
        } catch(IOException ex) {}
      }

    });
    loadDisplayMenu();
    loadToolsMenu();
    //help menu
    JMenu helpMenu = new JMenu(EjsRes.getString("EjsControlFrame.Help_menu")); //$NON-NLS-1$
    menuBar.add(helpMenu);
    JMenuItem aboutItem = new JMenuItem(EjsRes.getString("EjsControlFrame.About_menu_item")); //$NON-NLS-1$
    aboutItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        OSPRuntime.showAboutDialog(EjsControlFrame.this.getMainFrame());
      }

    });
    helpMenu.add(aboutItem);
    JMenuItem sysItem = new JMenuItem(EjsRes.getString("EjsControlFrame.System_menu_item")); //$NON-NLS-1$
    sysItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ControlUtils.showSystemProperties(true);
      }

    });
    helpMenu.add(sysItem);
    helpMenu.addSeparator();
    JMenuItem logItem = new JMenuItem(EjsRes.getString("EjsControlFrame.MessageLog_menu_item")); //$NON-NLS-1$
    logItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        OSPLog.showLog();
      }

    });
    helpMenu.add(logItem);
    menuBar.add(helpMenu);
  }

  /**
   * Adds a Tools menu to the menu bar.
   */
  protected JMenu loadToolsMenu() {
	if(org.opensourcephysics.js.JSUtil.isJS) {  // external tools not supported in JavaScript.
		  return null;
	}
    JMenuBar menuBar = mainFrame.getJMenuBar();
    if(menuBar==null) {
      return null;
    }
    // create Tools menu item
    JMenu toolsMenu = new JMenu(DisplayRes.getString("DrawingFrame.Tools_menu_title")); //$NON-NLS-1$
    menuBar.add(toolsMenu);
    // create dataset tool menu item if the tool exists in classpath
    JMenuItem datasetItem = new JMenuItem(DisplayRes.getString("DrawingFrame.DatasetTool_menu_item")); //$NON-NLS-1$
    toolsMenu.add(datasetItem);
    Class<?> datasetToolClass = null;
    if(OSPRuntime.loadDataTool) {
      try {
        datasetToolClass = Class.forName("org.opensourcephysics.tools.DataTool");      //$NON-NLS-1$
      } catch(ClassNotFoundException ex) {
        OSPRuntime.loadDataTool = false;
        OSPLog.finest("Cannot instantiate data analysis tool class:\n"+ex.toString()); //$NON-NLS-1$
        datasetItem.setEnabled(false);
      }
    }
    final Class<?> finalDatasetToolClass = datasetToolClass; // class must be final for action listener
    datasetItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          Method m = finalDatasetToolClass.getMethod("getTool", (Class[]) null); //$NON-NLS-1$
          Tool tool = (Tool) m.invoke(null, (Object[]) null);
          tool.send(new LocalJob(defaultDrawingPanel), reply);
          if(tool instanceof OSPFrame) {
            ((OSPFrame) tool).setKeepHidden(false);
          }
          ((JFrame) tool).setVisible(true);
        } catch(Exception ex) {
          System.out.println(ex);
        }
      }

    });
    // create snapshot menu item
    snapshotItem = new JMenuItem(DisplayRes.getString("DisplayPanel.Snapshot_menu_item")); //$NON-NLS-1$
    snapshotItem.setEnabled(false);
    if(OSPRuntime.applet==null) {
      toolsMenu.add(snapshotItem);
    }
    snapshotItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SnapshotTool tool = SnapshotTool.getTool();
        if(defaultDrawingPanel!=null) {
          tool.saveImage(null, defaultDrawingPanel);
        } else {
          tool.saveImage(null, getContentPane());
        }
      }

    });
    // create video capture menu item
    JMenuItem videoItem = new JMenuItem(DisplayRes.getString("DrawingFrame.MenuItem.Capture")); //$NON-NLS-1$
    if(false && OSPRuntime.applet==null) { // video capture not supported
      toolsMenu.add(videoItem);
    }
    Class<?> videoToolClass = null;
    if(OSPRuntime.loadVideoTool) {
      try {
        videoToolClass = Class.forName("org.opensourcephysics.tools.VideoCaptureTool"); //$NON-NLS-1$
      } catch(ClassNotFoundException ex) {
        OSPRuntime.loadVideoTool = false;
        OSPLog.finest("Cannot instantiate video capture tool class:\n"+ex.toString());  //$NON-NLS-1$
        videoItem.setEnabled(false);
      }
    }
    final Class<?> finalVideoToolClass = videoToolClass; // class must be final for action listener
    videoItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(defaultDrawingPanel.getVideoTool()==null) {
          try {
            Method m = finalVideoToolClass.getMethod("getTool", (Class[]) null); //$NON-NLS-1$
            Tool tool = (Tool) m.invoke(null, (Object[]) null);                  // tool is a VideoTool
            defaultDrawingPanel.setVideoTool((VideoTool) tool);
            ((VideoTool) tool).setVisible(true);
            ((VideoTool) tool).clear();
          } catch(Exception ex) {
        	  OSPLog.warning("Video Capature not supported.");
          }
        } else {
          defaultDrawingPanel.getVideoTool().setVisible(true);
        }
      }

    });
    return toolsMenu;
  }

  /**
   * Adds a Display menu to the menu bar.
   */
  protected void loadDisplayMenu() {
    JMenu displayMenu = new JMenu();
    displayMenu.setText(EjsRes.getString("EjsControlFrame.Display_menu")); //$NON-NLS-1$
    menuBar.add(displayMenu);
    // language menu
    languageMenu = new JMenu();
    languageMenu.setText(EjsRes.getString("EjsControlFrame.Language")); //$NON-NLS-1$
    translateItem = new JMenuItem();
    translateItem.setText(EjsRes.getString("EjsControlFrame.Translate")); //$NON-NLS-1$
    // added by D Brown 2007-10-17
    if(OSPRuntime.getTranslator()!=null) {
      translateItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          OSPRuntime.getTranslator().showProperties(model.getClass());
          if(OSPRuntime.getTranslator() instanceof Hidable) {
            ((Hidable) OSPRuntime.getTranslator()).setKeepHidden(false);
          }
          OSPRuntime.getTranslator().setVisible(true);
        }

      });
    }
    translateItem.setEnabled(OSPRuntime.getTranslator()!=null);
    languageMenu.add(translateItem, 0);
    final Locale[] locales = OSPRuntime.getInstalledLocales();
    Action languageAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        String language = e.getActionCommand();
        OSPLog.finest("setting language to "+language); //$NON-NLS-1$
        for(int i = 0; i<locales.length; i++) {
          if(language.equals(locales[i].getDisplayName())) {
            ToolsRes.setLocale(locales[i]);
            return;
          }
        }
      }

    };
    ButtonGroup languageGroup = new ButtonGroup();
    languageItems = new JMenuItem[locales.length];
    for(int i = 0; i<locales.length; i++) {
      languageItems[i] = new JRadioButtonMenuItem(locales[i].getDisplayName(locales[i]));
      languageItems[i].setActionCommand(locales[i].getDisplayName());
      languageItems[i].addActionListener(languageAction);
      languageMenu.add(languageItems[i]);
      languageGroup.add(languageItems[i]);
    }
    for(int i = 0; i<locales.length; i++) {
      if(locales[i].getLanguage().equals(ToolsRes.getLanguage())) {
        languageItems[i].setSelected(true);
      }
    }
    displayMenu.add(languageMenu);
    JMenu fontMenu = new JMenu(EjsRes.getString("EjsControlFrame.Font_menu")); //$NON-NLS-1$
    displayMenu.add(fontMenu);
    JMenuItem sizeUpItem = new JMenuItem();
    sizeUpItem.setText(EjsRes.getString("EjsControlFrame.IncreaseFontSize_menu_item")); //$NON-NLS-1$
    sizeUpItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        FontSizer.levelUp();
      }

    });
    fontMenu.add(sizeUpItem);
    final JMenuItem sizeDownItem = new JMenuItem();
    sizeDownItem.setText(EjsRes.getString("EjsControlFrame.DecreaseFontSize_menu_item")); //$NON-NLS-1$
    sizeDownItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        FontSizer.levelDown();
      }

    });
    fontMenu.add(sizeDownItem);
    JMenu aliasMenu = new JMenu(EjsRes.getString("EjsControlFrame.AntiAlias_menu")); //$NON-NLS-1$
    displayMenu.add(aliasMenu);
    final JCheckBoxMenuItem textAliasItem = new JCheckBoxMenuItem(EjsRes.getString("EjsControlFrame.Text_check_box"), //$NON-NLS-1$
      false);
    textAliasItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        defaultDrawingPanel.setAntialiasTextOn(textAliasItem.isSelected());
        defaultDrawingPanel.repaint();
      }

    });
    aliasMenu.add(textAliasItem);
    final JCheckBoxMenuItem shapeAliasItem = new JCheckBoxMenuItem(EjsRes.getString("EjsControlFrame.Drawing_check_box"), false); //$NON-NLS-1$
    shapeAliasItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        defaultDrawingPanel.setAntialiasShapeOn(shapeAliasItem.isSelected());
        defaultDrawingPanel.repaint();
      }

    });
    fontMenu.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        sizeDownItem.setEnabled(FontSizer.getLevel()>0);
      }

    });
    aliasMenu.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if(defaultDrawingPanel==null) {
          textAliasItem.setEnabled(false);
          shapeAliasItem.setEnabled(false);
        } else {
          textAliasItem.setEnabled(true);
          textAliasItem.setEnabled(true);
          textAliasItem.setSelected(defaultDrawingPanel.isAntialiasTextOn());
          shapeAliasItem.setSelected(defaultDrawingPanel.isAntialiasShapeOn());
        }
      }

    });
    aliasMenu.add(shapeAliasItem);
    menuBar.add(displayMenu);
    ToolsRes.addPropertyChangeListener("locale", new PropertyChangeListener() { //$NON-NLS-1$
      public void propertyChange(PropertyChangeEvent e) {
        refreshGUI();
      }

    });
  }

  /**
   * Draws the frame into a graphics object suitable for printing.
   * @param g
   * @param pageFormat
   * @param pageIndex
   * @return status code
   * @exception PrinterException
   */
  public int printToGraphics(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
    if(pageIndex>=1) { // only one page available
      return Printable.NO_SUCH_PAGE;
    }
    if(g==null) {
      return Printable.NO_SUCH_PAGE;
    }
    Graphics2D g2 = (Graphics2D) g;
    double scalex = pageFormat.getImageableWidth()/mainFrame.getWidth();
    double scaley = pageFormat.getImageableHeight()/mainFrame.getHeight();
    double scale = Math.min(scalex, scaley);
    g2.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());
    g2.scale(scale, scale);
    //frame.paintAll(g2);
    mainFrame.paint(g);
    return Printable.PAGE_EXISTS;
  }

  protected void refreshGUI() {
    createMenuBar();
    mainFrame.pack();
  }

  /**
   * Adds a PropertyChangeListener.
   *
   * @param listener the object requesting property change notification
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
  }

  /**
   * Removes a PropertyChangeListener.
   *
   * @param listener the listener requesting removal
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    support.removePropertyChangeListener(listener);
  }

  /**
   * Prints a string in the control's message area followed by a CR and LF.
   * GUI controls will usually display messages in a non-editable text area.
   *
   * @param s
   */
  public void println(String s) {
    print(s+"\n"); //$NON-NLS-1$
  }

  /**
   * Prints a blank line in the control's message area.  GUI controls will usually display
   * messages in a non-editable text area.
   */
  public void println() {
    print("\n"); //$NON-NLS-1$
  }

  /**
   * Prints a string in the control's message area.
   * GUI controls will usually display messages in a non-editable text area.
   *
   * @param s
   */
  public void print(final String s) {
    if(s==null) {
      return; // nothing to print
    }
    messageFrame.setVisible(true);
    if(SwingUtilities.isEventDispatchThread()||Thread.currentThread().getName().equals("main")) { //$NON-NLS-1$
      messageArea.append(s);
      return;
    }
    Runnable doLater = new Runnable() {
      public void run() {
        messageArea.append(s);
      }

    };
    // Update from the event queue.
    java.awt.EventQueue.invokeLater(doLater);
  }

  /**
   *  Remove all text from the message area.
   */
  public void clearMessages() {
    if(SwingUtilities.isEventDispatchThread()||Thread.currentThread().getName().equals("main")) { //$NON-NLS-1$
      messageArea.setText("");                                                                    //$NON-NLS-1$
      return;
    }
    Runnable doLater = new Runnable() {
      public void run() {
        messageArea.setText(""); //$NON-NLS-1$
      }

    };
    // Update from the event queue.
    java.awt.EventQueue.invokeLater(doLater);
  }

  //  private void appendMessageX(final String str) {
  //    Runnable doRun = new Runnable() {
  //      public void run() {
  //        messageArea.append(str);
  //      }
  //    };
  //    if(SwingUtilities.isEventDispatchThread()) {
  //      doRun.run();
  //      }else{
  //      SwingUtilities.invokeLater(doRun);
  //      }
  //  }
  //

  /**
   * Stops the animation and prints a message.
   * @param message String
   */
  public void calculationDone(String message) {
    if((message==null)||message.trim().equals("")) { //$NON-NLS-1$
      return;
    }
    super.calculationDone(message);
  }

  /**
   * Renders the frame.  Subclass this method to render the contents of this frame in the calling thread.
   */
  public void render() {}

  /**
   * Adds an existing object to this control.
   * @param object Object
   * @param classname String
   * @param propList String
   * @return ControlElement
   */
  public ControlElement addObject(Object object, String classname, String propList) {
    if(object instanceof DrawingPanel) {
      defaultDrawingPanel = (DrawingPanel) object;
      if(snapshotItem!=null) {
        snapshotItem.setEnabled(true);
      }
      if(videoItem!=null) {
        videoItem.setEnabled(true);
      }
    }
    return super.addObject(object, classname, propList);
  }

  /**
   * Clears data from drawing objects within this frame.
   *
   * Override this method to clear objects that have data.
   */
  public void clearData() {
    if(defaultDrawingPanel!=null) {
      ArrayList<?> list = defaultDrawingPanel.getDrawables(Dataset.class);
      Iterator<?> it = list.iterator();
      while(it.hasNext()) { // copy only the objects of the correct type
        Dataset obj = (Dataset) it.next();
        obj.clear();
      }
      list = defaultDrawingPanel.getDrawables(DatasetManager.class);
      it = list.iterator();
      while(it.hasNext()) { // copy only the objects of the correct type
        DatasetManager obj = (DatasetManager) it.next();
        obj.clear();
      }
      defaultDrawingPanel.invalidateImage();
    }
  }

  /**
   * Clears data from drawing objects within this frame.
   *
   * Override this method to clear and repaint objects that have data.
   */
  public void clearDataAndRepaint() {
    clearData();
    if(defaultDrawingPanel!=null) {
      defaultDrawingPanel.repaint();
    }
  }

  /**
   * Gets the frame that contains the control.
   *
   * @return
   */
  public Container getTopLevelAncestor() {
    return mainFrame;
  }

  /**
   * Gets the frame's root pane.  Implementation of RootPaneContainer.
   *
   * @return
   */
  public JRootPane getRootPane() {
    return mainFrame.getRootPane();
  }

  /**
   * Gets the frame's content pane. Implementation of RootPaneContainer.
   *
   * @return content pane of the frame
   */
  public Container getContentPane() {
    return mainFrame.getContentPane();
  }

  /**
   * Sets the frame's content pane. Implementation of RootPaneContainer.
   * @param contentPane
   */
  public void setContentPane(Container contentPane) {
    mainFrame.setContentPane(contentPane);
  }

  /**
   * Implementation of RootPaneContainer.
   *
   * @see javax.swing.RootPaneContainer
   *
   * @return layeredPane of the frame
   */
  public JLayeredPane getLayeredPane() {
    return mainFrame.getLayeredPane();
  }

  /**
   * Implementation of RootPaneContainer.
   *
   * @see javax.swing.RootPaneContainer
   * @param layeredPane
   */
  public void setLayeredPane(JLayeredPane layeredPane) {
    mainFrame.setLayeredPane(layeredPane);
  }

  /**
   * Implementation of RootPaneContainer.
   *
   * @see javax.swing.RootPaneContainer
   *
   * @return glass pane component
   */
  public Component getGlassPane() {
    return mainFrame.getGlassPane();
  }

  /**
   * Implementation of RootPaneContainer.
   *
   * @see javax.swing.RootPaneContainer
   * @param glassPane
   */
  public void setGlassPane(Component glassPane) {
    mainFrame.setGlassPane(glassPane);
  }

  /*
   * Creates a menu in the menu bar from the given XML document.
   * @param xmlMenu name of the xml file with menu data
   */
  public void parseXMLMenu(String xmlMenu) {
    System.out.println("The parseXMLMenu method has been disabled to reduce the size OSP jar files."); //$NON-NLS-1$
    //parseXMLMenu(xmlMenu, null);
  }

  /*
   *  public void parseXMLMenu(String xmlMenu) {
   *     if(menuBar==null) {
   *        return;
   *     }
   *     XMLControl xml = new XMLControlElement(xmlMenu);
   *     if(xml.failedToRead()) {
   *        OSPLog.info("Tools menu not found: "+xmlMenu);   //$NON-NLS-1$
   *     } else {
   *        Class type = xml.getObjectClass();
   *        if((type!=null)&&org.opensourcephysics.tools.LaunchNode.class.isAssignableFrom(type)) {
   *           // load the xml data into a launch node and add the menu item
   *           org.opensourcephysics.tools.LaunchNode node = (org.opensourcephysics.tools.LaunchNode) xml.loadObject(null);
   *           // get the menu name and find or create the menu
   *           String menuName = node.toString();
   *           JMenu menu = null;
   *           for(int i = 0; i<menuBar.getMenuCount(); i++) {
   *              JMenu next = menuBar.getMenu(i);
   *              if(next.getText().equals(menuName)) {
   *                 menu = next;
   *                 break;
   *              }
   *           }
   *           if(menu==null) {
   *              menu = new JMenu(menuName);
   *              menuBar.add(menu);
   *              menuBar.validate();
   *           }
   *           // add the node item to the menu
   *           node.setLaunchObject(model);
   *           node.addMenuItemsTo(menu);
   *           OSPLog.finest("Tools menu loaded: "+xmlMenu); //$NON-NLS-1$
   *        }
   *     }
   *  }
   */
  // The following methods the an XML framework for OSPApplications.
  protected OSPApplication app;

  public void saveXML() {
    JFileChooser chooser = OSPRuntime.getChooser();
    int result = chooser.showSaveDialog(null);
    if(result==JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      // check to see if file already exists
      if(file.exists()) {
        int selected = JOptionPane.showConfirmDialog(null, EjsRes.getString("EjsControlFrame.ReplaceExisting_dialog")+file.getName() //$NON-NLS-1$
          +EjsRes.getString("EjsControlFrame.question_mark"), EjsRes.getString( //$NON-NLS-1$
          "EjsControlFrame.RepalceFile_dialog_message"),                        //$NON-NLS-1$
            JOptionPane.YES_NO_CANCEL_OPTION);
        if(selected!=JOptionPane.YES_OPTION) {
          return;
        }
      }
      OSPRuntime.chooserDir = chooser.getCurrentDirectory().toString();
      String fileName = XML.getRelativePath(file.getAbsolutePath());
      if((fileName==null)||fileName.trim().equals("")) {                        //$NON-NLS-1$
        return;
      }
      int i = fileName.toLowerCase().lastIndexOf(".xml");                       //$NON-NLS-1$
      if(i!=fileName.length()-4) {
        fileName += ".xml";                                                     //$NON-NLS-1$
      }
      XMLControl xml = new XMLControlElement(getOSPApp());
      xml.write(fileName);
    }
  }

  /**
   * Gets the OSP Application controlled by this frame.
   */
  public OSPApplication getOSPApp() {
    if(app==null) {
      app = new OSPApplication(this, model);
    }
    return app;
  }

  /**
   * Loads the current XML default.
   */
  public void loadDefaultXML() {
    if(xmlDefault!=null) {
      xmlDefault.loadObject(getOSPApp());
      clearItem.setEnabled(true);
    }
  }

  /**
   * Clears the current XML default.
   */
  public void clearDefaultXML() {
    if((xmlDefault==null)||(model==null)) {
      return;
    }
    xmlDefault = null;
    clearItem.setEnabled(false);
    if(model instanceof Calculation) {
      ((Calculation) model).resetCalculation();
      ((Calculation) model).calculate();
    } else if(model instanceof Animation) {
      ((Animation) model).stopAnimation();
      ((Animation) model).resetAnimation();
      ((Animation) model).initializeAnimation();
    }
    GUIUtils.repaintOSPFrames();
  }

  public void loadXML(String fileName) {
    if((fileName==null)||fileName.trim().equals("")) { //$NON-NLS-1$
      loadXML();
      return;
    }
    XMLControlElement xml = null;
    try {
      xml = new XMLControlElement(fileName);
    } catch(Exception ex) {
      System.out.println("XML file not loaded: "+fileName); //$NON-NLS-1$
      System.out.println("EjsControlFrame Exception: "+ex); //$NON-NLS-1$
      //ex.printStackTrace();
      return;
    }
    loadXML(xml, false);
  }

  public void loadXML(XMLControlElement xml, boolean compatibleModel) {
    if(xml==null) {
      OSPLog.finer("XML not found in EjsControlFrame loadXML method."); //$NON-NLS-1$
      return;
    }
    // load xml into app if xml object class is an OSPApplication
    if(OSPApplication.class.isAssignableFrom(xml.getObjectClass())) {
      XMLControlElement mControl = (XMLControlElement) xml.getChildControl("model");   //$NON-NLS-1$
      if(mControl==null) {                                                             // control class cannot be found
        JOptionPane.showMessageDialog(mainFrame, "XML Control not found.",             //$NON-NLS-1$
          "Data not loaded.", JOptionPane.WARNING_MESSAGE);                            //$NON-NLS-1$
        return;
      }
      Class<?> modelClass = mControl.getObjectClass();
      if(modelClass==null) {                                                           // model class cannot be found in classpath
        JOptionPane.showMessageDialog(mainFrame, "Model specified in file not found.", //$NON-NLS-1$
          "Data not loaded.", JOptionPane.WARNING_MESSAGE);                            //$NON-NLS-1$
        return;
      }
      //System.out.println("model in xml " +modelClass);
      //System.out.println("this model " +model.getClass());
      boolean loaderMatch = XML.getLoader(modelClass).getClass()==XML.getLoader(model.getClass()).getClass();
      compatibleModel = compatibleModel||(modelClass==model.getClass())||              // identical model classes are always compatible
        (modelClass.isAssignableFrom(model.getClass())&&loaderMatch); // subclasses with identical loaders are assumed to be compatible
      //System.out.println("xml model Assignable to this model " +modelClass.isAssignableFrom(model.getClass()));
      //System.out.println("this model Assignable to xml model " +model.getClass().isAssignableFrom(modelClass));
      if(!compatibleModel) {
        JOptionPane.showMessageDialog(mainFrame, "Data not loaded. Data was created by: "+modelClass+".", //$NON-NLS-1$ //$NON-NLS-2$
          "Incompatible data file.", JOptionPane.WARNING_MESSAGE);                    //$NON-NLS-1$
        return;
      }
      app = getOSPApp();
      app.setCompatibleModel(compatibleModel);
      xml.loadObject(getOSPApp());
      app.setCompatibleModel(false);
      if(model.getClass()==app.getLoadedModelClass()) {                               // classes match so we have a new default
        xmlDefault = xml;
        clearItem.setEnabled(true);
      } else if(app.getLoadedModelClass()!=null) {                                    // a model was imported; create xml from current model
        xmlDefault = new XMLControlElement(getOSPApp());
        clearItem.setEnabled(true);
      } else {                                                                        // models are not compatible
        xmlDefault = null;
        clearItem.setEnabled(false);
      }
    } else {
      JOptionPane.showMessageDialog(mainFrame, "Data for: "+xml.getObjectClass()+".", //$NON-NLS-1$ //$NON-NLS-2$
        "OSP Application data not found.", JOptionPane.WARNING_MESSAGE);              //$NON-NLS-1$
    }
  }

  public void loadXML() {
    JFileChooser chooser = OSPRuntime.getChooser();
    int result = chooser.showOpenDialog(null);
    if(result==JFileChooser.APPROVE_OPTION) {
      String fileName = chooser.getSelectedFile().getAbsolutePath();
      loadXML(XML.getRelativePath(fileName));
    }
  }

  public void inspectXML() {
	System.err.println("EjsControlFrame line 1021");
    // display a TreePanel in a modal dialog
    XMLControl xml = new XMLControlElement(getOSPApp());
    JDialog dialog = new JDialog((java.awt.Frame) null, true);
    XMLTreePanel treePanel = new XMLTreePanel(xml);
    dialog.setTitle("XML Inspector");
    dialog.setContentPane(treePanel);
    dialog.setSize(new Dimension(600, 300));
    dialog.setVisible(true);
  }

  public void loadXML(String[] args) {
    if(args!=null) {
      for(int i = 0; i<args.length; i++) {
        loadXML(args[i]);
      }
    }
  }

  class EjsFrame extends OSPFrame implements MainFrame {
    public OSPFrame getMainFrame() {
      return this;
    }

    public void render() {
      EjsControlFrame.this.render();
    }

    public void clearData() {
      EjsControlFrame.this.clearData();
    }

    public void clearDataAndRepaint() {
      EjsControlFrame.this.clearDataAndRepaint();
    }

    public void dispose() {
      messageFrame.setVisible(false);
      messageFrame.dispose();
      super.dispose();
    }

    /**
     * Invalidates image buffers if a drawing panel is buffered.
     */
    public void invalidateImage() {
      if(defaultDrawingPanel!=null) {
        defaultDrawingPanel.invalidateImage();
      }
    }

    public OSPApplication getOSPApp() {
      return EjsControlFrame.this.getOSPApp();
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
