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
import java.awt.geom.*;

import javax.swing.SwingUtilities;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.FontSizer;

/**
 * This is a Step for a Compass. It is used for measuring and finding centers of circles.
 *
 * @author Douglas Brown
 */
public class CompassStep extends Step {
	
  protected static AffineTransform transform = new AffineTransform();
	protected static TPoint endPoint1 = new TPoint(); // used for large radius case
  protected static TPoint endPoint2 = new TPoint(); // used for large radius case

  // instance fields
  protected Compass compass;
  protected ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();
  protected TPoint center, edge; 
  protected Slider slider;
  protected double radius;
  protected Map<TrackerPanel, Shape> lineHitShapes = new HashMap<TrackerPanel, Shape>();
  protected ArrayList<Map<TrackerPanel, Shape>> pointHitShapes = new ArrayList<Map<TrackerPanel, Shape>>();
  protected Shape selectedShape;
  
  /**
   * Constructs an empty CompassStep.
   *
   * @param track the track
   * @param n the frame number
   */
  public CompassStep(Compass track, int n) {
    super(track, n);
    compass = track;
    center = new Center(0, 0);
    edge = new TPoint();
    slider = new Slider(0, 0);
    points = new TPoint[] {center, edge, slider};
    screenPoints = new Point[points.length];
  }

  /**
   * Adds an data point to this step at the specified image coordinates.
   *
   * @param x the image x coordinate of the data point
   * @param y the image y coordinate of the data point
   * @param refresh true to refresh the circle and fire property change event
   * 
   */
  public void addDataPoint(double x, double y, boolean refresh) {
		if (!compass.isFixed()) {
    	compass.keyFrames.add(n);
		}
    dataPoints.add(new DataPoint(x, y));
    if (refresh) {
	    defaultIndex = dataPoints.size()-1;
	    refreshCircle();
	  	compass.dataValid = false;
	  	compass.firePropertyChange("data", null, compass); //$NON-NLS-1$
	    if (compass.trackerPanel != null) {
	    	compass.trackerPanel.changed = true;
	    }
    }
  }

  /**
   * Removes a data point.
   *
   * @param p the point to remove
   */
  public void removeDataPoint(TPoint p) {
  	boolean found = false;
  	for (TPoint next: dataPoints) {
  		if (next==p) {
  	  	found = true;
  	  	break;
  		}
  	}
  	XMLControl control = new XMLControlElement(this);
  	if (found) {
  		if (!compass.isFixed()) {
      	compass.keyFrames.add(n);
  		}
	    dataPoints.remove((DataPoint)p);
      if (compass.trackerPanel != null) {
      	compass.trackerPanel.changed = true;
      }
	  }
    refreshCircle();
		if (found) {
			Undo.postStepEdit(this, control);
		}
  	if (n==compass.trackerPanel.getFrameNumber()) {
	    repaint();
  		compass.refreshFields(n);
  	}
  	compass.dataValid = false;
  	compass.firePropertyChange("data", null, null); //$NON-NLS-1$
  	compass.trackerPanel.setSelectedPoint(null);
  	TTrackBar.getTrackbar(compass.trackerPanel).refresh();
  }

  @Override
  public TPoint getDefaultPoint() {
  	if (dataPoints.size()>defaultIndex) {
  		return dataPoints.get(defaultIndex);
  	}
    return slider;
  }

