/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2018  Douglas Brown
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

import java.util.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.geom.*;

import javax.swing.SwingUtilities;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.FontSizer;

/**
 * This is a Step for a CircleFitter. It is used for measuring and finding centers of circles.
 *
 * @author Douglas Brown
 */
public class CircleFitterStep extends Step {
	
  protected static AffineTransform transform = new AffineTransform();
	protected static TPoint endPoint1 = new TPoint(); // used for large radius case
  protected static TPoint endPoint2 = new TPoint(); // used for large radius case
	protected static boolean doRefresh = true;
	
  // instance fields
  protected CircleFitter circleFitter;
  // dataPoints[0] are user-marked, dataPoints[1] are attached points
  protected DataPoint[][] dataPoints = new DataPoint[2][0];
  protected CenterPoint center; 
  protected TPoint edge; 
  protected double radius;
  protected Map<TrackerPanel, Shape> circleHitShapes = new HashMap<TrackerPanel, Shape>();
  protected Map<TrackerPanel, Shape> centerHitShapes = new HashMap<TrackerPanel, Shape>();
  protected ArrayList<Map<TrackerPanel, Shape>> pointHitShapes = new ArrayList<Map<TrackerPanel, Shape>>();
  protected Shape selectedShape;
  
  /**
   * Constructs an empty CircleFitterStep.
   *
   * @param track the track
   * @param n the frame number
   */
  public CircleFitterStep(CircleFitter track, int n) {
    super(track, n);
    circleFitter = track;
    center = new CenterPoint(0, 0);
    edge = new TPoint();
    points = new TPoint[] {center, edge};
    screenPoints = new Point[points.length];
  }

  /**
   * Sets the data point at a specified column and row. Replaces existing element, adds null elements if needed.
   * Data is stored in array element DataPoints[col][row]
   *
   * @param p the data point (may be null)
   * @param column the array index
   * @param index the array index
   * @param refreshAndPostEdit true to refresh circle, fire event and post undo edit
   * @param reduceArrayLengthIfNull true to eliminate null elements
   */
  public void setDataPoint(DataPoint p, int column, int row, boolean refreshAndPostEdit, boolean reduceArrayLengthIfNull) {
  	if (row<0 || column<0 || column>=dataPoints.length) return;
		XMLControl control = new XMLControlElement(this); // for undoable edit
		
  	if (!circleFitter.isFixed() && refreshAndPostEdit) {
    	circleFitter.keyFrames.add(n);
		}
  	
  	// make new array if needed
  	if (row>=dataPoints[column].length) {
  		int len = dataPoints[column].length;
      DataPoint[] newPoints = new DataPoint[row+1];
      System.arraycopy(dataPoints[column], 0, newPoints, 0, len);
      dataPoints[column] = newPoints;
  	}
  	
  	// set the array element
  	dataPoints[column][row] = p;
  	if (p==null && reduceArrayLengthIfNull) {
      DataPoint[] newPoints = new DataPoint[dataPoints[column].length-1];
      System.arraycopy(dataPoints[column], 0, newPoints, 0, row);
      System.arraycopy(dataPoints[column], row+1, newPoints, row, dataPoints[column].length-row-1);
      dataPoints[column] = newPoints;
  	}

    if (refreshAndPostEdit) {
	    defaultIndex = dataPoints[0].length-1;
	    refreshCircle();
	  	circleFitter.dataValid = false;
	  	circleFitter.firePropertyChange("data", null, circleFitter); //$NON-NLS-1$
	  	circleFitter.firePropertyChange("dataPoint", null, circleFitter); //$NON-NLS-1$
	    if (circleFitter.trackerPanel != null) {
	    	circleFitter.trackerPanel.changed = true;
	    }
	  	TTrackBar.getTrackbar(circleFitter.trackerPanel).refresh();
    	Undo.postStepEdit(this, control);
    }
  }

  /**
   * Adds a data point at the end of the array.
   *
   * @param p the data point
   * @param refreshAndPostEdit true to refresh circle, fire event and post undo edit
   * 
   */
  public void addDataPoint(DataPoint p, boolean refreshAndPostEdit) {
  	setDataPoint(p, 0, dataPoints[0].length, refreshAndPostEdit, p==null);
  }

