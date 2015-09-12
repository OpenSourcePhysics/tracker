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

import javax.swing.*;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLProperty;

/**
 * This displays plot track views selected from a dropdown list.
 *
 * @author Douglas Brown
 */
public class PlotTView extends TrackChooserTView {

  protected Icon icon;

  /**
   * Constructs a TrackChooserView for the specified tracker panel.
   *
   * @param panel the tracker panel
   */
  public PlotTView(TrackerPanel panel) {
    super(panel);
    icon = new ImageIcon(
        Tracker.class.getResource("resources/images/plot.gif")); //$NON-NLS-1$
  }

  /**
   * Gets the name of the view
   *
   * @return the name
   */
  public String getViewName() {
    return TrackerRes.getString("TFrame.View.Plot"); //$NON-NLS-1$
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
   * Creates a view for the specified track
   *
   * @param track the track
   * @return the track view
   */
  protected TrackView createTrackView(TTrack track) {
    return new PlotTrackView(track, trackerPanel);
  }

  /**
   * Refreshes the popup menus.
   */
  @Override
  protected void refreshMenus() { 
  	for (TrackView next: trackViews.values()) {
  		PlotTrackView plots = (PlotTrackView)next;
  		for (TrackPlottingPanel panel: plots.plots) {
  			panel.buildPopupmenu();
  		}
  	}
  }
  
  /**
   * Overrides TrackChooserTView method.
   *
   * @param track the track to be selected
   */
  @Override
  public void setSelectedTrack(TTrack track) {
  	if (track == null) {
    	noDataLabel.setText(TrackerRes.getString("PlotTView.Label.NoData")); //$NON-NLS-1$
  	}
  	super.setSelectedTrack(track);
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
      PlotTView view = (PlotTView)obj;
      TTrack track = view.getSelectedTrack();
      if (track != null) {
        control.setValue("selected_track", track.getName()); //$NON-NLS-1$
        java.util.ArrayList<TrackView> list = new java.util.ArrayList<TrackView>();
        for (TrackView next: view.trackViews.values()) {
        	if (next.isCustomState())
        		list.add(next);
        }
//        list.addAll(view.trackViews.values());
        if (!list.isEmpty())
        	control.setValue("track_views", list); //$NON-NLS-1$
      }
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
      PlotTView view = (PlotTView)obj;
      TTrack track = view.getTrack(control.getString("selected_track")); //$NON-NLS-1$
      if (track != null) {
      	view.setSelectedTrack(track);
      	// following code is for legacy xml only
        PlotTrackView trackView = (PlotTrackView)view.getTrackView(track);
        TrackPlottingPanel[] plots = trackView.plots;
        for (int i = 0; i < plots.length; i++) {
        	XMLControl child = control.getChildControl("plot"+i); //$NON-NLS-1$ 
        	if (child != null) {
        		child.loadObject(plots[i]);
        	}
        	else {
            trackView.setPlotCount(Math.max(1, i));
            break;
        	}
        }
      	// end legacy code
      }
	    // load the track_views property, if any
      java.util.List<Object> props = control.getPropertyContent();
	    for (int i = 0; i < props.size(); i++) {
	      XMLProperty prop = (XMLProperty)props.get(i);
	      if (prop.getPropertyName().equals("track_views")) { //$NON-NLS-1$
	      	XMLControl[] controls = prop.getChildControls();
	      	for (int j = 0; j < controls.length; j++) {
	      		// get name of track, find its track view and load it
	      		String trackName = controls[j].getString("track"); //$NON-NLS-1$
	          track = view.getTrack(trackName);
	          if (track != null) {
	            PlotTrackView trackView = (PlotTrackView)view.getTrackView(track);
	            controls[j].loadObject(trackView);
	          }
	      	}
	      	break;
	      }
	    }
      return obj;
    }
  }
}
