/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2015  Douglas Brown
 *
 * Tracker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tracker; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at <http://www.gnu.org/copyleft/gpl.html>
 *
 * For additional Tracker information and documentation, please see
 * <http://www.cabrillo.edu/~dbrown/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.beans.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.LaunchBuilder;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * This displays html or plain text in one or more tabs.
 *
 * @author Douglas Brown
 */
public class PageTView extends JPanel implements TView {

  // instance fields
  protected Icon icon;
  protected TrackerPanel trackerPanel;
  protected ArrayList<TabView> tabs = new ArrayList<TabView>();
  protected JTabbedPane tabbedPane;     // each tab is a TabView
  protected JButton pageButton;
  protected ArrayList<Component> toolbarComponents = new ArrayList<Component>();
  protected JDialog nameDialog;
  protected JTextField nameField;
  protected JPanel noTab;
  protected JLabel noTabLabel;
  protected JLabel tabTitleLabel;
  protected Box.Filler filler = (Box.Filler)Box.createHorizontalGlue();
  protected Border titleBorder;
  protected boolean locked;
  
  static {
  	XML.setLoader(TabView.class, new TabLoader());
  }

  /**
   * Constructs a TextTView for the specified tracker panel.
   *
   * @param panel the tracker panel
   */
  protected PageTView(TrackerPanel panel) {
    trackerPanel = panel;
    icon = new ImageIcon(
        Tracker.class.getResource("resources/images/html.gif")); //$NON-NLS-1$
    setBackground(panel.getBackground());
    createGUI();
    refresh();
  }

  /**
   * Refreshes this view.
   */
  public void refresh() {
  	refreshTabs();
  	removeAll();
    pageButton.setText(TrackerRes.getString("PageTView.Button.Page")); //$NON-NLS-1$
  	if (tabs.isEmpty()) {
    	noTabLabel.setText(TrackerRes.getString("TextTView.Label.NoTab")); //$NON-NLS-1$
    	add(noTab, BorderLayout.CENTER);  		
  	}
  	else if (tabs.size()==1) {
      add(tabs.get(0), BorderLayout.CENTER);
    }
    else {
    	add(tabbedPane, BorderLayout.CENTER);
    }
		FontSizer.setFonts(this, FontSizer.getLevel());

    validate();
    repaint();
  }

  /**
   * Initializes this view
   */
  public void init() {
  }

  /**
   * Cleans up this view
   */
  public void cleanup() {
  }

  /**
   * Gets the tracker panel containing the tracks
   *
   * @return the tracker panel
   */
  public TrackerPanel getTrackerPanel() {
    return trackerPanel;
  }


  /**
   * Gets the name of the view
   *
   * @return the name of this view
   */
  public String getViewName() {
  	return TrackerRes.getString("TFrame.View.Text"); //$NON-NLS-1$
  }

  /**
   * Gets the icon for this view
   *
   * @return the icon
   */
  public Icon getViewIcon() {
  	return icon;
  }

  /**
   * Gets the toolbar components
   *
   * @return an ArrayList of components to be added to a toolbar
   */
  public ArrayList<Component> getToolBarComponents() {
    return toolbarComponents;
  }

  /**
   * Returns true if this view is in a custom state.
   *
   * @return true if in a custom state, false if in the default state
   */
  public boolean isCustomState() {
  	return tabs.size()>0;
  }
  
  /**
   * Adds a tab to the tabbed pane.
   *
   * @param tab the tab to add
   */
  public void addTab(TabView tab) {
  	tabs.add(tab);
  	if (trackerPanel!=null) {
  		trackerPanel.changed = true;
  	}
  	refresh();
  }

  /**
   * Removes a tab from the tabbed pane.
   *
   * @param tab the tab to remove
   */
  public void removeTab(TabView tab) {
  	tabs.remove(tab);
  	if (trackerPanel!=null) {
  		trackerPanel.changed = true;
  	}
  	refresh();
  }

