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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.ResizableIcon;
import org.opensourcephysics.tools.LibraryResource.Metadata;

/**
 * This is a JPanel that displays an OSP LibraryResource at the root of a tree.
 * If the resource is a LibraryCollection, the tree is populated with its child resources.
 *
 * @author Douglas Brown
 */
public class LibraryTreePanel extends JPanel {
	
  // static constants
	protected final String AND = " AND "; //$NON-NLS-1$
	protected final String OR = " OR "; //$NON-NLS-1$
	protected final String OPENING = "("; //$NON-NLS-1$
	protected final String CLOSING = ")"; //$NON-NLS-1$
  
	// static fields
	protected static int keyFieldWidth = 100;
  protected static Color lightRed = new Color(255, 180, 200);
  protected static Color darkRed = new Color(220, 0, 0);
  protected static Color lightGreen = new Color(100, 200, 100);
  protected static Color defaultForeground;
  protected static Icon openFileIcon;
  protected static HyperlinkListener hyperlinkListener;
  protected static JFileChooser chooser;
  protected static FileFilter htmlFilter, folderFilter;
  protected static HashMap<URL, HTMLPane> htmlPanesByURL
  		= new HashMap<URL, HTMLPane>();
  protected static HashMap<LibraryTreeNode, HTMLPane> htmlPanesByNode
			= new HashMap<LibraryTreeNode, HTMLPane>();
  
