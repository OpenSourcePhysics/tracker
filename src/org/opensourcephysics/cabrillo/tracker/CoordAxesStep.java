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

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;

/**
 * This is a Step for a CoordAxes. It is used for displaying the axes and for
 * setting the origin, angle and/or scale of an ImageCoordSystem.
 *
 * @author Douglas Brown
 */
public class CoordAxesStep extends Step {

  // instance fields
  private Origin origin;
  private Handle handle;
  private boolean originEnabled = true;
  private boolean handleEnabled = true;
  private Map<TrackerPanel, Shape> handleShapes = new HashMap<TrackerPanel, Shape>();
  private Shape[] fillShapes = new Shape[2];
  private GeneralPath path = new GeneralPath();

  /**
   * Constructs an AxesStep.
   *
   * @param track the track
   * @param n the frame number
   */
  public CoordAxesStep(CoordAxes track, int n) {
    super(track, n);
    origin = new Origin();
    origin.setCoordsEditTrigger(true);
    handle = new Handle();
    handle.setCoordsEditTrigger(true);
    points = new TPoint[] {origin, handle}; // origin is "default" point
    screenPoints = new Point[1];
  }

  /**
   * Gets the origin.
   *
   * @return the origin
   */
  public TPoint getOrigin() {
    return origin;
  }

  /**
   * Gets the handle.
   *
   * @return the origin
   */
  public TPoint getHandle() {
    return handle;
  }

  /**
  /**
   * Enables and disables the interactivity of the origin.
   *
   * @param enabled <code>true</code> to enable the origin
   */
  public void setOriginEnabled(boolean enabled) {
    originEnabled = enabled;
  }

  /**
   * Gets whether the origin is enabled.
   *
   * @return <code>true</code> if the origin is enabled
   */
  public boolean isOriginEnabled() {
    return originEnabled;
  }

  /**
   * Enables and disables the interactivity of the handle.
   *
   * @param enabled <code>true</code> to enable the handle
   */
  public void setHandleEnabled(boolean enabled) {
    handleEnabled = enabled;
  }

