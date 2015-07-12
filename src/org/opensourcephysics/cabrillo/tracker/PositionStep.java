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

import java.awt.*;
import java.awt.geom.*;
import java.awt.font.*;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;

import java.util.*;

/**
 * This is a Step that represents a position. It is used by PointMass tracks.
 *
 * @author Douglas Brown
 */
public class PositionStep extends Step {
	
	private static Point[] twoPoints = new Point[2];

  // instance fields
  protected Position p;
  protected boolean labelVisible = true;
  protected boolean rolloverVisible = false;
  protected Map<TrackerPanel, TextLayout> textLayouts 
  		= new HashMap<TrackerPanel, TextLayout>();
  protected Map<TrackerPanel, Rectangle> layoutBounds 
  		= new HashMap<TrackerPanel, Rectangle>();
//  protected Font font;

  /**
   * Constructs a PositionStep with specified image coordinates.
   *
   * @param track the PointMass track
   * @param n the frame number
   * @param x the x coordinate
   * @param y the y coordinate
   */
  public PositionStep(PointMass track, int n, double x, double y) {
    super(track, n);
    p = new Position(x, y);
    p.setTrackEditTrigger(true);
    points = new TPoint[] {p};
    screenPoints = new Point[getLength()];
  }

  /**
   * Gets the position TPoint.
   *
   * @return the position TPoint
   */
  public Position getPosition() {
    return p;
  }

  /**
   * Gets the label visibility.
   *
   * @return <code>true</code> if label is visible
   */
  public boolean isLabelVisible() {
    return labelVisible;
  }

  /**
   * Sets the label visibility.
   *
   * @param visible <code>true</code> to make label visible
   */
  public void setLabelVisible(boolean visible) {
    labelVisible = visible;
  }

  /**
   * Gets the rollover visibility.
   *
   * @return <code>true</code> if labels are visible on rollover only
   */
  public boolean isRolloverVisible() {
    return rolloverVisible;
  }

  /**
   * Sets the rollover visibility.
   *
   * @param visible <code>true</code> to make labels visible on rollover only
   */
  public void setRolloverVisible(boolean visible) {
    rolloverVisible = visible;
  }

  /**
   * Overrides Step draw method.
   *
   * @param panel the drawing panel requesting the drawing
   * @param _g the graphics context on which to draw
   */
  public void draw(DrawingPanel panel, Graphics _g) {
		if (track.trackerPanel==panel) {
			AutoTracker autoTracker = track.trackerPanel.getAutoTracker();
			if (autoTracker.isInteracting(track)) return;
		}
    if (panel instanceof TrackerPanel) {
      TrackerPanel trackerPanel = (TrackerPanel) panel;
      super.draw(trackerPanel, _g);
      Graphics2D g = (Graphics2D) _g;
      if (isLabelVisible()) {
        TextLayout layout = textLayouts.get(trackerPanel);
        if (layout==null) return;
        Point p = getLayoutPosition(trackerPanel);
        Paint gpaint = g.getPaint();
        Font gfont = g.getFont();
        g.setPaint(footprint.getColor());
        g.setFont(textLayoutFont);
        layout.draw(g, p.x, p.y);
        g.setPaint(gpaint);
        g.setFont(gfont);
      }
    }
  }

  /**
   * Overrides Step findInteractive method.
   *
   * @param panel the drawing panel
   * @param xpix the x pixel position
   * @param ypix the y pixel position
   * @return the TPoint that is hit, or null
   */
  public Interactive findInteractive(
         DrawingPanel panel, int xpix, int ypix) {
    Interactive ia = super.findInteractive(panel, xpix, ypix);
    if (rolloverVisible) {
      if (ia != null && !labelVisible) {
        labelVisible = true;
        this.repaint();
      }
      if (ia == null && labelVisible) {
        labelVisible = false;
        this.repaint();
      }
    }
    return ia;
  }

