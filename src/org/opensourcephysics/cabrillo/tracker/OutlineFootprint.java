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
import javax.swing.*;

/**
 * An OutlineFootprint returns an outline shape for a Point array of length 2.
 *
 * @author Douglas Brown
 */
public class OutlineFootprint extends LineFootprint {

  // instance fields
  private int spread; // outline width is (2 + 2*spread) pixels

  /**
   * Constructs an OutlineFootprint.
   *
   * @param name the name of the footprint
   */
  public OutlineFootprint(String name) {
    super(name);
    setStroke(stroke);
  }

  /**
   * Sets the spread. The width of the outline is (1 + 2*spread).
   *
   * @param spread the desired spread
   */
  public void setSpread(int spread) {
    this.spread = spread;
  }

  /**
   * Gets the spread.
   *
   * @return the spread
   */
  public int getSpread() {
    return spread;
  }

  /**
   * Gets the icon.
   *
   * @param w width of the icon
   * @param h height of the icon
   * @return the icon
   */
  public Icon getIcon(int w, int h) {
    Point[] points = new Point[] {new Point(), new Point(w - 2, 2 - h)};
    int prevSpread = spread;
    spread = 1;
    Shape shape = getShape(points);
    ShapeIcon icon = new ShapeIcon(shape, w, h);
    icon.setColor(color);
    spread = prevSpread;
    return icon;
  }

  /**
   * Overrides LineProfile setStroke method.
   *
   * @param stroke the desired stroke
   */
  public void setStroke(BasicStroke stroke) {
    super.setStroke(stroke);
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
    double theta = Math.atan2(p1.y - p2.y, p1.x - p2.x);
    transform.setToRotation(theta, p2.x, p2.y);
    transform.translate(p2.x, p2.y);
    float d = (float)p1.distance(p2); // length of the line
    // create outline
    path.reset();
    path.moveTo(0, -1 - spread);
    path.lineTo(0, 1 + spread);
    path.lineTo(d, 1 + spread);
    path.lineTo(d, -1 - spread);
    path.closePath();
    // handle marker
    int w = Math.min(spread + 1, 4);
    path.moveTo(d/2, w);
    path.lineTo(d/2, -w);
    // centerline
    if (getSpread() > 4) {
      path.moveTo(0, 0);
      path.lineTo(d, 0);
    }
    Shape outline = transform.createTransformedShape(path); // outline shape
    outline = stroke.createStrokedShape(outline);
    // ceate hitshapes
    path.reset();
    path.moveTo(d, -1 - spread);
    path.lineTo(d, 1 + spread);
    hitShapes[0] = transform.createTransformedShape(path); // end 1
    path.reset();
    path.moveTo(0, -1 - spread);
    path.lineTo(0, 1 + spread);
    hitShapes[1] = transform.createTransformedShape(path); // end 2
    // set handle hitshape to center line
    float f = 0.45f; // hitshape will be 90% of tape length
    path.reset();
    path.moveTo(d * (0.5f + f), 0);
    path.lineTo(d * (0.5f - f), 0);
    hitShapes[2] = transform.createTransformedShape(path); // sides
    return outline;
  }
}

