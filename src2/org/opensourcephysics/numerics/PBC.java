/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * Implements methods to support periodic boundary condtions.
 *
 * @author W. Christian
 */
public class PBC {
  private PBC() {} // prohibit instantiation

  /**
   * Adjusts the postion of a particle assuming peridoic boundary conditions.
   * The postion will be in the interval [0,size).
   *
   * @param r double
   * @param size double
   * @return double
   */
  public static double position(final double r, final double size) {
    return(r<0) ? r%size+size : r%size;
  }

  /**
   * Adjusts the postion of a particle assuming peridoic boundary conditions.
   * The postion will be in the interval [0,size).
   *
   * @param r int
   * @param size int
   * @return int
   */
  public static int position(final int r, final int size) {
    return(r<0) ? (r+1)%size+size-1 : r%size;
  }

  /**
   * Computes the minimum separation using periodic boundary conditions.
   *
   * @param dr double the separation
   * @param size double the box size
   * @return double
   */
  public static double separation(final double dr, final double size) {
    return dr-size*Math.floor(dr/size+0.5);
  }

  /**
   * Computes the minimum separation using periodic boundary conditions.
   * @param dr int  the separation
   * @param size int the box size
   * @return int
   */
  public static int separation(final int dr, final int size) {
    if(dr<0) {
      return dr+size*((-2*dr+size)/(2*size));
    }
    return dr-size*((2*dr+size-1)/(2*size));
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
