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
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.Border;

import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.FontSizer;

/**
 * This is a panel with a toolbar for selecting and controlling TViews.
 *
 * @author Douglas Brown
 */
public class TViewChooser extends JPanel implements PropertyChangeListener {
	
	// static fields
	protected static Icon minIcon, maxIcon, restoreIcon;
	
	static {
    maxIcon = new ImageIcon(
        Tracker.class.getResource("resources/images/maximize.gif")); //$NON-NLS-1$
    restoreIcon = new ImageIcon(
        Tracker.class.getResource("resources/images/restore.gif")); //$NON-NLS-1$
		
	}

  // instance fields
  protected TrackerPanel trackerPanel;
  protected ArrayList<TView> views = new ArrayList<TView>();
  protected TView selectedView;
  protected JPanel viewPanel;
  protected JToolBar toolbar;
  protected JButton chooserButton;
  protected Component toolbarFiller = Box.createHorizontalGlue();
  protected JButton maximizeButton;
  protected JPopupMenu popup = new JPopupMenu();
  protected int[] dividerLocs = new int[4];
  protected int dividerSize;
  protected boolean maximized;

  /**
   * Constructs a TViewChooser.
   *
   * @param panel the tracker panel being viewed
   */
  public TViewChooser(TrackerPanel panel) {
    super(new BorderLayout());
    trackerPanel = panel;
    trackerPanel.addPropertyChangeListener("track", this); //$NON-NLS-1$
    // viewPanel
    viewPanel = new JPanel(new CardLayout());
    viewPanel.setBorder(BorderFactory.createEtchedBorder());
    add(viewPanel, BorderLayout.CENTER);
    // toolbar
    toolbar = new JToolBar();
    toolbar.setFloatable(false);
    toolbar.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
      	toolbar.requestFocusInWindow();
      	if (e.getClickCount()==2) {
      		maximizeButton.doClick(0);
      	}
      	if (OSPRuntime.isPopupTrigger(e)) {
      		final TView view = getSelectedView();
      		if (view==null) return;
        	JPopupMenu popup = new JPopupMenu();
          JMenuItem helpItem = new JMenuItem(TrackerRes.getString("Dialog.Button.Help")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
          helpItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if (view instanceof PageTView) {
            		trackerPanel.getTFrame().showHelp("textview", 0); //$NON-NLS-1$
            	}
            	else if (view instanceof TableTView) {
            		trackerPanel.getTFrame().showHelp("datatable", 0); //$NON-NLS-1$
            	}
            	else if (view instanceof PlotTView) {
            		trackerPanel.getTFrame().showHelp("plot", 0); //$NON-NLS-1$
            	}
            	else if (view instanceof WorldTView) {
            		trackerPanel.getTFrame().showHelp("GUI", 0); //$NON-NLS-1$
            	}
            }
          });
          popup.add(helpItem);
        	FontSizer.setFonts(popup, FontSizer.getLevel());
          popup.show(toolbar, e.getX(), e.getY());
      	}
      }
    });
    toolbar.setBorder(BorderFactory.createEtchedBorder());
    add(toolbar, BorderLayout.NORTH);
    // chooser button
    chooserButton = new TButton() {
    	protected JPopupMenu getPopup() {
        // inner popup menu listener class
        ActionListener listener = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            // select the named view
            String name = e.getActionCommand();
            setSelectedView(getView(name));
         }
        };
        // add view items to popup
        popup.removeAll();
        JMenuItem item;
        Iterator<TView> it = getViews().iterator();
        while (it.hasNext()) {
          TView view = it.next();
          String name = view.getViewName();
          item = new JMenuItem(name, view.getViewIcon());
          item.setActionCommand(name);
          item.addActionListener(listener);
          popup.add(item);
        }
        FontSizer.setFonts(popup, FontSizer.getLevel());
    		return popup;
    	}
    };
    // maximize buttons
    Border empty = BorderFactory.createEmptyBorder(7, 3, 7, 3);
    Border etched = BorderFactory.createEtchedBorder();
    maximizeButton = new TButton(maxIcon, restoreIcon);
    maximizeButton.setBorder(BorderFactory.createCompoundBorder(etched, empty));
  	maximizeButton.setToolTipText(TrackerRes.getString("TViewChooser.Maximize.Tooltip")); //$NON-NLS-1$
    maximizeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
    		if (!maximized) {
    			maximize();
  			}
    		else restore();
      	maximizeButton.setSelected(maximized);
      	maximizeButton.setToolTipText(maximized?
      			TrackerRes.getString("TViewChooser.Restore.Tooltip"): //$NON-NLS-1$
        		TrackerRes.getString("TViewChooser.Maximize.Tooltip")); //$NON-NLS-1$
      }
    });
    createDefaultViews();
    refresh();
  }

  /**
   * gets the TrackerPanel containing the tracks
   *
   * @return the tracker panel
   */
  public Dimension getMinimumSize() {
    return new Dimension(0, 0);
  }

  /**
   * gets the TrackerPanel containing the tracks
   *
   * @return the tracker panel
   */
  public TrackerPanel getTrackerPanel() {
    return trackerPanel;
  }

  /**
   * Adds a view of the tracker panel
   *
   * @param view the view being added
   */
  public void addView(TView view) {
    if (view == null || view.getTrackerPanel() != trackerPanel) return;
    if (getView(view.getClass()) != null) return;
    views.add(view);
    view.cleanup();
    refreshViewPanel();
  }

  /**
   * Adds a view of the tracker panel at a specified index
   *
   * @param index the list index desired
   * @param view the view being added
   */
  public void addView(int index, TView view) {
    if (view.getTrackerPanel() != trackerPanel) return;
    if (getView(view.getClass()) != null) return;
    views.add(index, view);
    view.cleanup();
    refreshViewPanel();
  }

  /**
   * Removes a view from this chooser
   *
   * @param view the view requesting to be removed
   */
  public void removeView(TView view) {
    views.remove(view);
    if (view == selectedView) selectedView = null;
    refreshViewPanel();
  }

  /**
   * Gets a list of the available views.
   *
   * @return the list of views
   */
  public Collection<TView> getViews() {
    return views;
  }

  /**
   * Gets the view with the specified name. May return null.
   *
   * @param viewName the name of the view
   * @return the view
   */
  public TView getView(String viewName) {
    Iterator<TView> it = getViews().iterator();
    while (it.hasNext()) {
      TView view = it.next();
      if (view.getViewName().equals(viewName)) return view;
    }
    return null;
  }

  /**
   * Gets a collection of views castable to the specified class or interface.
   *
   * @param type the class
   * @return a collection of views
   */
  public Collection<TView> getViews(Class<?extends TView> type) {
    Collection<TView> list = new ArrayList<TView>();
    for (TView view: list) {
      if (type.isInstance(view)) list.add(view);
    }
    return list;
  }

  /**
   * Gets the view of the specified class. May return null.
   *
   * @param c the view class
   * @return the view
   */
  public TView getView(Class<?>  c) {
    for (TView view: getViews()) {
      if (view.getClass() == c) return view;
    }
    return null;
  }

  /**
   * Gets the selected view
   *
   * @return the selected view
   */
  public TView getSelectedView() {
    return selectedView;
  }

  /**
   * Selects the specified view
   *
   * @param view the view to select
   */
  public void setSelectedView(TView view) {
  	if (view == null || selectedView==view) return;
    if (!getViews().contains(view)) {
      addView(view);
    }
    trackerPanel.changed = true;
    TTrack selectedTrack = null;
    // clean up previously selected view
    if (selectedView != null) {
      selectedView.cleanup();
      ((JComponent)selectedView).removePropertyChangeListener("trackview", this); //$NON-NLS-1$
      if (selectedView instanceof TrackChooserTView) {
        selectedTrack = ((TrackChooserTView)selectedView).getSelectedTrack();
      }
    }
    selectedView = view;
    // initialize and refresh newly selected view
    selectedView.init();
    ((JComponent)selectedView).addPropertyChangeListener("trackview", this); //$NON-NLS-1$
    if (selectedView instanceof TrackChooserTView) {
      ((TrackChooserTView)selectedView).setSelectedTrack(selectedTrack);
    }
    selectedView.refresh();
    // put icon in button
    chooserButton.setIcon(selectedView.getViewIcon());
    // show the view on the viewPanel
    CardLayout cl = (CardLayout) (viewPanel.getLayout());
    cl.show(viewPanel, selectedView.getViewName());
    repaint();
    // refresh the toolbar
    refreshToolbar();
  }

  /**
   * Responds to property change events. ViewChooser listens for the following
   * property names: "track" from trackerPanel
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    if (name.equals("track")) {             // track has been added or removed //$NON-NLS-1$
      if (selectedView != null) {
        selectedView.propertyChange(e);
        refreshToolbar();
      }
    }
    else if (name.equals("trackview")) {    // trackview has changed //$NON-NLS-1$
      refreshToolbar();
    }
  }

  /**
   * Refreshes this chooser and its views.
   */
  public void refresh() {
    chooserButton.setToolTipText(TrackerRes.getString("TViewChooser.Button.Choose.Tooltip")); //$NON-NLS-1$
    for (TView view: getViews()) 
    	view.refresh();         
  }
  
  /**
   * Refreshes the popup menus of the views.
   */
  public void refreshMenus() {
    for (TView view: getViews())
    	if (view instanceof TrackChooserTView) {
    		TrackChooserTView chooser = (TrackChooserTView)view;
    		chooser.refreshMenus(); 
    	}
  }
  
  /**
   * Maximizes this chooser and its views.
   */
  public void maximize() {
  	if (maximized) return;
  	TFrame frame = trackerPanel.getTFrame();
  	MainTView mainView = frame.getMainView(trackerPanel);
  	boolean mainViewHasPlayer = false;
  	for (Component next: mainView.getComponents()) {
  		mainViewHasPlayer = mainViewHasPlayer || next==mainView.getPlayerBar();
  	}
  	if (mainViewHasPlayer) {
  		JToolBar toolbar = mainView.getPlayerBar();
  		add(toolbar, BorderLayout.SOUTH);
      toolbar.setFloatable(false);
  	}  	
		// save divider locations and size
		for (int j=0; j<dividerLocs.length; j++) {
      JSplitPane pane = frame.getSplitPane(trackerPanel, j);
      dividerLocs[j] = pane.getDividerLocation();
      if (pane.getDividerSize()>0)
      	dividerSize=pane.getDividerSize();
			pane.setDividerSize(0);
		}
		maximized = true;
  	Container[] views = frame.getViews(trackerPanel);
  	for (int i = 0; i<views.length; i++) {
    	if (TViewChooser.this==views[i]) {
    		switch(i) {
    			case 0:
    				frame.setDividerLocation(trackerPanel, 0, 0.0);
    				frame.setDividerLocation(trackerPanel, 1, 1.0);
    				break;
    			case 1:
    				frame.setDividerLocation(trackerPanel, 0, 0.0);
    				frame.setDividerLocation(trackerPanel, 1, 0.0);
    				break;
    			case 2:
    				frame.setDividerLocation(trackerPanel, 0, 1.0);
    				frame.setDividerLocation(trackerPanel, 2, 0.0);
    				frame.setDividerLocation(trackerPanel, 3, 0.0);
    				break;
    			case 3:
    				frame.setDividerLocation(trackerPanel, 0, 1.0);
    				frame.setDividerLocation(trackerPanel, 2, 0.0);
    				frame.setDividerLocation(trackerPanel, 3, 1.0);
    		}
    	}
  	}
  }  

  /**
   * Restores this chooser and its views.
   */
  public void restore() {
  	TFrame frame = trackerPanel.getTFrame();
  	MainTView mainView = frame.getMainView(trackerPanel);
  	boolean thisHasPlayer = false;
  	for (Component next: getComponents()) {
  		thisHasPlayer = thisHasPlayer || next==mainView.getPlayerBar();
  	}
  	if (thisHasPlayer) {
    	JToolBar player = mainView.getPlayerBar();
      mainView.add(player, BorderLayout.SOUTH);
      player.setFloatable(true);
  	}  	
		for (int j=0; j<dividerLocs.length; j++) {
      JSplitPane pane = frame.getSplitPane(trackerPanel, j);
      pane.setDividerSize(dividerSize);
			frame.setDividerLocation(trackerPanel, j, dividerLocs[j]);
		}
		maximized = false;
  }  

  /**
   * Creates default views
   */
  protected void createDefaultViews() {
    addView(new PlotTView(trackerPanel));
    addView(new TableTView(trackerPanel));
    WorldTView worldView = new WorldTView(trackerPanel);
    addView(worldView);
    addView(new PageTView(trackerPanel));
  }

  /**
   * Refreshes the toolbar
   */
  protected void refreshToolbar() {
    toolbar.removeAll();
    toolbar.add(chooserButton);
    ArrayList<Component> list = selectedView.getToolBarComponents();
    if (list != null) {
      for (Component c: list) {
      	toolbar.add(c);
      }
    }
    toolbar.add(toolbarFiller);
    toolbar.add(maximizeButton);
    FontSizer.setFonts(toolbar, FontSizer.getLevel());
    toolbar.repaint();
  }

  /**
   * Refreshes the viewPanel.
   */
  private void refreshViewPanel() {
    viewPanel.removeAll();
    Iterator<TView> it = getViews().iterator();
    while (it.hasNext()) {
      TView view = it.next();
      String name = view.getViewName();
      viewPanel.add((JComponent)view, name);
    }
    // reselect selected view, if any
    if (selectedView != null && getViews().contains(selectedView))
      setSelectedView(selectedView);
    // otherwise select the first view in the list
    else {
      it = getViews().iterator();
      if (it.hasNext()) {
        setSelectedView(it.next());
      }
    }
  }

  /**
   * Returns an XML.ObjectLoader to save and load object data.
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
     * @param obj the TrackerPanel object to save
     */
    public void saveObject(XMLControl control, Object obj) {
      TViewChooser chooser = (TViewChooser)obj;
      // save the selected view
      control.setValue("selected_view", chooser.selectedView); //$NON-NLS-1$
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
      TViewChooser chooser = (TViewChooser)obj;
      TView view = (TView)control.getObject("selected_view"); //$NON-NLS-1$
      if (view != null) {
      	chooser.setSelectedView(view);
      }
      return obj;
    }
  }

}
