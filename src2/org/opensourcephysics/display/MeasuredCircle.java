/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import org.opensourcephysics.controls.XML;

/**
 * A drawable circle that implements Measurable.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public class MeasuredCircle extends Circle implements Measurable {
  boolean enableMeasure = true;

  /**
   * Constructs a MeasuredCircle with the given center.
   *
   * @param x the x position of the center
   * @param y the y position of the center
   */
  public MeasuredCircle(double x, double y) {
    super(x, y);
  }

  /**
   * Enables the measured flag so that this circle effects the scale of a drawing panel.
   *
   * @return minimum
   */
  public void setMeasured(boolean _enableMeasure) {
    enableMeasure = _enableMeasure;
  }

  /**
   * Determines if this circle should effect the scale of a drawing panel.
   *
   * @return minimum
   */
  public boolean isMeasured() {
    return enableMeasure;
  }

  /**
   * Implements measurable by getting the x center of the circle.
   *
   * @return minimum
   */
  public double getXMin() {
    return x;
  }

  /**
   * Implements measurable by getting the x center of the circle.
   *
   * @return maximum
   */
  public double getXMax() {
    return x;
  }

  /**
   * Implements measurable by getting the y center of the circle.
   *
   * @return minimum
   */
  public double getYMin() {
    return y;
  }

  /**
   * Implements measurable by getting the y center of the circle.
   *
   * @return maximum
   */
  public double getYMax() {
    return y;
  }

  /**
   * Gets a loader allows a Circle to be represented as XML data.
   * Objects without XML loaders cannot be saved and retrieved from an XML file.
   *
   * @return ObjectLoader
   */
  public static XML.ObjectLoader getLoader() {
    return new CircleLoader(); // use the same loader as the circle
  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2017  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
