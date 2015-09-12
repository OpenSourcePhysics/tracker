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

import java.awt.Component;
import java.beans.*;
import java.util.*;

import javax.swing.*;

/**
 * This displays a view of a single track on a TrackerPanel.
 *
 * @author Douglas Brown
 */
public abstract class TrackView extends JScrollPane
                                implements PropertyChangeListener {

  // instance fields
  protected TTrack track;
  protected TrackerPanel trackerPanel;
  protected ArrayList<Component> toolbarComponents = new ArrayList<Component>();

  // constructor
  protected TrackView(TTrack track, TrackerPanel panel) {
    this.track = track;
    trackerPanel = panel;
    trackerPanel.addPropertyChangeListener("selectedpoint", this); //$NON-NLS-1$
  }

  abstract void refresh(int stepNumber);

  abstract void refreshGUI();

  abstract void dispose();

  abstract boolean isCustomState();

  abstract JButton getViewButton();
  
  public String getName() {
  	if (track==null) return null;
    return track.getName();
  }

  Icon getIcon() {
  	if (track==null) return null;
    return track.getFootprint().getIcon(21, 16);
  }

  TTrack getTrack() {
    return track;
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
   * Responds to property change events. TrackView receives the following
   * events: "step" or "steps" from the track.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    if (name.equals("step")) {               // from track //$NON-NLS-1$
      Integer i = (Integer)e.getNewValue();
      refresh(i);
    }
    else if (name.equals("steps")) {         // from particle model tracks //$NON-NLS-1$
      refresh(trackerPanel.getFrameNumber());
    }
    else if (name.equals("selectedpoint")) { // from tracker panel //$NON-NLS-1$
      Step step = trackerPanel.getSelectedStep();
      if (step != null && trackerPanel.getSelectedTrack() == track) {
        refresh(step.getFrameNumber());
      }
      else {
        refresh(trackerPanel.getFrameNumber());
      }
    }
  }
  
  protected boolean isRefreshEnabled() {
  	return trackerPanel.isAutoRefresh;
  }

}
