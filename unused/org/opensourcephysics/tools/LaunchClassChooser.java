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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.display.OSPRuntime;

/**
 * This modal dialog lets the user choose launchable classes from jar files.
 */
public class LaunchClassChooser extends JDialog {
  // static fields
  private static Pattern pattern;
  private static Matcher matcher;
  private static Map<String, LaunchableClassMap> classMaps = new TreeMap<String, LaunchableClassMap>(); // maps path to classMap
  protected static boolean jarsOnly = true;
  protected static String baseDirectoryPath;
  // instance fields
  private JTextField searchField;
  private String defaultSearch = "";   //$NON-NLS-1$
  private String currentSearch = defaultSearch;
  private JScrollPane scroller;
  private JList choices;               // list of search results (class names)
  private LaunchableClassMap classMap; // map of  available launchable classes
  private boolean applyChanges = false;
  private JButton okButton;

  /**
   * Constructs an empty LaunchClassChooser dialog.
   *
   * @param owner the component that owns the dialog (may be null)
   */
  public LaunchClassChooser(Component owner) {
    super(JOptionPane.getFrameForComponent(owner), true);
    setTitle(LaunchRes.getString("ClassChooser.Frame.Title")); //$NON-NLS-1$
    JLabel textLabel = new JLabel(LaunchRes.getString("ClassChooser.Search.Label")+" "); //$NON-NLS-1$ //$NON-NLS-2$
    // create the buttons
    okButton = new JButton(LaunchRes.getString("ClassChooser.Button.Accept")); //$NON-NLS-1$
    okButton.setEnabled(false);
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        applyChanges = true;
        setVisible(false);
      }

    });
    JButton cancelButton = new JButton(LaunchRes.getString("ClassChooser.Button.Cancel")); //$NON-NLS-1$
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }

    });
    // create the search field
    searchField = new JTextField(defaultSearch);
    searchField.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        Object obj = choices.getSelectedValue();
        if("model".equals(searchField.getName())) { //$NON-NLS-1$
          searchForModel();
        } else {
          search();
        }
        choices.setSelectedValue(obj, true);
      }

    });
    getRootPane().setDefaultButton(okButton);
    // lay out the header pane
    JPanel headerPane = new JPanel();
    headerPane.setLayout(new BoxLayout(headerPane, BoxLayout.X_AXIS));
    headerPane.add(textLabel);
    headerPane.add(Box.createHorizontalGlue());
    headerPane.add(searchField);
    headerPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
    // lay out the scroll pane
    JPanel scrollPane = new JPanel(new BorderLayout());
    scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    // lay out the button pane
    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
    buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
    buttonPane.add(Box.createHorizontalGlue());
    buttonPane.add(okButton);
    buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
    buttonPane.add(cancelButton);
    // add everything to the content pane
    Container contentPane = getContentPane();
    contentPane.add(headerPane, BorderLayout.NORTH);
    contentPane.add(scrollPane, BorderLayout.CENTER);
    contentPane.add(buttonPane, BorderLayout.SOUTH);
    // create the scroll pane
    scroller = new JScrollPane();
    scroller.setPreferredSize(new Dimension(400, 300));
    scrollPane.add(scroller, BorderLayout.CENTER);
    pack();
    // center dialog on the screen
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (dim.width-this.getBounds().width)/2;
    int y = (dim.height-this.getBounds().height)/2;
    setLocation(x, y);
  }

  /**
   * Sets the path to be searched. The path must be a set of jar file names
   * separated by semicolons unless jarsOnly is set to false.
   *
   * @param path the search path
   * @return true if at least one jar file was successfully loaded
   */
  public boolean setPath(String path) {
    String[] jarNames = parsePath(path);
    // reset class map to null
    classMap = null;
    if((jarNames==null)||(jarNames.length==0)) {
      return false;
    }
    // create classMaps key
    String key = ""; //$NON-NLS-1$
    for(int i = 0; i<jarNames.length; i++) {
      if(!key.equals("")) { //$NON-NLS-1$
        // separate jars with semicolon
        key += ";";         //$NON-NLS-1$
      }
      key += jarNames[i];
    }
    // set the current classMap
    classMap = classMaps.get(key);
    if(classMap==null) {
      classMap = new LaunchableClassMap(jarNames);
      classMaps.put(key, classMap);
    }
    return true;
  }

  /**
   * Determines if the specified path is loaded. This will return true
   * only if the path is one or more jar files all of which are loaded.
   *
   * @param path the path
   * @return true if all jars in the path are loaded
   */
  public boolean isLoaded(String path) {
    if(classMap==null) {
      return false;
    }
    String[] jarNames = parsePath(path);
    for(int i = 0; i<jarNames.length; i++) {
      if(!classMap.includesJar(jarNames[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * Chooses a launchable class and assigns it to the specified launch node.
   *
   * @param node the node
   * @return true if the class assignment is approved
   */
  public boolean chooseClassFor(LaunchNode node) {
    if("model".equals(searchField.getName())) { //$NON-NLS-1$
      searchField.setText(null);
    }
    setTitle(LaunchRes.getString("ClassChooser.Frame.Title")); //$NON-NLS-1$
    searchField.setName("launch");                             //$NON-NLS-1$
    search();
    // select node's current launch class, if any
    choices.setSelectedValue(node.launchClassName, true);
    applyChanges = false;
    setVisible(true);
    if(!applyChanges) {
      return false;
    }
    Object obj = choices.getSelectedValue();
    if(obj==null) {
      return false;
    }
    String className = obj.toString();
    // get the class name and the launchable class
    node.launchClass = classMap.get(className);
    node.launchClassName = className;
    node.launchModelScroller = null;
    return true;
  }

  /**
   * Chooses and returns a model class.
   *
   * @param previousClassName the previous class name. May be null.
   * @return the newly selected class. May return null.
   */
  public Class<?> chooseModel(String previousClassName) {
    if("launch".equals(searchField.getName())) { //$NON-NLS-1$
      searchField.setText(null);
    }
    setTitle(LaunchRes.getString("ModelClassChooser.Frame.Title")); //$NON-NLS-1$
    searchField.setName("model");                                   //$NON-NLS-1$
    searchForModel();
    // select tab's previous model class, if any
    choices.setSelectedValue(previousClassName, true);
    applyChanges = false;
    setVisible(true);
    if(!applyChanges) {
      return null;
    }
    Object obj = choices.getSelectedValue();
    if(obj==null) {
      return null;
    }
    String className = obj.toString();
    return classMap.models.get(className);
  }

  /**
   * Gets the class with the given name in the current class map.
   *
   * @param className the class name
   * @return the Class object, or null if not found
   */
  public Class<?> getClass(String className) {
    if(classMap==null) {
      return null;
    }
    return classMap.getClass(className);
  }

  /**
   * Sets the base directory for classpaths.
   *
   * @param path the base directory path
   */
  public static void setBasePath(String path) {
  	baseDirectoryPath = path;
  }

  
  /**
   * Gets the class with the given name in the specified path.
   *
   * @param classPath the path
   * @param className the class name
   * @return the Class object, or null if not found
   */
  public static Class<?> getClass(String classPath, String className) {
    if((classPath==null)||(className==null)) {
      return null;
    }
    // get the classMap for the specified path
    String[] jarNames = parsePath(classPath);
    LaunchableClassMap classMap = getClassMap(jarNames);
    // get the class from the classMap
    return classMap.getClass(className);
  }

  /**
   * Gets the modelPane class with the given name in the specified path.
   *
   * @param classPath the path
   * @param className the class name
   * @return the Class object, or null if not found
   */
  public static Class<?> getModelClass(String classPath, String className) {
    if((classPath==null)||(className==null)) {
      return null;
    }
    // get the classMap for the specified path
    String[] jarNames = parsePath(classPath);
    LaunchableClassMap classMap = getClassMap(jarNames);
    // get the class from the classMap
    return classMap.getModelClass(className);
  }

  /**
   * Gets the class with the given name in the specified path.
   *
   * @param classPath the path
   * @param className the class name
   * @param type a subclass of the desired class
   * @return the Class object, or null if not found
   */
  public static Class<?> getClassOfType(String classPath, String className, Class<?> type) {
    if((classPath==null)||(className==null)) {
      return null;
    }
    // get the classMap for the specified path
    String[] jarNames = parsePath(classPath);
    LaunchableClassMap classMap = getClassMap(jarNames);
    // get the class from the classMap
    return classMap.getClassOfType(className, type);
  }

  /**
   * Gets the ClassLoader for the specified path.
   *
   * @param classPath the path
   * @return the ClassLoader object, or null if not found
   */
  public static ClassLoader getClassLoader(String classPath) {
    if((classPath==null)||classPath.equals("")) { //$NON-NLS-1$
      return null;
    }
    // get the classMap for the specified path
    String[] jarNames = parsePath(classPath);
    LaunchableClassMap classMap = getClassMap(jarNames);
    // get the class loader from the classMap
    return classMap.classLoader;
  }

  /**
   * Gets the launchable class map for the specified jar name array.
   *
   * @param jarNames the string array of jar names
   * @return the class map
   */
  private static LaunchableClassMap getClassMap(String[] jarNames) {
    // create a key string from the jar names
    String key = ""; //$NON-NLS-1$
    for(int i = 0; i<jarNames.length; i++) {
      if(!key.equals("")) { //$NON-NLS-1$
        key += ";";         //$NON-NLS-1$
      }
      key += jarNames[i];
    }
    // get the classMap for the key
    LaunchableClassMap classMap = classMaps.get(key);
    if(classMap==null) {
      classMap = new LaunchableClassMap(jarNames);
      classMaps.put(key, classMap);
    }
    return classMap;
  }

  /**
   * Searches using the current search field text.
   */
  private void search() {
    if(classMap==null) {
      return;
    }
    classMap.loadAllClasses();
    if(search(searchField.getText())) {
      currentSearch = searchField.getText();
      searchField.setBackground(Color.white);
    } else {
      JOptionPane.showMessageDialog(this, LaunchRes.getString("Dialog.InvalidRegex.Message")+" \""+searchField.getText()+"\"", LaunchRes.getString("Dialog.InvalidRegex.Title"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      searchField.setText(currentSearch);
    }
  }

  /**
   * Searches for a modelPane using the current search field text.
   */
  private void searchForModel() {
    if(classMap==null) {
      return;
    }
    classMap.loadAllClasses();
    if(searchForModel(searchField.getText())) {
      currentSearch = searchField.getText();
      searchField.setBackground(Color.white);
    } else {
      JOptionPane.showMessageDialog(this, LaunchRes.getString("Dialog.InvalidRegex.Message")+" \""+searchField.getText()+"\"", LaunchRes.getString("Dialog.InvalidRegex.Title"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      searchField.setText(currentSearch);
    }
  }

  /**
   * Searches for class names using a regular expression string
   * and puts matches into the class chooser list of choices.
   *
   * @param regex the regular expression
   * @return true if the search succeeded (even if no matches found)
   */
  private boolean search(String regex) {
    regex = regex.toLowerCase();
    okButton.setEnabled(false);
    try {
      pattern = Pattern.compile(regex);
    } catch(Exception ex) {
      return false;
    }
    ArrayList<String> matches = new ArrayList<String>();
    for(Iterator<?> it = classMap.keySet().iterator(); it.hasNext(); ) {
      String name = (String) it.next();
      matcher = pattern.matcher(name.toLowerCase());
      if(matcher.find()) {
        matches.add(name);
      }
    }
    Object[] results = matches.toArray();
    choices = new JList(results);
    choices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    choices.setFont(searchField.getFont());
    choices.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        JList theList = (JList) e.getSource();
        okButton.setEnabled(!theList.isSelectionEmpty());
      }

    });
    choices.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        JList theList = (JList) e.getSource();
        if((e.getClickCount()==2)&&!theList.isSelectionEmpty()) {
          okButton.doClick();
        }
      }

    });
    scroller.getViewport().setView(choices);
    return true;
  }

  /**
   * Searches for class names using a regular expression string
   * and puts matches into the class chooser list of choices.
   *
   * @param regex the regular expression
   * @return true if the search succeeded (even if no matches found)
   */
  private boolean searchForModel(String regex) {
    regex = regex.toLowerCase();
    okButton.setEnabled(false);
    try {
      pattern = Pattern.compile(regex);
    } catch(Exception ex) {
      return false;
    }
    ArrayList<String> matches = new ArrayList<String>();
    for(Iterator<?> it = classMap.models.keySet().iterator(); it.hasNext(); ) {
      String name = (String) it.next();
      matcher = pattern.matcher(name.toLowerCase());
      if(matcher.find()) {
        matches.add(name);
      }
    }
    Object[] results = matches.toArray();
    choices = new JList(results);
    choices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    choices.setFont(searchField.getFont());
    choices.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        JList theList = (JList) e.getSource();
        okButton.setEnabled(!theList.isSelectionEmpty());
      }

    });
    choices.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        JList theList = (JList) e.getSource();
        if((e.getClickCount()==2)&&!theList.isSelectionEmpty()) {
          okButton.doClick();
        }
      }

    });
    scroller.getViewport().setView(choices);
    return true;
  }

  /**
   * Parses the specified path into path tokens (at semicolons).
   *
   * @param path the path
   * @param jarsOnly true if only ".jar" names are returned
   * @return an array of path names
   */
  static String[] parsePath(String path) {
    return parsePath(path, jarsOnly);
  }

  /**
   * Parses the specified path into path tokens (at semicolons).
   *
   * @param path the path
   * @param jarsOnly true if only ".jar" names are returned
   * @return an array of path names
   */
  static String[] parsePath(String path, boolean jarsOnly) {
    Collection<String> tokens = new ArrayList<String>();
    // get the first path token
    String next = path;
    int i = path.indexOf(";"); //$NON-NLS-1$
    if(i!=-1) {
      next = path.substring(0, i);
      path = path.substring(i+1);
    } else {
      path = ""; //$NON-NLS-1$
    }
    // iterate thru the path tokens, trim and add to token list
    while(next.length()>0) {
      if(!jarsOnly||next.endsWith(".jar")) { //$NON-NLS-1$
        tokens.add(next);
      }
      i = path.indexOf(";");                 //$NON-NLS-1$
      if(i==-1) {
        next = path.trim();
        path = "";                           //$NON-NLS-1$
      } else {
        next = path.substring(0, i).trim();
        path = path.substring(i+1).trim();
      }
    }
    return tokens.toArray(new String[0]);
  }

}

