/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <https://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;

import java.io.File;
import java.net.URL;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.opensourcephysics.controls.OSPLog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This provides static methods for getting a LibraryCollection from ComPADRE.
 * Adapted from code written for EJS by Francisco Esquembre.
 * 
 * @author Francisco Esquembre, Douglas Brown
 * @version 1.0
 */
  @SuppressWarnings("javadoc")
public class LibraryComPADRE {
	public static final String OSP_INFO_URL="https://www.compadre.org/OSP/online_help/EjsDL/OSPCollection.html"; //$NON-NLS-1$
  public static final String EJS_SERVER_TREE="https://www.compadre.org/osp/services/REST/osp_jars.cfm?verb=Identify&OSPType=EJS%20Model&AttachedDocument=Source%20Code"; //$NON-NLS-1$
  public static final String EJS_SERVER_RECORDS="https://www.compadre.org/osp/services/REST/osp_jars.cfm?OSPType=EJS%20Model&AttachedDocument=Source%20Code"; //$NON-NLS-1$
  public static final String EJS_COLLECTION_NAME="EJS OSP Collection"; //$NON-NLS-1$
  public static final String EJS_INFO_URL="https://www.compadre.org/OSP/online_help/EjsDL/DLModels.html"; //$NON-NLS-1$
  public static final String TRACKER_SERVER_TREE="https://www.compadre.org/osp/services/REST/osp_tracker.cfm?verb=Identify&OSPType=Tracker"; //$NON-NLS-1$
  public static final String TRACKER_SERVER_RECORDS="https://www.compadre.org/osp/services/REST/osp_tracker.cfm?OSPType=Tracker"; //$NON-NLS-1$
  public static final String TRACKER_COLLECTION_NAME="Tracker OSP Collection"; //$NON-NLS-1$
  public static final String TRACKER_INFO_URL="https://physlets.org/tracker/library/comPADRE_collection.html"; //$NON-NLS-1$
  public static final String PRIMARY_ONLY="&OSPPrimary=Subject"; //$NON-NLS-1$
  public static final String GENERIC_COLLECTION_NAME="ComPADRE OSP Collection"; //$NON-NLS-1$
  public static final String ABOUT_OSP="About OSP and ComPADRE"; //$NON-NLS-1$
  public static final String HOST="www.compadre.org"; //$NON-NLS-1$
  public static String desiredOSPType; // if non-null, <osp-type> must contain this string

  /**
   * Loads a collection using a specified comPADRE search query.
   * 
   * @param collection the LibraryCollection to load
   * @param query the search query
   * @return true if successfully loaded
   */
  protected static boolean load(LibraryCollection collection, String query) { 
    try {
      URL url = new URL(query);
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      Document doc = factory.newDocumentBuilder().parse(url.openStream());
//      writeXmlFile(doc, "compadre_catalog.txt"); // for testing
      NodeList nodeList = doc.getElementsByTagName("Identify"); //$NON-NLS-1$
      boolean success = false;
      for (int i=0; i<nodeList.getLength(); i++) {
      	success = loadSubtrees(collection, nodeList.item(i).getChildNodes(),
      			"osp-subject", "") || success; //$NON-NLS-1$ //$NON-NLS-2$
      }
      return success;
    } 
    catch(Exception e) {
    	OSPLog.warning("failed to load ComPADRE collection "+query); //$NON-NLS-1$
    }
    return false;
  }
  
