/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * Transformation maps coordinates from one coordinate system to another.
 */
public interface Transformation extends Cloneable {
  /**
   * Provides a copy of this transformation.
   * This is used by an OSP 3D Element that will explicitely get a clone of
   * it whenever its setTransformation() method is invoked.
   * Thus, changing the original transformation directly
   * has no effect unless a new setTransformation is invoked.
   */
  public Object clone();

  /**
   * Transforms a given point
   * @param point double[] the coordinates to be transformed
   * (the array's contents will be changed accordingly)
   * @return double[] the transformed vector (i.e. point)
   */
  public double[] direct(double[] point);

  /**
   * The inverse transformation (if it exists).
   * If the transformation is not invertible, then a call to this
   * method must throw a UnsupportedOperationException exception.
   * @param point double[] the coordinates to be transformed
   * (the array's contents will be changed accordingly)
   * @return double[] the transformed vector (i.e. point)
   * @throws UnsupportedOperationException If the transformation is
   * not invertible
   */
  public double[] inverse(double[] point) throws UnsupportedOperationException;

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
