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

import javax.swing.JOptionPane;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.media.core.*;

/**
 * This is a Step for a Calibration. It is used for
 * setting the origin, angle and scale of an ImageCoordSystem.
 *
 * @author Douglas Brown
 */
public class CalibrationStep extends Step {

  // instance fields
  private Calibration cal;
  protected double worldX0, worldY0, worldX1=1, worldY1;

  /**
   * Constructs a CalibrationStep with specified image coordinates.
   *
   * @param track the calibration
   * @param n the frame number
   * @param x the image x coordinate of point 0
   * @param y the image y coordinate of point 0
   */
  public CalibrationStep(Calibration track, int n, double x, double y) {
    super(track, n);
    cal = track;
    screenPoints = new Point[getLength()];
    points = new TPoint[getLength()];
    Position p = new Position(x, y);
    points[0] = p;
  }

  /**
   * Adds a second position point to this step at the specified image coordinates.
   *
   * @param x the image x coordinate of the position point
   * @param y the image y coordinate of the position point
   */
  public void addSecondPoint(double x, double y) {
    Position p = new Position(x, y);
    points[1] = p;
    setWorldCoordinates(worldX0, worldY0, worldX1, worldY1);
  }

  /**
   * Gets the specified position point.
   *
   * @param n the point number (0 or 1)
   * @return the position
   */
  public Position getPosition(int n) {
    return (Position)points[n];
  }

