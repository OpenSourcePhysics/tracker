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
 * A Footprint creates a mark for a track step.
 *
 * @author Douglas Brown
 */
public interface Footprint {

  /**
   * Gets the name of the footprint.
   *
   * @return the name
   */
  public String getName();

  /**
   * Gets the display name of the footprint.
   *
   * @return the name
   */
  public String getDisplayName();

  /**
   * Gets the point array length required by this footprint.
   *
   * @return the length
   */
  public int getLength();

  /**
   * Gets an icon representing the footprint.
   *
   * @param w width of the icon
   * @param h height of the icon
   * @return the icon
   */
  public Icon getIcon(int w, int h);

  /**
   * Gets the hit shapes associated with the footprint.
   *
   * @return an array of hit shapes
   */
  public Shape[] getHitShapes();

  /**
   * Gets the footprint mark.
   *
   * @param points a Point array
   * @return the mark
   */
  public Mark getMark(Point[] points);

  /**
   * Gets the footprint shape.
   *
   * @param points a Point array
   * @return the shape
   */
  public Shape getShape(Point[] points);

  /**
   * Sets the stroke. Accepts only basic strokes.
   *
   * @param stroke the desired stroke
   */
  public void setStroke(BasicStroke stroke);

  /**
   * Gets the stroke.
   *
   * @return the basic stroke
   */
  public BasicStroke getStroke();

  /**
   * Sets the color.
   *
   * @param color the desired color
   */
  public void setColor(Color color);

  /**
   * Gets the color.
   *
   * @return the color
   */
  public Color getColor();

}