  /**
   * Removes a data point from the user-marked array.
   *
   * @param p the point to remove
   * @param postUndoableEdit true to post an undoable edit
   * @param fireEvents true to fire property change events
   */
  public void removeDataPoint(DataPoint p, boolean postUndoableEdit, boolean fireEvents) {
  	if (p==null) return;
  	int index = -1;
  	for (int i=0; i<dataPoints[0].length; i++) {
  		if (p==dataPoints[0][i]) {
  	  	index = i;
  	  	break;
  		}
  	}
  	XMLControl control = new XMLControlElement(this);
  	if (index>-1) {
  		if (!circleFitter.isFixed() && !p.isAttached()) {
      	circleFitter.keyFrames.add(n);
  		}
    	
    	// make new array
      DataPoint[] newPoints = new DataPoint[dataPoints[0].length-1];
      System.arraycopy(dataPoints[0], 0, newPoints, 0, index);
      System.arraycopy(dataPoints[0], index+1, newPoints, index, dataPoints[0].length-index-1);
      dataPoints[0] = newPoints;

	  }
    refreshCircle();
		if (index>-1 && postUndoableEdit) {
			Undo.postStepEdit(this, control);
      if (circleFitter.trackerPanel != null) {
      	circleFitter.trackerPanel.changed = true;
      }
		}
  	if (n==circleFitter.trackerPanel.getFrameNumber()) {
	    repaint();
  		circleFitter.refreshFields(n);
  	}
  	circleFitter.dataValid = false;
  	if (fireEvents) {
	  	circleFitter.firePropertyChange("data", null, null); //$NON-NLS-1$
	  	circleFitter.firePropertyChange("dataPoint", null, circleFitter); //$NON-NLS-1$
  	}
  	circleFitter.trackerPanel.setSelectedPoint(null);
  	TTrackBar.getTrackbar(circleFitter.trackerPanel).refresh();
  	repaint();
  }

  /**
   * Gets a data point.
   *
   * @param column the column: 0=marked points, 1=attached points
   * @param row the row
   * @return the DataPoint, or null if not found
   */
  public DataPoint getDataPoint(int column, int row) {
  	if (row>=0 && column>=0 && column<dataPoints.length && dataPoints[column].length>row) {
  		return dataPoints[column][row];
  	}
    return null;
  }

  /**
   * Gets the valid data points. A point is valid if non-null. 
   * This return points from all columns, with user-marked points (column 0) first.
   */
  public ArrayList<DataPoint> getValidDataPoints() {
  	ArrayList<DataPoint> validPoints = new ArrayList<DataPoint>();
  	for (int col=0; col<dataPoints.length; col++) {
  		DataPoint[] pts = dataPoints[col];
  		for (int row=0; row<pts.length; row++) {
	  		if (pts[row]!=null) {
	  			validPoints.add(pts[row]);
	  		}
  		}
  	}
    return validPoints;
  }

  /**
   * Trims the attached points array to a specified length.
   * 
   * @param len the trimmed length
   * @return true if any non-null points were trimmed
   */
  public boolean trimAttachedPointsToLength(int len) {
  	boolean changed = false;
  	if (len<dataPoints[1].length) {
    	DataPoint[] newPoints = new DataPoint[len];
    	System.arraycopy(dataPoints[1], 0, newPoints, 0, len);
    	for (int i=len; i<dataPoints[1].length; i++) {
    		changed = changed || dataPoints[1][i]!=null;
    	}
    	dataPoints[1] = newPoints;
  	}
  	return changed;
  }

  @Override
  public TPoint getDefaultPoint() {
  	if (defaultIndex>=0 && dataPoints[0].length>defaultIndex) {
  		return dataPoints[0][defaultIndex];
  	}
    return null;
  }

