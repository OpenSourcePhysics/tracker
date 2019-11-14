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

import java.awt.*;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.media.core.*;
import java.util.*;

/**
 * This is a Step that represents multiple positions. It is used by ParticleDataTracks.
 *
 * @author Douglas Brown
 */
public class MultiPositionStep extends PositionStep {
	
  // instance fields
	ParticleDataTrack dataTrack;

  /**
   * Constructs a MultiPositionStep with specified image coordinates.
   *
   * @param track the ParticleDataTrack
   * @param n the frame number
   * @param x the x coordinate
   * @param y the y coordinate
   */
  public MultiPositionStep(ParticleDataTrack track, int n, double x, double y) {
    super(track, n, x, y);
    dataTrack = track;
  }

//  /**
//   * Overrides Step draw method.
//   *
//   * @param panel the drawing panel requesting the drawing
//   * @param _g the graphics context on which to draw
//   */
//  public void draw(DrawingPanel panel, Graphics _g) {
//  	super.draw(panel, _g);
//    if (panel instanceof TrackerPanel) {
//      TrackerPanel trackerPanel = (TrackerPanel) panel;
//    }
//  }
//
  /**
   * Overrides Step getMark method.
   *
   * @param trackerPanel the tracker panel
   * @return the mark
   */
  protected Mark getMark(TrackerPanel trackerPanel) {
    Mark mark = marks.get(trackerPanel);
    if (mark == null) {
    	Mark aMark = null;
    	if (dataTrack.modelFootprintVisible) {
		    // get mark from modelFootprint
		    ArrayList<ParticleDataTrack> tracks = dataTrack.allPoints();
		    TPoint[] points = new TPoint[tracks.size()];
		    for (int i=0; i<points.length; i++) {
		    	ParticleDataTrack next = tracks.get(i);
		    	Step step = next.getStep(this.getFrameNumber());
		    	points[i] = step==null? null: step.getPoints()[0];
		    }
		    Point[] screenPoints = new Point[points.length];
		    for (int i = 0; i < points.length; i++) {
		    	if (points[i]==null || Double.isNaN(points[i].x) || Double.isNaN(points[i].y)) {
		    		screenPoints[i] = null;
		    	}
		    	else 
		    		screenPoints[i] = points[i].getScreenPosition(trackerPanel);
		    }
		    aMark = dataTrack.getModelFootprint().getMark(screenPoints);
    	}
      final Mark modelMark = aMark;
      final Mark positionMark = super.getMark(trackerPanel);
      
      mark = new Mark() {
        public void draw(Graphics2D g, boolean highlighted) {
        	if (!valid) {
        		return;
        	}
          if (modelMark!=null) modelMark.draw(g, highlighted);
          positionMark.draw(g, highlighted);
        }
        public Rectangle getBounds(boolean highlighted) {
        	Rectangle bounds = positionMark.getBounds(highlighted);
        	if (modelMark!=null) bounds.union(modelMark.getBounds(highlighted));
          return bounds;
        }
      };
      marks.put(trackerPanel, mark);
    }
    return mark;
  }

  /**
   * Overrides Step getBounds method.
   *
   * @param trackerPanel the tracker panel drawing the step
   * @return the bounding rectangle
   */
  public Rectangle getBounds(TrackerPanel trackerPanel) {
    Rectangle bounds = getMark(trackerPanel).getBounds(false);
    return bounds;
  }

  /**
   * Clones this Step.
   *
   * @return a cloned step
   */
  public Object clone() {
    MultiPositionStep step = (MultiPositionStep)super.clone();
//    if (step != null)
//      step.points[0] = step.p = step.new Position(p.getX(), p.getY());
    return step;
  }

  /**
   * Returns a String describing this.
   *
   * @return a descriptive string
   */
  public String toString() {
    return "MultiPositionStep " + n + " [" + format.format(p.x) //$NON-NLS-1$ //$NON-NLS-2$
                               + ", " + format.format(p.y) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Returns an ObjectLoader to save and load data for this class.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load data for this class.
   */
  static class Loader implements XML.ObjectLoader {

    /**
     * Saves an object's data to an XMLControl.
     *
     * @param control the control to save to
     * @param obj the object to save
     */
    public void saveObject(XMLControl control, Object obj) {
      PositionStep step = (PositionStep) obj;
      control.setValue("x", step.p.x); //$NON-NLS-1$
      control.setValue("y", step.p.y); //$NON-NLS-1$
    }

    /**
     * Creates a new object with data from an XMLControl.
     *
     * @param control the control
     * @return the newly created object
     */
    public Object createObject(XMLControl control) {
    	// this loader is not intended to be used to create new steps,
    	// but only for undo/redo step edits.
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
    	PositionStep step = (PositionStep)obj;
    	double x = control.getDouble("x"); //$NON-NLS-1$
      double y = control.getDouble("y"); //$NON-NLS-1$
      step.p.setXY(x, y);
    	return obj;
    }
  }
}

