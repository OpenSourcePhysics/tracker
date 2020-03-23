/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.OSPRuntime;

/**
 * A Library for a LibraryBrowser. Maintains lists of collection paths and imported sub-libraries.
 *
 * @author Douglas Brown
 */
public class Library {
		
	protected String name; // name of the library
	protected ArrayList<String> pathList = new ArrayList<String>();
	protected HashMap<String, String> pathToNameMap = new HashMap<String, String>();
	protected ArrayList<String> comPADREPathList = new ArrayList<String>();
	protected HashMap<String, String> comPADREPathToNameMap = new HashMap<String, String>();
	protected ArrayList<String> ospPathList = new ArrayList<String>();
	protected HashMap<String, Library> ospPathToLibraryMap = new HashMap<String, Library>();
	protected ArrayList<String> importedPathList = new ArrayList<String>();
	protected HashMap<String, Library> importedPathToLibraryMap = new HashMap<String, Library>();
	protected ArrayList<String> subPathList = new ArrayList<String>();
	protected HashMap<String, Library> subPathToLibraryMap = new HashMap<String, Library>();
	protected HashMap<String, String> allPathsToNameMap = new HashMap<String, String>();
	protected Set<String> noSearchSet = new TreeSet<String>();
	protected String[] openTabPaths;
	protected ArrayList<String> recentTabs = new ArrayList<String>();
	protected int maxRecentTabCount = 6;
	protected String chooserDir;
	protected LibraryBrowser browser;
	
	/**
	 * Adds an OSP-sponsored library. OSP libraries are not under user control.
	 * 
	 * @param path the library path
	 * @return true if successfully added
	 */
	public boolean addOSPLibrary(String path) {		
  	if (ospPathList.contains(path))
  		return false;
  	synchronized (ospPathList) {
			XMLControl control = new XMLControlElement(path);
			if (control.failedToRead() || control.getObjectClass() != Library.class) {
				return false;
			}
			Library library = new Library();
			control.loadObject(library);
			library.browser = this.browser;
			ospPathList.add(path);
			ospPathToLibraryMap.put(path, library);
		}
  	return true;
	}

	/**
	 * Imports a library. Imported libraries are managed by the user.
	 * 
	 * @param path the library path
	 * @return true if successfully imported
	 */
	public boolean importLibrary(String path) {		
  	if (importedPathList.contains(path))
  		return false;
  	XMLControl control = new XMLControlElement(path);
  	if (control.failedToRead() || control.getObjectClass()!=Library.class)
  		return false;
  	Library library = new Library();
  	library.browser = this.browser;
  	control.loadObject(library);
  	return importLibrary(path, library);
	}
	
	/**
	 * Adds a comPADRE collection. ComPADRE collections are not under user control.
	 * 
	 * @param path the comPADRE query
	 * @param name the name of the collection
	 * @return true if successfully added
	 */
	public boolean addComPADRECollection(String path, String name) {
		path = path.trim();
		// don't add duplicate paths
  	if (comPADREPathList.contains(path))
  		return false;
		comPADREPathList.add(path);
		comPADREPathToNameMap.put(path, name.trim());
		allPathsToNameMap.put(path, name.trim());
		return true;
	}
	
	/**
	 * Adds a sublibrary. Sublibraries are shown as submenus in a Library's Collections menu.
	 * Sublibraries are not under user control.
	 * 
	 * @param path the path to the sublibrary
	 * @return true if successfully added
	 */
	public boolean addSubLibrary(String path) {		
  	if (subPathList.contains(path))
  		return false;
  	synchronized (subPathList) {
			XMLControl control = new XMLControlElement(path);
			if (control.failedToRead() || control.getObjectClass() != Library.class)
				return false;
			Library library = new Library();
			library.browser = this.browser;
			control.loadObject(library);
			subPathList.add(path);
			subPathToLibraryMap.put(path, library);
		}
		return true;
	}

	/**
	 * Returns a string representation of this library.
	 * 
	 * @return the name of the library
	 */
	@Override
  public String toString() {
  	return getName();
  }
	
  /**
   * Sets the cache path.
   * 
   * @param cachePath the cache path
   */
  protected void setCache(String cachePath) {
  	File cacheDir = cachePath==null? ResourceLoader.getDefaultOSPCache(): new File(cachePath);
  	ResourceLoader.setOSPCache(cacheDir);
  }

//_____________________ protected and private methods _________________________
	
	/**
	 * Sets the name of this library.
	 * 
	 * @param name the name
	 */
	protected void setName(String name) {
		if (name==null) {
      name = OSPRuntime.getUserHome().replace('\\', '/');
      if(name.endsWith("/")) {                                         //$NON-NLS-1$ 
        name = name.substring(0, name.length()-1); 
      }
      name = XML.getName(name)+" "+ToolsRes.getString("Library.Name");  //$NON-NLS-1$//$NON-NLS-2$
		}
		this.name = name;
	}