  @Override
  public Interactive findInteractive(
         DrawingPanel panel, int xpix, int ypix) {
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    setHitRectCenter(xpix, ypix);
    Shape hitShape;
    Interactive hit = null;
    
  	hitShape = circleHitShapes.get(trackerPanel);
  	if (isValidCircle() && hitShape!=null && hitShape.intersects(hitRect)) { 
  		hit = edge;
  	}
    
  	hitShape = centerHitShapes.get(trackerPanel);
  	if (isValidCircle() && hitShape!=null && hitShape.intersects(hitRect)) { 
  		hit = center;
  	}
    
    for (int i=0; i<pointHitShapes.size(); i++) {
    	Map<TrackerPanel, Shape> map = pointHitShapes.get(i);
    	if (map!=null) {
      	hitShape = map.get(trackerPanel);
      	if (hitShape!=null && hitShape.intersects(hitRect)) { 
      		ArrayList<DataPoint> validPoints = getValidDataPoints();
      		if (i<validPoints.size())
      			hit = validPoints.get(i);
      	}
    	}

    }
    
  	if (hit!=null && hit instanceof DataPoint && ((DataPoint)hit).isAttached()) {
  		return null;
  	}

    return hit;
  }

  @Override  
	public void draw(DrawingPanel panel, Graphics _g) {
    // draw the mark
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    Graphics2D g = (Graphics2D)_g;
    getMark(trackerPanel).draw(g, false);
  }

  @Override
  protected Mark getMark(TrackerPanel trackerPanel) {
    Mark mark = marks.get(trackerPanel);
    TPoint selection = null;
    if (mark==null) {
      selection = trackerPanel.getSelectedPoint();
      // assemble screen points array
      ArrayList<DataPoint> pts = getValidDataPoints();
  		int dataCount = pts.size();
      if (screenPoints.length!=points.length+dataCount) {
      	screenPoints = new Point[points.length+dataCount];
      }
      Point p = null;
      for (int i = 0; i<points.length; i++) {
        screenPoints[i] = points[i].getScreenPosition(trackerPanel);
        if (selection==points[i]) p = screenPoints[i];
      }
      for (int i = 0; i<dataCount; i++) {
      	DataPoint next = pts.get(i);
        screenPoints[i+points.length] = next.getScreenPosition(trackerPanel);
        if (selection==next) p = screenPoints[i+points.length];
      }
      
      // set up footprint
      CircleFitterFootprint fitterFootprint = (CircleFitterFootprint)footprint;
      fitterFootprint.setSelectedPoint(p);
      fitterFootprint.setPixelRadius(radius*trackerPanel.getXPixPerUnit()); // radius in screen pixel units
      fitterFootprint.setMarkedPointCount(dataPoints[0].length);
      // get footprint mark
      mark = fitterFootprint.getMark(screenPoints);
      // make mark to draw selected point, if any
      if (p != null) {
        final Color color = footprint.getColor();
        final Mark stepMark = mark;
        transform.setToTranslation(p.x, p.y);
        int scale = FontSizer.getIntegerFactor();
        if (scale>1) {
        	transform.scale(scale, scale);
        }
        selectedShape = transform.createTransformedShape(selectionShape);
        mark = new Mark() {
          public void draw(Graphics2D g, boolean highlighted) {
            stepMark.draw(g, false);
            Paint gpaint = g.getPaint();
            g.setPaint(color);
            if (selectedShape != null) 
            	g.fill(selectedShape);
            g.setPaint(gpaint);
          }

          public Rectangle getBounds(boolean highlighted) {
            Rectangle bounds = stepMark.getBounds(false);
            if (selectedShape != null) {
            	bounds.add(selectedShape.getBounds());
            }
            return bounds;
          }
        };
      }
      marks.put(trackerPanel, mark);
      
      // get new hit shapes
      Shape[] shapes = footprint.getHitShapes();
      circleHitShapes.put(trackerPanel, shapes[0]);
      centerHitShapes.put(trackerPanel, shapes[1]);
      if (shapes.length-2<pointHitShapes.size()) {
      	pointHitShapes.clear();
      }
      for (int i=2; i<shapes.length; i++) {
      	if (pointHitShapes.size()<=i-1) {
      		Map<TrackerPanel, Shape> newMap = new HashMap<TrackerPanel, Shape>();
      		pointHitShapes.add(newMap);
      	}
      	Map<TrackerPanel, Shape> map = pointHitShapes.get(i-2);
      	map.put(trackerPanel, shapes[i]);
      }
      
    }
    return mark;
  }