	static {
    String imageFile = "/org/opensourcephysics/resources/tools/images/open.gif";        //$NON-NLS-1$
    openFileIcon = new ImageIcon(LibraryTreePanel.class.getResource(imageFile));
    hyperlinkListener = new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if(e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
          try {
          	// try the preferred way
            if (!org.opensourcephysics.desktop.OSPDesktop.browse(e.getURL().toURI())) {
              // try the old way
              org.opensourcephysics.desktop.ostermiller.Browser.init();
              org.opensourcephysics.desktop.ostermiller.Browser.displayURL(e.getURL().toString());
            }
          } catch(Exception ex) {}
        }
      }
    };
	}
	
  // instance fields
	protected LibraryBrowser browser;
	protected LibraryResource rootResource; // the root resource or collection displayed by this panel
	protected String pathToRoot;
  protected LibraryTreeNode rootNode;
  protected DefaultTreeModel treeModel;
  protected LibraryTreeNodeRenderer treeNodeRenderer;
  protected JTree tree;
  protected JScrollPane treeScroller = new JScrollPane();
  protected JScrollPane htmlScroller = new JScrollPane();
  protected JToolBar editorbar;
  protected Action cutAction, copyAction, pasteAction;
  protected Action addCollectionAction, addResourceAction;
  protected Action moveUpAction, moveDownAction, metadataAction;
  protected JButton cutButton, copyButton, pasteButton;
  protected JButton addCollectionButton, addResourceButton;
  protected JButton moveUpButton, moveDownButton, metadataButton;
  protected Box editorPanel, fileBox;
  protected JPanel displayPanel;
  protected HTMLPane emptyHTMLPane;
  protected JSplitPane splitPane;
  protected EntryField nameField, htmlField, basePathField, targetField;
  protected JLabel nameLabel, htmlLabel, basePathLabel, targetLabel;
  // metadata items
	protected ActionListener metadataFieldListener;
  protected EntryField authorField, contactField, keywordsField;
  protected JLabel authorLabel, contactLabel, keywordsLabel, metadataLabel;
  protected Box authorBox, contactBox, keywordsBox, metadataBox;
  protected MetadataComboBoxModel metadataModel;
	protected JComboBox metadataDropdown;
  protected MetadataEditField keyEditField, valueEditField;
  protected JLabel typeLabel, typeField;
  protected JButton openHTMLButton, openBasePathButton, openFileButton;
  protected ArrayList<JLabel> labels = new ArrayList<JLabel>();
  protected JPopupMenu popup;
  protected MouseAdapter treeMouseListener, convertPathMouseListener;
  protected TreeSelectionListener treeSelectionListener;
  protected XMLControl pasteControl;
  protected boolean isEditing, isChanged, isXMLPath, ignoreChanges;
  protected XMLControl revertControl;
  protected int typeFieldWidth;
  protected String command;
  protected Metadata emptyMetadata = new Metadata();
  protected MetadataLoader metadataLoader;
  protected Set<EntryField> entryFields = new HashSet<EntryField>();

  /**
   * Constructs an empty LibraryTreePanel.
   * 
   * @param browser the LibraryBrowser that will display this panel
   */
  public LibraryTreePanel(LibraryBrowser browser) {
    super(new BorderLayout());
    this.browser = browser;
    createGUI();
  }

  /**
   * Sets the root resource or collection displayed in the tree.
   * 
   * @param resource the resource
   * @param path the file path to the resource or collection
   * @param editable true if the collection is user-editable
   * @param pathIsXML true if the path points to a DL xml file
   */
  public void setRootResource(LibraryResource resource, String path, boolean editable, boolean pathIsXML) {
    rootResource = resource;
    isXMLPath = pathIsXML;
    pathToRoot = path;
    // clean up existing tree, if any
    if (tree!=null) {
      tree.removeTreeSelectionListener(treeSelectionListener);
      tree.removeMouseListener(treeMouseListener);    	
    }
    setEditing(false);
    if (resource!=null) {
    	ignoreChanges = true;
    	// create new tree
		  rootNode = new LibraryTreeNode(resource, this);
		  rootNode.setEditable(editable);
	    createTree(rootNode);
	    tree.setSelectionRow(0);
	    splitPane.setDividerLocation(treeScroller.getPreferredSize().width);
	    isChanged = false;
	    ignoreChanges = false;
	    refreshGUI();	    
	  }
  }
  
  /**
   * Gets the collection displayed in the tree.
   * 
   * @return the collection
   */
  public LibraryResource getCollection() {
    return rootResource;
  }

  /**
   * Gets the selected node.
   * 
   * @return the selected node, or null if none
   */
  protected LibraryTreeNode getSelectedNode() {
  	if (tree==null) return null;
  	return (LibraryTreeNode) tree.getLastSelectedPathComponent();
  }
  
  /**
   * Sets the selected node.
   *
   * @param node the node to select
   */
  protected void setSelectedNode(LibraryTreeNode node) {
    if(node!=null) {
      tree.setSelectionPath(new TreePath(node.getPath()));
    }
    else
    	tree.setSelectionPath(null);
  }

  /**
   * Sets the selection path.
   *
   * @param treePath tree path to select in root-first order (may be null)
   */
  protected void setSelectionPath(List<String> treePath) {
    if (treePath!=null && treePath.get(0).equals(rootNode.toString())) {
      LibraryTreeNode node = rootNode;
      for (int i=1; i<treePath.size(); i++) {
      	String name = treePath.get(i);
      	int n = node.getChildCount();
      	inner: for (int j=0; j<n; j++) {
      		if (name.equals(node.getChildAt(j).toString())) {
      			node = (LibraryTreeNode)node.getChildAt(j);
      			break inner;
      		}
      	}
      }
      setSelectedNode(node);
    }
  }

  /**
   * Sets the font level.
   *
   * @param level the desired font level
   */
  protected void setFontLevel(int level) {
		
		Object[] toSize = new Object[] {
				splitPane, editorPanel, editorbar, authorField, contactField, keywordsField,
				authorLabel, contactLabel, keywordsLabel, metadataLabel, metadataDropdown};
		FontSizer.setFonts(toSize, level);
		EntryField.font = authorField.getFont();
		
		// resize treeNodeRenderer and LibraryResource icons
		treeNodeRenderer.resizableOpenIcon.resize(FontSizer.getIntegerFactor());
		treeNodeRenderer.resizableClosedIcon.resize(FontSizer.getIntegerFactor());
		LibraryResource.trackerIcon.resize(FontSizer.getIntegerFactor());
		LibraryResource.ejsIcon.resize(FontSizer.getIntegerFactor());
		LibraryResource.imageIcon.resize(FontSizer.getIntegerFactor());
		LibraryResource.videoIcon.resize(FontSizer.getIntegerFactor());
		LibraryResource.htmlIcon.resize(FontSizer.getIntegerFactor());
		LibraryResource.pdfIcon.resize(FontSizer.getIntegerFactor());
		LibraryResource.unknownIcon.resize(FontSizer.getIntegerFactor());
		
		// resize LibraryResource stylesheet font sizes
		LibraryResource.bodyFont = FontSizer.getResizedFont(LibraryResource.bodyFont, level);
		LibraryResource.h1Font = FontSizer.getResizedFont(LibraryResource.h1Font, level);
		LibraryResource.h2Font = FontSizer.getResizedFont(LibraryResource.h2Font, level);
		
		// clear htmlPanesByNode to force new HTML code with new stylesheets
		htmlPanesByNode.clear();
		
		// refresh the tree structure
		TreeModel model = tree.getModel();
		if (model instanceof DefaultTreeModel) {
			LibraryTreeNode selectedNode = getSelectedNode();
			DefaultTreeModel treeModel = (DefaultTreeModel)model;
			treeModel.nodeStructureChanged(rootNode);
			setSelectedNode(selectedNode);
		}
		refreshGUI();

  }
  
  /**
   * Sets the editing state.
   *
   * @param edit true to start editing, false to stop
   */
  protected void setEditing(boolean edit) {
  	isEditing = edit;
  	if (isEditing) {
  		displayPanel.add(editorPanel, BorderLayout.NORTH);
      add(editorbar, BorderLayout.NORTH);
      showInfo(getSelectedNode());
    }
  	else {
  		displayPanel.remove(editorPanel);
      remove(editorbar);
  	}  	
  	validate();
  	if (isEditing) {
  		revertControl = new XMLControlElement(rootResource);
  	}
  }

  /**
   * Returns true if the collection is editable.
   *
   * @return true if editable
   */
  protected boolean isEditable() {
  	boolean editable = rootNode!=null && rootNode.isEditable();
  	if (editable && !pathToRoot.startsWith("http:")) { //$NON-NLS-1$
  		File file = new File(pathToRoot);
  		editable = !file.exists() || file.canWrite();
  	}
  	return editable;
  }
  
  /**
   * Gets the editing state.
   *
   * @return true if editing
   */
  protected boolean isEditing() {
  	return isEditing;
  }
  
  /**
   * Displays the resource data for the specified node.
   *
   * @param node the LibraryTreeNode
   */
  protected void showInfo(LibraryTreeNode node) {
  	if (node==null) {
  		htmlScroller.setViewportView(emptyHTMLPane);
    	nameField.setText(null);
    	typeField.setText(" "); //$NON-NLS-1$
    	basePathField.setText(null);
    	htmlField.setText(null);
    	targetField.setText(null);
    	nameField.setBackground(Color.white);
    	basePathField.setBackground(Color.white);
    	htmlField.setBackground(Color.white);
    	targetField.setBackground(Color.white);
    	nameField.setEnabled(false);
    	basePathField.setEnabled(false);
    	htmlField.setEnabled(false);
    	targetField.setEnabled(false);
    	typeField.setEnabled(false);
    	nameLabel.setEnabled(false);
    	htmlLabel.setEnabled(false);
    	basePathLabel.setEnabled(false);
    	targetLabel.setEnabled(false);
    	typeLabel.setEnabled(false);
    	openHTMLButton.setEnabled(false);
    	openBasePathButton.setEnabled(false);
    	openFileButton.setEnabled(false);
    	return;
  	}
  	// show node data
		showHTMLPane(node);
		
  	boolean isCollection = node.record instanceof LibraryCollection;
  	String path = node.isRoot()? pathToRoot: node.getAbsoluteTarget();
  	LibraryTreePanel selected = browser.getSelectedTreePanel();
  	if (selected==this && !browser.commandField.getText().equals(path)) {
  		browser.commandField.setText(path);
      // check to see if resource is available
  		boolean available = false;
  		if (path!=null) { 
  			if (path.startsWith("http:/")) {//$NON-NLS-1$
	  			available = LibraryBrowser.isWebConnected();
	  			if (!available) {
	  				File cachedFile = null;
			  		if (isCollection) {
			  			cachedFile = ResourceLoader.getSearchCacheFile(path);
			  		}
			  		else {
				  		String filename = node.record.getProperty("download_filename"); //$NON-NLS-1$
			  			cachedFile = ResourceLoader.getOSPCacheFile(path, filename);
			  		}
		      	available = cachedFile.exists();
	  			}
	  		}
  			else {
  				available = ResourceLoader.getResourceZipURLsOK(ResourceLoader.getURIPath(path))!=null;
  			}
  		}
      browser.commandField.setForeground(available? defaultForeground: darkRed);
  		browser.commandField.setCaretPosition(0);
  		if (node.isRoot()) browser.commandButton.setEnabled(false);
  	}
  	// show editor data if editing
  	if (isEditing()) {
	  	if (!nameField.getText().equals(node.getName())) {
	  		nameField.setText(node.getName());
	  		nameField.setCaretPosition(0);
	  	}
	  	String base = basePathField.hasFocus()? node.record.getBasePath(): node.getBasePath();
	  	if (!basePathField.getText().equals(base)) {
	  		basePathField.setText(base);
	  		basePathField.setCaretPosition(0);
	  	}
	  	if (!htmlField.getText().equals(node.record.getHTMLPath())) {
	  		htmlField.setText(node.record.getHTMLPath());
	  		htmlField.setCaretPosition(0);
	  	}
	  	boolean isValidHTML = true;
	  	if (!"".equals(node.record.getHTMLPath())) { //$NON-NLS-1$
	  		isValidHTML = node.getHTMLURL()!=null;
	  	}
			htmlField.setForeground(isValidHTML? defaultForeground: darkRed);
			htmlField.setBackground(Color.white);
//			htmlField.setBackground(isValidHTML? Color.white: lightRed);
	  	if (!targetField.getText().equals(node.getTarget())) {
	  		targetField.setText(node.getTarget());
	  		targetField.setCaretPosition(0);
	  	}
	  	boolean isValidTarget = true;
	  	if (node.getTarget()!=null) {
	  		isValidTarget = node.getTargetURL()!=null;
	  	}
	  	targetField.setForeground(isValidTarget? defaultForeground: darkRed);
	  	targetField.setBackground(Color.white);
//	  	targetField.setBackground(isValidTarget? Color.white: lightRed);
	  	String type = node.record.getType();
	  	type = ToolsRes.getString("LibraryResource.Type."+type); //$NON-NLS-1$
	  	typeField.setText(type);
	  	boolean hasBasePath = !"".equals(node.record.getBasePath()); //$NON-NLS-1$
	  	nameField.setEnabled(true);
	  	basePathField.setEnabled(true);
	  	htmlField.setEnabled(true);
	  	typeField.setEnabled(true);
	  	targetField.setEnabled(!isCollection);
	  	nameLabel.setEnabled(true);
	  	htmlLabel.setEnabled(true);
	  	basePathLabel.setEnabled(true);
	  	targetLabel.setEnabled(!isCollection);
	  	typeLabel.setEnabled(true);
	  	openHTMLButton.setEnabled(true);
	  	openBasePathButton.setEnabled(true);
	  	openFileButton.setEnabled(!isCollection);
	  	basePathField.setForeground(hasBasePath || basePathField.hasFocus()? 
	  			defaultForeground: lightGreen);
	    nameField.setBackground(Color.white);
	    basePathField.setBackground(Color.white);
	    
	    // show metadata
    	String authors = node.getMetadataValue(LibraryResource.META_AUTHOR);
    	if (authors==null) authors = ""; //$NON-NLS-1$
	  	if (!authorField.getText().equals(authors)) {
	  		authorField.setText(authors);
	  		authorField.setCaretPosition(0);
		  	authorField.setBackground(Color.white);
	  	}
    	String contact = node.getMetadataValue(LibraryResource.META_CONTACT);
    	if (contact==null) contact = ""; //$NON-NLS-1$
	  	if (!contactField.getText().equals(contact)) {
	  		contactField.setText(contact);
	  		contactField.setCaretPosition(0);
	  		contactField.setBackground(Color.white);
	  	}
    	String keys = node.getMetadataValue(LibraryResource.META_KEYWORDS);
    	if (keys==null) keys = ""; //$NON-NLS-1$
	  	if (!keywordsField.getText().equals(keys)) {
	  		keywordsField.setText(keys);
	  		keywordsField.setCaretPosition(0);
	  		keywordsField.setBackground(Color.white);
	  	}
	  	if (node.selectedMetadata!=null) {
	  		metadataDropdown.getEditor().setItem(node.selectedMetadata);
	  	}
	  	else {
	  		metadataDropdown.getEditor().setItem(emptyMetadata);		  		
	  	}
	    
	    // set tooltips
			path = htmlField.getText();
			if (!path.equals(XML.getPathRelativeTo(path, base))) {
				htmlField.setToolTipText(ToolsRes.getString("LibraryTreePanel.Tooltip.Relative")); //$NON-NLS-1$
			}
			else if (!path.equals(XML.getResolvedPath(path, base))) {
				htmlField.setToolTipText(ToolsRes.getString("LibraryTreePanel.Tooltip.Absolute")); //$NON-NLS-1$
			}
			else htmlField.setToolTipText(null);
			path = targetField.getText();
			if (!path.equals(XML.getPathRelativeTo(path, base))) {
				targetField.setToolTipText(ToolsRes.getString("LibraryTreePanel.Tooltip.Relative")); //$NON-NLS-1$
			}
			else if (!path.equals(XML.getResolvedPath(path, base))) {
				targetField.setToolTipText(ToolsRes.getString("LibraryTreePanel.Tooltip.Absolute")); //$NON-NLS-1$
			}
			else targetField.setToolTipText(null);
  	}
  	repaint();
  }
  
  /**
   * Displays the HTMLPane for a given tree node.
   * 
   * @param node the node
   */
  protected void showHTMLPane(final LibraryTreeNode node) {  	
  	HTMLPane htmlPane = htmlPanesByNode.get(node);
  	if (htmlPane!=null && htmlPane==htmlScroller.getViewport().getView()) 
  		return;
  	new HTMLDisplayer(node).execute();
  }
  
  /**
   * Creates the GUI and listeners.
   */
  protected void createGUI() {
    // create popup menu
    popup = new JPopupMenu();
    // create actions
		addCollectionAction = new AbstractAction() {
	    public void actionPerformed(ActionEvent e) {
	    	LibraryTreeNode node = getSelectedNode();
    		htmlPanesByNode.remove(node);
	    	LibraryCollection collection = (LibraryCollection)node.record;
      	LibraryCollection newCollection = new LibraryCollection(null);
      	collection.addResource(newCollection);
      	LibraryTreeNode newNode = new LibraryTreeNode(newCollection, LibraryTreePanel.this);
      	if (insertChildAt(newNode, node, node.getChildCount())) {
		    	TreePath path = new TreePath(newNode.getPath());
		    	tree.scrollPathToVisible(path);
		      tree.setSelectionPath(path);      		
      	}
      	setChanged();
	    }
		};
		addResourceAction = new AbstractAction() {
	    public void actionPerformed(ActionEvent e) {
	    	LibraryTreeNode node = getSelectedNode();
    		htmlPanesByNode.remove(node);
	    	LibraryCollection collection = (LibraryCollection)node.record;
	    	LibraryResource record = new LibraryResource(null);
	    	collection.addResource(record);
	    	LibraryTreeNode newNode = new LibraryTreeNode(record, LibraryTreePanel.this);
      	if (insertChildAt(newNode, node, node.getChildCount())) {
		    	TreePath path = new TreePath(newNode.getPath());
		    	tree.scrollPathToVisible(path);
		      tree.setSelectionPath(path);      		
      	}
      	setChanged();
	    }
		};
		copyAction = new AbstractAction() {
		  public void actionPerformed(ActionEvent e) {
      	LibraryTreeNode node = getSelectedNode();
      	if (node!=null) {
	        XMLControl control = new XMLControlElement(node.record);
	        String target = XML.forwardSlash(node.getTarget());
	        // set base path if target not absolute and treePanel not editing
	        if(!isEditing() && !target.startsWith("/") && target.indexOf(":")==-1) { //$NON-NLS-1$ //$NON-NLS-2$
		        control.setValue("base_path", node.getBasePath()); //$NON-NLS-1$
	        }
	        StringSelection data = new StringSelection(control.toXML());
	        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	        clipboard.setContents(data, data);
	        enableButtons();
		    }
		  }		
		};
	  cutAction = new AbstractAction() {
		  public void actionPerformed(ActionEvent e) {
	    	LibraryTreeNode node = getSelectedNode();
	    	if (node!=null) {
	    		copyAction.actionPerformed(null);
	    		removeNode(node);
	        enableButtons();
	      	setChanged();
	    	}
		  }
		};
		pasteAction = new AbstractAction() {
		  public void actionPerformed(ActionEvent e) {
      	LibraryTreeNode parent = getSelectedNode();
      	if (parent==null || !(parent.record instanceof LibraryCollection))
      		return;      	
		    try {
		      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		      Transferable data = clipboard.getContents(null);
		      String dataString = (String) data.getTransferData(DataFlavor.stringFlavor);
		      if(dataString!=null) {
		        XMLControlElement control = new XMLControlElement();
		        control.readXML(dataString);
		        if (LibraryResource.class.isAssignableFrom(control.getObjectClass())) {
		      		htmlPanesByNode.remove(parent);
		        	LibraryResource record =(LibraryResource) control.loadObject(null);
		      		LibraryCollection collection = (LibraryCollection)parent.record;
	          	collection.addResource(record);
	          	LibraryTreeNode newNode = new LibraryTreeNode(record, LibraryTreePanel.this);
	          	if (insertChildAt(newNode, parent, parent.getChildCount())) {
	    		    	TreePath path = new TreePath(newNode.getPath());
	    		    	tree.scrollPathToVisible(path);
	    		      tree.setSelectionPath(path);      		
	          	}
	          	setChanged();
		        }
		      }
		    } catch(Exception ex) {}
		    
        enableButtons();
		  }		
		};
		moveUpAction = new AbstractAction() {
		  public void actionPerformed(ActionEvent e) {
      	LibraryTreeNode node = getSelectedNode();
      	if (node!=null) {
      		LibraryTreeNode parent = (LibraryTreeNode) node.getParent();
	        if(parent!=null) {
		        int i = parent.getIndex(node);
		        if(i>0) {
		      		htmlPanesByNode.remove(parent);
		        	treeModel.removeNodeFromParent(node);
		        	treeModel.insertNodeInto(node, parent, i-1);
		        	LibraryCollection collection = (LibraryCollection)parent.record;
		        	collection.removeResource(node.record);
		        	collection.insertResource(node.record, i-1);
		        	setSelectedNode(node);
		        	enableButtons();
		        	setChanged();
		        }
	        }
		    }
		  }		
		};
		moveDownAction = new AbstractAction() {
		  public void actionPerformed(ActionEvent e) {
      	LibraryTreeNode node = getSelectedNode();
      	if (node!=null) {
      		LibraryTreeNode parent = (LibraryTreeNode) node.getParent();
	        if(parent!=null) {
	          int i = parent.getIndex(node);
	          int end = parent.getChildCount();
		        if(i<end-1) {
		      		htmlPanesByNode.remove(parent);
		        	treeModel.removeNodeFromParent(node);
		        	treeModel.insertNodeInto(node, parent, i+1);
		        	LibraryCollection collection = (LibraryCollection)parent.record;
		        	collection.removeResource(node.record);
		        	collection.insertResource(node.record, i+1);
		        	setSelectedNode(node);
		        	enableButtons();
		        	setChanged();
		        }
	        }
		    }
		  }		
		};
		metadataAction = new AbstractAction() {
		  public void actionPerformed(ActionEvent e) {
		  	boolean select = !metadataButton.isSelected();
		  	metadataButton.setSelected(select);
		  	metadataButton.setText(select?
		  			ToolsRes.getString("LibraryTreePanel.Button.Metadata.Hide"): //$NON-NLS-1$
		  			ToolsRes.getString("LibraryTreePanel.Button.Metadata.Show")); //$NON-NLS-1$
		  	if (select) {
		  		editorPanel.add(authorBox);
		  		editorPanel.add(contactBox);
		  		editorPanel.add(keywordsBox);
		  		editorPanel.add(metadataBox);
		  	}
		  	else {
		  		editorPanel.remove(authorBox);
		  		editorPanel.remove(contactBox);
		  		editorPanel.remove(keywordsBox);
		  		editorPanel.remove(metadataBox);
		  	}
		  }		
		};

		convertPathMouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
      	EntryField field = (EntryField)e.getSource();
      	String path = field.getText();
      	if ("".equals(path)) return; //$NON-NLS-1$
      	final boolean isTarget = field==targetField;
      	
        final LibraryTreeNode node = getSelectedNode();
        if (node !=null && OSPRuntime.isPopupTrigger(e)) {        	
          String base = node.getBasePath();
          if ("".equals(base)) return; //$NON-NLS-1$
        	JPopupMenu popup = new JPopupMenu();
      		final String relPath = XML.getPathRelativeTo(path, base);
      		final String absPath = XML.getResolvedPath(path, base);
      		if (!path.equals(relPath)) {
            JMenuItem item = new JMenuItem(ToolsRes.getString("LibraryTreePanel.MenuItem.SetToRelative")); //$NON-NLS-1$
            popup.add(item);
            item.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
              	if (isTarget)
              		node.setTarget(relPath);
              	else node.setHTMLPath(relPath);
              }
            });
      		}
      		else if (!path.equals(absPath)) {
            JMenuItem item = new JMenuItem(ToolsRes.getString("LibraryTreePanel.MenuItem.SetToAbsolute")); //$NON-NLS-1$
            popup.add(item);
            item.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
              	if (isTarget)
              		node.setTarget(absPath);
              	else node.setHTMLPath(absPath);             	
              }
            });
      		}
        	if (popup.getComponentCount()>0)
        		popup.show(field, e.getX()+2, e.getY()+2);
        }
      }
    };

    // create tree listeners
    treeSelectionListener = new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
    		emptyMetadata.clearData();
				metadataModel.dataChanged();
	    	LibraryTreeNode node = getSelectedNode();
      	showInfo(node);
        enableButtons();
      }
    };
    treeMouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        // select node and show popup menu
        TreePath path = tree.getPathForLocation(e.getX(), e.getY());
        if(path==null) {
          return;
        }
        tree.setSelectionPath(path);
        LibraryTreeNode node = (LibraryTreeNode) tree.getLastSelectedPathComponent();
        if (OSPRuntime.isPopupTrigger(e)) {
          getPopup(node).show(tree, e.getX(), e.getY()+8);
        }
        else if (e.getClickCount()==2 && node.getTarget()!=null) {
        	firePropertyChange("target", null, node); //$NON-NLS-1$
        }
      }
    };
    // create toolbar and buttons
    addCollectionButton = new JButton();
    addCollectionButton.setOpaque(false);
    addCollectionButton.setBorder(LibraryBrowser.buttonBorder);
    addCollectionButton.addActionListener(addCollectionAction);
    addResourceButton = new JButton(addResourceAction);
    addResourceButton.setOpaque(false);
    addResourceButton.setBorder(LibraryBrowser.buttonBorder);
    copyButton = new JButton(copyAction);
    copyButton.setOpaque(false);
    copyButton.setBorder(LibraryBrowser.buttonBorder);
    cutButton = new JButton(cutAction);
    cutButton.setOpaque(false);
    cutButton.setBorder(LibraryBrowser.buttonBorder);
    pasteButton = new JButton(pasteAction);
    pasteButton.setOpaque(false);
    pasteButton.setBorder(LibraryBrowser.buttonBorder);
    moveUpButton = new JButton(moveUpAction);
    moveUpButton.setOpaque(false);
    moveUpButton.setBorder(LibraryBrowser.buttonBorder);
    moveDownButton = new JButton(moveDownAction);
    moveDownButton.setOpaque(false);
    moveDownButton.setBorder(LibraryBrowser.buttonBorder);
    metadataButton = new JButton(metadataAction);
    metadataButton.setOpaque(false);
    metadataButton.setBorder(LibraryBrowser.buttonBorder);
    
    editorbar = new JToolBar();
    editorbar.setFloatable(false);
    editorbar.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
    editorbar.add(addResourceButton);
    editorbar.add(addCollectionButton);
    editorbar.addSeparator();
    editorbar.add(copyButton);
    editorbar.add(cutButton);
    editorbar.add(pasteButton);
    editorbar.addSeparator();
    editorbar.add(moveUpButton);
    editorbar.add(moveDownButton);
    editorbar.add(Box.createHorizontalGlue());
    editorbar.add(metadataButton);
    
    // create default html pane
    emptyHTMLPane = new HTMLPane();    
    // create display panel for right side of split pane
    displayPanel = new JPanel(new BorderLayout());
    displayPanel.add(htmlScroller, BorderLayout.CENTER);
    
    // create split pane
    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroller, displayPanel);
    add(splitPane, BorderLayout.CENTER); 
    treeScroller.setPreferredSize(new Dimension(240, 400));
    
    // create editorPanel and components
    editorPanel = Box.createVerticalBox();

    nameField = new EntryField();
    entryFields.add(nameField);
    nameField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	LibraryTreeNode node = getSelectedNode();
      	if (node!=null) {
      		htmlPanesByNode.remove(node);
      		LibraryTreeNode parent = (LibraryTreeNode)node.getParent();
      		if (parent!=null) htmlPanesByNode.remove(parent);
      		node.setName(nameField.getText());
      		if (node.isRoot()) {
      			browser.refreshTabTitle(pathToRoot, rootResource);
      		}
      	}
      }
    });
    
    // create typeField (really a label that looks like a field)
    typeField = new JLabel(" ") {  //$NON-NLS-1$
    	public Dimension getPreferredSize() {
    		Dimension dim = nameField.getPreferredSize();
    		dim.width = typeFieldWidth;
    		return dim;
    	}
    };
    typeField.setBorder(nameField.getBorder());
    typeField.setBackground(nameField.getBackground());
    typeField.setFont(nameField.getFont());
    typeField.setOpaque(true);
    typeField.addMouseListener(new MouseAdapter() {
    	public void mousePressed(MouseEvent e) {
      	final LibraryTreeNode node = getSelectedNode();
      	if (node!=null && !(node.record instanceof LibraryCollection)) {
        	JPopupMenu popup = new JPopupMenu();
        	ActionListener typeListener = new ActionListener() {
      		  public void actionPerformed(ActionEvent e) {
      		  	String type = e.getActionCommand();
      		  	if (!type.equals(node.record.getType())) {
            		htmlPanesByNode.remove(node);
            		LibraryTreeNode parent = (LibraryTreeNode)node.getParent();
            		if (parent!=null) htmlPanesByNode.remove(parent);
      		  		node.setType(type);
      		  		type = ToolsRes.getString("LibraryResource.Type."+node.record.getType()); //$NON-NLS-1$
      		  		typeField.setText(type);
    	        	setChanged();
    	        	showInfo(node);
      		  	}
            }
          };
        	for (String next: LibraryResource.RESOURCE_TYPES) {
            JMenuItem item = new JMenuItem(ToolsRes.getString("LibraryResource.Type."+next)) { //$NON-NLS-1$
            	public Dimension getPreferredSize() {
            		Dimension dim = typeField.getPreferredSize();
            		dim.width -= 2;
            		return dim;
            	}
            };
            popup.add(item);
            item.addActionListener(typeListener);
            item.setActionCommand(next);
        	}
          popup.show(typeField, 0, typeField.getHeight());      		
      	}
    	}
    });

    htmlField = new EntryField();
    entryFields.add(htmlField);
    htmlField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	LibraryTreeNode node = getSelectedNode();
      	if (node!=null) { 
      		node.setHTMLPath(htmlField.getText());
      	}
      }
    });
    htmlField.addMouseListener(convertPathMouseListener);
    
    openHTMLButton = new JButton(openFileIcon);
    openHTMLButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 2));
    openHTMLButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	LibraryTreeNode node = getSelectedNode();
      	if (node!=null) { 
          int result = JFileChooser.CANCEL_OPTION;
          JFileChooser chooser = getFileChooser();
          chooser.setDialogTitle(null);
          chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          chooser.setAcceptAllFileFilterUsed(true);
          chooser.addChoosableFileFilter(htmlFilter);
      	  result = chooser.showOpenDialog(LibraryTreePanel.this);
          File file = chooser.getSelectedFile();
      	  chooser.removeChoosableFileFilter(htmlFilter); 
          chooser.setSelectedFile(new File(""));  //$NON-NLS-1$
          if(result==JFileChooser.APPROVE_OPTION) {
            browser.library.chooserDir = chooser.getCurrentDirectory().toString();
        		if (file!=null) {
        			String path = XML.forwardSlash(file.getAbsolutePath());
              String base = node.getBasePath();
              if (!"".equals(base)) { //$NON-NLS-1$
              	path = XML.getPathRelativeTo(path, base);
              }
        			node.setHTMLPath(path);
        		}
          }
      	}
      }
    });
    
    basePathField = new EntryField();
    entryFields.add(basePathField);
    basePathField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	// special handling to prevent setting base path if same as inherited
      	LibraryTreeNode node = getSelectedNode();
      	if (basePathField.getBackground()!=Color.yellow) {
      		return;
      	}
      	if (node!=null) {
      		if (!basePathField.getText().equals(node.record.getBasePath())) {
        		htmlPanesByNode.remove(node);
        		LibraryTreeNode parent = (LibraryTreeNode)node.getParent();
        		if (parent!=null) htmlPanesByNode.remove(parent);
        		node.setBasePath(basePathField.getText());
          	setChanged();
        	}
      	}
      }
    });
    basePathField.addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) {
      	LibraryTreeNode node = getSelectedNode();
      	if ("".equals(node.record.getBasePath())) { //$NON-NLS-1$
      		basePathField.setText(null);
        	basePathField.setForeground(htmlField.getForeground());
        	basePathField.setBackground(Color.white);
      	}
      }
    });
    openBasePathButton = new JButton(openFileIcon);
    openBasePathButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 2));
    openBasePathButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	LibraryTreeNode node = getSelectedNode();
      	if (node!=null) { 
          int result = JFileChooser.CANCEL_OPTION;
          JFileChooser chooser = getFileChooser();
          chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          chooser.setAcceptAllFileFilterUsed(false);
          chooser.addChoosableFileFilter(folderFilter);
          chooser.setDialogTitle(ToolsRes.getString("LibraryTreePanel.FileChooser.Title.Base")); //$NON-NLS-1$
      	  result = chooser.showDialog(LibraryTreePanel.this, 
      	  		ToolsRes.getString("LibraryTreePanel.FileChooser.Button.Select")); //$NON-NLS-1$
          File file = chooser.getSelectedFile();
      	  chooser.removeChoosableFileFilter(folderFilter); 
          chooser.setSelectedFile(new File(""));  //$NON-NLS-1$
          if(result==JFileChooser.APPROVE_OPTION) {
            browser.library.chooserDir = chooser.getCurrentDirectory().toString();
        		if (file!=null) {
          		htmlPanesByNode.remove(node);
          		LibraryTreeNode parent = (LibraryTreeNode)node.getParent();
          		if (parent!=null) htmlPanesByNode.remove(parent);
        			node.setBasePath(XML.forwardSlash(file.getAbsolutePath()));
        			setChanged();
        			showInfo(node);
        		}
          }
      	}
      }
    });

    targetField = new EntryField();
    entryFields.add(targetField);
    targetField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	LibraryTreeNode node = getSelectedNode();
      	if (node!=null && !(node.record instanceof LibraryCollection)) {
      		node.setTarget(targetField.getText());
      	}
      }
    });
    targetField.addMouseListener(convertPathMouseListener);
    
    openFileButton = new JButton(openFileIcon);
    openFileButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 2));
    openFileButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	LibraryTreeNode node = getSelectedNode();
      	if (node!=null) {
          int result = JFileChooser.CANCEL_OPTION;
          JFileChooser chooser = getFileChooser();
          chooser.setDialogTitle(null);
          chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          chooser.setAcceptAllFileFilterUsed(true);
      	  result = chooser.showOpenDialog(LibraryTreePanel.this);
          File file = chooser.getSelectedFile();
          chooser.setSelectedFile(new File(""));  //$NON-NLS-1$
          if(result==JFileChooser.APPROVE_OPTION) {
            browser.library.chooserDir = chooser.getCurrentDirectory().toString();
        		if (file!=null) {
        			String path = XML.forwardSlash(file.getAbsolutePath());
              String base = node.getBasePath();
              if (!"".equals(base)) { //$NON-NLS-1$
              	path = XML.getPathRelativeTo(path, base);
              }
          		node.setTarget(path);
        		}
          }
      	}
      }
    });

    nameLabel = new JLabel();
    nameLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    nameLabel.setHorizontalAlignment(SwingConstants.TRAILING);
    typeLabel = new JLabel();
    typeLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 2));
    typeLabel.setHorizontalAlignment(SwingConstants.TRAILING);
    htmlLabel = new JLabel();
    htmlLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    htmlLabel.setHorizontalAlignment(SwingConstants.TRAILING);
    basePathLabel = new JLabel();
    basePathLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    basePathLabel.setHorizontalAlignment(SwingConstants.TRAILING);
    targetLabel = new JLabel();
    targetLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    targetLabel.setHorizontalAlignment(SwingConstants.TRAILING);
    labels.add(nameLabel);
    labels.add(htmlLabel);
    labels.add(basePathLabel);
    labels.add(targetLabel);
    
    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 2));
    box.add(nameLabel);
    box.add(nameField);    
    box.add(typeLabel);    
    box.add(typeField);    
    editorPanel.add(box);
    
    box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 2));
    box.add(htmlLabel);
    box.add(htmlField);    
    box.add(openHTMLButton);    
    editorPanel.add(box);

    box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 2));
    box.add(basePathLabel);
    box.add(basePathField);    
    box.add(openBasePathButton);    
    editorPanel.add(box);

    fileBox = Box.createHorizontalBox();
    fileBox.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 1));
    fileBox.add(targetLabel);
    fileBox.add(targetField);    
    fileBox.add(openFileButton);    
    editorPanel.add(fileBox);
    
    // metadata
    metadataFieldListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	LibraryTreeNode node = getSelectedNode();
      	if (node!=null) {
      		EntryField field = (EntryField)e.getSource();
      		String s = field.getText().trim();
      		
      		// get target Metadata, key and index
    			Metadata metadata = node.selectedMetadata;
    			String key = null;
    			int index = 1;
    			
    			if (field==authorField) {
    				key = LibraryResource.META_AUTHOR;
    				metadata = node.record.getMetadata(key);
    			}
    			else if (field==contactField) {
    				key = LibraryResource.META_CONTACT;
    				metadata = node.record.getMetadata(key);
    			}
    			else if (field==keywordsField) {
    				key = LibraryResource.META_KEYWORDS;
    				metadata = node.record.getMetadata(key);
    			}
    			else if (field==keyEditField) index = 0;
    			    			
      		// if no change, do nothing and return
    			if (metadata!=null && s.equals(metadata.getData()[index])) return;
      		
    			if (s.length()>0 && (metadata==null || metadata==emptyMetadata)) {
    				// add new metadata
    				metadata = index==0? new Metadata(s, null): new Metadata(key, s);
    				node.record.addMetadata(metadata);
    				metadataModel.dataAdded();
    				if (field==keyEditField || field==valueEditField)
    					node.selectedMetadata = metadata;
    			}
    			else if (metadata!=null) {
    				if ("".equals(s)) { //$NON-NLS-1$
    					// remove metadata
      				node.record.removeMetadata(metadata);
      				metadataModel.dataRemoved();      				
	    				if (node.selectedMetadata==metadata)
	    					node.selectedMetadata = null;
    				}
    				else {
	    				// modify existing metadata
	    				String[] data = metadata.getData();
	    				data[index] = s;
    					metadataModel.dataChanged();
    				}
    			}
    			setChanged();
    			node.tooltip = null; // triggers new tooltip
    			showInfo(node);
      	}
      }
    };
    
    authorField = new EntryField();
    entryFields.add(authorField);
    authorField.addActionListener(metadataFieldListener);
    contactField = new EntryField();
    entryFields.add(contactField);
    contactField.addActionListener(metadataFieldListener);
    keywordsField = new EntryField();
    entryFields.add(keywordsField);
    keywordsField.addActionListener(metadataFieldListener);
    metadataModel = new MetadataComboBoxModel();
    metadataDropdown = new JComboBox(metadataModel);
    metadataDropdown.setRenderer(new MetadataComboBoxRenderer());
    metadataDropdown.setEditor(new MetadataComboBoxEditor());
    metadataDropdown.setEditable(true);
    
    authorLabel = new JLabel();
    authorLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    authorLabel.setHorizontalAlignment(SwingConstants.TRAILING);
    contactLabel = new JLabel();
    contactLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    contactLabel.setHorizontalAlignment(SwingConstants.TRAILING);
    keywordsLabel = new JLabel();
    keywordsLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    keywordsLabel.setHorizontalAlignment(SwingConstants.TRAILING);
    metadataLabel = new JLabel();
    metadataLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    metadataLabel.setHorizontalAlignment(SwingConstants.TRAILING);
    labels.add(authorLabel);
    labels.add(contactLabel);
    labels.add(keywordsLabel);
    labels.add(metadataLabel);

    authorBox = Box.createHorizontalBox();
    authorBox.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 1));
    authorBox.add(authorLabel);
    authorBox.add(authorField);    
    contactBox = Box.createHorizontalBox();
    contactBox.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 1));
    contactBox.add(contactLabel);
    contactBox.add(contactField);    
    keywordsBox = Box.createHorizontalBox();
    keywordsBox.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 1));
    keywordsBox.add(keywordsLabel);
    keywordsBox.add(keywordsField);    
    metadataBox = Box.createHorizontalBox();
    metadataBox.add(metadataLabel);
    metadataBox.add(metadataDropdown);
  }
  
  /**
   * Refreshes the GUI including locale-dependent resource strings.
   */
  protected void refreshGUI() {
  	// set button and label text
  	addCollectionButton.setText(ToolsRes.getString("LibraryTreePanel.Button.AddCollection")); //$NON-NLS-1$
  	addResourceButton.setText(ToolsRes.getString("LibraryTreePanel.Button.AddResource")); //$NON-NLS-1$
  	copyButton.setText(ToolsRes.getString("LibraryTreePanel.Button.Copy")); //$NON-NLS-1$
  	cutButton.setText(ToolsRes.getString("LibraryTreePanel.Button.Cut")); //$NON-NLS-1$
  	pasteButton.setText(ToolsRes.getString("LibraryTreePanel.Button.Paste")); //$NON-NLS-1$
  	moveUpButton.setText(ToolsRes.getString("LibraryTreePanel.Button.Up")); //$NON-NLS-1$
  	moveDownButton.setText(ToolsRes.getString("LibraryTreePanel.Button.Down")); //$NON-NLS-1$
  	metadataButton.setText(metadataButton.isSelected()?
  			ToolsRes.getString("LibraryTreePanel.Button.Metadata.Hide"): //$NON-NLS-1$
  			ToolsRes.getString("LibraryTreePanel.Button.Metadata.Show")); //$NON-NLS-1$
  	nameLabel.setText(ToolsRes.getString("LibraryTreePanel.Label.Name")); //$NON-NLS-1$
  	typeLabel.setText(ToolsRes.getString("LibraryTreePanel.Label.Type")); //$NON-NLS-1$
  	htmlLabel.setText(ToolsRes.getString("LibraryTreePanel.Label.HTML")); //$NON-NLS-1$
  	basePathLabel.setText(ToolsRes.getString("LibraryTreePanel.Label.BasePath")); //$NON-NLS-1$
  	targetLabel.setText(ToolsRes.getString("LibraryTreePanel.Label.TargetFile")); //$NON-NLS-1$
  	authorLabel.setText(ToolsRes.getString("LibraryTreePanel.Label.Author")); //$NON-NLS-1$
  	contactLabel.setText(ToolsRes.getString("LibraryTreePanel.Label.Contact")); //$NON-NLS-1$
  	keywordsLabel.setText(ToolsRes.getString("LibraryTreePanel.Label.Keywords")); //$NON-NLS-1$
  	metadataLabel.setText(ToolsRes.getString("LibraryTreePanel.Label.Metadata")); //$NON-NLS-1$
    browser.refreshButton.setEnabled(getSelectedNode()!=null);
    if (getSelectedNode()==rootNode) {
    	browser.refreshButton.setToolTipText(ToolsRes.getString("LibraryBrowser.Tooltip.Reload")); //$NON-NLS-1$
    }
    else browser.refreshButton.setToolTipText(ToolsRes.getString("LibraryBrowser.Tooltip.Refresh")); //$NON-NLS-1$

		// adjust size of labels so they right-align
    int w = 0, h = 0;
    Font font = nameLabel.getFont();
    FontRenderContext frc = new FontRenderContext(null, false, false); 
    for(JLabel next: labels) {
      Rectangle2D rect = font.getStringBounds(next.getText()+" ", frc); //$NON-NLS-1$
      w = Math.max(w, (int) rect.getWidth()+4);
      h = Math.max(h, (int) rect.getHeight()+4);
    }
    h = Math.max(h, 20);
    Dimension labelSize = new Dimension(w, h);
    for(JLabel next: labels) {
      next.setPreferredSize(labelSize);
      next.setMinimumSize(labelSize);
    }
    // determine required size of type label
    typeFieldWidth = 0;
    for (String next: LibraryResource.RESOURCE_TYPES) {
    	next = ToolsRes.getString("LibraryResource.Type."+next); //$NON-NLS-1$
      Rectangle2D rect = font.getStringBounds(next+" ", frc); //$NON-NLS-1$
      typeFieldWidth = Math.max(typeFieldWidth, (int) rect.getWidth()+24);    	
    }
  }

  /**
   * Enables/disables buttons based on selected node and clipboard state.
   */
  protected void enableButtons() {
  	LibraryTreeNode node = getSelectedNode();
  	boolean nodeIsCollection = node!=null && node.record instanceof LibraryCollection;
  	addCollectionButton.setEnabled(nodeIsCollection);
  	addResourceButton.setEnabled(nodeIsCollection);
  	copyButton.setEnabled(node!=null);
  	cutButton.setEnabled(node!=null && node!=rootNode);
  	pasteButton.setEnabled(nodeIsCollection && isClipboardPastable());
		boolean canMoveUp = false, canMoveDown = false;
		if (node!=null && node.getParent()!=null) {
			LibraryTreeNode parent = (LibraryTreeNode) node.getParent();
      int i = parent.getIndex(node);
      canMoveUp = i>0;
      canMoveDown = i<parent.getChildCount()-1;
    }
  	moveUpButton.setEnabled(canMoveUp);
  	moveDownButton.setEnabled(canMoveDown);
  }
  
  /**
   * Discards collection edits and reverts to the previous state.
   */
  protected void revert() {
  	if (revertControl!=null) {
  		// copy revertControl to ensure new library resources
  		revertControl = new XMLControlElement(revertControl);
  		LibraryResource record = (LibraryResource)revertControl.loadObject(null);
  		isChanged = false;
  		setRootResource(record, pathToRoot, rootNode.isEditable(), isXMLPath);
			browser.refreshTabTitle(pathToRoot, rootResource);
  	}
  }

  /**
   * Creates the tree.
   * 
   * @param root the root node
   */
  protected void createTree(LibraryTreeNode root) {
    treeModel = new DefaultTreeModel(root);
    tree = new JTree(treeModel) {
    	public JToolTip createToolTip() {
    		return new JMultiLineToolTip();
    	}
    };
    if (root.createChildNodes()) {
	    LibraryTreeNode lastNode = (LibraryTreeNode)root.getLastChild();
	  	TreePath path = new TreePath(lastNode.getPath());
	  	tree.scrollPathToVisible(path);
    }
    treeNodeRenderer = new LibraryTreeNodeRenderer();
    tree.setCellRenderer(treeNodeRenderer);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    ToolTipManager.sharedInstance().registerComponent(tree);
    // listen for tree selections and display the contents
    tree.addTreeSelectionListener(treeSelectionListener);
    // listen for mouse events to display node info and inform propertyChangeListeners
    tree.addMouseListener(treeMouseListener);
    // put tree in scroller
    treeScroller.setViewportView(tree);
  }
  
  /**
   * Determines if the clipboard can be pasted.
   * 
   * @return true if the clipboard contains a LibraryTreeNode XMLControl string
   */
  protected boolean isClipboardPastable() {
  	pasteControl = null;
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable data = clipboard.getContents(null);
    String dataString = null;
		try {
			dataString = (String)data.getTransferData(DataFlavor.stringFlavor);
		} catch (Exception e) {} 
		if(dataString!=null) {
      XMLControlElement control = new XMLControlElement();
      control.readXML(dataString);
      if (LibraryResource.class.isAssignableFrom(control.getObjectClass())) {
      	pasteControl = control;
      	return true;
      }
    }
    return false;
  }
  
  /**
   * Returns a popup menu with items appropriate for a given tree node.
   * 
   * @param node the node
   * @return the popup menu
   */
  protected JPopupMenu getPopup(final LibraryTreeNode node) {
  	popup.removeAll(); 
  	if (!isEditing()) {
   		JMenuItem item = new JMenuItem(ToolsRes.getString("LibraryTreePanel.Button.Copy")); //$NON-NLS-1$
      popup.add(item);
      item.addActionListener(copyAction);
	  	if ("".equals(pathToRoot) && node.record.getCollectionPath()!=null) { //$NON-NLS-1$ // this is a search result tab
	   		item = new JMenuItem(ToolsRes.getString("LibraryTreePanel.Popup.Item.OpenCollection")); //$NON-NLS-1$
	   		popup.addSeparator();
	      popup.add(item);
	      item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
          	String path = node.record.getCollectionPath();
          	browser.loadTab(path, node.record.treePath);
          }
        });
	  	}
	    FontSizer.setFonts(popup, FontSizer.getLevel());
      return popup;
  	}
  	boolean isCollection = node.record instanceof LibraryCollection;
		boolean canMoveUp = false, canMoveDown = false;
		if (node.getParent()!=null) {
			LibraryTreeNode parent = (LibraryTreeNode) node.getParent();
      int i = parent.getIndex(node);
      canMoveUp = i>0;
      canMoveDown = i<parent.getChildCount()-1;
    }
		if (isCollection) {
  		// add resource
      JMenuItem item = new JMenuItem(ToolsRes.getString("LibraryTreePanel.Button.AddResource")); //$NON-NLS-1$
      popup.add(item);
      item.addActionListener(addResourceAction);
  		// add collection
      item = new JMenuItem(ToolsRes.getString("LibraryTreePanel.Button.AddCollection")); //$NON-NLS-1$
      popup.add(item);
      item.addActionListener(addCollectionAction);
      popup.addSeparator();			
		}
 		JMenuItem item = new JMenuItem(ToolsRes.getString("LibraryTreePanel.Button.Copy")); //$NON-NLS-1$
    popup.add(item);
    item.addActionListener(copyAction);
 		item = new JMenuItem(ToolsRes.getString("LibraryTreePanel.Button.Cut")); //$NON-NLS-1$
    popup.add(item);
    item.addActionListener(cutAction);
		if (isCollection && isClipboardPastable()) {
   		item = new JMenuItem(ToolsRes.getString("LibraryTreePanel.Button.Paste")); //$NON-NLS-1$
      popup.add(item);
      item.addActionListener(pasteAction);  		
		}
    if (canMoveUp || canMoveDown) {
      popup.addSeparator();
      if (canMoveUp) {
	   		item = new JMenuItem(ToolsRes.getString("LibraryTreePanel.Button.Up")); //$NON-NLS-1$
	      popup.add(item);
	      item.addActionListener(moveUpAction);
      }
      if (canMoveDown) {
	   		item = new JMenuItem(ToolsRes.getString("LibraryTreePanel.Button.Down")); //$NON-NLS-1$
	      popup.add(item);
	      item.addActionListener(moveDownAction);
      }
    }
    FontSizer.setFonts(popup, FontSizer.getLevel());
    return popup;
  }

  
  /**
   * Inserts a child into a parent node at a specified index.
   *
   * @param child the child node
   * @param parent the parent node
   * @param index the index
   * @return true if added
   */
  protected boolean insertChildAt(final LibraryTreeNode child, LibraryTreeNode parent, int index) {
  	if (tree==null || parent.getChildCount()<index) return false;
  	DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
  	model.insertNodeInto(child, parent, index);  	
    return true;
  }

  /**
   * Removes a given tree node.
   *
   * @param node the node
   */
  protected void removeNode(LibraryTreeNode node) {
  	if (rootNode==null || node==rootNode) return;
  	LibraryTreeNode parent = (LibraryTreeNode)node.getParent();
		htmlPanesByNode.remove(parent);
		htmlPanesByNode.remove(node);
  	LibraryCollection collection = (LibraryCollection)parent.record;
  	collection.removeResource(node.record);
  	DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
		model.removeNodeFromParent(node);
  	TreePath path = new TreePath(parent.getPath());
  	tree.scrollPathToVisible(path);
    tree.setSelectionPath(path);
  }

  /**
   * Called whenever a resource changes due to a user edit.
   */
  protected void setChanged() {
  	if (ignoreChanges) return;
  	isChanged = true;
		firePropertyChange("collection_edit", null, null); //$NON-NLS-1$
  }
  
  /**
   * Determines if the resource has been changed since the last save.
   * @return true if changed
   */
  protected boolean isChanged() {
  	return isEditable() && isChanged;
  }
  
  /**
   * Saves the current resource.
   * @return the path to the saved file, or null if not saved
   */
  protected String save() {
  	if (!isXMLPath) {
  		return browser.saveAs();
  	}
  	else if (isEditable()) {
			XMLControl control = new XMLControlElement(rootResource);
			control.write(pathToRoot);
			isChanged = false;
		}
		// save copy in OSP search folder
		File cacheFile = ResourceLoader.getSearchCacheFile(pathToRoot);
		XMLControl control = new XMLControlElement(rootNode.record);
		control.setValue("real_path", pathToRoot); //$NON-NLS-1$
		control.write(cacheFile.getAbsolutePath());
		
		return pathToRoot;
  }

  /**
   * Gives the user an opportunity to save changes.
   * @param name the name of the collection
   * @return <code>false</code> if the user cancels, otherwise <code>true</code>
   */
  protected boolean saveChanges(String name) {
    if (!isChanged()) return true;
    if (org.opensourcephysics.display.OSPRuntime.applet!=null) return true;
    int i = JOptionPane.showConfirmDialog(this,
         ToolsRes.getString("LibraryBrowser.Dialog.SaveChanges.Message") + " \"" + name + "\"?", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
         ToolsRes.getString("LibraryBrowser.Dialog.SaveChanges.Title"), //$NON-NLS-1$
         JOptionPane.YES_NO_CANCEL_OPTION,
         JOptionPane.QUESTION_MESSAGE);
    if (i==JOptionPane.CLOSED_OPTION || i==JOptionPane.CANCEL_OPTION) {
      return false;
    }
    if (i == JOptionPane.YES_OPTION) {
      if (save()==null) return false;
    }
    else revert();
    return true;
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
   * Returns the nodes that are contained in the keysets of both of two input maps.
   * @param results1 
   * @param results2
   * @return map of nodes found in both keysets
   */
  protected Map<LibraryTreeNode, List<String[]>> applyAND(Map<LibraryTreeNode, List<String[]>> results1,
  		Map<LibraryTreeNode, List<String[]>> results2) {
  	Map<LibraryTreeNode, List<String[]>> resultsAND = new HashMap<LibraryTreeNode, List<String[]>>();
  	Set<LibraryTreeNode> keys1 = results1.keySet();
  	for (LibraryTreeNode node: results2.keySet()) {
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
   * Returns the nodes that are contained in the keysets of either of two input maps.
   * @param results1 
   * @param results2
   * @return map of nodes found in either keyset
   */
  protected Map<LibraryTreeNode, List<String[]>> applyOR(Map<LibraryTreeNode, List<String[]>> results1,
  		Map<LibraryTreeNode, List<String[]>> results2) {
  	Map<LibraryTreeNode, List<String[]>> resultsOR = new HashMap<LibraryTreeNode, List<String[]>>();
  	// add nodes in results1
  	for (LibraryTreeNode node: results1.keySet()) {
  		List<String[]> matchedTerms = new ArrayList<String[]>();
  		matchedTerms.addAll(results1.get(node));
  		resultsOR.put(node, matchedTerms);
  	}
  	// add nodes in results2
  	for (LibraryTreeNode node: results2.keySet()) {
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
   * Gets the <body> code from an HTML page.
   * 
   * @param path the HTML path
   * @return the body of the HTML
   */
  protected String getHTMLBody(String path) {
		String code = ResourceLoader.getString(path);
		if (code!=null) {
			String[] parts = code.split("<body>"); //$NON-NLS-1$
			parts = parts[1].split("</body>"); //$NON-NLS-1$
			return parts[0];
		}
  	return null;
  }
  
  /**
   * Used to refresh the entry fields when the browser closes.
   */
  protected void refreshEntryFields() {
  	for (EntryField next: entryFields) {
  		if (next.getBackground()==Color.yellow)
  			next.processEntry();
  	}
  }
  
  /**
   * A JTextPane that displays html pages for LibraryTreeNodes.
   */
  protected static class HTMLPane extends JEditorPane {
  	
  	public HTMLPane() {
      setEditable(false);
      setFocusable(false);
      setContentType("text/html; charset=UTF-8"); //$NON-NLS-1$
      addHyperlinkListener(hyperlinkListener);  
	    HTMLDocument document = (HTMLDocument)getDocument();
	    document.getStyleSheet().addRule(LibraryResource.getBodyStyle());  	
	    document.getStyleSheet().addRule(LibraryResource.getH1Style());  	
	    document.getStyleSheet().addRule(LibraryResource.getH2Style());  	
  	}
  	
    public void paintComponent(Graphics g) {
      if(OSPRuntime.antiAliasText) {
        Graphics2D g2 = (Graphics2D) g;
        RenderingHints rh = g2.getRenderingHints();
        rh.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        rh.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      }
      super.paintComponent(g);
    }

  }
  
  /**
   * A renderer for Metadata objects.
   */
  protected class MetadataComboBoxRenderer extends Box implements ListCellRenderer {
    JTextField keyField, valueField;
    JLabel spacer;
    
    public MetadataComboBoxRenderer() {
    	super(BoxLayout.X_AXIS);
    	keyField = new JTextField() {
    		public Dimension getMaximumSize() {	return getPreferredSize(); }
    		public Dimension getPreferredSize() { return keyEditField.getPreferredSize(); }
    	};
    	keyField.setHorizontalAlignment(SwingConstants.RIGHT);
    	keyField.setFont(keyField.getFont().deriveFont(Font.BOLD));
    	
    	valueField = new JTextField() {
    		public Dimension getMaximumSize() {	return valueEditField.getMaximumSize(); }    		
    		public Dimension getPreferredSize() {	return getMinimumSize(); }    		
    	};
    	
    	Border border = BorderFactory.createCompoundBorder(keyField.getBorder(), BorderFactory.createEmptyBorder(0, 1, 0, 1));
    	keyField.setBorder(border);
    	valueField.setBorder(border);
    	
    	spacer = new JLabel();
    	spacer.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
    	add(keyField);
    	add(spacer);
    	add(valueField);
    }
    
    public Component getListCellRendererComponent(JList list, Object value, int index,
        boolean isSelected, boolean cellHasFocus) {
    	boolean empty = false;
      if (value!=null && value instanceof Metadata) {
      	Metadata metadata = (Metadata)value;
      	empty = metadata==emptyMetadata;
        if (empty) {
        	keyField.setText(ToolsRes.getString("LibraryTreePanel.Metadata.Name")); //$NON-NLS-1$
  	      valueField.setText(ToolsRes.getString("LibraryTreePanel.Metadata.Value")); //$NON-NLS-1$
        	keyField.setFont(keyEditField.getEmptyFont());
  	  		valueField.setFont(valueEditField.getEmptyFont());
        }
        else {
        	keyField.setText(metadata.getData()[0]);
  	      valueField.setText(metadata.getData()[1]);
        	keyField.setFont(keyEditField.getDefaultFont());
  	  		valueField.setFont(valueEditField.getDefaultFont());
        }
      }
    	if (isSelected) {
    		keyField.setBackground(list.getSelectionBackground());
    		keyField.setForeground(empty? Color.gray: list.getSelectionForeground());
    		valueField.setBackground(list.getSelectionBackground());
    		valueField.setForeground(empty? Color.gray: list.getSelectionForeground());
    	} else {
    		keyField.setBackground(list.getBackground());
    		keyField.setForeground(empty? Color.gray: list.getForeground());
    		valueField.setBackground(list.getBackground());
    		valueField.setForeground(empty? Color.gray: list.getForeground());
    	}
    	return this;
    }
  }
  
  /**
   * A ComboBoxModel for metadata.
   */
  protected class MetadataComboBoxModel extends AbstractListModel implements ComboBoxModel {
	  public int getSize() {
	  	LibraryTreeNode node = getSelectedNode();
	  	if (node==null) return 0;
	  	Set<Metadata> data = node.record.getMetadata();
	  	if (data==null) return 1;
	  	return data.size()+1; // extra one for empty metadata
	  }
	  
	  public Object getElementAt(int index) {
	  	LibraryTreeNode node = getSelectedNode();
	  	if (node==null) return 0;
	  	Set<Metadata> data = node.record.getMetadata();
	  	if (data==null) return emptyMetadata;
	  	int i = 0;
	  	for (Metadata next: data) {
	  		if (index==i) return next;
	  		i++;
	  	}
	  	return emptyMetadata;
	  }
	  
	  public void setSelectedItem(Object obj) {
	  	if (obj==null || !(obj instanceof Metadata)) return;
	  	LibraryTreeNode node = getSelectedNode();
	  	if (node==null) return;
	  	Metadata metadata = (Metadata)obj;
	  	node.selectedMetadata = metadata;
	  }
	  
	  public Object getSelectedItem() {
	  	LibraryTreeNode node = getSelectedNode();
	  	if (node==null) return null;
	  	Metadata metadata = node.selectedMetadata;
	  	return metadata;
	  }
	  
	  void dataChanged() {
	  	if (getSize()>0) fireContentsChanged(this, 0, getSize()-1);
	  } 	  
	  
	  void dataAdded() {
	  	if (getSize()>0) fireIntervalAdded(this, 0, getSize()-1);
	  } 	  
	  
	  void dataRemoved() {
	  	if (getSize()>0) fireIntervalRemoved(this, 0, getSize()-1);
	  } 	  
  }
  
  /**
   * An editor for Metadata objects.
   */
  protected class MetadataComboBoxEditor extends Box implements ComboBoxEditor {
    JLabel spacer;
    Metadata metadata;
    
    MetadataComboBoxEditor() {
    	super(BoxLayout.X_AXIS);
    	keyEditField = new MetadataEditField(keyFieldWidth) {
    		protected String getDefaultText() {
    			return metadata==emptyMetadata? ToolsRes.getString("LibraryTreePanel.Metadata.Name"): null; //$NON-NLS-1$
    		}
      	protected Font getEmptyFont() {
      		return font.deriveFont(Font.BOLD+Font.ITALIC);
      	}     	
      	protected Font getDefaultFont() {
      		return font.deriveFont(Font.BOLD);
      	}      	
    	};
      entryFields.add(keyEditField);

    	keyEditField.setHorizontalAlignment(SwingConstants.RIGHT);
    	keyEditField.setFont(keyEditField.getDefaultFont());
    	
    	valueEditField = new MetadataEditField(0) {
    		protected String getDefaultText() {
    			return metadata==emptyMetadata? ToolsRes.getString("LibraryTreePanel.Metadata.Value"): null; //$NON-NLS-1$
    		}
      	protected Font getEmptyFont() {
      		return font.deriveFont(Font.ITALIC);
      	}     	
      	protected Font getDefaultFont() {
      		return font.deriveFont(Font.PLAIN);
      	}      	
    	};
      entryFields.add(valueEditField);
    	
    	Border border = BorderFactory.createCompoundBorder(keyEditField.getBorder(), BorderFactory.createEmptyBorder(0, 1, 0, 1));
    	keyEditField.setBorder(border);
    	valueEditField.setBorder(border);

    	spacer = new JLabel();
    	spacer.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
    	add(keyEditField);
    	add(spacer);
    	add(valueEditField);
    }
    
    public Component getEditorComponent() { 
    	return this; 
    }
    
    public void setItem(Object obj) {
      if (obj==null) return;
      metadata = (Metadata)obj;
      boolean empty = metadata==emptyMetadata;
      
      if (empty) {
	      keyEditField.setText(ToolsRes.getString("LibraryTreePanel.Metadata.Name")); //$NON-NLS-1$
	      valueEditField.setText(ToolsRes.getString("LibraryTreePanel.Metadata.Value")); //$NON-NLS-1$
	  		keyEditField.setForeground(Color.gray);
	      valueEditField.setForeground(Color.gray);
	  		keyEditField.setFont(keyEditField.getEmptyFont());
	  		valueEditField.setFont(valueEditField.getEmptyFont());
      }
      else {
	      keyEditField.setText(metadata.getData()[0]);
	      keyEditField.setCaretPosition(0);
	      valueEditField.setText(metadata.getData()[1]);
	      valueEditField.setCaretPosition(0);
	  		keyEditField.setForeground(defaultForeground);
	    	valueEditField.setForeground(defaultForeground);
	  		keyEditField.setFont(keyEditField.getDefaultFont());
	  		valueEditField.setFont(valueEditField.getDefaultFont());
      }
  		keyEditField.setBackground(Color.white);
  		valueEditField.setBackground(Color.white);
    }
    
    
    public Object getItem() { 
    	return metadata;
    }
    
    public void selectAll() {}    
    public void addActionListener(ActionListener l) {}    
    public void removeActionListener(ActionListener l) {}    
  }
  
  /**
   * A SwingWorker class to load metadata and set up the search map for this tree panel.
   */
  class MetadataLoader extends SwingWorker<Void, Void> {
  	
  	boolean saveToCache;
  	boolean canceled = false;
  	List<String> treePath;
  	
  	MetadataLoader(boolean saveToCache, List<String> treePath) {
  		this.saveToCache = saveToCache;
  		this.treePath = treePath;
  	}
  	
  	void cancel() {
  		canceled = true;
  	}
  	  	
    @Override
    public Void doInBackground() {
    	
    	if (saveToCache) {
    		final ArrayList<NodeLoader> nodeLoaders = new ArrayList<NodeLoader>();
    		
    		// make property change listener to daisy-chain loading of the nodes
    		PropertyChangeListener listener = new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent e) {
						NodeLoader nodeLoader = (NodeLoader) e.getSource();
						if (nodeLoader.isDone()) {
							if (canceled) {
								return;
							}
							int i = nodeLoaders.indexOf(nodeLoader);
							if (i + 1 < nodeLoaders.size()) {
								nodeLoader = nodeLoaders.get(i + 1);
								nodeLoader.execute();
							} else {
								canceled = true; // prevents this from executing twice
								// finished loading all nodes, so write xml file in OSP search folder
								File cacheFile = ResourceLoader.getSearchCacheFile(pathToRoot);
								XMLControl control = new XMLControlElement(rootNode.record);
								control.setValue("real_path", pathToRoot); //$NON-NLS-1$
								control.write(cacheFile.getAbsolutePath());

								setSelectionPath(treePath);

								// clear descriptions of all collection nodes (forces refresh) since child names
								// may have changed
								Enumeration<?> en = rootNode.preorderEnumeration();
								while (en.hasMoreElements()) {
									LibraryTreeNode node = (LibraryTreeNode) en.nextElement();
									if (node.record instanceof LibraryCollection) {
										node.record.setDescription(null);
										LibraryTreePanel.htmlPanesByNode.remove(node);
									}
								}
								// inform library manager
								if (browser.libraryManager != null) {
									browser.libraryManager.refreshSearchTab();
								}

								showInfo(getSelectedNode());
							}
						}
					}
    		};
    		
    		// create and add NodeLoaders in top-to-bottom order
	      Enumeration<?> e = rootNode.preorderEnumeration();
	      while (e.hasMoreElements()) {
	      	LibraryTreeNode node = (LibraryTreeNode)e.nextElement();
	      	NodeLoader nodeLoader = new NodeLoader(node);
	      	nodeLoaders.add(nodeLoader);
	      	nodeLoader.addPropertyChangeListener(listener);
	      }
	      
	      // execute first node loader to start the chain
	      nodeLoaders.get(0).execute(); 	      	
    	}
			
    	return null;
    }
    
  }
  
  /**
   * A SwingWorker class to load the HTML and metadata for an individual node.
   */
  class NodeLoader extends SwingWorker<Void, Void> {
  	
  	LibraryTreeNode node;
  	boolean hasNewChildren = false;
  	
  	NodeLoader(LibraryTreeNode treeNode) {
  		node = treeNode;
  	}
  	  	
		@Override
		public Void doInBackground() {

			String htmlPath = node.getHTMLPath();
			String target = node.getAbsoluteTarget();
			boolean isZip = target != null
					&& (target.toLowerCase().endsWith(".zip") || target.toLowerCase().endsWith(".trz")); //$NON-NLS-1$ //$NON-NLS-2$
			// if htmlPath is null and target is ZIP, look for HTML and title inside zip
			// file
			if (htmlPath == null && isZip) {
				String ext = "." + XML.getExtension(target); //$NON-NLS-1$
				URL targetURL = node.getTargetURL(); // returns cached target URL, if any
				String targetURLPath = targetURL.toExternalForm();
				String targetName = XML.getName(targetURLPath);
				if (node.getName().equals(targetName)) {
					targetName = XML.stripExtension(targetName);
					// look inside zip for html with same name as zip or trk
					Set<String> files = ResourceLoader.getZipContents(targetURLPath);
					String htmlInfoPath = null;
					for (String s : files) {
						String htmlName = XML.stripExtension(XML.getName(s));
						if (s.toLowerCase().contains(".htm") //$NON-NLS-1$
								&& (htmlName.equals(targetName + "_info"))) { //$NON-NLS-1$
							htmlInfoPath = s;
						}
					}
					if (htmlInfoPath == null) {
						for (String s : files) {
							if ("trk".equals(XML.getExtension(s))) { //$NON-NLS-1$
								String trkName = XML.stripExtension(XML.getName(s));
								for (String ss : files) {
									String htmlName = XML.stripExtension(XML.getName(ss));
									if (ss.toLowerCase().contains(".htm") //$NON-NLS-1$
											&& (htmlName.equals(trkName + "_info"))) { //$NON-NLS-1$
										htmlInfoPath = ss;
									}
								}
							}
						}
					}

					if (htmlInfoPath != null) {
						// set node record's HTML path to relative path
						htmlPath = targetURLPath + "!/" + htmlInfoPath; //$NON-NLS-1$
						String htmlCode = ResourceLoader.getHTMLCode(htmlPath);

						String redirect = LibraryBrowser.getRedirectFromHTMLCode(htmlCode);
						if (redirect != null) {
							node.record.setHTMLPath(redirect);
							node.metadataSource = targetName + ext + "!/" + htmlInfoPath; //$NON-NLS-1$
						} else {
							node.record.setHTMLPath(targetName + ext + "!/" + htmlInfoPath); //$NON-NLS-1$
						}

						String title = ResourceLoader.getTitleFromHTMLCode(htmlCode);
						if (title != null) {
							node.record.setName(title);
						}
					}
				}
			}

			// load ComPADRE nodes
			String urlPath = node.record.getProperty("reload_url"); //$NON-NLS-1$
			if (target != null && target.contains(LibraryComPADRE.HOST)) {
				if (node.record instanceof LibraryCollection) {
					hasNewChildren = LibraryComPADRE.loadResources(node);
				} else if ("".equals(node.record.getDescription()) && urlPath != null) { //$NON-NLS-1$
					LibraryComPADRE.reloadResource(node, urlPath);
				}
			}

			// clear description for non-ComPADRE nodes with no HTML path
			if (htmlPath == null) {
				if (urlPath == null)
					node.record.setDescription(null);
			} else {
				// copy HTML to cache if needed
				File cachedFile = ResourceLoader.getOSPCacheFile(htmlPath);
				// TODO BH SwingJS all files default to exists
				boolean foundInCache = cachedFile.exists();
//		  	boolean isCacheable = htmlPath.startsWith("http") || htmlPath.contains("!/"); //$NON-NLS-1$ //$NON-NLS-2$
				boolean isCacheable = htmlPath.contains("!/"); //$NON-NLS-1$
				if (!foundInCache && isCacheable)
					cachedFile = ResourceLoader.copyHTMLToOSPCache(htmlPath);
			}

			htmlPanesByNode.remove(node);
			LibraryTreeNode.htmlURLs.remove(htmlPath);

			// load metadata into node
			node.getMetadata();

			System.out.println("LibaryTreePanel.NodeLoader path=" + htmlPath + " target=" + target + " node=" + node);
			return null;
		}

    @Override
    protected void done() {
  		LibraryTreePanel.htmlPanesByNode.remove(node);
  		LibraryTreePanel.htmlPanesByURL.remove(node.getHTMLURL());
  		if (hasNewChildren) {
  			node.createChildNodes();
		  	treeModel.nodeStructureChanged(node);
  		}
  		else {
  			treeModel.nodeChanged(node);
  		}
  		if (node==getSelectedNode()) {
  			showInfo(node);
  		}
  		if (node==rootNode) {
    		browser.refreshTabTitle(pathToRoot, rootResource);
  		}
    }
  }
  
  /**
   * A SwingWorker class to show the HTMLPane for a node.
   */
  class HTMLDisplayer extends SwingWorker<HTMLPane, Void> {
  	
  	LibraryTreeNode node;
  	boolean hasNewChildren = false;
  	
  	HTMLDisplayer(LibraryTreeNode treeNode) {
  		node = treeNode;
  	}
  	  	
    @Override
    public HTMLPane doInBackground() {
    	
			HTMLPane htmlPane = htmlPanesByNode.get(node);
			if (htmlPane==null) {
		  	URL url = node.getHTMLURL(); // returns URL of original (if available) or cached (if it exists) HTML file
		  	
		  	if (url!=null) {
		  		htmlPane = htmlPanesByURL.get(url);
			  	if (htmlPane==null) {
			  		htmlPane = new HTMLPane();
			  		htmlPanesByURL.put(url, htmlPane);
			  		htmlPane.setText("<h2>"+node+"</h2>"); //$NON-NLS-1$ //$NON-NLS-2$
			  		try {
	      			htmlPane.setPage(url);
	    			} catch (Exception ex) {}
			  	}
			  	else if (!url.equals(htmlPane.getPage())) {
		    		try {
			  			htmlPane.getDocument().putProperty(Document.StreamDescriptionProperty, null);
			  			htmlPane.setPage(url);
						} catch (Exception ex) {}	  			
			  	}
		  	}
		  	else { // url==null
		  		htmlPane = new HTMLPane();
		  		String htmlStr = node.getHTMLString();
		  		htmlPane.setText(htmlStr);
		  	}
		  	if (htmlPane.getDocument() instanceof HTMLDocument) {
	  	    HTMLDocument document = (HTMLDocument)htmlPane.getDocument();
	  	    document.getStyleSheet().addRule(LibraryResource.getBodyStyle());  	
	  	    document.getStyleSheet().addRule(LibraryResource.getH1Style());  	
	  	    document.getStyleSheet().addRule(LibraryResource.getH2Style());  
		  	}
	  		htmlPanesByNode.put(node, htmlPane);
				htmlPane.setCaretPosition(0);
			}
  		
  		return htmlPane;
    }

    @Override
    protected void done() {
    	HTMLPane htmlPane = null;
			try {
				htmlPane = get();
			} catch (Exception e) {
			}
			if (htmlPane!=null && node==getSelectedNode()) {
				htmlScroller.setViewportView(htmlPane);
		    if (node==rootNode) {
		    	browser.refreshButton.setToolTipText(ToolsRes.getString("LibraryBrowser.Tooltip.Reload")); //$NON-NLS-1$
		    }
		    else browser.refreshButton.setToolTipText(ToolsRes.getString("LibraryBrowser.Tooltip.Refresh")); //$NON-NLS-1$
			}
    }
  }
  
  /**
   * A JTextField for editing LibraryTreeNode data.
   */
  protected static class EntryField extends JTextField {
  	
		static Font font = new JTextField().getFont();
  	
  	EntryField() {
  		getDocument().putProperty("parent", this); //$NON-NLS-1$
      addFocusListener(focusListener);
      addActionListener(actionListener);
      getDocument().addDocumentListener(documentListener);
  	}
  	
  	protected String getDefaultText() {
  		return null;
  	}
  	
  	protected Font getEmptyFont() {
  		return getFont();
  	}
  	
  	protected Font getDefaultFont() {
  		return getFont();
  	}
  	
    protected void processEntry() {
    	boolean fire = getBackground()==Color.yellow;
    	if (getDefaultText()!=null && "".equals(getText())) { //$NON-NLS-1$
    		setText(getDefaultText());
    		setForeground(Color.gray);
    		setFont(getEmptyFont());
    	}
      setBackground(Color.white);
      if (fire)	fireActionPerformed();
    }
    
  	
  	static DocumentListener documentListener = new DocumentListener() {   
      public void insertUpdate(DocumentEvent e) {
      	EntryField field = (EntryField)e.getDocument().getProperty("parent"); //$NON-NLS-1$
      	field.setBackground(Color.yellow);
      }
      public void removeUpdate(DocumentEvent e) {
      	EntryField field = (EntryField)e.getDocument().getProperty("parent"); //$NON-NLS-1$
      	field.setBackground(Color.yellow);
      }
			public void changedUpdate(DocumentEvent e) {}
  	};
  	
    static FocusListener focusListener = new FocusAdapter() {
      public void focusGained(FocusEvent e) {
      	EntryField field = (EntryField)e.getSource();
      	if (field.getDefaultText()!=null) {
      		field.setText(null);
  	  		field.setFont(field.getDefaultFont());
	    		field.setForeground(defaultForeground);
      	}
      	field.selectAll();
	      field.setBackground(Color.white);
      }
      public void focusLost(FocusEvent e) {
      	EntryField field = (EntryField)e.getSource();
      	field.processEntry();
      }
    };

  	static ActionListener actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	EntryField field = (EntryField)e.getSource();
        field.setBackground(Color.white);
      }
    };
    
  }
  
  /**
   * An EntryField for editing Metadata.
   */
  protected class MetadataEditField extends EntryField {
  	
  	int preferredWidth;
  	
  	MetadataEditField(int width) {
    	preferredWidth = width;
    	addActionListener(metadataFieldListener);
    }
  	
		public Dimension getMaximumSize() {	
			Dimension dim = super.getMaximumSize();
			dim.height = getPreferredSize().height;
			if (preferredWidth>0) dim.width = preferredWidth;
			return dim;
		}
		
		public Dimension getMinimumSize() {	
			Dimension dim = super.getMinimumSize();
			if (preferredWidth>0) dim.width = preferredWidth;
			return dim;
		}
		
		public Dimension getPreferredSize() {
			Dimension dim = super.getPreferredSize();
			if (preferredWidth>0) dim.width = preferredWidth;
			return dim;
		}
		
  }

  /**
   * A tree node renderer to render LibraryTreeNodes.
   */
  protected class LibraryTreeNodeRenderer extends DefaultTreeCellRenderer {
  	ResizableIcon resizableOpenIcon, resizableClosedIcon;
  	
  	LibraryTreeNodeRenderer() {
  		resizableOpenIcon = new ResizableIcon(super.getOpenIcon());
  		resizableClosedIcon = new ResizableIcon(super.getClosedIcon());  		
  	}
  	
  	@Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
      LibraryTreeNode node = (LibraryTreeNode) value;
      Icon icon = node.record.getIcon();
      Color c = getForeground();
      if (node.record instanceof LibraryCollection) {
      	icon = expanded? getOpenIcon(): getClosedIcon();
      	if (node.getTarget()!=null)
      		c = Color.red;
      }
      setToolTipText(node.getToolTip());
      if (icon==null) {
      	LibraryResource.unknownIcon.resize(FontSizer.getIntegerFactor());
      	icon = LibraryResource.unknownIcon;
      }
      setIcon(icon);
      setForeground(c);
      return this;
    }
    
    @Override
    public Icon getOpenIcon() {
    	return resizableOpenIcon;
    }

    @Override
    public Icon getClosedIcon() {
    	return resizableClosedIcon;
    }
    
  }
  
