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

import org.opensourcephysics.media.core.*;

/**
 * This is a Step for a OffsetOrigin. It is used for
 * setting the origin of an ImageCoordSystem.
 *
 * @author Douglas Brown
 */
public class OffsetOriginStep extends Step {

  // instance fields
  private OffsetOrigin offset;
  private Position p;
  protected double worldX, worldY;

  /**
   * Constructs a OffsetOriginStep with specified image coordinates.
   *
   * @param track the offset origin
   * @param n the frame number
   * @param x the image x coordinate
   * @param y the image y coordinate
   */
  public OffsetOriginStep(OffsetOrigin track, int n, double x, double y) {
    super(track, n);
    offset = track;
    screenPoints = new Point[getLength()];
    points = new TPoint[getLength()];
    p = new Position(x, y); // sets initial worldX and worldY
    points[0] = p;
  }

  /**
   * Gets the position.
   *
   * @return the position
   */
  public Position getPosition() {
    return p;
  }

  /**
   * Sets the world coordinates. This moves the origin so the
   * image location does not change.
   *
   * @param x the world x coordinate
   * @param y the world y coordinate
   */
  public void setWorldXY(double x, double y) {
    if (track.isLocked()) return;
    
    if (offset.isFixedCoordinates()) {
    	OffsetOriginStep step = (OffsetOriginStep)offset.steps.getStep(0);
      step.worldX = x;
      step.worldY = y;
	    step.erase();
	    offset.refreshStep(OffsetOriginStep.this); // sets properties of this step
    }
    else {
      worldX = x;
      worldY = y;
    	offset.keyFrames.add(n);
  	}            
    
    if (offset.trackerPanel == null) return;
    ImageCoordSystem coords = offset.trackerPanel.getCoords();
    int n = offset.trackerPanel.getFrameNumber();
    // get the current image position of the origin
    double x0 = coords.getOriginX(n);
    double y0 = coords.getOriginY(n);
    // get the current image position of the world coordinates
    double x1 = coords.worldToImageX(n, x, y);
    double y1 = coords.worldToImageY(n, x, y);
    // translate the origin
    coords.setOriginXY(n, x0+p.x-x1, y0+p.y-y1);
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
      ImageCoordSystem coords = trackerPanel.getCoords();
      int n = trackerPanel.getFrameNumber();
      // set position location to image position of world coordinates
      double x = coords.worldToImageX(n, worldX, worldY);
      double y = coords.worldToImageY(n, worldX, worldY);
      p.setLocation(x, y);
      // get point shape
      Shape shape;
      selection = trackerPanel.getSelectedPoint();
      Point pt = points[0].getScreenPosition(trackerPanel);
      if (selection == points[0]) { // point is selected
        transform.setToTranslation(pt.x, pt.y);
        shape = transform.createTransformedShape(selectionShape);
      }
      else { // point is not selected
        shape = footprint.getShape(new Point[] {pt});
      }
      // create mark
      final Color color = footprint.getColor();
      final Shape fillShape = shape;
      mark = new Mark() {
        public void draw(Graphics2D g, boolean highlighted) {
          Paint gpaint = g.getPaint();
          g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);
          g.setPaint(color);
          g.fill(fillShape);
          g.setPaint(gpaint);
        }

        public Rectangle getBounds(boolean highlighted) {
          return fillShape.getBounds();
        }
      };
      marks.put(trackerPanel, mark);
    }
    return mark;
  }

  /**
   * Clones this Step.
   *
   * @return a clone of this step
   */
  public Object clone() {
    OffsetOriginStep step = (OffsetOriginStep)super.clone();
    step.points[0] = step.p = step.new Position(p.x, p.y);
    return step;
  }

  /**
   * Returns a String describing this step.
   *
   * @return a descriptive string
   */
  public String toString() {
    String s = "Offset Origin Step " + n //$NON-NLS-1$
           + " [" + format.format(worldX) //$NON-NLS-1$
           + ", " + format.format(worldY) //$NON-NLS-1$
           + "]"; //$NON-NLS-1$
    return s;
  }

  //______________________ inner Position class ________________________

  /**
   * A class to represent the position of the offset origin.
   */
  public class Position extends TPoint {

    private double lastX, lastY;
    
    /**
     * Constructs a position with specified image coordinates,
     * and transforms those coordinates to set the world coordinates.
     *
     * @param x the image x coordinate
     * @param y the image y coordinate
     */
    public Position(double x, double y) {
      super.setXY(x, y);
      setCoordsEditTrigger(true);
      if (offset.trackerPanel == null) return;
      // set the world coordinates using x and y
      ImageCoordSystem coords = offset.trackerPanel.getCoords();
      int n = offset.trackerPanel.getFrameNumber();
      worldX = coords.imageToWorldX(n, x, y);
      worldY = coords.imageToWorldY(n, x, y);
    }

    /**
     * Overrides TPoint setXY method. This moves the origin so the world
     * coordinates do not change.
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
      double dx = x - getX();
      double dy = y - getY();
      super.setXY(x, y);
      if (offset.trackerPanel == null) return;      
      ImageCoordSystem coords = offset.trackerPanel.getCoords();
      coords.setAdjusting(isAdjusting());
      int n = offset.trackerPanel.getFrameNumber();
      // get the current image position of the origin
      double x0 = coords.getOriginX(n);
      double y0 = coords.getOriginY(n);
      // translate the origin
      coords.setOriginXY(n, x0 + dx, y0 + dy);
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
    		track.firePropertyChange("step", null, n); //$NON-NLS-1$
    	}
    }

    /**
     * Overrides TPoint showCoordinates method. This updates the values
     * of the x and y fields.
     *
     * @param vidPanel the video panel
     */
    public void showCoordinates(VideoPanel vidPanel) {
      // put values into offset x and y fields
      offset.xField.setValue(worldX);
      offset.yField.setValue(worldY);
      super.showCoordinates(vidPanel);
    }
  }
}
