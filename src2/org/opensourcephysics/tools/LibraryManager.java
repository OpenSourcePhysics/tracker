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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;

/**
 * A GUI for managing My Library, search targets, and the OSP cache.
 *
 * @author Douglas Brown
 */
public class LibraryManager extends JDialog {
	
	LibraryBrowser browser;
	Library library;
	JTabbedPane tabbedPane;
	JPanel collectionsPanel, importsPanel, searchPanel, cachePanel, recentPanel;
	JList collectionList, guestList;
	JTextField nameField, pathField;
	ActionListener nameAction, pathAction;
	JButton okButton, setCacheButton;
	JButton moveUpButton, moveDownButton, addButton, removeButton; // for collections and imports tabs
	JButton allButton, noneButton, clearCacheButton; // for search and cache tabs
	JToolBar libraryButtonbar;
	Box nameBox, pathBox, libraryEditBox, searchBox, cacheBox;
	JLabel nameLabel, pathLabel;
	Font sharedFont;
	TitledBorder collectionsTitleBorder, importsTitleBorder, searchTitleBorder, cacheTitleBorder;
	ArrayList<SearchCheckBox> checkboxes = new ArrayList<SearchCheckBox>();
	Dimension defaultSize = new Dimension(500, 300);
	Border listButtonBorder;
	
	/**
	 * Constructor for a frame
	 * 
	 * @param browser a LibraryBrowser
	 * @param frame the frame
	 */
	protected LibraryManager(LibraryBrowser browser, JFrame frame) {
		super(frame, true);
		this.browser = browser;
		library = browser.library;
    setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    createGUI();
    Dimension dim = new Dimension(defaultSize);
    double factor = 1+FontSizer.getLevel()*0.25;
    dim.width = (int)(dim.width*factor);
    dim.height = (int)(dim.height*factor);
    setSize(dim); 
	}
	
	/**
	 * Constructor for a dialog
	 * 
	 * @param browser a LibraryBrowser
	 * @param dialog the dialog
	 */
	protected LibraryManager(LibraryBrowser browser, JDialog dialog) {
		super(dialog, true);
		this.browser = browser;
		library = browser.library;
    setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    createGUI();
    Dimension dim = new Dimension(defaultSize);
    double factor = 1+FontSizer.getLevel()*0.25;
    dim.width = (int)(dim.width*factor);
    dim.height = (int)(dim.height*factor);
    setSize(dim); 
	}
	
	@Override
	public void setVisible(boolean vis) {
		if (vis) {
			refreshSearchTab();
			refreshCacheTab();
		}
		else {
			library.noSearchSet.clear();
			for (SearchCheckBox next: checkboxes) {
				if (!next.isSelected())
					library.noSearchSet.add(next.urlPath);
			}
		}
		super.setVisible(vis);
	}
	
	/**
	 * Creates the GUI.
	 */
	protected void createGUI() {
		JButton throwaway = new JButton("by"); //$NON-NLS-1$
		throwaway.setBorder(LibraryBrowser.buttonBorder);
		int h = throwaway.getPreferredSize().height;
		sharedFont = throwaway.getFont();

		// create collections list
		ListModel collectionListModel = new AbstractListModel() {
      public int getSize() {
      	return library.pathList.size();
      }
      public Object getElementAt(int i) { 
      	String path = library.pathList.get(i);
      	return library.pathToNameMap.get(path);
      }
    };
		collectionList = new JList(collectionListModel);
		collectionList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				refreshGUI();
			}
		});
		collectionList.setFixedCellHeight(h);
		collectionList.setFont(sharedFont);
		collectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// create import list
		ListModel importListModel = new AbstractListModel() {
      public int getSize() {
      	return library.importedPathList.size();
      }
      public Object getElementAt(int i) { 
      	String path = library.importedPathList.get(i);
      	return library.importedPathToLibraryMap.get(path).getName();
      }
    };
		guestList = new JList(importListModel);
		guestList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				refreshGUI();
			}
		});
		guestList.setFont(sharedFont);
		guestList.setFixedCellHeight(h);
		guestList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// create name action, field and label
		nameAction = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	String path = pathField.getText();
  			String prev = library.pathToNameMap.get(path);
      	String input = nameField.getText().trim();
        if(input==null || input.equals("") || input.equals(prev)) { //$NON-NLS-1$
          return;
        }
        library.renameCollection(path, input);
        browser.refreshCollectionsMenu();
  			collectionList.repaint();
  			refreshGUI();
     	}
    };
  	nameField = new LibraryTreePanel.EntryField();
  	nameField.addActionListener(nameAction);
  	nameField.addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) {
      	nameField.selectAll();
      }
      public void focusLost(FocusEvent e) {
      	nameAction.actionPerformed(null);
      }
    });
  	nameField.setBackground(Color.white);

  	nameLabel = new JLabel();
  	nameLabel.setFont(sharedFont);
  	nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 2));
  	nameLabel.setHorizontalAlignment(SwingConstants.TRAILING);
	
		// create path action, field and label