  /**
   * Overrides Step getMark method.
   *
   * @param trackerPanel the tracker panel
   * @return the mark
   */
  protected Mark getMark(TrackerPanel trackerPanel) {
    Mark mark = marks.get(trackerPanel);
    TPoint selection = null;
    if (mark == null) {
      selection = trackerPanel.getSelectedPoint();
      Point p = null; // draws this step as "selected" shape if not null
      valid = true; // true if step is not NaN
      for (int n = 0; n < points.length; n++) {
      	if (!valid) continue;
      	// determine if point is valid (ie not NaN)
      	valid = valid 
      			&& !Double.isNaN(points[n].getX()) 
      			&& !Double.isNaN(points[n].getY());
        screenPoints[n] = points[n].getScreenPosition(trackerPanel);
        // step is "selected" if trackerPanel selectedPoint is position or selectedSteps contains this step 
        if (valid && (selection==points[n] || trackerPanel.selectedSteps.contains(this))) {
        	p = screenPoints[n];
        }
      }
      if (p == null) {
      	if (footprint instanceof PositionVectorFootprint) {     		
      		twoPoints[0] = screenPoints[0];
      		twoPoints[1] = trackerPanel.getSnapPoint().getScreenPosition(trackerPanel);
        	mark = footprint.getMark(twoPoints);
      	}
      	else mark = footprint.getMark(screenPoints);
      }
      else {
        transform.setToTranslation(p.x, p.y);
        final Color color = footprint.getColor();
        final Shape selectedShape
          = transform.createTransformedShape(selectionShape);
        mark = new Mark() {
          public void draw(Graphics2D g, boolean highlighted) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            Paint gpaint = g.getPaint();
            g.setPaint(color);
            g.fill(selectedShape);
            g.setPaint(gpaint);
          }

          public Rectangle getBounds(boolean highlighted) {
            return selectedShape.getBounds();
          }
        };
      }
      final Mark theMark = mark;
      mark = new Mark() {
        public void draw(Graphics2D g, boolean highlighted) {
        	if (!valid) {
        		return;
        	}
          theMark.draw(g, highlighted);
        }
        public Rectangle getBounds(boolean highlighted) {
          return theMark.getBounds(highlighted);
        }
      };
      marks.put(trackerPanel, mark);
      // get new text layout
      String s = ""; //$NON-NLS-1$
      VideoClip clip = trackerPanel.getPlayer().getVideoClip();
      if (clip.getStepCount() != 1) {
        s += clip.frameToStep(getFrameNumber());
      }
      if (s.length() == 0) s = " "; //$NON-NLS-1$
      TextLayout layout = new TextLayout(s, textLayoutFont, frc);
      textLayouts.put(trackerPanel, layout);
      // get layout position (bottom left corner of text)
      p = getLayoutPosition(trackerPanel);
      Rectangle bounds = layoutBounds.get(trackerPanel);
      if (bounds == null) {
        bounds = new Rectangle();
        layoutBounds.put(trackerPanel, bounds);
      }
      Rectangle2D rect = layout.getBounds();
      // set bounds (top left corner and size)
      bounds.setRect(p.x, p.y - rect.getHeight(),
                     rect.getWidth(), rect.getHeight());
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
    Rectangle layoutRect = layoutBounds.get(trackerPanel);
    if (layoutRect!=null) {
    	bounds.add(layoutRect);
    }
    return bounds;
  }

  /**
   * Clones this Step.
   *
   * @return a cloned step
   */
  public Object clone() {
    PositionStep step = (PositionStep)super.clone();
    if (step != null)
      step.points[0] = step.p = step.new Position(p.getX(), p.getY());
    step.textLayouts = new HashMap<TrackerPanel, TextLayout>();
    step.layoutBounds = new HashMap<TrackerPanel, Rectangle>();
    return step;
  }

  /**
   * Returns a String describing this.
   *
   * @return a descriptive string
   */
  public String toString() {
    return "PositionStep " + n + " [" + format.format(p.x) //$NON-NLS-1$ //$NON-NLS-2$
                               + ", " + format.format(p.y) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
  }

//____________________ inner Position class ______________________

  protected class Position extends TPoint {
  	
    /**
     * Constructs a Position with specified image coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Position(double x, double y) {
      super(x, y);
    }

    /**
     * Overrides TPoint setXY method.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public void setXY(double x, double y) {
      if (track.isLocked()) return;
      super.setXY(x, y);
      repaint();
      ((PointMass)track).updateDerivatives(n);
      track.support.firePropertyChange("step", null, new Integer(n)); //$NON-NLS-1$
    }

    /**
     * Overrides TPoint showCoordinates method.
     *
     * @param vidPanel the video panel
     */
    public void showCoordinates(VideoPanel vidPanel) {
      // put values into pointmass x and y fields
      Point2D p = getWorldPosition(vidPanel);
      track.xField.setValue(p.getX());
      track.yField.setValue(p.getY());
      track.magField.setValue(p.distance(0, 0));
      double theta = Math.atan2(p.getY(), p.getX());
      track.angleField.setValue(theta);
      super.showCoordinates(vidPanel);
    }

    /**
     * Overrides TPoint getFrameNumber method.
     *
     * @param vidPanel the video panel being drawn
     * @return the frame number
     */
    public int getFrameNumber(VideoPanel vidPanel) {
      return n;
    }
    
    /**
     * Sets the position without triggering any events.
     *
     * @param point the image position
     */
    void setPosition(Point2D point) {
      this.x = point.getX();    	
      this.y = point.getY();    	
    }
    
    /**
     * Sets the adjusting flag.
     *
     * @param adjusting true if being dragged
     */
    @Override
    public void setAdjusting(boolean adjusting) {
    	super.setAdjusting(adjusting);
      track.support.firePropertyChange("adjusting", null, adjusting); //$NON-NLS-1$    	
    }

  }

  /**
   * Gets TextLayout screen position.
   *
   * @param trackerPanel the tracker panel
   * @return the screen position point
   */
  protected Point getLayoutPosition(TrackerPanel trackerPanel) {
    Point pt = p.getScreenPosition(trackerPanel);
    pt.setLocation(pt.x - 4 - textLayoutFont.getSize(), pt.y - 6);
    return pt;
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

