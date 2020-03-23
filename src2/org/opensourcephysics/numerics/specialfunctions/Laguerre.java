/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.specialfunctions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.opensourcephysics.numerics.Polynomial;

/**
 * Computes Laguerre polynomials and associated Laguerre polynomials.
 *
 * @author W. Christian
 * @version 1.0
 */
public class Laguerre {
  static final ArrayList<Polynomial> laguerreList;
  static final Map<QNKey, Polynomial> associatedLaguerreMap = new HashMap<QNKey, Polynomial>();

  private Laguerre() {} // all methods are static so prohibit instantiation

  /**
   * Gets the n-th Laguerre polynomial. If the polynomial has already been calculated
   * it is returned  from the list. Uses the recurrence relationship to construct new polynomials
   * based on lower order polynomials.
   */
  public static synchronized Polynomial getPolynomial(int n) {
    if(n<0) {
      throw new IllegalArgumentException(Messages.getString("Laguerre.neg_degree")); //$NON-NLS-1$
    }
    if(n<laguerreList.size()) {
      return laguerreList.get(n);
    }
    Polynomial pk = new Polynomial(new double[] {(2.0*n-1.0)/n, -1.0/n});
    Polynomial p1 = getPolynomial(n-1).multiply(pk);
    Polynomial p2 = getPolynomial(n-2).multiply((1.0-n)/n);
    Polynomial p = p1.add(p2);
    laguerreList.add(p); // polynomial was not in the list so add it.
    return p;
  }

  /**
   * Gets the associated Laguerre polynomial. If the polynomial has already been calculated
   * it is returned  from the list. Uses the recurrence relationship to construct new polynomials
   * based on lower order polynomials.
   */
  public static synchronized Polynomial getPolynomial(int n, int k) {
    if(k<0) {
      throw new IllegalArgumentException(Messages.getString("Laguerre.neg_k")); //$NON-NLS-1$
    }
    QNKey key = new QNKey(n, k);
    Polynomial p = associatedLaguerreMap.get(key);
    if(p!=null) {
      return p;
    }
    p = getPolynomial(n+k);
    int sign = 1;
    for(int i = 0; i<k; i++) {
      sign *= -1;
      p = p.derivative();
    }
    if(sign==-1) {
      p = p.multiply(sign);
    }
    associatedLaguerreMap.put(key, p); // polynomial was not in the list so add it.
    return p;
  }

  /**
   * Evaluates the n-th Laguerre polynomial at x.
   *
   * @return the value of the function
   */
  public static double evaluate(int n, double x) {
    return getPolynomial(n).evaluate(x);
  }

  static {
    // seed the first two Laguerre polynomials
    laguerreList = new ArrayList<Polynomial>();
    Polynomial p0 = new Polynomial(new double[] {1.0});
    laguerreList.add(p0);
    Polynomial p1 = new Polynomial(new double[] {1, -1});
    laguerreList.add(p1);
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