  /**
   * Returns the circle radius in world units.
   * 
   * @return the radius in world units
   */
  public double getWorldRadius() {
		int dataCount=getValidDataPoints().size();
  	if (dataCount<3 || circleFitter.trackerPanel==null) {
  		return Double.NaN;
  	}
  	return radius/circleFitter.trackerPanel.getCoords().getScaleX(n);
  }
  
  /**
   * Returns the circle center coordinates in world units.
   * 
   * @return the center point in world units
   */
  public Point2D getWorldCenter() {
		int dataCount=getValidDataPoints().size();
  	if (dataCount<3 || Double.isInfinite(radius) || radius>CircleFitterFootprint.MAX_RADIUS
  			 || circleFitter.trackerPanel==null) {
  		return null;
  	}
    return center.getWorldPosition(circleFitter.trackerPanel);  	
  }
  
  /**
   * Returns true if the circle is valid (ie if at least 3 data points have been successfuly fit).
   * 
   *  @return true if valid
   */
  public boolean isValidCircle() {
		int dataCount=getValidDataPoints().size();
  	return dataCount>2 && !Double.isInfinite(radius) 
  			&& radius>0	&& radius<CircleFitterFootprint.MAX_RADIUS;
  }

  /**
   * Refreshes the circle based on the current data points. 
   */
  public void refreshCircle() {
  	double prevR = radius, prevX = center.x, prevY = center.y;

  	ArrayList<DataPoint> pts = getValidDataPoints();
		int len=pts.size();
  	TPoint p = null;
  	switch (len) {
  		case 0: 
  			break;
  		case 1:
  			DataPoint p0 = pts.get(0);
  	    center.setLocation(p0);
  	    break;
  		case 2:
  			p0 = pts.get(0);
  			DataPoint p1 = pts.get(1);
  	    center.center(p0, p1);
  	    edge.setLocation(p0);
  	    break;
  		case 3:
  			p0 = pts.get(0);
  			p1 = pts.get(1);
  			DataPoint p2 = pts.get(2);
  	    refreshCircle(p0, p1, p2);
  	    if (circleFitter.trackerPanel!=null) {
  	    	p = circleFitter.trackerPanel.getSelectedPoint();
  	    }
  	    edge.setLocation(p==p1? p1: p==p2? p2: p0);
  	    break;
  		default:
  			refreshCircle(pts);
      	if (Double.isInfinite(radius) || radius>CircleFitterFootprint.MAX_RADIUS) {
    	    if (circleFitter.trackerPanel!=null) {
    	    	p = circleFitter.trackerPanel.getSelectedPoint();
    	    }
    			p0 = pts.get(0);
    			p1 = pts.get(1);
    			p2 = pts.get(2);
    	    edge.setLocation(p==p1? p1: p==p2? p2: p0);
      	}
      	else {
      		edge.setLocation(center.x, center.y+radius);
      	}
  	}
  	
  	boolean isVisible = circleFitter.trackerPanel!=null && n==circleFitter.trackerPanel.getFrameNumber();
  	if (radius!=prevR || center.x!=prevX || center.y!=prevY) {
  		if (isVisible) {
  			repaint();
  		}
  		else erase();
  	}
  	
//  	if (isVisible) {
//  		circleFitter.refreshFields(n);
//  	}
  }
  