  /**
   * Loads a collection with subtree collections that meet the specified requirements.
   * 
   * @param collection the LibraryCollection to load
   * @param nodeList a list of Nodes
   * @param attributeType the desired attribute
   * @param serviceParameter the desired service parameter
   * @return true if at least one subtree collection was loaded
   */
  protected static boolean loadSubtrees(LibraryCollection collection, NodeList nodeList, 
  		String attributeType, String serviceParameter) {
  	boolean success = false;
    String dblClick = ToolsRes.getString("LibraryComPADRE.Description.DoubleClick");  //$NON-NLS-1$
    for (int i=0; i<nodeList.getLength(); i++) {
      if (!(nodeList.item(i) instanceof Element)) continue;
      Element node = (Element)nodeList.item(i);
      if (node.getNodeName().equals("sub-tree-set") && attributeType.equals(node.getAttribute("type")) ) { //$NON-NLS-1$ //$NON-NLS-2$
        List<Node> subTrees = getAllChildren(node, "sub-tree"); //$NON-NLS-1$
        if (subTrees.size()>0) { // node has subcategories
          String unclassifiedURL = null;
          for (int j=0; j<subTrees.size(); j++) {
            if (!(subTrees.get(j) instanceof Element)) continue;
            Element subtree = (Element)subTrees.get(j);
            String name = subtree.getAttribute("name"); //$NON-NLS-1$
            String serviceParam = subtree.getAttribute("service-parameter"); //$NON-NLS-1$
            serviceParam = serviceParameter+"&"+ResourceLoader.getNonURIPath(serviceParam); //$NON-NLS-1$
            if (name.equals("Unclassified")) { // unclassified node is processed last and adds its records to the parent //$NON-NLS-1$
              unclassifiedURL = serviceParam;
              continue;
            }
            LibraryCollection subCollection = new LibraryCollection(name);
            collection.addResource(subCollection);
            success = true;
            if (getAllChildren(subtree, "sub-tree-set").isEmpty()) { // has no subcategories //$NON-NLS-1$
              String nodeName = "<h2>"+name+"</h2><blockquote>"; //$NON-NLS-1$ //$NON-NLS-2$
            	subCollection.setDescription(nodeName+dblClick+"</blockquote>"); //$NON-NLS-1$
              subCollection.setTarget(serviceParam);
            }
            else
            	loadSubtrees(subCollection, subtree.getChildNodes(), attributeType+"-detail", serviceParam); //$NON-NLS-1$
          }
	        if (unclassifiedURL!=null) {
	        	collection.setTarget(unclassifiedURL);
	        }
        }
      }
    }
    return success;
  }
    
  /**
   * Loads ComPADRE records into a LibraryTreeNode collection.
   * 
   * @param treeNode the LibraryTreeNode to load--note record MUST be a collection 
   * @return true if one or more ComPADRE records were successfully loaded
   */
  protected static boolean loadResources(LibraryTreeNode treeNode) {
  	LibraryCollection collection = (LibraryCollection)treeNode.record;
  	boolean success = false;
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      String urlPath = treeNode.getAbsoluteTarget();
      URL url = new URL(urlPath);
      Document doc = factory.newDocumentBuilder().parse(url.openStream());
//      writeXmlFile(doc, "compadre_resource.txt"); // for testing

      // look at all ComPADRE records in the document
      NodeList list = doc.getElementsByTagName("record"); //$NON-NLS-1$
      for (int i=0; i<list.getLength(); i++) { // process nodes
        Node node = list.item(i);
      	String[] attachment = null;
      	if (isDesiredOSPType(node)) {
      		if ("EJS".equals(desiredOSPType)) { //$NON-NLS-1$
      			attachment = getAttachment(node, "Source Code"); //$NON-NLS-1$ 
      		}
        	else {
  	        attachment = getAttachment(node, "Main"); //$NON-NLS-1$
  	        if (attachment==null) {
  	        	attachment = getAttachment(node, "Supplemental"); //$NON-NLS-1$
  	        }      		
        	}
      	}
        // ignore if there is no associated attachment
        if (attachment==null) continue;
        
        // create and add a new record to this node's collection
        String name = getChildValue(node, "title");  //$NON-NLS-1$
        LibraryResource record = new LibraryResource(name);
        collection.addResource(record);
        
        if (loadResource(record, node, attachment, treeNode)) {
        	success = true;
	  			record.setProperty("reload_url", urlPath); //$NON-NLS-1$
        }
      }
    }
    catch(Exception e) { e.printStackTrace(); }
    