  /**
   * Gets the default point. The default point is the point initially selected
   * when the step is created. Overrides step getDefaultPoint method.
   *
   * @return the default TPoint
   */
  public TPoint getDefaultPoint() {
    if (points[1] == null) return points[0];
    if (cal.trackerPanel.getSelectedPoint()==points[0]) return points[0];
    return points[1];
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
      // set point locations to image positions of world coordinates
      for (int i = 0; i < points.length; i++) {
        Position pt = (Position)points[i];
        if (pt == null) continue;
        double worldX = i==0? worldX0: worldX1;
        double worldY = i==0? worldY0: worldY1;
        double x = coords.worldToImageX(n, worldX, worldY);
        double y = coords.worldToImageY(n, worldX, worldY);
        pt.setLocation(x, y);
      }
      // get point shapes
      selection = trackerPanel.getSelectedPoint();
      final Shape[] shapes = new Shape[points.length];
      for (int i = 0; i < points.length; i++) {
        if (points[i] == null) continue;
        Point p = points[i].getScreenPosition(trackerPanel);
        if (selection == points[i]) { // point is selected
          transform.setToTranslation(p.x, p.y);
          shapes[i] = transform.createTransformedShape(selectionShape);
        }
        else { // point not selected
          shapes[i] = footprint.getShape(new Point[] {p});
        }
      }
      // create mark
      final Color color = footprint.getColor();
      mark = new Mark() {
        public void draw(Graphics2D g, boolean highlighted) {
          Paint gpaint = g.getPaint();
          g.setPaint(color);
          g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);
          for (int i = 0; i < points.length; i++) {
            if (shapes[i] != null) g.fill(shapes[i]);
          }
          g.setPaint(gpaint);
        }

        public Rectangle getBounds(boolean highlighted) {
          Rectangle bounds = null;
          for (int i = 0; i < points.length; i++) {
            if (shapes[i] != null) {
              if (bounds == null) bounds = shapes[i].getBounds();
              else bounds.add(shapes[i].getBounds());
            }
          }
          return bounds;
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
    CalibrationStep step = (CalibrationStep)super.clone();
    step.points[0] = step.new Position(points[0].x, points[0].y);
    if (points[1] != null) {
      step.points[1] = step.new Position(points[1].x, points[1].y);
    }
    return step;
  }

  /**
   * Sets the world coordinates. When a single point is visible,
   * this sets the coords origin so its image position does not change.
   * When both points are visible, this sets the origin, angle and scale
   * so neither image position changes.
   *
   * @param x1 the world x coordinate of pt 1
   * @param y1 the world y coordinate of pt 1
   * @param x2 the world x coordinate of pt 2
   * @param y2 the world x coordinate of pt 2 
   * @return true if successfully set
   */
  public boolean setWorldCoordinates(double x1, double y1, double x2, double y2) {
    if (track.isLocked()) return false;
    // points can't share the same world position
    boolean sameX = x2==x1;
    boolean sameY = y2==y1;
    if ((sameX && cal.axes == Calibration.X_AXIS) ||
    		(sameY && cal.axes == Calibration.Y_AXIS) ||
    		(sameX && sameY && cal.axes == Calibration.XY_AXES)) {
      JOptionPane.showMessageDialog(track.trackerPanel, 
      				TrackerRes.getString("Calibration.Dialog.InvalidCoordinates.Message"),  //$NON-NLS-1$
      				TrackerRes.getString("Calibration.Dialog.InvalidCoordinates.Title"),  //$NON-NLS-1$
      				JOptionPane.WARNING_MESSAGE);
      return false;
    }
    
    if (cal.isFixedCoordinates()) {
    	CalibrationStep step = (CalibrationStep)cal.steps.getStep(0);
      step.worldX0 = x1;
      step.worldY0 = y1;
      step.worldX1 = x2;
      step.worldY1 = y2;
	    step.erase();
	    cal.refreshStep(CalibrationStep.this); // sets properties of this step
    }
    else {
      worldX0 = x1;
      worldY0 = y1;
      worldX1 = x2;
      worldY1 = y2;
    	cal.keyFrames.add(n);
  	}            
    
    if (points[1]!=null) {
      updateCoords();
    }
    else if (cal.trackerPanel!=null) {
      ImageCoordSystem coords = cal.trackerPanel.getCoords();
      int n = cal.trackerPanel.getFrameNumber();
      // get the current image position of the origin and points[0]
      double x0 = coords.getOriginX(n);
      double y0 = coords.getOriginY(n);
      double x = coords.worldToImageX(n, worldX0, worldY0);
      double y = coords.worldToImageY(n, worldX0, worldY0);
      // translate the origin
      coords.setOriginXY(n, x0+points[0].x-x, y0+points[0].y-y);
    }
    return true;
  }

  /**
   * Returns a String describing this step.
   *
   * @return a descriptive string
   */
  public String toString() {
    String s = "Calibration Points Step " + n //$NON-NLS-1$
           + " [" + format.format(worldX0) //$NON-NLS-1$
           + ", " + format.format(worldY0); //$NON-NLS-1$
    if (points[1] != null) {
      s = s + ", " + format.format(worldX1) //$NON-NLS-1$
            + ", " + format.format(worldY1) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
    else {
      s = s + "]"; //$NON-NLS-1$
    }
    return s;
  }

//_________________________ private methods ___________________________
  
  /**
   * Sets the angle, scale and origin based on the current image and world
   * coordinates of both calibration points.
   */
  private void updateCoords() {
    if (points[1] == null || cal.trackerPanel == null) return;
    if (cal.axes == Calibration.X_AXIS) {
    	updateCoordsXOnly();
    	return;
    }
    else if (cal.axes == Calibration.Y_AXIS) {
    	updateCoordsYOnly();
    	return;
    }
    ImageCoordSystem coords = cal.trackerPanel.getCoords();
    int n = cal.trackerPanel.getFrameNumber();
    // get the world coordinates of both points
    double wx0 = worldX0;
    double wy0 = worldY0;
    double wx1 = worldX1;
    double wy1 = worldY1;
    // get the image coordinates of point 0
    double x0 = points[0].getX();
    double y0 = points[0].getY();
    // get the image distance and angle from point 0 to 1
    double id = points[0].distance(points[1]);
    double itheta = points[0].angle(points[1]);
    // get the world distance and angle from point 0 to 1
    double dwx = wx1 - wx0;
    double dwy = wy1 - wy0;
    double wd = Math.sqrt(dwx*dwx + dwy*dwy);
    double wtheta = -Math.atan2(dwy, dwx);
    // set the scale
    double factor = id/wd;
    coords.setScaleXY(n, factor, factor);
    // set the angle
    double dtheta = wtheta - itheta;
    coords.setAngle(n, dtheta);
    // set the origin
    // get the current image position of the origin
    double xOrigin = coords.getOriginX(n);
    double yOrigin = coords.getOriginY(n);
    // move the origin to restore point 0 to its original image position
    double dx = coords.worldToImageX(n, wx0, wy0) - x0;
    double dy = coords.worldToImageY(n, wx0, wy0) - y0;
    coords.setOriginXY(n, xOrigin - dx, yOrigin - dy);
  }

  /**
   * Sets the scale and origin based on the current image and world
   * x-coordinates of both calibration points.
   */
  private void updateCoordsXOnly() {
    ImageCoordSystem coords = cal.trackerPanel.getCoords();
    int n = cal.trackerPanel.getFrameNumber();
    // get the world coordinates of the points
    double wx0 = worldX0;
    double wy0 = worldY0;
    double wx1 = worldX1;
    // get the image coordinates of the points
    double x0 = points[0].getX();
    double y0 = points[0].getY();
    double x1 = points[1].getX();
    double y1 = points[1].getY();
    // get the image distance and angle from point 0 to 1
    double dI = points[0].distance(points[1]);
    double thetaI = -points[0].angle(points[1]);
    // get the coords angle
    double thetaC = coords.getAngle(n);
    // get the image dxI from point 0 to 1
    double dTheta = thetaI-thetaC;
    double dxI = dI*Math.cos(dTheta);
    // get the world dx from point 0 to 1
    double dxW = wx1 - wx0;
    // set the scale
    double factor = dxI/dxW;
    if (factor > 0) coords.setScaleXY(n, factor, factor);
    else {
    	coords.setScaleXY(n, -factor, -factor);
    	coords.setAngle(n, thetaC+Math.PI);
    }
    // get the current image position of the origin
    double xOriginI = coords.getOriginX(n);
    double yOriginI = coords.getOriginY(n); 
    // move the origin to restore point 0 to its original image x position
    double dx = coords.worldToImageX(n, wx0, wy0) - x0;
    double dy = coords.worldToImageY(n, wx0, wy0) - y0;
    double theta = thetaC+Math.atan2(dy, dx);
    double dOrigin = Math.sqrt(dx*dx+dy*dy)*Math.cos(theta);
    double dxOrigin = dOrigin*Math.cos(thetaC);
    double dyOrigin = -dOrigin*Math.sin(thetaC);
    coords.setOriginXY(n, xOriginI-dxOrigin, yOriginI-dyOrigin);
    // set the world units to restore the original y-positions
    worldY0 = coords.imageToWorldY(n, x0, y0);
    worldY1 = coords.imageToWorldY(n, x1, y1);
  }
  
  /**
   * Sets the scale and origin based on the current image and world
   * y-coordinates of both calibration points.
   */
  private void updateCoordsYOnly() {
    ImageCoordSystem coords = cal.trackerPanel.getCoords();
    int n = cal.trackerPanel.getFrameNumber();
    // get the world coordinates of the points
    double wx0 = worldX0;
    double wy0 = worldY0;
    double wy1 = worldY1;
    // get the image coordinates of the points
    double x0 = points[0].getX();
    double y0 = points[0].getY();
    double x1 = points[1].getX();
    double y1 = points[1].getY();
    // get the image distance and angle from point 0 to 1
    double dI = points[0].distance(points[1]);
    double thetaI = -points[0].angle(points[1]);
    // get the coords angle
    double thetaC = coords.getAngle(n);
    // get the image dyI from point 0 to 1
    double dTheta = thetaI-thetaC;
    double dyI = dI*Math.sin(dTheta);
    // get the world dy from point 0 to 1
    double dyW = wy1 - wy0;
    // set the scale
    double factor = dyI/dyW;
    if (factor > 0) coords.setScaleXY(n, factor, factor);
    else {
    	coords.setScaleXY(n, -factor, -factor);
    	coords.setAngle(n, thetaC+Math.PI);
    }
    // get the current image position of the origin
    double xOriginI = coords.getOriginX(n);
    double yOriginI = coords.getOriginY(n); 
    // move the origin to restore point 0 to its original image y position
    double dx = coords.worldToImageX(n, wx0, wy0) - x0;
    double dy = coords.worldToImageY(n, wx0, wy0) - y0;
    double theta = thetaC+Math.atan2(dy, dx);
    double dOrigin = Math.sqrt(dx*dx+dy*dy)*Math.sin(theta);
    double dxOrigin = dOrigin*Math.sin(thetaC);
    double dyOrigin = dOrigin*Math.cos(thetaC);
    coords.setOriginXY(n, xOriginI-dxOrigin, yOriginI-dyOrigin);
    // set the world units to restore the original x-positions
    worldX0 = coords.imageToWorldX(n, x0, y0);
    worldX1 = coords.imageToWorldX(n, x1, y1);
  }    

  /**
   * Gets the step length.
   *
   * @return the length of the points array
   */
  public static int getLength() {
    return 2;
  }

//______________________ inner Position class ________________________

  /**
   * A class that represents the position of a calibration point.
   */
  public class Position extends TPoint {

    private double lastX, lastY;
    
    /**
     * Constructs a position with specified image coordinates,
     * and transforms those coordinates to set the world coordinates.
     * Calibration points are used in pairs to set the origin, scale and angle.
     *
     * @param x the image x coordinate
     * @param y the image y coordinate
     */
    public Position(double x, double y) {
      super.setXY(x, y);
      setCoordsEditTrigger(true);
      // set the world coordinates using x and y
      if (cal.trackerPanel != null) {
	      ImageCoordSystem coords = cal.trackerPanel.getCoords();
	      int n = cal.trackerPanel.getFrameNumber();
	      if (points[0]==null) { // this is first position created
	        worldX0 = coords.imageToWorldX(n, x, y);
	        worldY0 = coords.imageToWorldY(n, x, y);
	      }
	      else { // this is second position created
	        worldX1 = coords.imageToWorldX(n, x, y);
	        worldY1 = coords.imageToWorldY(n, x, y);
	      }
      }
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
      // calibration points can't share the same image position
      int i = this == points[0]? 1: 0;
      if (points[i] != null &&
          points[i].getX() == x &&
          points[i].getY() == y) {
        Toolkit.getDefaultToolkit().beep();
        return;
      }
      if (isAdjusting()) {
      	lastX = x;
      	lastY = y;
      }
      double dx = x - getX();
      double dy = y - getY();
      super.setXY(x, y);
      ImageCoordSystem coords = cal.trackerPanel.getCoords();
      coords.setAdjusting(isAdjusting());
      if (points[1] != null){
        updateCoords();
      }
      else if (cal.trackerPanel != null) {
        int n = cal.trackerPanel.getFrameNumber();
        // get the current image position of the origin
        double x0 = coords.getOriginX(n);
        double y0 = coords.getOriginY(n);
        // translate the origin
        coords.setOriginXY(n, x0 + dx, y0 + dy);
      }
      if (isAdjusting()) {
      	repaint();
      }
    }

    /**
     * Overrides TPoint showCoordinates method. This updates the values
     * of the x and y fields.
     *
     * @param vidPanel the video panel
     */
    public void showCoordinates(VideoPanel vidPanel) {
      // put values into calibration x and y fields
    	if (this==points[0]) {
	      cal.xField.setValue(worldX0);
	      cal.yField.setValue(worldY0);
    	}
    	else {
	      cal.x1Field.setValue(worldX1);
	      cal.y1Field.setValue(worldY1);
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
    		track.firePropertyChange("step", null, n); //$NON-NLS-1$
    	}
    }

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
    	CalibrationStep step = (CalibrationStep) obj;
    	double[] data = new double[] {step.worldX0, step.worldY0, step.worldX1, step.worldY1};
      control.setValue("world_coordinates", data); //$NON-NLS-1$
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
    	CalibrationStep step = (CalibrationStep)obj;
      double[] data = (double[])control.getObject("world_coordinates"); //$NON-NLS-1$
      if (data!=null) {
  			step.worldX0 = data[0];
  			step.worldY0 = data[1];
  			step.worldX1 = data[2];
  			step.worldY1 = data[3];
      }
      if (step.cal!=null)
      	step.cal.displayWorldCoordinates();
    	return obj;
    }
  }
}

