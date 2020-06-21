/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2019  Douglas Brown
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

import org.opensourcephysics.controls.OSPLog;

/**
 * This displays a view of a single track on a TrackerPanel.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public abstract class TrackView extends JScrollPane implements PropertyChangeListener {
	
	public static final int REFRESH_PLOTCOUNT = -1;
	public static final int REFRESH_DATA_STRUCTURE = 0;
	public static final int REFRESH_COLUMNS = 1;
	public static final int REFRESH_DATA_VALUES = 2;
	public static final int REFRESH_STEPNUMBER = 3;

	// instance fields
	private int trackID;
	protected TrackerPanel trackerPanel;
	protected ArrayList<Component> toolbarComponents = new ArrayList<Component>();
	protected TrackChooserTView parent;
	protected int myType;


	// constructor
	protected TrackView(TTrack track, TrackerPanel panel, TrackChooserTView view, int myType) {
		trackID = track.getID();
		this.myType = myType;
		System.out.println("TrackView adding listener for " + this);
		trackerPanel = panel;
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_LOADED, this); 
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT, this); 
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_UNITS, this); 
		parent = view;
	}

	protected void dispose() {
		for (Integer n : TTrack.activeTracks.keySet()) {
			TTrack track = TTrack.activeTracks.get(n);
			track.removeStepListener(this);
		}
		System.out.println("TrackView removing listener for " + this);
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_LOADED, this); 
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT, this); 
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_UNITS, this); 
		trackerPanel = null;
	}

	abstract void refresh(int stepNumber, int refreshType);

	abstract void refreshGUI();

	abstract boolean isCustomState();

	abstract JButton getViewButton();

	@Override
	public String getName() {
		return getTrack().getName();
	}

	public Icon getIcon() {
		return getTrack().getIcon(21, 16, "point"); //$NON-NLS-1$
	}

	TTrack getTrack() {
		return TTrack.getTrack(trackID);
	}

	/**
	 * Gets the TViewChooser that owns (displays) this track view.
	 * 
	 * @return the TViewChooser. May return null if this is not displayed
	 */
	protected TViewChooser getOwner() {
		// return TViewChooser with this view , if any
		TFrame frame = trackerPanel.getTFrame();
		TViewChooser[] choosers = frame.getViewChoosers(trackerPanel);
		for (int i = 0; i < choosers.length; i++) {
			TView tview = choosers[i].getSelectedView();
			if (tview != null && tview == parent && parent.getTrackView(parent.getSelectedTrack()) == this) {
				return choosers[i];
			}
		}
		return null;
	}

	/**
	 * Gets toolbar components for toolbar of parent view
	 *
	 * @return an ArrayList of components to be added to a toolbar
	 */
	public ArrayList<Component> getToolBarComponents() {
		return toolbarComponents;
	}

	/**
	 * Responds to property change events.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		String name = e.getPropertyName();
		OSPLog.debug(name+" pig1 ");
		switch (name) {
		case TTrack.PROPERTY_TTRACK_STEP:
			OSPLog.debug(TTrack.PROPERTY_TTRACK_STEP+" pig "+e.getOldValue());
			if (e.getOldValue() == TTrack.HINT_STEP_ADDED_OR_REMOVED)
				refresh((Integer) e.getNewValue(), REFRESH_DATA_STRUCTURE);
			else
				refresh((Integer) e.getNewValue(), REFRESH_DATA_VALUES);
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT:
			Step step = trackerPanel.getSelectedStep();
			TTrack track = getTrack();
			if (step != null && trackerPanel.getSelectedTrack() == track) {
				OSPLog.debug(TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT+" pig ");
				refresh(step.getFrameNumber(), REFRESH_STEPNUMBER);
				break;
			}
			// fall through //
		case TTrack.PROPERTY_TTRACK_STEPS:
			OSPLog.debug(TTrack.PROPERTY_TTRACK_STEPS+" pig2 ");
			if (e.getOldValue() == TTrack.HINT_STEPS_SELECTED)
				refresh(trackerPanel.getFrameNumber(), REFRESH_STEPNUMBER);
			else
				refresh(trackerPanel.getFrameNumber(), REFRESH_DATA_STRUCTURE);
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_LOADED:
			refresh(trackerPanel.getFrameNumber(), REFRESH_DATA_STRUCTURE);
			break;
		}
	}

	protected boolean isRefreshEnabled() {
		return trackerPanel.isAutoRefresh() 
				&& trackerPanel.getTFrame().isPaintable()
				&& parent.isTrackViewDisplayed(getTrack());
	}

}