  /**
   * Gets whether the handle is enabled.
   *
   * @return <code>true</code> if the handle is enabled
   */
  public boolean isHandleEnabled() {
    return handleEnabled;
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
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    setHitRectCenter(xpix, ypix);
    AutoTracker autoTracker = track.trackerPanel==null? null: track.trackerPanel.getAutoTracker();
    if (handleEnabled) {
      Shape hitShape = handleShapes.get(trackerPanel);
      if (hitShape != null && hitShape.intersects(hitRect)) {
    		if (autoTracker!=null && autoTracker.getTrack()==track && track.getTargetIndex()==1) {
	    		int n = track.trackerPanel.getFrameNumber();
	    		AutoTracker.FrameData frame = autoTracker.getFrame(n);
	    		if (frame==frame.getKeyFrame()) {
	    			return null;
	    		}
    		}      	
        return handle;
      }
    }
    if (originEnabled && !track.isLocked()) {
    	Interactive ia = super.findInteractive(panel, xpix, ypix);
    	if (ia==origin) {
    		if (autoTracker!=null && autoTracker.getTrack()==track && track.getTargetIndex()==0) {
	    		int n = track.trackerPanel.getFrameNumber();
	    		AutoTracker.FrameData frame = autoTracker.getFrame(n);
	    		if (frame==frame.getKeyFrame()) {
	    			return null;
	    		}
    		}      	
    		return origin;
    	}
    }
    return null;
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
    // draw the axes
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    Graphics2D g = (Graphics2D)_g;
    getMark(trackerPanel).draw(g, false); // no highlight
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
      // set origin location to coords origin
      ImageCoordSystem coords = trackerPanel.getCoords();
      int n = trackerPanel.getFrameNumber();
      if (track.trackerPanel != null) n = track.trackerPanel.getFrameNumber();
      double x = coords.getOriginX(n);
      double y = coords.getOriginY(n);
      origin.setLocation(x, y);
      // get default axes shape and handle hit shape (positive x-axis)
      Point p0 = screenPoints[0] = origin.getScreenPosition(trackerPanel);
      fillShapes[0] = footprint.getShape(screenPoints);
      path.reset();
      path.moveTo(p0.x + 15, p0.y);
      path.lineTo(p0.x + 3000, p0.y);
      Shape hitShape = path;
      // rotate axes and x-axis hit shape about origin if drawing in image space
      if (trackerPanel.isDrawingInImageSpace()) {
        double angle = coords.getAngle(n);
        transform.setToRotation(-angle, p0.x, p0.y);
        fillShapes[0] = transform.createTransformedShape(fillShapes[0]);
        hitShape = transform.createTransformedShape(hitShape);
      }
      handleShapes.put(trackerPanel, hitShape);
      // get selected point shape, if any
      if (selection == origin) {
        transform.setToTranslation(p0.x, p0.y);
        fillShapes[1] = transform.createTransformedShape(selectionShape);
      }
      else if (selection == handle) {
        Point p1 = handle.getScreenPosition(trackerPanel);
        transform.setToTranslation(p1.x, p1.y);
        fillShapes[1] = transform.createTransformedShape(selectionShape);
      }
      else fillShapes[1] = null;
      // create mark to draw fillShapes
      final Color color = footprint.getColor();
      final TrackerPanel panel = trackerPanel;
      mark = new Mark() {
        public void draw(Graphics2D g, boolean highlighted) {
          Paint gpaint = g.getPaint();
          g.setPaint(color);
          g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);
          for (int i = 0; i < 2; i++) {
            if (fillShapes[i] != null)
              g.fill(fillShapes[i]);
          }
          g.setPaint(gpaint);
        }

        public Rectangle getBounds(boolean highlighted) {
          Rectangle bounds = panel.getBounds();
          if (fillShapes[1] != null) {
            bounds.add(fillShapes[1].getBounds());
          }
          return bounds;
        }
      };
      marks.put(trackerPanel, mark);
    }
    return mark;
  }

  /**
   * Overrides Step getPointIndex method.
   *
   * @return the index, or -1 if not found
   */
  public int getPointIndex(TPoint p) {
  	int i = super.getPointIndex(p);
  	if (i==-1) {
  		if (p instanceof CoordAxes.OriginPoint) return 0;
  		if (p instanceof CoordAxes.AnglePoint) return 1;
  	}
    return i;
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
   * @return a clone of this step
   */
  public Object clone() {
    CoordAxesStep step = (CoordAxesStep)super.clone();
    if (step != null) {
      step.handleShapes = new HashMap<TrackerPanel, Shape>();
    }
    return step;
  }

  /**
   * Returns a String describing this.
   *
   * @return a descriptive string
   */
  public String toString() {
    return "CoordAxesStep " + n; //$NON-NLS-1$
  }

  /**
   * Gets the step length.
   *
   * @return the length of the points array
   */
  public static int getLength() {
    return 2;
  }

  // ______________________ inner Origin class ________________________

  /**
   * Inner class used to set the origin.
   */
  class Origin extends TPoint {

    private double lastX, lastY;
    
    /**
     * Overrides TPoint setXY method.
     *
     * @param x the x position
     * @param y the y position
     */
    public void setXY(double x, double y) {
      if (track.isLocked()) return;
      if (isAdjusting()) {
      	lastX = x;
      	lastY = y;
      }
      super.setXY(x, y);
      TrackerPanel panel = track.trackerPanel;
      if (panel != null) {
        ImageCoordSystem coords = track.trackerPanel.getCoords();
        coords.setAdjusting(isAdjusting());
	      int n = panel.getFrameNumber();
	      coords.setOriginXY(n, x, y);
	      CoordAxes coordAxes = (CoordAxes)track;
	      coordAxes.xField.setValue(coords.getOriginX(n));
	      coordAxes.yField.setValue(coords.getOriginY(n));
      }
      if (isAdjusting()) {
      	repaint();
      }
    }
    
    /**
     * Overrides TPoint method.
     *
     * @param adjusting true if being dragged
     */
    public void setAdjusting(boolean adjusting) {
    	boolean wasAdjusting = isAdjusting();
    	super.setAdjusting(adjusting);
    	if (wasAdjusting && !adjusting) {
    		setXY(lastX, lastY);
    		track.firePropertyChange("step", null, track.trackerPanel.getFrameNumber()); //$NON-NLS-1$
    	}
    }

}

  //______________________ inner Handle class ________________________

  class Handle extends TPoint {

    // instance fields
    private double angleIncrement = 0;
    protected Point2D.Double p = new Point2D.Double();
    private double lastX, lastY;

    /**
     * Overrides TPoint setXY method to set the angle of the x axis.
     *
     * @param x the x position
     * @param y the y position
     */
    public void setXY(double x, double y) {
      if (track.isLocked()) return;
      CoordAxes coordAxes = (CoordAxes)track;
      if (coordAxes.trackerPanel == null) {
        super.setXY(x, y);
      	return;
      }
      if (angleIncrement >= Math.PI/180) { // 1 degree of arc
        // place handle at same distance from origin at closest permitted angle
        p.setLocation(x, y);
        double d = origin.distance(p);
        double theta = origin.angle(p);
        int i = Math.round((float)(theta/angleIncrement));
        theta = i * angleIncrement;
        x = origin.getX() + d * Math.cos(theta);
        y = origin.getY() + d * Math.sin(theta);
      }
      if (isAdjusting()) {
      	lastX = x;
      	lastY = y;
      }
      super.setXY(x, y);
      double cos = origin.cos(this);
      double sin = origin.sin(this);
      ImageCoordSystem coords = coordAxes.trackerPanel.getCoords();
      coords.setAdjusting(isAdjusting());
      int n = coordAxes.trackerPanel.getFrameNumber();
      coords.setCosineSine(n, cos, sin);
      coordAxes.angleField.setValue(coords.getAngle(n));
      angleIncrement = 0;
      if (isAdjusting()) {
      	repaint();
      }
    }

    /**
     * Overrides TPoint setScreenPosition method.
     *
     * @param x the screen x coordinate
     * @param y the screen y coordinate
     * @param vidPanel the video panel
     * @param e the input event making the request
     */
    public void setScreenPosition(int x, int y,
                                  VideoPanel vidPanel,
                                  InputEvent e) {
      if (e == null) {
        angleIncrement = 0;
      }
      else if (e.isShiftDown()) {
        angleIncrement = Math.PI/36; // 5 degrees
      }
      else {
        angleIncrement = 0;
      }
      setScreenPosition(x, y, vidPanel);
    }

    /**
     * Overrides TPoint showCoordinates method so handle position can be set
     * to mouse position when first selecting this handle
     *
     * @param vidPanel the video panel
     */
    public void showCoordinates(VideoPanel vidPanel) {
      if (vidPanel instanceof TrackerPanel) {
        TrackerPanel trackerPanel = (TrackerPanel)vidPanel;
        if (!(this == trackerPanel.getSelectedPoint())) {
          // start by setting location to mouse point
          setLocation(vidPanel.getMouseX(), vidPanel.getMouseY());
          // then move to nearest point on x-axis
          Point2D p = getWorldPosition(vidPanel);
          p.setLocation(p.getX(), 0); // move to y = 0
          int n = vidPanel.getFrameNumber();
          AffineTransform toImage = vidPanel.getCoords().getToImageTransform(n);
          toImage.transform(p, p);
          setLocation(p);
        }
      }
      super.showCoordinates(vidPanel);
    }
    
    /**
     * Overrides TPoint method.
     *
     * @param adjusting true if being dragged
     */
    public void setAdjusting(boolean adjusting) {
    	boolean wasAdjusting = isAdjusting();
    	super.setAdjusting(adjusting);
    	if (wasAdjusting && !adjusting) {
    		setXY(lastX, lastY);
    	}
    }
  }
}

