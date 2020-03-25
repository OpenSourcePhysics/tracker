/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.TreePath;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.ResizableIcon;
import org.opensourcephysics.display.TextFrame;
import org.opensourcephysics.media.core.VideoIO;
import org.opensourcephysics.tools.LibraryCollection;
import org.opensourcephysics.tools.LibraryTreePanel;
import org.opensourcephysics.tools.LibraryResource.Metadata;

/**
 * A GUI for browsing OSP digital library collections.
 *
 * @author Douglas Brown
 */
public class LibraryBrowser extends JPanel {
	
  // static constants
	@SuppressWarnings("javadoc")
	public static final String TRACKER_LIBRARY = "http://physlets.org/tracker/library/tracker_library.xml"; //$NON-NLS-1$
	@SuppressWarnings("javadoc")
	public static final String SHARED_LIBRARY = "http://physlets.org/tracker/library/shared_library.xml"; //$NON-NLS-1$
	protected static final String AND = " AND "; //$NON-NLS-1$
	protected static final String OR = " OR "; //$NON-NLS-1$
	protected static final String OPENING = "("; //$NON-NLS-1$
	protected static final String CLOSING = ")"; //$NON-NLS-1$
	protected static final String MY_LIBRARY_NAME = "my_library.xml"; //$NON-NLS-1$
	protected static final String MY_COLLECTION_NAME = "my_collection.xml"; //$NON-NLS-1$
	protected static final String LIBRARY_HELP_NAME = "library_browser_help.html"; //$NON-NLS-1$
	protected static final String LIBRARY_HELP_BASE = "http://www.opensourcephysics.org/online_help/tools/"; //$NON-NLS-1$
	protected static final String WINDOWS_OSP_DIRECTORY = "/My Documents/OSP/"; //$NON-NLS-1$
	protected static final String OSP_DIRECTORY = "/Documents/OSP/"; //$NON-NLS-1$
	  
	// static fields
	private static LibraryBrowser browser;
	protected static Border buttonBorder;
  protected static boolean webConnected;
  protected static JFrame frame;
  protected static JDialog externalDialog;
  protected static JMenuBar menubar;
  protected static ResizableIcon expandIcon, contractIcon, heavyExpandIcon, heavyContractIcon, refreshIcon;
  protected static final FileFilter TRACKER_FILTER = new TrackerDLFilter();
  protected static javax.swing.filechooser.FileFilter filesAndFoldersFilter =  new FilesAndFoldersFilter();
  protected static Timer searchTimer;
  protected static String searchTerm;
	public static boolean fireHelpEvent = false;
  
	static {
    buttonBorder = BorderFactory.createEtchedBorder();
    Border space = BorderFactory.createEmptyBorder(1,2,2,2);
    buttonBorder = BorderFactory.createCompoundBorder(buttonBorder, space);
    space = BorderFactory.createEmptyBorder(0,1,0,1);
    buttonBorder = BorderFactory.createCompoundBorder(space, buttonBorder);
    menubar = new JMenuBar();
    String imageFile = "/org/opensourcephysics/resources/tools/images/expand.png";        //$NON-NLS-1$
    expandIcon = new ResizableIcon(new ImageIcon(LibraryTreePanel.class.getResource(imageFile)));
    imageFile = "/org/opensourcephysics/resources/tools/images/contract.png";        //$NON-NLS-1$
    contractIcon = new ResizableIcon(new ImageIcon(LibraryTreePanel.class.getResource(imageFile)));
    imageFile = "/org/opensourcephysics/resources/tools/images/expand_bold.png";        //$NON-NLS-1$
    heavyExpandIcon = new ResizableIcon(new ImageIcon(LibraryTreePanel.class.getResource(imageFile)));
    imageFile = "/org/opensourcephysics/resources/tools/images/contract_bold.png";        //$NON-NLS-1$
    heavyContractIcon = new ResizableIcon(new ImageIcon(LibraryTreePanel.class.getResource(imageFile)));
    imageFile = "/org/opensourcephysics/resources/tools/images/refresh.gif";        //$NON-NLS-1$
    refreshIcon = new ResizableIcon(new ImageIcon(LibraryTreePanel.class.getResource(imageFile)));
	}
	
	// instance fields
  protected Library library = new Library();
  protected String libraryPath;
  protected JToolBar toolbar;
  protected Action commandAction, searchAction, openRecentAction;
  protected JLabel commandLabel, searchLabel;
  protected JTextField commandField, searchField;
  protected JMenu fileMenu, recentMenu, collectionsMenu, manageMenu, helpMenu;
  protected JMenuItem newItem, openItem, saveItem, saveAsItem, closeItem, closeAllItem,
  		exitItem, deleteItem, collectionsItem, searchItem, cacheItem, aboutItem, logItem, helpItem;
  protected JButton commandButton, editButton, refreshButton;
  protected ActionListener loadCollectionAction;
  protected boolean exitOnClose;
  protected JTabbedPane tabbedPane;
  protected JScrollPane htmlScroller;
  protected PropertyChangeListener treePanelListener;
  protected boolean keyPressed, textChanged;
  protected TextFrame helpFrame;
  protected JEditorPane htmlAboutPane;
  protected FileFilter dlFileFilter = TRACKER_FILTER;
  protected boolean isRecentPathXML; 
	protected LibraryManager libraryManager;

	/**
	 * Gets the shared singleton browser.
	 * 
	 * @return the shared LibraryBrowser
	 */
  public static LibraryBrowser getBrowser() {
  	if (browser==null) {
  		browser = getBrowser(null);
  	}
  	return browser;
  }

  
  /**
	 * Gets the shared singleton browser in a JDialog or, if none, in a shared JFrame.
	 * 
	 * @param dialog a JDialog (if null, browser is returned in a JFrame)
	 * @return the shared LibraryBrowser
	 */
  public static LibraryBrowser getBrowser(JDialog dialog) {
  	boolean newFrame = false;
  	if (frame==null && dialog==null) {
  		newFrame = true;
  		frame = new JFrame();
  	}
  	externalDialog = dialog;
  	if (externalDialog!=null)
  		externalDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 	
  	if (browser==null) {
  		String userHome = OSPRuntime.getUserHome().replace('\\', '/');
  		String ospFolder = OSPRuntime.isWindows()? WINDOWS_OSP_DIRECTORY: OSP_DIRECTORY;
  		String ospPath = userHome+ospFolder;
  		// if OSP folder doesn't exist in user home, then look 
  		// in default OSPRuntime search directory
  		if (!new File(ospPath).exists()) {
    		ArrayList<String> dirs = OSPRuntime.getDefaultSearchPaths();
				ospPath = XML.forwardSlash(dirs.get(0));
  		}
			if (!ospPath.endsWith("/")) { //$NON-NLS-1$
				ospPath += "/"; //$NON-NLS-1$
			}
    	String libraryPath = ospPath+MY_LIBRARY_NAME;
      File libraryFile = new File(libraryPath);
    	// create new library if none exists
      boolean libraryExists = libraryFile.exists();
      if (!libraryExists) {
      	String collectionPath = ospPath+MY_COLLECTION_NAME;      	
  			File collectionFile = new File(collectionPath);
      	// create new collection if none exists
        if (!collectionFile.exists()) {
          String name = ToolsRes.getString("LibraryCollection.Name.Local"); //$NON-NLS-1$
    			LibraryCollection collection = new LibraryCollection(name);
    			String base = XML.getDirectoryPath(collectionPath);
    			collection.setBasePath(XML.forwardSlash(base));
    			// save new collection
    			XMLControl control = new XMLControlElement(collection);
    			control.write(collectionPath);
        }
        Library library = new Library();
        String name = ToolsRes.getString("LibraryCollection.Name.Local"); //$NON-NLS-1$
        library.addCollection(collectionPath, name);
        library.save(libraryPath);
      }
  		browser = new LibraryBrowser(libraryPath);

      LibraryTreePanel treePanel = browser.getSelectedTreePanel();
  		if (treePanel!=null) {
  			treePanel.setSelectedNode(treePanel.rootNode);
  			treePanel.showInfo(treePanel.rootNode);
  		}
  		OSPLog.getOSPLog(); // instantiate log in case of exceptions, etc 
  	}
  	  	
  	browser.setTitle(ToolsRes.getString("LibraryBrowser.Title")); //$NON-NLS-1$
    if (externalDialog!=null) {
    	externalDialog.setContentPane(browser);
    	externalDialog.setJMenuBar(menubar);
    	externalDialog.addWindowListener(new WindowAdapter() {
	      public void windowClosing(WindowEvent e) {
	        browser.exit();
	      }
	    });
    	externalDialog.pack();
    }
    else {
	  	frame.setContentPane(browser);
    	frame.setJMenuBar(menubar);
	    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	    // add window listener to exit
	    frame.addWindowListener(new WindowAdapter() {
	      public void windowClosing(WindowEvent e) {
	        browser.exit();
	      }
	    });
	    try {
	      java.net.URL url = LibraryBrowser.class.getResource(OSPRuntime.OSP_ICON_FILE);
	      ImageIcon icon = new ImageIcon(url);
	      frame.setIconImage(icon.getImage());
	    } catch(Exception ex) {} 
    	frame.pack();
    	if (newFrame) {
        // center on screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (dim.width - frame.getBounds().width) / 2;
        int y = (dim.height - frame.getBounds().height) / 2;
        frame.setLocation(x, y);    		
    	}
    }
  	
  	return browser;
  }
  
  /**
   * Sets the font level.
   *
   * @param level the desired font level
   */
  public void setFontLevel(int level) {
		FontSizer.setFonts(this.getTopLevelAncestor(), level);
		Font font = tabbedPane.getFont();	
		tabbedPane.setFont(FontSizer.getResizedFont(font, level));
		for (int i=0; i<tabbedPane.getTabCount(); i++) {
			LibraryTreePanel treePanel = getTreePanel(i);
  		treePanel.setFontLevel(level);
		}
		if (libraryManager!=null) {
			libraryManager.setFontLevel(level);
		}
		ResizableIcon[] icons = {expandIcon, contractIcon, heavyExpandIcon, heavyContractIcon, refreshIcon};
		for (ResizableIcon next: icons) {
			next.resize(FontSizer.getIntegerFactor());
		}    
		FontSizer.setFonts(OSPLog.getOSPLog(), level);
  }
  
  /**
   * Imports a library with a specified path.
   * 
   * @param path the path to the Library xml file
   */
	public void importLibrary(final String path) {		
		Runnable runner = new Runnable() {
			public void run() {
		  	library.importLibrary(path);		
        refreshCollectionsMenu();
			}
		};
		new Thread(runner).start();
	}
	
  /**
   * Adds an OSP-sponsored library with a specified path.
   * 
   * @param path the path to the Library xml file
   */
	public void addOSPLibrary(final String path) {		
		Runnable runner = new Runnable() {
			public void run() {
		  	library.addOSPLibrary(path);		
        refreshCollectionsMenu();
			}
		};
    new Thread(runner).start();
	}
	
  /**
   * Adds a ComPADRE collection with a specified path.
   * 
   * @param path the ComPADRE query
   */
	public void addComPADRECollection(String path) {
		library.addComPADRECollection(path, LibraryComPADRE.getCollectionName(path));
	}
	
	/**
	 * Refreshes the Collections menu.
	 */
  synchronized public void refreshCollectionsMenu() {
  	JMenu menu = collectionsMenu;
  	menu.removeAll();
		JMenu myLibraryMenu = new JMenu(ToolsRes.getString("Library.Name.Local")); //$NON-NLS-1$
		menu.add(myLibraryMenu);
  	if (!library.pathList.isEmpty()) {
	  	for (String path: library.pathList) {
	  		String name = library.pathToNameMap.get(path);
	      JMenuItem item = new JMenuItem(name);
	      myLibraryMenu.add(item);
	      item.addActionListener(loadCollectionAction);
	      item.setToolTipText(path);
	      item.setActionCommand(path);
	  	}
  	}
  	if (!library.comPADREPathList.isEmpty()) {
  		JMenu submenu = new JMenu(ToolsRes.getString("Library.Name.ComPADRE")); //$NON-NLS-1$
  		menu.add(submenu);
	  	for (String path: library.comPADREPathList) {
	  		String name = library.comPADREPathToNameMap.get(path);      
	  		JMenuItem item = new JMenuItem(name);
	  		submenu.add(item);
	      item.addActionListener(loadCollectionAction);
//	  		if (LibraryComPADRE.primary_only)
//	  			path += LibraryComPADRE.PRIMARY_ONLY;
	      item.setToolTipText(path);
	      item.setActionCommand(path);
	  	}
  	}
  	if (!library.ospPathList.isEmpty()) {
	  	for (String path: library.ospPathList) {
	  		Library lib = library.ospPathToLibraryMap.get(path);
	  		JMenu submenu = new JMenu(lib.getName());
	  		menu.add(submenu);
	  		populateSubMenu(submenu, lib);
	  	}
  	}
  	if (!library.importedPathList.isEmpty()) {
	  	menu.addSeparator();
	  	for (String path: library.importedPathList) {
	  		Library lib = library.importedPathToLibraryMap.get(path);
	  		JMenu submenu = new JMenu(lib.getName());
	  		menu.add(submenu);
	  		for (String next: lib.pathList) {
	    		String name = lib.pathToNameMap.get(next);
	        JMenuItem item = new JMenuItem(name);
	        submenu.add(item);
	        item.addActionListener(loadCollectionAction);
	        item.setToolTipText(next);
	        item.setActionCommand(next);	  			
	  		}
	  	}
  	}
  	FontSizer.setFonts(collectionsMenu, FontSizer.getLevel());
  }
  