	/**
	 * Gets the name of this library.
	 * 
	 * @return the name
	 */
	protected String getName() {
		return name;
	}

	/**
	 * Saves this library in an xml file.
	 * 
	 * @param path the path to the saved file
	 */
	protected void save(String path) {
		if (path==null) return;
  	XMLControl control = new XMLControlElement(this);
  	control.write(path);
	}
	
	/**
	 * Loads this library from an xml file.
	 * 
	 * @param path the path to the file
	 */
	protected void load(String path) {
		if (path==null) return;
  	XMLControl control = new XMLControlElement(path);
  	control.loadObject(this);
	}
	
	/**
	 * Gets the names of all collections maintained by this library.
	 * 
	 * @return a collection of names
	 */
	protected Collection<String> getNames() {
		return pathToNameMap.values();
	}
	
	/**
	 * Returns true if this library has no collections.
	 * 
	 * @return true if empty
	 */
	protected boolean isEmpty() {
		return pathList.isEmpty();
	}
	
	/**
	 * Returns true if this library contains a collection path.
	 * 
	 * @param path the collection path
	 * @param allLists true to search in all collection lists
	 * @return true if this contains the path
	 */
	protected boolean containsPath(String path, boolean allLists) {
		path = path.trim();
		int n = path.indexOf(LibraryComPADRE.PRIMARY_ONLY);
		if (n>-1)
			path = path.substring(0, n);
		boolean containsPath = pathList.contains(path);
		if (allLists) {
			containsPath = containsPath 
					|| comPADREPathList.contains(path) 
					|| ospPathList.contains(path);
		}
		return containsPath;
	}
	
	/**
	 * Adds a collection to this library.
	 * 
	 * @param path the path to the collection
	 * @param name the menu item name for the collection
	 */
	protected void addCollection(String path, String name) {
		path = path.trim();
		// don't add duplicate paths
  	if (pathList.contains(path))
  		return;
		pathList.add(path);
		pathToNameMap.put(path, name.trim());
		allPathsToNameMap.put(path, name.trim());
	}
	
	/**
	 * Renames a collection.
	 * 
	 * @param path the path to the collection
	 * @param newName the new name
	 */
	protected void renameCollection(String path, String newName) {
		path = path.trim();
		// change only paths that have already been added
  	if (!pathList.contains(path))
  		return;
		pathToNameMap.put(path, newName.trim());
		allPathsToNameMap.put(path, newName.trim());
  }
	
	/**
	 * Returns all collection paths in this Library and sublibraries.
	 * 
	 * @return array of paths
	 */
	protected TreeSet<String> getAllPaths() {
		TreeSet<String> paths = new TreeSet<String>();
		paths.addAll(pathList);
		paths.addAll(comPADREPathList);
		paths.addAll(ospPathList);

  	if (!subPathList.isEmpty()) {
	  	for (String path: subPathList) {
	  		Library library = subPathToLibraryMap.get(path);
	  		paths.addAll(library.getAllPaths());
	  	}
  	}
  	for (String path: ospPathList) {
  		Library library = ospPathToLibraryMap.get(path);
  		paths.addAll(library.getAllPaths());
  	}
		return paths;
	}
	
	/**
	 * Returns a Map of path-to-tab name.
	 * 
	 * @return path-to-name map
	 */
	protected HashMap<String, String> getNameMap() {
		return allPathsToNameMap;
	}
		
	/**
	 * Gets a clone of this library that is suitable for exporting. The exported
	 * library has no OSP libraries, ComPADRE collections or imported libraries.
	 * 
	 * @return a Library for export
	 */
	protected Library getCloneForExport() {
  	Library lib = new Library();
  	lib.pathList = pathList;
  	lib.pathToNameMap = pathToNameMap;
  	lib.name = name;
  	return lib;
	}

	/**
	 * Imports a Library if not already imported.
	 * 
	 * @param path the path to the library
	 * @param library the library
	 * @return true if imported
	 */
	protected boolean importLibrary(String path, Library library) {		
  	if (importedPathList.contains(path))
  		return false;
  	importedPathList.add(path);
  	importedPathToLibraryMap.put(path, library);
  	return true;
	}
	
  /**
   * Adds a path to the list of recently opened tabs.
   * 
   * @param filename the absolute path to a recently opened or saved file.
   * @param atEnd true to add at end of the list
   */
  protected void addRecent(String filename, boolean atEnd) {
  	if (filename==null) return;
  	synchronized(recentTabs) {
	  	while (recentTabs.contains(filename))
	  		recentTabs.remove(filename);
	  	if (atEnd)
	  		recentTabs.add(filename);
	  	else
	  		recentTabs.add(0, filename);
	    while (recentTabs.size()>maxRecentTabCount) {
	    	recentTabs.remove(recentTabs.size()-1);
	    }
  	}
  }
	
