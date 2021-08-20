/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2021 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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
 * <http://physlets.org/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.Component;
import java.beans.*;
import java.util.*;

import javax.swing.*;

/**
 * This is a view of a tracker panel that can be added to a TViewChooser.
 * Classes that implement TView must descend from JComponent.
 *
 * @author Douglas Brown
 */
public interface TView extends PropertyChangeListener {
	
	String PROPERTY_TVIEW_TRACKVIEW = "trackview";

	int VIEW_UNSET = -1;
	int VIEW_PLOT = 0;
	int VIEW_TABLE = 1;
	int VIEW_WORLD = 2;
	int VIEW_PAGE = 3;
	int VIEW_MAIN = 4;

	// view icons to show in chooserButton
	Icon[] VIEW_ICONS = { PlotTView.PLOTVIEW_ICON, TableTView.TABLEVIEW_ICON, WorldTView.WORLDVIEW_ICON,
			PageTView.PAGEVIEW_ICON };

	// view names for chooserButton are localizable
	String[] VIEW_NAMES = { "TFrame.View.Plot", "TFrame.View.Table", "TFrame.View.World", "TFrame.View.Text" };

	/**
	 * Initializes the view
	 */
	public void init();

	/**
	 * Refreshes the view
	 */
	public void refresh();

	/**
	 * Cleans up the view
	 */
	public void cleanup();

	/**
	 * Disposes of the view
	 */
	public void dispose();

	/**
	 * Gets the TrackerPanel containing the track data
	 *
	 * @return the tracker panel containing the data to be viewed
	 */
	public TrackerPanel getTrackerPanel();

	/**
	 * Gets the name of the view
	 *
	 * @return the name of the view
	 */
	public String getViewName();

	/**
	 * Gets the icon for this view
	 *
	 * @return the icon for the view
	 */
	public Icon getViewIcon();

	/**
	 * Gets the type of view
	 *
	 * @return one of the defined types
	 */
	public int getViewType();

	/**
	 * Gets the toolbar components for this view
	 *
	 * @return an ArrayList of components to be added to a toolbar
	 */
	public ArrayList<Component> getToolBarComponents();

	/**
	 * Refreshes a popup menu by adding items to it
	 *
	 * @param popup the popup to refresh
	 */
	public void refreshPopup(JPopupMenu popup);

	/**
	 * Returns true if this view is in a custom state.
	 *
	 * @return false
	 */
	default public boolean isCustomState() {
		return false;
	}

	/**
	 * Returns true if this view is in a visible pane.
	 *
	 * @return false
	 */
	default public boolean isViewPaneVisible() {
		final TrackerPanel trackerPanel = getTrackerPanel();
		TFrame tf;
		if (trackerPanel == null || (tf = trackerPanel.getTFrame()) == null || tf.getTabCount() == 0)
			return false;
		Integer id = trackerPanel.getID();
		TView[][] views = tf.getTViews(trackerPanel);
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

}
