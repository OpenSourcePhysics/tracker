/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2024 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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
 * <https://opensourcephysics.github.io/tracker/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.util.Collection;
import java.util.HashSet;

import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.ResizableIcon;
import org.opensourcephysics.tools.FontSizer;

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
	public static final float[] DOTTED_LINE = new float[] {2, 6};
	public static final float[] STIPPLED_LINE = new float[] {2, 2};
  private static final ProtractorFootprint CIRCLE_3, CIRCLE_5, 
  		CIRCLE_3_BOLD, CIRCLE_5_BOLD;

	// static fields
  protected static int arcRadius = 24;
  private static Collection<ProtractorFootprint> footprints 
			= new HashSet<ProtractorFootprint>();
  private static Shape hitShape = new Ellipse2D.Double(-6, -6, 12, 12);
  private static MultiShape arrowhead;
  private static Line2D line1 = new Line2D.Double(), line2 = new Line2D.Double();
  private static Point p = new Point();
  private static AffineTransform transform = new AffineTransform();
  private static Arc2D arc = new Arc2D.Double(-arcRadius, -arcRadius, 
  		2*arcRadius, 2*arcRadius, 0, 0, Arc2D.OPEN);

  // instance fields
  protected String name;
  protected BasicStroke baseStroke, stroke;
  protected Color color = Color.black;
  protected Shape[] hitShapes = new Shape[6];
	protected Shape circle;
	protected int radius;
  private Stroke arcStroke, arcAdjustStroke, armStroke;
  private boolean isArcVisible;

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

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDisplayName() {
  	return TrackerRes.getString(name);
  }

  @Override
  public int getLength() {
    return 3;
  }

  @Override
  public ResizableIcon getIcon(int w, int h) {
    transform.setToScale(1, 1);
    Shape shape = transform.createTransformedShape(circle);
  	if (stroke==null || stroke.getLineWidth()!=baseStroke.getLineWidth()) {
  		stroke = new BasicStroke(baseStroke.getLineWidth());
  		arcStroke = new BasicStroke(1);
      arcAdjustStroke = new BasicStroke(stroke.getLineWidth(),
          BasicStroke.CAP_BUTT,
          BasicStroke.JOIN_MITER,
          8,
          DOTTED_LINE,
          stroke.getDashPhase());  
  	}
    MultiShape drawShape = new MultiShape(shape).andStroke(stroke);
    double x0 = (radius+2)-w;
    double y0 = h-(radius+2);
    double d = Math.sqrt(x0*x0+y0*y0);
    double x1 = x0*radius/d;
    double y1 = y0*radius/d;
    drawShape.addDrawShape(new Line2D.Double(x0, y0, x1, y1), stroke);
    drawShape.addDrawShape(new Line2D.Double(x0, y0, radius-2, y0), stroke);
    ShapeIcon icon = new ShapeIcon(drawShape, w, h);
    icon.setColor(color);
    return new ResizableIcon(icon);
  }

  @Override
  public Mark getMark(Point[] points) {
    MultiShape shape = getShape(points, FontSizer.getIntegerFactor());
    return new Mark() {
      @Override
      public void draw(Graphics2D g, boolean highlighted) {
        Color gcolor = g.getColor();
        Stroke gstroke = g.getStroke();
        g.setColor(color);
        g.setStroke(stroke);
        if (OSPRuntime.setRenderingHints) g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        shape.draw(g);
        g.setColor(gcolor);
        g.setStroke(gstroke);
      }
    };
  }

  @Override
  public Shape[] getHitShapes() {
    return hitShapes;
  }

  @Override
  public void setStroke(BasicStroke stroke) {
    if (stroke == null) return;
    baseStroke = new BasicStroke(stroke.getLineWidth(),
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
    armStroke = new BasicStroke(stroke.getLineWidth(),
        BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_MITER,
        8,
        STIPPLED_LINE,
        stroke.getDashPhase());  
   }

  @Override
  public BasicStroke getStroke() {
    return baseStroke;
  }

  @Override
  public void setColor(Color color) {
    this.color = color;
  }

  @Override
  public Color getColor() {
    return color;
  }
  
  public void setArcVisible(boolean vis) {
  	isArcVisible = vis;
  }
  
  /**
   * Gets a circle shape.
   *
   * @param p the desired screen point of the circle
   * @return the circle shape
   */
  public MultiShape getCircleShape(Point p) {
  	transform.setToTranslation(p.x, p.y);
    int scale = FontSizer.getIntegerFactor();
    if (scale>1) {
    	transform.scale(scale, scale);
    }
  	Shape shape = transform.createTransformedShape(circle);
  	return new MultiShape(shape).andStroke(stroke);
  }

  /**
   * Gets an arcAdjust shape.
   *
   * @param vertex the screen point of the vertex
   * @param rotator the screen point of the rotator 
   * 
   * @return the arc-adjusting shape
   */
  public MultiShape getArcAdjustShape(Point vertex, Point rotator) {
  	double theta = Math.toRadians(arc.getAngleStart()+arc.getAngleExtent()/2);
    int scale = FontSizer.getIntegerFactor();
	  p.x = (int)Math.round(vertex.x + scale*arcRadius*Math.cos(theta));
	  p.y = (int)Math.round(vertex.y - scale*arcRadius*Math.sin(theta));
	  MultiShape circle = getCircleShape(p);
    MultiShape drawShape = new MultiShape(circle);
  	if (rotator!=null) {
      int r = circle.getBounds().width/2;
      double d = p.distance(rotator);
		  line1.setLine(p.getX(), p.getY(), rotator.getX(), rotator.getY());
	    if (d>1) adjustLineLength(line1, (d-r)/d, (d-6)/d);
	    drawShape.addDrawShape((Line2D)line1.clone(), arcAdjustStroke);
  	}
  	return drawShape;
  }

  /**
   * Gets the shape of this footprint for a Point array {vertex, end1, end2}.
   * Also sets up hit shapes {vertex, end1, end2, line1, line2, rotator}
   *
   * @param points an array of Points
   * @return the shape
   */
  @Override
public MultiShape getShape(Point[] points, int scale) {
    Point vertex = points[0];
    Point end1 = points[1];
    Point end2 = points[2];
    int r = scale*circle.getBounds().width/2;
    
    // set up strokes
  	if (stroke==null || stroke.getLineWidth()!=scale*baseStroke.getLineWidth()) {
  		stroke = new BasicStroke(scale*baseStroke.getLineWidth());
  		arcStroke = new BasicStroke(scale);
      arcAdjustStroke = new BasicStroke(stroke.getLineWidth(),
          BasicStroke.CAP_BUTT,
          BasicStroke.JOIN_MITER,
          8,
          DOTTED_LINE,
          stroke.getDashPhase());  
      armStroke = new BasicStroke(stroke.getLineWidth(),
          BasicStroke.CAP_BUTT,
          BasicStroke.JOIN_MITER,
          8,
          STIPPLED_LINE,
          stroke.getDashPhase());  
  	}
  	
    MultiShape drawMe = new MultiShape();
    
    // set up line shapes
    line1.setLine(vertex, end1); // "fixed" x-axis base: angles measured ccw from this
    double d1 = vertex.distance(end1);
    if (d1>1) adjustLineLength(line1, 1, (d1-r)/d1);
    drawMe.addDrawShape((Line2D)line1.clone(), null);
    
    line2.setLine(vertex, end2); // "movable" arm
    double d2 = vertex.distance(end2);    
    if (d2>1) adjustLineLength(line2, 1, (d2-r)/d2);
    drawMe.addDrawShape((Line2D)line2.clone(), armStroke);
    
    // add line end shapes
    transform.setToTranslation(end1.x, end1.y);
    if (scale>1) {
    	transform.scale(scale, scale);
    }    
    Shape end1Shape = transform.createTransformedShape(circle);
    drawMe.addDrawShape(end1Shape, null);
    transform.setToTranslation(end2.x, end2.y);
    if (scale>1) {
    	transform.scale(scale, scale);
    }
    Shape end2Shape = transform.createTransformedShape(circle);
    drawMe.addDrawShape(end2Shape, null);
    
    // arc shape
    double theta1 = -Math.atan2(end1.y - vertex.y, end1.x - vertex.x);
    double theta2 = -Math.atan2(end2.y - vertex.y, end2.x - vertex.x);
    arc.setAngleStart(Math.toDegrees(theta1));
    double degrees = Math.toDegrees(theta2-theta1);
    if (degrees > 180) degrees -= 360;
    if (degrees < -180) degrees += 360;
    arc.setAngleExtent(degrees);
    transform.setToTranslation(vertex.x, vertex.y);
    if (scale>1) {
    	transform.scale(scale, scale);
    }
    Shape arcShape = transform.createTransformedShape(arc);
    if (isArcVisible) {
	    drawMe.addDrawShape(arcShape, arcStroke);
	    
	    // arrowhead where arc hits line2
	    if (Math.abs(degrees)>10) {
		    double xDot = vertex.getX() + scale*arcRadius*(end2.getX()-vertex.getX())/d2;
		    double yDot = vertex.getY() + scale*arcRadius*(end2.getY()-vertex.getY())/d2;
		    double angle = -theta2-Math.PI/2;
		    if (degrees<0)
		    	angle += Math.PI;
		    transform.setToRotation(angle, xDot, yDot);
		    transform.translate(xDot, yDot);
		    if (scale>1) {
		    	transform.scale(scale, scale);
		    }
		    Shape arrowShape = arrowhead.transform(transform);
		    drawMe.addFillShape(arrowShape);
	    }
    }
    
    // hit shapes    
    transform.setToTranslation(vertex.x, vertex.y);
    if (scale>1) {
    	transform.scale(scale, scale);
    }
    hitShapes[0] = transform.createTransformedShape(hitShape); // vertex
    transform.setToTranslation(end1.x, end1.y);
    if (scale>1) {
    	transform.scale(scale, scale);
    }
    hitShapes[1] = transform.createTransformedShape(hitShape); // end1
    transform.setToTranslation(end2.x, end2.y);
    if (scale>1) {
    	transform.scale(scale, scale);
    }
    hitShapes[2] = transform.createTransformedShape(hitShape); // end2
    if (d1>1) adjustLineLength(line1, (d1-scale*arcRadius-8)/d1, (d1-8)/d1);
    if (d2>1) adjustLineLength(line2, (d2-scale*arcRadius-8)/d2, (d2-8)/d2);
    hitShapes[3] = (Line2D)line1.clone();
    hitShapes[4] = (Line2D)line2.clone();
    hitShapes[5] = arcShape;
    
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
  	arrowhead = new MultiShape(path).andStroke(stroke);
  	  	
    // create standard footprints
    CIRCLE_3 = new ProtractorFootprint("ProtractorFootprint.Circle3", 3); //$NON-NLS-1$
    CIRCLE_3.setStroke(stroke);
    footprints.add(CIRCLE_3);
    
    CIRCLE_5 = new ProtractorFootprint("ProtractorFootprint.Circle5", 8); //$NON-NLS-1$
    CIRCLE_5.setStroke(stroke);
    footprints.add(CIRCLE_5);

  	stroke = new BasicStroke(2);
    CIRCLE_3_BOLD = new ProtractorFootprint("ProtractorFootprint.Circle3Bold", 3); //$NON-NLS-1$
  	CIRCLE_3_BOLD.setStroke(stroke);
    footprints.add(CIRCLE_3_BOLD);

    CIRCLE_5_BOLD = new ProtractorFootprint("ProtractorFootprint.Circle5Bold", 8); //$NON-NLS-1$
  	CIRCLE_5_BOLD.setStroke(stroke);
    footprints.add(CIRCLE_5_BOLD);
  }
}
