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
import java.awt.image.*;
import java.util.HashMap;
import java.util.Map;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;

/**
 * This is a step for RGB tracks. It is used for obtaining
 * RGB data in a region of a video image.
 *
 * @author Douglas Brown
 */
public class RGBStep extends Step {

  // static fields
	protected static GeneralPath crosshair;
	
	// instance fields
  protected Position position;
  protected RGBRegion rgbRegion;
  protected int radius;
  protected Map<TrackerPanel, Shape> hitShapes = new HashMap<TrackerPanel, Shape>();
	protected double[] rgbData = new double[5];
	protected boolean dataValid = false;

  static {
  	crosshair = new GeneralPath();
  	crosshair.moveTo(0, -3);
  	crosshair.lineTo(0, 3);
  	crosshair.moveTo(-3, 0);
  	crosshair.lineTo(3, 0);
  }

  /**
   * Constructs a RGBStep with specified coordinates in image space.
   *
   * @param track the track
   * @param n the frame number
   * @param x the x coordinate
   * @param y the y coordinate
   * @param r the radius
   */
  public RGBStep(RGBRegion track, int n, double x, double y, int r) {
    super(track, n);
    this.radius = r;
    rgbRegion = track;
    position = new Position(x, y);
    position.setStepEditTrigger(true);
    points = new TPoint[] {position};
    screenPoints = new Point[getLength()];
  }

  /**
   * Gets the position TPoint.
   *
   * @return the position TPoint
   */
  public TPoint getPosition() {
    return position;
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
    Shape hitShape = hitShapes.get(trackerPanel);
    if (hitShape != null && hitShape.intersects(hitRect)) return position;
    return null;
  }

  /**
   * Overrides Step draw method.
   *
   * @param panel the drawing panel requesting the drawing
   * @param _g the graphics context on which to draw
   */
  public void draw(DrawingPanel panel, Graphics _g) {
    // draw the mark
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    Graphics2D g = (Graphics2D)_g;
    Mark mark = getMark(trackerPanel);
    if (mark != null) {
      mark.draw(g, false);
    }
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
      transform = trackerPanel.getPixelTransform();
      if (!trackerPanel.isDrawingInImageSpace()) {
        transform.concatenate(trackerPanel.getCoords().getToWorldTransform(n));
      }
    	// make region of interest
      Shape region = new Ellipse2D.Double(
        		position.getX()-radius, position.getY()-radius,
        		2*radius, 2*radius);
      final Shape rgn = transform.createTransformedShape(region);
      // center of circle is crosshair or selectionShape
    	Point p = position.getScreenPosition(trackerPanel);
      transform.setToTranslation(p.x, p.y);
      final Shape square = position == trackerPanel.getSelectedPoint()?
      	transform.createTransformedShape(selectionShape): null;
      final Shape cross = transform.createTransformedShape(crosshair);
      mark = new Mark() {
        public void draw(Graphics2D g, boolean highlighted) {
          Paint gpaint = g.getPaint();
          g.setPaint(footprint.getColor());
          g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
          		RenderingHints.VALUE_ANTIALIAS_ON);
          if (square != null) g.fill(square);
          else g.draw(cross);
          g.setStroke(footprint.getStroke());
          g.draw(rgn);
          g.setPaint(gpaint);
        }

        public Rectangle getBounds(boolean highlighted) {
          return rgn.getBounds();
        }
      };
      marks.put(trackerPanel, mark);
      // center is also the hit shape
      hitShapes.put(trackerPanel, cross);
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
   * Sets the radius.
   *
   * @param r the radius
   */
  public void setRadius(int r) {
  	radius = r;
  }

  /**
   * Clones this Step.
   *
   * @return a clone of this step
   */
  public Object clone() {
    RGBStep step = (RGBStep)super.clone();
    if (step != null) {
      step.hitShapes = new HashMap<TrackerPanel, Shape>();
      step.points[0] = step.position = step.new Position(
      			position.getX(), position.getY());
      step.position.setStepEditTrigger(true);
      step.rgbData = new double[5];
      step.dataValid = false;
    }
    return step;
  }