  /**
   * Refreshes the circle based on exactly 3 points. 
   * 
   * @param p1 point 1
   * @param p2 point 2
   * @param p3 point 3
   */
  private void refreshCircle(TPoint p1, TPoint p2, TPoint p3) {
  	double xDeltaA = p2.getX()-p1.getX();
  	double yDeltaA = p2.getY()-p1.getY();
  	double xDeltaB = p3.getX()-p2.getX();
  	double yDeltaB = p3.getY()-p2.getY();
  	double slopeA = yDeltaA/xDeltaA;
  	double slopeB = yDeltaB/xDeltaB;
  	double xMidA = (p2.getX()+p1.getX())/2;
  	double yMidA = (p2.getY()+p1.getY())/2;
  	double xMidB = (p3.getX()+p2.getX())/2;
  	double yMidB = (p3.getY()+p2.getY())/2;
  	
  	// check for colinear points
  	if ((xDeltaA==0 && xDeltaB==0) || slopeA==slopeB) {
  		radius = Double.POSITIVE_INFINITY;
  		return;
  	}
  	
  	// check for horizontal/vertical cases
  	if (yDeltaA==0) { // slopeA==0
	    center.x = xMidA;
	    if (xDeltaB==0) { // slopeB==INFINITY	    
	      center.y = yMidB;
	    }
	    else {
	      center.y = yMidB + (xMidB-center.x)/slopeB;
	    }  		
  	}
  	else if (yDeltaB==0) { // slopeB==0
	    center.x = xMidB;
	    if (xDeltaA==0) { // slopeA==INFINITY	    
	      center.y = yMidA;
	    }
	    else {
	      center.y = yMidA + (xMidA-center.x)/slopeA;
	    }  		
  	}
  	else if (xDeltaA==0) { // slopeA==INFINITY  	
  	  center.y = yMidA;
  	  center.x = slopeB*(yMidB-center.y) + xMidB;
  	}
  	else if (xDeltaB==0) { // slopeB==INFINITY  	
	    center.y = yMidB;
	    center.x = slopeA*(yMidA-center.y) + xMidA;
  	}
  	else {
  	  center.x = (slopeA*slopeB*(yMidA-yMidB) - slopeA*xMidB + slopeB*xMidA)/(slopeB-slopeA);
  	  center.y = yMidA - (center.x - xMidA)/slopeA;
  	}
  	// set radius in image units
  	radius = center.distance(p1); // in image units
  }
  
  /**
   * Refreshes the circle center and radius based on 4 or more points.
   * Uses the Modified Least Squares method in closed form described in
   * Dale Umbach & Kerry N. Jones, A Few Methods for Fitting Circles to Data,
   * IEEE TRANSACTIONS ON INSTRUMENTATION AND MEASUREMENT, date unknown (2000?) 
   */
  private void refreshCircle(ArrayList<DataPoint> pts) {
  	// check for colinear points
  	double[] deltax = new double[pts.size()-1];
  	double[] deltay = new double[pts.size()-1];
  	double[] slope = new double[pts.size()-1];
  	DataPoint prev = null;
  	boolean allDeltaXZero = true, allSameSlope = true;
  	for (int i=0; i<pts.size(); i++) {
  		DataPoint p = pts.get(i);
  		if (i==0) {
  			prev = p;
  			continue;
  		}
  		deltax[i-1] = p.x-prev.x;
  		deltay[i-1] = p.y-prev.y;
  		slope[i-1] = deltay[i-1]/deltax[i-1];
  		if (i>1) {
  			allDeltaXZero = allDeltaXZero && deltax[i-1]==deltax[i-2];
  			allSameSlope = allSameSlope && slope[i-1]==slope[i-2];
  		}
  	}
  	if (allDeltaXZero || allSameSlope) {
  		radius = Double.POSITIVE_INFINITY;
  		return;
  	}
  	
  	// find center
  	double sumx=0, sumy=0, sumx2=0, sumy2=0, sumx3=0, sumy3=0, sumxy=0, sumxy2=0, sumx2y=0;
  	double val;
  	for (DataPoint p: pts) {
  		val = p.x;
  		sumx += val;
  		val *= p.x;
  		sumx2 += val;
  		val *= p.x;
  		sumx3 += val;
  		val = p.y;
  		sumy += val;
  		val *= p.y;
  		sumy2 += val;
  		val *= p.y;
  		sumy3 += val;
  		val = p.x*p.y;
  		sumxy += val;
  		sumxy2 += val*p.y;
  		sumx2y += val*p.x;
  	}
  	double n = pts.size();
  	double a = n*sumx2 - sumx*sumx;
  	double b = n*sumxy - sumx*sumy;
  	double c = n*sumy2 - sumy*sumy;
  	double d = 0.5*(n*sumxy2 - sumx*sumy2 +n*sumx3 - sumx*sumx2);
  	double e = 0.5*(n*sumx2y - sumy*sumx2 +n*sumy3 - sumy*sumy2);
  	double denom = a*c - b*b;
  	double x = (d*c - b*e)/denom; // center x-coordinate
  	double y = (a*e - b*d)/denom; // center y-coordinate
  	center.setLocation(x, y);
  	
  	 // find radius
  	double r = 0, dx, dy;
  	for (DataPoint p: pts) {
  		dx = p.x - x;
  		dy = p.y - y;
  		r += Math.sqrt(dx*dx + dy*dy);
  	}
  	radius = r/n;
  }
  
