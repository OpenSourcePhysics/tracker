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

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.Icon;

import org.opensourcephysics.tools.FontSizer;

/**
 * A CircleFitterFootprint returns a circle, center point and data point marks.
 * It requires a minimum Point array of length 2 {center, edge} but accommodates many data points.
 *
 * @author Douglas Brown
 */
public class CircleFitterFootprint implements Footprint, Cloneable {

	// static constants
  @SuppressWarnings("javadoc")
  private static BasicStroke hitStroke = new BasicStroke(4);
  private static final CircleFitterFootprint CIRCLE_4, CIRCLE_7, 
  		CIRCLE_4_BOLD, CIRCLE_7_BOLD, CIRCLE_4_POINTS_ONLY;
  protected static final int MAX_RADIUS = 100000;

	// static fields
  private static Collection<CircleFitterFootprint> footprints 
			= new HashSet<CircleFitterFootprint>();
  private static Shape hitShape = new Ellipse2D.Double(-6, -6, 12, 12);
  private static Shape emptyHitShape = new Rectangle();
  private static Line2D line = new Line2D.Double();
  private static AffineTransform transform = new AffineTransform();
  private static Arc2D.Float iconArc = new Arc2D.Float(); 

  // instance fields
  protected String name;
  protected BasicStroke baseStroke, stroke;
  protected Color color = Color.black;
  protected ArrayList<Shape> hitShapes = new ArrayList<Shape>();
	protected Ellipse2D circle;
	protected double radius;
	protected Shape marker;
	protected Shape crosshatch;
	protected int markerSize;
	protected Point selectedPoint;
	protected int markedPointCount;
	protected boolean drawCircle = true;

  /**
   * Constructs a CircleFitterFootprint.
   *
   * @param name the name
   * @param size the radius
   */
  public CircleFitterFootprint(String name, int size) {
    this.name = name;
  	markerSize = size;
  	circle = new Ellipse2D.Double();
  	marker = new Ellipse2D.Double(-size, -size, 2*size, 2*size);
  	double d = size*0.707;
  	GeneralPath path = new GeneralPath();
  	path.moveTo(-d, -d);
  	path.lineTo(d, d);
  	path.moveTo(-d, d);
  	path.lineTo(d, -d);
  	crosshatch = path;
  	setStroke(new BasicStroke());
  }

  /**
   * Gets the name of this footprint.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the display name of the footprint.
   *
   * @return the localized display name
   */
  public String getDisplayName() {
  	return TrackerRes.getString(name);
  }

  /**
   * Gets the minimum point array length required by this footprint.
   *
   * @return the length
   */
  public int getLength() {
    return 3;
  }

  /**
   * Gets the icon.
   *
   * @param w width of the icon
   * @param h height of the icon
   * @return the icon
   */
  public Icon getIcon(int w, int h) {  	
    int scale = FontSizer.getIntegerFactor();
    w *= scale;
    h *= scale;
    Shape shape = emptyHitShape;
  	if (stroke==null || stroke.getLineWidth()!=scale*baseStroke.getLineWidth()) {
  		stroke = new BasicStroke(scale*baseStroke.getLineWidth());
  	}
    if (drawCircle) {
	    iconArc.setArc(0, 0, scale*20, scale*20, 200, 140, Arc2D.OPEN);
	  	shape = stroke.createStrokedShape(iconArc);
    }
    Area area = new Area(shape);
    int r = markerSize/2;
    circle.setFrameFromCenter(scale*10, scale*20, scale*(10+r), scale*(20+r));
  	shape = stroke.createStrokedShape(circle);
  	area.add(new Area(shape));
    ShapeIcon icon = new ShapeIcon(area, w, h);
    icon.setColor(color);
    return icon;
  }

  /**
   * Gets the footprint mark.
   *
   * @param points a Point array
   * @return the mark
   */
  public Mark getMark(Point[] points) {
    final Shape shape = getShape(points);
    final Color color = this.color;
    return new Mark() {
      public void draw(Graphics2D g, boolean highlighted) {
        Color gcolor = g.getColor();
        g.setColor(color);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.fill(shape);
        g.setColor(gcolor);
      }

      public Rectangle getBounds(boolean highlighted) {
        return shape.getBounds();
      }
    };
  }

  /**
   * Gets the hit shapes {vertex, end1, end2, line1, line2, rotator}.
   *
   * @return the hit shapes
   */
  public Shape[] getHitShapes() {
    return hitShapes.toArray(new Shape[hitShapes.size()]);
  }

  /**
   * Sets the stroke.
   *
   * @param stroke the desired stroke
   */
  public void setStroke(BasicStroke stroke) {
    if (stroke == null) return;
    baseStroke = new BasicStroke(stroke.getLineWidth(),
                                  BasicStroke.CAP_BUTT,
                                  BasicStroke.JOIN_MITER,
                                  8,
                                  stroke.getDashArray(),
                                  stroke.getDashPhase());
   }

  /**
   * Gets the stroke.
   *
   * @return the stroke
   */
  public BasicStroke getStroke() {
    return baseStroke;
  }

  /**
   * Sets the color.
   *
   * @param color the desired color
   */
  public void setColor(Color color) {
    this.color = color;
  }

  /**
   * Gets the color.
   *
   * @return the color
   */
  public Color getColor() {
    return color;
  }
  
  /**
   * Sets the radius of the datapoint circle.
   *
   * @param r the radius
   */
  protected void setPixelRadius(double r) {
  	radius = r;
  }

  /**
   * Sets the visibility of the circle.
   *
   * @param vis true to draw the circle
   */
  protected void setCircleVisible(boolean vis) {
  	drawCircle = vis;
  }

