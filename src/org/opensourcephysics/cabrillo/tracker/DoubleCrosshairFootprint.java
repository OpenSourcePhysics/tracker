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
import java.awt.geom.Area;
import java.awt.geom.Line2D;

import javax.swing.Icon;

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
  public Icon getIcon(int w, int h) {
    Shape target = stroke.createStrokedShape(targetShape);
    Area area = new Area(target);
    double x0 = size/2-w+2;
    double y0 = h-size/2-2;
    double d = Math.sqrt(x0*x0+y0*y0);
    double x1 = x0*size/d;
    double y1 = y0*size/d;
    Line2D line = new Line2D.Double(x0, y0, x1, y1);
    area.add(new Area(stroke.createStrokedShape(line)));
    ShapeIcon icon = new ShapeIcon(area, w, h);
    icon.setColor(color);
    return icon;
  }

  /**
   * Sets the stroke.
   *
   * @param stroke the desired stroke
   */
  public void setStroke(BasicStroke stroke) {
    if (stroke == null) return;
    this.stroke = stroke;
  }

  /**
   * Gets the shape of this footprint.
   *
   * @param points an array of Points
   * @return the shape
   */
  public Shape getShape(Point[] points) {
    Point p1 = points[0];
    Point p2 = points[1];
    
    // set up end shapes
    transform.setToTranslation(p1.x, p1.y);
    Shape target1 = transform.createTransformedShape(targetShape);
    hitShapes[0] = transform.createTransformedShape(hitShape); // end1
    transform.setToTranslation(p2.x, p2.y);
    Shape target2 = transform.createTransformedShape(targetShape);
    hitShapes[1] = transform.createTransformedShape(hitShape); // end2
    
    // set up line shapes
    float d = (float)p1.distance(p2); // distance between ends
    float center = d/2; // center point
    float l = d - 2*size-6; // line length
    float f = 0.45f; // hit shape is 90% of line length
    path.reset();
    path.moveTo(center - f*l, 0);
    path.lineTo(center + f*l, 0);
    double theta = Math.atan2(p1.y - p2.y, p1.x - p2.x);
    transform.setToRotation(theta, p2.x, p2.y);
    transform.translate(p2.x, p2.y);
    hitShapes[2] = transform.createTransformedShape(path); // line    
    path.reset();
    path.moveTo(center - l/2, 0);
    path.lineTo(center + l/2, 0);
    Shape line = transform.createTransformedShape(path);
    
    // set up drawing area
    Area area = new Area(stroke.createStrokedShape(target1));
    area.add(new Area(stroke.createStrokedShape(target2)));
    area.add(new Area(stroke.createStrokedShape(line)));
    return area;
  }
}
