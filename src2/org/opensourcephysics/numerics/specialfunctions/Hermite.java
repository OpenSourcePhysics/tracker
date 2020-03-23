/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.specialfunctions;
import java.util.ArrayList;
import org.opensourcephysics.numerics.Polynomial;

/**
 * Calculates Hermite polynomials.
 *
 * @author W. Christian
 * @version 1.0
 */
public class Hermite {
  static final ArrayList<Polynomial> hermiteList;
  static final Polynomial twoX = new Polynomial(new double[] {0, 2.0}); // 2x used in recursion

  private Hermite() {}                                                  // all methods are static so prohibit instantiation

  /**
   * Gets the n-th Hermite polynomial. If it has already been calculated
   * it just returns it from the list. If we have not calculated it uses
   * the recursion relationship to construct the polynomial based on the prior
   * polynomials.
   *
   * @param n degree of polynomial
   */
  public static synchronized Polynomial getPolynomial(int n) {
    if(n<0) {
      throw new IllegalArgumentException(Messages.getString("Hermite.neg_degree")); //$NON-NLS-1$
    }
    if(n<hermiteList.size()) {
      return hermiteList.get(n);
    }
    Polynomial p1 = getPolynomial(n-1).multiply(twoX);
    Polynomial p2 = getPolynomial(n-2).multiply(2*(n-1));
    Polynomial p = p1.subtract(p2);
    hermiteList.add(p); // polynomial was not on the list so add it.
    return p;
  }

  /**
   * Evaluates the n-th Hermite polynomial at x.
   *
   * @return the value of the function
   */
  public static double evaluate(int n, double x) {
    return getPolynomial(n).evaluate(x);
  }

  static {
    hermiteList = new ArrayList<Polynomial>();
    Polynomial p = new Polynomial(new double[] {1.0});
    hermiteList.add(p);
    p = new Polynomial(new double[] {0, 2.0});
    hermiteList.add(p);
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
