/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display.axes;

/**
 * CartesianAxes defines common Cartesian coordinate methods.
 *
 * @author W. Christian
 */
public interface CartesianAxes extends DrawableAxes {
  /**
   * Sets the drawing location.
   * @param x
   */
  public void setX(double x);

  /**
   * Sets the drawing location.
   * @param y
   */
  public void setY(double y);

  /**
   * Gets the drawing location.
   * @return the x location
   */
  public double getX();

  /**
   * Gets the drawing location.
   * @return the y location
   */
  public double getY();

  /**
   *  Determines if the x axis is logarithmic.
   *
   * @return  true if logarithmic; false otherwise
   */
  public boolean isXLog();

  /**
   *  Deteermines if the y axis is logarithmic.
   *
   * @return  true if logarithmic; false otherwise
   */
  public boolean isYLog();

  /**
   * Sets the x axis to linear or logarithmic.
   *
   * @param isLog true for log scale; false otherwise
   */
  void setXLog(boolean isLog);

  /**
   * Sets the y axis to linear or logarithmic.
   *
   * @param isLog true for log scale; false otherwise
   */
  void setYLog(boolean isLog);

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