	/**
	 * Populates a submenu.
	 * 
	 * @param menu the menu to populate
	 * @param lib the library with collections for the submenu
	 */
  private void populateSubMenu(JMenu menu, Library lib) {
		for (String next: lib.pathList) {
  		String name = lib.pathToNameMap.get(next);
      JMenuItem item = new JMenuItem(name);
      menu.add(item);
      item.addActionListener(loadCollectionAction);
      item.setToolTipText(next);
      item.setActionCommand(next);	  			
		}
  	if (!lib.subPathList.isEmpty()) {
	  	for (String path: lib.subPathList) {
	  		if (library.ospPathList.contains(path))
	  			continue;
	  		Library sublib = lib.subPathToLibraryMap.get(path);
	  		JMenu submenu = new JMenu(sublib.getName());
	  		menu.add(submenu);
	  		populateSubMenu(submenu, sublib);
	  	}
  	}

  }


	
  /**
   * Sets the title of this DL browser.
   * @param title the title
   */
  public void setTitle(String title) {
     if (frame!=null) {
    	frame.setTitle(title);
    }
    else if (externalDialog!=null) {
    	externalDialog.setTitle(title);
    }
  }
  
  /**
   * Gets the fileFilter used to determine which files are DL resources.
   * 
   * @return the file filter
   */
  public FileFilter getDLFileFilter() {
  	return dlFileFilter;
  }
  
  /**
   * Sets the fileFilter used to determine which files are DL resources.
   * @param filter the file filter (may be null)
    */
  public void setDLFileFilter(FileFilter filter) {
  	dlFileFilter = filter;
  }
  
  /**
   * Sets the visibility of this browser
   * @param vis true to show, false to hide
   */
  @Override
  public void setVisible(boolean vis) {
  	super.setVisible(vis);
  	if (externalDialog!=null) {
  		externalDialog.setVisible(vis);
  	}
  	else frame.setVisible(vis);
  }

  /**
   * Exits this browser.
   * @return true if exited, false if cancelled by user
   */
  public boolean exit() {
  	// request focus?
  	LibraryTreePanel selected = getSelectedTreePanel();
  	if (selected!=null)
  		selected.refreshEntryFields();
  	for (int i=0; i < tabbedPane.getTabCount(); i++) {
  		LibraryTreePanel treePanel = getTreePanel(i);
      if (!treePanel.saveChanges(getTabTitle(i))) return false; // true unless the user cancels      		
  	}
  	// determine which open tabs to save
  	ArrayList<String> tabsToSave = new ArrayList<String>();
  	int n = tabbedPane.getTabCount();
  	for (int i=0; i<n; i++) {
  		String path = getTreePanel(i).pathToRoot;
  		if (path.equals("")) continue; //$NON-NLS-1$
  		tabsToSave.add(path);
  	}
  	library.openTabPaths = tabsToSave.isEmpty()? null: tabsToSave.toArray(new String[tabsToSave.size()]);
  	// save library
  	library.save(libraryPath);
  	
  	if (exitOnClose) {
      System.exit(0);
    } else {
    	refreshGUI();
      setVisible(false);
    }
  	return true;
  }

//____________________ private and protected methods ____________________________

  /**
   * Private constructor to prevent instantiation except for singleton.
   * 
   * @param libraryPath the path to a Library xml file
   */
  private LibraryBrowser(String libraryPath) {
  	super(new BorderLayout());
  	this.libraryPath = libraryPath;
  	library.browser = this;
    createGUI();
    refreshGUI();
		refreshCollectionsMenu();
		editButton.requestFocusInWindow();
    ToolsRes.addPropertyChangeListener("locale", new PropertyChangeListener() { //$NON-NLS-1$
      public void propertyChange(PropertyChangeEvent e) {
        refreshGUI();
        refreshCollectionsMenu();
        if (libraryManager!=null)
        	libraryManager.refreshGUI();
        LibraryTreePanel.htmlPanesByNode.clear();
        LibraryTreePanel treePanel = getSelectedTreePanel();
        if (treePanel!=null)
        	treePanel.showInfo(treePanel.getSelectedNode());
      }
    });
  }
  
	/**
	 * Gets the library manager for this browser.
	 * 
	 * @return the collections manager
	 */
	protected LibraryManager getManager() {
		if (libraryManager==null) {
			if (externalDialog!=null)
				libraryManager = new LibraryManager(this, LibraryBrowser.externalDialog);
			else
				libraryManager = new LibraryManager(this, LibraryBrowser.frame);
	    // center on screen
	    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	    int x = (dim.width-libraryManager.getBounds().width)/2;
	    int y = (dim.height-libraryManager.getBounds().height)/2;
	    libraryManager.setLocation(x, y);
		}
		if (library.pathList.size()>0 && libraryManager.collectionList.getSelectedIndex()==-1) {
			libraryManager.collectionList.setSelectedIndex(0);
		}
		if (library.importedPathList.size()>0 && libraryManager.guestList.getSelectedIndex()==-1) {
			libraryManager.guestList.setSelectedIndex(0);
		}
		libraryManager.setFontLevel(FontSizer.getLevel());
		return libraryManager;
	}
  

  
  /**
   * Gets the selected LibraryTreePanel, if any.
   * 
   * @return the selected treePanel, or null if none
   */
  public LibraryTreePanel getSelectedTreePanel() {
  	return (LibraryTreePanel)tabbedPane.getSelectedComponent();
  }
  
  /**
   * Gets the LibraryTreePanel at a specified tab index.
   * 
   * @param index the tab index
   * @return the treePanel
   */
  protected LibraryTreePanel getTreePanel(int index) {  	
  	return (LibraryTreePanel)tabbedPane.getComponentAt(index);
  }

  /**
   * Gets the title of the tab associated with a given path.
   * 
   * @param path the collection path
   * @return the tab title
   */
  protected String getTabTitle(String path) {
  	int i = getTabIndexFromPath(path);
  	return i>-1? getTabTitle(i): null;
  }

  /**
   * Gets the title of the tab at a given index.
   * 
   * @param index the tab index
   * @return the tab title
   */
  protected String getTabTitle(int index) { 
  	String title = tabbedPane.getTitleAt(index);
		if (title.endsWith("*")) //$NON-NLS-1$
			title = title.substring(0, title.length()-1);
		return title;
  }

  /**
   * Gets the index of the tab associated with a given path.
   * 
   * @param path the collection path
   * @return the tab index
   */
  protected int getTabIndexFromPath(String path) { 
  	for (int i=0; i<tabbedPane.getTabCount(); i++) {
  		LibraryTreePanel next = getTreePanel(i);
  		if (next.pathToRoot.equals(path)) 
  			return i;
  	}
  	return -1;
  }

  /**
   * Gets the index of the tab associated with a given title.
   * 
   * @param title the tab title
   * @return the tab index
   */
  protected int getTabIndexFromTitle(String title) { 
  	for (int i=0; i<tabbedPane.getTabCount(); i++) {
  		String next = tabbedPane.getTitleAt(i);
  		if (next.equals(title)) 
  			return i;
  	}
  	return -1;
  }

  /**
   * Loads a tab from a given path. If the tab is already loaded, this selects it.
   * if not yet loaded, this adds a new tab and selects it.
   * If a treePath is specified, the node it points to will be selected 
   * 
   * @param path the path
   * @param treePath tree path to select in root-first order (may be null)
   */
  protected void loadTab(String path, List<String> treePath) {
  	path = XML.forwardSlash(path);
		library.addRecent(path, false);
		refreshRecentMenu();
    // select tab and treePath if path is already loaded
    int i = getTabIndexFromPath(path);
    if (i>-1) {
    	tabbedPane.setSelectedIndex(i);
    	LibraryTreePanel treePanel = getTreePanel(i);
    	treePanel.setSelectionPath(treePath);
    	return;
    }
    // otherwise add new tab
		TabLoader tabAdder = addTab(path, treePath);
		if (tabAdder==null) return;
  	tabAdder.addPropertyChangeListener(new PropertyChangeListener() {  
			public  void propertyChange(PropertyChangeEvent e) {  
			  if ("progress".equals(e.getPropertyName())) {   //$NON-NLS-1$
			    Integer n = (Integer)e.getNewValue();  
		    	if (n>-1) {
		    		tabbedPane.setSelectedIndex(n);
		    	}
			  }  
		  }  
		});
  	tabAdder.execute();  	
  }
  
  /**
   * Loads a library resource from a given path.
   * 
   * @param path the path
   * @return the resource, or null if failed
   */
  protected LibraryResource loadResource(String path) {
  	isRecentPathXML = false;
  	File targetFile = new File(path);
		if (targetFile.isDirectory()) {
			return createCollection(targetFile, targetFile, dlFileFilter);
		}
    if (LibraryComPADRE.isComPADREPath(path)) {
	  	return LibraryComPADRE.getCollection(path);
	  }
  	XMLControlElement control = new XMLControlElement(path);
  	if (!control.failedToRead() 
  			&& control.getObjectClass()!=null
  			&& LibraryResource.class.isAssignableFrom(control.getObjectClass())) {
    	isRecentPathXML = true;    	
  		return (LibraryResource)control.loadObject(null);
  	}    	  	
  	return createResource(targetFile, targetFile.getParentFile(), dlFileFilter);
  }
  
  /**
   * Creates a LibraryResource that describes and targets a file.
   * @param targetFile the target file
   * @param baseDir the base directory for relative paths
   * @param filter a FileFilter to determine if the file is a DL library resource
   * @return a LibraryResource that describes and targets the file
   */
	protected LibraryResource createResource(File targetFile, File baseDir, FileFilter filter) {
		if (targetFile==null || !targetFile.exists()) return null;
		if (!filter.accept(targetFile)) return null;
		
		String fileName = targetFile.getName();
		String path = XML.forwardSlash(targetFile.getAbsolutePath());
		String base = XML.forwardSlash(baseDir.getAbsolutePath());
		String relPath = XML.getPathRelativeTo(path, base);
    LibraryResource record = new LibraryResource(fileName);
    record.setBasePath(base);
    record.setTarget(relPath);
    
    fileName = fileName.toLowerCase();
    if (fileName.indexOf(".htm")>-1) { //$NON-NLS-1$
	  	record.setHTMLPath(relPath);
	  	record.setType(LibraryResource.HTML_TYPE);
    }
    if (fileName.endsWith(".zip")) { //$NON-NLS-1$
      if (filter==TRACKER_FILTER) {
      	record.setType(LibraryResource.TRACKER_TYPE);
      }
    }
    if (fileName.endsWith(".trz")) { //$NON-NLS-1$
      record.setType(LibraryResource.TRACKER_TYPE);
    }
    return record;
	}

