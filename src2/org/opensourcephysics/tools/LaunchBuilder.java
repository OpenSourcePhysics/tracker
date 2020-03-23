/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.PrintUtils;

/**
 * This provides a GUI for building LaunchNode and LaunchSet xml files.
 *
 * @author Douglas Brown
 * @version 1.2
 */
public class LaunchBuilder extends Launcher {
  // static constants
  static final Color RED = new Color(255, 102, 102);
  // static fields
  static JFileChooser fileChooser;
  static javax.swing.filechooser.FileFilter jarFileFilter;
  static javax.swing.filechooser.FileFilter htmlFileFilter;
  static javax.swing.filechooser.FileFilter pdfFileFilter;
  static javax.swing.filechooser.FileFilter allFileFilter;
  static int maxArgs = 4; // max number of main method args
  static File ospJarFolder;
  static Color enabledColor, disabledColor;
  // instance fields
  Action newTabSetAction; // creates a new tabset with a single tab
  Action changeAction;    // loads input data into selected node
  Action newTabAction;    // creates a new root node and tab
  Action addAction;       // adds new node to selected node
  Action cutAction;       // cuts selected node to clipboard
  Action copyAction;      // copies selected node to clipboard
  Action pasteAction;     // adds clipboard node to selected node
  Action importAction;    // adds xml file node to selected node
  Action saveAsAction;    // saves selected node to a new file
  Action saveAction;      // saves selected node
  Action saveAllAction;   // saves tabset and all nodes
  Action saveSetAsAction; // saves tabset and all nodes to new files
  Action moveUpAction;    // moves selected node up
  Action moveDownAction;  // moves selected node down
  Action openJarAction;   // opens a jar file and sets node path
  Action searchJarAction; // searches the current jars for a launchable class
  Action saveJarAction;   // saves a jar file containing the current tab set
  Action openArgAction;   // opens an xml file and sets the current argument
  Action openModelArgAction; // opens an xml file and sets the current model argument
  Action openURLAction;   // opens an html file and sets url
  Action openPDFAction;   // opens a pdf file and sets url
  Action searchJarForModelAction;   // searches the current jars for a model class
  Action openTabAction;   // opens a node in a new tab
  Icon openIcon;
  JTabbedPane editorTabs;
  FocusListener focusListener;
  KeyListener keyListener;
  JPanel displayPanel;
  JPanel launchPanel;
  JPanel authorPanel;
  ArrayList<JLabel> labels;
  JTextField titleField;
  JLabel titleLabel;
  JTextField passwordEditor;
  JLabel passwordLabel;
  JTextField nameField;
  JLabel nameLabel;
  JTextField tooltipField;
  JLabel tooltipLabel;
  TitledBorder displayTitle;
  JTextField classField;
  JLabel classLabel;
  JTextField argField;
  JLabel argLabel;
  JSpinner argSpinner;
  JTextField jarField;
  JLabel jarLabel;
  JToolBar displayBar;
  JLabel displayLabel;
  SpinnerNumberModel displaySpinnerModel;
  JSpinner displaySpinner;
  JLabel pathLabel;
  JTextField pathField;
  JButton openDisplayChooserButton;
  JButton showModelArgsButton;
  JDialog modelArgsDialog;
  JTextField modelArgField;
  JLabel modelArgLabel;
  JSpinner modelArgSpinner;
  JButton modelArgCloseButton;
  JButton modelArgClearButton;
  JLabel tabTitleLabel;
  JTextField tabTitleField;
  JPanel urlPanel;
  JTextPane descriptionPane;
  JScrollPane descriptionScroller;
  TitledBorder descriptionTitle;
  JEditorPane htmlPane;
  JScrollPane htmlScroller;
  JSplitPane displaySplitPane;
  JTextField authorField;
  JLabel authorLabel;
  JTextField keywordField;
  JLabel keywordLabel;
  JTextField levelField;
  JLabel levelLabel;
  JTextField languagesField;
  JLabel languagesLabel;
  JTextPane commentPane;
  JScrollPane commentScroller;
  TitledBorder commentTitle;
  TitledBorder optionsTitle;
  TitledBorder securityTitle;
  JCheckBox editorEnabledCheckBox;
  JCheckBox encryptCheckBox;
  JCheckBox onEditCheckBox;
  JCheckBox onLoadCheckBox;
  JCheckBox hideRootCheckBox;
  JCheckBox hiddenCheckBox;
  JCheckBox buttonViewCheckBox;
  JCheckBox singleVMCheckBox;
  JCheckBox showLogCheckBox;
  JCheckBox clearLogCheckBox;
  JCheckBox singletonCheckBox;
  JCheckBox singleAppCheckBox;
  JComboBox levelDropDown;
  String previousClassPath;
  JButton newTabButton;
  JButton addButton;
  JButton cutButton;
  JButton copyButton;
  JButton pasteButton;
  JButton moveUpButton;
  JButton moveDownButton;
  JMenuItem newItem;
  JMenuItem previewItem;
  JMenuItem saveNodeItem;
  JMenuItem saveNodeAsItem;
  JMenuItem saveSetAsItem;
  JMenuItem saveAllItem;
  JMenuItem saveJarItem;
  JMenuItem importItem;
  JMenuItem openTabItem;
  JMenu toolsMenu;
  JMenuItem encryptionToolItem;
  JToolBar toolbar;
  LaunchSaver saver = new LaunchSaver(this);

  static {
//  	LaunchClassChooser.setBasePath("C:/Documents and Settings/DoBrown/My Documents/Eclipse/workspace_deploy");
//  	Launcher.setJarsOnly(false);
  	enabledColor = UIManager.getColor("Label.foreground"); //$NON-NLS-1$
  	if (enabledColor==null)
  		enabledColor = Color.BLACK;
  	disabledColor = UIManager.getColor("Label.disabledForeground"); //$NON-NLS-1$
  	if (disabledColor==null)
    	disabledColor = UIManager.getColor("Label.disabledText"); //$NON-NLS-1$
  	if (disabledColor==null)
  		disabledColor = Color.LIGHT_GRAY;
  }

  /**
   * No-arg constructor.
   */
  public LaunchBuilder() {
    OSPRuntime.setAuthorMode(true);
    XML.setLoader(NodeSet.class, new NodeSet.Loader());
  }

  /**
   * Constructs a builder and loads the specified file.
   *
   * @param fileName the file name
   */
  public LaunchBuilder(String fileName) {
    super(fileName);
    OSPRuntime.setAuthorMode(true);
    XML.setLoader(NodeSet.class, new NodeSet.Loader());
  }

  /**
   * Constructs a builder with or without a splash screen.
   *
   * @param splash true to show the splash screen
   */
  public LaunchBuilder(boolean splash) {
    super(splash);
    OSPRuntime.setAuthorMode(true);
    XML.setLoader(NodeSet.class, new NodeSet.Loader());
  }

  /**
   * Constructs a builder and loads the specified file with or without splash.
   *
   * @param fileName the file name
   * @param splash true to show the splash screen
   */
  public LaunchBuilder(String fileName, boolean splash) {
    super(fileName, splash);
    OSPRuntime.setAuthorMode(true);
    XML.setLoader(NodeSet.class, new NodeSet.Loader());
  }

  /**
   * Main entry point when used as application.
   *
   * @param args args[0] may be an xml file name
   */
  public static void main(String[] args) {
    //    java.util.Locale.setDefault(new java.util.Locale("es"));
    //    OSPLog.setLevel(ConsoleLevel.ALL);
    //    OSPRuntime.setJavaLookAndFeel(true);
    String fileName = null;
    if((args!=null)&&(args.length!=0)) {
      fileName = args[0];
    }
    LaunchBuilder builder = new LaunchBuilder(fileName);
    builder.frame.setVisible(true);
  }

  /**
   * Saves a node to the specified file.
   *
   * @param node the node
   * @param fileName the desired name of the file
   * @return the name of the saved file, or null if not saved
   */
  public String save(LaunchNode node, String fileName) {
    if(node==null) {
      return null;
    }
    if((fileName==null)||fileName.trim().equals("")) { //$NON-NLS-1$
      return saveAs(node);
    }
    // add .xml extension if none but don't require it
    if(XML.getExtension(fileName)==null) {
      while(fileName.endsWith(".")) { //$NON-NLS-1$
        fileName = fileName.substring(0, fileName.length()-1);
      }
      fileName += ".xml";             //$NON-NLS-1$
    }
    if(!saveOwnedNodes(node)) {
      return null;
    }
    String filepath = fileName;
    if(!tabSetBasePath.equals("")) { //$NON-NLS-1$
      filepath = XML.getResolvedPath(fileName, tabSetBasePath);
    } else {
      String jarBase = OSPRuntime.getLaunchJarDirectory();
      filepath = XML.getResolvedPath(fileName, jarBase);
    }
    File file = new File(filepath);
    OSPLog.fine(fileName+" = "+file.getAbsolutePath()); //$NON-NLS-1$
    String fullName = XML.forwardSlash(file.getAbsolutePath());
    String path = XML.getDirectoryPath(fullName);
    XML.createFolders(path);
    XMLControlElement control = new XMLControlElement(node);
    control.write(fullName);
    if(!control.canWrite) {
      OSPLog.info(LaunchRes.getString("Dialog.SaveFailed.Message")+" "+fullName);                        //$NON-NLS-1$//$NON-NLS-2$
      JOptionPane.showMessageDialog(null, LaunchRes.getString("Dialog.SaveFailed.Message")+" "+fileName, //$NON-NLS-1$//$NON-NLS-2$
        LaunchRes.getString("Dialog.SaveFailed.Title"), //$NON-NLS-1$
          JOptionPane.WARNING_MESSAGE);
      return null;
    }
    node.setFileName(fileName);
    changedFiles.remove(node.getFileName());
    return fileName;
  }

  /**
   * Saves a node to an xml file selected with a chooser.
   *
   * @param node the node
   * @return the name of the file
   */
  public String saveAs(LaunchNode node) {
    getXMLChooser().setFileFilter(xmlFileFilter);
    if(node.getFileName()!=null) {
      String name = XML.getResolvedPath(node.getFileName(), tabSetBasePath);
      getXMLChooser().setSelectedFile(new File(name));
    } else {
      String name = node.name;
      if(name.equals(LaunchRes.getString("NewNode.Name"))||           //$NON-NLS-1$
        name.equals(LaunchRes.getString("NewTab.Name"))) {            //$NON-NLS-1$
        name = LaunchRes.getString("NewFile.Name");                   //$NON-NLS-1$
      }
      String path = XML.getResolvedPath(name+".xml", tabSetBasePath); //$NON-NLS-1$
      getXMLChooser().setSelectedFile(new File(path));
    }
    int result = getXMLChooser().showDialog(null, LaunchRes.getString("FileChooser.SaveAs.Title")); //$NON-NLS-1$
    if(result==JFileChooser.APPROVE_OPTION) {
      File file = getXMLChooser().getSelectedFile();
      // create folder structure
      String path = XML.forwardSlash(file.getParent());
      XML.createFolders(path);
      // check to see if file already exists
      if(file.exists()) {
        // prevent duplicate file names
        String name = XML.forwardSlash(file.getAbsolutePath());
        name = XML.getPathRelativeTo(name, tabSetBasePath);
        if(getOpenPaths().contains(name)) {
          JOptionPane.showMessageDialog(frame, LaunchRes.getString("Dialog.DuplicateFileName.Message")+" \""+name+"\"", //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
            LaunchRes.getString("Dialog.DuplicateFileName.Title"),                                                 //$NON-NLS-1$
              JOptionPane.WARNING_MESSAGE);
          return null;
        }
        int selected = JOptionPane.showConfirmDialog(frame, LaunchRes.getString("Dialog.ReplaceFile.Message")+" "+ //$NON-NLS-1$//$NON-NLS-2$
          file.getName()+XML.NEW_LINE+LaunchRes.getString("Dialog.ReplaceFile.Question"), //$NON-NLS-1$
            LaunchRes.getString("Dialog.ReplaceFile.Title"),                              //$NON-NLS-1$
              JOptionPane.YES_NO_OPTION);
        if(selected!=JOptionPane.YES_OPTION) {
          return null;
        }
      }
      path = XML.forwardSlash(file.getAbsolutePath());
      String fileName = XML.getPathRelativeTo(path, tabSetBasePath);
      OSPRuntime.chooserDir = XML.getDirectoryPath(path);
      // get clones before saving
      Map<LaunchPanel, LaunchNode> clones = getClones(node);
      path = save(node, fileName);
      if(path!=null) {
        if(node.isRoot()) {
          // refresh title of root tab
          for(int i = 0; i<tabbedPane.getTabCount(); i++) {
            LaunchPanel tab = (LaunchPanel) tabbedPane.getComponentAt(i);
            if(tab.getRootNode()==node) {
              tabbedPane.setTitleAt(i, node.toString());
              break;
            }
          }
        }
        // refresh clones
        for(Iterator<LaunchPanel> it = clones.keySet().iterator(); it.hasNext(); ) {
          LaunchPanel cloneTab = it.next();
          LaunchNode clone = clones.get(cloneTab);
          clone.setFileName(node.getFileName());
          // refresh title of clone tab
          if(clone==cloneTab.getRootNode()) {
            int n = tabbedPane.indexOfComponent(cloneTab);
            tabbedPane.setTitleAt(n, node.toString());
          }
        }
        if(tabSetName!=null) {
          changedFiles.add(tabSetName);
        }
      }
      return path;
    }
    return null;
  }

  // ______________________________ protected methods _____________________________

  /**
   * Saves the owned nodes of the specified node.
   *
   * @param node the node
   * @return true unless cancelled by user
   */
  protected boolean saveOwnedNodes(LaunchNode node) {
    if(node==null) {
      return false;
    }
    if(node.isSelfContained()) {
      return true; // owned nodes saved within node
    }
    LaunchNode[] nodes = node.getChildOwnedNodes();
    for(int i = 0; i<nodes.length; i++) {
      // save owned nodes of owned node, if any
      if(nodes[i].getChildOwnedNodes().length>1) {
        if(!saveOwnedNodes(nodes[i])) {
          return false;
        }
      }
      if(save(nodes[i], nodes[i].getFileName())==null) {
        return false;
      }
    }
    return true;
  }