  /**
   * Clones this Step.
   *
   * @return a clone of this step
   */
  public Object clone() {
    CircleFitterStep step = (CircleFitterStep)super.clone();
    if (step != null) {
      step.points[0] = step.center = step.new CenterPoint(center.x, center.y);
      step.points[1] = step.edge = new TPoint(edge.getX(), edge.getY());
      step.circleHitShapes = new HashMap<TrackerPanel, Shape>();
      step.pointHitShapes = new ArrayList<Map<TrackerPanel, Shape>>();
      step.dataPoints = new DataPoint[2][0];
      step.dataPoints[0] = new DataPoint[dataPoints[0].length];
      for (int i=0; i<dataPoints[0].length; i++) {
      	DataPoint p = dataPoints[0][i];
      	step.dataPoints[0][i] = step.new DataPoint(p.x, p.y);
      }
    }
    return step;
  }
  
  /**
   * Copies data points from another step, then refreshes the circle.
   *
   * @param step the step to copy
   */
  public void copy(CircleFitterStep step) {
  	// copy only marked data points--attached points set in refreshAttachments() method
  	if (dataPoints[0].length!=step.dataPoints[0].length) {
  		dataPoints[0] = new DataPoint[step.dataPoints[0].length];
    	for (int i=0; i<step.dataPoints[0].length; i++) {
    		DataPoint next = step.dataPoints[0][i];
    		dataPoints[0][i] = next==null? null: this.new DataPoint(next.x, next.y);
    	}
  	}
  	else {
	  	for (int i=0; i<step.dataPoints[0].length; i++) {
	  		if (dataPoints[0][i]!=null && step.dataPoints[0][i]!=null) {
	  			dataPoints[0][i].setLocation(step.dataPoints[0][i]);
	  		}
	  	}
  	}
    defaultIndex = dataPoints[0].length-1;
  	refreshCircle();
  }

  /**
   * Returns a String describing this.
   *
   * @return a descriptive string
   */
  public String toString() {
  	String s = ""; //$NON-NLS-1$
  	for (int i=0; i<dataPoints[0].length; i++) {
  		s += "\n"+i+": "+dataPoints[0][i]; //$NON-NLS-1$ //$NON-NLS-2$
  	}
    return "CircleFitterStep "+n+" [center ("+center.x+", "+center.y+"), radius "+radius+"]"+s; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
  }

  /**
   * Gets the step length.
   *
   * @return the length of the points array
   */
  public static int getLength() {
    return 3;
  }

  @Override
  protected void dispose() {
  	centerHitShapes.clear();
  	circleHitShapes.clear();  	
  	for (Map<TrackerPanel, Shape> shapes: pointHitShapes) {
  		shapes.clear();
  	}
  	pointHitShapes.clear();
  	super.dispose();
  }

  //______________________ inner DataPoint and CenterPoint classes ________________________

  class DataPoint extends TPoint {
  	