  /**
   * Renames a tab.
   *
   * @param tab the tab to rename
   */
  public void renameTab(TabView tab) {
    // show dialog with name of this track selected
  	nameDialog = getNameDialog();
  	nameDialog.setTitle(TrackerRes.getString("TextTView.Dialog.TabTitle.Title")); //$NON-NLS-1$
  	nameField.setText(tab.data.title);
  	nameField.setBackground(Color.white);
  	nameField.selectAll();
  	nameDialog.pack();
  	Point p = getLocationOnScreen();
  	p.x += (getWidth()-nameDialog.getWidth())/2;
  	p.y -= pageButton.getHeight();
  	nameDialog.setLocation(p);
    nameDialog.setVisible(true);
  }

  /**
   * Gets the selected tab.
   *
   * @return the tab
   */
  public TabView getSelectedTab() {
  	TabView tab = (TabView)tabbedPane.getSelectedComponent();
  	if (tab==null && !tabs.isEmpty()) {
  		tab = tabs.get(0);
  	}
  	return tab;
  }

  /**
   * Sets the selected tab.
   *
   * @param tab the tab
   */
  public void setSelectedTab(TabView tab) {
  	if (tabs.size()>1)
  		tabbedPane.setSelectedComponent(tab);
  }

  /**
   * Responds to property change events.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
//    String name = e.getPropertyName();
  }
  
//_________________________ protected and private methods _________________

  /**
   * Creates the GUI.
   */
  protected void createGUI() {
    setPreferredSize(new Dimension(400, 200));
    setLayout(new BorderLayout());
    // create the tabbed pane
    tabbedPane = new JTabbedPane(SwingConstants.TOP);
    tabbedPane.setBackground(trackerPanel.getBackground());
    tabbedPane.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
      	refreshTitle();
      }
    });
    tabbedPane.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
      	tabbedPane.requestFocusInWindow();
      	if (OSPRuntime.isPopupTrigger(e)) {
      		// show popup menu
        	JPopupMenu popup = getPopup(getSelectedTab());
          popup.show(tabbedPane, e.getX(), e.getY());
      	}
      	else if (e.getClickCount()==2 && !locked
      			&& trackerPanel.isEnabled("pageView.edit")) { //$NON-NLS-1$
        	renameTab(getSelectedTab());
      	}
      }
    });
    // create the new tab button
    pageButton = new TButton() {
    	// override getMaximumSize method so has same height as chooser button
	    public Dimension getMaximumSize() {
	      Dimension dim = super.getMaximumSize();
	      Dimension min = getMinimumSize();
	    	Container c = getParent().getParent();
	  		if (c instanceof TViewChooser) {
	  			int h = ((TViewChooser)c).chooserButton.getHeight();
	  			dim.height = Math.max(h, min.height);
	  		}
	      return dim;
	    } 
	    
	    public JPopupMenu getPopup() {
      	JPopupMenu popup = new JPopupMenu();
	    	if (!trackerPanel.isEnabled("pageView.edit")) {//$NON-NLS-1$
	        JMenuItem item = new JMenuItem(TrackerRes.getString("TTrack.MenuItem.Locked")); //$NON-NLS-1$
	        item.setEnabled(false);
	        popup.add(item);
	    		FontSizer.setFonts(popup, FontSizer.getLevel());
	    		return popup;
	    	}
        JMenuItem item = new JMenuItem(TrackerRes.getString("TextTView.Button.NewTab")); //$NON-NLS-1$
        item.addActionListener(new ActionListener() {
    	    public void actionPerformed(ActionEvent e) {
    	    	TabView tab = new TabView(new TabData());
    	      int n = tabs.size()+1;
    	      if (n>1) {
    	      	tab.data.title += " "+n; //$NON-NLS-1$
    	      }
    	      addTab(tab);
    	      setSelectedTab(tab);
    	    }
        });
        item.setEnabled(!locked);
        popup.add(item);
        item = new JMenuItem(TrackerRes.getString("TextTView.MenuItem.OpenHTML")); //$NON-NLS-1$
        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = LaunchBuilder.getHTMLChooser();
            int result = chooser.showOpenDialog(trackerPanel);
            if(result==JFileChooser.APPROVE_OPTION) {
              File file = chooser.getSelectedFile();
              TabView tab = getSelectedTab();
              if (tab==null) {
              	tab = new TabView(new TabData());
              	addTab(tab);
              }
            	tab.setUndoableText(XML.getAbsolutePath(file));
              refresh();
              OSPRuntime.chooserDir = XML.getDirectoryPath(file.getPath());
            }
          }
        });
        item.setEnabled(!locked);
        popup.add(item);
        popup.addSeparator();
        item = new JRadioButtonMenuItem(TrackerRes.getString("TTrack.MenuItem.Locked")); //$NON-NLS-1$
        item.addActionListener(new ActionListener() {
    	    public void actionPerformed(ActionEvent e) {
    	    	JMenuItem item = (JMenuItem)e.getSource();
    	    	locked = item.isSelected();
    	    }
        });
        item.setSelected(locked);
        popup.add(item);
    		FontSizer.setFonts(popup, FontSizer.getLevel());
        return popup;
	    }
    };
    // create tabTitleLabel
		tabTitleLabel = new JLabel();
  	tabTitleLabel.setOpaque(false);
  	tabTitleLabel.setForeground(Color.BLUE.darker());
  	tabTitleLabel.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
      	if ("".equals(tabTitleLabel.getText())) return; //$NON-NLS-1$
      	tabTitleLabel.requestFocusInWindow();
      	if (OSPRuntime.isPopupTrigger(e)) {
      		// show popup menu
        	JPopupMenu popup = getPopup(getSelectedTab());
          popup.show(tabTitleLabel, e.getX(), e.getY());
      	}
      	else if (e.getClickCount()==2 && !locked
      			&& trackerPanel.isEnabled("pageView.edit")) { //$NON-NLS-1$
        	renameTab(getSelectedTab());
      	}
      }
      
      public void mouseEntered(MouseEvent e) {
      	if (!locked && trackerPanel.isEnabled("pageView.edit")) //$NON-NLS-1$
      		tabTitleLabel.setBorder(titleBorder);
      }
      
      public void mouseExited(MouseEvent e) {
      	tabTitleLabel.setBorder(null);
      }

    });
  	Border empty = BorderFactory.createEmptyBorder(0, 2, 1, 2);
  	Border line = BorderFactory.createLineBorder(tabTitleLabel.getForeground());
  	titleBorder = BorderFactory.createCompoundBorder(line, empty);
   	// asssemble toolbar components
    toolbarComponents.add(pageButton);
  	toolbarComponents.add(filler);
    toolbarComponents.add(tabTitleLabel);
		// create the noTab panel
		noTab = new JPanel();
		noTabLabel = new JLabel();
		Font font = new JTextField().getFont();
		noTabLabel.setFont(font);
		noTab.add(noTabLabel);
		noTab.setBackground(getBackground());
		noTab.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
      	if (OSPRuntime.isPopupTrigger(e)) {
        	JPopupMenu popup = new JPopupMenu();
          JMenuItem helpItem = new JMenuItem(TrackerRes.getString("Dialog.Button.Help")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
          helpItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              trackerPanel.getTFrame().showHelp("textview", 0); //$NON-NLS-1$
            }
          });
          popup.add(helpItem);
        	FontSizer.setFonts(popup, FontSizer.getLevel());
          popup.show(noTab, e.getX(), e.getY());
      	}
      }
    });
  }

  /**
   * Refreshes all tabs.
   */
  protected void refreshTabs() {
  	TabView prev = getSelectedTab();
    tabbedPane.removeAll();
    for (TabView tab: tabs) {
    	tab.pageView = this;
    	tab.data.trackerPanel = trackerPanel;
    	tab.refreshView(false);
      tabbedPane.addTab(tab.data.title, tab);
    }
    if (prev!=null && tabbedPane.indexOfComponent(prev)>-1) {
    	tabbedPane.setSelectedComponent(prev);
    }
    refreshTitle();
    if (trackerPanel!=null) {
    	TToolBar.getToolbar(trackerPanel).refresh(false);
    }
  }
  
  /**
   * Refreshes the title bar.
   */
  protected void refreshTitle() {
  	TabView tab = getSelectedTab();
  	tabTitleLabel.setText(tab==null? null: tab.data.title);
  }
  
  /**
   * Gets the popup menu for a specified tab.
   *
   * @param tab the tab
   * @return the popup menu
   */
  protected JPopupMenu getPopup(final TabView tab) {
  	JPopupMenu popup = new JPopupMenu();
    String s = null;
  	if (trackerPanel.isEnabled("pageView.edit")) { //$NON-NLS-1$
      int keyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	    JMenuItem renameItem = new JMenuItem(TrackerRes.getString("TextTView.MenuItem.SetTitle")); //$NON-NLS-1$
	    renameItem.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
					renameTab(tab);
	      }
	    });
	    renameItem.setEnabled(!locked);
	    popup.add(renameItem);
	    JMenuItem openItem = new JMenuItem(TrackerRes.getString("TextTView.MenuItem.OpenHTML")); //$NON-NLS-1$
	    openItem.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        JFileChooser chooser = LaunchBuilder.getHTMLChooser();
	        int result = chooser.showOpenDialog(trackerPanel);
	        if(result==JFileChooser.APPROVE_OPTION) {
	          File file = chooser.getSelectedFile();
	        	tab.setUndoableText(XML.getAbsolutePath(file));
	          refresh();
	          OSPRuntime.chooserDir = XML.getDirectoryPath(file.getPath());
	        }
	      }
	    });
	    openItem.setEnabled(!locked);
	    popup.add(openItem);
	    popup.addSeparator();
	  	s = TrackerRes.getString("PageTView.MenuItem.ClosePage")+" \""; //$NON-NLS-1$ //$NON-NLS-2$
	  	s += tab.data.title+"\""; //$NON-NLS-1$
	    JMenuItem closeItem = new JMenuItem(s);
	    closeItem.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	      	removeTab(tab);
	      }
	    });
	    closeItem.setEnabled(!locked);
	    popup.add(closeItem);
	    if (tab.data.url!=null) {
	    	s = TrackerRes.getString("PageTView.MenuItem.OpenInBrowser"); //$NON-NLS-1$
		    JMenuItem item = new JMenuItem(s);
		    item.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent e) {
          	OSPDesktop.displayURL(tab.data.url.toExternalForm());
		      }
		    });
		    popup.add(item);
	    }
	    if (tab.undoManager.canUndoOrRedo()) {
		    popup.addSeparator();
		    if (tab.undoManager.canUndo()) {
		      s = TrackerRes.getString("TMenuBar.MenuItem.Undo")+" "; //$NON-NLS-1$ //$NON-NLS-2$
		      s += TrackerRes.getString("TextTView.TextEdit.Description"); //$NON-NLS-1$	    	
			    JMenuItem undoItem = new JMenuItem(s);
			    undoItem.setAccelerator(KeyStroke.getKeyStroke('Z', keyMask));
			    undoItem.addActionListener(new ActionListener() {
			      public void actionPerformed(ActionEvent e) {
			        tab.undoManager.undo();
			      }
			    });
			    undoItem.setEnabled(!locked);
			    popup.add(undoItem);
		    }
		    if (tab.undoManager.canRedo()) {
		      s = TrackerRes.getString("TMenuBar.MenuItem.Redo")+" "; //$NON-NLS-1$ //$NON-NLS-2$
		      s += TrackerRes.getString("TextTView.TextEdit.Description"); //$NON-NLS-1$	    	
			    JMenuItem redoItem = new JMenuItem(s);
			    redoItem.setAccelerator(KeyStroke.getKeyStroke('Y', keyMask));
			    redoItem.addActionListener(new ActionListener() {
			      public void actionPerformed(ActionEvent e) {
			        tab.undoManager.redo();
			      }
			    });
			    redoItem.setEnabled(!locked);
			    popup.add(redoItem);
		    }
	    }
	    popup.addSeparator();
  	}
  	s = TrackerRes.getString("Dialog.Button.Help")+"..."; //$NON-NLS-1$ //$NON-NLS-2$
    JMenuItem helpItem = new JMenuItem(s);
    helpItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        trackerPanel.getTFrame().showHelp("textview", 0); //$NON-NLS-1$
      }
    });
    popup.add(helpItem);
		FontSizer.setFonts(popup, FontSizer.getLevel());
    return popup;
  }
  
  protected JDialog getNameDialog() {
  	if (nameDialog==null) {
      // create the name dialog
      nameField = new JTextField(20);
      nameField.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	TabView tab = getSelectedTab();
        	tab.data.setTitle(nameField.getText());
        	refresh();
        	nameDialog.setVisible(false);
        }
      });
      nameField.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          nameField.setBackground(Color.yellow);
        }
      });
      JToolBar bar = new JToolBar();
      bar.setFloatable(false);
      bar.add(nameField);
      JPanel contentPane = new JPanel(new BorderLayout());
      contentPane.add(bar, BorderLayout.CENTER);
	    nameDialog = new JDialog(JOptionPane.getFrameForComponent(this), true);
	    nameDialog.addWindowListener(new WindowAdapter() {
	      public void windowClosing(WindowEvent e) {
	      	TabView tab = getSelectedTab();
        	tab.data.setTitle(nameField.getText());
	      	refresh();
	      }
	    });
	    nameDialog.setContentPane(contentPane);
	    nameDialog.pack();
	    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	    int x = (dim.width - nameDialog.getBounds().width) / 2;
	    int y = (dim.height - nameDialog.getBounds().height) / 2;
	    nameDialog.setLocation(x, y);
  	}
		FontSizer.setFonts(nameDialog, FontSizer.getLevel());
    return nameDialog;
  }
 
