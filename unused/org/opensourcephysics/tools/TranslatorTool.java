/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;

import org.opensourcephysics.controls.ControlsRes;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.controls.XMLTable;
import org.opensourcephysics.controls.XMLTableModel;
import org.opensourcephysics.display.Hidable;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.TextFrame;

/**
 * This provides a GUI for creating and editing string resources associated
 * with a class. Resources are stored in properties files
 * with the same name and located in the same folder as the class.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class TranslatorTool extends JFrame implements Tool, Hidable, Translator {
  // instance fields
  private Dimension dim = new Dimension(320, 240);
  private Map<Class<?>, Map<String, String>> defaultProps // maps class to default properties map (name->value translations)
    = new HashMap<Class<?>, Map<String, String>>();
  private Map<Class<?>, Map<String, Map<String, String>>> classes                // maps class to map of language->properties
    = new HashMap<Class<?>, Map<String, Map<String, String>>>();
  private Map<Object, Class<?>> associates = new HashMap<Object, Class<?>>();    // maps object to class
  private Set<Map<String, String>> changed = new HashSet<Map<String, String>>(); // contains properties with unsaved changes
  private Locale locale = Locale.getDefault();
  private Set<Class<?>> searched = new HashSet<Class<?>>();                      // classes searched for translations
  private Map<Class<?>, String> paths = new HashMap<Class<?>, String>();				 // property file directory paths
  private String helpURL = "http://www.opensourcephysics.org/online_help/tools/translator_tool_help.html"; //$NON-NLS-1$
  private boolean keepHidden = false;
  private XMLControl control = new XMLControlElement();
  private XMLTable table;
  private Class<?> classType;
  private JPanel contentPane = new JPanel(new BorderLayout());
  private String fileExtension;
  private JLabel descriptionLabel;
  private JComboBox localeDropDown;
  private Icon saveIcon;
  private JButton saveButton;
  private JButton closeButton;
  private JButton helpButton;
  private String preferredTitle=null;

  /**
   * The singleton shared translator tool.
   */
  private static final TranslatorTool TOOL = new TranslatorTool();

  /**
   * Gets the shared TranslatorTool.
   *
   * @return the shared TranslatorTool
   */
  public static TranslatorTool getTool() {
    return TOOL;
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
      super.setVisible(b);
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
   * Private constructor.
   */
  private TranslatorTool() {
	  if(org.opensourcephysics.js.JSUtil.isJS) {  // external tools not supported in JavaScript.
		  return;
	  }
    if(OSPRuntime.appletMode) {
      keepHidden = true;
    }
    String name = "TranslatorTool"; //$NON-NLS-1$
    setName(name);
    createGUI();
    refreshGUI();
    setLocale(ToolsRes.resourceLocale);
    Toolbox.addTool(name, this);
  }

  /**
   * Sends a job to this tool and specifies a tool to reply to.
   *
   * @param job the Job
   * @param replyTo the tool to notify when the job is complete (may be null)
   */
  public void send(Job job, Tool replyTo) {
    /** not implemented */
  }

  /**
   * Sets the locale.
   *
   * @param locale the locale
   */
  public void setLocale(Locale locale) {
    if(locale==this.locale) {
      return;
    }
    this.locale = locale;
    showProperties(classType);
    // add new item if locale is not in dropdown
    LocaleItem item = null;
    if(localeDropDown!=null) {
      // look for existing item
      for(int i = 0; i<localeDropDown.getItemCount(); i++) {
        item = (LocaleItem) localeDropDown.getItemAt(i);
        if(item.loc.getLanguage().equals(locale.getLanguage())) {
          break;
        }
        item = null;
      }
      // if not found, create and add new item
      if(item==null) {
        item = new LocaleItem(locale);
        addDropDownItem(item);
      }
      localeDropDown.setSelectedItem(item);
      // enable/disable save button
      Map<String, String> properties = getProperties(classType, locale);
      saveButton.setEnabled(changed.contains(properties));
      // refresh objects associated with current class
      refreshAssociates(classType);
    }
  }

  /**
   * Associates an object with a class for property lookup purposes.
   *
   * @param obj the object needing translations
   * @param type the class
   */
  public synchronized void associate(Object obj, Class<?> type) {
    if(obj==null) {
      return;
    }
    associates.put(obj, type);
  }

  /**
   * Shows the properties for the specified class.
   *
   * @param type the class
   */
  public void showProperties(Class<?> type) {
    if(type==null) {
      return;
    }
    classType = type;
    // clear current values
    control.clearValues();
    // set file extension (language only)
    fileExtension = ""; //$NON-NLS-1$
    String addon = locale.getLanguage();
    if(!addon.equals("")) {       //$NON-NLS-1$
      fileExtension += "_"+addon; //$NON-NLS-1$
    }
    fileExtension += ".properties"; //$NON-NLS-1$
    // initialize control with default values
    Collection<String> names = control.getPropertyNames();
    Iterator<String> it = names.iterator();
    while(it.hasNext()) {
      String next = it.next();
      control.setValue(next, next);
    }
    // set control values per current properties map
    Map<String, String> properties = getProperties(type, locale);
    Iterator<?> it2 = properties.keySet().iterator();
    while(it2.hasNext()) {
      String key = (String) it2.next();
      control.setValue(key, properties.get(key));
    }
    // compare with defaults and flag unused properties
    Set<String> keys = getDefaults(type).keySet();
    it2 = properties.keySet().iterator();
    while(it2.hasNext()) {
      String key = (String) it2.next();
      if(!keys.contains(key)) {
        table.setBackgroundColor(key, Color.PINK);
      }
    }
    table.refresh();
    refreshGUI();
  }

  /**
   * Sets a title for the tool
   */
  public void setPreferredTitle(String title) {
    preferredTitle = title;
    refreshGUI();
  }
  
  /**
   * Gets the localized value of a property for the specified class.
   * If no localized value is found, the key is returned.
   *
   * @param type the class requesting the localized value
   * @param key the string to localize
   * @return the localized string
   */
  public String getProperty(Class<?> type, String key) {
    return getProperty(type, key, key, ToolsRes.resourceLocale);
  }

  /**
   * Gets the localized value of a property for the specified class.
   * If no localized value is found, the defaultValue is returned.
   *
   * @param type the class requesting the localized value
   * @param key the string to localize
   * @param defaultValue the default if no localized value found
   * @return the localized string
   */
  public String getProperty(Class<?> type, String key, String defaultValue) {
    return getProperty(type, key, defaultValue, ToolsRes.resourceLocale);
  }

  /**
   * Gets the localized value of a property for the specified object.
   * The object must first be associated with a class.
   * If no localized value is found, the key is returned.
   *
   * @param obj the object requesting the localized value
   * @param key the string to localize
   * @return the localized string
   */
  public String getProperty(Object obj, String key) {
    return getProperty(obj, key, key);
  }

  /**
   * Gets the localized value of a property for the specified object.
   * The object must first be associated with a class.
   * If no localized value is found, the defaultValue is returned.
   *
   * @param obj the object requesting the localized value
   * @param key the string to localize
   * @param defaultValue the default if no localized value found
   * @return the localized string
   */
  public String getProperty(Object obj, String key, String defaultValue) {
    if(obj==null) {
      return(defaultValue==null) ? key : defaultValue;
    }
    Class<?> type = associates.get(obj);
    return getProperty(type, key, defaultValue, ToolsRes.resourceLocale);
  }

  /**
   * Removes a property from those defined for the specified class.
   *
   * @param type the class
   * @param key the property to remove
   */
  public void removeProperty(Class<?> type, String key) {
    if(type==null) {
      return;
    }
    // remove key from all properties maps for class type
    getDefaults(type).remove(key);
    Map<String, Map<String, String>> locales = classes.get(type);
    if(locales!=null) {
      Iterator<String> it = locales.keySet().iterator();
      while(it.hasNext()) {
        Map<String, String> properties = locales.get(it.next());
        properties.remove(key);
        flagChange(properties);
      }
    }
    TOOL.showProperties(TOOL.classType);
    refreshAssociates(TOOL.classType);
  }

  /**
   * Removes a property from those defined for the specified object.
   * The object must first be associated with a class.
   *
   * @param obj the object
   * @param key the property to remove
   */
  public void removeProperty(Object obj, String key) {
    Class<?> type = associates.get(obj);
    removeProperty(type, key);
  }

  /**
   * Adds a property to those defined for the specified class.
   *
   * @param type the class
   * @param key the property to add
   * @param defaultValue the default value
   */
  public void addProperty(Class<?> type, String key, String defaultValue) {
    if((type==null)||(key==null)) {
      return;
    }
    if(defaultValue==null) {
      defaultValue = key;
    }
    // add key-value to all properties maps for class type
    getDefaults(type).put(key, defaultValue);
    Map<String, String> properties = getProperties(type, locale);
    if(properties.get(key)==null) {
      properties.put(key, defaultValue);
      flagChange(properties);
    }
    Map<String, Map<String, String>> locales = classes.get(type);
    if(locales!=null) {
      Iterator<String> it = locales.keySet().iterator();
      while(it.hasNext()) {
        properties = locales.get(it.next());
        if(properties.get(key)==null) {
          properties.put(key, defaultValue);
          flagChange(properties);
        }
      }
    }
    TOOL.showProperties(TOOL.classType);
    // refresh objects associated with current class
    refreshAssociates(TOOL.classType);
  }

  /**
   * Gets objects associated with the specified class.
   */
  public Collection<Object> getAssociates(Class<?> type) {
    Collection<Object> c = new ArrayList<Object>();
    Iterator<Object> it = associates.keySet().iterator();
    while(it.hasNext()) {
      Object obj = it.next();
      if(associates.get(obj).equals(type)) {
        c.add(obj);
      }
    }
    return c;
  }

  /**
   * Returns Locales for which translations exist for the specified class.
   */
  public Locale[] getTranslatedLocales(Class<?> type) {
    if(!searched.contains(type)) {
      // search for and load all saved locales
      synchronized(searched) {
        searched.add(type);
      }
      Map<String, Map<String, String>> locales = classes.get(type);
      if(locales==null) {
        locales = new HashMap<String, Map<String, String>>();
        synchronized(classes) {
          classes.put(type, locales);
        }
      }
      if(OSPRuntime.applet==null) {                                              //  search for languages if NOT an applet
        Set<String> langs = locales.keySet();                                    // loaded language codes
        String path = getPath(type);
        Resource res = null;
        String[] languages = Locale.getISOLanguages();                           // all standard language codes
        for(int i = 0; i<languages.length; i++) {
          if(langs.contains(languages[i])) {
            continue;
          }
          res = ResourceLoader.getResource(path+"_"+languages[i]+".properties"); //$NON-NLS-1$ //$NON-NLS-2$
          if(res!=null) {
            Map<String, String> properties = new TreeMap<String, String>();
            locales.put(languages[i], properties);
            // fill properties from resource
            OSPLog.finer(res.getAbsolutePath());
            readProperties(res.openReader(), properties);
          }
        }
      }
    }
    Set<String> languages = new TreeSet<String>();
    languages.addAll(classes.get(type).keySet()); // alphabetize
    ArrayList<Locale> locales = new ArrayList<Locale>();
    for(Iterator<String> it = languages.iterator(); it.hasNext(); ) {
      locales.add(new Locale(it.next().toString()));
    }
    return locales.toArray(new Locale[0]);
  }

  /**
   * Returns true if a String is a valid 2-letter language code.
   *
   * @param lang the 2-letter code
   * @return true if valid 2-letter language code
   */
  protected boolean isLanguage(String lang) {
    String[] languages = Locale.getISOLanguages(); // all standard language codes
    for(int i = 0; i<languages.length; i++) {
      if(languages[i].equals(lang)) {
        return true;
      }
    }
    return false;
  }

  // ______________________________ private methods _____________________________
  private void addDropDownItem(LocaleItem item) {
    // sort items for dropdown: default first, then alphabetized
    Map<String, LocaleItem> items = new TreeMap<String, LocaleItem>();
    LocaleItem defaultItem = item.isDefault() ? item : null;
    if(!item.isDefault()) {
      items.put(item.language.toLowerCase(), item);
    }
    for(int i = 0; i<localeDropDown.getItemCount(); i++) {
      LocaleItem next = (LocaleItem) localeDropDown.getItemAt(i);
      if(next.isDefault()) {
        defaultItem = next;
      } else {
        items.put(next.language.toLowerCase(), next);
      }
    }
    localeDropDown.removeAllItems();
    if(defaultItem!=null) {
      localeDropDown.addItem(defaultItem);
    }
    for(Iterator<String> it = items.keySet().iterator(); it.hasNext(); ) {
      localeDropDown.addItem(items.get(it.next()));
    }
  }

  /**
   * Gets the localized value of a property.
   *
   * @param type the class requesting the localized value
   * @param key the string to localize
   * @param defaultValue the default if no localized value found
   * @param locale the locale
   * @return the localized string
   */
  private synchronized String getProperty(Class<?> type, String key, String defaultValue, Locale locale) {
    if(defaultValue==null) {
      defaultValue = key;
    }
    if(type==null) {
      return defaultValue;
    }
    if(!getDefaults(type).keySet().contains(key)) {
      addProperty(type, key, defaultValue);
    }
    return getProperties(type, locale).get(key);
  }

  /**
   * Gets the properties file path for a class.
   *
   * @param type the class
   * @return the path
   */
  public String getPath(Class<?> type) {
    if(type==null) {
      return null;
    }
    String path = paths.get(type);
    if (path!=null) { // saved path always ends with forward slash
    	return path+type.getSimpleName();
    }
    path = type.getName();
    // replace all "." with "/"
    int i = path.indexOf("."); //$NON-NLS-1$
    while(i!=-1) {
      path = path.substring(0, i)+"/"+path.substring(i+1); //$NON-NLS-1$
      i = path.indexOf(".");                               //$NON-NLS-1$
    }
    return path;
  }

  /**
   * Sets the path for a given class.
   *
   * @param type the class
   * @param directory the path
   */
  public void setPath(Class<?> type, String directory) {
  	directory = XML.forwardSlash(directory);
  	// add trailing slash, if none
  	if (!directory.endsWith("/")) //$NON-NLS-1$
  		directory += "/"; //$NON-NLS-1$
  	paths.put(type, directory);
  }

  /**
   * Gets the default properties for the specified class.
   *
   * @param type the class type
   * @return the default property-value map
   */
  private Map<String, String> getDefaults(Class<?> type) {
    Map<String, String> defaults = defaultProps.get(type);
    if(defaults==null) {
      defaults = new TreeMap<String, String>();
      synchronized(defaultProps) {
        defaultProps.put(type, defaults);
      }
    }
    return defaults;
  }

  /**
   * Gets the properties for the specified class and locale.
   *
   * @param type the class
   * @param locale the locale
   * @return the properties map <String key, String value>
   */
  private Map<String, String> getProperties(Class<?> type, Locale locale) {
    // look for properties map by class and language name
    Map<String, Map<String, String>> locales = classes.get(type);
    if(locales==null) {
      locales = new HashMap<String, Map<String, String>>();
      synchronized(classes) {
        classes.put(type, locales);
      }
    }
    Map<String, String> properties = locales.get(locale.getLanguage());
    if(properties==null) {
      properties = new TreeMap<String, String>();
      locales.put(locale.getLanguage(), properties);
      // fill properties from resource, if any
      String path = getPath(type);
      Resource res = null;
      String lang = locale.getLanguage();
      if(!lang.equals("")) {                                           //$NON-NLS-1$
        res = ResourceLoader.getResource(path+"_"+lang+".properties"); //$NON-NLS-1$ //$NON-NLS-2$
      }
      if(res==null) {
        res = ResourceLoader.getResource(path+".properties");          //$NON-NLS-1$
      }
      if(res!=null) {
        OSPLog.finer(res.getAbsolutePath());
        readProperties(res.openReader(), properties);
      } else {
        // load properties with default values
        Map<String, String> defaults = getDefaults(type);
        Iterator<String> it = defaults.keySet().iterator();
        while(it.hasNext()) {
          String key = it.next();
          String val = defaults.get(key);
          properties.put(key, val);
        }
        // add properties to changed set
        flagChange(properties);
      }
    }
    return properties;
  }

  /**
   * Reads the properties into the specified map.
   *
   * @param input the input reader
   * @param map the map
   */
  private void readProperties(BufferedReader input, Map<String, String> map) {
    try {
      // read properties line by line and put entries into the map
      String next = input.readLine();
      while(next!=null) {
        int i = next.indexOf("="); //$NON-NLS-1$
        if(i>-1) {
          String key = next.substring(0, i);
          String val = next.substring(i+1);
          map.put(key, val);
        }
        next = input.readLine();
      }
    } catch(IOException ex) {
      return;
    }
  }

  /**
   * Saves the current properties to the specified file.
   *
   * @param fileName the file name
   * @return the name of the saved file, or null if not saved
   */
  private String save(String fileName) {
    if((fileName==null)||fileName.equals("")) { //$NON-NLS-1$
      return null;
    }
    int n = fileName.lastIndexOf("/"); //$NON-NLS-1$
    if(n<0) {
      n = fileName.lastIndexOf("\\"); //$NON-NLS-1$
    }
    if(n>0) {
      String dir = fileName.substring(0, n+1);
      File file = new File(dir);
      if(!file.exists()) {
        XML.createFolders(dir);
      }
      if(!file.exists()) {
        return null;
      }
    }
    // assemble the properties
    StringBuffer content = new StringBuffer();
    String s = XML.stripExtension(fileName);
    content.append("# This is the "+s+".properties file"+XML.NEW_LINE+XML.NEW_LINE); //$NON-NLS-1$ //$NON-NLS-2$
    Iterator<String> it = control.getPropertyNames().iterator();
    while(it.hasNext()) {
      String key = it.next();
      String alias = control.getString(key);
      content.append(key+"="+alias+XML.NEW_LINE); //$NON-NLS-1$
    }
    File file = new File(fileName);
    try {
      if(file.exists()&&!file.canWrite()) {
    		JOptionPane.showMessageDialog(null, 
    				ControlsRes.getString("Dialog.ReadOnly.Message"),  //$NON-NLS-1$
    				ControlsRes.getString("Dialog.ReadOnly.Title"),  //$NON-NLS-1$
    				JOptionPane.PLAIN_MESSAGE);
        return null;
      }
      FileOutputStream stream = new FileOutputStream(file);
      java.nio.charset.Charset charset = java.nio.charset.Charset.forName("UTF-8");          //$NON-NLS-1$
      Writer out = new OutputStreamWriter(stream, charset);
      out = new BufferedWriter(out);
      out.write(content.toString());
      out.flush();
      out.close();
      if(file.exists()) {
        OSPLog.finest(file.getAbsolutePath());
        synchronized(changed) {
          changed.remove(getProperties(classType, locale));
        }
        // disable save button
        saveButton.setEnabled(false);
        return file.getAbsolutePath();
      }
    } catch(IOException ex) {
      OSPLog.warning(ex.getMessage());
    }
    return null;
  }

  /**
   * Creates the GUI.
   */
  private void createGUI() {
    XMLTableModel model = new XMLTableModel(control) {
      public String getColumnName(int column) {
        return(column==0) ? ToolsRes.getString("TranslatorTool.ColumnTitle.Property") : //$NON-NLS-1$
          ToolsRes.getString("TranslatorTool.ColumnTitle.PropValue");                   //$NON-NLS-1$
      }

    };
    // configure the frame
    contentPane.setPreferredSize(dim);
    setContentPane(contentPane);
    setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    // create the XMLTable
    table = new XMLTable(model);
    table.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if(OSPRuntime.isPopupTrigger(e)) {
          // find clicked row
          for(int i = 0; i<table.getRowCount(); i++) {
            Rectangle rect = table.getCellRect(i, 0, true);
            if(rect.contains(e.getX(), e.getY())) {
              table.setRowSelectionInterval(i, i);
              final String name = (String) table.getValueAt(i, 0);
              // show popup menu
              JPopupMenu popup = new JPopupMenu();
              JMenuItem removeItem = new JMenuItem(ToolsRes.getString("TranslatorTool.Popup.MenuItem.Remove") //$NON-NLS-1$
                +" \""+name+"\""); //$NON-NLS-1$ //$NON-NLS-2$
              popup.add(removeItem);
              removeItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                  removeProperty(classType, name);
                }

              });
              popup.show(table, e.getX(), e.getY()+8);
            }
          }
        }
      }

    });
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
    contentPane.add(toolbar, BorderLayout.NORTH);
    JScrollPane tableScroller = new JScrollPane(table);
    contentPane.add(tableScroller, BorderLayout.CENTER);
    JToolBar buttonbar = new JToolBar();
    buttonbar.setFloatable(false);
    contentPane.add(buttonbar, BorderLayout.SOUTH);
    descriptionLabel = new JLabel();
    descriptionLabel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 6));
    toolbar.add(descriptionLabel);
    // assemble locale dropdown
    localeDropDown = new JComboBox();
    LocaleItem selectedItem = new LocaleItem(locale);
    localeDropDown.addItem(selectedItem);
    Locale[] locales = OSPRuntime.getInstalledLocales();
    for(int i = 0; i<locales.length; i++) {
      if(locales[i].getDisplayLanguage().equals(locale.getDisplayLanguage())) {
        continue;
      }
      addDropDownItem(new LocaleItem(locales[i]));
    }
    localeDropDown.setSelectedItem(selectedItem);
    localeDropDown.setEditable(true);
    localeDropDown.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object next = localeDropDown.getSelectedItem();
        if(next==null) {
          return;
        }
        if(next instanceof LocaleItem) {
          ToolsRes.setLocale(((LocaleItem) next).loc);
        } else if(isLanguage(next.toString())) {
          String lang = new Locale(next.toString()).getLanguage();
          // select existing LocaleItem, if any
          Locale locale = null;
          LocaleItem item = null;
          for(int i = 0; i<localeDropDown.getItemCount(); i++) {
            item = (LocaleItem) localeDropDown.getItemAt(i);
            if(lang.equals(item.loc.getLanguage())) {
              locale = item.loc;
              break;
            }
            item = null;
          }
          if(locale==null) {
            locale = new Locale(next.toString());
            Map<String, String> properties = getProperties(classType, locale);
            flagChange(properties);
          }
          ToolsRes.setLocale(locale);
          if(item!=null) {
            localeDropDown.setSelectedItem(item);
          }
          localeDropDown.getEditor().selectAll();
        } else {
          localeDropDown.setSelectedIndex(0);
          localeDropDown.getEditor().selectAll();
        }
      }

    });
    toolbar.add(localeDropDown);
    // create buttons
    helpButton = new JButton();
    helpButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        TextFrame frame;
        // show helpURL if available
        if(ResourceLoader.getResource(helpURL)!=null) {
          frame = new TextFrame(helpURL);
        } else {
          String htmlFile = "/org/opensourcephysics/resources/tools/html/translator_tool_help.html"; //$NON-NLS-1$
          frame = new TextFrame(htmlFile);
        }
        frame.setSize(800, 600);
        frame.setVisible(true);
      }

    });
    buttonbar.add(helpButton);
    String imageFile = "/org/opensourcephysics/resources/tools/images/save.gif"; //$NON-NLS-1$
    saveIcon = ResourceLoader.getIcon(imageFile);
    saveButton = new JButton(saveIcon);
    saveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        save(getPath(classType)+fileExtension);
      }

    });
    saveButton.setEnabled(false);
    buttonbar.add(Box.createHorizontalGlue());
    buttonbar.add(saveButton);
    closeButton = new JButton();
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }

    });
    buttonbar.add(closeButton);
    pack();
    // center this on the screen
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (dim.width-getBounds().width)/2;
    int y = (dim.height-getBounds().height)/2;
    setLocation(x, y);
    // listen to ToolsRes for locale changes
    ToolsRes.addPropertyChangeListener("locale", new PropertyChangeListener() { //$NON-NLS-1$
      public void propertyChange(PropertyChangeEvent e) {
        Locale locale = (Locale) e.getNewValue();
        if(locale!=null) {
          setLocale(locale);
        }
      }

    });
    table.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        // update current properties map
        Object val = e.getNewValue();
        if(val instanceof TableModelEvent) {
          TableModelEvent event = (TableModelEvent) val;
          int row = event.getFirstRow();
          if(row<0) {
            return;
          }
          Map<String, String> properties = getProperties(classType, locale);
          String key = (String) table.getValueAt(row, 0);
          String alias = (String) table.getValueAt(row, 1);
          if((alias!=null)&&!alias.equals("")) { //$NON-NLS-1$
            properties.put(key, alias);
          } else {
            table.setValueAt(key, row, 1);
            properties.put(key, key);
          }
          // refresh objects associated with current class
          refreshAssociates(classType);
          // add properties to changed set
          flagChange(properties);
          // enable save button
          saveButton.setEnabled(true);
        }
      }

    });
  }

  /**
   * Refreshes the GUI.
   */
  protected void refreshGUI() {
    String fileName = XML.getName(getPath(classType));
    if (preferredTitle==null) {
      String title = ToolsRes.getString("TranslatorTool.Title"); //$NON-NLS-1$
      title += " "+fileName; //$NON-NLS-1$
      setTitle(title);
    }
    else setTitle(preferredTitle);

    fileName += fileExtension;
    if(classType!=null) {
      Map<String, String> properties = getProperties(classType, locale);
      saveButton.setEnabled(changed.contains(properties));
    }
    helpButton.setText(ToolsRes.getString("Tool.Button.Help"));                                   //$NON-NLS-1$
    helpButton.setToolTipText(ToolsRes.getString("Tool.Button.Help.ToolTip"));                    //$NON-NLS-1$
    saveButton.setText(ToolsRes.getString("TranslatorTool.Button.Save"));                         //$NON-NLS-1$
    saveButton.setToolTipText(ToolsRes.getString("TranslatorTool.Button.Save.ToolTip")+fileName); //$NON-NLS-1$
    closeButton.setText(ToolsRes.getString("Tool.Button.Close"));                                 //$NON-NLS-1$
    closeButton.setToolTipText(ToolsRes.getString("Tool.Button.Close.ToolTip"));                  //$NON-NLS-1$
    descriptionLabel.setText(ToolsRes.getString("TranslatorTool.Label.Description"));             //$NON-NLS-1$
    table.refresh();
  }

  /**
   * Refreshes objects associated with the specified class.
   */
  protected void refreshAssociates(Class<?> type) {
    Iterator<Object> it = getAssociates(type).iterator();
    while(it.hasNext()) {
      Object obj = it.next();
      if(obj instanceof XMLTable) {
        ((XMLTable) obj).refresh();
      } else if(obj instanceof PropertyChangeListener) {
        ((PropertyChangeListener) obj).propertyChange(new java.beans.PropertyChangeEvent(TOOL, "translation", null, null)); //$NON-NLS-1$
      }
    }
  }

  private synchronized void flagChange(Map<String, String> properties) {
    synchronized(changed) {
      changed.add(properties);
    }
  }

  /**
   *              A class to display languages in a JComboBox.
   */
  private class LocaleItem {
    Locale loc;
    String language;

    LocaleItem(Locale locale) {
      loc = locale;
      language = OSPRuntime.getDisplayLanguage(loc);
      if(isDefault()) {
        language += " ("+ToolsRes.getString("TranslatorTool.Language.Default")+")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
    }

    public String toString() {
      return language;
    }

    public boolean isDefault() {
      return loc.getDisplayLanguage(loc).equals(Locale.getDefault().getDisplayLanguage(loc));
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