	/**
   * Creates a LibraryCollection containing all DL resources in a target directory.
   * @param targetDir the target directory
   * @param base the base directory for relative paths
   * @param filter a FileFilter to determine which files are DL resources
   * @return the collection
   */
	protected LibraryCollection createCollection(File targetDir, File base, FileFilter filter) {
		// find HTML files in this folder 
		FileFilter htmlFilter = new HTMLFilter();
		File[] htmlFiles = targetDir.listFiles(htmlFilter);
		HashSet<File> matchedNames = new HashSet<File>();
		
		String name = targetDir.getName();
		LibraryCollection collection = new LibraryCollection(name);
		if (base==targetDir) { // set base path ONLY for the root directory
			collection.setBasePath(XML.forwardSlash(base.getAbsolutePath()));
		}
  	// look for HTML with name = folder name + "_info"
  	for (File htmlFile: htmlFiles) {
  		if (XML.stripExtension(htmlFile.getName()).equals(name+"_info")) { //$NON-NLS-1$
  			String relPath = XML.getPathRelativeTo(htmlFile.getAbsolutePath(), base.getAbsolutePath());
  			collection.setHTMLPath(relPath);
  			String htmlCode = ResourceLoader.getHTMLCode(htmlFile.getAbsolutePath());
  			String title = ResourceLoader.getTitleFromHTMLCode(htmlCode);
  			if (title!=null) {
  				collection.setName(title);
  			}
  			matchedNames.add(htmlFile);
  		}
  	}
  	
		// find subfolders
		File[] subdirs = targetDir.listFiles(new DirectoryFilter());
		for (File dir: subdirs) {
			LibraryCollection subCollection = createCollection(dir, base, filter);
			if (subCollection.getResources().length>0)
			collection.addResource(subCollection);
		}
		
		// find filtered DL resources
		File[] resourceFiles = filter==null? targetDir.listFiles(): targetDir.listFiles(filter);
		for (File next: resourceFiles) {
			if (htmlFilter.accept(next)) continue;
			String relPath = XML.getPathRelativeTo(next.getAbsolutePath(), base.getAbsolutePath());
			String fileName = next.getName();
			String baseName = XML.stripExtension(fileName);
      LibraryResource record = new LibraryResource(fileName);
      collection.addResource(record);
      record.setTarget(relPath);
      // assign resource type to zip files
      if (fileName.toLowerCase().endsWith(".zip")) { //$NON-NLS-1$
	      if (filter==TRACKER_FILTER) {
	      	record.setType(LibraryResource.TRACKER_TYPE);
	      }
      }
      if (fileName.toLowerCase().endsWith(".trz")) { //$NON-NLS-1$
      	record.setType(LibraryResource.TRACKER_TYPE);
      }
    	// look for HTML with base name + "_info"
    	for (File htmlFile: htmlFiles) {
				String htmlName = XML.stripExtension(htmlFile.getName());   		
    		if (htmlName.equals(baseName+"_info")) { //$NON-NLS-1$
          if ("".equals(record.getHTMLPath())) { //$NON-NLS-1$
      			relPath = XML.getPathRelativeTo(htmlFile.getAbsolutePath(), base.getAbsolutePath());
	    			record.setHTMLPath(relPath);
	    			String htmlCode = ResourceLoader.getHTMLCode(htmlFile.getAbsolutePath());
	    			String title = ResourceLoader.getTitleFromHTMLCode(htmlCode);
		  			if (title!=null) {
		  				record.setName(title);
		  			}
          }
    			matchedNames.add(htmlFile);
    			break;
    		}
    	}

		}
  	// insert unmatched HTML files at top of the collection
		int i = 0;
  	for (File html: htmlFiles) {
  		if (matchedNames.contains(html) || !filter.accept(html)) continue;
			String fileName = html.getName();
      LibraryResource record = new LibraryResource(fileName);
			String relPath = XML.getPathRelativeTo(html.getAbsolutePath(), base.getAbsolutePath());
	  	record.setHTMLPath(relPath);
	  	record.setType(LibraryResource.HTML_TYPE);
      collection.insertResource(record, i++);  		
  	}
		return collection;
	}
	
  /**
   * Adds a tab displaying a library resource with a given path.
   * If a treePath is specified, the node it points to will be selected 
   * 
   * @param path the path to the resource
   * @param treePath tree path to select in root-first order (may be null)
   * @return the TabLoader that adds the tab
   */
  protected TabLoader addTab(String path, List<String> treePath) {
  	if (path==null) return null;
		File cachedFile = ResourceLoader.getSearchCacheFile(path);
  	boolean isCachePath = cachedFile.exists();
  	System.out.println("LibraryBrowser tab " + path);
  	if (!isCachePath && !isWebConnected() && path.startsWith("http:")) { //$NON-NLS-1$
  		JOptionPane.showMessageDialog(this, 
  				ToolsRes.getString("LibraryBrowser.Dialog.ServerUnavailable.Message"), //$NON-NLS-1$
  				ToolsRes.getString("LibraryBrowser.Dialog.ServerUnavailable.Title"), //$NON-NLS-1$
  				JOptionPane.WARNING_MESSAGE);  		
  		return null;
  	}
  	TabLoader tabAdder = new TabLoader(path, -1, treePath);
  	return tabAdder;
  }
  
  /**
   * Refreshes the title of a tab based on the properties of a LibraryCollection
   * and the path associated with that collection.
   * 
   * @param path the collection path
   * @param collection the LibraryCollection itself
   */
  protected void refreshTabTitle(String path, LibraryResource collection) {
  	int n = getTabIndexFromPath(path);
  	if (n==-1) return;
  	
  	String title = collection.getTitle(path);
  	
    // add a TabTitle with expand and contract icons to ComPADRE tab 
    if (path.contains(LibraryComPADRE.TRACKER_SERVER_TREE) && tabbedPane.getTabComponentAt(n)==null) {
    	boolean primary = path.contains("OSPPrimary"); //$NON-NLS-1$
    	Icon icon = primary? expandIcon: contractIcon;
    	Icon heavyIcon = primary? heavyExpandIcon: heavyContractIcon;
    	final TabTitle tabTitle = new TabTitle(icon, heavyIcon);
    	FontSizer.setFonts(tabTitle, FontSizer.getLevel());
	  	tabTitle.iconLabel.setToolTipText(primary? ToolsRes.getString("LibraryBrowser.Tooltip.Expand"): //$NON-NLS-1$
	  			ToolsRes.getString("LibraryBrowser.Tooltip.Contract")); //$NON-NLS-1$
    	Action action = new AbstractAction() {
  		  public void actionPerformed(ActionEvent e) {
  		  	boolean primaryOnly = tabTitle.normalIcon==contractIcon;
  		  	int index = getTabIndexFromTitle(tabTitle.titleLabel.getText());
  		  	if (index>-1) {
	  	  		LibraryTreePanel treePanel = getTreePanel(index);
	  		  	String path = LibraryComPADRE.getCollectionPath(treePanel.pathToRoot, primaryOnly);	  		  	
	  		  	new TabLoader(path, index, null).execute();
	  		  	
	  		  	tabTitle.setIcons(primaryOnly? expandIcon: contractIcon, primaryOnly? heavyExpandIcon: heavyContractIcon);
	  		  	tabTitle.iconLabel.setToolTipText(primaryOnly? ToolsRes.getString("LibraryBrowser.Tooltip.Expand"): //$NON-NLS-1$
	  		  		ToolsRes.getString("LibraryBrowser.Tooltip.Contract")); //$NON-NLS-1$
  		  	}
  		  }
    	};
    	tabTitle.setAction(action);
    	tabbedPane.setTabComponentAt(n, tabTitle);
    }
		boolean changed = getTreePanel(n).isChanged();
  	tabbedPane.setTitleAt(n, changed? title+"*": title);  //$NON-NLS-1$
  	library.getNameMap().put(path, title);
  	if (n==tabbedPane.getSelectedIndex()) {
	    String tabname = " '"+title+"'"; //$NON-NLS-1$ //$NON-NLS-2$
	    closeItem.setText(ToolsRes.getString("LibraryBrowser.MenuItem.CloseTab")+tabname); //$NON-NLS-1$
  	}
  }