  /**
   * Saves a tabset to a file selected with a chooser.
   *
   * @return the absolute path of the tabset file
   */
  protected String saveTabSetAs() {
    saver.setBuilder(this);
    saver.setVisible(true);
    if(!saver.isApproved()) {
      return null;
    }
    String fileName = XML.getResolvedPath(tabSetName, tabSetBasePath);
    File file = new File(fileName);
    if(file.exists()) {
      int selected = JOptionPane.showConfirmDialog(frame, LaunchRes.getString("Dialog.ReplaceFile.Message")+" "+ //$NON-NLS-1$//$NON-NLS-2$
        file.getName()+XML.NEW_LINE+LaunchRes.getString("Dialog.ReplaceFile.Question"), //$NON-NLS-1$
          LaunchRes.getString("Dialog.ReplaceFile.Title"),                              //$NON-NLS-1$
            JOptionPane.YES_NO_OPTION);
      if(selected!=JOptionPane.YES_OPTION) {
        return null;
      }
    }
    saveState = saver.saveStateCheckBox.isSelected();
    return saveTabSet();
  }

  /**
   * Saves the current tabset.
   *
   * @return the full path to the saved file, or null if not saved
   */
  public String saveTabSet() {
    if(tabSetName==null) {
      return null;
    }
    if(tabSetName.trim().equals("")) { //$NON-NLS-1$
      return saveTabSetAs();
    }
    // check for read-only files
    if(!isTabSetWritable()) {
      return saveTabSetAs();
    }
    if(!selfContained&&!saveTabs()) {
      return null;
    }
    // save tab set
    String fileName = tabSetName;
    if(!tabSetBasePath.equals("")) { //$NON-NLS-1$
      fileName = XML.getResolvedPath(tabSetName, tabSetBasePath);
    } else {
      String jarBase = OSPRuntime.getLaunchJarDirectory();
      fileName = XML.getResolvedPath(tabSetName, jarBase);
    }
    OSPLog.fine(fileName);
    File file = new File(fileName);
    fileName = XML.forwardSlash(file.getAbsolutePath());
    String path = XML.getDirectoryPath(fileName);
    XML.createFolders(path);
    LaunchSet tabset = new LaunchSet(this, tabSetName);
    XMLControlElement control = new XMLControlElement(tabset);
    if(control.write(fileName)==null) {
      return null;
    }
    changedFiles.clear();
    jarBasePath = null; // signals tabset is now a file
    if(spawner!=null) {
      spawner.open(fileName);
      spawner.refreshGUI();
    }
    return fileName;
  }

  /**
   * Saves tabs.
   * @return true unless cancelled by user
   */
  public boolean saveTabs() {
    Component[] tabs = tabbedPane.getComponents();
    for(int i = 0; i<tabs.length; i++) {
      LaunchPanel tab = (LaunchPanel) tabs[i];
      LaunchNode root = tab.getRootNode();
      if((root.getFileName()==null)||root.getFileName().equals("")) { //$NON-NLS-1$
        continue;
      }
      save(root, XML.getResolvedPath(root.getFileName(), tabSetBasePath));
    }
    return true;
  }

  /**
   * Refreshes the selected node.
   */
  protected void refreshSelectedNode() {
    refreshNode(getSelectedNode());
  }

  /**
   * Refreshes the specified node with data from the input fields.
   *
   * @param node the node to refresh
   */
  protected void refreshNode(LaunchNode node) {
    boolean changed = false;
    if(node!=null) {
      if(node.isSingleVM()!=singleVMCheckBox.isSelected()) {
        OSPLog.finest(LaunchRes.getString("Log.Message.ChangeNodeVM"));                                          //$NON-NLS-1$
        LaunchNode parent = (LaunchNode) node.getParent();
        if((parent!=null)&&parent.isSingleVM()) {
          node.singleVM = false;
          node.singleVMOff = !singleVMCheckBox.isSelected();
        } else {
          node.singleVM = singleVMCheckBox.isSelected();
          node.singleVMOff = false;
        }
        if(node.isSingleVM()) {
          showLogCheckBox.setSelected(node.showLog);
          clearLogCheckBox.setSelected(node.clearLog);
          singleAppCheckBox.setSelected(node.isSingleApp());
        } else {
          singletonCheckBox.setSelected(node.singleton);
        }
        changed = true;
      }
      if(node.isSingleVM()&&(node.showLog!=showLogCheckBox.isSelected())) {
        OSPLog.finest(LaunchRes.getString("Log.Message.ChangeNodeShowLog"));                                     //$NON-NLS-1$
        node.showLog = showLogCheckBox.isSelected();
        changed = true;
      }
      if(node.isSingleVM()&&node.isShowLog()&&(node.clearLog!=clearLogCheckBox.isSelected())) {
        OSPLog.finest(LaunchRes.getString("Log.Message.ChangeNodeClearLog"));                                    //$NON-NLS-1$
        node.clearLog = clearLogCheckBox.isSelected();
        changed = true;
      }
      if(node.isSingleVM()&&(node.isSingleApp()!=singleAppCheckBox.isSelected())) {
        OSPLog.finest(LaunchRes.getString("Log.Message.ChangeNodeSingleApp"));                                   //$NON-NLS-1$
        LaunchNode parent = (LaunchNode) node.getParent();
        if((parent!=null)&&parent.isSingleApp()) {
          node.singleApp = false;
          node.singleAppOff = !singleAppCheckBox.isSelected();
        } else {
          node.singleApp = singleAppCheckBox.isSelected();
          node.singleAppOff = false;
        }
        changed = true;
      }
      if(node.singleton!=singletonCheckBox.isSelected()) {
        OSPLog.finest(LaunchRes.getString("Log.Message.ChangeNodeSingleton"));                                   //$NON-NLS-1$
        node.singleton = singletonCheckBox.isSelected();
        changed = true;
      }
      if(node.hiddenInLauncher!=hiddenCheckBox.isSelected()) {
        OSPLog.finest(LaunchRes.getString("Log.Message.ChangeNodeHidden"));                                      //$NON-NLS-1$
        node.hiddenInLauncher = hiddenCheckBox.isSelected();
        changed = true;
      }
      if(node.isButtonView()!=buttonViewCheckBox.isSelected()) {
        OSPLog.finest(LaunchRes.getString("Log.Message.ChangeButtonView"));                                      //$NON-NLS-1$
        node.setButtonView(buttonViewCheckBox.isSelected());
        changed = true;
      }
      if(!node.name.equals(nameField.getText())) {
        OSPLog.finest(LaunchRes.getString("Log.Message.ChangeNodeName"));                                        //$NON-NLS-1$
        node.name = nameField.getText();
        changed = true;
      }
      if(!node.tooltip.equals(tooltipField.getText())) {
        OSPLog.finest(LaunchRes.getString("Log.Message.ChangeTooltip"));                                         //$NON-NLS-1$
        node.tooltip = tooltipField.getText();
        changed = true;
      }
      if(!node.description.equals(descriptionPane.getText())) {
        OSPLog.finest(LaunchRes.getString("Log.Message.ChangeNodeDesc"));                                        //$NON-NLS-1$
        node.description = descriptionPane.getText();
        changed = true;
      }
      int n = ((Integer) argSpinner.getValue()).intValue();
      String arg = argField.getText();
      if(!arg.equals("")) {                                                                                      //$NON-NLS-1$
        node.setMinimumArgLength(n+1);
      }
      if((node.args.length>n)&&!arg.equals(node.args[n])) {
        OSPLog.finest(LaunchRes.getString("Log.Message.ChangeNodeArgs")+" "+n);                                  //$NON-NLS-1$//$NON-NLS-2$
        node.args[n] = arg;
        if(arg.equals("")) {                                                                                     //$NON-NLS-1$
          node.setMinimumArgLength(1);
        }
        changed = true;
      }
      String jarPath = jarField.getText();
      if((jarPath.equals("")&&(node.classPath!=null))||(!jarPath.equals("")&&!jarPath.equals(node.classPath))) { //$NON-NLS-1$//$NON-NLS-2$
        OSPLog.finest(LaunchRes.getString("Log.Message.ChangeNodePath"));                                          //$NON-NLS-1$
        node.setClassPath(jarPath.equals("")                                                                       //$NON-NLS-1$
                          ? null : jarPath);
        changed = true;
      }
      n = ((Integer) displaySpinner.getValue()).intValue();
      LaunchNode.DisplayTab displayTab = node.getDisplayTab(n);
      String input = pathField.getText();
      if (displayTab==null
      		||((displayTab.path!=null &&!displayTab.path.equals(input))
      		||(input!=null && !input.equals(displayTab.path)))) {
        String title = (displayTab==null) ? null : displayTab.title;
        String[] args = (displayTab==null) ? null : displayTab.getModelArgs();
        node.setDisplayTab(n, title, input, args);
        OSPLog.finest(LaunchRes.getString("Log.Message.ChangeNodeURL"));                                  //$NON-NLS-1$
        changed = true;
      }
      if (displayTab!=null 
      		&& displayTab.modelClass != null 
      		&& modelArgsDialog.isVisible()) {
        n = ((Integer) modelArgSpinner.getValue()).intValue();
        arg = modelArgField.getText();
        if(!arg.equals("")) {                                                                                      //$NON-NLS-1$
          displayTab.setMinimumModelArgLength(n+1);
        }
        String[] args = displayTab.getModelArgs();
        if(args.length>n && !arg.equals(args[n])) {
          OSPLog.finest(LaunchRes.getString("Log.Message.ChangeModelArgs")+" "+n);                                  //$NON-NLS-1$//$NON-NLS-2$
          args[n] = arg.equals("")? null: arg; //$NON-NLS-1$
        	displayTab.setModelArgs(args);
          if(arg.equals("")) {                                                                                     //$NON-NLS-1$
            displayTab.setMinimumModelArgLength(0);
          }          
          changed = true;
        }
      }
      input = tabTitleField.getText();
      if(input.equals("")) {                                                                                       //$NON-NLS-1$
        input = null;
      }
      if(displayTab!= null 
      		&& ((displayTab.title!=null && !displayTab.title.equals(input)) 
      		|| (input!=null && !input.equals(displayTab.title)))) {
        OSPLog.finest(LaunchRes.getString("Log.Message.ChangeNodeHTMLTabTitle"));                                  //$NON-NLS-1$
        node.setDisplayTab(n, input, displayTab.path, displayTab.getModelArgs());
        changed = true;
      }
      String className = classField.getText();
      if(className.equals("")) {                                                                                   //$NON-NLS-1$
        if(node.launchClassName!=null) {
          node.launchClassName = null;
          node.launchClass = null;
          node.launchModelScroller = null;
          OSPLog.finest(LaunchRes.getString("Log.Message.ChangeNodeLaunchClass"));                                 //$NON-NLS-1$
          changed = true;
        }
      } else if(!className.equals(node.launchClassName)||(!className.equals("")&&(node.getLaunchClass()==null))) { //$NON-NLS-1$
        boolean change = node.setLaunchClass(className);
        if(change) {
          OSPLog.finest(LaunchRes.getString("Log.Message.ChangeNodeLaunchClass"));             //$NON-NLS-1$
          changed = true;
        }
      }
      if(!node.getAuthor().equals(authorField.getText())) {
        OSPLog.finest(LaunchRes.getString("Log.Message.ChangeNodeAuthor"));                    //$NON-NLS-1$
        node.author = authorField.getText();
        changed = true;
      }
      if(!node.keywords.equals(keywordField.getText())) {
        OSPLog.finest(LaunchRes.getString("Log.Message.ChangeNodeKeywords"));                  //$NON-NLS-1$
        node.keywords = keywordField.getText();
        changed = true;
      }
      if(!node.level.equals(levelField.getText())) {
        OSPLog.finest(LaunchRes.getString("Log.Message.ChangeNodeLevel"));                     //$NON-NLS-1$
        node.level = levelField.getText();
        changed = true;
      }
      if(!node.languages.equals(languagesField.getText())) {
        OSPLog.finest(LaunchRes.getString("Log.Message.ChangeNodeLanguages"));                 //$NON-NLS-1$
        node.languages = languagesField.getText();
        changed = true;
      }
      if(!node.comment.equals(commentPane.getText())) {
        OSPLog.finest(LaunchRes.getString("Log.Message.ChangeNodeComment"));                   //$NON-NLS-1$
        node.comment = commentPane.getText();
        changed = true;
      }
      LaunchNode root = (LaunchNode) node.getRoot();
      if(root!=null) {
        boolean hide = hideRootCheckBox.isSelected();
        if(hide!=root.hiddenWhenRoot) {
          root.hiddenWhenRoot = hide;
          OSPLog.finest(LaunchRes.getString("Log.Message.ChangeNodeRootHidden"));              //$NON-NLS-1$
          changed = true;
        }
        boolean edit = editorEnabledCheckBox.isSelected();
        if(edit!=editorEnabled) {
          editorEnabled = edit;
          OSPLog.finest(LaunchRes.getString("Log.Message.ChangeNodeEditorEnabled"));           //$NON-NLS-1$
          if(tabSetName!=null) {
            changedFiles.add(tabSetName);
          }
          refreshGUI();
        }
      }
      if(changed) {
        OSPLog.fine(LaunchRes.getString("Log.Message.ChangeNode")+" \""+node.toString()+"\""); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
        LaunchPanel tab = getSelectedTab();
        if(tab!=null) {
          tab.treeModel.nodeChanged(node);
        }
        if(node.getOwner()!=null) {
          changedFiles.add(node.getOwner().getFileName());
        } else {
          changedFiles.add(tabSetName);
        }
        refreshClones(node);
        refreshGUI();
      }
    }
  }

  /**
   * Creates a LaunchPanel with the specified root and adds it to a new tab.
   *
   * @param root the root node
   * @return true if tab successfully added
   */
  public boolean addTab(LaunchNode root) {
    if(root==null) {
      return false;
    }
    OSPLog.finest(root.toString());
    boolean added = super.addTab(root);
    if(added) {
      if(tabSetName==null) {
        tabSetName = LaunchRes.getString("Tabset.Name.New"); //$NON-NLS-1$
      }
      changedFiles.add(tabSetName);
      refreshGUI();
    }
    return added;
  }