  /**
   * Returns a String describing this step.
   *
   * @return a descriptive string
   */
  public String toString() {
    return "RGBStep " + n //$NON-NLS-1$
           + " [" + format.format(position.x) //$NON-NLS-1$
           + ", " + format.format(position.y) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Gets the RGB data. Return array is {R,G,B,luma,pixels}
   *
   * @param trackerPanel the tracker panel
   * @return an integer array of data values
   */
  public double[] getRGBData(TrackerPanel trackerPanel) {
		Video vid = trackerPanel.getVideo();
		if (vid == null || !vid.isVisible()) return null;
  	if (!dataValid && trackerPanel.getFrameNumber() == n) {
	    BufferedImage image = vid.getImage();
	    if (image != null 
	    			&& image.getType() == BufferedImage.TYPE_INT_RGB) {
	    	RGBStep step = rgbRegion.isFixedPosition()? 
	    				(RGBStep)rgbRegion.getStep(0): this;	
	    	TPoint pt = step.getPosition();
	      Shape region = new Ellipse2D.Double(
	        		pt.getX()-radius, pt.getY()-radius,
	        		2*radius, 2*radius);
	      int h = 2*radius + 1;
	      int w = h;
        // locate starting pixel
        int x0 = (int)pt.getX()-radius;
        int y0 = (int)pt.getY()-radius;
        Point2D centerPt = new Point2D.Double();
	      try {
	        int[] pixels = new int[h*w];
	        int n = 0, r = 0, g = 0, b = 0;
	        // fill pixels array with pixel data
	        image.getRaster().getDataElements(x0, y0, w, h, pixels);
	        // step thru pixels horizontally
	        for (int i = 0; i < w; i++) {
	          // step vertically
	          for (int j = 0; j < h; j++) {
	          	// include pixel if center is inside region
	          	centerPt.setLocation(x0+i+.5, y0+j+.5);
	          	if (region.contains(centerPt)) {
		            int pixel = pixels[i + j*w];
		            n++; // pixel count
		            r += (pixel >> 16) & 0xff; // red
		            g += (pixel >> 8) & 0xff; // green
		            b += (pixel) & 0xff; // blue
	          	}
	          }
	        }
	        if (n == 0) return null;
	        double rMean = 1.0*r/n;
	        double gMean = 1.0*g/n;
	        double bMean = 1.0*b/n;
	        rgbData[0] = rMean;
	        rgbData[1] = gMean;
	        rgbData[2] = bMean;
	        rgbData[3] = RGBRegion.getLuma(rMean, gMean, bMean);
	        rgbData[4] = n;
	  	    dataValid = true;
	      } catch(ArrayIndexOutOfBoundsException ex) {return null;}
	    }
  	}
    dataVisible = true;
    return rgbData;
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
      if (rgbRegion.isFixedPosition()) {
      	RGBStep step = (RGBStep)rgbRegion.steps.getStep(0);
      	step.getPosition().setLocation(x, y); // set location of step 0
  	    step.erase();
      	rgbRegion.refreshStep(RGBStep.this); // set location of this step
    		rgbRegion.clearData(); // all data is invalid
      }
      else {
      	setLocation(x, y);
      	rgbRegion.keyFrames.add(n);
        dataValid = false; // this step's data is invalid      
    	}      
      repaint();
      track.support.firePropertyChange("step", null, new Integer(n)); //$NON-NLS-1$
    }

    /**
     * Overrides TPoint showCoordinates method.
     *
     * @param vidPanel the video panel
     */
    public void showCoordinates(VideoPanel vidPanel) {
      // put values into x and y fields
      Point2D p = getWorldPosition(vidPanel);
      track.xField.setValue(p.getX());
      track.yField.setValue(p.getY());
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

  }

//__________________________ static methods ___________________________

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
      RGBStep step = (RGBStep) obj;
      control.setValue("x", step.position.x); //$NON-NLS-1$
      control.setValue("y", step.position.y); //$NON-NLS-1$
      control.setValue("radius", step.radius); //$NON-NLS-1$
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
    	RGBStep step = (RGBStep) obj;
      step.setRadius(control.getInt("radius")); //$NON-NLS-1$
    	double x = control.getDouble("x"); //$NON-NLS-1$
      double y = control.getDouble("y"); //$NON-NLS-1$
      step.position.setXY(x, y);
    	return obj;
    }
  }
}

