/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import org.opensourcephysics.controls.ConsoleLevel;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.controls.XMLLoader;
import org.opensourcephysics.display.OSPLayout;
import org.opensourcephysics.display.OSPRuntime;

/**
 * This is a tree node that can describe and launch an application.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class LaunchNode extends DefaultMutableTreeNode {
  // static fields
  static final Level DEFAULT_LOG_LEVEL = ConsoleLevel.OUT_CONSOLE;
  // instance fields
  Object launchObj;                                            // object providing xml data when arg 0 is "this"
  String classPath;                                            // relative paths to jar files with launch/support classes
  String launchClassName;                                      // name of class to be launched
  Class<?> launchClass;                                        // class to be launched
  String[] args = new String[] {""};                           // args passed to main method when launching //$NON-NLS-1$
  boolean showLog = false;                                     // shows OSPLog when in single vm
  boolean clearLog = false;                                    // clears OSPLog if shown
  Level logLevel = DEFAULT_LOG_LEVEL;                          // OSPLog set to this level before log is shown
  boolean singleVM = false;                                    // launches in current vm
  boolean singleVMOff = false;                                 // allows child to override single vm of parent
  boolean hiddenWhenRoot = false;                              // hides node when at root position
  boolean buttonView = false;                                  // displays node in button view when root
  boolean singleton = false;                                   // allows single instance when in separate vm
  boolean singleApp = false;                                   // allows single app when in single vm
  boolean singleAppOff = false;                                // allows child to override single app of parent
  boolean hiddenInLauncher = false;                            // hides node in Launcher (not LaunchBuilder)
  String name = "";                                            // display name of this node //$NON-NLS-1$
  String description = "";                                     // description displayed if no url specified or found //$NON-NLS-1$
  String tooltip = "";                                         // tooltip for this node //$NON-NLS-1$
  String xsetName = "";                                        // name of xset to be loaded (splash) //$NON-NLS-1$
  String author = "";                                          // author of the curricular activity defined by this node //$NON-NLS-1$
  String keywords = "";                                        // key words associated with this node //$NON-NLS-1$
  String level = "";                                           // complexity level for this node //$NON-NLS-1$
  String languages = "";                                       // languages associated with this node //$NON-NLS-1$
  String comment = "";                                         // comments about this node //$NON-NLS-1$
  String appletWidth = "";                                     // preferred applet width //$NON-NLS-1$
  String appletHeight = "";                                    // preferred applet height //$NON-NLS-1$
  ArrayList<DisplayTab> tabData = new ArrayList<DisplayTab>(); // list of display tabs
  private String fileName;                                     // xml file name relative to tabset base
  Collection<Process> processes = new HashSet<Process>();      // processes launched by this node
  Collection<Frame> frames = new HashSet<Frame>();             // frames launched by this node
  Collection<Action> actions = new HashSet<Action>();          // terminateActions to be performed by this node
  Map<Runnable, Thread> threads = new HashMap<Runnable, Thread>();
  int launchCount = 0;
  LaunchPanel launchPanel;
  boolean selfContained;
  boolean parentSelfContained;
  boolean previewing;
  boolean saveHiddenNodes;
  boolean enabled = true;
  List<String> jars = new ArrayList<String>();
  List<String> pdf = new ArrayList<String>();
  // following used for undoable NavEdits
  int tabNumber = -1;                                          // current tab number, or -1 if no tabs
  int prevTabNumber = -1;                                      // previous tab number
  URL htmlURL;                                                 // current URL--may be null
  URL prevURL;                                                 // previous URL
  JScrollPane launchModelScroller;

  /**
   * Constructs a node with the specified name.
   *
   * @param name the name
   */
  public LaunchNode(String name) {
    setUserObject(this);
    if(name!=null) {
      this.name = name;
    }
  }

  /**
   * Signals that a launch thread for this node is about to start or end.
   *
   * @param starting true if the thread is starting
   */
  public void threadRunning(boolean starting) {
    launchCount += starting ? +1 : -1;
    launchCount = Math.max(0, launchCount);
    if(launchPanel!=null) {
      launchPanel.repaint();
    }
  }

  /**
   * Launches this node.
   */
  public void launch() {
    launch(null);
  }

  /**
   * Launches this node from the specified launch panel.
   *
   * @param tab the launch panel
   */
  public void launch(LaunchPanel tab) {
    if(!isLeaf()) {
      return;
    }
    launchPanel = tab;
    OSPRuntime.launchingInSingleVM = this.isSingleVM();
    Launcher.singleAppMode = this.isSingleApp();
    Launcher.classPath = getClassPath(); // in node-to-root order
    if(isShowLog()&&isSingleVM()) {
      OSPLog.setLevel(getLogLevel());
      OSPLog log = OSPLog.getOSPLog();
      if(isClearLog()) {
        log.clear();
      }
      log.setVisible(true);
    }
    setMinimumArgLength(1); // trim args if nec
    String arg0 = args[0];
    if(getLaunchClass()!=null) {
      if(arg0.equals("this")) {                  //$NON-NLS-1$
        Object launchObj = getLaunchObject();
        if(launchObj!=null) {
          // replace with xml from launch object, if any
          XMLControl control = new XMLControlElement(launchObj);
          args[0] = control.toXML();
        } else {
          args[0] = "";                          //$NON-NLS-1$
        }
      }
      if(args[0].equals("")&&(args.length==1)) { //$NON-NLS-1$
        Launcher.launch(getLaunchClass(), null, this);
      } else {
        Launcher.launch(getLaunchClass(), args, this);
      }
    }
    args[0] = arg0;
  }

  /**
   * Returns the nearest ancestor with a non-null file name.
   * This node is returned if it has a non-null file name.
   *
   * @return the file node
   */
  public LaunchNode getOwner() {
    if(fileName!=null) {
      return this;
    }
    if(getParent()!=null) {
      return((LaunchNode) getParent()).getOwner();
    }
    return null;
  }

  /**
   * Returns all descendents of this node with non-null file names.
   * This node is not included.
   *
   * @return an array of launch nodes
   */
  public LaunchNode[] getAllOwnedNodes() {
    Collection<LaunchNode> nodes = new ArrayList<LaunchNode>();
    Enumeration<?> e = breadthFirstEnumeration();
    while(e.hasMoreElements()) {
      LaunchNode next = (LaunchNode) e.nextElement();
      if((next.fileName!=null)&&(next!=this)) {
        nodes.add(next);
      }
    }
    return nodes.toArray(new LaunchNode[0]);
  }

  /**
   * Returns the nearest descendents of this node with non-null file names.
   * This node is not included.
   *
   * @return an array of launch nodes
   */
  public LaunchNode[] getChildOwnedNodes() {
    Collection<LaunchNode> nodes = new ArrayList<LaunchNode>();
    LaunchNode[] owned = getAllOwnedNodes();
    LaunchNode owner = getOwner();
    for(int i = 0; i<owned.length; i++) {
      LaunchNode next = ((LaunchNode) owned[i].getParent()).getOwner();
      if(next==owner) {
        nodes.add(owned[i]);
      }
    }
    return nodes.toArray(new LaunchNode[0]);
  }

  /**
   * Returns a string used as a display name for this node.
   *
   * @return the string name of this node
   */
  public String toString() {
    // return name, if any
    if((name!=null)&&!name.equals("")) { //$NON-NLS-1$
      return name;
    }
    // return launch class name, if any
    if(launchClassName!=null) {
      return XML.getExtension(launchClassName);
    }
    // return args[0] name, if any
    if(!args[0].equals("")) { //$NON-NLS-1$
      String name = args[0];
      name = XML.getName(name);
      return XML.stripExtension(name);
    }
    return ""; //$NON-NLS-1$
  }

  /**
   * Gets the unique ID string for this node.
   *
   * @return the ID string
   */
  public String getID() {
    return String.valueOf(this.hashCode());
  }

  /**
   * Sets the name of this node.
   *
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the name of this node.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the description of this node.
   *
   * @param desc the description
   */
  public void setDescription(String desc) {
    description = desc;
  }

  /**
   * Gets the description of this node.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the launch arguments of this node.
   *
   * @param args the arguments
   */
  public void setArgs(String[] args) {
    if((args!=null)&&(args.length>0)&&(args[0]!=null)) {
      this.args = args;
    }
  }

  /**
   * Gets the launch arguments of this node.
   *
   * @return the launch arguments
   */
  public String[] getArgs() {
    return args;
  }

  /**
   * Sets the tooltip of this node
   *
   * @param _tooltip the tooltip
   */
  public void setTooltip(String _tooltip) {
    tooltip = _tooltip;
  }

  /**
   * Gets the tooltip of this node.
   *
   * @return the tooltip
   */
  public String getTooltip() {
    return tooltip;
  }

  /**
   * Sets the author of this node
   *
   * @param _author the author
   */
  public void setAuthor(String _author) {
    author = _author;
  }

  /**
   * Gets the author.
   *
   * @return the first non-null author of this or an ancestor
   */
  public String getAuthor() {
    if(!author.equals("")) { //$NON-NLS-1$
      return author;
    } else if(isRoot()) {
      return "";             //$NON-NLS-1$
    }
    LaunchNode parent = (LaunchNode) getParent();
    return parent.getAuthor();
  }

  /**
   * Sets the keywords of this node
   *
   * @param _keywords the keywords
   */
  public void setKeyword(String _keywords) {
    keywords = _keywords;
  }

  /**
   * Gets the keywords of this node or ancestor.
   *
   * @return the keywords
   */
  public String getKeywords() {
    if(!keywords.equals("")) { //$NON-NLS-1$
      return keywords;
    } else if(isRoot()) {
      return "";               //$NON-NLS-1$
    }
    LaunchNode parent = (LaunchNode) getParent();
    return parent.getKeywords();
  }

  /**
   * Sets the comment of this node
   *
   * @param _comment the comment
   */
  public void setComment(String _comment) {
    comment = _comment;
  }

  /**
   * Gets the comment of this node.
   *
   * @return the comment
   */
  public String getComment() {
    return comment;
  }

  /**
   * Sets the preferred applet width of this node
   *
   * @param _width the width
   */
  public void setPreferredAppletWidth(String _width) {
    this.appletWidth = _width;
  }

  /**
   * Gets the preferred applet width of this node.
   *
   * @return the width
   */
  public String getPreferredAppletWidth() {
    return this.appletWidth;
  }

  /**
   * Sets the preferred applet height of this node
   *
   * @param _height the height
   */
  public void setPreferredAppletHeight(String _height) {
    this.appletHeight = _height;
  }

  /**
   * Gets the preferred applet height of this node.
   *
   * @return the height
   */
  public String getPreferredAppletHeight() {
    return this.appletHeight;
  }

  /**
   * Sets the course level of this node
   *
   * @param _level the level
   */
  public void setCourseLevel(String _level) {
    level = _level;
  }

  /**
   * Gets the course level of this node or ancestor.
   *
   * @return the level
   */
  public String getCourseLevel() {
    if(!level.equals("")) { //$NON-NLS-1$
      return level;
    } else if(isRoot()) {
      return "";            //$NON-NLS-1$
    }
    LaunchNode parent = (LaunchNode) getParent();
    return parent.getCourseLevel();
  }

  /**
   * Sets the languages for which translations exist for this node
   *
   * @param _lang the languages
   */
  public void setLanguages(String _lang) {
    languages = _lang;
  }

  /**
   * Gets the languages for which translations exist for this node or ancestor.
   *
   * @return the languages
   */
  public String getLanguages() {
    if(!languages.equals("")) { //$NON-NLS-1$
      return languages;
    } else if(isRoot()) {
      return "";                //$NON-NLS-1$
    }
    LaunchNode parent = (LaunchNode) getParent();
    return parent.getLanguages();
  }

  /**
   * Gets the complete class path in node-to-root order.
   * If Launcher is running from a jar, that jar is in every classpath.
   *
   * @return the class path
   */
  public String getClassPath() {
    String path = ""; //$NON-NLS-1$
    if(classPath!=null) {
      path += classPath;
    }
    LaunchNode node = this;
    while(!node.isRoot()) {
      node = (LaunchNode) node.getParent();
      if(node.classPath!=null) {
        if(!path.equals("")) { //$NON-NLS-1$
          path += ";";         //$NON-NLS-1$
        }
        path += node.classPath;
      }
    }
    if(!path.equals("")) {       //$NON-NLS-1$
      // eliminate duplicate jars
      jars.clear();
      String next = path;
      int i = path.indexOf(";"); //$NON-NLS-1$
      if(i==-1) {
        i = path.indexOf(":");   //$NON-NLS-1$
      }
      if(i!=-1) {
        next = path.substring(0, i);
        path = path.substring(i+1);
      } else {
        path = "";               //$NON-NLS-1$
      }
      // iterate thru path and add each unique jar to jars list
      while(next.length()>0) {
        if(!jars.contains(next)) {
          jars.add(next);
        }
        i = path.indexOf(";");   //$NON-NLS-1$
        if(i==-1) {
          i = path.indexOf(":"); //$NON-NLS-1$
        }
        if(i==-1) {
          next = path.trim();
          path = "";             //$NON-NLS-1$
        } else {
          next = path.substring(0, i).trim();
          path = path.substring(i+1).trim();
        }
      }
      // reconstruct clean path using semicolon delimiters
      Iterator<String> it = jars.iterator();
      while(it.hasNext()) {
        if(!path.equals("")) {   //$NON-NLS-1$
          path += ";";           //$NON-NLS-1$
        }
        path += it.next();
      }
    }
    // add ResourceLoader.launchJarName, if any, unless base directory path has been set
    if(OSPRuntime.getLaunchJarName()!=null
    		&& path.indexOf(OSPRuntime.getLaunchJarName())==-1
    		&& LaunchClassChooser.baseDirectoryPath==null) {
      if(!path.equals("")) { //$NON-NLS-1$
        path += ";";         //$NON-NLS-1$
      }
      path += OSPRuntime.getLaunchJarName();
    }
    return path;
  }

  /**
   * Sets the class path (relative jar or directory paths separated by colons or semicolons).
   *
   * @param jarNames the class path
   */
  public void setClassPath(String jarNames) {
    if((jarNames==null)||jarNames.equals("")) { //$NON-NLS-1$
      classPath = null;
      return;
    }
    // eliminate extra colons and semicolons
    while(jarNames.startsWith(":")||jarNames.startsWith(";")) { //$NON-NLS-1$ //$NON-NLS-2$
      jarNames = jarNames.substring(1);
    }
    while(jarNames.endsWith(":")||jarNames.endsWith(";")) { //$NON-NLS-1$ //$NON-NLS-2$
      jarNames = jarNames.substring(0, jarNames.length()-1);
    }
    String s = jarNames;
    int i = jarNames.indexOf(";;"); //$NON-NLS-1$
    if(i==-1) {
      i = jarNames.indexOf("::"); //$NON-NLS-1$
    }
    if(i==-1) {
      i = jarNames.indexOf(":;"); //$NON-NLS-1$
    }
    if(i==-1) {
      i = jarNames.indexOf(";:"); //$NON-NLS-1$
    }
    while(i>-1) {
      jarNames = jarNames.substring(0, i+1)+s.substring(i+2);
      s = jarNames;
      i = jarNames.indexOf(";;");   //$NON-NLS-1$
      if(i==-1) {
        i = jarNames.indexOf("::"); //$NON-NLS-1$
      }
      if(i==-1) {
        i = jarNames.indexOf(":;"); //$NON-NLS-1$
      }
      if(i==-1) {
        i = jarNames.indexOf(";:"); //$NON-NLS-1$
      }
    }
    classPath = jarNames;
  }

  /**
   * Returns the name of the class to launch
   * @return the class name
   */
  public String getLaunchClassName() { return this.launchClassName; }
  
  /**
   * Sets the launch class for this node.
   *
   * @param className the name of the class
   * @return true if the class was successfully loaded for the first time
   */
  public boolean setLaunchClass(String className) {
    if(className==null) {
      return false;
    }
    if((launchClassName==className)&&(launchClass!=null)) {
      return false;
    }
    launchModelScroller = null;
    launchClassName = className;
    launchClass = LaunchClassChooser.getClass(getClassPath(), className);
    OSPLog.finest("node "+getName()+": "+LaunchRes.getString("Log.Message.SetLaunchClass") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    		+" "+className+(launchClass==null? " (not found!)": "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    return launchClass!=null;
  }

  /**
   * Gets the launch class for this node.
   *
   * @return the launch class
   */
  public Class<?> getLaunchClass() {
    if((launchClass==null)&&(launchClassName!=null)&&!launchClassName.equals("")) { //$NON-NLS-1$
      setLaunchClass(launchClassName);
    }
    return launchClass;
  }

  /**
   * Gets the launch object. May be null.
   *
   * @return the launch object
   */
  public Object getLaunchObject() {
    if(launchObj!=null) {
      return launchObj;
    } else if(isRoot()) {
      return null;
    }
    LaunchNode node = (LaunchNode) getParent();
    return node.getLaunchObject();
  }

  /**
   * Sets the launch object.
   *
   * @param obj the launch object
   */
  public void setLaunchObject(Object obj) {
    launchObj = obj;
  }

  /**
   * Sets the specified display tab title and url path.
   * Specifying a tab number greater than the current tab count adds a new tab.
   * Setting a path to null removes the tab.
   *
   * @param n the tab number
   * @param title the tab title
   * @param path the path.
   * @param args model arguments. May be null.
   * @return the DisplayTab, or null if unsuccessful
   */
  public DisplayTab setDisplayTab(int n, String title, String path, String[] args) {
    if(n>=tabData.size()) {
      // add a new tab
      return addDisplayTab(title, path, args);
    } else if((path==null)||path.equals("")) {                           //$NON-NLS-1$
      // remove the tab
      return removeDisplayTab(n);
    } else {
      // change the tab
      DisplayTab tab = tabData.get(n);
      tab.title = title;
      tab.setPath(path);
      tab.setModelArgs(args);
      OSPLog.finest("tab "+n+" changed: [\""+title+"\", \""+path+"\"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      return tab;
    }
  }

  /**
   * Adds a display tab with the specified title and relative path.
   * No tab is added if path is null or "".
   *
   * @param title the tab title. May be null.
   * @param path the path.
   * @param args model arguments. May be null.
   * @return the newly created DisplayTab, if any
   */
  public DisplayTab addDisplayTab(String title, String path, String[] args) {
    if((path==null)||path.equals("")) {  //$NON-NLS-1$
      return null; 
    }
    // assemble DisplayTab and add to tabData
    DisplayTab tab = new DisplayTab(title, path);
    tab.setModelArgs(args);
    tabData.add(tab);
    OSPLog.finest("tab added: [\""+title+"\", \""+path+"\"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    return tab;
  }

  /**
   * Inserts a display tab with the specified title and relative path.
   * No tab is inserted if path is null or "", or if the specified tab index
   * is invalid.
   *
   * @param n the insertion index
   * @param title the tab title. May be null.
   * @param path the path.
   * @param args model arguments. May be null.
   * @return the newly created DisplayTab, if any
   */
  public DisplayTab insertDisplayTab(int n, String title, String path, String[] args) {
    if((path==null)||path.equals("")||(n>=tabData.size())) { //$NON-NLS-1$
      return null; 
    }
    // assemble tab and insert into tabData
    DisplayTab tab = new DisplayTab(title, path);
    tab.setModelArgs(args);
    tabData.add(n, tab);
    OSPLog.finest("tab inserted: [\""+title+"\", \""+path+"\"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    return tab;
  }

  /**
   * Removes a display tab.
   *
   * @param n the tab number
   * @return the DisplayTab removed, or null if none removed
   */
  public DisplayTab removeDisplayTab(int n) {
    DisplayTab tab = getDisplayTab(n);
    if(tab!=null) {
      tabData.remove(tab);
      OSPLog.finest("tab "+n+" removed: [\""+ //$NON-NLS-1$ //$NON-NLS-2$
        tab.title+"\", \""+tab.path+"\"]");   //$NON-NLS-1$ //$NON-NLS-2$
    }
    return tab;
  }

  /**
   * Gets a display tab.
   *
   * @param n the tab number
   * @return the DisplayTab, or null if none found
   */
  public DisplayTab getDisplayTab(int n) {
	// n<0 added by W. Christian
    if(n<0 || n>=tabData.size()) {
      return null;
    }
    return tabData.get(n);
  }

  /**
   * Returns the number of display tabs for this node.
   * @return the tab count
   */
  public int getDisplayTabCount() {
    return tabData.size();
  }

  /**
   * Gets the list of pdf documents associated with this node.
   * @return a list of PDF documents (may be empty)
   */
  public List<String> getPDFPaths() {
  	int n = getDisplayTabCount();
  	pdf.clear();
  	for (int i = 0; i<n; i++) {
  		String path = getDisplayTab(i).getPath();
  		if (path.toLowerCase().endsWith(".pdf")) { //$NON-NLS-1$
  			pdf.add(path);
  		}
  	}
    return pdf;
  }

  /**
   * Gets the fileName.
   *
   * @return the fileName, assumed to be a relative path
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Gets the string path to this node, starting from the root.
   * This returns the names of the nodes in the path, separated by "/".
   *
   * @return the path
   */
  public String getPathString() {
    TreeNode[] nodes = getPath();
    LaunchNode next = (LaunchNode) nodes[0];
    StringBuffer path = new StringBuffer(next.name);
    for(int i = 1; i<nodes.length; i++) {
      next = (LaunchNode) nodes[i];
      path.append("/"+next.name); //$NON-NLS-1$
    }
    return path.toString();
  }

  /**
   * Sets the fileName. Accepts relative paths or
   * will convert absolute paths to relative.
   *
   * @param path the path to the file
   * @return the file name
   */
  public String setFileName(String path) {
    if(path==null) {
      fileName = null;
    } else {
      fileName = XML.getPathRelativeTo(path, Launcher.tabSetBasePath);
    }
    return fileName;
  }

  /**
   * Gets the parentSelfContained flag.
   *
   * @return true if parentSelfContained is true for this or an ancestor
   */
  public boolean isParentSelfContained() {
    if(parentSelfContained) {
      return true;
    } else if(isRoot()) {
      return false;
    }
    LaunchNode parent = (LaunchNode) getParent();
    return parent.isSelfContained();
  }

  /**
   * Gets the selfContained flag.
   *
   * @return true if selfContained is true for this or an ancestor
   */
  public boolean isSelfContained() {
    if(selfContained||isParentSelfContained()) {
      return true;
    }
    return false;
  }

  /**
   * Gets the previewing flag.
   *
   * @return true if previewing is true for this or an ancestor
   */
  public boolean isPreviewing() {
    if(previewing) {
      return true;
    } else if(isRoot()) {
      return false;
    }
    LaunchNode node = (LaunchNode) getParent();
    return node.isPreviewing();
  }

  /**
   * Gets the saveHiddenNodes flag.
   *
   * @return true if saveHiddenNodes is true for this or an ancestor
   */
  public boolean isSavingHiddenNodes() {
    if(saveHiddenNodes) {
      return true;
    } else if(isRoot()) {
      return false;
    }
    LaunchNode node = (LaunchNode) getParent();
    return node.isSavingHiddenNodes();
  }

  /**
   * Sets the selfContained flag.
   *
   * @param selfContained true if self contained
   */
  public void setSelfContained(boolean selfContained) {
    this.selfContained = selfContained;
  }

  /**
   * Gets the singleVM flag.
   *
   * @return true if singleVM is true for this or an ancestor
   */
  public boolean isSingleVM() {
    if(singleVM||org.opensourcephysics.display.OSPRuntime.isWebStart()) {
      return true;
    } else if(isRoot()) {
      return false;
    }
    LaunchNode parent = (LaunchNode) getParent();
    return singleVMOff ? false : parent.isSingleVM();
  }

  /**
   * Sets the single VM flag.
   *
   * @param singleVM true if single vm
   */
  public void setSingleVM(boolean singleVM) {
    this.singleVM = singleVM;
  }

  /**
   * Gets the showLog value.
   *
   * @return true if showLog is true for this or an ancestor
   */
  public boolean isShowLog() {
    if(showLog) {
      return true;
    } else if(isRoot()) {
      return false;
    }
    LaunchNode parent = (LaunchNode) getParent();
    return parent.isShowLog();
  }

  /**
   * Sets the showLog flag.
   *
   * @param show true to show the OSPLog (single vm only)
   */
  public void setShowLog(boolean show) {
    showLog = show;
  }

  /**
   * Gets the clearLog value.
   *
   * @return true if clearLog is true for this or an ancestor
   */
  public boolean isClearLog() {
    if(clearLog) {
      return true;
    } else if(isRoot()) {
      return false;
    }
    LaunchNode parent = (LaunchNode) getParent();
    return parent.isClearLog();
  }

  /**
   * Sets the showLog flag.
   *
   * @param clear true to clear the OSPLog (single vm only)
   */
  public void setClearLog(boolean clear) {
    clearLog = clear;
  }

  /**
   * Gets the log level.
   *
   * @return the level
   */
  public Level getLogLevel() {
    if(isRoot()) {
      return logLevel;
    }
    LaunchNode parent = (LaunchNode) getParent();
    if(parent.isShowLog()) {
      Level parentLevel = parent.getLogLevel();
      if(parentLevel.intValue()<logLevel.intValue()) {
        return parentLevel;
      }
    }
    return logLevel;
  }

  /**
   * Sets the log level.
   *
   * @param level the level
   */
  public void setLogLevel(Level level) {
    if(level!=null) {
      logLevel = level;
    }
  }

  /**
   * Gets the singleApp value.
   *
   * @return true if singleApp is true for this or an ancestor
   */
  public boolean isSingleApp() {
    if(singleApp) {
      return true;
    } else if(isRoot()) {
      return false;
    }
    LaunchNode parent = (LaunchNode) getParent();
    return singleAppOff ? false : parent.isSingleApp();
  }

  /**
   * Sets the singleApp flag.
   *
   * @param singleApp true to close other apps when launching new app (single vm only)
   */
  public void setSingleApp(boolean singleApp) {
    this.singleApp = singleApp;
  }

  /**
   * Sets the hiddenWhenRoot flag.
   *
   * @param hide true to hide node when at root
   */
  public void setHiddenWhenRoot(boolean hide) {
    hiddenWhenRoot = hide;
  }

  /**
   * Gets the buttonView value.
   *
   * @return true if buttonView is true for this or an ancestor
   */
  public boolean isButtonView() {
    if(buttonView) {
      return true;
    } else if(isRoot()) {
      return false;
    }
    LaunchNode parent = (LaunchNode) getParent();
    return parent.isButtonView();
  }

  /**
   * Sets the buttonView flag.
   *
   * @param buttonView true to display in buttonView
   */
  public void setButtonView(boolean buttonView) {
    LaunchNode root = (LaunchNode) getRoot();
    root.buttonView = buttonView;
  }

  /**
   * Gets the singleton value.
   *
   * @return true if singleApp is true for this or an ancestor
   */
  public boolean isSingleton() {
    if(singleton) {
      return true;
    } else if(isRoot()) {
      return false;
    }
    LaunchNode parent = (LaunchNode) getParent();
    return parent.isSingleton();
  }

  /**
   * Sets the singleton flag.
   *
   * @param singleton true to allow single instance when in separate vm
   */
  public void setSingleton(boolean singleton) {
    this.singleton = singleton;
  }

  /**
   * Gets the hiddenInLauncher value.
   *
   * @return true if hiddenInLauncher is true for this or an ancestor
   */
  public boolean isHiddenInLauncher() {
    if(hiddenInLauncher) {
      return true;
    } else if(isRoot()) {
      return false;
    }
    LaunchNode parent = (LaunchNode) getParent();
    return parent.isHiddenInLauncher();
  }

  /**
   * Sets the hiddenInLauncher flag.
   *
   * @param hide true to hide this node in Launcher
   */
  public void setHiddenInLauncher(boolean hide) {
    hiddenInLauncher = hide;
  }

  /**
   * Gets the resource, if any, for this node
   *
   * @return the resource
   */
  public Resource getResource() {
    if(fileName==null) {
      return null;
    }
    String path = XML.getResolvedPath(fileName, Launcher.tabSetBasePath);
    return ResourceLoader.getResource(path);
  }

  /**
   * Determines whether a resource exists for this node.
   *
   * @return true if a resource exists
   */
  public boolean exists() {
    return(getResource()!=null);
  }

  /**
   * Return an existing file with current file name and specified base.
   * May return null.
   *
   * @return existing file, or null
   */
  public File getFile() {
    if(exists()) {
      return getResource().getFile();
    }
    return null;
  }

  /**
   * Determines if this node matches another node.
   *
   * @param node the node to match
   * @return true if the nodes match
   */
  public boolean matches(LaunchNode node) {
    if(node==null) {
      return false;
    }
    boolean match = (showLog==node.showLog)&&(clearLog==node.clearLog)&&(singleton==node.singleton)&&(singleVM==node.singleVM)&&(hiddenWhenRoot==node.hiddenWhenRoot)&&name.equals(node.name)&&description.equals(node.description)&&args[0].equals(node.args[0])&&(((fileName==null)&&(node.fileName==null))||((fileName!=null)&&fileName.equals(node.fileName)))&&(((getLaunchClass()==null)&&(node.getLaunchClass()==null))||((getLaunchClass()!=null)&&getLaunchClass().equals(node.getLaunchClass())))&&(((classPath==null)&&(node.classPath==null))||((classPath!=null)&&classPath.equals(node.classPath)));
    return match;
  }

  /**
   * Gets a child node specified by fileName.
   *
   * @param childFileName the file name of the child
   * @return the first child found, or null
   */
  public LaunchNode getChildNode(String childFileName) {
    Enumeration<?> e = breadthFirstEnumeration();
    while(e.hasMoreElements()) {
      LaunchNode next = (LaunchNode) e.nextElement();
      if(childFileName.equals(next.fileName)) {
        return next;
      }
    }
    return null;
  }

  /**
   * Adds menu item to a JPopupMenu or JMenu.
   *
   * @param menu the menu
   */
  public void addMenuItemsTo(final JComponent menu) {
    Enumeration<?> e = children();
    while(e.hasMoreElements()) {
      LaunchNode child = (LaunchNode) e.nextElement();
      if(child.isLeaf()) {
        JMenuItem item = new JMenuItem(child.toString());
        menu.add(item);
        item.setToolTipText(child.tooltip);
        item.setActionCommand(child.getID());
        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String id = e.getActionCommand();
            LaunchNode root = (LaunchNode) LaunchNode.this.getRoot();
            Enumeration<?> e2 = root.postorderEnumeration();
            while(e2.hasMoreElements()) {
              LaunchNode node = (LaunchNode) e2.nextElement();
              if(node.getID().equals(id)) {
                node.launch();
                break;
              }
            }
          }

        });
      } else {
        JMenu item = new JMenu(child.toString());
        menu.add(item);
        child.addMenuItemsTo(item);
      }
    }
  }

  /**
   * Adds an action to this node's actions collection.
   *
   * @param action the action to add
   */
  public void addTerminateAction(Action action) {
    actions.add(action);
    launchCount++;
  }

  /**
   * Removes an action from this node's actions collection.
   *
   * @param action the action to remove
   */
  public void removeTerminateAction(Action action) {
    actions.remove(action);
    launchCount = Math.max(0, --launchCount);
  }

  /**
   * Removes an action from this node's actions collection.
   *
   * @param action the action to remove
   */
  public void terminate(Action action) {
    if(actions.contains(action)) {
      removeTerminateAction(action);
      if(launchPanel!=null) {
        launchPanel.repaint();
      }
    }
  }

  /**
   * Terminates all apps launched by this node.
   */
  public void terminateAll() {
    for(Iterator<Process> it = processes.iterator(); it.hasNext(); ) {
      Process proc = it.next();
      proc.destroy();
    }
    for(Iterator<Frame> it = frames.iterator(); it.hasNext(); ) {
      Frame frame = it.next();
      WindowListener[] listeners = frame.getWindowListeners();
      for(int j = 0; j<listeners.length; j++) {
        if(listeners[j] instanceof Launcher.FrameCloser) {
          frame.removeWindowListener(listeners[j]);
        }
      }
      frame.dispose();
    }
    for(Iterator<Thread> it = threads.values().iterator(); it.hasNext(); ) {
      Thread thread = it.next();
      if(thread!=null) {
        thread.interrupt();
      }
    }
    Collection<Action> allActions = new HashSet<Action>(actions);
    for(Iterator<Action> it = allActions.iterator(); it.hasNext(); ) {
      Action action = it.next();
      if(action!=null) {
        action.actionPerformed(null);
      }
    }
    processes.clear();
    frames.clear();
    threads.clear();
    actions.clear();
    launchCount = 0;
  }

  /**
   * Gets the launchModelScroller. May return null.
   *
   * @return the scroller containing the modelPane from the launch class
   */
  protected JScrollPane getLaunchModelScroller() {
    if(launchModelScroller==null) {
      final JComponent content = Launcher.getModelPane(getLaunchClass(), getArgs());
      if(content==null) {
        return null;
      }
      JPanel panel = new JPanel(new OSPLayout()) {
        public Dimension getPreferredSize() {
          Dimension dim = content.getPreferredSize();
          dim.width += 8;
          dim.height += 8;
          return dim;
        }

      };
      panel.setBackground(java.awt.Color.white);
      panel.add(content, OSPLayout.CENTERED);
      launchModelScroller = new JScrollPane(panel);
    }
    return launchModelScroller;
  }

  /**
   * Returns the XML.ObjectLoader for this class.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load LaunchNode data in an XMLControl.
   */
  private static class Loader extends XMLLoader {
    public void saveObject(XMLControl control, Object obj) {
      LaunchNode node = (LaunchNode) obj;
      node.setMinimumArgLength(1); // trim args if nec
      if(!node.name.equals("")) {            //$NON-NLS-1$
        control.setValue("name", node.name); //$NON-NLS-1$
      }
      if(!node.description.equals("")) {                   //$NON-NLS-1$
        control.setValue("description", node.description); //$NON-NLS-1$
      }
      if(!node.tooltip.equals("")) {               //$NON-NLS-1$
        control.setValue("tooltip", node.tooltip); //$NON-NLS-1$
      }
      if(!node.xsetName.equals("")) {                 //$NON-NLS-1$
        control.setValue("launchset", node.xsetName); //$NON-NLS-1$
      }
      control.setValue("display_tabs", node.getDisplayData()); //$NON-NLS-1$
      control.setValue("display_args", node.getDisplayArgs()); //$NON-NLS-1$
      if(node.getLaunchClass()!=null) {
        control.setValue("launch_class", node.getLaunchClass().getName()); //$NON-NLS-1$
      } else if(node.launchClassName!=null) {
        control.setValue("launch_class", node.launchClassName);            //$NON-NLS-1$
      }
      if(!node.args[0].equals("")||(node.args.length>1)) { //$NON-NLS-1$
        control.setValue("launch_args", node.args);        //$NON-NLS-1$
      }
      if((node.classPath!=null)&&!node.classPath.equals("")) { //$NON-NLS-1$
        control.setValue("classpath", node.classPath);         //$NON-NLS-1$
      }
      if(node.hiddenWhenRoot) {
        control.setValue("root_hidden", true); //$NON-NLS-1$
      }
      if(node.buttonView) {
        control.setValue("button_view", true); //$NON-NLS-1$
      }
      if(node.singleton) {
        control.setValue("singleton", true); //$NON-NLS-1$
      }
      if(node.singleVM) {
        control.setValue("single_vm", true); //$NON-NLS-1$
      }
      if(node.singleVMOff) {
        control.setValue("single_vm_off", true); //$NON-NLS-1$
      }
      if(node.showLog) {
        control.setValue("show_log", true); //$NON-NLS-1$
      }
      if(node.logLevel!=DEFAULT_LOG_LEVEL) {
        control.setValue("log_level", node.logLevel.getName()); //$NON-NLS-1$
      }
      if(node.clearLog) {
        control.setValue("clear_log", true); //$NON-NLS-1$
      }
      if(node.singleApp) {
        control.setValue("single_app", true); //$NON-NLS-1$
      }
      if(node.singleAppOff) {
        control.setValue("single_app_off", true); //$NON-NLS-1$
      }
      if(node.hiddenInLauncher) {
        control.setValue("hidden_in_launcher", true); //$NON-NLS-1$
      }
      if(!node.author.equals("")) {              //$NON-NLS-1$
        control.setValue("author", node.author); //$NON-NLS-1$
      }
      if(!node.keywords.equals("")) {                //$NON-NLS-1$
        control.setValue("keywords", node.keywords); //$NON-NLS-1$
      }
      if(!node.level.equals("")) {             //$NON-NLS-1$
        control.setValue("level", node.level); //$NON-NLS-1$
      }
      if(!node.languages.equals("")) {                 //$NON-NLS-1$
        control.setValue("languages", node.languages); //$NON-NLS-1$
      }
      if(!node.comment.equals("")) {               //$NON-NLS-1$
        control.setValue("comment", node.comment); //$NON-NLS-1$
      }
      if(!node.appletWidth.equals("")) {                    //$NON-NLS-1$
        control.setValue("applet_width", node.appletWidth); //$NON-NLS-1$
      }
      if(!node.appletHeight.equals("")) {                     //$NON-NLS-1$
        control.setValue("applet_height", node.appletHeight); //$NON-NLS-1$
      }
      //      Dimension dim = EjsTool.getEjsAppletDimension(node.getLaunchClass());
      //      if(dim!=null) {
      //        control.setValue("applet_width", dim.width); //$NON-NLS-1$
      //        control.setValue("applet_height", dim.height); //$NON-NLS-1$
      //      }
      if(node.children!=null) {
        // list the children and save the list
        ArrayList<Serializable> children = new ArrayList<Serializable>();
        Enumeration<?> e = node.children();
        boolean saveAll = node.isSavingHiddenNodes();
        while(e.hasMoreElements()) {
          LaunchNode child = (LaunchNode) e.nextElement();
          if(!saveAll&&child.isHiddenInLauncher()) {
            continue;
          }
          if(node.isPreviewing()) {
            children.add(child);
          } else if((child.fileName!=null)&&!node.isSelfContained()) {
            children.add(child.fileName);
          } else {
            child.fileName = null;
            child.setSelfContained(false);
            children.add(child);
          }
        }
        if(children.size()>0) {
          control.setValue("child_nodes", children); //$NON-NLS-1$
        }
      }
    }

    public Object createObject(XMLControl control) {
      String name = control.getString("name"); //$NON-NLS-1$
      if(name==null) {
        name = LaunchRes.getString("NewNode.Name"); //$NON-NLS-1$
      }
      return new LaunchNode(name);
    }

    public Object loadObject(XMLControl control, Object obj) {
      LaunchNode node = (LaunchNode) obj;
      String name = control.getString("name"); //$NON-NLS-1$
      if(name!=null) {
        node.name = name;
      }
      String description = control.getString("description"); //$NON-NLS-1$
      if(description!=null) {
        node.description = description;
      }
      String tooltip = control.getString("tooltip"); //$NON-NLS-1$
      if(tooltip!=null) {
        node.tooltip = tooltip;
      }
      String xsetName = control.getString("launchset"); //$NON-NLS-1$
      if(xsetName!=null) {
        node.xsetName = xsetName;
      }
      // read legacy xml files with "url" properties
      String url = control.getString("url"); //$NON-NLS-1$
      if(url!=null) {
        node.setDisplayData(new String[][] {
          {null, url}
        });
      } else {
        if(control.getPropertyNames().contains("html")) {                      //$NON-NLS-1$ pre-September 2009 files
          node.setDisplayData((String[][]) control.getObject("html"));         //$NON-NLS-1$
        } else {                                                               // post-September 2009 files
          node.setDisplayData((String[][]) control.getObject("display_tabs")); //$NON-NLS-1$
        }
        node.setDisplayArgs((String[][]) control.getObject("display_args"));   //$NON-NLS-1$
      }
      node.setClassPath(control.getString("classpath")); //$NON-NLS-1$
      String className = control.getString("launch_class"); //$NON-NLS-1$
      if(className!=null) {
        node.launchClassName = className;
        node.launchModelScroller = null;
      }
      String[] args = (String[]) control.getObject("launch_args"); //$NON-NLS-1$
      if(args!=null) {
        node.setArgs(args);
      }
      node.hiddenWhenRoot = control.getBoolean("root_hidden");          //$NON-NLS-1$
      node.buttonView = control.getBoolean("button_view");              //$NON-NLS-1$
      node.singleton = control.getBoolean("singleton");                 //$NON-NLS-1$
      node.singleVM = control.getBoolean("single_vm");                  //$NON-NLS-1$
      node.singleVMOff = control.getBoolean("single_vm_off");           //$NON-NLS-1$
      node.showLog = control.getBoolean("show_log");                    //$NON-NLS-1$
      node.clearLog = control.getBoolean("clear_log");                  //$NON-NLS-1$
      node.singleApp = control.getBoolean("single_app");                //$NON-NLS-1$
      node.singleAppOff = control.getBoolean("single_app_off");         //$NON-NLS-1$
      node.hiddenInLauncher = control.getBoolean("hidden_in_launcher"); //$NON-NLS-1$
      Level logLevel = OSPLog.parseLevel(control.getString("log_level")); //$NON-NLS-1$
      if(logLevel!=null) {
        node.logLevel = logLevel;
      }
      // meta items should not be set to null
      String author = control.getString("author"); //$NON-NLS-1$
      if(author!=null) {
        node.author = author;
      }
      String keywords = control.getString("keywords"); //$NON-NLS-1$
      if(keywords!=null) {
        node.keywords = keywords;
      }
      String level = control.getString("level"); //$NON-NLS-1$
      if(level!=null) {
        node.level = level;
      }
      String lang = control.getString("languages"); //$NON-NLS-1$
      if(lang!=null) {
        node.languages = lang;
      }
      String comment = control.getString("comment"); //$NON-NLS-1$
      if(comment!=null) {
        node.comment = comment;
      }
      String width = control.getString("applet_width"); //$NON-NLS-1$
      if(width!=null) {
        node.appletWidth = width;
      }
      String height = control.getString("applet_height"); //$NON-NLS-1$
      if(height!=null) {
        node.appletHeight = height;
      }
      name = control.getString("filename"); //$NON-NLS-1$
      if(name!=null) {
        node.setFileName(name);
      }
      Collection<?> children = (ArrayList<?>) control.getObject("child_nodes"); //$NON-NLS-1$
      if(children!=null) {
        node.removeAllChildren();
        Iterator<?> it = children.iterator();
        while(it.hasNext()) {
          Object next = it.next();
          if(next instanceof LaunchNode) {                                                                                                                                                            // child node
            // add the child node
            LaunchNode child = (LaunchNode) next;
            node.add(child);
            child.setLaunchClass(child.launchClassName);
          } else if(next instanceof String) {                                                                                                                                                         // file name
            String fileName = (String) next;
            String path = XML.getResolvedPath(fileName, Launcher.tabSetBasePath);
            // last path added is first path searched
            ResourceLoader.addSearchPath(Launcher.resourcesPath);
            ResourceLoader.addSearchPath(Launcher.tabSetBasePath);
            // open the file in an xml control
            XMLControlElement childControl = new XMLControlElement();
            String absolutePath = childControl.read(path);
            if(childControl.failedToRead()) {
              JOptionPane.showMessageDialog(null, LaunchRes.getString("Dialog.InvalidXML.Message")+" \""+fileName+"\"", LaunchRes.getString("Dialog.InvalidXML.Title"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
            // check root to prevent circular references
            LaunchNode root = (LaunchNode) node.getRoot();
            if((root.getChildNode(fileName)!=null)||fileName.equals(root.fileName)) {
              continue;
            }
            Class<?> type = childControl.getObjectClass();
            if(LaunchNode.class.isAssignableFrom(type)) {
              // first add the child with just the fileName
              // this allows descendants to get the root
              LaunchNode child = new LaunchNode(LaunchRes.getString("NewNode.Name"));      //$NON-NLS-1$
              child.setFileName(fileName);
              OSPLog.finest(LaunchRes.getString("Log.Message.Loading")+": "+absolutePath); //$NON-NLS-1$ //$NON-NLS-2$
              node.add(child);
              // then load the child with data
              childControl.loadObject(child);
            }
          }
        }
      }
      return node;
    }

  }

  /**
   * Gets the display tab data.
   *
   * @return an array of String[2] {title, path}, one for each tab
   */
  private String[][] getDisplayData() {
    if(tabData.isEmpty()) {
      return null;
    }
    String[][] data = new String[tabData.size()][2];
    for(int i = 0; i<tabData.size(); i++) {
      DisplayTab tab = tabData.get(i);
      data[i] = new String[] {tab.title, tab.path};
    }
    return data;
  }

  /**
   * Sets the display tab data.
   *
   * @param data an array of String[2] {title, path}, one for each tab
   */
  private void setDisplayData(String[][] data) {
    if(data==null) {
      return;
    }
    tabData.clear();
    for(int i = 0; i<data.length; i++) {
      DisplayTab tab = new DisplayTab(data[i][0], data[i][1]);
      tabData.add(tab);
    }
  }

  /**
   * Gets the arguments for display tab models.
   *
   * @return an array of String[] arguments, one for each tab
   */
  private String[][] getDisplayArgs() {
    if(tabData.isEmpty()) {
      return null;
    }
    String[][] data = new String[tabData.size()][];
    for(int i = 0; i<tabData.size(); i++) {
      DisplayTab tab = tabData.get(i);
      String[] args = tab.getModelArgs();
      data[i] = (args.length==0) ? null : args;
    }
    return data;
  }

  /**
   * Sets the arguments for display tab models.
   *
   * @param data an array of String[] arguments, one for each tab
   */
  private void setDisplayArgs(String[][] data) {
    if(data==null) {
      return;
    }
    int len = Math.min(data.length, tabData.size());
    for(int i = 0; i<len; i++) {
      DisplayTab tab = tabData.get(i);
      tab.setModelArgs(data[i]);
    }
  }

  protected void setMinimumArgLength(int n) {
    n = Math.max(n, 1); // never shorter than 1 element
    if(n==args.length) {
      return;
    }
    if(n>args.length) {
      // increase the size of the args array
      String[] newArgs = new String[n];
      for(int i = 0; i<n; i++) {
        if(i<args.length) {
          newArgs[i] = args[i];
        } else {
          newArgs[i] = "";                                     //$NON-NLS-1$
        }
      }
      setArgs(newArgs);
    } else {
      while((args.length>n)&&args[args.length-1].equals("")) { //$NON-NLS-1$
        String[] newArgs = new String[args.length-1];
        for(int i = 0; i<newArgs.length; i++) {
          newArgs[i] = args[i];
        }
        setArgs(newArgs);
      }
    }
  }

  protected void removeThread(Runnable runner) {
    threads.remove(runner);
  }

  /**
   * A class to hold display tab data.
   */
  public class DisplayTab {
    String title;
    boolean hyperlinksEnabled = true;
    String path; // url path or model class name
    URL url;
    Class<?> modelClass;
    JComponent modelPane;
    JScrollPane modelScroller;
    String[] modelArgs = new String[0];

    /**
     * Constructor.
     *
     * @param title the tab title (may be null)
     * @param path the path
     */
    DisplayTab(String title, String path) {
      setTitle(title);
      setPath(path);
    }

    /**
     * Gets the path.
     *
     * @return the path
     */
    public String getPath() {
      return path;
    }

    /**
     * Sets the path.
     *
     * @param path the path
     */
    public void setPath(String path) {
      this.path = path;
      if(!setURL(path)) {
        setModelClass(path);
      }
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle() {
      return title;
    }

    /**
     * Sets the title.
     *
     * @param title the title
     */
    public void setTitle(String title) {
      this.title = title;
    }

    /**
     * Gets the URL. May return null.
     *
     * @return the URL
     */
    public URL getURL() {
      if((url==null)&&(modelClass==null)&&(path!=null)&&!"".equals(path)) { //$NON-NLS-1$
        setURL(path);
      }
      return url;
    }

    /**
     * Gets the model class. May return null.
     *
     * @return the model class
     */
    public Class<?> getModelClass() {
      if((url==null)&&(modelClass==null)&&(path!=null)&&!path.equals("")) { //$NON-NLS-1$
        setModelClass(path);
      }
      return modelClass;
    }

    /**
     * Gets the modelPane. May return null.
     *
     * @return the model pane
     */
    public JComponent getModelPane() {
      if((modelPane==null)&&(getModelClass()!=null)) {
        if(JComponent.class.isAssignableFrom(modelClass)) {
          try {
            modelPane = (JComponent) modelClass.newInstance();
          } catch(Exception ex) {}
        } else {
          modelPane = Launcher.getModelPane(modelClass, getModelArgs());
        }
      }
      return modelPane;
    }

    /**
     * Gets the model arguments. May return null.
     *
     * @return the model arguments
     */
    public String[] getModelArgs() {
      return modelArgs;
    }

    /**
     * Sets the model arguments.
     *
     * @param args the model arguments
     */
    public void setModelArgs(String[] args) {
      if(args!=null) {
        modelArgs = args;
        modelPane = null;
        modelScroller = null;
      }
    }

    /**
     * Gets the modelScroller. May return null.
     *
     * @return the model scroller
     */
    protected JScrollPane getModelScroller() {
      final JComponent content = getModelPane();
      if((modelScroller==null)&&(content!=null)) {
        JPanel panel = new JPanel(new OSPLayout()) {
          public Dimension getPreferredSize() {
            Dimension dim = content.getPreferredSize();
            dim.width += 8;
            dim.height += 8;
            return dim;
          }

        };
        panel.setBackground(java.awt.Color.white);
        panel.add(content, OSPLayout.CENTERED);
        modelScroller = new JScrollPane(panel);
      }
      return modelScroller;
    }

    protected void setMinimumModelArgLength(int n) {
      n = Math.max(n, 0);
      if(n==modelArgs.length) {
        return;
      }
      if(n>modelArgs.length) {
        // increase the size of the modelArgs array
        String[] newArgs = new String[n];
        for(int i = 0; i<n; i++) {
          if(i<modelArgs.length) {
            newArgs[i] = modelArgs[i];
          } else {
            newArgs[i] = null;
          }
        }
        setModelArgs(newArgs);
      } else {
        while((modelArgs.length>n)&&(modelArgs[modelArgs.length-1]==null)) {
          String[] newArgs = new String[modelArgs.length-1];
          for(int i = 0; i<newArgs.length; i++) {
            newArgs[i] = modelArgs[i];
          }
          setModelArgs(newArgs);
        }
      }
    }

    /**
     * Sets the URL.
     *
     * @param path the url path
     * @return true if a url resource was found
     */
    private boolean setURL(String path) {
      url = null;
      Resource res = ResourceLoader.getResource(path);
      if((res!=null)&&(res.getURL()!=null)) {
        url = res.getURL();
        try {
          InputStream in = url.openStream();
          in.close();
          OSPLog.finer(LaunchRes.getString("Log.Message.URL")+" "+url); //$NON-NLS-1$ //$NON-NLS-2$
        } catch(Exception ex) {
          url = null;
        }
      }
      return url!=null;
    }

    /**
     * Sets the model class. The model class must either descend
     * from JPanel or define a static getModelPane() method.
     *
     * @param className the name of the class
     * @return true if the class was successfully loaded for the first time
     */
    private boolean setModelClass(String className) {
      path = className;
      if(className==null) {
        return false;
      }
      if((modelClass!=null)&&className.equals(modelClass.getName())) { // no change      
        return false;
      }
      modelPane = null;
      modelScroller = null;
      modelClass = LaunchClassChooser.getModelClass(getClassPath(), className);
      return modelClass!=null;
    }

  }

  //___________________________ deprecated methods _________________________

  /**
   * Adds an HTML tab with the specified title and relative path.
   * @deprecated replaced by addDisplayTab
   */
  @SuppressWarnings("javadoc")
	public DisplayTab addHTML(String title, String path) {
    return addDisplayTab(title, path, null);
  }

  /**
   * Inserts an HTML tab with the specified title and relative path.
   * @deprecated replaced by insertDisplayTab
   */
  @SuppressWarnings("javadoc")
	public DisplayTab insertHTML(int n, String title, String path) {
    return insertDisplayTab(n, title, path, null);
  }

  /**
   * Removes an HTML tab.
   * @deprecated replaced by removeDisplayTab
   */
  @SuppressWarnings("javadoc")
	public DisplayTab removeHTML(int n) {
    return removeDisplayTab(n);
  }

  /**
   * Gets an HTML tab.
   * @deprecated replaced by getDisplayTab
   */
  @SuppressWarnings("javadoc")
	public DisplayTab getHTML(int n) {
    return getDisplayTab(n);
  }

  /**
   * Returns the number of html objects in the list.
   * @deprecated replaced by getDisplayTabCount
   */
  @SuppressWarnings("javadoc")
	public int getHTMLCount() {
    return getDisplayTabCount();
  }

  /**
   * Sets the specified HTML tab title and url path.
   * @deprecated replaced by setDisplayTab
   */
  @SuppressWarnings("javadoc")
	public DisplayTab setHTML(int n, String title, String path) {
    return setDisplayTab(n, title, path, null);
  }

}

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