  /**
   * Removes the selected tab. Overrides Launcher method.
   *
   * @return true if the tab was removed
   */
  public boolean removeSelectedTab() {
    // close the tabset if only one tab is open
    if(tabbedPane.getTabCount()==1) {
      String[] prevArgs = undoManager.getLauncherState();
      boolean removed = removeAllTabs();
      if(removed&&(prevArgs!=null)) {
        // null new args indicate not redoable
        LauncherUndo.LoadEdit edit = undoManager.new LoadEdit(null, prevArgs);
        undoSupport.postEdit(edit);
      }
      return removed;
    }
    // check for unsaved changes in the selected tab
    LaunchPanel tab = (LaunchPanel) tabbedPane.getSelectedComponent();
    if(tab!=null) {
      if(!saveChanges(tab)) {
        return false;
      }
    }
    boolean removed = super.removeSelectedTab();
    if((tabSetName!=null)&&removed) {
      changedFiles.add(tabSetName);
      refreshGUI();
    }
    return removed;
  }

  /**
   * Offers to save changes, if any, to the specified tab.
   *
   * @param tab the tab
   * @return true unless cancelled by user
   */
  protected boolean saveChanges(LaunchPanel tab) {
    LaunchNode root = tab.getRootNode();
    int n = tabbedPane.indexOfComponent(tab);
    String name = (n>-1) ? tabbedPane.getTitleAt(n) : getDisplayName(root.getFileName());
    boolean changed = changedFiles.contains(root.getFileName());
    LaunchNode[] nodes = root.getAllOwnedNodes();
    for(int i = 0; i<nodes.length; i++) {
      changed = changed||changedFiles.contains(nodes[i].getFileName());
    }
    if(changed) {
      int selected = JOptionPane.showConfirmDialog(frame, LaunchRes.getString("Dialog.SaveChanges.Tab.Message")+" \""+ //$NON-NLS-1$//$NON-NLS-2$
        name+"\""+XML.NEW_LINE+                               //$NON-NLS-1$
          LaunchRes.getString("Dialog.SaveChanges.Question"), //$NON-NLS-1$
            LaunchRes.getString("Dialog.SaveChanges.Title"),  //$NON-NLS-1$
              JOptionPane.YES_NO_CANCEL_OPTION);
      if(selected==JOptionPane.CANCEL_OPTION) {
        return false;
      }
      if(selected==JOptionPane.YES_OPTION) {
        // save root and all owned nodes
        save(root, root.getFileName());
      }
    }
    return true;
  }

  /**
   * Removes all tabs and closes the tabset.
   *
   * @return true if all tabs were removed
   */
  protected boolean removeAllTabs() {
    if(!saveAllChanges()) {
      return false;
    }
    return super.removeAllTabs();
  }

  /**
   * Offers to save all changes, if any.
   *
   * @return true unless cancelled by user
   */
  protected boolean saveAllChanges() {
    // save changes to tab set
    if(!changedFiles.isEmpty()&&(tabbedPane.getTabCount()>0)) {
      String message = LaunchRes.getString("Dialog.SaveChanges.Tabset.Message"); //$NON-NLS-1$
      int selected = JOptionPane.showConfirmDialog(frame, message+"\""+          //$NON-NLS-1$
        tabSetName+"\""+XML.NEW_LINE+                                            //$NON-NLS-1$
          LaunchRes.getString("Dialog.SaveChanges.Question"),                    //$NON-NLS-1$
            LaunchRes.getString("Dialog.SaveChanges.Title"),                     //$NON-NLS-1$
              JOptionPane.YES_NO_CANCEL_OPTION);
      if(selected==JOptionPane.CANCEL_OPTION) {
        return false;
      }
      if(selected==JOptionPane.YES_OPTION) {
        if(tabSetName.equals(LaunchRes.getString("Tabset.Name.New"))||           //$NON-NLS-1$
                  !saveAllItem.isEnabled()) {
          saveTabSetAs();
        } else {
          saveTabSet();
        }
      }
    }
    return true;
  }

  /**
   * Refreshes string resources.
   */
  protected void refreshStringResources() {
    super.refreshStringResources();
    saver = new LaunchSaver(this);
    editorTabs.setTitleAt(0, LaunchRes.getString("Tab.Display"));                 //$NON-NLS-1$
    editorTabs.setTitleAt(1, LaunchRes.getString("Tab.Launch"));                  //$NON-NLS-1$
    editorTabs.setTitleAt(2, LaunchRes.getString("Tab.Author"));                  //$NON-NLS-1$
    displayTitle.setTitle(LaunchRes.getString("Label.DisplayPane"));              //$NON-NLS-1$
    commentTitle.setTitle(LaunchRes.getString("Label.Comments"));                 //$NON-NLS-1$
    descriptionTitle.setTitle(LaunchRes.getString("Label.Description"));          //$NON-NLS-1$
    optionsTitle.setTitle(LaunchRes.getString("Label.Options"));                  //$NON-NLS-1$
    hiddenCheckBox.setText(LaunchRes.getString("Checkbox.Hidden"));               //$NON-NLS-1$
    buttonViewCheckBox.setText(LaunchRes.getString("Checkbox.ButtonView"));       //$NON-NLS-1$
    nameLabel.setText(LaunchRes.getString("Label.Name"));                         //$NON-NLS-1$
    tooltipLabel.setText(LaunchRes.getString("Label.Tooltip"));                   //$NON-NLS-1$
    displayLabel.setText(LaunchRes.getString("Label.Display"));                   //$NON-NLS-1$
    tabTitleLabel.setText(LaunchRes.getString("Label.TabTitle"));         				//$NON-NLS-1$
    pathLabel.setText(LaunchRes.getString("Label.Path"));                          //$NON-NLS-1$
    jarLabel.setText(LaunchRes.getString("Label.Jar"));                           //$NON-NLS-1$
    classLabel.setText(LaunchRes.getString("Label.Class"));                       //$NON-NLS-1$
    argLabel.setText(LaunchRes.getString("Label.Args"));                          //$NON-NLS-1$
    singleVMCheckBox.setText(LaunchRes.getString("Checkbox.SingleVM"));           //$NON-NLS-1$
    showLogCheckBox.setText(LaunchRes.getString("Checkbox.ShowLog"));             //$NON-NLS-1$
    clearLogCheckBox.setText(LaunchRes.getString("Checkbox.ClearLog"));           //$NON-NLS-1$
    singletonCheckBox.setText(LaunchRes.getString("Checkbox.Singleton"));         //$NON-NLS-1$
    singleAppCheckBox.setText(LaunchRes.getString("Checkbox.SingleApp"));         //$NON-NLS-1$
    authorLabel.setText(LaunchRes.getString("Label.Author"));                     //$NON-NLS-1$
    keywordLabel.setText(LaunchRes.getString("Label.Keywords"));                  //$NON-NLS-1$
    levelLabel.setText(LaunchRes.getString("Label.Level"));                       //$NON-NLS-1$
    languagesLabel.setText(LaunchRes.getString("Label.Languages"));               //$NON-NLS-1$
    securityTitle.setTitle(LaunchRes.getString("Label.Security"));                //$NON-NLS-1$
    editorEnabledCheckBox.setText(LaunchRes.getString("Checkbox.EditorEnabled")); //$NON-NLS-1$
    encryptCheckBox.setText(LaunchRes.getString("Checkbox.Encrypted"));           //$NON-NLS-1$
    passwordLabel.setText(LaunchRes.getString("Label.Password"));                 //$NON-NLS-1$
    onLoadCheckBox.setText(LaunchRes.getString("Checkbox.PWLoad"));               //$NON-NLS-1$
    titleLabel.setText(LaunchRes.getString("Label.Title"));                       //$NON-NLS-1$
    hideRootCheckBox.setText(LaunchRes.getString("Checkbox.HideRoot"));           //$NON-NLS-1$
    previewItem.setText(LaunchRes.getString("Menu.File.Preview"));                //$NON-NLS-1$
    encryptionToolItem.setText(LaunchRes.getString("MenuItem.EncryptionTool"));   //$NON-NLS-1$
    newItem.setText(LaunchRes.getString("Menu.File.New"));                        //$NON-NLS-1$
    importItem.setText(LaunchRes.getString("Action.Import"));                     //$NON-NLS-1$
    saveNodeItem.setText(LaunchRes.getString("Action.SaveNode"));                 //$NON-NLS-1$
    saveNodeAsItem.setText(LaunchRes.getString("Action.SaveNodeAs"));             //$NON-NLS-1$
    saveAllItem.setText(LaunchRes.getString("Action.SaveAll"));                   //$NON-NLS-1$
    openTabItem.setText(LaunchRes.getString("Action.OpenTab"));                   //$NON-NLS-1$
    saveSetAsItem.setText(LaunchRes.getString("Action.SaveSetAs"));               //$NON-NLS-1$
    toolsMenu.setText(LaunchRes.getString("Menu.Tools"));                         //$NON-NLS-1$
    newTabButton.setText(LaunchRes.getString("Action.New"));                      //$NON-NLS-1$
    addButton.setText(LaunchRes.getString("Action.Add"));                         //$NON-NLS-1$
    cutButton.setText(LaunchRes.getString("Action.Cut"));                         //$NON-NLS-1$
    copyButton.setText(LaunchRes.getString("Action.Copy"));                       //$NON-NLS-1$
    pasteButton.setText(LaunchRes.getString("Action.Paste"));                     //$NON-NLS-1$
    moveUpButton.setText(LaunchRes.getString("Action.Up"));                       //$NON-NLS-1$
    moveDownButton.setText(LaunchRes.getString("Action.Down"));                   //$NON-NLS-1$
    showModelArgsButton.setText(LaunchRes.getString("Button.ModelArgs"));         //$NON-NLS-1$
    modelArgsDialog.setTitle(LaunchRes.getString("Dialog.ModelArgs.Title"));      //$NON-NLS-1$
    modelArgCloseButton.setText(LaunchRes.getString("Dialog.Button.Close"));      //$NON-NLS-1$
    modelArgClearButton.setText(LaunchRes.getString("Dialog.Button.Clear"));      //$NON-NLS-1$
    modelArgLabel.setText(LaunchRes.getString("Label.Args"));                     //$NON-NLS-1$
    // set tool tips
    pathField.setToolTipText(LaunchRes.getString("Display.Path.Tooltip"));         //$NON-NLS-1$
    tabTitleField.setToolTipText(LaunchRes.getString("Display.Tab.Title.Tooltip")); //$NON-NLS-1$
    displaySpinner.setToolTipText(LaunchRes.getString("Display.Tab.Number.Tooltip"));//$NON-NLS-1$
    openDisplayChooserButton.setToolTipText(LaunchRes.getString("Button.OpenDisplay.Tooltip")); //$NON-NLS-1$
    showModelArgsButton.setToolTipText(LaunchRes.getString("Button.ModelArgs.Tooltip")); //$NON-NLS-1$
    // reset label sizes
    labels.clear();
    labels.add(nameLabel);
    labels.add(tooltipLabel);
    labels.add(displayLabel);
    // set label sizes
    FontRenderContext frc = new FontRenderContext(null, false, false); 
    Font font = nameLabel.getFont();
    //display panel labels
    int w = 0;
    for(Iterator<JLabel> it = labels.iterator(); it.hasNext(); ) {
      JLabel next = it.next();
      Rectangle2D rect = font.getStringBounds(next.getText()+" ", frc); //$NON-NLS-1$
      w = Math.max(w, (int) rect.getWidth()+1);
    }
    Dimension labelSize = new Dimension(w, 20);
    for(Iterator<JLabel> it = labels.iterator(); it.hasNext(); ) {
      JLabel next = it.next();
      next.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
      next.setPreferredSize(labelSize);
      next.setHorizontalAlignment(SwingConstants.TRAILING);
    }
    // launch panel labels
    labels.clear();
    labels.add(jarLabel);
    labels.add(classLabel);
    labels.add(argLabel);
    w = 0;
    for(Iterator<JLabel> it = labels.iterator(); it.hasNext(); ) {
      JLabel next = it.next();
      Rectangle2D rect = font.getStringBounds(next.getText()+" ", frc); //$NON-NLS-1$
      w = Math.max(w, (int) rect.getWidth()+1);
    }
    labelSize = new Dimension(w, 20);
    for(Iterator<JLabel> it = labels.iterator(); it.hasNext(); ) {
      JLabel next = it.next();
      next.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
      next.setPreferredSize(labelSize);
      next.setHorizontalAlignment(SwingConstants.TRAILING);
    }
    labels.clear();
    // author panel labels
    labels.add(authorLabel);
    labels.add(keywordLabel);
    labels.add(levelLabel);
    labels.add(languagesLabel);
    w = 0;
    for(Iterator<JLabel> it = labels.iterator(); it.hasNext(); ) {
      JLabel next = it.next();
      Rectangle2D rect = font.getStringBounds(next.getText()+" ", frc); //$NON-NLS-1$
      w = Math.max(w, (int) rect.getWidth()+1);
    }
    labelSize = new Dimension(w, 20);
    for(Iterator<JLabel> it = labels.iterator(); it.hasNext(); ) {
      JLabel next = it.next();
      next.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
      next.setPreferredSize(labelSize);
      next.setHorizontalAlignment(SwingConstants.TRAILING);
    }
  }

