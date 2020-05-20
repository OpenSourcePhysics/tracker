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

/**
 * This displays a view of a single track on a TrackerPanel.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public abstract class TrackView extends JScrollPane implements PropertyChangeListener {

	// instance fields
	private int trackID;
	protected TrackerPanel trackerPanel;
	protected ArrayList<Component> toolbarComponents = new ArrayList<Component>();
	protected TrackChooserTView parent;

	// constructor
	protected TrackView(TTrack track, TrackerPanel panel, TrackChooserTView view) {
		trackID = track.getID();
		trackerPanel = panel;
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT, this); 
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_UNITS, this); 
		parent = view;
	}

	protected void dispose() {
		for (Integer n : TTrack.activeTracks.keySet()) {
			TTrack track = TTrack.activeTracks.get(n);
			track.removeStepListener(this);
		}
		trackerPanel.removePropertyChangeListener("selectedpoint", this); //$NON-NLS-1$
		trackerPanel.removePropertyChangeListener("units", this); //$NON-NLS-1$
		trackerPanel = null;
	}

	abstract void refresh(int stepNumber);

	abstract void refreshGUI();

	abstract boolean isCustomState();

	abstract JButton getViewButton();

	@Override
	public String getName() {
		TTrack track = getTrack();
		return track.getName();
	}

	public Icon getIcon() {
		TTrack track = getTrack();
		return track.getIcon(21, 16, "point"); //$NON-NLS-1$
	}

	TTrack getTrack() {
		return TTrack.getTrack(trackID);
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
	 * Responds to property change events. TrackView receives the following events:
	 * "step" or "steps" from the track.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		String name = e.getPropertyName();
		switch (name) {
		case TTrack.PROPERTY_TTRACK_STEP:
			refresh((Integer) e.getNewValue());
			break;
		case TTrack.PROPERTY_TTRACK_STEPS:
			refresh(trackerPanel.getFrameNumber());
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT:
			Step step = trackerPanel.getSelectedStep();
			TTrack track = getTrack();
			if (step != null && trackerPanel.getSelectedTrack() == track) {
				refresh(step.getFrameNumber());
			} else {
				refresh(trackerPanel.getFrameNumber());
			}
			break;
		}
	}

	protected boolean isRefreshEnabled() {

		return Tracker.allowDataRefresh 
				&& trackerPanel.getAutoRefresh() 
				&& trackerPanel.frame.isPainting()
				&& parent.isTrackViewDisplayed(getTrack());
	}

}