//__________________________________ inner classes _________________________
  
  /**
   * A class to hold the view for a single tab.
   */
  public static class TabView extends JPanel {
  	
  	protected TabData data;
    protected JEditorPane displayPane;
    protected JEditorPane editorPane;
    protected JScrollPane scroller;
    protected PageTView pageView;
    protected UndoableEditSupport undoSupport;
    protected UndoManager undoManager;
    protected HyperlinkListener hyperlinkListener;

    TabView(TabData tab) {
    	super(new BorderLayout());
    	data = tab;
    	class TextView extends JTextPane {
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
    	
    	// display pane
    	displayPane = new TextView();
      displayPane.setEditable(false);
      hyperlinkListener = new HyperlinkListener() {
        public void hyperlinkUpdate(HyperlinkEvent e) {
          if(data.hyperlinksEnabled
          		&& e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
            try {
              if(!org.opensourcephysics.desktop.OSPDesktop.browse(e.getURL().toURI())) {
                // try the old way
                org.opensourcephysics.desktop.ostermiller.Browser.init();
                org.opensourcephysics.desktop.ostermiller.Browser.displayURL(e.getURL().toString());
              }
            } catch(Exception ex) {}
          }
        }
      };
      displayPane.addHyperlinkListener(hyperlinkListener);      
      displayPane.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          if(e.getKeyCode()==KeyEvent.VK_Z && e.isControlDown()) {
          	if (undoManager.canUndo()) {
          		undoManager.undo();
          	}
          }
          else if(e.getKeyCode()==KeyEvent.VK_Y && e.isControlDown()) {
          	if (undoManager.canRedo()) {
          		undoManager.redo();
          	}
          }
        }
      });
      displayPane.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
        	if (OSPRuntime.isPopupTrigger(e)) {
        		// show popup menu        		
          	JPopupMenu popup = pageView.getPopup(pageView.getSelectedTab());
            popup.show(displayPane, e.getX(), e.getY());
        	}
        	else if (e.getClickCount()==2 && !pageView.locked
        			&& pageView.trackerPanel.isEnabled("pageView.edit")) { //$NON-NLS-1$
          	editorPane.setBackground(Color.white);
	          refreshView(true);
	          editorPane.selectAll();
        	}
        }
      });
      
      // editor pane
    	editorPane = new TextView();
      editorPane.setContentType("text"); //$NON-NLS-1$
      editorPane.setEditable(true);
      editorPane.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
        	if (OSPRuntime.isPopupTrigger(e)) {
        		// show popup menu        		
          	JPopupMenu popup = pageView.getPopup(pageView.getSelectedTab());
            popup.show(editorPane, e.getX(), e.getY());
        	}
        }
      });
      editorPane.addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) {
        	if (editorPane.getBackground().equals(Color.yellow)) {
	        	setUndoableText(editorPane.getText());
        	}
	        refreshView(false);
        }
      });
      editorPane.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          if(e.getKeyCode()==KeyEvent.VK_Z && e.isControlDown()) {
          	setUndoableText(editorPane.getText());
  	        refreshView(false);
          	if (undoManager.canUndo()) {
          		undoManager.undo();
          	}
          }
          else if(e.getKeyCode()==KeyEvent.VK_ENTER && e.isShiftDown()) {
          	setUndoableText(editorPane.getText());
  	        refreshView(false);
          } else {
          	editorPane.setBackground(Color.yellow);
          }
        }
      });
      scroller = new JScrollPane(displayPane);
      add(scroller, BorderLayout.CENTER);
    	refreshView(false);
      // set up the undo system
      undoManager = new UndoManager();
      undoSupport = new UndoableEditSupport();
      undoSupport.addUndoableEditListener(undoManager);
    }
    
    void refreshView(boolean editing) {
    	if (editing) {
    		scroller.setViewportView(editorPane);
      	editorPane.setText(data.text);
        editorPane.requestFocusInWindow();
    	}
    	else {
        editorPane.setBackground(Color.white);
    		scroller.setViewportView(displayPane);
        if (data.getURL()!=null) {
      		try {      			
            displayPane.setContentType("text/html"); //$NON-NLS-1$
            displayPane.setPage(data.url);
  	        if(data.url.getRef()!=null) {
  	        	displayPane.scrollToReference(data.url.getRef());
  	        }
          } catch(IOException e) {
          	displayPane.setContentType("text"); //$NON-NLS-1$
          	displayPane.setText(data.text);
          }
        }
        else {
        	displayPane.setContentType("text"); //$NON-NLS-1$
        	displayPane.setText(data.text);
        }
        displayPane.requestFocusInWindow();
    	}
      revalidate();
    	repaint();    	
    }
    
    void setUndoableText(String text) {
    	if (text==null || text.equals(data.text)) return;
    	UndoableEdit edit = new TextEdit(this, text, data.text);
      undoSupport.postEdit(edit);
      data.setText(text);
    }
  }
 
  /**
   * A class to hold the data for a single tab.
   */
  public static class TabData {
    String title;
    boolean hyperlinksEnabled = true;
    String text; // may be text for display or url path
    URL url;
    TrackerPanel trackerPanel;

    /**
     * No-arg constructor.
     */
    TabData() {
    	text = TrackerRes.getString("TextTView.NewTab.Text1"); //$NON-NLS-1$
    	text += XML.NEW_LINE+XML.NEW_LINE;
    	text += TrackerRes.getString("TextTView.NewTab.Text2"); //$NON-NLS-1$
    	title = TrackerRes.getString("TextTView.NewTab.Title"); //$NON-NLS-1$
    }

    /**
     * Constructor with tab text and title.
     *
     * @param title the tab title (may be null)
     * @param text the text
     */
    TabData(String title, String text) {
      this.title = title;
    	setText(text);
    }

    /**
     * Sets the title.
     *
     * @param title the title
     */
    public void setTitle(String title) {
    	if (title==null) return;
      this.title = title;
      if (trackerPanel!=null) {
      	trackerPanel.changed = true;
      	TToolBar.getToolbar(trackerPanel).refresh(false);
      }
    }

    /**
     * Sets the text.
     *
     * @param text the text
     */
    public void setText(String text) {
    	if (text==null) return;
      this.text = text;
      setURL(text); // fails for non-url text
    }

    /**
     * Gets the URL. May return null.
     *
     * @return the URL
     */
    public URL getURL() {
      if(url==null) { 
        setURL(text);
      }
      return url;
    }

    /**
     * Sets the URL.
     *
     * @param path the url path
     */
    private void setURL(String path) {
      url = null;
      Resource res = ResourceLoader.getResource(path);
      if((res!=null)&&(res.getURL()!=null)) {
        url = res.getURL();
        try {
          InputStream in = url.openStream();
          in.close();
        } catch(Exception ex) {
          url = null;
        }
      }
      if (trackerPanel!=null) {
      	trackerPanel.changed = true;
      	TToolBar.getToolbar(trackerPanel).refresh(false);
      }
    }
    
  }
  
  /**
   * A class to undo/redo a text edit.
   */
  protected static class TextEdit extends AbstractUndoableEdit {
    TabView tab;
  	String text, prev;

    /**
     * Constructor.
     * @param tab the TabView being edited
     * @param newText the new text
     * @param prevText the previous text
     */
    public TextEdit(TabView tab, String newText, String prevText) {
      this.tab = tab;
      text = newText;
      prev = prevText;
    }

    public void undo() throws CannotUndoException {
      super.undo();
      tab.data.setText(prev);
      tab.refreshView(false);
    }

    public void redo() throws CannotUndoException {
      super.redo();
      tab.data.setText(text);
      tab.refreshView(false);
    }

    public String getPresentationName() {
      return TrackerRes.getString("TextTView.TextEdit.Description"); //$NON-NLS-1$
    }

  }

  /**
   * A class to save and load object data for the TabData class.
   */
  static class TabLoader implements XML.ObjectLoader {

    /**
     * Saves object data.
     *
     * @param control the control to save to
     * @param obj the object to save
     */
    public void saveObject(XMLControl control, Object obj) {
      TabView tab = (TabView)obj;
      TabData data = tab.data;
      control.setValue("title", data.title); //$NON-NLS-1$
      if (data.url==null) {
      	control.setValue("text", data.text);  //$NON-NLS-1$
      }
      else if (data.url.getProtocol().equals("file")) { //$NON-NLS-1$
      	TrackerPanel panel = data.trackerPanel;
      	File file = panel.getDataFile();
      	if (file!=null) {
      		String path = data.url.getFile();
      		// strip leading slashes from path
      		while (path.startsWith("/")) { //$NON-NLS-1$
      			path = path.substring(1);
      		}
      		String base = XML.getDirectoryPath(XML.getAbsolutePath(file)); 
          control.setValue("text", XML.getPathRelativeTo(path, base)); //$NON-NLS-1$
      	}
      	else {
        	control.setValue("text", data.url.toExternalForm());  //$NON-NLS-1$
      	}
      }
      else {
      	control.setValue("text", data.url.toExternalForm());  //$NON-NLS-1$
      }
    }

    /**
     * Creates an object.
     *
     * @param control the control
     * @return the newly created object
     */
    public Object createObject(XMLControl control){
    	String title = control.getString("title"); //$NON-NLS-1$
    	String text = control.getString("text"); //$NON-NLS-1$
      return new TabView(new TabData(title, text));
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
  
  /**
   * Returns an XML.ObjectLoader to save/load data for the TextTView class.
   *
   * @return the XML.ObjectLoader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load object data.
   */
  static class Loader implements XML.ObjectLoader {

    /**
     * Saves object data.
     *
     * @param control the control to save to
     * @param obj the object to save
     */
    public void saveObject(XMLControl control, Object obj) {
      PageTView view = (PageTView)obj;
      control.setValue("tabs", view.tabs); //$NON-NLS-1$
      control.setValue("locked", view.locked); //$NON-NLS-1$
    }

    /**
     * Creates an object.
     *
     * @param control the control
     * @return the newly created object
     */
    public Object createObject(XMLControl control){
      return null;
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
    	PageTView view = (PageTView)obj;
    	view.locked = control.getBoolean("locked"); //$NON-NLS-1$
      // load the tabs
      ArrayList<?> tabs = ArrayList.class.cast(control.getObject("tabs")); //$NON-NLS-1$
      if (tabs != null) {
        Iterator<?> it = tabs.iterator();
        while (it.hasNext()) {
        	view.addTab((TabView)it.next());
        }
      }
      return obj;
    }
  }
}

