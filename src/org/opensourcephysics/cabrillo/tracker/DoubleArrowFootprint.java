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

import javax.swing.Icon;

/**
 * An DoubleArrowFootprint returns a double arrow shape
 * for a Point array of length 2.
 *
 * @author Douglas Brown
 */
public class DoubleArrowFootprint extends LineFootprint {

  // instance fields
  protected int tipLength = 16;
  protected int tipWidth = 4;
  boolean openHead = true;
  protected BasicStroke headStroke = new BasicStroke();

  /**
   * Constructs a DoubleArrowFootprint.
   *
   * @param name the name
   */
  public DoubleArrowFootprint(String name) {
    super(name);
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
    Shape shape = getShape(points, false);
    ShapeIcon icon = new ShapeIcon(shape, w, h);
    icon.setColor(color);
    return icon;
  }

  /**
   * Sets the length of the arrow tip.
   *
   * @param tipLength the desired tip length in pixels
   */
  public void setTipLength(int tipLength) {
    tipLength = Math.max(8, tipLength);
    tipWidth = tipLength / 4;
    this.tipLength = 4 * tipWidth;
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
   * Sets the solid arrowhead property.
   *
   * @param solid true for a filled arrowhead
   */
  public void setSolidHead(boolean solid) {
    openHead = !solid;
  }

  /**
   * Gets the shape of this footprint.
   *
   * @param points an array of Points
   * @return the shape
   */
  public Shape getShape(Point[] points) {
    return getShape(points, true);
  }
  
  /**
   * Gets the shape of this footprint.
   *
   * @param points an array of Points
   * @param bothEnds true to draw both ends
   * @return the shape
   */
  private Shape getShape(Point[] points, boolean bothEnds) {
    Point p1 = points[0];
    Point p2 = points[1];
    double theta = Math.atan2(p1.y - p2.y, p1.x - p2.x);
    transform.setToRotation(theta, p2.x, p2.y);
    transform.translate(p2.x, p2.y);
    float d = (float) p1.distance(p2); // length of the line
    // set arrowhead dimensions and stroke
    int tipL = tipLength;
    if (bothEnds) 
    	tipL = Math.min(tipLength, Math.round(d/2-3));
    tipL = Math.max(8, tipL);
    int tipW = Math.max(tipL/4, 3);
    float f = stroke.getLineWidth();
    BasicStroke s = f < tipL/4? stroke: 
    	new BasicStroke(Math.max(tipL/4, 0.8f),
          BasicStroke.CAP_BUTT,
          BasicStroke.JOIN_MITER,
          8,
          stroke.getDashArray(),
          stroke.getDashPhase());
    // set up tip hitShape using full length
    path.reset();
    path.moveTo(d - 4, 0);
    path.lineTo(d - 6, -2);
    path.lineTo(d, 0);
    path.lineTo(d - 6, 2);
    path.closePath();
    hitShapes[0] = transform.createTransformedShape(path); // for tip
    // set up tail hitShape
    path.reset();
    path.moveTo(4, 0);
    path.lineTo(6, -2);
    path.lineTo(0, 0);
    path.lineTo(6, 2);
    path.closePath();
    hitShapes[1] = transform.createTransformedShape(path); // for tail
    // set up shaft hitShape
    float center = d/2; // center point
    float l = d - 2 * tipL; // center section length
    path.reset();
    path.moveTo(center - 0.45f*l, 0);
    path.lineTo(center + 0.45f*l, 0);
    hitShapes[2] = transform.createTransformedShape(path); // for shaft
    // shorten d to account for the width of the stroke
    // see Java 2D API Graphics, by VJ Hardy (Sun, 2000) page 147
    float w = (float) (s.getLineWidth() * 1.58) - 1;
    d = d - w;
    
		// set up draw shape
		path.reset();
		path.moveTo(tipL-tipW, 0);
		path.lineTo(bothEnds? d-tipL+tipW: d, 0);
		Shape shaft = transform.createTransformedShape(path);
		shaft = s.createStrokedShape(shaft);
		Area area = new Area(shaft);
		path.reset();
	  path.moveTo(w+tipL-tipW, 0);
	  path.lineTo(w+tipL, tipW);
	  path.lineTo(w, 0);
	  path.lineTo(w+tipL, -tipW);
		path.closePath();
		Shape end1 = transform.createTransformedShape(path);
		if (openHead) {
			end1 = headStroke.createStrokedShape(end1);
		}
		area.add(new Area(end1));
		if (!openHead) {
			area.add(new Area(headStroke.createStrokedShape(end1)));
		}
		if (bothEnds) {
			path.reset();
			path.moveTo(d-tipL+tipW, 0);
			path.lineTo(d-tipL, -tipW);
			path.lineTo(d, 0);
			path.lineTo(d-tipL, tipW);
			path.closePath();
			Shape end2 = transform.createTransformedShape(path);
			if (openHead) {
				end2 = headStroke.createStrokedShape(end2);
			}
			area.add(new Area(end2));
			if (!openHead) {
				area.add(new Area(headStroke.createStrokedShape(end2)));
			}
		}
		return area;
  }

}
