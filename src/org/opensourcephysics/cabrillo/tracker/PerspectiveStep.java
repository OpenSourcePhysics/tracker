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

import java.awt.Point;

import org.opensourcephysics.media.core.*;

/**
 * This is a Step for a PerspectiveTrack.
 *
 * @author Douglas Brown
 */
public class PerspectiveStep extends Step {
	
  /**
   * Constructs a PerspectiveStep with specified image coordinates.
   *
   * @param track the PerspectiveTrack track
   * @param n the frame number
   * @param x the x coordinate
   * @param y the y coordinate
   */
  public PerspectiveStep(PerspectiveTrack track, int n, double x, double y) {
    super(track, n);
    points = new TPoint[] {new Corner(), new Corner(), new Corner(), new Corner()};
    screenPoints = new Point[getLength()];
  }

  /**
   * Gets the index of a position or perspective filter corner point. 
   * 
   * @param p the point
   * @return the index, or -1 if not found
   */
  public int getPointIndex(TPoint p) {
  	if (p instanceof PerspectiveFilter.Corner) {
	  	PerspectiveFilter.Corner corner = (PerspectiveFilter.Corner)p;
	  	PerspectiveTrack ptrack = (PerspectiveTrack)track;
	  	int i = ptrack.filter.getCornerIndex(corner);
	  	if (i<4) return i;
  	}
  	for (int i=0; i<points.length; i++) {
  		if (p==points[i]) return i;
  	}
  	return -1;
  }

  /**
   * Gets the default point.
   *
   * @return the default TPoint
   */
  public TPoint getDefaultPoint() {
  	PerspectiveTrack ptrack = (PerspectiveTrack)track;
  	int index = ptrack.getTargetIndex();
    return ptrack.filter.getCorner(index);
  }

  /**
   * Overrides Step getMark method.
   *
   * @param trackerPanel the tracker panel
   * @return the mark
   */
  protected Mark getMark(TrackerPanel trackerPanel) {
    Mark mark = marks.get(trackerPanel);
    if (mark == null) {
      mark = footprint.getMark(screenPoints);
      marks.put(trackerPanel, mark);
    }
    return mark;
  }

  /**
   * Clones this Step.
   *
   * @return a cloned step
   */
  public Object clone() {
    PerspectiveStep step = (PerspectiveStep)super.clone();
    step.points = new TPoint[] {new Corner(), new Corner(), new Corner(), new Corner()};
    return step;
  }

  /**
   * Returns a String describing this step.
   *
   * @return a descriptive string
   */
  public String toString() {
    return "PerspectiveStep"; //$NON-NLS-1$
  }

  /**
   * A corner TPoint class to fire property change events
   */
  public class Corner extends TPoint {
  	
  	public void setXY(double x, double y) {
  		super.setLocation(x, y);
	  	PerspectiveTrack ptrack = (PerspectiveTrack)track;
	  	if (ptrack.trackerPanel!=null) {
	  		int n = ptrack.trackerPanel.getFrameNumber();
	  	  ptrack.support.firePropertyChange("step", null, n); //$NON-NLS-1$	  		
	  	}
  	}
  }
  
}