/**
 * A map of class name to launchable Class object.
 * The name of the jar or directory is prepended to the class name.
 */
class LaunchableClassMap extends TreeMap<String, Class<?>> {
  // instance fields
  ClassLoader classLoader; // loads classes
  String[] jarOrDirectoryNames; // paths of jars or directories relative to jar base
  boolean allLoaded = false;
  TreeMap<String, Class<?>> models = new TreeMap<String, Class<?>>();

  // constructor creates class loader
  LaunchableClassMap(String[] names) {
    jarOrDirectoryNames = names;
    // create a URL for each jar or directory
    Collection<URL> urls = new ArrayList<URL>();
    // changed by D Brown 2007-11-06 for Linux
    // changed by F Esquembre and D Brown 2010-03-02 to allow arbitrary base path
    String basePath = LaunchClassChooser.baseDirectoryPath;
    if (basePath==null) basePath = OSPRuntime.getLaunchJarDirectory();
    for(int i = 0; i<names.length; i++) {
      String path = XML.getResolvedPath(names[i], basePath);
      if (!path.endsWith(".jar") && !path.endsWith("/")) { //$NON-NLS-1$ //$NON-NLS-2$
      	path += "/";  // directories passed to URLClassLoader must end with slash //$NON-NLS-1$
      }
      try {
        urls.add(new URL("file:"+path)); //$NON-NLS-1$
      } catch(MalformedURLException ex) {
        OSPLog.info(ex+" "+path);        //$NON-NLS-1$
      }
    }
    // create the class loader
    classLoader = URLClassLoader.newInstance(urls.toArray(new URL[0]));
  }