  /**
   * Creates the visible components of this panel.
   */
  protected void createGUI() {
  	double factor = 1+ FontSizer.getLevel()*0.25;
  	int w = (int)(factor*800);
  	int h = (int)(factor*440);
    setPreferredSize(new Dimension(w, h));

    loadCollectionAction = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        loadTab(e.getActionCommand(), null);
      }
    };
    
    // create command action, label, field and button
		commandAction = new AbstractAction() {
		  public void actionPerformed(ActionEvent e) {
		  	if (e==null) return;
      	commandField.setBackground(Color.white);
      	commandField.setForeground(LibraryTreePanel.defaultForeground);
		  	if (!commandButton.isEnabled()) return;
        String path = commandField.getText().trim();
        if (path.equals("")) return; //$NON-NLS-1$
				path = XML.forwardSlash(path);
        path = ResourceLoader.getNonURIPath(path);
        Resource res = null;
    		String xmlPath = path;
        
        // if path has no extension, look for xml file with same name
        if (!path.startsWith("http://www.compadre.org/OSP/") //$NON-NLS-1$
        		&& XML.getExtension(path)==null) {
      		while (xmlPath.endsWith("/")) //$NON-NLS-1$
      			xmlPath = xmlPath.substring(0, xmlPath.length()-1);
      		if (!xmlPath.equals("")) { //$NON-NLS-1$
      			String name = XML.getName(xmlPath);
      			xmlPath += "/"+name+".xml"; //$NON-NLS-1$ //$NON-NLS-2$
            res = ResourceLoader.getResource(xmlPath);
      		}
        }
        
        if (res!=null)
        	path = xmlPath;
        else 
        	res = ResourceLoader.getResourceZipURLsOK(path);
        
        if (res==null) {
        	commandField.setForeground(LibraryTreePanel.darkRed);
        	return;
        }
        
        boolean isCollection = res.getFile()!=null && res.getFile().isDirectory();
        if (!isCollection)	{
	  			XMLControl control = new XMLControlElement(path);
	  			isCollection = !control.failedToRead() && control.getObjectClass()==LibraryCollection.class;         	
        }
        
        if (isCollection) {
      		loadTab(path, null);
      		refreshGUI();
      		LibraryTreePanel treePanel = getSelectedTreePanel();
      		if (treePanel!=null && treePanel.pathToRoot.equals(path)) {
	      		treePanel.setSelectedNode(treePanel.rootNode);
	      		commandField.setBackground(Color.white);
	      		commandField.repaint();
      		}
      		return;
        }
        
  			// send command
		  	LibraryResource record = null;
        LibraryTreePanel treePanel = getSelectedTreePanel();
        if (treePanel!=null && treePanel.getSelectedNode()!=null) {
        	record = treePanel.getSelectedNode().record.getClone();
        	record.setBasePath(treePanel.getSelectedNode().getBasePath());
        }
	    	else {
	    		record = new LibraryResource(""); //$NON-NLS-1$
	    		record.setTarget(path);
	    	}
	    	LibraryBrowser.this.firePropertyChange("target", null, record); //$NON-NLS-1$
		  }		
		};
    commandLabel = new JLabel();
    commandLabel.setAlignmentX(CENTER_ALIGNMENT);
    commandLabel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 2));
    commandField = new JTextField() {
    	public Dimension getPreferredSize() {
    		Dimension dim = super.getPreferredSize();
    		dim.width = Math.max(dim.width, 400);
    		return dim;
    	}
    };
    LibraryTreePanel.defaultForeground = commandField.getForeground();
    commandField.addActionListener(commandAction);
    commandField.getDocument().addDocumentListener(new DocumentListener() {   
      public void insertUpdate(DocumentEvent e) {
      	String text = commandField.getText();
        commandButton.setEnabled(!"".equals(text)); //$NON-NLS-1$
        textChanged = keyPressed;
        LibraryTreePanel treePanel = getSelectedTreePanel();
        if (treePanel!=null) {
        	treePanel.command = text;
        	LibraryTreeNode node = treePanel.getSelectedNode();
        	if (node!=null && node.isRoot() && node.record instanceof LibraryCollection && treePanel.pathToRoot.equals(text))
        		commandButton.setEnabled(false);
        }
        else {
        	commandField.setBackground(Color.yellow);
        	commandField.setForeground(LibraryTreePanel.defaultForeground);
        }
      }
      public void removeUpdate(DocumentEvent e) {
        commandButton.setEnabled(!"".equals(commandField.getText())); //$NON-NLS-1$
        textChanged = keyPressed;
        LibraryTreePanel treePanel = getSelectedTreePanel();
        if (treePanel!=null) {
        	treePanel.command = commandField.getText();
        }
        else {
        	commandField.setBackground(Color.yellow);
        	commandField.setForeground(LibraryTreePanel.defaultForeground);
        }
      }
			public void changedUpdate(DocumentEvent e) {}
  	});
    commandField.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        keyPressed = true;
      }
      public void keyReleased(KeyEvent e) {
        LibraryTreePanel treePanel = getSelectedTreePanel();
        if (treePanel!=null && textChanged && e.getKeyCode()!=KeyEvent.VK_ENTER) {
        	commandField.setBackground(Color.yellow);
        	commandField.setForeground(LibraryTreePanel.defaultForeground);
          treePanel.setSelectedNode(null);
        }
        textChanged = keyPressed = false;
      }
    });
    commandField.addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) {
      	commandField.selectAll();
      }
    });

    commandButton = new JButton(commandAction);
    commandButton.setOpaque(false);
    commandButton.setBorder(buttonBorder);
    
    // create search action, label, field and button
		searchAction = new AbstractAction() {
		  public void actionPerformed(ActionEvent e) {
		  	searchTerm = searchField.getText();
		  	if ("".equals(searchTerm.trim())) return; //$NON-NLS-1$
		  	searchField.selectAll();
		  	searchField.setBackground(Color.white);
		  	
		  	// do actual search in separate swingworker thread
				class Searcher extends SwingWorker<LibraryTreePanel, Object> {
		      @Override
		      public LibraryTreePanel doInBackground() {
				  	// search all cache targets except those in the library no_search set
				  	Set<LibraryResource> searchTargets = getSearchCacheTargets();
				  	for (Iterator<LibraryResource> it = searchTargets.iterator(); it.hasNext();) {
				  		LibraryResource next = it.next();
							if (library.noSearchSet.contains(next.collectionPath))
								it.remove();
				  	}
		      	return searchFor(searchTerm.trim(), searchTargets);
		      }

		      @Override
		      protected void done() {
	          try {
	          	LibraryTreePanel results = get();
       		  	if (results==null) {
      	        Toolkit.getDefaultToolkit().beep();
      	        // give visual cue, too
      	        final Color color = searchField.getForeground();
      	        searchField.setText(ToolsRes.getString("LibraryBrowser.Search.NotFound")); //$NON-NLS-1$
      	        searchField.setForeground(Color.RED);
      	        searchField.setBackground(Color.white);
      	    		if (searchTimer==null) {
      	    			searchTimer = new Timer(1000, new ActionListener() {
      	    				 public void actionPerformed(ActionEvent e) {
      	      	        searchField.setText(searchTerm);
      	      	        searchField.setForeground(color);
      	      			  	searchField.selectAll();
      	      	        searchField.setBackground(Color.white);
      	    				 }
      	    			 });
      	    			searchTimer.setRepeats(false);
      	    			searchTimer.start();
      	    		}
      	    		else {
      	    			searchTimer.restart();
      	    		}
      	        
      		  		return;
      		  	}
      		  	String title = "'"+searchTerm.trim()+"'"; //$NON-NLS-1$ //$NON-NLS-2$
      		  	int i = getTabIndexFromTitle(title);
      		  	synchronized (tabbedPane) {
  							if (i > -1) {
  								// replace existing tab
  								tabbedPane.setComponentAt(i, results);
  							} 
  							else {
  								tabbedPane.addTab(title, results);
  							}
								tabbedPane.setSelectedComponent(results);
  						}
      				LibraryTreePanel.htmlPanesByNode.remove(results.rootNode);  
      		  	results.showInfo(results.rootNode);

  						refreshGUI();
						} catch (Exception e) {							
    	        Toolkit.getDefaultToolkit().beep();
						}
		      }
				}		 
				new Searcher().execute();
		  }		
		};
    searchLabel = new JLabel();
    searchLabel.setAlignmentX(CENTER_ALIGNMENT);
    searchLabel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 2));
    searchField = new LibraryTreePanel.EntryField() {
  		public Dimension getMaximumSize() {	
  			Dimension dim = super.getMaximumSize();
  			dim.width = (int)(120*(1+FontSizer.getLevel()*0.25));
  			return dim;
  		}
  		
  		public Dimension getPreferredSize() {
  			Dimension dim = super.getPreferredSize();
  			dim.width = (int)(120*(1+FontSizer.getLevel()*0.25));
  			return dim;
  		}

    };
    searchField.addActionListener(searchAction);
    
    refreshButton = new JButton(refreshIcon);
    refreshButton.setBorder(buttonBorder);
    refreshButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	LibraryTreePanel treePanel = getSelectedTreePanel();
      	if (treePanel!=null) {
	        LibraryTreeNode node = treePanel.getSelectedNode();
	        // if node is root, delete the cache file, if any, and reload the entire collection
      		if (node==treePanel.rootNode) {
		    		File cachedFile = ResourceLoader.getSearchCacheFile(treePanel.pathToRoot);
		    		if (cachedFile.exists()) {
		    			cachedFile.delete();
		    		}
      			// reload the root resource or directory
      			LibraryResource resource = loadResource(treePanel.pathToRoot);
      			if (resource!=null) {
      				treePanel.setRootResource(resource, treePanel.pathToRoot, treePanel.rootNode.isEditable(), false);
    	    		refreshTabTitle(treePanel.pathToRoot, treePanel.rootResource);
    			    // start background SwingWorker to load metadata and set up search database
    	    		if (treePanel.metadataLoader!=null) {
    	    			treePanel.metadataLoader.cancel();
    	    		}
    	    		treePanel.metadataLoader = treePanel.new MetadataLoader(true, null);
    	    		treePanel.metadataLoader.execute();
      				return;
      			}
      		}
	        // for other nodes delete cached files and reload the node
      		else if (node!=null) {
      			LibraryTreePanel.HTMLPane pane = new LibraryTreePanel.HTMLPane();
      			pane.setText("<h2>"+ToolsRes.getString("LibraryBrowser.Info.Refreshing")+" '"+node+"'</h2>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      			treePanel.htmlScroller.setViewportView(pane);
	        	URL url = node.getHTMLURL(); // returns cached file URL, if any
	        	if (url!=null) {
			    		File cachedFile = ResourceLoader.getOSPCacheFile(url.toExternalForm());
			    		if (cachedFile.exists()) {
			    			cachedFile.delete();
			    		}	        		
		        	LibraryTreePanel.htmlPanesByURL.remove(url);
	        	}

		        // delete thumbnail image, if any
		        String target = node.getAbsoluteTarget();
		        if (target!=null) {
		        	File thumb = node.getThumbnailFile();
		        	if (thumb.exists()) {
		        		thumb.delete();
		        		node.record.setThumbnail(null);
		        	}
		        }
		        // clear metadata and description
		        node.record.setMetadata(null);
		        node.record.setDescription(null);
		        
		        treePanel.new NodeLoader(node).execute();
	        }
      	}
      }
    });
   
    tabbedPane = new JTabbedPane(SwingConstants.TOP) {
    	@Override
    	public void setTitleAt(int i, String title) {
  			super.setTitleAt(i, title);
    		Component c = tabbedPane.getTabComponentAt(i);
    		if (c!=null) {
    			TabTitle tabTitle = (TabTitle)c;
    			tabTitle.setTitle(title);
    		}
    	}
    };
    tabbedPane.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
      	refreshGUI();
      	LibraryTreePanel treePanel = getSelectedTreePanel();
      	if (treePanel!=null) {
	        LibraryTreeNode node = treePanel.getSelectedNode();
	        if (node!=null) {
	        	String path = node.isRoot()? treePanel.pathToRoot: node.getAbsoluteTarget();
	        	commandField.setText(path);
		        treePanel.showInfo(node);
	        }
	        else {
    	  		commandField.setText(treePanel.command);
    	  		commandField.setCaretPosition(0);
	        }
      	}
        commandField.setBackground(Color.white);
      	commandField.setForeground(LibraryTreePanel.defaultForeground);
      	if (libraryManager!=null && libraryManager.isVisible())
      		libraryManager.refreshGUI();
      }
    });
    tabbedPane.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if(OSPRuntime.isPopupTrigger(e)) {
          // make popup and add items
          JPopupMenu popup = new JPopupMenu();
          // close this tab
          JMenuItem item = new JMenuItem(ToolsRes.getString("MenuItem.Close")); //$NON-NLS-1$
          item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              int i = tabbedPane.getSelectedIndex();
              closeTab(i);
            }
          });
          popup.add(item);
          // add tab to Collections menu
          final LibraryTreePanel treePanel = getSelectedTreePanel();
          if (!"".equals(treePanel.pathToRoot) && !library.containsPath(treePanel.pathToRoot, false)) { //$NON-NLS-1$
	          item = new JMenuItem(ToolsRes.getString("LibraryBrowser.MenuItem.AddToLibrary")); //$NON-NLS-1$
	          item.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	              addToCollections(treePanel.pathToRoot);
	           }
	          });
	          popup.addSeparator();
	          popup.add(item);
          }
    	    FontSizer.setFonts(popup, FontSizer.getLevel());
          popup.show(tabbedPane, e.getX(), e.getY()+8);
        }
      }
    });
    
    // create property change listener for treePanels
    treePanelListener = new PropertyChangeListener() {
  		public void propertyChange(PropertyChangeEvent e) {
  			String propertyName = e.getPropertyName();
  			if (propertyName.equals("collection_edit")) { //$NON-NLS-1$
    			refreshGUI();
  			}
  			else if (propertyName.equals("target")) { //$NON-NLS-1$
    			LibraryResource record = null;
    			if (e.getNewValue() instanceof LibraryTreeNode) {
  	  			LibraryTreeNode node = (LibraryTreeNode)e.getNewValue();
  	  			if (node.record instanceof LibraryCollection) {
  		  			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
  	  				if (!LibraryComPADRE.loadResources(node)) {
    		  			setCursor(Cursor.getDefaultCursor());
  	  		  		JOptionPane.showMessageDialog(LibraryBrowser.this, 
  	  		  				ToolsRes.getString("LibraryBrowser.Dialog.NoResources.Message"), //$NON-NLS-1$
  	  		  				ToolsRes.getString("LibraryBrowser.Dialog.NoResources.Title"), //$NON-NLS-1$
  	  		  				JOptionPane.PLAIN_MESSAGE); 
  	  		  		return;
  	  				}
  	  				node.createChildNodes();
  	  				LibraryTreePanel.htmlPanesByNode.remove(node);
  	  		    LibraryTreeNode lastChild = (LibraryTreeNode)node.getLastChild();
  	  		  	TreePath path = new TreePath(lastChild.getPath());
  	  		  	getSelectedTreePanel().tree.scrollPathToVisible(path);
  	  				getSelectedTreePanel().showInfo(node);
  		  			setCursor(Cursor.getDefaultCursor());
  	  				return;
  	  			}
  	  			record = node.record.getClone();
  	  			record.setBasePath(node.getBasePath());
    			}
    			else record = (LibraryResource)e.getNewValue();
    			
    			String target = record.getTarget();
    			if (target!=null && (target.toLowerCase().endsWith(".pdf") //$NON-NLS-1$
    					 || target.toLowerCase().endsWith(".html") //$NON-NLS-1$
    					 || target.toLowerCase().endsWith(".htm"))) { //$NON-NLS-1$
    				target = XML.getResolvedPath(target, record.getBasePath());
    				target = ResourceLoader.getURIPath(target);
		  			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    				OSPDesktop.displayURL(target);
		  			setCursor(Cursor.getDefaultCursor());
    			}
    			else {
	    			// forward the event to browser listeners
	    			firePropertyChange("target", e.getOldValue(), record); //$NON-NLS-1$
    			}
  			}
  		}
  	};
    
    // create edit button
    editButton = new JButton();
    editButton.setOpaque(false);
	  editButton.setBorder(buttonBorder);
    editButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	final LibraryTreePanel treePanel = getSelectedTreePanel();
      	if (!treePanel.isEditing()) {
        	treePanel.setEditing(true);
        	refreshGUI();
      	}
      	else if (!treePanel.isChanged()) {
        	treePanel.setEditing(false);
        	refreshGUI();
      	}
      	else {
        	JPopupMenu popup = new JPopupMenu(); 
          JMenuItem item = new JMenuItem(ToolsRes.getString("LibraryBrowser.MenuItem.SaveEdits")); //$NON-NLS-1$
          popup.add(item);
          item.addActionListener(new ActionListener() {
      		  public void actionPerformed(ActionEvent e) {
            	String path = save();
            	if (path==null) return;
            	treePanel.setEditing(false);
            	refreshGUI();
            }
          });
          item = new JMenuItem(ToolsRes.getString("LibraryBrowser.MenuItem.Discard")); //$NON-NLS-1$
          popup.add(item);
          item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	treePanel.setEditing(false);
            	treePanel.revert();
            	refreshGUI();
      		  }		
      		});
    	    FontSizer.setFonts(popup, FontSizer.getLevel());
          popup.show(editButton, 0, editButton.getHeight());      		
      	}
      }
    });
    
    // assemble toolbar
    toolbar = new JToolBar();
    toolbar.setFloatable(false);
    Border empty = BorderFactory.createEmptyBorder(1, 2, 1, 2);
    Border etched = BorderFactory.createEtchedBorder();
    toolbar.setBorder(BorderFactory.createCompoundBorder(etched, empty));
    toolbar.add(commandLabel);
    toolbar.add(commandField);
    toolbar.add(commandButton);
    toolbar.addSeparator();
    toolbar.add(searchLabel);
    toolbar.add(searchField);
    toolbar.addSeparator();
    toolbar.add(editButton);
    toolbar.addSeparator();
    toolbar.add(refreshButton);

    add(toolbar, BorderLayout.NORTH);
    
    // menu items
    fileMenu = new JMenu();
    menubar.add(fileMenu);
    newItem = new JMenuItem();
    int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    newItem.setAccelerator(KeyStroke.getKeyStroke('N', mask));
    newItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	String path = createNewCollection();
    		library.addRecent(path, false);
    		refreshRecentMenu();
      }
    });
    openItem = new JMenuItem();
    openItem.setAccelerator(KeyStroke.getKeyStroke('O', mask));
    openItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        open();
      }
    });
    closeItem = new JMenuItem();
    closeItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int i = tabbedPane.getSelectedIndex();
        if (closeTab(i)) {
          refreshGUI();
        }
      }
    });
    closeAllItem = new JMenuItem();
    closeAllItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	for (int i=tabbedPane.getTabCount()-1; i>=0; i--) {
      		if (!closeTab(i)) break;
      	}
        refreshGUI();
      }
    });
    recentMenu = new JMenu();
    fileMenu.add(recentMenu);
    saveItem = new JMenuItem();
    saveItem.setAccelerator(KeyStroke.getKeyStroke('S', mask));
    saveItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	save();
      }
    });
    saveAsItem = new JMenuItem();
    saveAsItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	String path = saveAs();
    		library.addRecent(path, false);
    		refreshRecentMenu();
      }
    });

    exitItem = new JMenuItem();
    exitItem.setAccelerator(KeyStroke.getKeyStroke('Q', mask));
    exitItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	exit();
      }
    });
        
    collectionsMenu = new JMenu();
    menubar.add(collectionsMenu);
    
    manageMenu = new JMenu();
    menubar.add(manageMenu);
    collectionsItem = new JMenuItem();
    manageMenu.add(collectionsItem);
    collectionsItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	LibraryManager manager = browser.getManager();
      	manager.tabbedPane.setSelectedComponent(manager.collectionsPanel);
      	manager.setVisible(true);
      }
    });
    searchItem = new JMenuItem();
    manageMenu.add(searchItem);
    searchItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	LibraryManager manager = browser.getManager();
      	manager.tabbedPane.setSelectedComponent(manager.searchPanel);
      	manager.setVisible(true);
      }
    });
    cacheItem = new JMenuItem();
    manageMenu.add(cacheItem);
    cacheItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	LibraryManager manager = browser.getManager();
      	manager.tabbedPane.setSelectedComponent(manager.cachePanel);
      	manager.setVisible(true);
      }
    });

    helpMenu = new JMenu();
    menubar.add(helpMenu);
    helpItem = new JMenuItem();
    helpItem.setAccelerator(KeyStroke.getKeyStroke('H', mask));
    helpItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showHelp();
      }
    });
    helpMenu.add(helpItem);
    helpMenu.addSeparator();
    logItem = new JMenuItem();
    logItem.setAccelerator(KeyStroke.getKeyStroke('L', mask));
    logItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Point p0 = new Frame().getLocation();
        JFrame frame = OSPLog.getOSPLog();
        if((frame.getLocation().x==p0.x)&&(frame.getLocation().y==p0.y)) {
          frame.setLocationRelativeTo(LibraryBrowser.this);
        }
        frame.setVisible(true);
      }
    });
    helpMenu.add(logItem);
    helpMenu.addSeparator();
    aboutItem = new JMenuItem();
    aboutItem.setAccelerator(KeyStroke.getKeyStroke('A', mask));
    aboutItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showAboutDialog();
      }
    });
    helpMenu.add(aboutItem);

    // create html about-browser pane
    htmlAboutPane = new LibraryTreePanel.HTMLPane();
		htmlScroller = new JScrollPane(htmlAboutPane);
		htmlAboutPane.setText(getAboutLibraryBrowserText());
		htmlAboutPane.setCaretPosition(0);
		
    if (externalDialog!=null) {
    	externalDialog.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowOpened(WindowEvent e) {
      		new LibraryLoader().execute();
        }
      });
    }
    else {
    	frame.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowOpened(WindowEvent e) {
      		new LibraryLoader().execute();
        }
      });    	
    }
  	
  }
  
  /**
   * Refreshes the GUI, including locale-dependent resources strings.
   */
  protected void refreshGUI() {
  	if (tabbedPane.getTabCount()==0) {
  		remove(tabbedPane);
      add(htmlScroller, BorderLayout.CENTER);
      validate();
  	}
  	else {
  		remove(htmlScroller);
      add(tabbedPane, BorderLayout.CENTER);
  	}
  	// set text strings
  	setTitle(ToolsRes.getString("LibraryBrowser.Title")); //$NON-NLS-1$
    fileMenu.setText(ToolsRes.getString("Menu.File")); //$NON-NLS-1$
    newItem.setText(ToolsRes.getString("LibraryBrowser.MenuItem.New")); //$NON-NLS-1$
    openItem.setText(ToolsRes.getString("LibraryBrowser.MenuItem.Open")); //$NON-NLS-1$
    closeAllItem.setText(ToolsRes.getString("LibraryBrowser.MenuItem.CloseAll")); //$NON-NLS-1$
    saveItem.setText(ToolsRes.getString("LibraryBrowser.MenuItem.Save")); //$NON-NLS-1$
    saveAsItem.setText(ToolsRes.getString("LibraryBrowser.MenuItem.SaveAs")); //$NON-NLS-1$
    exitItem.setText(ToolsRes.getString("MenuItem.Exit")); //$NON-NLS-1$
    collectionsMenu.setText(ToolsRes.getString("LibraryBrowser.Menu.Collections")); //$NON-NLS-1$
    manageMenu.setText(ToolsRes.getString("LibraryBrowser.Menu.Manage")); //$NON-NLS-1$
    collectionsItem.setText(ToolsRes.getString("LibraryManager.Tab.MyLibrary")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
    searchItem.setText(ToolsRes.getString("LibraryManager.Tab.Search")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
    cacheItem.setText(ToolsRes.getString("LibraryManager.Tab.Cache")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
    helpMenu.setText(ToolsRes.getString("Menu.Help")); //$NON-NLS-1$
    helpItem.setText(ToolsRes.getString("LibraryBrowser.MenuItem.Help"));                    //$NON-NLS-1$
    logItem.setText(ToolsRes.getString("MenuItem.Log"));                               //$NON-NLS-1$
    aboutItem.setText(ToolsRes.getString("MenuItem.About"));                           //$NON-NLS-1$
    commandLabel.setText(ToolsRes.getString("LibraryTreePanel.Label.Target")); //$NON-NLS-1$
  	commandButton.setText(ToolsRes.getString("LibraryTreePanel.Button.Load")); //$NON-NLS-1$
  	commandField.setToolTipText(ToolsRes.getString("LibraryBrowser.Field.Command.Tooltip")); //$NON-NLS-1$
    searchLabel.setText(ToolsRes.getString("LibraryBrowser.Label.Search")); //$NON-NLS-1$
  	searchField.setToolTipText(ToolsRes.getString("LibraryBrowser.Field.Search.Tooltip")); //$NON-NLS-1$ 
    saveAsItem.setEnabled(true);
    refreshRecentMenu();
  	// rebuild file menu
    fileMenu.removeAll();
    fileMenu.add(newItem);
    fileMenu.add(openItem);
    fileMenu.add(recentMenu);
    fileMenu.addSeparator();
    fileMenu.add(closeItem);
    fileMenu.add(closeAllItem);
    fileMenu.addSeparator();
    fileMenu.add(saveItem);
    fileMenu.add(saveAsItem);
    fileMenu.addSeparator();
    fileMenu.add(exitItem);
    
  	LibraryTreePanel treePanel = getSelectedTreePanel();
    if (treePanel!=null) {
      editButton.setText(!treePanel.isEditing()?
      		ToolsRes.getString("LibraryBrowser.Button.OpenEditor"): //$NON-NLS-1$
      		ToolsRes.getString("LibraryBrowser.Button.CloseEditor")); //$NON-NLS-1$
      editButton.setEnabled(treePanel.isEditable());
      String tabname = " '"+getTabTitle(treePanel.pathToRoot)+"'"; //$NON-NLS-1$ //$NON-NLS-2$
      closeItem.setText(ToolsRes.getString("LibraryBrowser.MenuItem.CloseTab")+tabname); //$NON-NLS-1$
      closeItem.setEnabled(true);
      closeAllItem.setEnabled(true);
      saveItem.setEnabled(treePanel.isChanged());
      int i = tabbedPane.getSelectedIndex();
    	String title = tabbedPane.getTitleAt(i);
      if (treePanel.isChanged() && !title.endsWith("*")) { //$NON-NLS-1$
      	tabbedPane.setTitleAt(i, title+"*");  //$NON-NLS-1$
      }
      else if (!treePanel.isChanged() && title.endsWith("*")) { //$NON-NLS-1$
      	tabbedPane.setTitleAt(i, title.substring(0, title.length()-1)); 
      }
      treePanel.refreshGUI();
    }
    else {
    	refreshButton.setToolTipText(ToolsRes.getString("LibraryBrowser.Tooltip.Refresh")); //$NON-NLS-1$
    	editButton.setText(ToolsRes.getString("LibraryBrowser.Button.OpenEditor")); //$NON-NLS-1$
      saveItem.setEnabled(false);
      closeItem.setText(ToolsRes.getString("LibraryBrowser.MenuItem.CloseTab")); //$NON-NLS-1$
      closeItem.setEnabled(false);
      closeAllItem.setEnabled(false);
      editButton.setEnabled(false);
      refreshButton.setEnabled(false);
      commandField.setText(null);
      commandButton.setEnabled(false);
      saveAsItem.setEnabled(false);
    }
    repaint();
  }
  
  /**
   * Refreshes the open recent files menu.
   *
   * @param menu the menu to refresh
   */
  public void refreshRecentMenu() {
  	synchronized(library.recentTabs) {
  		recentMenu.setText(ToolsRes.getString("LibraryBrowser.Menu.OpenRecent")); //$NON-NLS-1$
  		recentMenu.setEnabled(!library.recentTabs.isEmpty());
	  	if (openRecentAction==null) {
		  	openRecentAction = new AbstractAction() {
		  		public void actionPerformed(ActionEvent e) {
		  			String path = e.getActionCommand();
		  			library.addRecent(path, false);
		  	    // select tab if path is already loaded
		  	    int i = getTabIndexFromPath(path);
		  	    if (i>-1) {
		  	    	tabbedPane.setSelectedIndex(i);
		  	    	return;
		  	    }
	    			TabLoader tabAdder = addTab(path, null);
	    			if (tabAdder!=null) {
	    	    	tabAdder.addPropertyChangeListener(new PropertyChangeListener() {  
	    	  			public  void propertyChange(PropertyChangeEvent e) {  
	    	  			  if ("progress".equals(e.getPropertyName())) {   //$NON-NLS-1$
	    	  			    Integer n = (Integer)e.getNewValue();  
	    				    	if (n>-1) {
	    				    		tabbedPane.setSelectedIndex(n);
	    					    	refreshGUI();
	    				    	}
	    	  			  }  
	    	  		  }  
	    	  		});
	    				tabAdder.execute();
	    			}
	    			else {
		        	library.recentTabs.remove(path);
		          refreshRecentMenu();
		        	JOptionPane.showMessageDialog(LibraryBrowser.this, 
		        			ToolsRes.getString("LibraryBrowser.Dialog.FileNotFound.Message") //$NON-NLS-1$
		        			+": "+path,  //$NON-NLS-1$
		        			ToolsRes.getString("LibraryBrowser.Dialog.FileNotFound.Title"),  //$NON-NLS-1$
		        			JOptionPane.WARNING_MESSAGE);
		    		}
		  		}
		  	};
	  	}
	  	recentMenu.removeAll();
	  	recentMenu.setEnabled(!library.recentTabs.isEmpty());
	  	for (String next: library.recentTabs) {
	  		String text = library.getNameMap().get(next);
	  		if (text==null) text = XML.getName(next);
	    	JMenuItem item = new JMenuItem(text);
	    	item.setActionCommand(next);
	    	item.setToolTipText(next);
	    	item.addActionListener(openRecentAction);
	    	recentMenu.add(item);
	    }
  	}
  	FontSizer.setFonts(recentMenu, FontSizer.getLevel());
  }

  /**
   * Opens a file using a file chooser.
   */
  protected void open() {
    JFileChooser fileChooser = OSPRuntime.getChooser();
    for (javax.swing.filechooser.FileFilter filter: fileChooser.getChoosableFileFilters()) {
    	fileChooser.removeChoosableFileFilter(filter);
    }
    fileChooser.addChoosableFileFilter(filesAndFoldersFilter);
    fileChooser.addChoosableFileFilter(Launcher.getXMLFilter());
    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    fileChooser.setFileFilter(filesAndFoldersFilter);
    File file = GUIUtils.showOpenDialog(this);
    // reset chooser to original state    
    fileChooser.removeChoosableFileFilter(filesAndFoldersFilter);
    fileChooser.removeChoosableFileFilter(Launcher.getXMLFilter());
    fileChooser.setAcceptAllFileFilterUsed(true);
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (file!=null) {
			open(file.getAbsolutePath());
		}
  }
  
  /**
   * Opens a file with a specified path.
   * @param path the path to the file
   */
  public void open(String path) {
  	if (path==null) return;
  	loadTab(path, null);
  }
  
  /**
   * Closes a tab.
   * @param index the tab number
   * @return true unless cancelled by user
   */
  protected boolean closeTab(int index) {
  	if (index<0 || index>=tabbedPane.getTabCount()) return true;
    LibraryTreePanel treePanel = getTreePanel(index);
    if (!treePanel.saveChanges(getTabTitle(index))) return false;
    tabbedPane.removeTabAt(index);
    return true;
  }
  
  /**
   * Saves the selected LibraryTreePanel collection.
   * @return the path to the saved file, or null if not saved
   */
  protected String save() {
  	LibraryTreePanel treePanel = getSelectedTreePanel();
		String path = treePanel.save();
		refreshGUI();
		return path;
  }

  /**
   * Saves the current root resource as a new xml file.
   * @return the path to the saved file, or null if not saved
   */
  protected String saveAs() {
  	String title = ToolsRes.getString("LibraryBrowser.FileChooser.Title.SaveAs"); //$NON-NLS-1$ 	
  	String path = getChooserSavePath(title);
		if (path!=null) {
			path = XML.forwardSlash(path);
	  	LibraryTreePanel treePanel = getSelectedTreePanel();
			treePanel.setRootResource(treePanel.rootResource, path, true, true);
			path = save();
			treePanel.setEditing(true);
			refreshTabTitle(path, treePanel.rootResource);
    	refreshGUI();
    	commandField.setForeground(LibraryTreePanel.defaultForeground);
    }
		return path;
  }
  
  /**
   * Uses a file chooser to define a path to which a library or resource file (xml) can be saved.
   * This adds the extension ".xml", if none, and checks for duplicates.
   * @param chooserTitle the title of the file chooser
   * @return the path, or null if canceled by the user
   */
  protected String getChooserSavePath(String chooserTitle) {
		File file = GUIUtils.showSaveDialog(this, chooserTitle);
		if (file ==null) return null;
		String path = file.getAbsolutePath();
    String extension = XML.getExtension(path);
    if (extension==null) {
    	path = XML.stripExtension(path)+".xml"; //$NON-NLS-1$
    	file = new File(path);
      if(file.exists()) {
        int response = JOptionPane.showConfirmDialog(this, 
        		ToolsRes.getString("Tool.Dialog.ReplaceFile.Message") //$NON-NLS-1$
        		+" "+file.getName()+"?", //$NON-NLS-1$ //$NON-NLS-2$
        		ToolsRes.getString("Tool.Dialog.ReplaceFile.Title"), //$NON-NLS-1$
            JOptionPane.YES_NO_CANCEL_OPTION);
        if(response!=JOptionPane.YES_OPTION) {
          return null;
        }
      }
    }
    return path;  	
  }
  
	/**
	 * Returns the set of all searchable cache resources.
	 * 
	 * @return a set of searchable resources
	 */
	protected Set<LibraryResource> getSearchCacheTargets() {
		// set up search targets
		Set<LibraryResource> searchTargets = new TreeSet<LibraryResource>();
		File cache = ResourceLoader.getSearchCache();
		FileFilter xmlFilter = new XMLFilter();
		List<File> xmlFiles = ResourceLoader.getFiles(cache, xmlFilter);
		for (File file: xmlFiles) {
			XMLControl control = new XMLControlElement(file.getAbsolutePath());
			if (!control.failedToRead() && LibraryResource.class.isAssignableFrom(control.getObjectClass())) {
				LibraryResource resource = (LibraryResource)control.loadObject(null);
				resource.collectionPath = control.getString("real_path"); //$NON-NLS-1$
				searchTargets.add(resource);
			}
		}
		return searchTargets;
	}
  
	/**
	 * Searches a set of LibraryResources for resources matching a search phrase.
	 * @param searchPhrase the phrase to match
	 * @param searchTargets a set of LibraryResources to search
	 * @return a LibraryTreePanel containing the search results, or null if no nodes found
	 */
	protected LibraryTreePanel searchFor(String searchPhrase, Set<LibraryResource> searchTargets) {
  	if (searchPhrase==null || searchPhrase.trim().equals("")) //$NON-NLS-1$
  			return null;

  	Map<LibraryResource, List<String[]>> found = new TreeMap<LibraryResource, List<String[]>>();
  	
		for (LibraryResource target: searchTargets) {
			if (target==null) continue;
			if (target instanceof LibraryCollection) {
				Map<LibraryResource, List<String[]>> map = searchCollectionFor(searchPhrase, (LibraryCollection)target);
				for (LibraryResource next: map.keySet()) {
					next.collectionPath = target.collectionPath;
					found.put(next, map.get(next));
				}
			}
			else {
				List<String[]> results = searchResourceFor(searchPhrase, target);
				if (results!=null) {
					found.put(target, results);
				}
			}
		}
		
		if (found.isEmpty()) return null;
  	
  	// create a LibraryCollection for the search results
  	String title = "'"+searchPhrase+"'"; //$NON-NLS-1$ //$NON-NLS-2$
  	LibraryTreePanel treePanel = createLibraryTreePanel();
  	String name = ToolsRes.getString("LibraryBrowser.SearchResults")+": "+title; //$NON-NLS-1$ //$NON-NLS-2$
  	LibraryCollection results = new LibraryCollection(name);
  	treePanel.setRootResource(results, "", false, false); //$NON-NLS-1$
  	LibraryTreeNode root = treePanel.rootNode;
  	
		for (LibraryResource next: found.keySet()) {
			LibraryResource clone = next.getClone();
			results.addResource(clone);
	    LibraryTreeNode newNode = new LibraryTreeNode(clone, treePanel);
	    newNode.setBasePath(next.getInheritedBasePath());
    	treePanel.insertChildAt(newNode, root, root.getChildCount());
		}
		  	
  	LibraryTreeNode last = (LibraryTreeNode)root.getLastChild();
  	TreePath path = new TreePath(last.getPath());
  	treePanel.tree.scrollPathToVisible(path);
  	treePanel.isChanged = false;
  	return treePanel;		
	}
	
	/**
	 * Searches a LibraryCollection for matches to a search phrase.
	 * @param searchPhrase the phrase
	 * @param collection the LibraryResource
	 * @return a List of String[] {where match was found, value in which match was found}, or null if no match found
	 */
	protected Map<LibraryResource, List<String[]>> searchCollectionFor(String searchPhrase, LibraryCollection collection) {
  	// deal with AND and OR requests
  	String[] toAND = searchPhrase.split(AND); 
  	String[] toOR = searchPhrase.split(OR); 
  	if (toAND.length>1 && toOR.length==1) {
  		Map<LibraryResource, List<String[]>> results = searchCollectionFor(toAND[0], collection);
			OSPLog.finer("AND '"+toAND[0]+"' (found: "+results.size()+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  		for (int i=1; i<toAND.length; i++) {
  			Map<LibraryResource, List<String[]>> next = searchCollectionFor(toAND[i], collection);
  			OSPLog.finer("AND '"+toAND[i]+"' (found: "+results.size()+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  			results = applyAND(results, next);
  		}
			OSPLog.finer("AND found: "+results.size()); //$NON-NLS-1$
  		return results;
  	}
  	if (toOR.length>1 && toAND.length==1) {
  		Map<LibraryResource, List<String[]>> results = searchCollectionFor(toOR[0], collection);
			OSPLog.finer("OR '"+toOR[0]+"' (found: "+results.size()+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  		for (int i=1; i<toOR.length; i++) {
  			Map<LibraryResource, List<String[]>> next = searchCollectionFor(toOR[i], collection);
  			OSPLog.finer("OR '"+toOR[i]+"' (found: "+results.size()+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  			results = applyOR(results, next);
  		}
			OSPLog.finer("OR found: "+results.size()); //$NON-NLS-1$
  		return results;
  	}
  	if (toOR.length>1 && toAND.length>1) {
  		// apply operations in left-to-right order but give precedence to parentheses
  		String[] split = getNextSplit(searchPhrase);
  		Map<LibraryResource, List<String[]>> results = searchCollectionFor(split[0], collection);
  		while (split.length>2) {
  			String operator = split[1];
	  		String remainder = split[2];
	  		split = getNextSplit(remainder);
  			Map<LibraryResource, List<String[]>> next = searchCollectionFor(split[0], collection);
	  		if (operator.equals(AND)) {
	  			results = applyAND(results, next);	  			
	  		}
	  		else if (operator.equals(OR)) {
	  			results = applyOR(results, next);	  			
	  		}
  		}
  		return results;  		
  	}
  	
		// do actual searching
  	Map<LibraryResource, List<String[]>> found = new TreeMap<LibraryResource, List<String[]>>();
		List<String[]> results = searchResourceFor(searchPhrase, collection);
		if (results!=null) {
			found.put(collection, results);
		}
		for (LibraryResource record: collection.getResources()) {
			if (record==null) continue;
			if (record instanceof LibraryCollection) {
				Map<LibraryResource, List<String[]>> map = searchCollectionFor(searchPhrase, (LibraryCollection)record);
				for (LibraryResource next: map.keySet()) {
					found.put(next, map.get(next));
				}
			}
			else {
				results = searchResourceFor(searchPhrase, record);
				if (results!=null) {
					found.put(record, results);
				}
			}
		}
		return found;
	}
    
	/**
	 * Searches a LibraryResource for matches to a search phrase.
	 * @param searchPhrase the phrase
	 * @param record the LibraryResource
	 * @return a List of String[] {category where match found, value where match found}, or null if no match found
	 */
	protected List<String[]> searchResourceFor(String searchPhrase, LibraryResource record) {
		String toMatch = searchPhrase.toLowerCase();
		ArrayList<String[]> foundData = new ArrayList<String[]>();
		// search node name
		String name = record.getName();
		if (name.toLowerCase().contains(toMatch)) {
			foundData.add(new String[] {"name", name});	    			 //$NON-NLS-1$
		}
		// search node type
		String type = record.getType();
		if (type.toLowerCase().contains(toMatch)) {
			foundData.add(new String[] {"type", type});	    			 //$NON-NLS-1$
		}
		// search metadata
		Set<Metadata> metadata = record.getMetadata();
		if (metadata!=null) {
			for (Metadata next: metadata) {
				String key = next.getData()[0];
				String value = next.getData()[1];
				if (value.toLowerCase().indexOf(toMatch)>-1) {
					foundData.add(new String[] {key, value});
	  		}
			}
		}
		return foundData.isEmpty()? null: foundData;
	}
  
  /**
   * Returns the phrase before the next AND or OR operator, the operator itself, and the remainder of the phrase.
   *
   * @param phrase a search phrase
   * @return String[]
   */
  protected String[] getNextSplit(String phrase) {
		String[] and = phrase.split(AND, 2);
		String[] or = phrase.split(OR, 2);
		String[] open = phrase.split(Pattern.quote(OPENING), 2);
		int which = and[0].length()<=or[0].length()?
				and[0].length()<=open[0].length()? 0: 2: 
				or[0].length()<=open[0].length()?	1: 2;
		if (which==2 && open.length>1) { // found opening parentheses

			// split remainder into parenthesis contents and remainder
			String[] split = getParenthesisSplit(open[1]);
			if (split.length==1) {
				return new String[] {split[0]};
			}
			int n = split[1].indexOf(AND);
			int m = split[1].indexOf(OR);
			if (n==-1 && m==-1) {
				return new String[] {open[1]};				
			}
			if (n>-1 && (m==-1 || n<m)) { // AND
				return new String[] {split[0], AND, split[1].substring(n+AND.length())};								
			}
			if (m>-1 && (n==-1 || m<n)) { // OR
				return new String[] {split[0], OR, split[1].substring(m+OR.length())};								
			}
		}
		switch(which) {
			case 0:
				if (and.length==1) return new String[] {and[0]};
				return new String[] {and[0], AND, and[1]};
			case 1:
				if (or.length==1) return new String[] {or[0]};
				return new String[] {or[0], OR, or[1]};
		}
		return new String[] {phrase};
  }
  
  /**
   * Returns the phrase enclosed in parentheses along with the remainder of a phrase.
   *
   * @param phrase a phrase that starts immediately AFTER an opening parenthesis
   * @return String[] {the enclosed phrase, the remainder}
   */
  protected String[] getParenthesisSplit(String phrase) {
  	
    int index = 1; // index of closing parenthesis
    int n = 1; // number of unpaired opening parentheses
    int opening = phrase.indexOf(OPENING, index);
    int closing = phrase.indexOf(CLOSING, index);
    while (n>0) {
    	if (opening>-1 && opening<closing) {
    		n++;
    		index = opening+1;
        opening = phrase.indexOf(OPENING, index);
    	}
    	else if (closing>-1) {
    		n--;
    		index = closing+1;
        closing = phrase.indexOf(CLOSING, index);
    	}
    	else return new String[] {phrase};
    }
    String token = phrase.substring(0, index-1);
    String remainder = phrase.substring(index);
    return remainder.trim().equals("")? new String[] {token}: new String[] {token, remainder}; //$NON-NLS-1$
  }
  
  /**
   * Returns the resources that are contained in the keysets of both of two input maps.
   * @param results1 
   * @param results2
   * @return map of resources found in both keysets
   */
  protected Map<LibraryResource, List<String[]>> applyAND(Map<LibraryResource, List<String[]>> results1,
  		Map<LibraryResource, List<String[]>> results2) {
  	Map<LibraryResource, List<String[]>> resultsAND = new TreeMap<LibraryResource, List<String[]>>();
  	Set<LibraryResource> keys1 = results1.keySet();
  	for (LibraryResource node: results2.keySet()) {
  		if (keys1.contains(node)) { // node is in both keysets
  			List<String[]> matchedTerms = new ArrayList<String[]>();
  			matchedTerms.addAll(results1.get(node));
  			matchedTerms.addAll(results2.get(node));
  			resultsAND.put(node, matchedTerms);
  		}
  	}
  	return resultsAND;
  }

  /**
   * Returns the resources that are contained in the keysets of either of two input maps.
   * @param results1 
   * @param results2
   * @return map of resources found in either keyset
   */
  protected Map<LibraryResource, List<String[]>> applyOR(Map<LibraryResource, List<String[]>> results1,
  		Map<LibraryResource, List<String[]>> results2) {
  	Map<LibraryResource, List<String[]>> resultsOR = new TreeMap<LibraryResource, List<String[]>>();
  	// add nodes in results1
  	for (LibraryResource node: results1.keySet()) {
  		List<String[]> matchedTerms = new ArrayList<String[]>();
  		matchedTerms.addAll(results1.get(node));
  		resultsOR.put(node, matchedTerms);
  	}
  	// add nodes in results2
  	for (LibraryResource node: results2.keySet()) {
  		if (resultsOR.keySet().contains(node)) {
  			resultsOR.get(node).addAll(results2.get(node));
  			continue;
  		}
  		List<String[]> matchedTerms = new ArrayList<String[]>();
  		matchedTerms.addAll(results2.get(node));
  		resultsOR.put(node, matchedTerms);
  	}
  	return resultsOR;
  }

  /**
   * Adds a collection to this browser's library after prompting the user to 
   * assign it a name.
   * 
   * @param path the path to the collection
   */
  protected void addToCollections(String path) {
  	if (library.containsPath(path, true)) {
  		return;
  	}
    String proposed = getTabTitle(path);
    if (proposed==null) {
    	LibraryResource collection = loadResource(path);
    	if (collection!=null) proposed = collection.getName();
    }
    if (proposed.equals("")) { //$NON-NLS-1$
    	proposed = XML.getName(path); // filename
    }
    
    library.addCollection(path, proposed);
    refreshCollectionsMenu();
  	refreshGUI();
  }

  /**
   * Creates a new LibraryCollection file.
   * @return the path to the new collection
   */
  protected String createNewCollection() {
  	String title = ToolsRes.getString("LibraryBrowser.FileChooser.Title.SaveCollectionAs"); //$NON-NLS-1$ 	
  	String path = getChooserSavePath(title);
		if (path!=null) {
			LibraryCollection collection = new LibraryCollection(null);
			// save new collection
			XMLControl control = new XMLControlElement(collection);
			control.write(path);
			path = XML.forwardSlash(path);
  		TabLoader tabAdder = addTab(path, null);
  		if (tabAdder==null) return null;
    	tabAdder.addPropertyChangeListener(new PropertyChangeListener() {  
  			public  void propertyChange(PropertyChangeEvent e) {  
  			  if ("progress".equals(e.getPropertyName())) {   //$NON-NLS-1$
  			    Integer n = (Integer)e.getNewValue();  
			    	if (n>-1) {
			    		tabbedPane.setSelectedIndex(n);
			    		LibraryTreePanel treePanel = getSelectedTreePanel();
			    		treePanel.setEditing(true);
				    	refreshGUI();
			    	}
  			  }  
  		  }  
  		});
    	tabAdder.execute();
		}
  	return path;
  }
  
  /**
   * Returns a name that is not a duplicate of an existing name.
   * 
   * @param proposed a proposed name
   * @param nameToIgnore a name that is ignored when comparing
   * @return a unique name that is the proposed name plus a possible suffix
   */
  protected String getUniqueName(String proposed, String nameToIgnore) {
  	proposed = proposed.trim();
  	if (isDuplicateName(proposed, nameToIgnore)) {
  		int i = 2;
  		String s = proposed+" ("+i+")"; //$NON-NLS-1$ //$NON-NLS-2$
  		while (isDuplicateName(s, nameToIgnore)) {
  			i++;
  			s = proposed+" ("+i+")"; //$NON-NLS-1$ //$NON-NLS-2$
  		}
  		return s;
  	}
  	return proposed;
  }
  
  /**
   * Determines if a name duplicates an existing name.
   * 
   * @param name the proposed name
   * @param nameToIgnore a name that is ignored when comparing
   * @return true if name is a duplicate
   */
  protected boolean isDuplicateName(String name, String nameToIgnore) {
  	// compare with existing names in library and tabbedPane
  	for (String next: library.getNames()) {
  		if (next.equals(nameToIgnore)) continue;
  		if (name.equals(next)) return true;
  	}
  	for (int i=0; i<tabbedPane.getTabCount(); i++) {
  		String title = tabbedPane.getTitleAt(i);
  		if (title.endsWith("*")) //$NON-NLS-1$
  			title = title.substring(0, title.length()-1);
  		if (title.equals(nameToIgnore)) continue;
  		if (name.equals(title)) return true; 		
  	}
  	return false;
  }
  
  /**
   * Creates a new empty LibraryTreePanel.
   * @return the library tree panel
   */
  protected LibraryTreePanel createLibraryTreePanel() {
  	LibraryTreePanel treePanel = new LibraryTreePanel(this);
    treePanel.addPropertyChangeListener(treePanelListener);
  	return treePanel;
  }
  
  /**
   * Shows the about dialog.
   */
  protected void showAboutDialog() {
    String aboutString = ToolsRes.getString("LibraryBrowser.Title")+" 2.0,  Dec 2012\n"   //$NON-NLS-1$ //$NON-NLS-2$
                         +"Open Source Physics Project\n" //$NON-NLS-1$
                         +"www.opensourcephysics.org";    //$NON-NLS-1$
    JOptionPane.showMessageDialog(this, aboutString, ToolsRes.getString("Dialog.About.Title") //$NON-NLS-1$
    		+" "+ToolsRes.getString("LibraryBrowser.Title"), //$NON-NLS-1$ //$NON-NLS-2$
      JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Shows the help frame and displays a help HTML page.
   */
  protected void showHelp() {
  	if (fireHelpEvent) {
  		firePropertyChange("help", null, null); //$NON-NLS-1$
  		return;
  	}
    String helpPath = XML.getResolvedPath(LIBRARY_HELP_NAME, LIBRARY_HELP_BASE);
    if(ResourceLoader.getResource(helpPath)==null) {
      String classBase = "/org/opensourcephysics/resources/tools/html/"; //$NON-NLS-1$
      helpPath = XML.getResolvedPath(LIBRARY_HELP_NAME, classBase);
    }
    if((helpFrame==null)||!helpPath.equals(helpFrame.getTitle())) {
      helpFrame = new TextFrame(helpPath);
      helpFrame.enableHyperlinks();
      helpFrame.setSize(760, 560);
      // center on the screen
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      int x = (dim.width-helpFrame.getBounds().width)/2;
      int y = (dim.height-helpFrame.getBounds().height)/2;
      helpFrame.setLocation(x, y);
    }
    helpFrame.setVisible(true);
  }
  
  /**
   * Returns html code that describes this browser. 
   * This is displayed when no LibraryTreePanel is loaded.
   * @return the html code
   */
  protected String getAboutLibraryBrowserText() {
  	
    String path = "org/opensourcephysics/resources/tools/images/compadre_banner.jpg";  //$NON-NLS-1$
    Resource res = ResourceLoader.getResource(path);
    String imageCode = "<p align=\"center\"><img src=\""+res.getURL()+"\"></p>"; //$NON-NLS-1$ //$NON-NLS-2$
  	String code = imageCode+
	  	"<h1>Open Source Physics Digital Library Browser</h1>"+ //$NON-NLS-1$
	  	"<p>The OSP Digital Library Browser enables you to browse, organize and access collections of digital library resources "+ //$NON-NLS-1$
	  	"such as EJS models and Tracker experiments. Collections and resources may be on a local drive or remote server.</p>"+ //$NON-NLS-1$
	  	"<ul>"+ //$NON-NLS-1$
	  	"  <li>Open a collection by choosing from the <strong>Collections</strong> menu or entering a URL directly in the toolbar "+ //$NON-NLS-1$
	  	"as with a web browser.</li>"+ //$NON-NLS-1$
	  	"	 <li>Collections are organized and displayed in a tree. Each tree node is a resource or sub-collection. "+ //$NON-NLS-1$
	  	"Click a node to learn about the resource or double-click to download and/or open it in EJS or Tracker.</li>"+ //$NON-NLS-1$
	  	"	 <li>Build and organize your own local collection by clicking the <strong>Open Editor</strong> button. "+ //$NON-NLS-1$
	  	"Collections are stored as xml documents that contain references to the actual resource files. "+ //$NON-NLS-1$
	  	"For more information, see the Help menu.</li>"+ //$NON-NLS-1$
	  	"	 <li>Share your collections by uploading all files to the web or a local network. For more information, see the Help menu.</li>"+ //$NON-NLS-1$
	  	"</ul>"+ //$NON-NLS-1$
	  	"<h2>ComPADRE Digital Library</h2>"+ //$NON-NLS-1$
	  	"<p>The ComPADRE Pathway, a part of the National Science Digital Library, is a growing network of educational resource "+ //$NON-NLS-1$
	  	"collections supporting teachers and students in Physics and Astronomy. As a user you may explore collections designed to meet "+ //$NON-NLS-1$
	  	"your specific needs and help build the network by recommending resources, commenting on resources, and starting or joining "+ //$NON-NLS-1$
	  	"discussions. For more information, see &lt;<b><a href=\"http://www.compadre.org/OSP/\">http://www.compadre.org/OSP/</a></b>&gt;. "+ //$NON-NLS-1$
	  	"To recommend an OSP resource for ComPADRE, visit the Suggest a Resource page at &lt;<b><a href="+ //$NON-NLS-1$
	  	"\"http://www.compadre.org/osp/items/suggest.cfm\">http://www.compadre.org/osp/items/suggest.cfm</a></b>&gt;.&nbsp; "+ //$NON-NLS-1$
	  	"Contact the OSP Collection editor, Wolfgang Christian, for additional information.</p>"; //$NON-NLS-1$
  	return code;
  }
  
//______________________________ inner classes _________________________________
  
  /**
   * A class to display and handle actions for a ComPADRE tab title.
   */
  class TabTitle extends JPanel {
  	JLabel titleLabel, iconLabel;
  	Icon normalIcon, boldIcon;
  	Action action;
  	
  	TabTitle(Icon lightIcon, Icon heavyIcon) {
  		super(new BorderLayout());
  		this.setOpaque(false);
  		titleLabel = new JLabel();
  		iconLabel = new JLabel();
  		iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
  		iconLabel.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
        	int i = getTabIndexFromTitle(titleLabel.getText());
        	if (i>-1 && tabbedPane.getSelectedIndex()!=i) tabbedPane.setSelectedIndex(i);
        	action.actionPerformed(null);
        }
        public void mouseEntered(MouseEvent e) {
      		iconLabel.setIcon(boldIcon);
        }
        public void mouseExited(MouseEvent e) {
      		iconLabel.setIcon(normalIcon);
        }
  		});
  		add(titleLabel, BorderLayout.WEST);
  		add(iconLabel, BorderLayout.EAST);
  		setIcons(lightIcon, heavyIcon);
  	}
  	
  	void setTitle(String title) {
  		titleLabel.setText(title);
  	}
  	
  	void setIcons(Icon lightIcon, Icon heavyIcon) {
  		normalIcon = lightIcon;
  		boldIcon = heavyIcon;
  		iconLabel.setIcon(normalIcon);
  	}
  	
  	void setAction(Action action) {
  		this.action = action;
  	}

  }

  /**
   * A SwingWorker class to load the Library at startup.
   */
  class LibraryLoader extends SwingWorker<Library, Object> {
  	
    @Override
    public Library doInBackground() {
 	  	Runnable runner = new Runnable() {
 	  		public void run() {
		  		webConnected = ResourceLoader.isURLAvailable("http://www.opensourcephysics.org"); //$NON-NLS-1$
		    	if (!webConnected) {
		    		JOptionPane.showMessageDialog(LibraryBrowser.this, 
		    				ToolsRes.getString("LibraryBrowser.Dialog.ServerUnavailable.Message"), //$NON-NLS-1$
		    				ToolsRes.getString("LibraryBrowser.Dialog.ServerUnavailable.Title"), //$NON-NLS-1$
		    				JOptionPane.WARNING_MESSAGE);  		
		    	}
 	  		}
 	  	};
    	if (!libraryPath.startsWith("http:")) { //$NON-NLS-1$
		    // load library
	    	library.load(libraryPath);
     	  // add previously open tabs that are available
	 	  	if (library.openTabPaths!=null) {
	 	  		ArrayList<String> unopenedTabs = new ArrayList<String>();
	 	  		String[] paths = library.openTabPaths;
		  		for (String path: paths) {
		  			// first check search cache
		    		File cachedFile = ResourceLoader.getSearchCacheFile(path);
		    		if (cachedFile.exists()) {
		    			TabLoader tabAdder = addTab(path, null);
		    			if (tabAdder!=null) tabAdder.execute();  	
		  			}
		  			else {
		  				unopenedTabs.add(path);
		  			}
		  		}
		  		if (!unopenedTabs.isEmpty()) {
			  		paths = unopenedTabs.toArray(new String[unopenedTabs.size()]);
			  		unopenedTabs.clear();
			  		for (String path: paths) {
			  			// check for local resource
			      	Resource res = ResourceLoader.getResource(path);
			    		if (res!=null && !path.startsWith("http:")) { //$NON-NLS-1$
			    			TabLoader tabAdder = addTab(path, null);
			    			if (tabAdder!=null) tabAdder.execute();  	
			  			}
			  			else {
			  				unopenedTabs.add(path);
			  			}
			  		}		  			
		  		}
		  		boolean done = unopenedTabs.isEmpty();
		  		// save web-based tabs for done() method
		  		library.openTabPaths = done? null: unopenedTabs.toArray(new String[unopenedTabs.size()]);
		  	}
	 	  	runner.run();
    	}
    	else {
    		runner.run(); // check web connection first
    		if (webConnected)
    			library.load(libraryPath);
    	}
			return library;
    }

    @Override
    protected void done() {
      try {
     	  Library library = get();
     	  // add previously open tabs not available for loading in doInBackground method
	 	  	if (library.openTabPaths!=null) {  	  		
		  		for (final String path: library.openTabPaths) {
		  			boolean available = isWebConnected() && path.startsWith("http:"); //$NON-NLS-1$
		  			if (available) {
		    			TabLoader tabAdder = addTab(path, null);
		    			if (tabAdder!=null) tabAdder.execute();  	
		  			}
		  		}
		  	}
      } catch (Exception ignore) {
      }
      refreshCollectionsMenu();
  		refreshRecentMenu();
    }
  }
  
  /**
   * A SwingWorker class to load tabs.
   */
  class TabLoader extends SwingWorker<LibraryTreePanel, Object> {
  	
  	String path;
  	int index;
  	List<String> treePath;
  	boolean saveToCache = true;
  	
  	TabLoader(String pathToAdd, int tabIndex, List<String> treePath) {
  		path = pathToAdd;
  		index = tabIndex;
  		this.treePath = treePath;
  	}
  	
    @Override
    public LibraryTreePanel doInBackground() {
    	setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    	String realPath = path;
  		File cachedFile = ResourceLoader.getSearchCacheFile(path);
  		if (cachedFile.exists() && path.startsWith("http:")) {  			 //$NON-NLS-1$
  			realPath = cachedFile.getAbsolutePath();
  			saveToCache = false;
  		}

    	LibraryResource resource = loadResource(realPath);
    	if (resource!=null) {
    		LibraryTreePanel treePanel = index<0? createLibraryTreePanel(): getTreePanel(index);
    		// tab is editable only if it is a local XML file
    		boolean editable = !path.startsWith("http:") && path.toLowerCase().endsWith(".xml"); //$NON-NLS-1$ //$NON-NLS-2$
    		treePanel.setRootResource(resource, path, editable, isRecentPathXML);
    		return treePanel;
    	}
    	return null;
    }

    @Override
    protected void done() {
      try {
      	LibraryTreePanel treePanel = get();
	    	if (treePanel!=null) {
	    		treePanel.setFontLevel(FontSizer.getLevel());
	    		
	    		if (index<0) {
	    			tabbedPane.addTab("", treePanel); //$NON-NLS-1$
		    		index = tabbedPane.getTabCount()-1;
	    		}
	    		refreshTabTitle(path, treePanel.rootResource);
	    		tabbedPane.setToolTipTextAt(index, path);
	    		
	    		treePanel.setSelectionPath(treePath);

			    // start background SwingWorker to load metadata and set up search database
	    		if (treePanel.metadataLoader!=null) {
	    			treePanel.metadataLoader.cancel();
	    		}
	    		treePanel.metadataLoader = treePanel.new MetadataLoader(saveToCache, treePath);
	    		treePanel.metadataLoader.execute();
	    		setProgress(index);
	    	}
	    	else {
		    	String s = ToolsRes.getString("LibraryBrowser.Dialog.CollectionNotFound.Message"); //$NON-NLS-1$
		    	JOptionPane.showMessageDialog(LibraryBrowser.this, 
		    			s+":\n"+path, //$NON-NLS-1$
		  				ToolsRes.getString("LibraryBrowser.Dialog.CollectionNotFound.Title"), //$NON-NLS-1$
		  				JOptionPane.WARNING_MESSAGE);  
		  		library.removeRecent(path);
		  		refreshRecentMenu();		    	
		    	setProgress(-1);
	    	}
      } catch (Exception ignore) {
      }
    	setCursor(Cursor.getDefaultCursor());
    }
  }
  
//______________________________ static methods and classes ____________________________

  /**
   * Entry point when run as an independent application.
   * 
   * @param args String[] ignored
   */
  public static void main(String[] args) {
  	final LibraryBrowser browser = LibraryBrowser.getBrowser();
  	browser.addOSPLibrary(TRACKER_LIBRARY);
  	browser.addOSPLibrary(SHARED_LIBRARY);
  	browser.addComPADRECollection(LibraryComPADRE.EJS_SERVER_TREE+LibraryComPADRE.PRIMARY_ONLY);
  	browser.addComPADRECollection(LibraryComPADRE.TRACKER_SERVER_TREE+LibraryComPADRE.PRIMARY_ONLY);
  	browser.refreshCollectionsMenu();
  	
  	// code below opens Tracker when LibraryBrowser is launched as an independent application
  	
//  	browser.addPropertyChangeListener("target", new PropertyChangeListener() { //$NON-NLS-1$
//  		public void propertyChange(PropertyChangeEvent e) {
//  			LibraryResource record = (LibraryResource)e.getNewValue();
//				String target = XML.getResolvedPath(record.getTarget(), record.getBasePath());
//					  				
//  			ArrayList<String> extensions = new ArrayList<String>();
//  			for (String ext: VideoIO.getVideoExtensions()) {
//  				extensions.add(ext);
//  			}
//  			extensions.add("trk"); //$NON-NLS-1$
//  			extensions.add("zip"); //$NON-NLS-1$
//  			extensions.add("trz"); //$NON-NLS-1$
//  			for (String ext: extensions) {
//  				if (target.endsWith("."+ext)) { //$NON-NLS-1$
//  			    Tracker tracker = Tracker.getTracker();
//  			    final TFrame frame = tracker.getFrame();
//  			    frame.setVisible(true);
//            try {
//        			target = ResourceLoader.getURIPath(target);
//							URL url = new URL(target);
//							TrackerIO.open(url, new TrackerPanel(), frame);
//						} catch (Exception ex) {ex.printStackTrace();}
//     			}
//  			}
//  		}
//  	});
 	
  	browser.exitOnClose = true;
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (dim.width - browser.getBounds().width) / 2;
    int y = (dim.height - browser.getBounds().height) / 2;
    browser.setLocation(x, y);
    browser.setVisible(true);
  }
  
  /**
   * Returns true if connected to the web.
   * 
   * @return true if web connected
   */
	protected static boolean isWebConnected() {
		return webConnected;
	}
	
  /**
   * Returns the redirect URL path, if any, of an HTML page.
   * @param code the HTML code 
   * @return the redirect path
   */
	protected static String getRedirectFromHTMLCode(String code) {
		if (code==null) return null;
		String[] parts = code.split("<!--"); //$NON-NLS-1$
		if (parts.length>1) {
			for (int i=1; i<parts.length; i++) {
				if (parts[i].trim().startsWith("redirect:")) { //$NON-NLS-1$
					String[] subparts = parts[i].split("-->"); //$NON-NLS-1$
					return subparts.length>1? subparts[0].substring(9).trim(): null;
				}
			}
		}
		return null;
	}
	
  /**
   * A FileFilter that accepts trk, pdf, html and zip (if trk found inside) files
   * with names that do NOT start with underscore.
   */
  static class TrackerDLFilter implements FileFilter {
  	
  	static {
  		// if Xuggle is available, register with VideoIO
  		String className = "org.opensourcephysics.media.xuggle.XuggleIO"; //$NON-NLS-1$
      try {
				Class<?> xuggleIOClass = Class.forName(className);
	      Method method = xuggleIOClass.getMethod("registerWithVideoIO", (Class[]) null);  //$NON-NLS-1$
	      method.invoke(null, (Object[]) null);
			} catch (Exception ex) {
			}
  	}
  	
    public boolean accept(File file) {
    	if (file==null || file.isDirectory()) return false;
    	String name = file.getName();
    	if (name.startsWith("_")) return false; //$NON-NLS-1$
    	String ext = XML.getExtension(name);
    	if (ext==null) return false;
    	ext = ext.toLowerCase();
    	if (ext.equals("trk")) return true; //$NON-NLS-1$
    	if (ext.indexOf("htm")>-1) return true; //$NON-NLS-1$
    	if (ext.equals("pdf")) return true; //$NON-NLS-1$
    	if (ext.equals("trz")) return true; //$NON-NLS-1$
    	for (String next: VideoIO.getVideoExtensions()) {
    		if (ext.equals(next.toLowerCase())) return true;
    	}
    	if (ext.equals("zip")) { //$NON-NLS-1$
				Set<String> files = ResourceLoader.getZipContents(file.getAbsolutePath());
				for (String next: files) {
					if (next.toLowerCase().endsWith(".trk")) return true; //$NON-NLS-1$
				}
    	}
      return false;
    }
  }

  /**
   * A FileFilter that accepts only directories with names that do NOT start with underscore.
   */
  static class DirectoryFilter implements FileFilter {  	
    public boolean accept(File file) {
    	if (file.getName().startsWith("_")) return false; //$NON-NLS-1$
    	return file.isDirectory();
    }
  }

  /**
   * A FileFilter that accepts html files with names that do NOT start with underscore.
   */
  static class HTMLFilter implements FileFilter {  	
    public boolean accept(File file) {
    	if (file==null || file.isDirectory()) return false;
    	String name = file.getName();
    	if (name.startsWith("_")) return false; //$NON-NLS-1$
    	String ext = XML.getExtension(name);
    	if (ext==null) return false;
    	if (ext.toLowerCase().startsWith("htm")) return true; //$NON-NLS-1$
    	return false;
    }
  }
  
  /**
   * A FileFilter that accepts xml files and directories.
   */
  static class XMLFilter implements FileFilter {  	
    public boolean accept(File file) {
    	if (file==null) return false;
    	if (file.isDirectory()) return true;
    	String ext = XML.getExtension(file.getName());
    	if (ext==null) return false;
    	if (ext.toLowerCase().equals("xml")) return true; //$NON-NLS-1$
    	return false;
    }
  }
  
  /**
   * A filechooser FileFilter that accepts files and folders.
   */
  static class FilesAndFoldersFilter extends javax.swing.filechooser.FileFilter {
    // accept all directories and files.
    public boolean accept(File f) {
      return f!=null;
    }
    // the description of this filter
    public String getDescription() {
      return ToolsRes.getString("LibraryBrowser.FilesAndFoldersFilter.Description"); //$NON-NLS-1$
    }

  }
  
}
