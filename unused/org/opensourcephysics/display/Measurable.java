/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;

/**
 * A Measurable object returns min and max values for its x and y extent.
 * This interface is used to autoscale the x and y axes on a drawing panel.
 *
 * Minimum and maximum values may NOT be valid if isMeasured returns false.  Objects that
 * store data, for example, usually return zero if data is null.
 *
 * Copyright:    Copyright (c) 2005
 * @author Wolfgang Christian
 * @version 1.0
 */
public interface Measurable extends Drawable {
  /**
   * Gets the minimum x needed to draw this object.
   * @return minimum
   */
  public double getXMin();

  /**
   * Gets the maximum x needed to draw this object.
   * @return maximum
   */
  public double getXMax();

  /**
   * Gets the minimum y needed to draw this object.
   * @return minimum
   */
  public double getYMin();

  /**
   * Gets the maximum y needed to draw this object.
   * @return minimum
   */
  public double getYMax();

  /**
   * Determines if information is available to set min/max values.
   * Objects that store data should return false if data is null.
   *
   * @return true if min/max values are valid
   */
  public boolean isMeasured(); // set to true if the measure is valid.

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