  /**
   * Loads a class from the URLClassLoader or, if this fails, from the
   * current class loader.
   *
   * @param name the class name
   * @return the Class
   * @throws ClassNotFoundException
   */
  Class<?> smartLoadClass(String name) throws ClassNotFoundException {
    try {
      return classLoader.loadClass(name);
    } catch(ClassNotFoundException e) { // added by Kip Barros
      return this.getClass().getClassLoader().loadClass(name);
    }
  }
  
  // returns a list of class files at all depths in a specified directory
  ArrayList<File> getClassFiles(File directory) {
  	ArrayList<File> files = new ArrayList<File>();
  	for (File next: directory.listFiles()) {
  		if (next.isDirectory()) {
  			files.addAll(getClassFiles(next)); 
  		}
  		else if (next.getName().endsWith(".class")) { //$NON-NLS-1$
  			files.add(next);
  		}
  	}
  	return files;
  }

  // loads all launchable and model classes
  void loadAllClasses() {
    if(allLoaded) {
      return;
    }
    JApplet applet = org.opensourcephysics.display.OSPRuntime.applet;
    // for each jar or directory name, find launchable classes
    for(String next: jarOrDirectoryNames) {
    	if (next.indexOf(".jar")>-1) { // next is a relative jar path  //$NON-NLS-1$
	      // create a JarFile
	      JarFile jar = null;
	      try {
	        if(applet==null) { // application mode
	          String basePath = LaunchClassChooser.baseDirectoryPath;
	          if (basePath==null) basePath = OSPRuntime.getLaunchJarDirectory();
	          String path = XML.getResolvedPath(next, basePath);
	          jar = new JarFile(path);
	        } 
	        else { // applet mode
	          String path = XML.getResolvedPath(next, applet.getCodeBase().toExternalForm());
	          // create a URL that refers to a jar file on the web
	          URL url = new URL("jar:"+path+"!/");   //$NON-NLS-1$ //$NON-NLS-2$
	          // get the jar
	          JarURLConnection conn = (JarURLConnection) url.openConnection();
	          jar = conn.getJarFile();
	        }
	      } catch(Exception ex) {
	        OSPLog.info(ex.getClass().getName()+": "+ex.getMessage()); //$NON-NLS-1$
	      }
	      if(jar==null) {
	        continue;
	      }
	      // iterate thru JarFile entries and load classes
	      for(Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements(); ) {
	        JarEntry entry = e.nextElement();
	        String name = entry.getName();
	        if(name.endsWith(".class")) {     //$NON-NLS-1$
	          loadClass(name);
	        }
	      }
    	}
    	else { // next is a relative directory path
        String basePath = LaunchClassChooser.baseDirectoryPath;
        if (basePath==null) basePath = OSPRuntime.getLaunchJarDirectory();
        String directoryPath = XML.getResolvedPath(next, basePath);
        for (File nextFile: getClassFiles(new File(directoryPath))) {
          String name = XML.getPathRelativeTo(nextFile.getPath(), directoryPath);
          loadClass(name);
        }
    	}
    }
    allLoaded = true;
  }
  