//		pathAction = new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//	    	int i = collectionList.getSelectedIndex();
//      	String path = library.pathList.get(i);
//  			String name = library.pathToNameMap.get(path);
//      	String input = pathField.getText().trim();
//        if(input==null || input.equals("") || input.equals(path)) { //$NON-NLS-1$
//          return;
//        }
//        library.pathList.remove(i);
//        library.pathList.add(i, input);
//        library.pathToNameMap.remove(path);
//        library.pathToNameMap.put(input, name);
//
//        browser.refreshCollectionsMenu();
//  			collectionList.repaint();
//  			refreshGUI();
//     	}
//    };
  	pathField = new LibraryTreePanel.EntryField();
		pathField.setEditable(false);
//  	pathField.addActionListener(pathAction);
//  	pathField.addFocusListener(new FocusAdapter() {
//      public void focusGained(FocusEvent e) {
//      	pathField.selectAll();
//      }
//      public void focusLost(FocusEvent e) {
//      	pathAction.actionPerformed(null);
//      }
//    });
  	pathField.setBackground(Color.white);

  	pathLabel = new JLabel();
  	pathLabel.setFont(sharedFont);
    pathLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 2));
    pathLabel.setHorizontalAlignment(SwingConstants.TRAILING);
  	
    // create buttons
    okButton = new JButton();
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	setVisible(false);
      }
    });
    
    moveUpButton = new JButton();
    moveUpButton.setOpaque(false);
    moveUpButton.setBorder(LibraryBrowser.buttonBorder);
    moveUpButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	boolean isImports = tabbedPane.getSelectedComponent()==importsPanel;
      	JList list = isImports? guestList: collectionList;
      	ArrayList<String> paths = isImports? library.importedPathList: library.pathList;
				int i = list.getSelectedIndex();
				String path = paths.get(i);
				paths.remove(path);
				paths.add(i-1, path);
      	list.setSelectedIndex(i-1);
      	browser.refreshCollectionsMenu();
  			browser.refreshGUI();
     	}
    });
    moveDownButton = new JButton();
    moveDownButton.setOpaque(false);
    moveDownButton.setBorder(LibraryBrowser.buttonBorder);
    moveDownButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	boolean isImports = tabbedPane.getSelectedComponent()==importsPanel;
      	JList list = isImports? guestList: collectionList;
      	ArrayList<String> paths = isImports? library.importedPathList: library.pathList;
				int i = list.getSelectedIndex();
				String path = paths.get(i);
				paths.remove(path);
				paths.add(i+1, path);
      	list.setSelectedIndex(i+1);
      	browser.refreshCollectionsMenu();
  			browser.refreshGUI();
     	}
    });
    addButton = new JButton();
    addButton.setOpaque(false);
    addButton.setBorder(LibraryBrowser.buttonBorder);
    addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	boolean imported = tabbedPane.getSelectedComponent()==importsPanel;
      	String message = imported? 
      			ToolsRes.getString("LibraryBrowser.Dialog.AddLibrary.Message"): //$NON-NLS-1$
      			ToolsRes.getString("LibraryBrowser.Dialog.AddCollection.Message"); //$NON-NLS-1$
        String title = imported? 
          	ToolsRes.getString("LibraryBrowser.Dialog.AddLibrary.Title"): //$NON-NLS-1$
          	ToolsRes.getString("LibraryBrowser.Dialog.AddCollection.Title"); //$NON-NLS-1$
        
        Object input = JOptionPane.showInputDialog(browser, 
        		message, title, JOptionPane.QUESTION_MESSAGE, null, null, null);
        
        if(input==null || input.equals("")) {                            //$NON-NLS-1$
          return;
        }
        String path = input.toString();
        path = XML.forwardSlash(path);
        path = ResourceLoader.getNonURIPath(path);
        
        if (tabbedPane.getSelectedComponent()==collectionsPanel) {
        	boolean isResource = false;
	        if (!path.startsWith("http") && new File(path).isDirectory()) { //$NON-NLS-1$
	        	isResource = true;
	        }
	        else {
	    			XMLControl control = new XMLControlElement(path);
	    			if (!control.failedToRead() && control.getObjectClass()==LibraryCollection.class) { 
	    				isResource = true;
	    			}	        	
	        }
	        if (isResource) {
	      		browser.addToCollections(path);
	      		ListModel model = collectionList.getModel();
	      		collectionList.setModel(model);
	      		refreshGUI();
	      		collectionList.repaint();
	      		collectionList.setSelectedIndex(library.pathList.size()-1);
	        	browser.refreshCollectionsMenu();
						return;
	        }
        }
        if (tabbedPane.getSelectedComponent()==importsPanel) {
        	boolean isLibrary = false;
    			XMLControl control = new XMLControlElement(path);
    			if (!control.failedToRead() && control.getObjectClass()==Library.class) { 
    				isLibrary = true;
    			}	        	
	        if (isLibrary) {
  			  	Library newLibrary = new Library();
  			  	newLibrary.browser = LibraryManager.this.browser;
  			  	control.loadObject(newLibrary);
  					if (library.importLibrary(path, newLibrary)) {
  	      		ListModel model = guestList.getModel();
  	      		guestList.setModel(model);
  	      		refreshGUI();
  	      		guestList.repaint();
  	        	guestList.setSelectedIndex(library.importedPathList.size()-1);
  	        	browser.refreshCollectionsMenu();
  					}
  					return;	        	
	        }
        }

		  	String s = ToolsRes.getString("LibraryBrowser.Dialog.CollectionNotFound.Message"); //$NON-NLS-1$
		  	JOptionPane.showMessageDialog(LibraryManager.this, 
		  			s+":\n"+path, //$NON-NLS-1$
						ToolsRes.getString("LibraryBrowser.Dialog.CollectionNotFound.Title"), //$NON-NLS-1$
						JOptionPane.WARNING_MESSAGE);  		
        
     	}
    });
    removeButton = new JButton();
    removeButton.setOpaque(false);
    removeButton.setBorder(LibraryBrowser.buttonBorder);
    removeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	boolean isImports = tabbedPane.getSelectedComponent()==importsPanel;
      	JList list = isImports? guestList: collectionList;
      	ArrayList<String> paths = isImports? library.importedPathList: library.pathList;
				int i = list.getSelectedIndex();
				String path = paths.get(i);
				paths.remove(path);
				if (isImports)
					library.importedPathToLibraryMap.remove(path);
				else
					library.pathToNameMap.remove(path);
				list.repaint();
      	if (i>=paths.size()) {
      		list.setSelectedIndex(paths.size()-1);
      	}
      	browser.refreshCollectionsMenu();
  			refreshGUI();
  			browser.refreshGUI();
     	}
    });
    // create all and none buttons
    allButton = new JButton();
    allButton.setOpaque(false);
    allButton.setBorder(LibraryBrowser.buttonBorder);
    allButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
  			for (SearchCheckBox next: checkboxes) {
  				next.setSelected(true);
  			}
      }
    });
    noneButton = new JButton();
    noneButton.setOpaque(false);
    noneButton.setBorder(LibraryBrowser.buttonBorder);
    noneButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
  			for (SearchCheckBox next: checkboxes) {
  				next.setSelected(false);
  			}
      }
    });
    
    clearCacheButton = new JButton();
    clearCacheButton.setOpaque(false);
    clearCacheButton.setBorder(LibraryBrowser.buttonBorder);
    clearCacheButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	File cache = ResourceLoader.getOSPCache();
      	ResourceLoader.clearOSPCache(cache, false);
      	refreshCacheTab();
      	tabbedPane.repaint();
      }
    });
    
    setCacheButton = new JButton();
    setCacheButton.setOpaque(false);
    setCacheButton.setBorder(LibraryBrowser.buttonBorder);
    setCacheButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	File newCache = ResourceLoader.chooseOSPCache(browser);
    		ResourceLoader.setOSPCache(newCache);
    		refreshCacheTab();
      }
    });
    Border emptyInside = BorderFactory.createEmptyBorder(1, 2, 1, 2);
    Border etched = BorderFactory.createEtchedBorder();
    Border buttonbarBorder = BorderFactory.createCompoundBorder(etched, emptyInside);
    
		libraryButtonbar = new JToolBar();
    libraryButtonbar.setFloatable(false);
    libraryButtonbar.setBorder(buttonbarBorder);
    libraryButtonbar.add(moveUpButton);
    libraryButtonbar.add(moveDownButton);
    libraryButtonbar.add(addButton);
    libraryButtonbar.add(removeButton);

    nameBox = Box.createHorizontalBox();
    nameBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 4));
    nameBox.add(nameLabel);
    nameBox.add(nameField);    
    pathBox = Box.createHorizontalBox();
    pathBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 4));
    pathBox.add(pathLabel);
    pathBox.add(pathField); 
    libraryEditBox = Box.createVerticalBox();
    libraryEditBox.add(nameBox);
    libraryEditBox.add(pathBox);
    
    // create and assemble tabs
    // collections tab
		collectionsPanel = new JPanel(new BorderLayout());
		JScrollPane scroller = new JScrollPane(collectionList);
    scroller.setViewportBorder(etched);
    scroller.getVerticalScrollBar().setUnitIncrement(8);
		collectionsTitleBorder = BorderFactory.createTitledBorder(""); //$NON-NLS-1$
		scroller.setBorder(collectionsTitleBorder);
		collectionsPanel.add(scroller, BorderLayout.CENTER);		
		collectionsPanel.add(libraryEditBox, BorderLayout.SOUTH);
		collectionsPanel.add(libraryButtonbar, BorderLayout.NORTH);
		
		// imports tab
		importsPanel = new JPanel(new BorderLayout());  		
		scroller = new JScrollPane(guestList);
    scroller.setViewportBorder(etched);
    scroller.getVerticalScrollBar().setUnitIncrement(8);
		importsTitleBorder = BorderFactory.createTitledBorder(""); //$NON-NLS-1$
		scroller.setBorder(importsTitleBorder);
		importsPanel.add(scroller, BorderLayout.CENTER);
		
		// search tab
		searchPanel = new JPanel(new BorderLayout());
		searchBox = Box.createVerticalBox();
		searchBox.setBackground(Color.white);
		searchBox.setOpaque(true);
		refreshSearchTab();
		
    scroller = new JScrollPane(searchBox);
    scroller.setViewportBorder(etched);
    scroller.getVerticalScrollBar().setUnitIncrement(8);
    searchTitleBorder = BorderFactory.createTitledBorder(""); //$NON-NLS-1$
		scroller.setBorder(searchTitleBorder);
    searchPanel.add(scroller, BorderLayout.CENTER);
    JToolBar searchButtonbar = new JToolBar();
    searchButtonbar.setFloatable(false);
    searchButtonbar.setBorder(buttonbarBorder);
    searchButtonbar.add(allButton);
    searchButtonbar.add(noneButton);
    searchPanel.add(searchButtonbar, BorderLayout.NORTH);

    // cache tab
    cachePanel = new JPanel(new BorderLayout());
		cacheBox = Box.createVerticalBox();
		cacheBox.setBackground(Color.white);
		cacheBox.setOpaque(true);
		refreshCacheTab();
		
    scroller = new JScrollPane(cacheBox);
    scroller.setViewportBorder(etched);
    scroller.getVerticalScrollBar().setUnitIncrement(8);
    cacheTitleBorder = BorderFactory.createTitledBorder(""); //$NON-NLS-1$
		scroller.setBorder(cacheTitleBorder);
		cachePanel.add(scroller, BorderLayout.CENTER);
    JToolBar cacheButtonbar = new JToolBar();
    cacheButtonbar.setFloatable(false);
    cacheButtonbar.setBorder(buttonbarBorder);
    cacheButtonbar.add(clearCacheButton);
    cacheButtonbar.add(setCacheButton);
    cachePanel.add(cacheButtonbar, BorderLayout.NORTH);
    
		// create tabbedPane
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("", collectionsPanel); //$NON-NLS-1$
//		tabbedPane.addTab("", importsPanel); //$NON-NLS-1$
		tabbedPane.addTab("", searchPanel); //$NON-NLS-1$
		tabbedPane.addTab("", cachePanel); //$NON-NLS-1$

    // add change listener last
		tabbedPane.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
      	if (tabbedPane.getSelectedComponent()==collectionsPanel) {
      		collectionsPanel.add(libraryButtonbar, BorderLayout.NORTH);
      		collectionsPanel.add(libraryEditBox, BorderLayout.SOUTH);
      		refreshGUI();
      	}
      	else if (tabbedPane.getSelectedComponent()==importsPanel) {
      		importsPanel.add(libraryButtonbar, BorderLayout.NORTH);
      		importsPanel.add(libraryEditBox, BorderLayout.SOUTH);
      		refreshGUI();
      	}
      }
    });
		
    Border space = BorderFactory.createEmptyBorder(0,2,0,2);
    listButtonBorder = BorderFactory.createCompoundBorder(etched, space);

    // assemble content pane
		JPanel contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);  		
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		JPanel south = new JPanel();
		south.add(okButton);
		contentPane.add(south, BorderLayout.SOUTH);
	}
	
	/**
	 * Refreshes the GUI including locale-based resource strings.
	 */
	protected void refreshGUI() {
		setTitle(ToolsRes.getString("LibraryManager.Title")); //$NON-NLS-1$
		
  	okButton.setText(ToolsRes.getString("Tool.Button.Close")); //$NON-NLS-1$
		addButton.setText(ToolsRes.getString("LibraryManager.Button.Add")); //$NON-NLS-1$
		removeButton.setText(ToolsRes.getString("LibraryManager.Button.Remove")); //$NON-NLS-1$
  	moveUpButton.setText(ToolsRes.getString("LibraryTreePanel.Button.Up")); //$NON-NLS-1$
  	moveDownButton.setText(ToolsRes.getString("LibraryTreePanel.Button.Down")); //$NON-NLS-1$
    allButton.setText(ToolsRes.getString("LibraryManager.Button.All")); //$NON-NLS-1$
    noneButton.setText(ToolsRes.getString("LibraryManager.Button.None")); //$NON-NLS-1$
    clearCacheButton.setText(ToolsRes.getString("LibraryManager.Button.ClearCache")); //$NON-NLS-1$
    setCacheButton.setText(ToolsRes.getString("LibraryManager.Button.SetCache")); //$NON-NLS-1$
    
		addButton.setToolTipText(ToolsRes.getString("LibraryManager.Button.Add.Tooltip")); //$NON-NLS-1$
		removeButton.setToolTipText(ToolsRes.getString("LibraryManager.Button.Remove.Tooltip")); //$NON-NLS-1$
  	moveUpButton.setToolTipText(ToolsRes.getString("LibraryTreePanel.Button.Up.Tooltip")); //$NON-NLS-1$
  	moveDownButton.setToolTipText(ToolsRes.getString("LibraryTreePanel.Button.Down.Tooltip")); //$NON-NLS-1$
    allButton.setToolTipText(ToolsRes.getString("LibraryManager.Button.All.Tooltip")); //$NON-NLS-1$
    noneButton.setToolTipText(ToolsRes.getString("LibraryManager.Button.None.Tooltip")); //$NON-NLS-1$
    clearCacheButton.setToolTipText(ToolsRes.getString("LibraryManager.Button.ClearCache.Tooltip")); //$NON-NLS-1$
    setCacheButton.setToolTipText(ToolsRes.getString("LibraryManager.Button.SetCache.Tooltip")); //$NON-NLS-1$

    nameLabel.setText(ToolsRes.getString("LibraryManager.Label.Name")+":"); //$NON-NLS-1$ //$NON-NLS-2$
  	pathLabel.setText(ToolsRes.getString("LibraryManager.Label.Path")+":"); //$NON-NLS-1$ //$NON-NLS-2$
		collectionsTitleBorder.setTitle(ToolsRes.getString("LibraryManager.Title.MenuItems")+":"); //$NON-NLS-1$ //$NON-NLS-2$
		importsTitleBorder.setTitle(ToolsRes.getString("LibraryManager.Title.Import")+":"); //$NON-NLS-1$ //$NON-NLS-2$
		searchTitleBorder.setTitle(ToolsRes.getString("LibraryManager.Title.Search")+":"); //$NON-NLS-1$ //$NON-NLS-2$
		cacheTitleBorder.setTitle(ToolsRes.getString("LibraryManager.Title.Cache")+":"); //$NON-NLS-1$ //$NON-NLS-2$
		int k = tabbedPane.indexOfComponent(collectionsPanel);
		if (k>-1) {
			tabbedPane.setTitleAt(k, ToolsRes.getString("LibraryManager.Tab.MyLibrary")); //$NON-NLS-1$
			tabbedPane.setToolTipTextAt(k, ToolsRes.getString("LibraryManager.Tab.MyLibrary.Tooltip")); //$NON-NLS-1$
		}
		k = tabbedPane.indexOfComponent(importsPanel);
  	if (k>-1) {
  		tabbedPane.setTitleAt(k, ToolsRes.getString("LibraryManager.Tab.Import")); //$NON-NLS-1$
  		tabbedPane.setToolTipTextAt(k, ToolsRes.getString("LibraryManager.Tab.Import.Tooltip")); //$NON-NLS-1$
  	}
		k = tabbedPane.indexOfComponent(searchPanel);
  	if (k>-1) {
  		tabbedPane.setTitleAt(k, ToolsRes.getString("LibraryManager.Tab.Search")); //$NON-NLS-1$
  		tabbedPane.setToolTipTextAt(k, ToolsRes.getString("LibraryManager.Tab.Search.Tooltip")); //$NON-NLS-1$
  	}
		k = tabbedPane.indexOfComponent(cachePanel);
  	if (k>-1) {
  		tabbedPane.setTitleAt(k, ToolsRes.getString("LibraryManager.Tab.Cache")); //$NON-NLS-1$
  		tabbedPane.setToolTipTextAt(k, ToolsRes.getString("LibraryManager.Tab.Cache.Tooltip")); //$NON-NLS-1$
  	}
		
		resizeLabels();

  	pathField.setForeground(LibraryTreePanel.defaultForeground);
  	if (tabbedPane.getSelectedComponent()==collectionsPanel) {
  		nameField.setEditable(true);
    	int i = collectionList.getSelectedIndex();
  		moveDownButton.setEnabled(i<library.pathList.size()-1);
  		moveUpButton.setEnabled(i>0);
  		if (i>-1 && library.pathList.size()>i) {
	  		removeButton.setEnabled(true);
				String path = library.pathList.get(i);
				pathField.setText(path);
				pathField.setCaretPosition(0);
				String name = library.pathToNameMap.get(path);
				nameField.setText(name);
				boolean unavailable = path.startsWith("http:") && !LibraryBrowser.webConnected; //$NON-NLS-1$
	      Resource res = unavailable? null: ResourceLoader.getResourceZipURLsOK(path);
	      if (res==null) {
	      	pathField.setForeground(LibraryTreePanel.darkRed);
	      }
  		}
  		else {
	  		removeButton.setEnabled(false);
	  		nameField.setEditable(false);
				nameField.setText(null);  			
      	nameField.setBackground(Color.white);
				pathField.setText(null);  			
      	pathField.setBackground(Color.white);
  		}
		}
		else if (tabbedPane.getSelectedComponent()==importsPanel) {
  		nameField.setEditable(false);
    	int i = guestList.getSelectedIndex();
  		moveDownButton.setEnabled(i<library.importedPathList.size()-1);
  		moveUpButton.setEnabled(i>0);
  		if (i>-1 && library.importedPathList.size()>i) {
	  		removeButton.setEnabled(true);
				String path = library.importedPathList.get(i);
				pathField.setText(path);
				pathField.setCaretPosition(0);
				String name = library.importedPathToLibraryMap.get(path).getName();
				nameField.setText(name);
				boolean unavailable = path.startsWith("http:") && !LibraryBrowser.webConnected; //$NON-NLS-1$
	      Resource res = unavailable? null: ResourceLoader.getResourceZipURLsOK(path);
	      if (res==null) {
	      	pathField.setForeground(LibraryTreePanel.darkRed);
	      }
  		}
  		else {
	  		removeButton.setEnabled(false);
				nameField.setText(null);  			
      	nameField.setBackground(Color.white);
				pathField.setText(null);  			
      	pathField.setBackground(Color.white);
  		}
		}
		nameField.setBackground(Color.white);
  	pathField.setBackground(Color.white);
	}
	
	protected void refreshSearchTab() {
		// refresh list of cached search targets
		searchBox.removeAll();
		checkboxes.clear();
		ArrayList<JLabel> labels = new ArrayList<JLabel>();
		for (LibraryResource next: browser.getSearchCacheTargets()) {
			String path = next.collectionPath;
			if (path==null) continue;
			
			String name = next.toString();
			JLabel label = new JLabel(name);
			label.setToolTipText(path);
			labels.add(label);
			
			SearchCheckBox checkbox = new SearchCheckBox(path);
      checkboxes.add(checkbox);
      JButton button = new DeleteButton(path);
      
      Box bar = Box.createHorizontalBox();
      bar.add(label);
      bar.add(checkbox);
      bar.add(button);
      bar.add(Box.createHorizontalGlue());
      
      searchBox.add(bar);
  		FontSizer.setFonts(searchBox, FontSizer.getLevel());
		}
		if (labels.isEmpty()) return;
		
    // set label sizes
    FontRenderContext frc = new FontRenderContext(null, false, false); 
    Font font = labels.get(0).getFont();
    int w = 0;
    for(JLabel next: labels) {
      Rectangle2D rect = font.getStringBounds(next.getText(), frc);
      w = Math.max(w, (int) rect.getWidth());
    }
    Dimension labelSize = new Dimension(w+48, 20);
    for(JLabel next: labels) {
      next.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
      next.setPreferredSize(labelSize);
    }

	}

	protected void refreshCacheTab() {
		// refresh list of cache hosts
		cacheBox.removeAll();
		ArrayList<JLabel> labels = new ArrayList<JLabel>();
		File cache = ResourceLoader.getOSPCache();
  	File[] hosts = cache==null? new File[0]: cache.listFiles(ResourceLoader.OSP_CACHE_FILTER);
    clearCacheButton.setEnabled(hosts.length>0);

    if (hosts.length==0) {
			JLabel label = new JLabel(ToolsRes.getString("LibraryManager.Cache.IsEmpty")); //$NON-NLS-1$
			label.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
      Box box = Box.createHorizontalBox();
      box.add(label);
      box.add(Box.createHorizontalGlue());      
      cacheBox.add(box);
    	return;
    }
    
    for (File hostFile: hosts) {
    	// eliminate the "osp-" that starts all cache host filenames
    	String hostText = hostFile.getName().substring(4).replace('_', '.');
      long bytes = getFileSize(hostFile);
    	long size = bytes/(1024*1024);
    	if (bytes>0) {
    		if (size>0) hostText += " ("+size+" MB)"; //$NON-NLS-1$ //$NON-NLS-2$
    		else hostText += " ("+bytes/1024+" kB)"; //$NON-NLS-1$ //$NON-NLS-2$
    	}
			JLabel label = new JLabel(hostText);
			label.setToolTipText(hostFile.getAbsolutePath());
			labels.add(label);
			
			ClearHostButton button = new ClearHostButton(hostFile);
      
      Box bar = Box.createHorizontalBox();
      bar.add(label);
      bar.add(button);
      bar.add(Box.createHorizontalGlue());
      
      cacheBox.add(bar);
  		FontSizer.setFonts(cacheBox, FontSizer.getLevel());
		}
		
    // set label sizes
    FontRenderContext frc = new FontRenderContext(null, false, false); 
    Font font = labels.get(0).getFont();
    int w = 0;
    for(JLabel next: labels) {
      Rectangle2D rect = font.getStringBounds(next.getText(), frc);
      w = Math.max(w, (int) rect.getWidth());
    }
    Dimension labelSize = new Dimension(w+48, 20);
    for(JLabel next: labels) {
      next.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
      next.setPreferredSize(labelSize);
    }
	}

  /**
   * Sets the font level.
   *
   * @param level the desired font level
   */
  protected void setFontLevel(int level) {
		FontSizer.setFonts(this, level);
		// set cell height of collectionList
		Font font = collectionList.getFont();
		font = FontSizer.getResizedFont(font, level);
		int space = 8+level;
		collectionList.setFixedCellHeight(font.getSize()+space);
		resizeLabels();
  }
  
  private void resizeLabels() {
		// adjust size of labels so they right-align
    int w = 0;
    Font font = nameLabel.getFont();
    FontRenderContext frc = new FontRenderContext(null, false, false); 
    Rectangle2D rect = font.getStringBounds(nameLabel.getText()+" ", frc); //$NON-NLS-1$
    w = Math.max(w, (int) rect.getWidth()+4);
    rect = font.getStringBounds(pathLabel.getText()+" ", frc); //$NON-NLS-1$
    w = Math.max(w, (int) rect.getWidth()+4);

    Dimension labelSize = new Dimension(w, 20);
    nameLabel.setPreferredSize(labelSize);
    nameLabel.setMinimumSize(labelSize);
    pathLabel.setPreferredSize(labelSize);
    pathLabel.setMinimumSize(labelSize);  	
  }
  
  /**
   * Gets the total size of a folder.
   * 
   * @param folder the folder
   * @return the size in bytes
   */
  private long getFileSize(File folder) {
    if (folder==null) return 0;
    long foldersize = 0;
    File[] files = folder.equals(ResourceLoader.getOSPCache())? 
    		folder.listFiles(ResourceLoader.OSP_CACHE_FILTER):
    		folder.listFiles();
    if (files==null) return 0;
    for (int i = 0; i < files.length; i++) {
	    if (files[i].isDirectory()) {
	    	foldersize += getFileSize(files[i]);
	    } else {
	    	foldersize += files[i].length();
	    }
    }
    return foldersize;
  }

  /**
   * A checkbox to add and remove a collection from the no-search list
   */
  protected class SearchCheckBox extends JCheckBoxMenuItem {
  	
  	String urlPath;
  	
    /**
     * Constructs a RemoveButton.
     * @param path 
     */
    public SearchCheckBox(String path) {
    	urlPath = path;
    	setText(ToolsRes.getString("LibraryManager.Checkbox.Search")); //$NON-NLS-1$
    	setFont(sharedFont);
      setSelected(!library.noSearchSet.contains(path));
      setOpaque(false);
      int space = 20 + FontSizer.getLevel()*5;
      setBorder(BorderFactory.createEmptyBorder(0, 0, 0, space));
    }
    
    public Dimension getMaximumSize() {
    	return getPreferredSize();
    }
    
  }
  
  /**
   * A button to delete xml files from the search cache
   */
  protected class DeleteButton extends JButton {
  	
  	String urlPath;
  	
    /**
     * Constructs a DeleteButton.
     * @param path 
     */
    public DeleteButton(String path) {
    	urlPath = path;
    	setText(ToolsRes.getString("LibraryManager.Button.Delete")); //$NON-NLS-1$
    	setToolTipText(ToolsRes.getString("LibraryManager.Button.Delete.Tooltip")); //$NON-NLS-1$
  		setOpaque(false);
  		setBorder(listButtonBorder);
  		setBorderPainted(false);
  		setContentAreaFilled(false);
      addMouseListener(new MouseAdapter() {
      	public void mouseEntered(MouseEvent e) {
      		setBorderPainted(true);
      		setContentAreaFilled(true);
      	}
      	public void mouseExited(MouseEvent e) {
      		setBorderPainted(false);
      		setContentAreaFilled(false);
      	}
      });
      addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	File file = ResourceLoader.getSearchCacheFile(urlPath);
        	file.delete();
        	refreshSearchTab();
       	}
      });
    }
    
    @Override
    public Dimension getMaximumSize() {
    	return getPreferredSize();
    }
    
  }
  
  /**
   * A button to delete xml files from the search cache
   */
  protected class ClearHostButton extends JButton {
  	
  	File hostCacheDir;
  	
    /**
     * Constructs a RemoveButton.
     * @param host 
     */
    public ClearHostButton(File host) {
    	hostCacheDir = host;
    	setText(ToolsRes.getString("LibraryManager.Button.Clear")); //$NON-NLS-1$
    	setToolTipText(ToolsRes.getString("LibraryManager.Button.Clear.Tooltip")); //$NON-NLS-1$
  		setOpaque(false);
  		setBorder(listButtonBorder);
  		setBorderPainted(false);
  		setContentAreaFilled(false);
      addMouseListener(new MouseAdapter() {
      	public void mouseEntered(MouseEvent e) {
      		setBorderPainted(true);
      		setContentAreaFilled(true);
      	}
      	public void mouseExited(MouseEvent e) {
      		setBorderPainted(false);
      		setContentAreaFilled(false);
      	}
      });
      addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	ResourceLoader.clearOSPCacheHost(hostCacheDir);
        	refreshCacheTab();
        	tabbedPane.repaint();
       	}
      });
    }
    
    @Override
    public Dimension getMaximumSize() {
    	return getPreferredSize();
    }
    
  }

}
