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
import org.opensourcephysics.numerics.Function;
import org.opensourcephysics.numerics.Polynomial;

/**
 * Computes Laguerre polynomials and associated Laguerre polynomials.
 *
 * @author W. Christian
 * @version 1.0
 */
public class Legendre {
  static final ArrayList<Polynomial> legendreList;
  static final Map<QNKey, AssociatedLegendreFunction> associatedMap = new HashMap<QNKey, AssociatedLegendreFunction>();

  private Legendre() {} // all methods are static so prohibit instantiation

  /**
   * Gets the el-th Legendre polynomial. If the polynomial has already been calculated
   * it is returned  from the list. Uses the recurrence relationship to construct new polynomials
   * based on lower order polynomials.
   */
  public static synchronized Polynomial getPolynomial(int el) {
    if(el<0) {
      throw new IllegalArgumentException(Messages.getString("Legendre.neg_degree")); //$NON-NLS-1$
    }
    if(el<legendreList.size()) {
      return legendreList.get(el);
    }
    Polynomial pk = new Polynomial(new double[] {0, (2.0*el-1.0)/el});
    Polynomial p1 = getPolynomial(el-1).multiply(pk);
    Polynomial p2 = getPolynomial(el-2).multiply((1.0-el)/el);
    Polynomial p = p1.add(p2);
    legendreList.add(p); // polynomial was not in the list so add it.
    return p;
  }

  /**
   * Gets the associated Legendre function. If the function has already been calculated
   * it is returned  from the map. Uses the Legendre polynomial recurrence relationship to construct new functions
   * based on derivatives of lower order polynomials.
   */
  public static synchronized Function getAssociatedFunction(int el, int m) {
    if(m*m>el*el) {
      throw new IllegalArgumentException(Messages.getString("Legendre.out_of_range_m")); //$NON-NLS-1$
    }
    QNKey key = new QNKey(el, m);
    AssociatedLegendreFunction f = associatedMap.get(key);
    if(f!=null) {
      return f;
    }
    f = new AssociatedLegendreFunction(el, m);
    associatedMap.put(key, f); // polynomial was not in the list so add it.
    return f;
  }

  /**
   * Evaluates the el-th Legendre polynomial at x.
   *
   * @return the value of the function
   */
  public static double evaluate(int el, double x) {
    return getPolynomial(el).evaluate(x);
  }

  /**
   * Computes the AssoicatedLegendre function.
   * @author Wolfgang Christian
   */
  static class AssociatedLegendreFunction implements Function {
    Polynomial p;
    int n, m;
    boolean oddPower, positiveM;

    AssociatedLegendreFunction(int el, int m) {
      this.n = el;
      positiveM = (m>0);
      m = Math.abs(m); // following definition valid for positive m
      this.m = m;
      oddPower = (m%2==1);
      p = getPolynomial(n);
      for(int i = 0; i<m; i++) {
        p = p.derivative();
      }
      if(positiveM&&oddPower) { // odd positive m terms have negative sign
        p = p.multiply(-1);
      }
      if(!positiveM) { // apply definition for negative m
        p = p.multiply(Factorials.factorial(n-m)/Factorials.factorial(n+m));
      }
    }

    /**
     * Evaluates the associated Legendre function.
     */
    public double evaluate(final double x) {
      double val = Math.pow((1-x*x), m/2)*p.evaluate(x); // power function is more efficient with integer values
      return oddPower ? val*Math.sqrt(1-x*x) : val;
    }

  }

  static {
    // seed the first two Legendre polynomials
    legendreList = new ArrayList<Polynomial>();
    Polynomial p0 = new Polynomial(new double[] {1.0});
    legendreList.add(p0);
    Polynomial p1 = new Polynomial(new double[] {0, 1.0});
    legendreList.add(p1);
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