  // load a class and, if launchable or a model, store it with class name key
  void loadClass(String name) {
    // ignore manifest and inner classes
    if(name.indexOf("$")==-1) {     //$NON-NLS-1$
      // remove class extension and replace slashes
      name = name.substring(0, name.indexOf(".class"));        //$NON-NLS-1$
      int j = name.indexOf("/");                               //$NON-NLS-1$
      while(j!=-1) {
        name = name.substring(0, j)+"."+name.substring(j+1);   //$NON-NLS-1$
        j = name.indexOf("/");                                 //$NON-NLS-1$
      }
      // return if class is already loaded
      if((get(name)!=null)||(models.get(name)!=null)) {
        return;
      }
      try {
        // load the class
        Class<?> nextClass = smartLoadClass(name);  // changed by Kip Barros
        // if launchable, store it
        if(Launcher.isLaunchable(nextClass)) {
          put(name, nextClass);
        }
        // if a model, store it
        if(Launcher.isModel(nextClass)) {
          models.put(name, nextClass);
        }
      } catch(ClassNotFoundException ex) {
        /** empty block */
      } catch(NoClassDefFoundError err) {
        OSPLog.info(err.toString());
      }
    }  	
  }

  // returns true of this classMap includes specified jar
  boolean includesJar(String jarName) {
    for(int i = 0; i<jarOrDirectoryNames.length; i++) {
      if(jarOrDirectoryNames[i].equals(jarName)) {
        return true;
      }
    }
    return false;
  }