//______________________________  static methods ___________________________
  
  /**
   * Gets a shared file chooser.
   * 
   * @return the file chooser
   */
  protected static JFileChooser getFileChooser() {
  	if (chooser==null) {
  		String chooserDir = LibraryBrowser.getBrowser().library.chooserDir;
      chooser = (chooserDir==null)? 
      		new JFileChooser() : new JFileChooser(new File(chooserDir));
      htmlFilter = new FileFilter() {
        // accept directories and html files
        public boolean accept(File f) {
          if(f==null) return false;
          if(f.isDirectory()) return true;
          String ext = XML.getExtension(f.getName());
          String[] accept = new String[] {"html", "htm"}; //$NON-NLS-1$ //$NON-NLS-2$
          for (String next: accept) {
            if (next.equals(ext)) return true;
          }
          return false;
        }
        public String getDescription() {
          return ToolsRes.getString("LibraryTreePanel.HTMLFileFilter.Description"); //$NON-NLS-1$
        } 	     	
      };
      folderFilter = new FileFilter() {
        // accept directories only
        public boolean accept(File f) {
          if(f!=null && f.isDirectory()) return true;
          return false;
        }
        public String getDescription() {
          return ToolsRes.getString("LibraryTreePanel.FolderFileFilter.Description"); //$NON-NLS-1$
        } 	     	
      };
  	}
  	FontSizer.setFonts(chooser, FontSizer.getLevel());
  	return chooser;
  }

  public void refreshSelectedNode() {
	// BH needed by tracker -- what does it do?
	// TODO Auto-generated method stub
	
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