    /**
     * Constructs a DataPoint with specified image coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public DataPoint(double x, double y) {
      super(x, y);
      setStepEditTrigger(true);
    }

    /**
     * Overrides TPoint setXY method.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public void setXY(double x, double y) {
      if (getTrack().locked) return;

      if (circleFitter.isFixed()) {
      	int row = 0;
    		for (int j=0; j<dataPoints[0].length; j++) {
      		if (this==dataPoints[0][j]) {
      			row = j;
      			break;
      		}
      	}
      	CircleFitterStep keyStep = (CircleFitterStep)circleFitter.steps.getStep(0);
      	while (keyStep.dataPoints[0].length<=row) {
      		keyStep.addDataPoint(keyStep.new DataPoint(0, 0), false);
      	}
      	keyStep.dataPoints[0][row].setLocation(x, y); // set property of step 0
      	if (doRefresh) keyStep.refreshCircle();
      	if (doRefresh) circleFitter.refreshStep(CircleFitterStep.this); // sets properties of this step
      }
      else {
      	setLocation(x, y);
      	if (!this.isAttached())
      		circleFitter.keyFrames.add(n);
      	if (doRefresh) refreshCircle();
    	}
      if (doRefresh) circleFitter.refreshFields(n);
      
	  	circleFitter.dataValid = false;
	  	if (doRefresh) circleFitter.firePropertyChange("data", null, circleFitter); //$NON-NLS-1$
      if (circleFitter.trackerPanel != null) {
      	circleFitter.trackerPanel.changed = true;
      }
    }

    @Override
    public void setScreenPosition(int x, int y, VideoPanel vidPanel, InputEvent e) {
    	if (this.isAttached()) return; // don't drag or nudge when attached to another point
      setScreenPosition(x, y, vidPanel);
    }

    @Override
    public String toString() {
    	return "DataPoint "+n+": "+super.toString(); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public Step getAttachedStep() {
    	TTrack track = getTrack();
    	if (this.attachedTo!=null && track.trackerPanel!=null) {
    		ArrayList<PointMass> masses = track.trackerPanel.getDrawables(PointMass.class);
    		for (PointMass next: masses) {
    			Step step = next.getStep(attachedTo, track.trackerPanel);
    			if (step!=null) return step;
    		}
    	}
    	return null;
    }

  }
  
  class CenterPoint extends TPoint {
  	
    /**
     * Constructs a CenterPoint with specified image coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public CenterPoint(double x, double y) {
      super(x, y);
    }

    /**
     * Overrides TPoint setXY method to prevent user dragging/nudging.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public void setXY(double x, double y) {
      return;
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
      CircleFitterStep step = (CircleFitterStep) obj;
      // save data points
      DataPoint[] pts = step.dataPoints[0];
      double[][] pointData = new double[pts.length][2];
      for (int i=0; i<pts.length; i++) {
      	DataPoint next = pts[i];
      	double[] position = new double[] {next.x, next.y};
      	pointData[i] = position;
      }
      control.setValue("datapoints", pointData); //$NON-NLS-1$
    	if (step.circleFitter!=null && !step.circleFitter.isFixed()) {
    		control.setValue("iskey", step.circleFitter.keyFrames.contains(step.n)); //$NON-NLS-1$
    	}
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
    	CircleFitterStep step = (CircleFitterStep)obj;
    	if (step.circleFitter!=null && step.circleFitter.isFixed() && step.n!=0) {
    		step = (CircleFitterStep)step.circleFitter.getStep(0);
    	}
    	if (step.circleFitter!=null && !step.circleFitter.isFixed()) {
    		boolean isKey =control.getBoolean("iskey"); //$NON-NLS-1$
    		if (isKey) {
    			step.circleFitter.keyFrames.add(step.n);
    		}
    		else {
    			step.circleFitter.keyFrames.remove(step.n);
    		}
    	}
    	double[][] pointData = (double[][])control.getObject("datapoints"); //$NON-NLS-1$
      DataPoint[] pts = step.dataPoints[0];
    	int diff = pointData.length-pts.length;
    	if (diff<0) {
    		// remove data point(s)
    		for (int i=0; i<-diff; i++) {
    			DataPoint p = pts[pts.length-1]; // remove from the end
    			step.removeDataPoint(p, false, false);
    		}
    		pts = step.dataPoints[0];
    	}
    	else if (diff>0) {
    		// add data point(s)
    		for (int i=0; i<diff; i++) {
    			double[] position = pointData[pointData.length-1-i];
    			step.addDataPoint(step.new DataPoint(position[0], position[1]), false);
    		}
    		pts = step.dataPoints[0];
    	}
    	// set locations of all points
    	for (int i=0; i<pointData.length; i++) {
    		// set locations of valid points
  			double[] position = pointData[i];
  			pts[i].setLocation(position[0], position[1]);
    	}
  		step.refreshCircle();
    	if (step.circleFitter!=null) {
    		final CircleFitterStep cstep = step;
    		Runnable runner = new Runnable() {
    			public void run() {
        		cstep.circleFitter.dataValid = false;
        		cstep.circleFitter.firePropertyChange("data", null, null); //$NON-NLS-1$
    			}
    		};
    		SwingUtilities.invokeLater(runner);
    	}
    	return obj;
    }
  }
}

