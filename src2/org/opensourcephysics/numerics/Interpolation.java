/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * Class Interpolation defines simple interpolation algorithms.
 * This class cannot be subclassed or instantiated because all methods are static.
 *
 * @author Wolfgang Christian
 */
public class Interpolation {
  private Interpolation() {}

  /**
   * Linear interpolation at a single point x.
   * @param x double
   * @param x0 double
   * @param x1 double
   * @param y0 double
   * @param y1 double
   * @return double
   */
  static public double linear(final double x, final double x0, final double x1, final double y0, final double y1) {
    if((x1-x0)==0) {
      return(y0+y1)/2;
    }
    return y0+(x-x0)*(y1-y0)/(x1-x0);
  }

  /**
   * Lagrange polynomial interpolation at a single point x.
   *
   * Because Lagrange polynomials tend to be ill behaved, this method should be used with care.
   *
   * A LagrangeInterpolator object should be used if multiple interpolations
   * are to be performed using the same data.
   *
   * @param x double
   * @param xd double[] the x data
   * @param yd double[] the y data
   * @return double
   */
  static public double lagrange(final double x, final double[] xd, final double yd[]) {
    if(xd.length!=yd.length) {
      throw new IllegalArgumentException("Arrays must be of equal length."); //$NON-NLS-1$
    }
    double sum = 0;
    for(int i = 0, n = xd.length; i<n; i++) {
      if(x-xd[i]==0) {
        return yd[i];
      }
      double product = yd[i];
      for(int j = 0; j<n; j++) {
        if((i==j)||(xd[i]-xd[j]==0)) {
          continue;
        }
        product *= (x-xd[i])/(xd[i]-xd[j]);
      }
      sum += product;
    }
    return sum;
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
