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

import javax.swing.*;

/**
 * An ArrowFootprint returns an arrow shape for a Point array of length 2.
 *
 * @author Douglas Brown
 */
public class ArrowFootprint extends LineFootprint {

  // instance fields
  protected double stretch = 1;
  protected int tipLength = 16;
  protected int tipWidth = 4;
  boolean openHead = true;
  protected BasicStroke headStroke = new BasicStroke();

  /**
   * Constructs an ArrowFootprint.
   *
   * @param name the name
   */
  public ArrowFootprint(String name) {
    super(name);
  }

  /**
   * Sets the stretch. The length of the arrow is stretched by this factor.
   *
   * @param stretch the desired stretch
   */
  public void setStretch(double stretch) {
    this.stretch = stretch;
  }

  /**
   * Gets the stretch.
   *
   * @return the stretch
   */
  public double getStretch() {
    return stretch;
  }

  /**
   * Sets the length of the arrow tip.
   *
   * @param tipLength the desired tip length in pixels
   */
  public void setTipLength(int tipLength) {
    tipLength = Math.max(32, tipLength);
    tipWidth = tipLength / 4;
    this.tipLength = 4 * tipWidth;
  }

  /**
   * Sets the solid arrowhead property.
   *
   * @param solid true for a filled arrowhead
   */
  public void setSolidHead(boolean solid) {
    openHead = !solid;
  }

  /**
   * Sets the stroke.
   *
   * @param stroke the desired stroke
   */
  @Override
  public void setStroke(BasicStroke stroke) {
    if (stroke == null) return;
    super.setStroke(stroke);
    headStroke = new BasicStroke(stroke.getLineWidth(),
                                  BasicStroke.CAP_BUTT,
                                  BasicStroke.JOIN_MITER,
                                  8,
                                  null,
                                  stroke.getDashPhase());
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
    double prevStretch = stretch;
    stretch = 1;
    Shape shape = getShape(points);
    ShapeIcon icon = new ShapeIcon(shape, w, h);
    icon.setColor(color);
    stretch = prevStretch;
    return icon;
  }

  /**
   * Gets the shape of this footprint.
   *
   * @param points an array of Points
   * @return the shape
   */
  public synchronized Shape getShape(Point[] points) {
    Point p1 = points[0];
    Point p2 = points[1];
    double theta = Math.atan2(p1.y - p2.y, p1.x - p2.x);
    
    transform.setToRotation(theta, p1.x, p1.y);
    transform.translate(p1.x, p1.y);
    highlight = transform.createTransformedShape(HIGHLIGHT);
    
    transform.setToRotation(theta, p2.x, p2.y);
    transform.translate(p2.x, p2.y);
    float d = (float)(stretch * p1.distance(p2)); // length of the arrow
    // set arrowhead dimensions and stroke
    int tipL = Math.min(tipLength, Math.round(d-4));
    tipL = Math.max(8, tipL);
    int tipW = Math.max(tipL/4, 2);
    float f = stroke.getLineWidth();
    BasicStroke s = f < tipL/4? stroke: 
    	new BasicStroke(Math.max(tipL/4, 0.8f),
          BasicStroke.CAP_BUTT,
          BasicStroke.JOIN_MITER,
          8,
          stroke.getDashArray(),
          stroke.getDashPhase());
    try {
			// set up tip hitShape using full length
			path.reset();
			path.moveTo(d-4, 0);
			path.lineTo(d-6, -2);
			path.lineTo(d, 0);
			path.lineTo(d-6, 2);
			path.closePath();
			hitShapes[0] = transform.createTransformedShape(path); // for tip
			// shorten d to account for the width of the stroke
			// see Java 2D API Graphics, by VJ Hardy (Sun, 2000) page 147
			d = d - (float)(s.getLineWidth()*1.58) + 1;
			// set up shaft hitShape
			path.reset();
			path.moveTo(0, 0);
			path.lineTo(d-tipL, 0);
			hitShapes[2] = transform.createTransformedShape(path); // for shaft
			hitShapes[1] = new Rectangle(p2.x-1, p2.y-1, 2, 2);    // for tail
			// set up draw shape
			path.reset();
			path.moveTo(0, 0);
			path.lineTo(d-tipL+tipW, 0);
			Shape shaft = transform.createTransformedShape(path);
			shaft = s.createStrokedShape(shaft);
			Area area = new Area(shaft);
			path.reset();
			path.moveTo(d-tipL+tipW, 0);
			path.lineTo(d-tipL, -tipW);
			path.lineTo(d, 0);
			path.lineTo(d-tipL, tipW);
			path.closePath();
			Shape head = transform.createTransformedShape(path);
			if (openHead) {
				head = headStroke.createStrokedShape(head);
			}
			area.add(new Area(head));
			if (!openHead) {
				area.add(new Area(headStroke.createStrokedShape(head)));
			}
			return area;
		} catch (Exception e) { // occasionally throws path exception for reasons unknown!
	    d = (float)(stretch * p1.distance(p2));
			java.awt.geom.Line2D line = new java.awt.geom.Line2D.Double(0, 0, d, 0); 
			return s.createStrokedShape(transform.createTransformedShape(line));
		}
  }
}
