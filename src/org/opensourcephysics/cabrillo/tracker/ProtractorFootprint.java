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
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.Icon;

/**
 * A ProtractorFootprint returns a pair of lines that meet at a vertex
 * at one end and have a specified end shape at the other.
 * This requires a Point array of length 3.
 *
 * @author Douglas Brown
 */
public class ProtractorFootprint implements Footprint, Cloneable {

	// static constants
  @SuppressWarnings("javadoc")
	public static final float[] DOTTED_LINE = new float[] {2, 2};
  private static final ProtractorFootprint CIRCLE_3, CIRCLE_5, 
  		CIRCLE_3_BOLD, CIRCLE_5_BOLD;

	// static fields
  protected static int arcRadius = 24;
  private static Collection<ProtractorFootprint> footprints 
			= new HashSet<ProtractorFootprint>();
  private static Shape hitShape = new Ellipse2D.Double(-6, -6, 12, 12);
  private static Shape arrowhead;
  private static Line2D line1 = new Line2D.Double(), line2 = new Line2D.Double();
  private static Point p = new Point();
  private static AffineTransform transform = new AffineTransform();
  private static Arc2D arc = new Arc2D.Double(-arcRadius, -arcRadius, 
  		2*arcRadius, 2*arcRadius, 0, 0, Arc2D.OPEN);
  private static BasicStroke arcStroke = new BasicStroke();


  // instance fields
  protected String name;
  protected BasicStroke stroke;
  protected Color color = Color.black;
  protected Shape[] hitShapes = new Shape[6];
	protected Shape circle;
	protected int radius;
  private Stroke arcAdjustStroke;

  /**
   * Constructs a ProtractorFootprint.
   *
   * @param name the name
   * @param r the radius
   */
  public ProtractorFootprint(String name, int r) {
    this.name = name;
  	radius = r;
  	circle = new Ellipse2D.Double(-r, -r, 2*r, 2*r);
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
  	Shape shape = stroke.createStrokedShape(circle);
    Area area = new Area(shape);
    double x0 = radius-w+2;
    double y0 = h-radius-2;
    double d = Math.sqrt(x0*x0+y0*y0);
    double x1 = x0*radius/d;
    double y1 = y0*radius/d;
    Line2D line = new Line2D.Double(x0, y0, x1, y1);
    area.add(new Area(stroke.createStrokedShape(line)));
    line.setLine(x0, y0, radius-2, y0);
    area.add(new Area(stroke.createStrokedShape(line)));
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
    return hitShapes;
  }

  /**
   * Sets the stroke.
   *
   * @param stroke the desired stroke
   */
  public void setStroke(BasicStroke stroke) {
    if (stroke == null) return;
    this.stroke = new BasicStroke(stroke.getLineWidth(),
                                  BasicStroke.CAP_BUTT,
                                  BasicStroke.JOIN_MITER,
                                  8,
                                  stroke.getDashArray(),
                                  stroke.getDashPhase());
    arcAdjustStroke = new BasicStroke(stroke.getLineWidth(),
        BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_MITER,
        8,
        DOTTED_LINE,
        stroke.getDashPhase());  
   }