  @Override
  public Interactive findInteractive(
         DrawingPanel panel, int xpix, int ypix) {
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    setHitRectCenter(xpix, ypix);
    Shape hitShape;
    Interactive hit = null;
    
    for (int i=0; i<pointHitShapes.size(); i++) {
    	Map<TrackerPanel, Shape> map = pointHitShapes.get(i);
    	if (map!=null) {
      	hitShape = map.get(trackerPanel);
      	if (hitShape!=null && hitShape.intersects(hitRect)) {
      		hit = dataPoints.get(i);
      	}
    	}

    }
    
    if (hit==null && compass.isRadialLineVisible()) {
    	hitShape = lineHitShapes.get(trackerPanel);
    	if (hitShape!=null && hitShape.intersects(hitRect)) {
        hit = slider;
    	}
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
      if (screenPoints.length!=points.length+dataPoints.size()) {
      	screenPoints = new Point[points.length+dataPoints.size()];
      }
      Point p = null;
      for (int i = 0; i<points.length; i++) {
        screenPoints[i] = points[i].getScreenPosition(trackerPanel);
        if (selection==points[i]) p = screenPoints[i];
      }
      for (int i = 0; i<dataPoints.size(); i++) {
      	DataPoint next = dataPoints.get(i);
        screenPoints[i+points.length] = next.getScreenPosition(trackerPanel);
        if (selection==next) p = screenPoints[i+points.length];
      }
      
      // set up footprint
      CompassFootprint cf = (CompassFootprint)footprint;
      cf.setSelectedPoint(p);
      cf.setDrawRadialLine(compass.isRadialLineVisible()); 
      cf.setPixelRadius(radius*trackerPanel.getXPixPerUnit()); // radius in screen pixel units
      // get footprint mark
      mark = footprint.getMark(screenPoints);
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
      lineHitShapes.put(trackerPanel, shapes[0]);
      if (shapes.length-1<pointHitShapes.size()) {
      	pointHitShapes.clear();
      }
      for (int i=1; i<shapes.length; i++) {
      	if (pointHitShapes.size()<=i-1) {
      		Map<TrackerPanel, Shape> newMap = new HashMap<TrackerPanel, Shape>();
      		pointHitShapes.add(newMap);
      	}
      	Map<TrackerPanel, Shape> map = pointHitShapes.get(i-1);
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
  	if (dataPoints.size()<3) {
  		return Double.NaN;
  	}
  	return radius/compass.trackerPanel.getCoords().getScaleX(n);
  }
  
  /**
   * Returns the circle center coordinates in world units.
   * 
   * @return the center point in world units
   */
  public Point2D getWorldCenter() {
  	if (dataPoints.size()<3 || Double.isInfinite(radius) || radius>CompassFootprint.MAX_RADIUS) {
  		return null;
  	}
    return center.getWorldPosition(compass.trackerPanel);  	
  }
  
  /**
   * Returns the slider angle relative to the +x-axis.
   * 
   * @return the slider angle
   */
  public double getSliderAngle() {
  	// deal with special cases
  	if (dataPoints.size()<3 || Double.isNaN(radius) || radius>CompassFootprint.MAX_RADIUS) {
  		return Double.NaN;
  	}
  	double theta = -center.angle(slider);
  	if (compass.trackerPanel!=null) {
  		theta -= compass.trackerPanel.getCoords().getAngle(n);
  	}
  	return theta;
  }

  /**
   * Returns the slider angle relative to the horizontal.
   * 
   * @return the slider angle
   */
  public void setSliderAngle(double theta) {
  	double prev = getSliderAngle();
  	if (theta==prev || Double.isNaN(prev)) return;
  	if (compass.trackerPanel!=null) {
  		theta += compass.trackerPanel.getCoords().getAngle(n);
  	}
  	double sin = -Math.sin(theta);
  	double cos = Math.cos(theta);
  	slider.setLocation(center.x+radius*cos, center.y+radius*sin);
  	repaint();
  	compass.refreshFields(n);
//  	compass.dataValid = false;
//  	compass.firePropertyChange("data", null, null);
  }

  /**
   * Refreshes the circle based on the current data points. 
   */
  public void refreshCircle() {
  	double prevR = radius, prevX = center.x, prevY = center.y;
  	int len = dataPoints.size();
		double sin = 1, cos = 0;
  	if (!Double.isInfinite(radius) 
  			&& radius>0  
  			&& radius<CompassFootprint.MAX_RADIUS) {
			cos = (slider.x-center.x)/radius;
			sin = (slider.y-center.y)/radius;
		}

  	TPoint p = null;
  	switch (len) {
  		case 0: 
  			break;
  		case 1:
  			DataPoint p0 = dataPoints.get(0);
  	    center.setLocation(p0);
  	    break;
  		case 2:
  			p0 = dataPoints.get(0);
  			DataPoint p1 = dataPoints.get(1);
  	    center.center(p0, p1);
  	    edge.setLocation(p0);
  	    break;
  		case 3:
  			p0 = dataPoints.get(0);
  			p1 = dataPoints.get(1);
  			DataPoint p2 = dataPoints.get(2);
  	    refreshCircle(p0, p1, p2);
  	    if (compass.trackerPanel!=null) {
  	    	p = compass.trackerPanel.getSelectedPoint();
  	    }
  	    edge.setLocation(p==p1? p1: p==p2? p2: p0);
  	    break;
  		default:
  			refreshCircle(dataPoints);
      	if (Double.isInfinite(radius) || radius>CompassFootprint.MAX_RADIUS) {
    	    if (compass.trackerPanel!=null) {
    	    	p = compass.trackerPanel.getSelectedPoint();
    	    }
    			p0 = dataPoints.get(0);
    			p1 = dataPoints.get(1);
    			p2 = dataPoints.get(2);
    	    edge.setLocation(p==p1? p1: p==p2? p2: p0);
      	}
      	else {
      		edge.setLocation(center.x, center.y+radius);
      	}
  	}
  	
  	boolean isVisible = compass.trackerPanel!=null && n==compass.trackerPanel.getFrameNumber();
  	if (radius!=prevR || center.x!=prevX || center.y!=prevY) {
    	if (!Double.isInfinite(radius) 
    			&& radius>0  
    			&& radius<CompassFootprint.MAX_RADIUS) {
    		// set position of slider
    		slider.setLocation(center.x+radius*cos, center.y+radius*sin);
    	}
  		if (isVisible) {
  			repaint();
  		}
  		else erase();
  	}
  	if (isVisible) {
  		compass.refreshFields(n);
  	}
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
    CompassStep step = (CompassStep)super.clone();
    if (step != null) {
      step.points[0] = step.center = step.new Center(center.getX(), center.getY());
      step.points[1] = step.edge = new TPoint(edge.getX(), edge.getY());
      step.points[2] = step.slider = step.new Slider(slider.getX(), slider.getY());
      step.lineHitShapes = new HashMap<TrackerPanel, Shape>();
      step.pointHitShapes = new ArrayList<Map<TrackerPanel, Shape>>();
      step.dataPoints = new ArrayList<DataPoint>();
      for (DataPoint next: dataPoints) {
      	step.dataPoints.add(new DataPoint(next.x, next.y));
      }
    }
    return step;
  }
  
  /**
   * Copies data points from another step, then refreshes the circle.
   *
   * @param step the step to copy
   */
  public void copy(CompassStep step) {
  	if (dataPoints.size()!=step.dataPoints.size()) {
  		dataPoints.clear();    		
    	for (int i=0; i<step.dataPoints.size(); i++) {
    		dataPoints.add(new DataPoint(0, 0));
    	}
  	}
  	for (int i=0; i<step.dataPoints.size(); i++) {
  		DataPoint source = step.dataPoints.get(i);
  		dataPoints.get(i).setLocation(source);
  	}
    defaultIndex = dataPoints.size()-1;
    double theta = step.getSliderAngle();
  	refreshCircle();
  	setSliderAngle(theta);
  }

  /**
   * Returns a String describing this.
   *
   * @return a descriptive string
   */
  public String toString() {
    return "CompassStep "+n+" [center ("+center.x+", "+center.y+"), radius "+radius+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
  }

  /**
   * Gets the step length.
   *
   * @return the length of the points array
   */
  public static int getLength() {
    return 3;
  }

  //______________________ inner Slider class ________________________

  class Slider extends TPoint {
  	
    /**
     * Constructs a Edge with specified image coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Slider(double x, double y) {
      super(x, y);
      setStepEditTrigger(true);
    }

    /**
     * Overrides TPoint setXY method to move slider along the circle.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public void setXY(double x, double y) {
      if (compass.isFixed()) {
      	CompassStep keyStep = (CompassStep)compass.steps.getStep(0);
      	keyStep.slider.setLocation(x, y); // set property of keyStep 0
      	Point p = keyStep.slider.getScreenPosition(compass.trackerPanel);
      	keyStep.slider.setPositionOnCircle(p.x, p.y, compass.trackerPanel);
      	compass.refreshStep(CompassStep.this); // sets properties of this step
      }
      else {
      	setLocation(x, y);
      	Point p = getScreenPosition(compass.trackerPanel);
      	setPositionOnCircle(p.x, p.y, compass.trackerPanel);
      }
      repaint();
      compass.refreshFields(n);
	  	compass.dataValid = false;
      if (compass.trackerPanel != null) {
      	compass.trackerPanel.changed = true;
      }      
    }

    /**
     * Sets the position of the slider on the circle nearest the specified
     * screen position.
     *
     * @param xScreen the x screen position
     * @param yScreen the y screen position
     * @param trackerPanel the trackerPanel drawing this step
     */
    public void setPositionOnCircle(int xScreen, int yScreen, TrackerPanel trackerPanel) {
    	
	    if (compass.isFixed() && n!=0) {
		  	CompassStep keyStep = (CompassStep)compass.steps.getStep(0);
		  	keyStep.slider.setPositionOnCircle(xScreen, yScreen, trackerPanel);
		  	return;
		  }
    	
    	compass.keyFrames.add(n);
    	// check for large radius condition
    	if (java.lang.Double.isInfinite(radius) || radius>CompassFootprint.MAX_RADIUS) {
    		if (dataPoints.size()<2) return;
      	double dx = dataPoints.get(1).getX()-dataPoints.get(0).getX();
      	double dy = dataPoints.get(1).getY()-dataPoints.get(0).getY();
      	double slope = dy/dx;
      	double len = CompassFootprint.MAX_RADIUS/100;
      	if (dx==0) { // vertical line
      		endPoint1.setLocation(edge.x, edge.y-len);
      		endPoint2.setLocation(edge.x, edge.y+len);
      	}
      	else {
	      	if (Math.abs(dx)>Math.abs(dy)) {
	      		endPoint1.setLocation(edge.x-len, edge.y-slope*len);
	      		endPoint2.setLocation(edge.x+len, edge.y+slope*len);
	      	}
	      	else {
	      		endPoint1.setLocation(edge.x-len/slope, edge.y-len);
	      		endPoint2.setLocation(edge.x+len/slope, edge.y+len);
	      	}
      	}
    		setPositionOnLine(xScreen, yScreen, trackerPanel, endPoint1, endPoint2);
    		return;
    	}

      // get image coordinates of the screen point
      if(screenPt==null) {
        screenPt = new Point();
      }
      if(worldPt==null) {
        worldPt = new Point2D.Double();
      }
      screenPt.setLocation(xScreen, yScreen);
      AffineTransform toScreen = trackerPanel.getPixelTransform();
      if(!trackerPanel.isDrawingInImageSpace()) {
        int n = getFrameNumber(trackerPanel);
        toScreen.concatenate(trackerPanel.getCoords().getToWorldTransform(n));
      }
      try {
        toScreen.inverseTransform(screenPt, worldPt);
      } catch(NoninvertibleTransformException ex) {
        ex.printStackTrace();
      }
      // set location to nearest point on circle
      double d = center.distance(worldPt);
      double dx = worldPt.getX()-center.getX();
      double dy = worldPt.getY()-center.getY();
      double r = center.distance(edge);
      double x = center.getX()+r*dx/d;
      double y = center.getY()+r*dy/d;
      setLocation(x, y);
      repaint();
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
	  	compass.firePropertyChange("data", null, compass); //$NON-NLS-1$
   	}
   }

    
  }

  //______________________ inner Center class ________________________

  class Center extends TPoint {
  	
    /**
     * Constructs a Center with specified image coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Center(double x, double y) {
      super(x, y);
//      setStepEditTrigger(true);
    }

//    @Override
//    public void setXY(double x, double y) {
//      if (compass.isFixed()) {
////      	CompassStep keyStep = (CompassStep)compass.steps.getStep(0);
////      	keyStep.slider.setLocation(x, y); // set property of keyStep 0
////      	Point p = keyStep.slider.getScreenPosition(compass.trackerPanel);
////      	keyStep.slider.setPositionOnCircle(p.x, p.y, compass.trackerPanel);
////      	compass.refreshStep(CompassStep.this); // sets properties of this step
//      }
//      else {
//      	setLocation(x, y);
////      	Point p = getScreenPosition(compass.trackerPanel);
//      }
//      repaint();
//      compass.refreshFields(n);
//	  	compass.dataValid = false;
//      if (compass.trackerPanel != null) {
//      	compass.trackerPanel.changed = true;
//      }      
//    }

    @Override
    public int getFrameNumber(VideoPanel vidPanel) {
      return n;
    }

//   /**
//    * Overrides TPoint method.
//    *
//    * @param adjusting true if being dragged
//    */
//   public void setAdjusting(boolean adjusting) {
//   	boolean wasAdjusting = isAdjusting();
//   	super.setAdjusting(adjusting);
//   	if (wasAdjusting && !adjusting) {
//	  	compass.firePropertyChange("data", null, compass); //$NON-NLS-1$
//   	}
//   }
//
    
  }

  //______________________ inner DataPoint class ________________________

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
      if (track.locked) return;
      if (compass.isFixed()) {
      	int pointIndex = 0;
      	for (int i=0; i<CompassStep.this.dataPoints.size(); i++) {
      		DataPoint next = CompassStep.this.dataPoints.get(i);
      		if (next==this) {
      			pointIndex = i;
      			break;
      		}
      	}
      	CompassStep keyStep = (CompassStep)compass.steps.getStep(0);
      	while (keyStep.dataPoints.size()<=pointIndex) {
      		keyStep.dataPoints.add(keyStep.new DataPoint(0, 0));
      	}
      	keyStep.dataPoints.get(pointIndex).setLocation(x, y); // set property of step 0
        keyStep.refreshCircle();
  	    compass.refreshStep(CompassStep.this); // sets properties of this step
      }
      else {
      	setLocation(x, y);
      	compass.keyFrames.add(n);
	      refreshCircle();
    	} 
	  	compass.dataValid = false;
	  	compass.firePropertyChange("data", null, compass); //$NON-NLS-1$
      if (compass.trackerPanel != null) {
      	compass.trackerPanel.changed = true;
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
      CompassStep step = (CompassStep) obj;
      // save data points
      double[][] pointData = new double[step.dataPoints.size()][2];
      for (int i=0; i<pointData.length; i++) {
      	DataPoint next = step.dataPoints.get(i);
      	double[] position = new double[] {next.x, next.y};
      	pointData[i] = position;
      }
      control.setValue("datapoints", pointData); //$NON-NLS-1$
      // save slider position
      double[] sliderData = new double[] {step.slider.x, step.slider.y};
      control.setValue("slider", sliderData); //$NON-NLS-1$
    	if (step.compass!=null && !step.compass.isFixed()) {
    		control.setValue("iskey", step.compass.keyFrames.contains(step.n)); //$NON-NLS-1$
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
    	CompassStep step = (CompassStep)obj;
    	if (step.compass!=null && step.compass.isFixed() && step.n!=0) {
    		step = (CompassStep)step.compass.getStep(0);
    	}
    	if (step.compass!=null && !step.compass.isFixed()) {
    		boolean isKey =control.getBoolean("iskey"); //$NON-NLS-1$
    		if (isKey) {
    			step.compass.keyFrames.add(step.n);
    		}
    		else {
    			step.compass.keyFrames.remove(step.n);
    		}
    	}
    	double[][] pointData = (double[][])control.getObject("datapoints"); //$NON-NLS-1$
    	int diff = pointData.length-step.dataPoints.size();
    	if (diff<0) {
    		// remove data point(s)
    		for (int i=0; i<-diff; i++) {
    			step.removeDataPoint(step.dataPoints.get(step.dataPoints.size()-1));
    		}
    	}
    	else if (diff>0) {
    		// add data point(s)
    		for (int i=0; i<diff; i++) {
    			double[] position = pointData[pointData.length-1-i];
    			step.addDataPoint(position[0], position[1], false);
    		}
    	}
    	for (int i=0; i<pointData.length; i++) {
  			double[] position = pointData[i];
    		step.dataPoints.get(i).setLocation(position[0], position[1]);
    	}
    	double[] sliderData = (double[])control.getObject("slider"); //$NON-NLS-1$
  		step.refreshCircle();
  		step.slider.setLocation(sliderData[0], sliderData[1]);
    	if (step.compass!=null) {
    		final CompassStep cstep = step;
    		Runnable runner = new Runnable() {
    			public void run() {
        		cstep.compass.dataValid = false;
        		cstep.compass.firePropertyChange("data", null, null); //$NON-NLS-1$
    			}
    		};
    		SwingUtilities.invokeLater(runner);
    	}
    	return obj;
    }
  }
}

