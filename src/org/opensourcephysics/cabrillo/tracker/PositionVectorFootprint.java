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
import java.awt.geom.*;

import javax.swing.Icon;

/**
 * A PositionVectorFootprint returns a vector shape for a Point[] of length 2,
 * but creates a hit shape only at the tip of the vector.
 */
public class PositionVectorFootprint extends PointShapeFootprint {

  // instance fields
	TTrack track;
	LineFootprint arrow;
	Point[] onePoint = new Point[1];

  /**
   * Constructs a PositionVectorFootprint.
   *
   * @param track the (PointMass) track that will use this footprint
   * @param name the name of the footprint
   * @param w the stroke line width
   */
  public PositionVectorFootprint(TTrack track, String name, int w) {
    super(name, new Ellipse2D.Double(-2, -2, 4, 4));
    this.track = track;
    arrow = (LineFootprint)LineFootprint.getFootprint("Footprint.Arrow"); //$NON-NLS-1$
    arrow.setLineWidth(w);
    stroke = null;
  }
  
  /**
   * Gets the fill shape for a specified point.
   *
   * @param points an array of points
   * @return the fill shape
   */
  public Shape getShape(Point[] points) {
  	super.getShape(points); // this sets up hitShapes[] at vector tip
    return arrow.getShape(points);
  }

  /**
   * Gets the icon.
   *
   * @param w width of the icon
   * @param h height of the icon
   * @return the icon
   */
  public Icon getIcon(int w, int h) {
  	arrow.setColor(color);
    return arrow.getIcon(w, h);
  }

  /**
   * Sets the stroke. May be set to null.
   *
   * @param stroke the desired stroke
   */
  public void setStroke(BasicStroke stroke) {
    arrow.setStroke(stroke);
  }

  /**
   * Gets the stroke. May return null;
   *
   * @return the stroke
   */
  public BasicStroke getStroke() {
    return arrow.getStroke();
  }

  /**
   * Sets the line width.
   *
   * @param w the desired line width
   */
  public void setLineWidth(double w) {
    arrow.setLineWidth(w);
  }

}