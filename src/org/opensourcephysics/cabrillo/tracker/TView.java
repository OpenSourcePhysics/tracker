/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2025 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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
 * <https://opensourcephysics.github.io/tracker-website/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.display.OSPRuntime.Disposable;

/**
 * This is a view of a tracker panel that can be added to a TViewChooser.
 * Classes that implement TView must descend from JComponent.
 *
 * @author Douglas Brown
 */
public abstract class TView extends JPanel implements PropertyChangeListener, Disposable {
	
	public final static String PROPERTY_TVIEW_TRACKVIEW = "trackview";

	public final static int VIEW_UNSET = -1;
	public final static int VIEW_PLOT = 0;
	public final static int VIEW_TABLE = 1;
	public final static int VIEW_WORLD = 2;
	public final static int VIEW_PAGE = 3;
	public final static int VIEW_MAIN = 4;

	// view icons to show in chooserButton
	public final static Icon[] VIEW_ICONS = { PlotTView.PLOTVIEW_ICON, TableTView.TABLEVIEW_ICON, WorldTView.WORLDVIEW_ICON,
			PageTView.PAGEVIEW_ICON };

	// view names for chooserButton are localizable
	public final static String[] VIEW_NAMES = { "TFrame.View.Plot", "TFrame.View.Table", "TFrame.View.World", "TFrame.View.Text" };

	
    protected TFrame frame;
    protected Integer panelID;
    
	protected ArrayList<Component> toolbarComponents = new ArrayList<Component>();

	public TView(TrackerPanel panel) {
		frame = panel.getTFrame();
		panelID = panel.getID();
	}

	/**
	 * Initializes the view
	 */
	public abstract void init();

	/**
	 * Refreshes the view
	 */
	public abstract void refresh();

	/**
	 * Cleans up the view
	 */
	public abstract void cleanup();

	/**
	 * Gets the TrackerPanel containing the track data
	 *
	 * @return the tracker panel containing the data to be viewed
	 */
	public abstract TrackerPanel getTrackerPanel();

	/**
	 * Gets the name of the view
	 *
	 * @return the name of the view
	 */
	public abstract String getViewName();

	/**
	 * Gets the icon for this view
	 *
	 * @return the icon for the view
	 */
	public abstract Icon getViewIcon();

	/**
	 * Gets the type of view
	 *
	 * @return one of the defined types
	 */
	public abstract int getViewType();

	/**
	 * Gets the toolbar components for this view. Overridden by most subclasses
	 *
	 * @return an ArrayList of components to be added to a toolbar
	 */
	public ArrayList<Component> getToolBarComponents() {
		return toolbarComponents;
	}


	/**
	 * Refreshes a popup menu by adding items to it
	 *
	 * @param popup the popup to refresh
	 */
	public void refreshPopup(JPopupMenu popup) {
		// see TableTView
	}


	/**
	 * Returns true if this view is in a custom state.
	 *
	 * @return false
	 */
	public boolean isCustomState() {
		return false;
	}

	/**
	 * Returns true if this view is in a visible pane.
	 *
	 * @return false
	 */
	public boolean isViewPaneVisible() {
		final TrackerPanel trackerPanel = getTrackerPanel();
		TFrame tf;
		if (trackerPanel == null || (tf = trackerPanel.getTFrame()) == null || tf.getTabCount() == 0)
			return false;
		Integer id = trackerPanel.getID();
		TView[][] views = tf.getTViews(trackerPanel, false);
		if (views == null)
			return false;
		for (int i = 0; i < views.length; i++) {
			if (views[i] != null)
				for (int j = 0; j < views[i].length; j++) {
					if (views[i][j] == this) {
						int[] order = (TFrame.isPortraitLayout() ? TFrame.PORTRAIT_VIEW_ORDER : TFrame.DEFAULT_ORDER);
						return tf.isViewPaneVisible(order[i], id);
					}
				}
		}
		return false;
	}

	@Override
	public void dispose() {		
		frame = null;
		panelID = null;
		toolbarComponents = null;
	}
	
	@Override
	public void finalize() {
		OSPLog.finalized(this);
	}
}