  // gets the specified class, or null if not loadable or launchable
  Class<?> getClass(String className) {
    Class<?> type = get(className);
    if((type!=null)||allLoaded) {
      return type;
    }
    try {
      // load the class and, if launchable, return it
      type = smartLoadClass(className); // changed by Kip Barros
      if(Launcher.isLaunchable(type)) {
        return type;
      }
    } catch(ClassNotFoundException ex) {
      /** empty block */
    } catch(NoClassDefFoundError err) {
      OSPLog.info(err.toString());
    }
    return null;
  }

  // gets the specified model class, or null if not loadable or not a model
  Class<?> getModelClass(String className) {
    Class<?> type = models.get(className);
    if((type!=null)) {
      return type;
    }
    try {
      // load the class and, if a model, return it
      type = smartLoadClass(className); // changed by Kip Barros
      if(Launcher.isModel(type)) {
        return type;
      }
      if(JComponent.class.isAssignableFrom(type)) {
        try {
          type.getConstructor((Class[]) null);
          return type;
        } catch(Exception ex) {}
      }
    } catch(ClassNotFoundException ex) {}
    catch(NoClassDefFoundError err) {
      OSPLog.info(err.toString());
    }
    return null;
  }

  // gets the specified class, or null if not found or wrong type
  Class<?> getClassOfType(String className, Class<?> type) {
    try {
      // load the class and, if right type, return it
    	Class<?> theClass = smartLoadClass(className);
      if(type.isAssignableFrom(theClass)) {
        return theClass;
      }
    } catch(ClassNotFoundException ex) {
      /** empty block */
    } catch(NoClassDefFoundError err) {
      OSPLog.info(err.toString());
    }
    return null;
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
