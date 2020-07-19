/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2019  Douglas Brown
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
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;

import org.opensourcephysics.tools.FontSizer;

/**
 * A double crosshair footprint for a Point array of length 2.
 *
 * @author Douglas Brown
 */
public class DoubleCrosshairFootprint extends LineFootprint {

  // instance fields
	protected Shape targetShape;
  protected int size;
  protected Shape hitShape;

  /**
   * Constructs a DoubleCrosshairFootprint.
   *
   * @param name the name
   */
  public DoubleCrosshairFootprint(String name) {
    super(name);
    setCrosshairSize(4, 0);
  }

  /**
  /**
   * Sets the size of the crosshair.
   *
   * @param out the outside end of the crosshair
   * @param in the inside end of the crosshair
   */
  public void setCrosshairSize(int out, int in) {
    size = out;
    // make target shape
    path.reset();
    path.moveTo(-out, 0);
    path.lineTo(-in, 0);
    path.moveTo(out, 0);
    path.lineTo(in, 0);
    path.moveTo(0, out);
    path.lineTo(0, in);
    path.moveTo(0, -out);
    path.lineTo(0, -in);
    transform.setToIdentity();
    targetShape = transform.createTransformedShape(path);
    hitShape = new Rectangle(-size/2, -size/2, size, size);
  }

  /**
   * Gets the icon.
   *
   * @param w width of the icon
   * @param h height of the icon
   * @return the icon
   */
  @Override
public Icon getIcon(int w, int h) {
		int scale = FontSizer.getIntegerFactor();
		w *= scale;
		h *= scale;
		Point[] points = new Point[] { new Point(), new Point(w - 2, 2 - h) };
		MultiShape shape = getShape(points, false);
		ShapeIcon icon = new ShapeIcon(shape, w, h);
		icon.setColor(color);
		icon.setStroke(stroke);
    return icon;
  }

  /**
   * Sets the stroke.
   *
   * @param stroke the desired stroke
   */
  @Override
public void setStroke(BasicStroke stroke) {
    if (stroke == null) return;
    this.baseStroke = stroke;
  }

  /**
   * Gets the shape of this footprint.
   *
   * @param points an array of Points
   * @return the shape
   */
  @Override
public MultiShape getShape(Point[] points) {
    return getShape(points, true);
  }
  
  /**
   * Gets the shape of this footprint.
   *
   * @param points an array of Points
   * @param bothEnds true to draw both ends (single end used for icon)
   * @return the shape
   */
  private MultiShape getShape(Point[] points, boolean bothEnds) {
    Point p1 = points[0];
    Point p2 = points[1];
    int scale = FontSizer.getIntegerFactor();
    
    
//    // for line shapes
//    float d = (float)p1.distance(p2); // distance between ends
//    float center = d/2; // center point
//    float l = Math.max(d - scale*2*(size+3), size); // line length
//    
//    // set up crosshair end shapes
//    transform.setToTranslation(p1.x, p1.y);
//    if (scale>1) {
//    	transform.scale(scale, scale);
//    }
//    Shape target1 = transform.createTransformedShape(targetShape);    
//    hitShapes[0] = transform.createTransformedShape(hitShape); // end1
//    transform.setToTranslation(p2.x, p2.y);
//    if (scale>1) {
//    	transform.scale(scale, scale);
//    }
//    Shape target2 = transform.createTransformedShape(targetShape);
//    hitShapes[1] = transform.createTransformedShape(hitShape); // end2
//    
//    double theta = Math.atan2(p1.y - p2.y, p1.x - p2.x);
//    if (Double.isNaN(theta)) {
//    	theta = 0;
//    }
//    transform.setToRotation(theta, p2.x, p2.y);
//    transform.translate(p2.x, p2.y);
//    
//  	// set up line shapes
//    float f = 0.45f; // hit shape is 90% of line length
//    path.reset();
//    path.moveTo(center - f*l, 0);
//    path.lineTo(center + f*l, 0);
//    hitShapes[2] = transform.createTransformedShape(path); // line  
//    
//    path.reset();
//    path.moveTo(center - l/2, 0);
//    path.lineTo(center + l/2, 0);
//    Shape line = transform.createTransformedShape(path);
    
    // float code above, double code below
    
    // for line shapes
    double d = p1.distance(p2); // distance between ends
    double center = d/2; // center point
    double setback = size + 6; // pixel space between line and crosshair
    double l = Math.max(d - scale*2*setback, size); // line length
    
    // set up crosshair end shapes
    transform.setToTranslation(p1.x, p1.y);
    if (scale>1) {
    	transform.scale(scale, scale);
    }
    Shape target1 = transform.createTransformedShape(targetShape);    
    hitShapes[0] = transform.createTransformedShape(hitShape); // end1
    transform.setToTranslation(p2.x, p2.y);
    if (scale>1) {
    	transform.scale(scale, scale);
    }
    Shape target2 = transform.createTransformedShape(targetShape);
    hitShapes[1] = transform.createTransformedShape(hitShape); // end2
    
    double theta = Math.atan2(p1.y - p2.y, p1.x - p2.x);
    if (Double.isNaN(theta)) {
    	theta = 0;
    }
    transform.setToRotation(theta, p2.x, p2.y);
    transform.translate(p2.x, p2.y);
    
  	// set up line shapes
    double f = 0.45; // hit shape is 90% of line length
    path.reset();
    path.moveTo(center - f*l, 0);
    path.lineTo(center + f*l, 0);
    hitShapes[2] = transform.createTransformedShape(path); // line  
    
    path.reset();
    path.moveTo(center - l/2, 0);
    path.lineTo(center + l/2, 0);
    Shape line = transform.createTransformedShape(path);

//    Rectangle2D rect0 = hitShapes[0].getBounds2D();
//    Rectangle2D rect1 = hitShapes[1].getBounds2D();
//    double x0 = rect0.getCenterX();
//    double y0 = rect0.getCenterY();
//    double x1 = rect1.getCenterX();
//    double y1 = rect1.getCenterY();
//    double dx = x1 - x0;
//    double dy = y1 - y0;
//    double shortenByFraction = setback / Math.sqrt(dx*dx + dy*dy);
//    
//    path.moveTo(x0 + shortenByFraction * dx, y0 + shortenByFraction * dy);
//    path.lineTo(x0 + (1-shortenByFraction) * dx, y0 + (1-shortenByFraction) * dy);
//    Shape line = (Shape)path.clone();
    
    
    // set up stroke
  	if (stroke==null || stroke.getLineWidth()!=scale*baseStroke.getLineWidth()) {
  		stroke = new BasicStroke(scale*baseStroke.getLineWidth());
  	}
  	
    // return draw shape
  	return bothEnds? new MultiShape(line, target1, target2): new MultiShape(line, target2);
  }

}