  /**
   * Refreshes the GUI.
   */
  protected void refreshGUI() {
    if(previousNode!=null) { // new tab has been selected
      LaunchNode prev = previousNode;
      previousNode = null;
      refreshNode(prev);
    }
    if(newNodeSelected) {
      argSpinner.setValue(new Integer(0));
      displaySpinner.setValue(new Integer(0));
      newNodeSelected = false;
    }
    LaunchNode root = getRootNode();
    boolean rootEnabled = root==null || root.enabled;
    if (!rootEnabled)
    	editorTabs.setSelectedIndex(0);
    titleField.setText(title);
    titleField.setBackground(Color.white);
    super.refreshGUI();
    // refresh frame title
    String theTitle = frame.getTitle();
    if(this.title!=null) {
      if(!changedFiles.isEmpty()) {
        theTitle += " ["+tabSetName+"*]"; //$NON-NLS-1$//$NON-NLS-2$
      } else {
        theTitle += " ["+tabSetName+"]";  //$NON-NLS-1$//$NON-NLS-2$
      }
    } else if(!changedFiles.isEmpty()) {
      if(tabbedPane.getTabCount()==0) {
        changedFiles.clear();
      } else {
        theTitle += "*";                  //$NON-NLS-1$
      }
    }
    frame.setTitle(theTitle);
    // refresh selected node items
    final LaunchNode node = getSelectedNode();
    if(node!=null) {
      // refresh tab titles
      for(int i = 0; i<tabbedPane.getTabCount(); i++) {
        root = getTab(i).getRootNode();
        tabbedPane.setTitleAt(i, root.toString());
      }
      // refresh display tab
      hiddenCheckBox.setSelected(node.isHiddenInLauncher());
      boolean parentHidden = (node.getParent()!=null)&&((LaunchNode) node.getParent()).isHiddenInLauncher();
      hiddenCheckBox.setEnabled(!parentHidden && rootEnabled);
      nameField.setText(node.toString());
      nameField.setBackground(Color.white);
      tooltipField.setText(node.tooltip);
      tooltipField.setBackground(Color.white);
      descriptionPane.setText(node.description);
      descriptionPane.setBackground(Color.white);
      int n = ((Integer) displaySpinner.getValue()).intValue();
      LaunchNode.DisplayTab displayTab = node.getDisplayTab(n);
      String urlPath = (displayTab!=null)? displayTab.path: null;
      boolean badURL = (urlPath!=null && displayTab.url==null && displayTab.modelClass==null);
      pathField.setText(urlPath);
      pathField.setBackground(badURL ? RED : Color.white);
      displaySpinnerModel.setMaximum(new Integer(node.getDisplayTabCount()));
      displaySpinner.setVisible(node.getDisplayTab(0)!=null);
      boolean hasHTML = displayTab!=null && displayTab.path!=null && !displayTab.path.toLowerCase().endsWith("pdf"); //$NON-NLS-1$
      tabTitleLabel.setVisible(hasHTML);
      tabTitleField.setVisible(hasHTML);
      tabTitleField.setText((displayTab!=null) ? displayTab.title : null);
      tabTitleField.setBackground(Color.white);
      displayBar.remove(showModelArgsButton);
      if(displayTab!=null 
      		&& ((displayTab.url!=null && !displayTab.url.getPath().toLowerCase().endsWith("pdf")) //$NON-NLS-1$
      		|| displayTab.getModelScroller()!=null)) {
	      if(displayTab.url!=null) {
	      	displaySplitPane.setTopComponent(htmlScroller);
	      	if (htmlPane.getPage()!=displayTab.url) {
		        try {
		          if(displayTab.url.getContent()!=null) {
		            final java.net.URL url = displayTab.url;
		            Runnable runner = new Runnable() {
		              public void run() {
		                try {
		                  htmlPane.setPage(url);
		                } catch(IOException ex) {
		                  OSPLog.fine(LaunchRes.getString("Log.Message.BadURL")+" "+url); //$NON-NLS-1$//$NON-NLS-2$
		                }
		              }
		
		            };
		            SwingUtilities.invokeLater(runner);
		          }
		        } catch(IOException ex) {
		          htmlPane.setText(null);
		        }
	      	}
	      }
	      else if (displayTab.getModelScroller()!= null) {
	      	JScrollPane scroller = displayTab.getModelScroller();
	      	scroller.setBorder(displayTitle);
          displaySplitPane.setTopComponent(scroller);
          // add modelArgsButton at end
          displayBar.add(showModelArgsButton);
          n = ((Integer) modelArgSpinner.getValue()).intValue();
          if(displayTab.modelArgs.length>n) {
            modelArgField.setText(displayTab.modelArgs[n]);
          } else {
          	modelArgField.setText(""); //$NON-NLS-1$
          }
          boolean xmlArg = modelArgField.getText().endsWith(".xml"); //$NON-NLS-1$
          Resource res = null;
          if(xmlArg) {
            res = ResourceLoader.getResource(modelArgField.getText());
            modelArgField.setBackground((res==null) ? RED : Color.white);
          } else {
          	modelArgField.setBackground(Color.white);
          }
	      }
      }
      else {
      	if (n == 0 && node.getLaunchModelScroller() != null) {
        	JScrollPane scroller = node.getLaunchModelScroller();
      		scroller.setBorder(displayTitle);
          displaySplitPane.setTopComponent(scroller);
        } 
      	else {
          displaySplitPane.setTopComponent(htmlScroller);
          htmlPane.setContentType(LaunchPanel.TEXT_TYPE);
          htmlPane.setText(null);
	      }
      }
      displayBar.validate();
      // refresh launch node
      // check the path and update the class chooser
      String path = node.getClassPath(); // in node-to-root order
      if(!path.equals(previousClassPath)) {
        boolean success = getClassChooser().setPath(path);
        searchJarAction.setEnabled(success);
        searchJarForModelAction.setEnabled(success);
      }
      // store path for later comparison
      previousClassPath = node.getClassPath();
      jarField.setText(node.classPath);
      jarField.setBackground(((node.classPath!=null)&&!getClassChooser().isLoaded(node.classPath)) ? RED : Color.white);
      classField.setText(node.launchClassName);
      classField.setBackground(((node.getLaunchClass()==null)&&(node.launchClassName!=null)) ? RED : Color.white);
      n = ((Integer) argSpinner.getValue()).intValue();
      if(node.args.length>n) {
        argField.setText(node.args[n]);
      } else {
        argField.setText(""); //$NON-NLS-1$
      }
      boolean xmlArg = argField.getText().endsWith(".xml"); //$NON-NLS-1$
      Resource res = null;
      if(xmlArg) {
        res = ResourceLoader.getResource(argField.getText());
        argField.setBackground((res==null) ? RED : Color.white);
      } else {
        argField.setBackground(Color.white);
      }
      // set enabled and selected states of checkboxes
      LaunchNode parent = (LaunchNode) node.getParent(); // may be null
      singletonCheckBox.setEnabled((parent==null)||!parent.isSingleton());
      singletonCheckBox.setSelected(node.isSingleton());
      singleVMCheckBox.setSelected(node.isSingleVM());
      if(node.isSingleVM()) {
        showLogCheckBox.setEnabled((parent==null)||!parent.isShowLog());
        showLogCheckBox.setSelected(node.isShowLog());
        clearLogCheckBox.setEnabled((parent==null)||!parent.isClearLog());
        clearLogCheckBox.setSelected(node.isClearLog());
        singleAppCheckBox.setEnabled(true);
        singleAppCheckBox.setSelected(node.isSingleApp());
      } else {
        showLogCheckBox.setEnabled(false);
        showLogCheckBox.setSelected(false);
        clearLogCheckBox.setEnabled(false);
        clearLogCheckBox.setSelected(false);
        singleAppCheckBox.setEnabled(false);
        singleAppCheckBox.setSelected(false);
      }
      levelDropDown.setVisible(node.isShowLog());
      clearLogCheckBox.setVisible(node.isShowLog());
      // refresh the level dropdown if visible
      if(levelDropDown.isVisible()) {
        boolean useAll = ((parent==null)||!parent.isShowLog());
        // disable during refresh to prevent triggering events
        levelDropDown.setEnabled(false);
        levelDropDown.removeAllItems();
        for(int i = 0; i<OSPLog.levels.length; i++) {
          if(useAll||(OSPLog.levels[i].intValue()<=parent.getLogLevel().intValue())) {
            levelDropDown.addItem(OSPLog.levels[i]);
          }
        }
        levelDropDown.setSelectedItem(node.getLogLevel());
        levelDropDown.setEnabled(true);
      }
      // refresh author tab
      authorField.setText(node.getAuthor());
      authorField.setBackground(Color.white);
      keywordField.setText(node.keywords);
      keywordField.setBackground(Color.white);
      levelField.setText(node.level);
      levelField.setBackground(Color.white);
      languagesField.setText(node.languages);
      languagesField.setBackground(Color.white);
      commentPane.setText(node.comment);
      commentPane.setBackground(Color.white);
      boolean hasPW = (password!=null)&&!password.equals(""); //$NON-NLS-1$
      onLoadCheckBox.setEnabled(hasPW);
      onLoadCheckBox.setSelected(hasPW&&pwRequiredToLoad);
      encryptCheckBox.setSelected(password!=null);
      passwordEditor.setEnabled(encryptCheckBox.isSelected());
      passwordLabel.setEnabled(encryptCheckBox.isSelected());
      passwordEditor.setText(password);
      passwordEditor.setBackground(Color.white);
    }
    // rebuild file menu
    fileMenu.removeAll();
    fileMenu.add(newItem);
    if(undoManager.canReload()) {
      fileMenu.add(backItem);
    }
    fileMenu.addSeparator();
    fileMenu.add(openItem);
    if(openFromJarMenu!=null) {
      fileMenu.add(openFromJarMenu);
    }
    LaunchPanel tab = getSelectedTab();
    if (tab!=null && !tab.getRootNode().enabled) {
      fileMenu.add(passwordItem);
    }
    boolean isZipped = (jarBasePath!=null)&&!jarBasePath.equals(""); //$NON-NLS-1$
    saveAllItem.setEnabled(!isZipped&&isTabSetWritable());
    if(tab!=null) {
    	if (rootEnabled) fileMenu.add(importItem);
      fileMenu.addSeparator();
      if (rootEnabled) fileMenu.add(closeTabItem);
      fileMenu.add(closeAllItem);
      fileMenu.addSeparator();
      fileMenu.add(previewItem);
      if (rootEnabled) {
	      fileMenu.addSeparator();
	      fileMenu.add(saveAllItem);
	      fileMenu.add(saveSetAsItem);
	      if(OSPRuntime.getLaunchJarName()!=null) {
	        fileMenu.addSeparator();
	        fileMenu.add(saveJarItem);
	      }
      }
      // added by W. Christian to capture screen
      JMenu printMenu = new JMenu(DisplayRes.getString("DrawingFrame.Print_menu_title"));                          //$NON-NLS-1$
      JMenuItem printFrameItem = new JMenuItem(DisplayRes.getString("DrawingFrame.PrintFrame_menu_item"));         //$NON-NLS-1$
      JMenuItem saveFrameAsEPSItem = new JMenuItem(DisplayRes.getString("DrawingFrame.SaveFrameAsEPS_menu_item")); //$NON-NLS-1$
      printMenu.add(printFrameItem);
      printMenu.add(saveFrameAsEPSItem);
      fileMenu.add(printMenu);
      // print action
      printFrameItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          PrintUtils.printComponent(frame);
        }

      });
      // save as EPS action
      saveFrameAsEPSItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          try {
            PrintUtils.saveComponentAsEPS(frame);
          } catch(IOException ex) {
            /** empty block */
          }
        }

      });
      // end W. Christian code
      // set tab properties
      frame.getContentPane().add(toolbar, BorderLayout.NORTH);
      tab.dataPanel.add(editorTabs, BorderLayout.CENTER);
      // hidden root
      if(getRootNode().getChildCount()==0) {
        getRootNode().hiddenWhenRoot = false;
        hideRootCheckBox.setEnabled(false);
      } else {
        hideRootCheckBox.setEnabled((node==null)||!node.isButtonView());
      }
      if (!rootEnabled) 
      	hideRootCheckBox.setEnabled(false);
      boolean rootVisible = !getRootNode().hiddenWhenRoot;
      hideRootCheckBox.setSelected(!rootVisible);
      tab.tree.setRootVisible(rootVisible);
      if((getSelectedNode()==null)&&!rootVisible) {
        tab.setSelectedNode((LaunchNode) getRootNode().getChildAt(0));
      }
      // button view
      buttonViewCheckBox.setSelected((node!=null)&&node.isButtonView());
      buttonViewCheckBox.setEnabled(rootVisible && rootEnabled);
      // editor enabled
      editorEnabledCheckBox.setSelected(editorEnabled);
      // rootEnabled effects
      editorTabs.setEnabled(rootEnabled);
      nameField.setEnabled(rootEnabled);
      tooltipField.setEnabled(rootEnabled);
      descriptionPane.setEnabled(rootEnabled);
      pathField.setEnabled(rootEnabled);
      titleField.setEnabled(rootEnabled);
      displaySpinner.setEnabled(rootEnabled);
      openDisplayChooserButton.setEnabled(rootEnabled);
      titleLabel.setEnabled(rootEnabled);
      nameLabel.setEnabled(rootEnabled);
      tooltipLabel.setEnabled(rootEnabled);
      displayLabel.setEnabled(rootEnabled);
      pathLabel.setEnabled(rootEnabled);
      newTabButton.setEnabled(rootEnabled);
      addButton.setEnabled(rootEnabled);
      cutButton.setEnabled(rootEnabled);
      copyButton.setEnabled(rootEnabled);
      pasteButton.setEnabled(rootEnabled);
      moveUpButton.setEnabled(rootEnabled);
      moveDownButton.setEnabled(rootEnabled);
      tabTitleField.setEnabled(rootEnabled);
    	htmlScroller.setEnabled(rootEnabled);
      showModelArgsButton.setEnabled(rootEnabled);
      displayTitle.setTitleColor(rootEnabled? enabledColor: disabledColor);
      descriptionTitle.setTitleColor(rootEnabled? enabledColor: disabledColor);
    } else { // no tab
      frame.getContentPane().remove(toolbar);
    }
    if(exitItem!=null) {
      fileMenu.addSeparator();
      fileMenu.add(exitItem);
    }
    // update tabbed pane tool tips and icons
    for(int k = 0; k<tabbedPane.getTabCount(); k++) {
      root = getTab(k).getRootNode();
      if(root.getFileName()!=null) {
        tabbedPane.setIconAt(k, getFileIcon(root));
        tabbedPane.setToolTipTextAt(k, LaunchRes.getString("ToolTip.FileName")+" \""+root.getFileName()+"\""); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
      } else {
        tabbedPane.setIconAt(k, null);
        tabbedPane.setToolTipTextAt(k, null);
      }
    }
  }

  /**
   * Creates the GUI.
   *
   * @param splash true to show the splash screen
   */
  protected void createGUI(boolean splash) {
    wInit = 600;
    hInit = 540;
    labels = new ArrayList<JLabel>();
    super.createGUI(splash);
    // add listeners to refresh GUI
    frame.addWindowListener(new WindowAdapter() {
      // Added by W. Christian to disable the Language menu
      public void windowGainedFocus(WindowEvent e) {
        OSPRuntime.setAuthorMode(true);
      }
      public void windowActivated(WindowEvent e) {
        OSPRuntime.setAuthorMode(true);
      }
      public void windowOpened(WindowEvent e) {
        if(getSelectedNode()!=null) {
          // clear htmlPane to force a full refresh
          htmlPane.setContentType(LaunchPanel.TEXT_TYPE);
          htmlPane.setText(null);
        }
        refreshGUI();
      }

    });
    tabbedPane.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        refreshGUI();
      }

    });
    // create additional file and folder icons
    String imageFile = "/org/opensourcephysics/resources/tools/images/whitefile.gif"; //$NON-NLS-1$
    whiteFileIcon = loadIcon(imageFile);
    imageFile = "/org/opensourcephysics/resources/tools/images/ghostfile.gif";    //$NON-NLS-1$
    ghostFileIcon = loadIcon(imageFile);
    imageFile = "/org/opensourcephysics/resources/tools/images/redfile.gif";      //$NON-NLS-1$
    redFileIcon = loadIcon(imageFile);
    imageFile = "/org/opensourcephysics/resources/tools/images/yellowfile.gif";   //$NON-NLS-1$
    yellowFileIcon = loadIcon(imageFile);
    imageFile = "/org/opensourcephysics/resources/tools/images/redfolder.gif";    //$NON-NLS-1$
    redFolderIcon = loadIcon(imageFile);
    imageFile = "/org/opensourcephysics/resources/tools/images/greenfolder.gif";  //$NON-NLS-1$
    greenFolderIcon = loadIcon(imageFile);
    imageFile = "/org/opensourcephysics/resources/tools/images/yellowfolder.gif"; //$NON-NLS-1$
    yellowFolderIcon = loadIcon(imageFile);
    // create actions
    createActions();
    // create fields
    titleField = new JTextField();
    titleField.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if(e.getKeyCode()==KeyEvent.VK_ENTER) {
          String text = titleField.getText();
          if(text.equals("")) { //$NON-NLS-1$
            text = null;
          }
          if(text!=title) {
            changedFiles.add(tabSetName);
          }
          title = text;
          refreshGUI();
        } else {
          titleField.setBackground(Color.yellow);
        }
      }

    });
    titleField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        String text = titleField.getText();
        if(text.equals("")) { //$NON-NLS-1$
          text = null;
        }
        if(text!=title) {
          changedFiles.add(tabSetName);
        }
        title = text;
        refreshGUI();
      }

    });
    nameField = new JTextField();
    nameField.addKeyListener(keyListener);
    nameField.addFocusListener(focusListener);
    tooltipField = new JTextField();
    tooltipField.addKeyListener(keyListener);
    tooltipField.addFocusListener(focusListener);
    displayTitle = BorderFactory.createTitledBorder(LaunchRes.getString("Label.DisplayPane")); //$NON-NLS-1$
    classField = new JTextField();
    classField.addKeyListener(keyListener);
    classField.addFocusListener(focusListener);
    argField = new JTextField();
    argField.addKeyListener(keyListener);
    argField.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if(OSPRuntime.isPopupTrigger(e)) {
          XMLControl control = new XMLControlElement();
          if(control.read(argField.getText())!=null) {
            JPopupMenu popup = new JPopupMenu();
            JMenuItem item = new JMenuItem(LaunchRes.getString("MenuItem.EncryptionTool")); //$NON-NLS-1$
            item.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                EncryptionTool tool = EncryptionTool.getTool();
                tool.open(argField.getText());
                tool.setVisible(true);
              }

            });
            popup.add(item);
            popup.show(argField, e.getX(), e.getY()+8);
          }
        }
      }

    });
    argField.addFocusListener(focusListener);
    modelArgField = new JTextField();
    modelArgField.addKeyListener(keyListener);
    modelArgField.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if(OSPRuntime.isPopupTrigger(e)) {
          XMLControl control = new XMLControlElement();
          if(control.read(modelArgField.getText())!=null) {
            JPopupMenu popup = new JPopupMenu();
            JMenuItem item = new JMenuItem(LaunchRes.getString("MenuItem.EncryptionTool")); //$NON-NLS-1$
            item.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                EncryptionTool tool = EncryptionTool.getTool();
                tool.open(modelArgField.getText());
                tool.setVisible(true);
              }

            });
            popup.add(item);
            popup.show(modelArgField, e.getX(), e.getY()+8);
          }
        }
      }

    });
    modelArgField.addFocusListener(focusListener);
    jarField = new JTextField();
    jarField.addKeyListener(keyListener);
    jarField.addFocusListener(focusListener);
    pathField = new JTextField();
    pathField.addKeyListener(keyListener);
    pathField.addFocusListener(focusListener);
    tabTitleField = new JTextField();
    tabTitleField.addKeyListener(keyListener);
    tabTitleField.addFocusListener(focusListener);
    keywordField = new JTextField();
    keywordField.addKeyListener(keyListener);
    keywordField.addFocusListener(focusListener);
    authorField = new JTextField();
    authorField.addKeyListener(keyListener);
    authorField.addFocusListener(focusListener);
    levelField = new JTextField();
    levelField.addKeyListener(keyListener);
    levelField.addFocusListener(focusListener);
    languagesField = new JTextField();
    languagesField.addKeyListener(keyListener);
    languagesField.addFocusListener(focusListener);
    commentPane = new JTextPane() {
      public void paintComponent(Graphics g) {
        if(OSPRuntime.antiAliasText) {
          Graphics2D g2 = (Graphics2D) g;
          RenderingHints rh = g2.getRenderingHints();
          rh.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
          rh.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        super.paintComponent(g);
      }

    };
    commentPane.addKeyListener(keyListener);
    commentPane.addFocusListener(focusListener);
    commentScroller = new JScrollPane(commentPane);
    commentTitle = BorderFactory.createTitledBorder(LaunchRes.getString("Label.Comments")); //$NON-NLS-1$
    commentScroller.setBorder(commentTitle);
    descriptionPane = new JTextPane();
    descriptionPane.addKeyListener(keyListener);
    descriptionPane.addFocusListener(focusListener);
    descriptionScroller = new JScrollPane(descriptionPane);
    descriptionTitle = BorderFactory.createTitledBorder(LaunchRes.getString("Label.Description")); //$NON-NLS-1$
    descriptionScroller.setBorder(descriptionTitle);
    displaySplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT) {
      public void setDividerLocation(int loc) {
        super.setDividerLocation(loc);
        double divider = getDividerLocation();
        divider /= getHeight()-getDividerSize();
        setName(""+divider); //$NON-NLS-1$
      }
      public void setTopComponent(Component comp) {
      	if (comp == getTopComponent()) return;
        int prev = getLastDividerLocation();
        String divider = getName();
        super.setTopComponent(comp);
        if(divider!=null) {
          double loc = Double.parseDouble(divider);
          loc = Math.max(0.0, loc);
          loc = Math.min(1.0, loc);
          setDividerLocation(loc);
          setLastDividerLocation(prev);
          Runnable runner = new Runnable() {
            public void run() {
              JViewport view = descriptionScroller.getViewport();
              view.setViewPosition(new Point(0, 0));
            }
          };
          SwingUtilities.invokeLater(runner);
        }
      }

    };
    displaySplitPane.setBottomComponent(descriptionScroller);
    displaySplitPane.setOneTouchExpandable(true);
    displaySplitPane.setResizeWeight(1);
    htmlPane = new JTextPane() {
      public void paintComponent(Graphics g) {
        if(OSPRuntime.antiAliasText) {
          Graphics2D g2 = (Graphics2D) g;
          RenderingHints rh = g2.getRenderingHints();
          rh.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
          rh.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        super.paintComponent(g);
      }

    };
    htmlPane.setEditable(false);
    htmlScroller = new JScrollPane(htmlPane);
    htmlScroller.setBorder(displayTitle);
    displaySplitPane.setTopComponent(htmlScroller);
    hiddenCheckBox = new JCheckBox();
    hiddenCheckBox.addActionListener(changeAction);
    hiddenCheckBox.setContentAreaFilled(false);
    hiddenCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    buttonViewCheckBox = new JCheckBox();
    buttonViewCheckBox.addActionListener(changeAction);
    buttonViewCheckBox.setContentAreaFilled(false);
    buttonViewCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    // assemble display panel
    JPanel displayPanel = new JPanel(new BorderLayout());
    JToolBar nameBar = new JToolBar();
    nameBar.setFloatable(false);
    nameLabel = new JLabel();
    labels.add(nameLabel);
    nameBar.add(nameLabel);
    nameBar.add(nameField);
    nameBar.add(hiddenCheckBox);
    displayPanel.add(nameBar, BorderLayout.NORTH);
    JPanel tooltipPanel = new JPanel(new BorderLayout());
    displayPanel.add(tooltipPanel, BorderLayout.CENTER);
    JToolBar tooltipBar = new JToolBar();
    tooltipBar.setFloatable(false);
    tooltipLabel = new JLabel();
    tooltipBar.add(tooltipLabel);
    tooltipBar.add(tooltipField);
    tooltipPanel.add(tooltipBar, BorderLayout.NORTH);
    urlPanel = new JPanel(new BorderLayout());
    tooltipPanel.add(urlPanel, BorderLayout.CENTER);
    displayBar = new JToolBar();
    displayBar.setFloatable(false);
    displayLabel = new JLabel();
    pathLabel = new JLabel();
    pathLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
    tabTitleLabel = new JLabel();
    tabTitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
    displaySpinnerModel = new SpinnerNumberModel(0, 0, 1, 1);
    displaySpinner = new JSpinner(displaySpinnerModel);
    JSpinner.NumberEditor editor = new JSpinner.NumberEditor(displaySpinner);
    displaySpinner.setEditor(editor);
    displaySpinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if(pathField.getBackground()==Color.yellow) {
          refreshSelectedNode();
        } else {
          refreshGUI();
        }
      }

    });
    displayBar.add(displayLabel);
    displayBar.add(displaySpinner);
    displayBar.add(pathLabel);
    displayBar.add(pathField);
    openDisplayChooserButton = new JButton(openIcon);
    openDisplayChooserButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	JPopupMenu openPopup = new JPopupMenu();
      	openPopup.add(openURLAction);
      	openPopup.add(openPDFAction);
      	openPopup.add(searchJarForModelAction);
      	openPopup.show(openDisplayChooserButton, 0, openDisplayChooserButton.getHeight());
      }
    });
    displayBar.add(openDisplayChooserButton);
    modelArgsDialog = new JDialog(frame, true);
    JToolBar modelArgBar = new JToolBar();
    modelArgBar.setFloatable(false);
    modelArgBar.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 10));
    modelArgLabel = new JLabel();
    modelArgLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));
    SpinnerModel model = new SpinnerNumberModel(0, 0, maxArgs-1, 1);
    modelArgSpinner = new JSpinner(model);
    editor = new JSpinner.NumberEditor(modelArgSpinner);
    modelArgSpinner.setEditor(editor);
    modelArgSpinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if(argField.getBackground()==Color.yellow) {
          refreshSelectedNode();
        } else {
          refreshGUI();
        }
      }

    });
    modelArgBar.add(modelArgLabel);
    modelArgBar.add(modelArgSpinner);
    modelArgBar.add(modelArgField);
    modelArgBar.add(openModelArgAction);
    modelArgCloseButton = new JButton(LaunchRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
    modelArgCloseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	modelArgsDialog.setVisible(false);
      }
    });
    modelArgClearButton = new JButton(LaunchRes.getString("Dialog.Button.Clear")); //$NON-NLS-1$
    modelArgClearButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int n = ((Integer) displaySpinner.getValue()).intValue();
      	LaunchNode node = getSelectedNode();
        LaunchNode.DisplayTab tab = node.getDisplayTab(n);
        if (tab!=null) {
        	tab.setModelArgs(new String[0]);
        	refreshGUI();
        }
      }
    });
    JPanel buttonPanel = new JPanel();    
    buttonPanel.add(modelArgClearButton);
    buttonPanel.add(modelArgCloseButton);
    modelArgsDialog.add(modelArgBar, BorderLayout.CENTER);
    modelArgsDialog.add(buttonPanel, BorderLayout.SOUTH);
    modelArgsDialog.pack();
    Dimension dim = modelArgsDialog.getSize();
    int w = Math.max(dim.width, 240);
    modelArgsDialog.setSize(w, dim.height);
    // button to show the modelArgs dialog
    showModelArgsButton = new JButton();
    showModelArgsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Point p0 = new Frame().getLocation();
      	if (modelArgsDialog.getLocation().x == p0.x)
      		modelArgsDialog.setLocationRelativeTo(showModelArgsButton);
      	modelArgsDialog.setVisible(true);
      }
    });
    displayBar.add(tabTitleLabel);
    displayBar.add(tabTitleField);
    urlPanel.add(displayBar, BorderLayout.NORTH);
    urlPanel.add(displaySplitPane, BorderLayout.CENTER);
    // assemble launch panel
    JPanel launchPanel = new JPanel(new BorderLayout());
    JToolBar jarBar = new JToolBar();
    jarBar.setFloatable(false);
    jarLabel = new JLabel();
    labels.add(jarLabel);
    jarBar.add(jarLabel);
    jarBar.add(jarField);
    jarBar.add(openJarAction);
    launchPanel.add(jarBar, BorderLayout.NORTH);
    JPanel classPanel = new JPanel(new BorderLayout());
    launchPanel.add(classPanel, BorderLayout.CENTER);
    JToolBar classBar = new JToolBar();
    classBar.setFloatable(false);
    classLabel = new JLabel();
    labels.add(classLabel);
    classBar.add(classLabel);
    classBar.add(classField);
    classBar.add(searchJarAction);
    classPanel.add(classBar, BorderLayout.NORTH);
    JPanel argPanel = new JPanel(new BorderLayout());
    classPanel.add(argPanel, BorderLayout.CENTER);
    JToolBar argBar = new JToolBar();
    argBar.setFloatable(false);
    argLabel = new JLabel();
    labels.add(argLabel);
    argBar.add(argLabel);
    model = new SpinnerNumberModel(0, 0, maxArgs-1, 1);
    argSpinner = new JSpinner(model);
    editor = new JSpinner.NumberEditor(argSpinner);
    argSpinner.setEditor(editor);
    argSpinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if(argField.getBackground()==Color.yellow) {
          refreshSelectedNode();
        } else {
          refreshGUI();
        }
      }

    });
    argBar.add(argSpinner);
    argBar.add(argField);
    argBar.add(openArgAction);
    argPanel.add(argBar, BorderLayout.NORTH);
    JPanel optionsPanel = new JPanel(new BorderLayout());
    argPanel.add(optionsPanel, BorderLayout.CENTER);
    JToolBar optionsBar = new JToolBar();
    optionsBar.setFloatable(false);
    singleVMCheckBox = new JCheckBox();
    singleVMCheckBox.addActionListener(changeAction);
    singleVMCheckBox.setContentAreaFilled(false);
    singleVMCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    showLogCheckBox = new JCheckBox();
    showLogCheckBox.addActionListener(changeAction);
    showLogCheckBox.setContentAreaFilled(false);
    showLogCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    clearLogCheckBox = new JCheckBox();
    clearLogCheckBox.addActionListener(changeAction);
    clearLogCheckBox.setContentAreaFilled(false);
    clearLogCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    singletonCheckBox = new JCheckBox();
    singletonCheckBox.addActionListener(changeAction);
    singletonCheckBox.setContentAreaFilled(false);
    singletonCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    singleAppCheckBox = new JCheckBox();
    singleAppCheckBox.addActionListener(changeAction);
    singleAppCheckBox.setContentAreaFilled(false);
    singleAppCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    levelDropDown = new JComboBox(OSPLog.levels);
    levelDropDown.setMaximumSize(levelDropDown.getMinimumSize());
    levelDropDown.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(levelDropDown.isEnabled()) {
          LaunchNode node = getSelectedNode();
          if(node!=null) {
            node.setLogLevel((Level) levelDropDown.getSelectedItem());
          }
        }
      }

    });
    Box checkBoxPanel = Box.createVerticalBox();
    JToolBar bar = new JToolBar();
    bar.setFloatable(false);
    bar.setAlignmentX(Component.LEFT_ALIGNMENT);
    bar.add(singletonCheckBox);
    bar.add(Box.createHorizontalGlue());
    checkBoxPanel.add(bar);
    bar = new JToolBar();
    bar.setFloatable(false);
    bar.setAlignmentX(Component.LEFT_ALIGNMENT);
    bar.add(singleVMCheckBox);
    bar.add(Box.createHorizontalGlue());
    checkBoxPanel.add(bar);
    bar = new JToolBar();
    bar.setFloatable(false);
    bar.setAlignmentX(Component.LEFT_ALIGNMENT);
    bar.add(singleAppCheckBox);
    bar.add(Box.createHorizontalGlue());
    checkBoxPanel.add(bar);
    bar = new JToolBar();
    bar.setFloatable(false);
    bar.add(showLogCheckBox);
    bar.add(Box.createHorizontalStrut(4));
    bar.add(levelDropDown);
    bar.add(Box.createHorizontalStrut(4));
    bar.add(clearLogCheckBox);
    bar.add(Box.createHorizontalGlue());
    bar.setAlignmentX(Component.LEFT_ALIGNMENT);
    checkBoxPanel.add(bar);
    optionsTitle = BorderFactory.createTitledBorder(LaunchRes.getString("Label.Options")); //$NON-NLS-1$
    Border recess = BorderFactory.createLoweredBevelBorder();
    optionsBar.setBorder(BorderFactory.createCompoundBorder(recess, optionsTitle));
    optionsBar.add(checkBoxPanel);
    optionsPanel.add(optionsBar, BorderLayout.NORTH);
    // create author tab
    JPanel authorPanel = new JPanel(new BorderLayout());
    // put author field on top
    JToolBar authorBar = new JToolBar();
    authorPanel.add(authorBar, BorderLayout.NORTH);
    authorBar.setFloatable(false);
    authorLabel = new JLabel();
    labels.add(authorLabel);
    authorBar.add(authorLabel);
    authorBar.add(authorField);
    // add keyword panel
    JPanel keywordPanel = new JPanel(new BorderLayout());
    authorPanel.add(keywordPanel, BorderLayout.CENTER);
    JToolBar keywordBar = new JToolBar();
    keywordPanel.add(keywordBar, BorderLayout.NORTH);
    keywordBar.setFloatable(false);
    keywordLabel = new JLabel();
    labels.add(keywordLabel);
    keywordBar.add(keywordLabel);
    keywordBar.add(keywordField);
    // add level panel
    JPanel levelPanel = new JPanel(new BorderLayout());
    keywordPanel.add(levelPanel, BorderLayout.CENTER);
    JToolBar levelBar = new JToolBar();
    levelPanel.add(levelBar, BorderLayout.NORTH);
    levelBar.setFloatable(false);
    levelLabel = new JLabel();
    labels.add(levelLabel);
    levelBar.add(levelLabel);
    levelBar.add(levelField);
    // add languages panel
    JPanel languagesPanel = new JPanel(new BorderLayout());
    levelPanel.add(languagesPanel, BorderLayout.CENTER);
    JToolBar languagesBar = new JToolBar();
    languagesPanel.add(languagesBar, BorderLayout.NORTH);
    languagesBar.setFloatable(false);
    languagesLabel = new JLabel();
    labels.add(languagesLabel);
    languagesBar.add(languagesLabel);
    languagesBar.add(languagesField);
    // put security panel in center
    JPanel securityPanel = new JPanel(new BorderLayout());
    languagesPanel.add(securityPanel, BorderLayout.CENTER);
    JToolBar securityBar = new JToolBar();
    securityBar.setFloatable(false);
    securityPanel.add(securityBar, BorderLayout.NORTH);
    securityTitle = BorderFactory.createTitledBorder(LaunchRes.getString("Label.Security")); //$NON-NLS-1$
    securityBar.setBorder(BorderFactory.createCompoundBorder(recess, securityTitle));
    // use vertical box to stack components in security panel
    Box securityBox = Box.createVerticalBox();
    securityBar.add(securityBox);
    // add editorEnabled to box
    bar = new JToolBar();
    bar.setFloatable(false);
    bar.setAlignmentX(Component.LEFT_ALIGNMENT);
    editorEnabledCheckBox = new JCheckBox();
    editorEnabledCheckBox.addActionListener(changeAction);
    editorEnabledCheckBox.setContentAreaFilled(false);
    editorEnabledCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    bar.add(editorEnabledCheckBox);
    bar.add(Box.createHorizontalGlue());
    securityBox.add(bar);
    // add encrypt checkbox to box
    bar = new JToolBar();
    bar.setFloatable(false);
    bar.setAlignmentX(Component.LEFT_ALIGNMENT);
    encryptCheckBox = new JCheckBox();
    encryptCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(encryptCheckBox.isSelected()&&(password==null)) {
          password = "";                                                   //$NON-NLS-1$
        } else if(!encryptCheckBox.isSelected()) {
          password = null;
        }
        OSPLog.finest(LaunchRes.getString("Log.Message.ChangeEncrypted")); //$NON-NLS-1$
        if(tabSetName!=null) {
          changedFiles.add(tabSetName);
        }
        refreshGUI();
      }

    });
    encryptCheckBox.setContentAreaFilled(false);
    bar.add(encryptCheckBox);
    bar.add(Box.createHorizontalGlue());
    securityBox.add(bar);
    // add password field to box
    bar = new JToolBar();
    bar.setFloatable(false);
    bar.setAlignmentX(Component.LEFT_ALIGNMENT);
    passwordLabel = new JLabel();
    passwordLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 2));
    bar.add(passwordLabel);
    passwordEditor = new JTextField();
    passwordEditor.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if(e.getKeyCode()==KeyEvent.VK_ENTER) {
          String text = passwordEditor.getText();
          if(text.equals("")&& //$NON-NLS-1$
            (!encryptCheckBox.isSelected()||!encryptCheckBox.isEnabled())) {
            text = null;
          }
          if(text!=password) {
            changedFiles.add(tabSetName);
          }
          password = text;
          refreshGUI();
        } else {
          passwordEditor.setBackground(Color.yellow);
        }
      }

    });
    bar.add(passwordEditor);
    bar.add(Box.createHorizontalGlue());
    onLoadCheckBox = new JCheckBox();
    onLoadCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pwRequiredToLoad = onLoadCheckBox.isSelected();
        OSPLog.finest(LaunchRes.getString("Log.Message.ChangePWRequirement")); //$NON-NLS-1$
        if(tabSetName!=null) {
          changedFiles.add(tabSetName);
        }
        refreshGUI();
      }

    });
    onLoadCheckBox.setContentAreaFilled(false);
    bar.add(onLoadCheckBox);
    securityBox.add(bar);
    securityPanel.add(commentScroller, BorderLayout.CENTER);
    // create the editor tabs
    editorTabs = new JTabbedPane(SwingConstants.TOP);
    editorTabs.addTab(LaunchRes.getString("Tab.Display"), displayPanel); //$NON-NLS-1$
    editorTabs.addTab(LaunchRes.getString("Tab.Launch"), launchPanel);   //$NON-NLS-1$
    editorTabs.addTab(LaunchRes.getString("Tab.Author"), authorPanel);   //$NON-NLS-1$
    // editorTabs.addTab(LaunchRes.getString("Tab.Server"), serverPanel);
    // create toolbar
    toolbar = new JToolBar();
    toolbar.setFloatable(false);
    toolbar.setRollover(true);
    toolbar.setBorder(BorderFactory.createLineBorder(Color.gray));
    frame.getContentPane().add(toolbar, BorderLayout.NORTH);
    newTabButton = new JButton(newTabAction);
    toolbar.add(newTabButton);
    addButton = new JButton(addAction);
    toolbar.add(addButton);
    cutButton = new JButton(cutAction);
    toolbar.add(cutButton);
    copyButton = new JButton(copyAction);
    toolbar.add(copyButton);
    pasteButton = new JButton(pasteAction);
    toolbar.add(pasteButton);
    moveUpButton = new JButton(moveUpAction);
    toolbar.add(moveUpButton);
    moveDownButton = new JButton(moveDownAction);
    toolbar.add(moveDownButton);
    titleLabel = new JLabel(LaunchRes.getString("Label.Title")); //$NON-NLS-1$
    titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 2));
    toolbar.add(titleLabel);
    toolbar.add(titleField);
    toolbar.add(buttonViewCheckBox);
    hideRootCheckBox = new JCheckBox();
    hideRootCheckBox.addActionListener(changeAction);
    hideRootCheckBox.setContentAreaFilled(false);
    toolbar.add(hideRootCheckBox);
    // create menu items
    int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    // create new tabset item
    newItem = new JMenuItem(newTabSetAction);
    newItem.setAccelerator(KeyStroke.getKeyStroke('N', mask));
    // create preview item
    previewItem = new JMenuItem();
    previewItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String base = tabSetBasePath;
        previewing = true;
        LaunchSet set = new LaunchSet(LaunchBuilder.this, tabSetName);
        XMLControlElement control = new XMLControlElement(set);
        control.setValue("filename", tabSetName); //$NON-NLS-1$
        Launcher launcher = new Launcher(control.toXML());
        LaunchNode node = LaunchBuilder.this.getSelectedNode();
        if(node!=null) {
          launcher.setSelectedNode(node.getPathString());
        }
        Point p = LaunchBuilder.this.frame.getLocation();
        launcher.frame.setLocation(p.x+24, p.y+24);
        launcher.frame.setVisible(true);
        // set close operation to dispose to distinguish from default hide
        launcher.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        tabSetBasePath = base;
        previewing = false;
        launcher.password = password;
        launcher.previewing = true;
        launcher.spawner = LaunchBuilder.this;
        launcher.refreshGUI();
      }

    });
    // create other menu items
    importItem = new JMenuItem(importAction);
    saveJarItem = new JMenuItem(saveJarAction);
    saveNodeItem = new JMenuItem(saveAction);
    saveNodeAsItem = new JMenuItem(saveAsAction);
    saveAllItem = new JMenuItem(saveAllAction);
    openTabItem = new JMenuItem(openTabAction);
    saveAllItem.setAccelerator(KeyStroke.getKeyStroke('S', mask));
    saveSetAsItem = new JMenuItem(saveSetAsAction);
    toolsMenu = new JMenu();
    frame.getJMenuBar().add(toolsMenu, 2);
    encryptionToolItem = new JMenuItem();
    toolsMenu.add(encryptionToolItem);
    encryptionToolItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        EncryptionTool.getTool().setVisible(true);
      }

    });
    // replace tab listener
    tabbedPane.removeMouseListener(tabListener);
    tabListener = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if(contentPane.getTopLevelAncestor()!=frame) {
          return;
        }
        if(OSPRuntime.isPopupTrigger(e)) {
          // make popup and add items
          JPopupMenu popup = new JPopupMenu();
          JMenuItem item = new JMenuItem(LaunchRes.getString("MenuItem.Close")); //$NON-NLS-1$
          item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              removeSelectedTab();
            }

          });
          popup.add(item);
          popup.addSeparator();
          item = new JMenuItem(LaunchRes.getString("Menu.File.SaveAs"));         //$NON-NLS-1$
          item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              LaunchNode node = getSelectedTab().getRootNode();
              if(saveAs(node)!=null) {
                int i = tabbedPane.getSelectedIndex();
                tabbedPane.setTitleAt(i, node.toString());
              }
              refreshGUI();
            }

          });
          popup.add(item);
          final int i = tabbedPane.getSelectedIndex();
          if((i>0)||(i<tabbedPane.getTabCount()-1)) {
            popup.addSeparator();
          }
          if(i<tabbedPane.getTabCount()-1) {
            item = new JMenuItem(LaunchRes.getString("Popup.MenuItem.MoveUp")); //$NON-NLS-1$
            item.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                LaunchPanel tab = getSelectedTab();
                LaunchNode root = tab.getRootNode();
                LaunchBuilder.super.removeSelectedTab();
                tabbedPane.insertTab(getDisplayName(root.getFileName()), null, tab, null, i+1);
                tabbedPane.setSelectedComponent(tab);
                tabs.add(i+1, tab);
              }

            });
            popup.add(item);
          }
          if(i>0) {
            item = new JMenuItem(LaunchRes.getString("Popup.MenuItem.MoveDown")); //$NON-NLS-1$
            item.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                LaunchPanel tab = getSelectedTab();
                LaunchNode root = tab.getRootNode();
                LaunchBuilder.super.removeSelectedTab();
                tabbedPane.insertTab(getDisplayName(root.getFileName()), null, tab, null, i-1);
                tabbedPane.setSelectedComponent(tab);
                tabs.add(i-1, tab);
              }

            });
            popup.add(item);
          }
          popup.show(tabbedPane, e.getX(), e.getY()+8);
        }
      }

    };
    tabbedPane.addMouseListener(tabListener);
    frame.pack();
    displaySplitPane.setDividerLocation(0.7);
  }

  /**
   * Sets the font level.
   *
   * @param level the level
   */
  public void setFontLevel(int level) {
    final int prev = displaySplitPane.getLastDividerLocation();
    final String divider = displaySplitPane.getName();
    // set font levels of titled borders
    FontSizer.setFonts(displayTitle, level);
    FontSizer.setFonts(commentTitle, level);
    FontSizer.setFonts(descriptionTitle, level);
    FontSizer.setFonts(optionsTitle, level);
    FontSizer.setFonts(securityTitle, level);
    super.setFontLevel(level);
    if(divider!=null) {
      Runnable runner = new Runnable() {
        public void run() {
          double loc = Double.parseDouble(divider);
          loc = Math.max(0.0, loc);
          loc = Math.min(1.0, loc);
          displaySplitPane.setDividerLocation(loc);
          displaySplitPane.setLastDividerLocation(prev);
        }

      };
      SwingUtilities.invokeLater(runner);
    }
  }

  /**
   * Creates the actions.
   */
  protected void createActions() {
    String imageFile = "/org/opensourcephysics/resources/tools/images/open.gif"; //$NON-NLS-1$
    openIcon = loadIcon(imageFile);
    openJarAction = new AbstractAction(null, openIcon) {
      public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = getJARChooser();
        int result = chooser.showOpenDialog(null);
        if(result==JFileChooser.APPROVE_OPTION) {
          File file = chooser.getSelectedFile();
          String newJar = XML.getRelativePath(file.getPath());
          String jars = jarField.getText();
          if(jars.indexOf(newJar)>-1) {
            newJar = null;
          }
          if(!jars.equals("")) { //$NON-NLS-1$
            jars += ";";         //$NON-NLS-1$
          }
          if(newJar!=null) {
            jarField.setText(jars+newJar);
          }
          OSPRuntime.chooserDir = XML.getDirectoryPath(file.getPath());
          searchJarAction.setEnabled(true);
          refreshSelectedNode();
        }
      }

    };
    searchJarAction = new AbstractAction(null, openIcon) {
      public void actionPerformed(ActionEvent e) {
        LaunchNode node = getSelectedNode();
        if((node!=null)&&getClassChooser().chooseClassFor(node)) {
          if(node.getOwner()!=null) {
            changedFiles.add(node.getOwner().getFileName());
          } else {
            changedFiles.add(tabSetName);
          }
          refreshClones(node);
          refreshGUI();
        }
      }

    };
    searchJarAction.setEnabled(false);
    saveJarAction = new AbstractAction(LaunchRes.getString("LaunchBuilder.Action.CreateJar.Name")) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        // define base folder
        File base = new File(XML.getDirectoryPath(OSPRuntime.getLaunchJarPath()));
        String jarName = XML.stripExtension(LaunchBuilder.this.tabSetName)+".jar";                   //$NON-NLS-1$
        File target = new File(base, jarName);
        // source list includes launch jar plus user-selected files/folders
        final ArrayList<String> source = new ArrayList<String>();
        JarTreeDialog jarChooser = new JarTreeDialog(frame, base);
        // in jarChooser, preselect launch jar and all non-zip/jar child files
        source.add(OSPRuntime.getLaunchJarName());                                                   // launch jar added first
        // add files in accept method
        base.listFiles(new java.io.FileFilter() {
          public boolean accept(File f) {
            // skip jars, zip files and other undesirables
            if(f.getName().endsWith(".jar")||                                                        //$NON-NLS-1$
              f.getName().endsWith(".zip")||                                                         //$NON-NLS-1$
              f.getName().endsWith(".trz")||                                                         //$NON-NLS-1$
                f.getName().endsWith(".DS_Store")||                                                  //$NON-NLS-1$
                  f.getName().endsWith(".localized")||                                               //$NON-NLS-1$
                    f.getName().endsWith(".tmp")) {                                                  //$NON-NLS-1$
              return false;
            }
            // file is accepted, so add to source list
            source.add(f.getName());
            return true;
          }

        });
        jarChooser.setSelectionRelativePaths(source.toArray(new String[0]));
        jarChooser.setVisible(true);
        String[] paths = jarChooser.getSelectionRelativePaths();
        if(paths==null) {
          return;
        }
        source.clear();
        // add paths to source list
        for(int i = 0; i<paths.length; i++) {
          if(paths[i].equals(OSPRuntime.getLaunchJarName())) {
            // put launch jar first so later paths will overwrite
            source.add(0, paths[i]);
          } else {
            source.add(paths[i]);
          }
        }
        java.util.jar.Manifest manifest = JarTool.createManifest("", "org.opensourcephysics.tools.Launcher"); //$NON-NLS-1$//$NON-NLS-2$
        JarTool.alwaysOverwrite();    // overwrite all with no warning
        JarTool.setOwnerFrame(frame); // set owner of progress dialog
        JarTool.getTool().create(source, base, target, manifest);
      }

    };
    openArgAction = new AbstractAction(null, openIcon) {
      public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = getFileChooser();
        int result = chooser.showOpenDialog(null);
        if(result==JFileChooser.APPROVE_OPTION) {
          File file = chooser.getSelectedFile();
          argField.setText(XML.getRelativePath(file.getPath()));
          OSPRuntime.chooserDir = XML.getDirectoryPath(file.getPath());
          refreshSelectedNode();
        }
      }

    };
    openModelArgAction = new AbstractAction(null, openIcon) {
      public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = getFileChooser();
        int result = chooser.showOpenDialog(null);
        if(result==JFileChooser.APPROVE_OPTION) {
          File file = chooser.getSelectedFile();
          modelArgField.setText(XML.getRelativePath(file.getPath()));
          OSPRuntime.chooserDir = XML.getDirectoryPath(file.getPath());
          refreshSelectedNode();
        }
      }

    };
    openURLAction = new AbstractAction(LaunchRes.getString("Popup.MenuItem.OpenHTML")) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = getHTMLChooser();
        int result = chooser.showOpenDialog(null);
        if(result==JFileChooser.APPROVE_OPTION) {
          File file = chooser.getSelectedFile();
          pathField.setText(XML.getRelativePath(file.getPath()));
          OSPRuntime.chooserDir = XML.getDirectoryPath(file.getPath());
          refreshSelectedNode();
        }
      }

    };
    openPDFAction = new AbstractAction(LaunchRes.getString("Popup.MenuItem.OpenPDF")) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = getPDFChooser();
        int result = chooser.showOpenDialog(null);
        if(result==JFileChooser.APPROVE_OPTION) {
          File file = chooser.getSelectedFile();
          pathField.setText(XML.getRelativePath(file.getPath()));
          OSPRuntime.chooserDir = XML.getDirectoryPath(file.getPath());
          refreshSelectedNode();
        }
      }

    };
    searchJarForModelAction = new AbstractAction(LaunchRes.getString("Popup.MenuItem.OpenModel")) { //$NON-NLS-1$
		  public void actionPerformed(ActionEvent e) {
		    Class<?> type = getClassChooser().chooseModel(pathField.getText());
		    if(type!=null) {
		      pathField.setText(type.getName());
		      refreshSelectedNode();
		    }
		  }
		};
    searchJarForModelAction.setEnabled(false);
    openTabAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        LaunchNode node = getSelectedNode();
        String tabName = node.toString();
        for(int i = 0; i<tabbedPane.getComponentCount(); i++) {
          if(tabbedPane.getTitleAt(i).equals(tabName)) {
            tabbedPane.setSelectedIndex(i);
            return;
          }
        }
        XMLControl control = new XMLControlElement(node);
        XMLControl cloneControl = new XMLControlElement(control);
        LaunchNode clone = (LaunchNode) cloneControl.loadObject(null);
        clone.setFileName(node.getFileName());
        addTab(clone);
        refreshGUI();
      }

    };
    changeAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        refreshSelectedNode();
      }

    };
    newTabSetAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        String[] prevArgs = undoManager.getLauncherState();
        if(removeAllTabs()) {
          if(prevArgs!=null) {
            // null new args indicate not redoable
            LauncherUndo.LoadEdit edit = undoManager.new LoadEdit(null, prevArgs);
            undoSupport.postEdit(edit);
          }
          LaunchNode root = new LaunchNode(LaunchRes.getString("NewTab.Name")); //$NON-NLS-1$
          addTab(root);
        }
      }

    };
    addAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        LaunchNode newNode = new LaunchNode(LaunchRes.getString("NewNode.Name")); //$NON-NLS-1$
        addChildToSelectedNode(newNode);
      }

    };
    newTabAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        LaunchNode root = new LaunchNode(LaunchRes.getString("NewTab.Name")); //$NON-NLS-1$
        addTab(root);
      }

    };
    cutAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        copyAction.actionPerformed(null);
        removeSelectedNodes();
      }
    };
    copyAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        if(getSelectedTab()!=null) {
          ArrayList<LaunchNode> nodes = getSelectedTab().getSelectedNodes();
          if(nodes!=null) {
          	NodeSet nodeSet = new NodeSet(nodes);
            XMLControl control = new XMLControlElement(nodeSet);
            StringSelection data = new StringSelection(control.toXML());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(data, data);
          }
        }
      }

    };
    pasteAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        try {
          Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
          Transferable data = clipboard.getContents(null);
          String dataString = (String) data.getTransferData(DataFlavor.stringFlavor);
          if(dataString!=null) {
            XMLControlElement control = new XMLControlElement();
            control.readXML(dataString);
            if(control.getObjectClass()==NodeSet.class) {
              NodeSet nodeSet = (NodeSet) control.loadObject(null);
              for (Object[] next: nodeSet.nodes) {
              	LaunchNode node = (LaunchNode)next[0];
              	if (next.length>1 && next[1]!=null)
              		node.setFileName(next[1].toString());
                addChildToSelectedNode(node);              	
              }
            }
          }
        } catch(Exception ex) {
          ex.printStackTrace();
        }
      }

    };
    importAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        getXMLChooser().setFileFilter(xmlFileFilter);
        int result = getXMLChooser().showOpenDialog(null);
        if(result==JFileChooser.APPROVE_OPTION) {
          // open the file in an xml control
          File file = getXMLChooser().getSelectedFile();
          String fileName = file.getAbsolutePath();
          OSPRuntime.chooserDir = XML.getDirectoryPath(file.getPath());
          XMLControlElement control = new XMLControlElement(fileName);
          if(control.failedToRead()) {
            OSPLog.info(LaunchRes.getString("Log.Message.InvalidXML")+" "+fileName);                                         //$NON-NLS-1$//$NON-NLS-2$
            JOptionPane.showMessageDialog(null, LaunchRes.getString("Dialog.InvalidXML.Message")+" \""+XML.getName(fileName) //$NON-NLS-1$ //$NON-NLS-2$
                                          +"\"",                                                                          //$NON-NLS-1$
                                            LaunchRes.getString("Dialog.InvalidXML.Title"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
            return;
          }
          if(control.getObjectClass()==LaunchNode.class) {
            // add the child node
            LaunchNode child = (LaunchNode) control.loadObject(null);
            child.setFileName(fileName);
            addChildToSelectedNode(child);
          } else {
            OSPLog.info(LaunchRes.getString("Log.Message.NotLauncherFile")+" "+fileName);                                 //$NON-NLS-1$//$NON-NLS-2$
            JOptionPane.showMessageDialog(null, LaunchRes.getString("Dialog.NotLauncherFile.Message")+" \""               //$NON-NLS-1$ //$NON-NLS-2$
                                          +XML.getName(fileName)+"\"",                           //$NON-NLS-1$
                                            LaunchRes.getString("Dialog.NotLauncherFile.Title"), //$NON-NLS-1$
                                            JOptionPane.WARNING_MESSAGE);
          }
        }
      }

    };
    saveAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        LaunchNode node = getSelectedNode();
        if(node.getFileName()!=null) {
          save(node, node.getFileName());
          refreshGUI();
        }
      }

    };
    saveAsAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        LaunchNode node = getSelectedNode();
        LaunchNode parent = (LaunchNode) node.getParent();
        String fileName = saveAs(node);
        if(fileName!=null) {
          // set ancestors to self-contained=false so this node will be saved correctly
          selfContained = false;
          Enumeration<?> en = node.pathFromAncestorEnumeration(node.getRoot());
          while(en.hasMoreElements()) {
            LaunchNode next = (LaunchNode) en.nextElement();
            next.setSelfContained(false);
            next.parentSelfContained = false;
          }
          if(parent!=null) {
            if(parent.getOwner()!=null) {
              changedFiles.add(parent.getOwner().getFileName());
            }
            refreshClones(parent);
          }
        }
        refreshGUI();
      }

    };
    saveAllAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        if(tabSetName.equals(LaunchRes.getString("Tabset.Name.New"))) { //$NON-NLS-1$
          saveTabSetAs();
        } else {
          saveTabSet();
        }
        refreshGUI();
      }

    };
    saveSetAsAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        saveTabSetAs();
        refreshGUI();
      }

    };
    moveUpAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
      	ArrayList<LaunchNode> nodes = getSelectedTab().getSelectedNodes();
      	if (nodes == null) return;
      	// move nodes up in top to bottom order
        for (LaunchNode node: nodes) {
	        LaunchNode parent = (LaunchNode) node.getParent();
	        if(parent==null) {
	          continue;
	        }
	        int i = parent.getIndex(node);
	        if(i>0 && !nodes.contains(parent.getChildBefore(node))) {
	          getSelectedTab().treeModel.removeNodeFromParent(node);
	          getSelectedTab().treeModel.insertNodeInto(node, parent, i-1);
	          if(parent.getOwner()!=null) {
	            changedFiles.add(parent.getOwner().getFileName());
	          } else {
	            changedFiles.add(tabSetName);
	          }
	        }
        }
        getSelectedTab().setSelectedNodes(nodes);
        refreshGUI();
      }

    };
    moveDownAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
      	ArrayList<LaunchNode> nodes = getSelectedTab().getSelectedNodes();
      	if (nodes == null) return;
      	// move nodes down in bottom to top order
        for (int j = nodes.size()-1; j > -1; j--) {
        	LaunchNode node = nodes.get(j);
	        LaunchNode parent = (LaunchNode) node.getParent();
	        if(parent==null) {
	          continue;
	        }
	        int i = parent.getIndex(node);
	        int end = parent.getChildCount();
	        if(i<end-1 && !nodes.contains(parent.getChildAfter(node))) {
	          getSelectedTab().treeModel.removeNodeFromParent(node);
	          getSelectedTab().treeModel.insertNodeInto(node, parent, i+1);
	          if(parent.getOwner()!=null) {
	            changedFiles.add(parent.getOwner().getFileName());
	          } else {
	            changedFiles.add(tabSetName);
	          }
	        }
        }
        getSelectedTab().setSelectedNodes(nodes);
        refreshGUI();
      }

    };
    // create focus listener
    focusListener = new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        refreshSelectedNode();
        refreshGUI();
      }

    };
    // create key listener
    keyListener = new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        JComponent comp = (JComponent) e.getSource();
        if((e.getKeyCode()==KeyEvent.VK_ENTER)&&(((comp!=descriptionPane)&&(comp!=commentPane))||e.isControlDown()||e.isShiftDown())) {
          refreshSelectedNode();
          refreshGUI();
        } else {
          comp.setBackground(Color.yellow);
        }
      }

    };
  }

  /**
   * Removes the selected node.
   */
  public void removeSelectedNode() {
    LaunchNode node = getSelectedNode();
    if((node==null)||(node.getParent()==null)) {
      return;
    }
    LaunchNode parent = (LaunchNode) node.getParent();
    getSelectedTab().treeModel.removeNodeFromParent(node);
    getSelectedTab().setSelectedNode(parent);
    if(parent.getOwner()!=null) {
      changedFiles.add(parent.getOwner().getFileName());
    } else {
      changedFiles.add(tabSetName);
    }
    refreshClones(parent);
    refreshGUI();
  }

  /**
   * Removes the selected nodes.
   */
  public void removeSelectedNodes() {
  	if (getSelectedTab()==null) return;
  	ArrayList<LaunchNode> nodes = getSelectedTab().getSelectedNodes();
  	if (nodes == null) return;
  	LaunchNode toSelect = null; // parent of first (top) node removed
    for (LaunchNode node: nodes) {
	    if((node.getParent()==null) || node.getRoot()!=getRootNode()) {
	      continue;
	    }
	    LaunchNode parent = (LaunchNode) node.getParent();
	    if (toSelect == null)
	    	toSelect = parent;
	    getSelectedTab().treeModel.removeNodeFromParent(node);
	    if(parent.getOwner()!=null) {
	      changedFiles.add(parent.getOwner().getFileName());
	    } else {
	      changedFiles.add(tabSetName);
	    }
	    refreshClones(parent);
    }
    if (toSelect != null) {
		  getSelectedTab().setSelectedNode(toSelect);
	    refreshGUI();
    }
  }

  /**
   * Adds a child node to the selected node.
   *
   * @param child the child node to add
   */
  public void addChildToSelectedNode(LaunchNode child) {
    LaunchNode parent = getSelectedNode();
    if((parent!=null)&&(child!=null)) {
      LaunchNode[] nodes = child.getAllOwnedNodes();
      for(int i = 0; i<nodes.length; i++) {
        LaunchNode node = getSelectedTab().getClone(nodes[i]);
        if(node!=null) {
          getSelectedTab().setSelectedNode(node);
          JOptionPane.showMessageDialog(frame, LaunchRes.getString("Dialog.DuplicateNode.Message")+" \""+node+"\"", //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
            LaunchRes.getString("Dialog.DuplicateNode.Title"), //$NON-NLS-1$
              JOptionPane.WARNING_MESSAGE);
          return;
        }
      }
      getSelectedTab().treeModel.insertNodeInto(child, parent, parent.getChildCount());
      getSelectedTab().tree.scrollPathToVisible(new TreePath(child.getPath()));
      child.setLaunchClass(child.launchClassName);
      if(parent.getOwner()!=null) {
        changedFiles.add(parent.getOwner().getFileName());
      } else {
        changedFiles.add(tabSetName);
      }
      refreshClones(parent);
      refreshGUI();
    }
  }

  /**
   * Replaces clones of a specified node with new clones.
   *
   * @param node the current version of the node to clone
   */
  protected void refreshClones(LaunchNode node) {
    Map<LaunchPanel, LaunchNode> clones = getClones(node);
    replaceClones(node, clones);
  }

  /**
   * Replaces nodes with clones of the specified node.
   *
   * @param node the current version of the node to clone
   * @param clones the current clones to replace
   */
  protected void replaceClones(LaunchNode node, Map<LaunchPanel, LaunchNode> clones) {
    // find clones
    if(clones.isEmpty()) {
      return;
    }
    // replace clones
    XMLControl control = new XMLControlElement(node.getOwner());
    Iterator<LaunchPanel> it = clones.keySet().iterator();
    while(it.hasNext()) {
      LaunchPanel tab = it.next();
      LaunchNode clone = clones.get(tab);
      LaunchNode parent = (LaunchNode) clone.getParent();
      boolean expanded = tab.tree.isExpanded(new TreePath(clone.getPath()));
      if(parent!=null) {
        int index = parent.getIndex(clone);
        tab.treeModel.removeNodeFromParent(clone);
        clone = (LaunchNode) new XMLControlElement(control).loadObject(null);
        clone.setFileName(node.getFileName());
        tab.treeModel.insertNodeInto(clone, parent, index);
      } else {
        clone = (LaunchNode) new XMLControlElement(control).loadObject(null);
        clone.setFileName(node.getFileName());
        tab.treeModel.setRoot(clone);
      }
      if(expanded) {
        tab.tree.expandPath(new TreePath(clone.getPath()));
      }
    }
  }

  /**
   * Returns clones containing a specified node in a tab-to-node map.
   *
   * @param node the node
   * @return the tab-to-node map
   */
  protected Map<LaunchPanel, LaunchNode> getClones(LaunchNode node) {
    Map<LaunchPanel, LaunchNode> clones = new HashMap<LaunchPanel, LaunchNode>();
    // find clones
    node = node.getOwner();
    if(node==null) {
      return clones;
    }
    Component[] tabs = tabbedPane.getComponents();
    for(int i = 0; i<tabs.length; i++) {
      LaunchPanel tab = (LaunchPanel) tabs[i];
      LaunchNode clone = tab.getClone(node);
      if((clone!=null)&&(clone!=node)) {
        clones.put(tab, clone);
      }
    }
    return clones;
  }

  /**
   * Gets a file chooser for selecting jar files.
   *
   * @return the jar chooser
   */
  protected static JFileChooser getJARChooser() {
    getFileChooser().setFileFilter(jarFileFilter);
    return fileChooser;
  }

  /**
   * Gets a file chooser for selecting html files.
   *
   * @return the html chooser
   */
  public static JFileChooser getHTMLChooser() {
    getFileChooser().setFileFilter(htmlFileFilter);
    return fileChooser;
  }

  /**
   * Gets a file chooser for selecting pdf files.
   *
   * @return the pdf chooser
   */
  public static JFileChooser getPDFChooser() {
    getFileChooser().setFileFilter(pdfFileFilter);
    return fileChooser;
  }

  /**
   * Gets a file chooser.
   *
   * @return the file chooser
   */
  protected static JFileChooser getFileChooser() {
    if(fileChooser==null) {
      fileChooser = new JFileChooser(new File(OSPRuntime.chooserDir));
      allFileFilter = fileChooser.getFileFilter();
      // create jar file filter
      jarFileFilter = new javax.swing.filechooser.FileFilter() {
        // accept all directories and *.jar files.
        public boolean accept(File f) {
          if(f==null) {
            return false;
          }
          if(f.isDirectory()) {
            return true;
          }
          String extension = null;
          String name = f.getName();
          int i = name.lastIndexOf('.');
          if((i>0)&&(i<name.length()-1)) {
            extension = name.substring(i+1).toLowerCase();
          }
          if((extension!=null)&&extension.equals("jar")) {                 //$NON-NLS-1$
            return true;
          }
          return false;
        }
        // the description of this filter
        public String getDescription() {
          return LaunchRes.getString("FileChooser.JarFilter.Description"); //$NON-NLS-1$
        }

      };
      // create html file filter
      htmlFileFilter = new javax.swing.filechooser.FileFilter() {
        // accept all directories, *.htm and *.html files.
        public boolean accept(File f) {
          if(f==null) {
            return false;
          }
          if(f.isDirectory()) {
            return true;
          }
          String extension = null;
          String name = f.getName();
          int i = name.lastIndexOf('.');
          if((i>0)&&(i<name.length()-1)) {
            extension = name.substring(i+1).toLowerCase();
          }
          if((extension!=null)&&(extension.equals("htm")||extension.equals("html"))) { //$NON-NLS-1$//$NON-NLS-2$
            return true;
          }
          return false;
        }
        // the description of this filter
        public String getDescription() {
          return LaunchRes.getString("FileChooser.HTMLFilter.Description"); //$NON-NLS-1$
        }

      };
      // create html file filter
      pdfFileFilter = new javax.swing.filechooser.FileFilter() {
        // accept all directories and *.pdf files.
        public boolean accept(File f) {
          if (f==null) return false;
          if (f.isDirectory()) return true;
          String extension = XML.getExtension(f.getName());
          if(extension!=null && extension.equals("pdf")) return true; //$NON-NLS-1$
          return false;
        }
        // the description of this filter
        public String getDescription() {
          return LaunchRes.getString("FileChooser.PDFFilter.Description"); //$NON-NLS-1$
        }

      };
    }
    fileChooser.removeChoosableFileFilter(jarFileFilter);
    fileChooser.removeChoosableFileFilter(htmlFileFilter);
    fileChooser.setFileFilter(allFileFilter);
  	FontSizer.setFonts(fileChooser, FontSizer.getLevel());
    return fileChooser;
  }

  /**
   * Gets a file filter for selecting HTML files.
   *
   * @return the html file filter
   */
  public static javax.swing.filechooser.FileFilter getHTMLFilter() {
  	if (htmlFileFilter==null) {
  		getFileChooser();
  	}
    return htmlFileFilter;
  }

  /**
   * Gets a file filter for selecting PDF files.
   *
   * @return the pdf file filter
   */
  public static javax.swing.filechooser.FileFilter getPDFFilter() {
  	if (pdfFileFilter==null) {
  		getFileChooser();
  	}
    return pdfFileFilter;
  }

  /**
   * Handles a mouse pressed event.
   *
   * @param e the mouse event
   * @param tab the launch panel triggering the event
   */
  protected void handleMousePressed(MouseEvent e, final LaunchPanel tab) {
    super.handleMousePressed(e, tab);
    if(OSPRuntime.isPopupTrigger(e)) {
      TreePath path = tab.tree.getPathForLocation(e.getX(), e.getY());
      if(path==null) {
        return;
      }
      final LaunchNode node = getSelectedNode();
      if(node==null) {
        return;
      }
      // add items to popup
      String fileName = node.getFileName();
      if((fileName!=null)&&changedFiles.contains(fileName)) {
        // add saveNode item
        if(popup.getComponentCount()!=0) {
          popup.addSeparator();
        }
        popup.add(saveNodeItem);
      }
      // add saveNodeAs item
      if(popup.getComponentCount()!=0) {
        popup.addSeparator();
      }
      popup.add(saveNodeAsItem);
      // add openTab item
      if(!node.isRoot()) {
        popup.addSeparator();
        openTabItem.setText(LaunchRes.getString("Action.OpenTab")); //$NON-NLS-1$
        popup.add(openTabItem);
      }
      popup.show(tab, e.getX()+4, e.getY()+12);
    }
  }

  /**
   * Overrides Launcher exit method.
   */
  protected void exit() {
    OSPRuntime.setAuthorMode(false);
    if(!saveAllChanges()) {
      // change default close operation to prevent window closing
      final int op = frame.getDefaultCloseOperation();
      frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      // restore default close operation
      Runnable runner = new Runnable() {
        public void run() {
          frame.setDefaultCloseOperation(op);
        }

      };
      SwingUtilities.invokeLater(runner);
      return;
    }
    super.exit();
  }

  /**
   * Returns true if tabset is writable.
   *
   * @return true if writable
   */
  protected boolean isTabSetWritable() {
    String path = XML.getResolvedPath(tabSetName, tabSetBasePath);
    Resource res = ResourceLoader.getResource(path);
    File file = (res==null) ? null : res.getFile();
    boolean writable = (file==null) ? true : file.canWrite();
    if(!selfContained) {
      for(int i = 0; i<tabbedPane.getTabCount(); i++) {
        LaunchNode root = getTab(i).getRootNode();
        writable = writable&&isNodeWritable(root);
      }
    }
    return writable;
  }

  /**
   * Returns true if node is writable.
   *
   * @param node the node
   * @return true if writable
   */
  protected boolean isNodeWritable(LaunchNode node) {
    File file = node.getFile();
    boolean writable = (file==null) ? true : file.canWrite();
    // check node's owned child nodes if not self contained
    if(!node.isSelfContained()) {
      LaunchNode[] nodes = node.getChildOwnedNodes();
      for(int i = 0; i<nodes.length; i++) {
        writable = writable&&isNodeWritable(nodes[i]);
      }
    }
    return writable;
  }
  
  protected static class NodeSet {
  	ArrayList<Object[]> nodes;

  	private NodeSet(ArrayList<LaunchNode> list) {
  		nodes = new ArrayList<Object[]>();
  		for (LaunchNode next: list) {
  			Object[] data = new Object[] {next, next.getFileName()};
  			nodes.add(data);
  		}
  	}
  	
  	private NodeSet(XMLControl control) {
  		nodes = new ArrayList<Object[]>();
  		ArrayList<?> input  = (ArrayList<?>)control.getObject("nodes"); //$NON-NLS-1$
  		if (input != null) {
  			for (Object next: input) {
  				nodes.add((Object[])next);
  			}
  		}
  	}
  	
    /**
     * Returns an ObjectLoader to save and load data for this class.
     *
     * @return the object loader
     */
    public static XML.ObjectLoader getLoader() {
      return new Loader();
    }

    /**
     * A class to save and load data for this class.
     */
    static class Loader implements XML.ObjectLoader {

      /**
       * Saves an object's data to an XMLControl.
       *
       * @param control the control to save to
       * @param obj the object to save
       */
      public void saveObject(XMLControl control, Object obj) {
      	NodeSet nodeSet = (NodeSet)obj;
      	control.setValue("nodes", nodeSet.nodes); //$NON-NLS-1$
      }

      /**
       * Creates a new object.
       *
       * @param control the XMLControl with the object data
       * @return the newly created object
       */
      public Object createObject(XMLControl control) {
        return new NodeSet(control);
      }

      /**
       * Loads an object with data from an XMLControl.
       *
       * @param control the control
       * @param obj the object
       * @return the loaded object
       */
      public Object loadObject(XMLControl control, Object obj) {
        return obj;
      }
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