  /**
   * Gets the stroke.
   *
   * @return the stroke
   */
  public BasicStroke getStroke() {
    return stroke;
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
   * Gets a circle shape.
   *
   * @param p the desired screen point of the circle
   * @return the circle shape
   */
  public Shape getCircleShape(Point p) {
  	transform.setToTranslation(p.x, p.y);
  	Shape shape = stroke.createStrokedShape(circle);
  	shape = transform.createTransformedShape(shape);
  	return shape;
  }

  /**
   * Gets an arcAdjust shape.
   *
   * @param vertex the screen point of the vertex
   * @param rotator the screen point of the rotator 
   * 
   * @return the arc-adjusting shape
   */
  public Shape getArcAdjustShape(Point vertex, Point rotator) {
  	double theta = Math.toRadians(arc.getAngleStart()+arc.getAngleExtent()/2);
	  p.x = (int)Math.round(vertex.x + arcRadius*Math.cos(theta));
	  p.y = (int)Math.round(vertex.y - arcRadius*Math.sin(theta));
	  Shape circle = getCircleShape(p);
    Area area = new Area(circle);
  	if (rotator!=null) {
      int r = circle.getBounds().width/2;
      double d = p.distance(rotator);
		  line1.setLine(p.getX(), p.getY(), rotator.getX(), rotator.getY());
	    if (d>1) adjustLineLength(line1, (d-r)/d, (d-6)/d);
	    area.add(new Area(arcAdjustStroke.createStrokedShape(line1)));
  	}
  	return area;
  }

  /**
   * Gets the shape of this footprint for a Point array {vertex, end1, end2}.
   * Also sets up hit shapes {vertex, end1, end2, line1, line2, rotator}
   *
   * @param points an array of Points
   * @return the shape
   */
  public Shape getShape(Point[] points) {
    Point vertex = points[0];
    Point end1 = points[1];
    Point end2 = points[2];
    int r = circle.getBounds().width/2;
    
    // line1 and line2 shapes
    line1.setLine(vertex, end1);
    double d1 = vertex.distance(end1);
    if (d1>1) adjustLineLength(line1, 1, (d1-r)/d1);
    line2.setLine(vertex, end2);
    double d2 = vertex.distance(end2);    
    if (d2>1) adjustLineLength(line2, 1, (d2-r)/d2);
    
    // end1 & end2 shapes
    transform.setToTranslation(end1.x, end1.y);
    Shape end1Shape = transform.createTransformedShape(circle);
    end1Shape = stroke.createStrokedShape(end1Shape);
    transform.setToTranslation(end2.x, end2.y);
    Shape end2Shape = transform.createTransformedShape(circle);
    end2Shape = stroke.createStrokedShape(end2Shape);
    
    // arc shape
    double theta1 = -Math.atan2(end1.y - vertex.y, end1.x - vertex.x);
    double theta2 = -Math.atan2(end2.y - vertex.y, end2.x - vertex.x);
    arc.setAngleStart(Math.toDegrees(theta1));
    double degrees = Math.toDegrees(theta2-theta1);
    if (degrees > 180) degrees -= 360;
    if (degrees < -180) degrees += 360;
    arc.setAngleExtent(degrees);
    transform.setToTranslation(vertex.x, vertex.y);
    Shape arcShape = transform.createTransformedShape(arc);
    
    // arrowhead where arc hits line2
    Shape dotShape = null;
    if (Math.abs(degrees)>10) {
	    double xDot = vertex.getX() + arcRadius*(end2.getX()-vertex.getX())/d2;
	    double yDot = vertex.getY() + arcRadius*(end2.getY()-vertex.getY())/d2;
	    double angle = -theta2-Math.PI/2;
	    if (degrees<0)
	    	angle += Math.PI;
	    transform.setToRotation(angle, xDot, yDot);
	    transform.translate(xDot, yDot);
	    dotShape = transform.createTransformedShape(arrowhead);
    }
   
    Area drawMe = new Area(stroke.createStrokedShape(line1));
		drawMe.add(new Area(stroke.createStrokedShape(line2)));
    drawMe.add(new Area(end1Shape));
    drawMe.add(new Area(end2Shape));
    drawMe.add(new Area(arcStroke.createStrokedShape(arcShape)));
    if (dotShape!=null)  drawMe.add(new Area(dotShape));
    
    // hit shapes    
    transform.setToTranslation(vertex.x, vertex.y);
    hitShapes[0] = transform.createTransformedShape(hitShape); // vertex
    transform.setToTranslation(end1.x, end1.y);
    hitShapes[1] = transform.createTransformedShape(hitShape); // end1
    transform.setToTranslation(end2.x, end2.y);
    hitShapes[2] = transform.createTransformedShape(hitShape); // end2
    if (d1>1) adjustLineLength(line1, (d1-arcRadius-8)/d1, (d1-8)/d1);
    if (d2>1) adjustLineLength(line2, (d2-arcRadius-8)/d2, (d2-8)/d2);
    hitShapes[3] = stroke.createStrokedShape(line1);
    hitShapes[4] = stroke.createStrokedShape(line2);
    hitShapes[5] = stroke.createStrokedShape(arcShape);
    
    return drawMe;
  }
  
  /**
   * Adjusts the length of a Line. Each end is adjusted independently
   * by a specified factor.
   *
   * @param line the Line
   * @param end1Factor the factor by which to change end1
   * @param end2Factor the factor by which to change end2
   */
  private static void adjustLineLength(Line2D line, double end1Factor, double end2Factor) {
  	double x1 = line.getX2() + (line.getX1()-line.getX2())*end1Factor;
  	double y1 = line.getY2() + (line.getY1()-line.getY2())*end1Factor;
  	double x2 = line.getX1() + (line.getX2()-line.getX1())*end2Factor;
  	double y2 = line.getY1() + (line.getY2()-line.getY1())*end2Factor;
  	line.setLine(x1, y1, x2, y2);
  }
  
  /**
   * Gets a predefined Footprint.
   *
   * @param name the name of the footprint
   * @return the footprint
   */
  public static Footprint getFootprint(String name) {
    for (ProtractorFootprint footprint: footprints) {
      if (name == footprint.getName()) try {
        return (ProtractorFootprint)footprint.clone();
      } catch(CloneNotSupportedException ex) {ex.printStackTrace();}
    }
    return null;
  }
  
  // static initializers
  static {
  	BasicStroke stroke = new BasicStroke(1);
  	  	
  	GeneralPath path = new GeneralPath();
  	path.moveTo(-6, 2);
  	path.lineTo(0, 0);
  	path.lineTo(-6, -3);
  	arrowhead = stroke.createStrokedShape(path);
  	  	
    // create standard footprints
    CIRCLE_3 = new ProtractorFootprint("ProtractorFootprint.Circle3", 3); //$NON-NLS-1$
    CIRCLE_3.setStroke(stroke);
    footprints.add(CIRCLE_3);
    
    CIRCLE_5 = new ProtractorFootprint("ProtractorFootprint.Circle5", 5); //$NON-NLS-1$
    CIRCLE_5.setStroke(stroke);
    footprints.add(CIRCLE_5);

  	stroke = new BasicStroke(2);
    CIRCLE_3_BOLD = new ProtractorFootprint("ProtractorFootprint.Circle3Bold", 3); //$NON-NLS-1$
  	CIRCLE_3_BOLD.setStroke(stroke);
    footprints.add(CIRCLE_3_BOLD);

    CIRCLE_5_BOLD = new ProtractorFootprint("ProtractorFootprint.Circle5Bold", 5); //$NON-NLS-1$
  	CIRCLE_5_BOLD.setStroke(stroke);
    footprints.add(CIRCLE_5_BOLD);
  }
}
