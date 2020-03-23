/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.media.core.VideoFileFilter;
import org.opensourcephysics.media.core.VideoIO;
import org.opensourcephysics.media.gif.GifDecoder;
import org.opensourcephysics.tools.LibraryResource.Metadata;

/**
 * A DefaultMutableTreeNode for a LibraryTreePanel tree, with a LibraryResource user object.
 * Provides convenience methods for getting, setting and displaying LibraryResource data.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class LibraryTreeNode extends DefaultMutableTreeNode implements Comparable<LibraryTreeNode> {

  protected static HashMap<String, URL> htmlURLs = new HashMap<String, URL>();
  protected static HashMap<String, URL> targetURLs = new HashMap<String, URL>();
	protected static Dimension defaultThumbnailDimension = new Dimension(320, 240);

  protected LibraryResource record;
  protected boolean editable = true;
  protected LibraryTreePanel treePanel;
  protected ArrayList<LibraryResource> resources = new ArrayList<LibraryResource>();
  protected String tooltip;
  protected Metadata selectedMetadata;
  protected String metadataSource;

  /**
   * Constructs a node with a LibraryResource.
   *
   * @param resource the resource
   * @param treePanel the LibraryTreePanel that will use the node
   */
  protected LibraryTreeNode(LibraryResource resource, LibraryTreePanel treePanel) {
  	this.record = resource;
  	this.treePanel = treePanel;
  	if (treePanel.tree!=null)
  		createChildNodes();
    setUserObject(this);
  }
  
  /**
   * Compares this to the specified object. Return true if both nodes have same name, target and HTML.
   *
   * @param object the object
   * @return <code>true</code> if this equals the specified object
   */
  public boolean equals(Object obj) {
  	if (obj==this) return true;
  	if((obj==null) || (obj.getClass()!=this.getClass()))
  		return false;
  	LibraryTreeNode treeNode = (LibraryTreeNode)obj;
  	String target1 = this.getAbsoluteTarget();
  	String target2 = treeNode.getAbsoluteTarget();
  	String html1 = this.getHTMLPath();
  	String html2 = treeNode.getHTMLPath();
  	return ( 
  			((target1==null&&target2==null) || (target1!=null && target2!=null && target1.equals(target2)))
  			&& ((html1==null&&html2==null) || (html1!=null && html2!=null && html1.equals(html2)))
  			&& treeNode.getName().equals(this.getName()) );
  }

  
  /**
   * Compares this to the specified node.
   *
   * @param node the node to compare 
   * @return 0 if equal, otherwise alphabetical name order
   */
	public int compareTo(LibraryTreeNode node) {
		final int BEFORE = -1;
    final int EQUAL = 0;
    final int AFTER = 1;

    if (this==node || this.equals(node)) return EQUAL;
		
		// compare names
    int result = this.getName().compareTo(node.getName());
		if (result!=EQUAL) return result;
		
		// compare targets
  	String tar1 = this.getAbsoluteTarget();
  	String tar2 = node.getAbsoluteTarget();
  	if (tar1!=null || tar2!=null) {
  		if (tar1==null) return AFTER;
  		if (tar2==null) return BEFORE;
  		result = tar1.compareTo(tar2);
  	}
		if (result!=EQUAL) return result;
		
		// compare HTML paths
  	String html1 = this.getHTMLPath();
  	String html2 = node.getHTMLPath();
  	if (html1!=null || html2!=null) {
  		if (html1==null) return AFTER;
  		if (html2==null) return BEFORE;
  		return html1.compareTo(html2);
  	}
		return EQUAL;
	}
	
  /**
   * Creates the child nodes of this node if this is a collection node.
   *
   * @return true if children were added
   */
  protected boolean createChildNodes() {
  	ArrayList<String> children = new ArrayList<String>();
  	for (int i=0; i< getChildCount(); i++) {
  		children.add(this.getChildAt(i).toString());
  	}
  	boolean changed = false;
    if (record instanceof LibraryCollection) {
    	LibraryCollection collection = (LibraryCollection)record;
    	for (LibraryResource next: collection.getResources()) {
    		if (next!=null && !children.contains(next.getName())) {
    			LibraryTreeNode newNode = new LibraryTreeNode(next, treePanel);
        	if (treePanel.insertChildAt(newNode, this, getChildCount())) {
        		changed = true;
        	}
    		}
    	}
    }
    if (changed) treePanel.setChanged();
    return changed;
  }
  
  /**
   * Returns the name of this node's resource.
   * 
   * @return the name
   */
  protected String getName() {
  	return record.getName();
  }
  
  /**
   * Returns the base path of this node's resource.
   * 
   * @return the base path
   */
  protected String getBasePath() {
  	String base = record.getBasePath();
  	if (!base.equals("")) //$NON-NLS-1$
  		return base;
  	LibraryTreeNode parent = (LibraryTreeNode)getParent();
  	if (parent!=null)
  		return parent.getBasePath();
  	if (treePanel!=null) {
  		return XML.getDirectoryPath(treePanel.pathToRoot);
  	}
  	return ""; //$NON-NLS-1$
  }

  /**
   * Returns the html path of this node's resource.
   * 
   * @return the html path
   */
  protected String getHTMLPath() {
  	String path = record.getHTMLPath();
		if (path!=null && !path.trim().equals("")) { //$NON-NLS-1$
	  	path = XML.getResolvedPath(path, getBasePath());
			return path;
		}
  	return null;
  }
  
  /**
   * Returns the html URL for this node, or null if html path is empty or invalid.
   * If a cached file exists, this returns its URL instead of the original.
   * 
   * @return the URL
   */
  protected URL getHTMLURL() {
  	String path = getHTMLPath();
  	if (path==null) return null;
		URL url = null;
		
		File cachedFile = ResourceLoader.getOSPCacheFile(path);
  	boolean foundInCache = cachedFile.exists();

	  // see if URL is in the map
  	if (htmlURLs.keySet().contains(path)) { 
  		url = htmlURLs.get(path);
  	}
  	else {
  		String workingPath = path;
			if (foundInCache) {
				workingPath = cachedFile.toURI().toString();
			}
	  	// first try to get URL with raw path
  		Resource res = ResourceLoader.getResourceZipURLsOK(workingPath);
  		if (res!=null) {
  			url = res.getURL();
  		}  		
  		else {
	    	// try with URI form of path
  			workingPath = ResourceLoader.getURIPath(workingPath);
	  		res = ResourceLoader.getResourceZipURLsOK(workingPath);
	  		if (res!=null) {
	  			url = res.getURL();
	  		}
  		}
  	}
  	htmlURLs.put(path, url);
  	return url;
  }
  
  /**
   * Returns an HTML string that describes this node's resource.
   * This is displayed if no html URL is available.
   * 
   * @return the html string
   */
  protected String getHTMLString() {
  	if (!record.getDescription().equals("")) { //$NON-NLS-1$
  		return record.getDescription();
  	}
    
    boolean isImage = record.getType().equals(LibraryResource.IMAGE_TYPE) && record.getTarget()!=null;    
    boolean isVideo = record.getType().equals(LibraryResource.VIDEO_TYPE) && record.getTarget()!=null;    
    boolean isZip = record.getTarget()!=null && 
    		(record.getTarget().toLowerCase().endsWith(".zip") || record.getTarget().toLowerCase().endsWith(".trz")); //$NON-NLS-1$ //$NON-NLS-2$
    boolean isThumbnailType = isVideo || isZip || isImage;
    
    String thumb = isThumbnailType? record.getThumbnail(): null;
    if (isThumbnailType && thumb==null) {
			String source = getAbsoluteTarget();
			File thumbFile = getThumbnailFile();
			if (thumbFile.exists()) {
				thumb = thumbFile.getAbsolutePath();
				record.setThumbnail(thumb);
			}
			else {
		    new ThumbnailLoader(source, thumbFile.getAbsolutePath()).execute();
			}
    }
    if (thumb!=null) {
    	thumb = XML.forwardSlash(thumb);
    	thumb = ResourceLoader.getURIPath(thumb);
    }    
    
  	StringBuffer buffer = new StringBuffer();
    String collection = " "+ToolsRes.getString("LibraryResource.Type.Collection.Description"); //$NON-NLS-1$ //$NON-NLS-2$
    String title = record.getName();
    if ("".equals(title) && this.isRoot()) //$NON-NLS-1$
    	title = record.getTitle(treePanel.pathToRoot);
    for (String type: LibraryResource.allResourceTypes) {
    	if (type.equals(LibraryResource.UNKNOWN_TYPE)) continue;
    	if (type.equals(LibraryResource.PDF_TYPE)) continue;
    	String[] types = new String[] {type};    	
    	if (type.equals(LibraryResource.HTML_TYPE)) {
    		type = "Other"; //$NON-NLS-1$
    		types = new String[] {LibraryResource.HTML_TYPE, LibraryResource.PDF_TYPE, LibraryResource.UNKNOWN_TYPE};
    	}
    	ArrayList<LibraryResource> children = getChildResources(types);
      if (!children.isEmpty()) { // node has children
      	String s = "LibraryResource.Type."+type+".List"; //$NON-NLS-1$ //$NON-NLS-2$
        buffer.append("<p>"+ToolsRes.getString(s) //$NON-NLS-1$
        		+" "+title+collection+":</p>\n");  //$NON-NLS-1$//$NON-NLS-2$
        buffer.append("<ol>\n"); //$NON-NLS-1$
        for (LibraryResource next: children) {
        	String name = next.getName();
        	if (name.equals("")) //$NON-NLS-1$
        		name = ToolsRes.getString("LibraryResource.Name.Default"); //$NON-NLS-1$
          buffer.append("<li>"+name+"</li>\n"); //$NON-NLS-1$ //$NON-NLS-2$      	
        }
        buffer.append("</ol>\n"); //$NON-NLS-1$
      }   	
    }
    
    String description = buffer.toString();
    String htmlCode = LibraryResource.getHTMLBody(title, record.getType(),
    		thumb, description, null, null, null, null);
    return htmlCode;
  }
  
  /**
   * Returns the target of this node's resource.
   * The target may be absolute or relative to base path.
   * 
   * @return the target
   */
  protected String getTarget() {
		return record.getTarget();
  }

  /**
   * Returns the absolute target path of this node's resource.
   * 
   * @return the absolute target path
   */
  protected String getAbsoluteTarget() {
  	if (getTarget()==null) return null;
  	if (record instanceof LibraryCollection) {
	  	return getBasePath()+getTarget();
  	}
		return XML.getResolvedPath(getTarget(), getBasePath());
  }

  /**
   * Returns the target URL for this node, or null if target is empty or invalid.
   * If a cached file exists, this returns its URL instead of the original.
   * 
   * @return the URL
   */
  protected URL getTargetURL() {
  	String path = getAbsoluteTarget();
  	if (path==null) return null;
		String workingPath = path;
		URL url = null;

		String filename = record.getProperty("download_filename"); //$NON-NLS-1$
		File cachedFile = ResourceLoader.getOSPCacheFile(path, filename);
  	boolean foundInCache = cachedFile.exists();
		if (foundInCache) {
			workingPath = ResourceLoader.getURIPath(cachedFile.getAbsolutePath());
		}

	  // see if URL is in the map
  	if (targetURLs.keySet().contains(path)) { 
  		url = targetURLs.get(path);
  	}
  	else {
	  	// first try to get URL with raw path
  		Resource res = ResourceLoader.getResourceZipURLsOK(workingPath);
  		if (res!=null) {
  			url = res.getURL();
  		}  		
  		else {
	    	// try with URI form of path
  			workingPath = ResourceLoader.getURIPath(workingPath);
	  		res = ResourceLoader.getResourceZipURLsOK(workingPath);
	  		if (res!=null) {
	  			url = res.getURL();
	  		}
  		}
  	}
  	targetURLs.put(path, url);
  	return url;
  }
  
  /**
   * Used by the tree node to get the display name.
   *
   * @return the display name of the node
   */
  @Override
  public String toString() {
  	return record.toString();
  }
  
  /**
   * Determines if this node is editable.
   * Note: returns true only if this and its parent are editable.
   * 
   * @return true of editable
   */
  protected boolean isEditable() {
  	if (isRoot()) return editable;
  	LibraryTreeNode parent = (LibraryTreeNode)getParent();
  	return editable && parent.isEditable();
  }
  
  /**
   * Sets the editable property for this node.
   * 
   * @param edit true to make this node editable
   */
  protected void setEditable(boolean edit) {
  	editable = edit;
  }
  
  /**
   * Sets the name of this node's resource.
   * 
   * @param name the name
   */
  protected void setName(String name) {
		if (record.setName(name)) {
	  	treePanel.tree.getModel().valueForPathChanged(new TreePath(getPath()), name);
			treePanel.showInfo(this);
  		treePanel.setChanged();
		}
  }
  
  /**
   * Sets the target of this node's resource. May be absolute or relative path.
   * 
   * @param path the target path
   * @return true if changed
   */
  protected boolean setTarget(String path) {
		if (record.setTarget(path)) {
			// target has changed
			if (path==null) path = ""; //$NON-NLS-1$
			if (path.toLowerCase().endsWith(".trk") || path.toLowerCase().endsWith(".trz")) //$NON-NLS-1$ //$NON-NLS-2$
				setType(LibraryResource.TRACKER_TYPE);
			else if (path.indexOf("EJS")>-1) { //$NON-NLS-1$
				setType(LibraryResource.EJS_TYPE);
  		}
  		else if (path.toLowerCase().endsWith(".zip")) { //$NON-NLS-1$
		    Runnable runner = new Runnable() {
		      public void run() {
	  				String zipPath = getAbsoluteTarget();
						Set<String> files = ResourceLoader.getZipContents(zipPath);
						for (String next: files) {
							if (next.endsWith(".trk")) { //$NON-NLS-1$
								setType(LibraryResource.TRACKER_TYPE);
								break;
							}
						}
		      }
		    };
	      new Thread(runner).start();
			}
  		else if (path.equals("")) { //$NON-NLS-1$
  			if (getHTMLPath()==null)
  				setType(LibraryResource.UNKNOWN_TYPE);
  			else setType(LibraryResource.HTML_TYPE);
  		}
  		else {
  			boolean found = false;
  			for (FileFilter next: LibraryResource.imageFilters) {
  				if (found) break;
    			VideoFileFilter filter = (VideoFileFilter)next;
    			for (String ext: filter.getExtensions()) {  				
  					if (path.toUpperCase().endsWith("."+ext.toUpperCase())) { //$NON-NLS-1$
  						setType(LibraryResource.IMAGE_TYPE);
  						found = true;
  		  		}
    			}
  			}
	  		for (String ext: VideoIO.getVideoExtensions()) {
  				if (found) break;
					if (path.toUpperCase().endsWith("."+ext.toUpperCase())) { //$NON-NLS-1$
						setType(LibraryResource.VIDEO_TYPE);
						found = true;
		  		}
				}
  		}
  		LibraryTreePanel.htmlPanesByNode.remove(this);
  		record.setThumbnail(null);
			treePanel.showInfo(this);
  		treePanel.setChanged();
			tooltip = null; // triggers new tooltip
  		return true;
		}
		return false;
  }
  
  /**
   * Sets the html path of this node's resource.
   * 
   * @param path the html path
   */
  protected void setHTMLPath(String path) {
  	if (record.setHTMLPath(path)) {
  		treePanel.showInfo(this);
  		treePanel.setChanged();
			tooltip = null; // triggers new tooltip
  	}
  }
  
  /**
   * Sets the base path of this node's resource.
   * 
   * @param path the base path
   */
  protected void setBasePath(String path) {
		if (record.setBasePath(path)) {
  		LibraryTreePanel.htmlPanesByNode.remove(this);      			
  		record.setThumbnail(null);
			treePanel.showInfo(this);
  		treePanel.setChanged();
		}
  }
  
  /**
   * Sets the type of this node's resource.
   * The types are static constants defined by LibraryResource.
   * 
   * @param type the type
   */
  protected void setType(String type) {
  	if (record.setType(type)) {
  		LibraryTreePanel.htmlPanesByNode.remove(this);      			
  		treePanel.showInfo(this);
  		treePanel.setChanged();
			tooltip = null; // triggers new tooltip
  	}
  }
  
  /**
   * Returns this node's child resources, if any, of a given set of types.
   * The types are static constants defined by LibraryResource.
   * 
   * @param types an array of resource types
   * @return a list of LibraryResources
   */
  protected ArrayList<LibraryResource> getChildResources(String[] types) {
  	resources.clear();
  	for (String type: types) {
	    for (int i=0; i<getChildCount(); i++) {
	    	LibraryTreeNode child = (LibraryTreeNode)getChildAt(i);
	    	if (child.record.getType().equals(type))
	    		resources.add(child.record);
	    }
  	}
  	return resources;
  }
  
  /**
   * Returns the (multiline) tooltip for this node.
   * 
   * @return tooltip String
   */
  protected String getToolTip() {
  	if (tooltip==null) {
  		StringBuffer buf = new StringBuffer();
  		
  		// add path to collection types
  		if (record.getType().equals(LibraryResource.COLLECTION_TYPE)) {
	  		if (isRoot()) {
	  			if (!"".equals(treePanel.pathToRoot)) { //$NON-NLS-1$
	  				buf.append(ToolsRes.getString("LibraryTreeNode.Tooltip.CollectionPath")+": "+treePanel.pathToRoot); //$NON-NLS-1$ //$NON-NLS-2$
	  			}
	  		}
  		}
  		
  		// add metadata
  		Set<Metadata> data = record.getMetadata();
  		if (data!=null) {
	  		for (Metadata next: data) {
	  			String key = next.getData()[0];
	  			String value = next.getData()[1];
	  			boolean breakLine = false;
	  			for (String metadataType: LibraryResource.META_TYPES) {
	  				if (metadataType.toLowerCase().contains(key.toLowerCase()))
	  					key = ToolsRes.getString("LibraryTreePanel.Label."+metadataType); //$NON-NLS-1$
	  				breakLine = metadataType.equals(LibraryResource.META_KEYWORDS);
	  			}
	  			if (breakLine && value.length()>100) {
	  				int len = key.length();
	  				String space = ""; //$NON-NLS-1$
	  				for (int i=0; i<len; i++) {
	  					space += "  "; //$NON-NLS-1$
	  				}
	  				StringBuffer b = new StringBuffer();
	  				String line = value.substring(0, 80);
	  				String remainder = value.substring(80);
	  				while (true) {
		  				String[] parts = remainder.split(" ", 2); //$NON-NLS-1$
		  				b.append(line+parts[0]);
		  				if (parts.length==1) break;
		  				if (parts[1].length()<100) {
			  				b.append("\n"+space+parts[1]); //$NON-NLS-1$
		  					break;
		  				}
		  				b.append("\n"+space); //$NON-NLS-1$
		  				line = parts[1].substring(0, 80);
		  				remainder = parts[1].substring(80);
	  				}
	  				value = b.toString();
	  			}
	  			if (buf.length()>0) buf.append("\n"); //$NON-NLS-1$
	  			buf.append(key+": "+value); //$NON-NLS-1$	  			
	  		}
  		}
  		tooltip = buf.toString();  		
  	}
  	return tooltip.length()>0? tooltip: ToolsRes.getString("LibraryTreeNode.Tooltip.None"); //$NON-NLS-1$
  }
  
  /**
   * Returns the path to a source of metadata (usually HTML path)
   * 
   * @return the metadata path
   */
  protected String getMetadataSourcePath() {
  	String path = metadataSource;
		if (path!=null) {
	  	return XML.getResolvedPath(path, getBasePath());
		}
  	return getHTMLPath();
  }


  
  /**
   * Returns the metadata for this node.
   * 
   * @return a Set of Metadata entries
   */
  protected TreeSet<Metadata> getMetadata() {
  	TreeSet<Metadata> searchData = record.getMetadata();
  	if (searchData==null) {
  		searchData = new TreeSet<Metadata>();
  		record.setMetadata(searchData);
  		// look for metadata in HTML code
  		String path = getMetadataSourcePath();
    	if (path!=null) {
    		Resource res = ResourceLoader.getResourceZipURLsOK(path);
    		String code = res.getString();
				if (code!=null) {
					boolean[] isStandardType = new boolean[LibraryResource.META_TYPES.length];
					String[] parts = code.split("<meta name="); //$NON-NLS-1$
					for (int i=1; i<parts.length; i++) { // ignore parts[0]
						// parse metadata and add to HashMap
						int n = parts[i].indexOf("\">"); //$NON-NLS-1$
						if (n>-1) {
		  				parts[i] = parts[i].substring(0, n);
		    			String[] subparts = parts[i].split("content=\""); //$NON-NLS-1$
							if (subparts.length>1) {
								// subparts[0] is name in quotes
								String name = subparts[0].trim();
								if (name.startsWith("\"")) name = name.substring(1); //$NON-NLS-1$
								if (name.endsWith("\"")) name = name.substring(0, name.length()-1); //$NON-NLS-1$
								// assign to standard metadata type if appropriate
								for (int k=0; k<LibraryResource.META_TYPES.length; k++) {
									if (!isStandardType[k] && LibraryResource.META_TYPES[k].toLowerCase().contains(name.toLowerCase())) {
										name = LibraryResource.META_TYPES[k];
										isStandardType[k] = true;
									}
								}
								// subparts[1] is value
								String value = subparts[1].trim();
								record.addMetadata(new Metadata(name, value));
							}
						}
					}
				}
    	}
    	tooltip = null;
  	}
		return searchData;
  }
  
  /**
   * Returns the metadata value of a specified type.
   * @param key the type of the metadata
   * @return the value, or null if none
   */
  protected String getMetadataValue(String key) {
  	Set<Metadata> searchData = record.getMetadata();
  	if (searchData!=null) {
  		for (Metadata next: searchData) {
  			if (next.getData()[0].indexOf(key)>-1) return next.getData()[1];
  		}
  	}
  	return null;
  }
  
  /**
   * Returns a File that points to the cached thumbnail, if any, for this node.
   * Note: the thumbnail file may not exist--this just determines where it should be.
   * 
   * @return the thumbnail File, whether or not it exists
   */
  protected File getThumbnailFile() {
  	String thumbPath = record.getThumbnail();
  	if (thumbPath!=null)
  		return new File(thumbPath);
  	String path = getAbsoluteTarget();
  	String name = XML.stripExtension(XML.getName(path));
		String fileName = name+"_thumbnail.png"; //$NON-NLS-1$
		return ResourceLoader.getOSPCacheFile(path, fileName);
  }
  
  /**
   * Creates a thumbnail image and writes it to a specified path.
   * @param image the full-size image from which to create the thumbnail 
   * @param path the path for the thumbnail image file
   * @param maxSize the maximum size of the thumbnail image
   * @return the thumbnail File, or null if failed
   */
  protected File createThumbnailFile(BufferedImage image, String path, Dimension maxSize) {
  	// determine image resize factor
    double widthFactor = maxSize.getWidth()/image.getWidth();
    double heightFactor = maxSize.getHeight()/image.getHeight();
    double factor = Math.min(widthFactor, heightFactor);

    // determine dimensions of thumbnail image
    int w = (int)(image.getWidth()*factor);
    int h = (int)(image.getHeight()*factor);
  	
		// create and draw thumbnail image
    BufferedImage thumbnailImage = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = thumbnailImage.createGraphics();

    AffineTransform transform = AffineTransform.getScaleInstance(factor, factor);
    g.setTransform(transform);
    g.drawImage(image, 0, 0, null);
    
    // write thumbnail image to file
    return VideoIO.writeImageFile(thumbnailImage, path);
  }
  
  /**
   * A SwingWorker class to create new thumbnails.
   */
  class ThumbnailLoader extends SwingWorker<File, Object> {
  	String thumbPath, sourcePath;
  	
  	ThumbnailLoader(String imageSource, String thumbnailPath) {
  		thumbPath = thumbnailPath;
  		sourcePath = imageSource;
  	}
  	
    @Override
    public File doInBackground() {
			// create a new thumbnail
			File thumbFile = null; 
			String ext = XML.getExtension(sourcePath);
			
			// GIF files
			if (ext!=null && "GIF".equals(ext.toUpperCase())) { //$NON-NLS-1$
				GifDecoder decoder = new GifDecoder();
			  int status = decoder.read(sourcePath);
			  if(status!=0) { // error
					OSPLog.fine("failed to create thumbnail for GIF "+thumbPath); //$NON-NLS-1$
			  }
			  else {
			  	BufferedImage image = decoder.getImage();
			  	Dimension size = new Dimension(image.getWidth(), image.getHeight());
			  	thumbFile = createThumbnailFile(image, thumbPath, size);
			  }
			}
			
			// PNG and JPEG files
			else if (ext!=null && 
					("PNG".equals(ext.toUpperCase()) || ext.toUpperCase().contains("JP"))) { //$NON-NLS-1$ //$NON-NLS-2$

				try {
					URL url = new URL(ResourceLoader.getURIPath(sourcePath));
					BufferedImage image = ImageIO.read(url);
			  	Dimension size = new Dimension(image.getWidth(), image.getHeight());
			  	thumbFile = createThumbnailFile(image, thumbPath, size);
				} catch (Exception e) {
					OSPLog.fine("failed to create thumbnail for "+thumbPath); //$NON-NLS-1$
				}
			}

			// ZIP files
			else if (ext!=null && ("ZIP".equals(ext.toUpperCase()) || "TRZ".equals(ext.toUpperCase()))) { //$NON-NLS-1$ //$NON-NLS-2$
				// look for image file in zip with name that includes "_thumbnail"
				for (String next: ResourceLoader.getZipContents(sourcePath)) {
					if (next.indexOf("_thumbnail")>-1) { //$NON-NLS-1$
						String s = ResourceLoader.getURIPath(sourcePath+"!/"+next); //$NON-NLS-1$
						thumbFile = JarTool.extract(s, new File(thumbPath));
					}
				}							
			}
			
			// video files: use Xuggle thumbnail tool, if available
			else {
	      String className = "org.opensourcephysics.media.xuggle.XuggleThumbnailTool"; //$NON-NLS-1$
	      Class<?>[] types = new Class<?>[] {Dimension.class, String.class, String.class};
	      Object[] values = new Object[] {defaultThumbnailDimension, sourcePath, thumbPath};
		    try {
		      Class<?> xuggleClass = Class.forName(className);
		      Method method=xuggleClass.getMethod("createThumbnailFile", types); //$NON-NLS-1$
		      thumbFile = (File)method.invoke(null, values);
				} catch(Exception ex) {
					OSPLog.fine("failed to create thumbnail: "+ex.toString()); //$NON-NLS-1$
				} catch(Error err) {
				}
			 }
			return thumbFile;
    }

    @Override
    protected void done() {
      try {
     	 File thumbFile = get();
       record.setThumbnail(thumbFile==null || !thumbFile.exists()? null: thumbFile.getAbsolutePath());
       
       if (record.getThumbnail()!=null) {
      	 LibraryTreePanel.htmlPanesByNode.remove(LibraryTreeNode.this);      			
      	 treePanel.showInfo(treePanel.getSelectedNode());
       }
      } catch (Exception ignore) {
      }
    }
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
