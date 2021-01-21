/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2021 Douglas Brown, Wolfgang Christian, Robert Hanson
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

import org.opensourcephysics.display.DataTable;
import org.opensourcephysics.display.TeXParser;

/**
 * This displays a view of a single track on a TrackerPanel.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public abstract class TrackView extends JScrollPane implements PropertyChangeListener {

	static final String DEFINED_AS = ": "; //$NON-NLS-1$

	static int TVID = 0;
	
	// instance fields
	protected TrackerPanel trackerPanel;
	protected TrackChooserTView viewParent;
	
	private int trackID;
	protected int myType;

	protected boolean forceRefresh = false;
	
	protected boolean highlightVisible = true;
	final protected BitSet highlightFrames = new BitSet();
	final protected BitSet highlightRows = new BitSet(); 

	// toolbarComponents and GUI
	
	protected ArrayList<Component> toolbarComponents = new ArrayList<Component>();
	protected Icon trackIcon;

	protected int myID;

	protected boolean clipAdjusting;

	
	// constructor
	protected TrackView(TTrack track, TrackerPanel panel, TrackChooserTView view, int myType) {
		trackID = track.getID();
		myID = ++TVID;
		this.myType = myType;
		//System.out.println("TrackView adding listener for " + this);
		trackerPanel = panel;
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_LOADED, this); 
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT, this); 
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_UNITS, this); 
		viewParent = view;
	}

	protected void dispose() {
		for (Integer n : TTrack.activeTracks.keySet()) {
			TTrack track = TTrack.activeTracks.get(n);
			track.removeStepListener(this);
		}
		//System.out.println("TrackView removing listener for " + this);
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_LOADED, this); 
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT, this); 
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_UNITS, this); 
		trackerPanel = null;
	}

	abstract void refresh(int frameNumber, int mode);

	abstract void refreshGUI();

	abstract boolean isCustomState();

	abstract JButton getViewButton();

	@Override
	public String getName() {
		return getTrack().getName();
	}

	public Icon getIcon() {
		if (trackIcon == null)
			trackIcon = getTrack().getIcon(21, 16, "point");
		return trackIcon;
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
		TFrame frame = trackerPanel.getTFrame();
		TViewChooser[] choosers = frame.getViewChoosers(trackerPanel);
		for (int i = 0; i < choosers.length; i++) {
			TView tview = (choosers[i] == null ? null : choosers[i].getSelectedView());
			if (tview == viewParent && viewParent.getTrackView(viewParent.getSelectedTrack()) == this) {
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
	 * Responds to property change events. TrackView receives the following events:
	 * "step" or "steps" from the track.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		int stepNumber = Integer.MIN_VALUE;
		int mode = 0;
		String name = e.getPropertyName();
		switch (name) {
		case TTrack.PROPERTY_TTRACK_STEP:
			stepNumber = (Integer) e.getNewValue();
			mode = (e.getOldValue() == TTrack.HINT_STEP_ADDED_OR_REMOVED 
					? DataTable.MODE_APPEND_ROW : DataTable.MODE_TRACK_STEP);
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT:
			Step step = trackerPanel.getSelectedStep();
			TTrack track = getTrack();
			if (step != null && trackerPanel.getSelectedTrack() == track) {
				stepNumber = step.getFrameNumber();
			}
			mode = DataTable.MODE_TRACK_SELECTEDPOINT;
			break;
		case TTrack.PROPERTY_TTRACK_STEPS:
			mode = DataTable.MODE_TRACK_STEPS;
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_LOADED:
			mode = DataTable.MODE_TRACK_LOADED;
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_TRACK:
			mode = DataTable.MODE_TRACK_NEW;
			break;
		}
		refresh(stepNumber == Integer.MIN_VALUE ? trackerPanel.getFrameNumber() : stepNumber, mode);
	}

	protected boolean isRefreshEnabled() {
		return trackerPanel.isAutoRefresh() 
				&& trackerPanel.getTFrame().isPaintable()
				&& viewParent.isTrackViewDisplayed(getTrack());
	}

	public static String trimDefined(String name) {
		int pt = (name == null ? -1 : name.indexOf(DEFINED_AS));
		return TeXParser.removeSubscript(pt >= 0 ? name.substring(0, pt) : name);
	}

	/**
	 * The user is adjusting the clip using the carets under the VideoPlayer slider.
	 * Minimize the amount of updates.
	 * 
	 * @param frameNo
	 * @param adjusting
	 */
	public void setClipAdjusting(int frameNo, boolean adjusting) {
		clipAdjusting = adjusting;
		if (!adjusting) {
			// Columns will not have changed, but we need a full rewrite of the rows.
			// Give the sytem a chance to propagate all the property changes
			// before updating.
			SwingUtilities.invokeLater(() -> {
				refresh(frameNo, DataTable.MODE_UPDATE_ROWS);
			});
		}
	}
	
	boolean isClipAdjusting() {
		return clipAdjusting;
	}

	/**
	 * While this is sufficient for PlotTrackView, TableTrackView will
	 * still have to convert this to row numbers;
	 * 
	 * @param frameNumber
	 */
	public void highlightFrames(int frameNumber) {
		highlightFrames.clear();
		if (trackerPanel.selectedSteps.size() > 0) {
			for (Step step : trackerPanel.selectedSteps) {
				if (step.getTrack() != this.getTrack())
					continue;
				highlightFrames.set(step.getFrameNumber());
			}
		} else {
			highlightFrames.set(frameNumber);
		}
	}

	@Override
	public String toString() {
		return "[" + getClass().getSimpleName() + " " + getTrack().getName() + " " + viewParent + " ]";
	}

}