    // clear the collection target and description
    collection.setDescription(null);
    collection.setTarget(null);
    return success;
  }
  
  
  /**
   * Reloads a ComPADRE record into a LibraryTreeNode.
   * 
   * @param treeNode the LibraryTreeNode to reload 
   * @return true if successfully reloaded
   */
  protected static boolean reloadResource(LibraryTreeNode treeNode, String urlPath) {
  	boolean success = false;
    try {
	  	LibraryResource record = treeNode.record;
      URL url = new URL(urlPath);
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      Document doc = factory.newDocumentBuilder().parse(url.openStream());

      // look through all ComPADRE records for a match
      NodeList list = doc.getElementsByTagName("record"); //$NON-NLS-1$
      for (int i=0; i<list.getLength(); i++) { // process nodes
        Node node = list.item(i);
      	String[] attachment = null;
      	if (isDesiredOSPType(node)) {
      		if ("EJS".equals(desiredOSPType)) { //$NON-NLS-1$
      			attachment = getAttachment(node, "Source Code"); //$NON-NLS-1$ 
      		}
        	else {
  	        attachment = getAttachment(node, "Main"); //$NON-NLS-1$
  	        if (attachment==null) {
  	        	attachment = getAttachment(node, "Supplemental"); //$NON-NLS-1$
  	        }      		
        	}
      	}
        // ignore if there is no associated attachment
        if (attachment==null) continue;
        
        // check to see that the target is the one desired
        String downloadURL = processURL(attachment[0]);
        if (!downloadURL.equals(record.getTarget())) continue;
        
        return loadResource(record, node, attachment, treeNode);        
      }
    }
    catch(Exception e) { e.printStackTrace(); }
    return success;
  }
  
  /**
   * Reloads a ComPADRE record into a LibraryTreeNode.
   * 
   * @param treeNode the LibraryTreeNode to reload 
   * @return true if successfully reloaded
   */
  protected static boolean loadResource(LibraryResource record, Node node, String[] attachment, LibraryTreeNode treeNode) {
    try {        
      // get the node data and create the HTML code
      String downloadURL = processURL(attachment[0]);
      record.setTarget(downloadURL);
      String name = getChildValue(node, "title");  //$NON-NLS-1$
      record.setName(name);
      record.setProperty("download_filename", attachment[1]); //$NON-NLS-1$
      String type = getChildValue(node, "osp-type");  //$NON-NLS-1$
    	if (isDesiredOSPType(node)) {
    		if ("EJS".equals(desiredOSPType)) { //$NON-NLS-1$
        	type = LibraryResource.EJS_TYPE;
          record.setType(type);		
    		}
      	else if ("Tracker".equals(desiredOSPType)){ //$NON-NLS-1$
        	type = LibraryResource.TRACKER_TYPE;
          record.setType(type);		
      	}
      	else {
      		record.setType(LibraryResource.UNKNOWN_TYPE);
      	}
    	}

      String description = getChildValue(node, "description"); //$NON-NLS-1$
      String infoURL = getChildValue(node, "information-url"); //$NON-NLS-1$
      String thumbnailURL = getChildValue(node,"thumbnail-url"); //$NON-NLS-1$
      String authors = ""; //$NON-NLS-1$
      for (Node next: getAllChildren(getFirstChild(node, "contributors"), "contributor")) { //$NON-NLS-1$ //$NON-NLS-2$
        Element el = (Element)next;
        if ("Author".equals(el.getAttribute("role")))  //$NON-NLS-1$ //$NON-NLS-2$
        	authors += getNodeValue(next)+", "; //$NON-NLS-1$
      }
      if (authors.endsWith(", "))  //$NON-NLS-1$
      	authors = authors.substring(0, authors.length()-2);
      
  		// cache the thumbnail
      File cachedFile = ResourceLoader.getOSPCacheFile(thumbnailURL);
      String cachePath = cachedFile.getAbsolutePath();
  		record.setThumbnail(cachePath);
  		if (!cachedFile.exists()) {
		    treeNode.new ThumbnailLoader(thumbnailURL, cachePath).execute();
  		}
  		thumbnailURL = ResourceLoader.getURIPath(cachePath);
     
      // get the html code and set the description
      String htmlCode = LibraryResource.getHTMLBody(name, type,	thumbnailURL, description, authors, null, infoURL, attachment);
      record.setDescription(htmlCode);
      
      // convert <osp-subject> information into keywords and add metadata to the library record
      record.setMetadata(null);
      ArrayList<String> words = new ArrayList<String>();
      for (Node next: getAllChildren(node, "osp-subject")) { //$NON-NLS-1$
      	String[] subjects = getNodeValue(next).split(" / "); //$NON-NLS-1$
      	for (String s: subjects) {
      		// skip "General" subject--too vague
      		if (s.equals("General")) continue; //$NON-NLS-1$
      		if (!words.contains(s)) words.add(s);
      	}
      }        
      if (!words.isEmpty()) {
      	StringBuffer buf = new StringBuffer();
      	for (String s: words) {
      		buf.append(s+", "); //$NON-NLS-1$
      	}
      	String keywords = buf.toString();
      	keywords = keywords.substring(0, keywords.length()-2);
        record.addMetadata(new LibraryResource.Metadata("keywords", keywords)); //$NON-NLS-1$
      }
      if (!"".equals(authors))  //$NON-NLS-1$
      	record.addMetadata(new LibraryResource.Metadata("author", authors)); //$NON-NLS-1$

      return true;
    }
    catch(Exception e) { e.printStackTrace(); }
    return false;
  }
    
  /**
   * Returns data for a downloadable DOM Node attachment.
   * 
   * @param node the DOM Node
   * @param attachmentType the attachment type
   * @return String[] {URL, filename, size in Bytes}, or null if no attachment found
   */
  protected static String[] getAttachment(Node node, String attachmentType) {
  	String id = getChildValue(node, "file-identifier"); //$NON-NLS-1$
  	NodeList childList = node.getChildNodes();
  	String[] attachment = null;
  	for (int i=0,n=childList.getLength(); i<n; i++) {
      Node child = childList.item(i);
      if (!child.getNodeName().equals("attached-document")) continue; //$NON-NLS-1$
      Node fileTypeNode = getFirstChild(child,"file-type"); //$NON-NLS-1$
      if (fileTypeNode!=null && attachmentType.equals(getNodeValue(fileTypeNode))) {
        Node urlNode = getFirstChild(child,"download-url"); //$NON-NLS-1$
        if (urlNode!=null) { // found downloadable attachment
	      	// keep first attachment or (preferred) attachment with the same id as the node
        	if (attachment==null || id.equals(getChildValue(child, "file-identifier"))) { //$NON-NLS-1$
	          String attachmentURL = getNodeValue(urlNode);
	          Element fileNode = (Element)getFirstChild(child,"file-name"); //$NON-NLS-1$
	          if (fileNode!=null) {
	          	attachment = new String[] {attachmentURL, getNodeValue(fileNode),
	          		fileNode.getAttribute("file-size")}; //$NON-NLS-1$
	          }
	          else attachment = new String[] {attachmentURL, null, null};
        	}
        }
      }
    }
    return attachment;
  }

  /**
   * Returns the first child node with the given name.
   * 
   * @param parent the parent Node
   * @param name the child name
   * @return the first child Node found, or null if none
   */
  protected static Node getFirstChild(Node parent, String name) {
    NodeList childList = parent.getChildNodes();
    for (int i=0,n=childList.getLength(); i<n; i++) {
      Node child = childList.item(i);
      if (child.getNodeName().equals(name)) 
      	return child;
    }
    return null;
  }

  /**
   * Returns all child nodes with the given name.
   * 
   * @param parent the parent Node
   * @param name the name
   * @return a list of Nodes (may be empty)
   */
  protected static List<Node> getAllChildren(Node parent, String name) {
    java.util.List<Node> list = new ArrayList<Node>();
    NodeList childrenList = parent.getChildNodes();
    for (int i=0,n=childrenList.getLength(); i<n; i++) {
      Node child = childrenList.item(i);
      if (child.getNodeName().equals(name)) list.add(child);
    }
    return list;
  }

  /**
   * Gets the value of a Node.
   * 
   * @param node the Node
   * @return the value
   */
  protected static String getNodeValue(Node node) {
    for (Node child = node.getFirstChild(); child!=null; child=child.getNextSibling() ){
      if (child.getNodeType()==Node.TEXT_NODE) 
      	return child.getNodeValue();
    }
    return null;
  }    

  /**
   * Gets the value of the first child node with a given name.
   * @param parent the parent Node
   * @param name the name of the child
   * @return the value of the first child found, or null if none
   */
  protected static String getChildValue(Node parent, String name) {
    Node node = getFirstChild(parent,name);
    if (node!=null) 
    	return getNodeValue(node);
    return null;
  }    

  /**
   * Replaces "&amp" with "&" in HTML code.
   * @param url the HTML code
   * @return the clean URL string
   */
  protected static String processURL(String url) {
    StringBuffer processed=new StringBuffer();
    int index = url.indexOf("&amp;"); //$NON-NLS-1$
    while (index>=0) {
      processed.append(url.subSequence(0,index+1));
      url = url.substring(index+5);
      index = url.indexOf("&amp;"); //$NON-NLS-1$
    }
    processed.append(url);
    return processed.toString();
  }

  /**
   * Writes a DOM document to a file for testing.
   * 
   * @param doc the Document
   * @param filename the filename to write to
   * @return the String contents of the document
   */
  protected static String writeXmlFile(Document doc, String filename) {
    try {
      // Prepare the DOM document for writing
      Source source = new DOMSource(doc);

      // Prepare the output file
      File file = new File(filename);
      Result result = new StreamResult(file);

      // Write the DOM document to the file
      Transformer xformer = TransformerFactory.newInstance().newTransformer();
      xformer.transform(source, result);
      return ResourceLoader.getString(filename);
    } catch (Exception e) {}
    return null;
  }
  
  /**
   * Returns a descriptive name for a given ComPADRE path (query).
   * 
   * @param path the query string
   * @return the name of the collection
   */
  public static String getCollectionName(String path) {
  	if (path.startsWith(EJS_SERVER_TREE)) 
  		return EJS_COLLECTION_NAME;
  	if (path.startsWith(TRACKER_SERVER_TREE)) 
  		return TRACKER_COLLECTION_NAME;
  	return GENERIC_COLLECTION_NAME;
  }

  /**
   * Returns the LibraryCollection for a given ComPADRE path (query).
   * 
   * @param path the query string
   * @return the collection
   */
  protected static LibraryCollection getCollection(String path) {
  	String name = getCollectionName(path);
  	boolean primarySubjectOnly = path.indexOf(PRIMARY_ONLY)>-1;
  	LibraryCollection collection = new LibraryCollection(name);
  	if (name.equals(EJS_COLLECTION_NAME)) {
  		collection.setHTMLPath(EJS_INFO_URL);
  	}
  	else if (name.equals(TRACKER_COLLECTION_NAME)) {
  		collection.setHTMLPath(TRACKER_INFO_URL);
  	}
    LibraryResource aboutOSP = new LibraryResource(ABOUT_OSP);
    aboutOSP.setHTMLPath(OSP_INFO_URL); 
    collection.addResource(aboutOSP);
  	load(collection, path);
  	String base = EJS_SERVER_RECORDS;
  	if (name.equals(TRACKER_COLLECTION_NAME)) {
    	base = TRACKER_SERVER_RECORDS; 		
  	}
  	if (primarySubjectOnly) base += PRIMARY_ONLY;
  	collection.setBasePath(base);
  	
  	return collection;
  }

  /**
   * Returns the collection path for an EJS or tracker tree.
   * 
   * @param path the ComPADRE query string
   * @param primarySubjectOnly true to limit results to their primary subject
   * @return the corrected ComPADRE query string
   */
  protected static String getCollectionPath(String path, boolean primarySubjectOnly) {
  	boolean isPrimary = path.endsWith(PRIMARY_ONLY);
  	if (isPrimary && primarySubjectOnly) return path;
  	if (!isPrimary && !primarySubjectOnly) return path;
  	if (!isPrimary && primarySubjectOnly) return path+PRIMARY_ONLY;
  	return path.substring(0, path.length()-PRIMARY_ONLY.length());
  }

  /**
   * Determines if a path is a valid ComPADRE query.
   * 
   * @param path the path
   * @return true if path is a valid ComPADRE query string
   */
  protected static boolean isComPADREPath(String path) {
  	if (path.startsWith(EJS_SERVER_TREE)
  			|| path.startsWith(TRACKER_SERVER_TREE)) 
  		return true;
  	return false;
  }

  /**
   * Determines if a query path limits results to the primary subject only.
   * 
   * @param path the path
   * @return true if path contains a primary-subject-only flag
   */
  protected static boolean isPrimarySubjectOnly(String path) {
  	return path.indexOf(PRIMARY_ONLY)>-1;
  }

  /**
   * Determines if a node has an <osp-type> consistent with desiredOSPType.
   * 
   * @param node the node
   * @return true if is of the desired <osp-type>
   */
  protected static boolean isDesiredOSPType(Node node) {
  	List<Node> nodes = getAllChildren(node, "osp-type"); //$NON-NLS-1$
  	for (Node next: nodes) {
	  	// if desiredOSPType has not been specified then all osp-types are valid
  		if (desiredOSPType==null || getNodeValue(next).contains(desiredOSPType)) return true;
  	}
  	return false;
  }

}