  /**
   * Removes a path from the list of recently opened tabs.
   * 
   * @param filename the path to remove.
   */
  protected void removeRecent(String filename) {
  	if (filename==null) return;
  	synchronized(recentTabs) {
	  	while (recentTabs.contains(filename))
	  		recentTabs.remove(filename);
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
    	Library library = (Library)obj;
    	control.setValue("name", library.getName()); //$NON-NLS-1$
    	if (!library.pathList.isEmpty()) {
	    	String[] paths = library.pathList.toArray(new String[0]);
	    	control.setValue("collection_paths", paths); //$NON-NLS-1$
	    	String[] names = new String[paths.length];
	    	for (int i=0; i< paths.length; i++) {
	    		names[i] = library.pathToNameMap.get(paths[i]);
	    	}
	    	control.setValue("collection_names", names); //$NON-NLS-1$
    	}
    	if (!library.subPathList.isEmpty()) {
	    	String[] paths = library.subPathList.toArray(new String[0]);
	    	control.setValue("sublibrary_paths", paths); //$NON-NLS-1$
    	}
    	if (!library.importedPathList.isEmpty()) {
	    	String[] paths = library.importedPathList.toArray(new String[0]);
	    	control.setValue("imported_library_paths", paths); //$NON-NLS-1$
    	}
    	control.setValue("open_tabs", library.openTabPaths); //$NON-NLS-1$
    	control.setValue("chooser_directory", library.chooserDir); //$NON-NLS-1$
    	if (!library.recentTabs.isEmpty()) {
    		String[] paths = library.recentTabs.toArray(new String[0]);
	    	control.setValue("recently_opened", paths); //$NON-NLS-1$
	    	String[] names = new String[paths.length];
	    	for (int i=0; i<names.length; i++) {
	    		names[i] = library.getNameMap().get(paths[i]);
	    		if (names[i]==null) names[i] = XML.getName(paths[i]);
	    	}
	    	control.setValue("recently_opened_names", names); //$NON-NLS-1$	    	
    	}
    	if (!library.noSearchSet.isEmpty()) {
	    	String[] paths = library.noSearchSet.toArray(new String[0]);
	    	control.setValue("no_search_paths", paths); //$NON-NLS-1$
    	}
    	if (ResourceLoader.getOSPCache()!=null) {
    		File cache = ResourceLoader.getOSPCache();
    		control.setValue("cache", cache.getPath()); //$NON-NLS-1$
    	}
    }
    
    /**
     * Creates a new object.
     *
     * @param control the XMLControl with the object data
     * @return the newly created object
     */
    public Object createObject(XMLControl control) {
      return new Library();
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
    	final Library library = (Library)obj;
    	library.setName(control.getString("name")); //$NON-NLS-1$
    	String[] paths = (String[])control.getObject("collection_paths"); //$NON-NLS-1$
    	if (paths!=null) {
      	String[] names = (String[])control.getObject("collection_names"); //$NON-NLS-1$
      	library.pathList.clear();
      	library.pathToNameMap.clear();
    		for (int i=0; i<paths.length; i++) {
    			if (paths[i]==null || names[i]==null) continue;
    			library.pathList.add(paths[i]);
    			library.pathToNameMap.put(paths[i], names[i]);
    			library.allPathsToNameMap.put(paths[i], names[i]);
    		}
    	}
    	paths = (String[])control.getObject("sublibrary_paths"); //$NON-NLS-1$
    	if (paths!=null) {
				for (String path: paths) {
					library.addSubLibrary(path);
				}
    	}
    	paths = (String[])control.getObject("imported_library_paths"); //$NON-NLS-1$
    	if (paths!=null) {
				for (String path: paths) {
					library.importLibrary(path);
				}
    	}
    	paths = (String[])control.getObject("recently_opened"); //$NON-NLS-1$
    	String[] names = (String[])control.getObject("recently_opened_names"); //$NON-NLS-1$
    	if (paths!=null) {
				for (String path: paths) {
					library.addRecent(path, true); // add at end
				}
	    	if (names!=null) {
					for (int i=0; i<names.length; i++) {
						library.getNameMap().put(paths[i], names[i]);
					}
	    	}
    	}
    	paths = (String[])control.getObject("no_search_paths"); //$NON-NLS-1$
    	if (paths!=null) {
				for (String path: paths) {
					library.noSearchSet.add(path);
				}
    	}
    	library.openTabPaths = (String[])control.getObject("open_tabs"); //$NON-NLS-1$
    	library.chooserDir = control.getString("chooser_directory"); //$NON-NLS-1$
    	// set cache only if it has not yet been set
    	if (ResourceLoader.getOSPCache()==null) {
    		library.setCache(control.getString("cache")); //$NON-NLS-1$
    	}
    	return obj;
    }
  }  	
}