  /**
   * Sets the selected screen point. The selected point is not drawn so CircleStep 
   * can draw a selection shape instead.
   *
   * @param p the selected screen point (may be null)
   */
  protected void setSelectedPoint(Point p) {
  	selectedPoint = p;
  }
  
  /**
   * Sets the marked point count. Marked points are drawn differently than attached points.
   *
   * @param n the number of user-marked points in the step
   */
  protected void setMarkedPointCount(int n) {
  	markedPointCount = n;
  }
  
  /**
   * Gets the shape of this footprint for a Point array {center, edge, data0, data1, ...}.
   * Also sets up hit shapes {circle, center, data1, data2, ...}
   *
   * @param points an array of Points
   * @return the shape
   */
  public Shape getShape(Point[] points) {
    Point center = points[0];
    Point edge = points[1];
    hitShapes.clear();
    Area drawMe = new Area();
    int scale = FontSizer.getIntegerFactor();
  	if (stroke==null || stroke.getLineWidth()!=scale*baseStroke.getLineWidth()) {
  		stroke = new BasicStroke(scale*baseStroke.getLineWidth());
  	}
        
    // draw shapes only if there are 3 or more data points (plus center & edge)
    if (drawCircle && points.length>=5) {
    	// special case: infinite or very large radius, so draw straight line thru edge
    	if (Double.isInfinite(radius) || radius>MAX_RADIUS) {
    		double x = edge.getX();
    		double y = edge.getY();
    		// get slope of line
      	double dx = points[3].getX()-points[2].getX();
      	double dy = points[3].getY()-points[2].getY();
      	double slope = dy/dx;
      	// draw long line to extend past window bounds
      	double len = MAX_RADIUS/10;
      	if (dx==0) { // vertical line
      		line.setLine(x, y-len, x, y+len);
      	}
      	else {
	      	if (Math.abs(dx)>Math.abs(dy)) {
	      		line.setLine(x-len, y-slope*len, x+len, y+slope*len);	      		
	      	}
	      	else {
	      		line.setLine(x-len/slope, y-len, x+len/slope, y+len);	      		
	      	}
      	}
		    drawMe.add(new Area(stroke.createStrokedShape(line)));
		    hitShapes.add(hitStroke.createStrokedShape(line));
    	}
    	else { // standard case
	    	// circle
	      circle.setFrameFromCenter(center.x, center.y, center.x+radius, center.y+radius);
		    drawMe.add(new Area(stroke.createStrokedShape(circle)));
		    hitShapes.add(stroke.createStrokedShape(circle));
	    
	    	// center
	      transform.setToTranslation(points[0].x, points[0].y);
	      if (scale>1) {
	      	transform.scale(scale, scale);
	      }
	      Shape s = transform.createTransformedShape(marker);
	      drawMe.add(new Area(stroke.createStrokedShape(s)));
		    hitShapes.add(stroke.createStrokedShape(s)); // center hit shape
	      s = transform.createTransformedShape(crosshatch);
	      drawMe.add(new Area(stroke.createStrokedShape(s)));	    
    	}
    }
    while (hitShapes.size()<2) {
	    hitShapes.add(emptyHitShape);  // add empty hit shapes to pad
    }
        
    // always draw data points
    for (int i=2; i<points.length; i++) {
      transform.setToTranslation(points[i].x, points[i].y);
      if (scale>1) {
      	transform.scale(scale, scale);
      }
      if (points[i]!=selectedPoint) {
	      Shape s = transform.createTransformedShape(marker);
      	drawMe.add(new Area(stroke.createStrokedShape(s)));
	      if (i>=2+markedPointCount) {
	      	drawMe.add(new Area(s));
	      }
      }
      hitShapes.add(transform.createTransformedShape(hitShape));
    }
    
    return drawMe;
  }
  
  /**
   * Gets a predefined Footprint.
   *
   * @param name the name of the footprint
   * @return the footprint
   */
  public static Footprint getFootprint(String name) {
    for (CircleFitterFootprint footprint: footprints) {
      if (name == footprint.getName()) try {
        return (CircleFitterFootprint)footprint.clone();
      } catch(CloneNotSupportedException ex) {ex.printStackTrace();}
    }
    return null;
  }
  
  // static initializers
  static {
  	BasicStroke stroke = new BasicStroke(1);
  	  	
    // create standard footprints
    CIRCLE_4 = new CircleFitterFootprint("CircleFitterFootprint.Circle4", 4); //$NON-NLS-1$
    CIRCLE_4.setStroke(stroke);
    footprints.add(CIRCLE_4);
    
    CIRCLE_7 = new CircleFitterFootprint("CircleFitterFootprint.Circle7", 7); //$NON-NLS-1$
    CIRCLE_7.setStroke(stroke);
    footprints.add(CIRCLE_7);
    
    CIRCLE_4_POINTS_ONLY = new CircleFitterFootprint("CircleFitterFootprint.Circle4.PointsOnly", 4); //$NON-NLS-1$
    CIRCLE_4_POINTS_ONLY.setStroke(stroke);
    CIRCLE_4_POINTS_ONLY.setCircleVisible(false);
    footprints.add(CIRCLE_4_POINTS_ONLY);

  	stroke = new BasicStroke(2);
    CIRCLE_4_BOLD = new CircleFitterFootprint("CircleFitterFootprint.Circle4Bold", 4); //$NON-NLS-1$
  	CIRCLE_4_BOLD.setStroke(stroke);
    footprints.add(CIRCLE_4_BOLD);

    CIRCLE_7_BOLD = new CircleFitterFootprint("CircleFitterFootprint.Circle7Bold", 7); //$NON-NLS-1$
  	CIRCLE_7_BOLD.setStroke(stroke);
    footprints.add(CIRCLE_7_BOLD);
  }
}
