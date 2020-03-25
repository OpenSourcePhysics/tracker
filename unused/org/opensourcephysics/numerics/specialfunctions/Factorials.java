/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.specialfunctions;

/**
 * Computes the factorial of an integer and functions closely related to factorials.
 */
public class Factorials {
  static double[] cof = {76.18009172947146, -86.50532032941677, 24.01409824083091, -1.231739572450155, 0.1208650973866179e-2, -0.5395239384953e-5};
  static long[] fac;

  private Factorials() { // prohibit instantiation because all methods are static
  }

  /**
   * Calculates the logarithm of the Gamma function using the Lanczos approximation.
   * @param x double
   * @return double
   */
  public static double gammaln(final double x) {
    double y = x, tmp = x+5.5;
    tmp -= (x+0.5)*Math.log(tmp);
    double sum = 1.000000000190015;
    for(int j = 0; j<=5; j++) {
      sum += cof[j]/++y;
    }
    return -tmp+Math.log(2.5066282746310005*sum/x);
  }

  /**
   * Calculates the factorial.
   * @param n int
   * @return double
   */
  public static double factorial(final int n) {
    if(n<0) {
      throw new IllegalArgumentException(Messages.getString("Factorials.neg_val")); //$NON-NLS-1$
    }
    if(n<fac.length) {
      return fac[n];
    }
    return Math.exp(gammaln(n+1.0));
  }

  static {
    long val = 1;
    int maxN = 1;
    while(val*maxN>=val) {
      val = val*maxN;
      maxN++;
    }
    fac = new long[maxN]; // maxN should be ~21
    fac[0] = 1;
    for(int i = 1; i<maxN; i++) {
      fac[i] = fac[i-1]*i;
    }
  }

  /**
   * Returns log (n!) = log (n * (n-1) * ... 2 * 1)
   *
   * @param n
   * @return log(n!)
   */
  public static double logFactorial(int n) {
    if(n<0) {
      throw new IllegalArgumentException(Messages.getString("Factorials.log_neg_val")); //$NON-NLS-1$
    }
    return Factorials.gammaln(n+1.0);
  }

  /**
   * Returns the Poisson distribution (nu^n e^(-nu) / n!)
   *
   * @param nu
   * @param n
   * @return poisson_nu(n)
   */
  public static double poisson(double nu, int n) {
    return Math.exp(n*Math.log(nu)-nu-logFactorial(n));
  }

  /**
   * Returns the logarithm of the binomial coefficient (n, k)
   * In other notation: log (n choose k)
   * (n choose k) represents the number of ways of picking k unordered outcomes from n possibilities
   *
   * @param n
   * @param k
   * @return log (n choose k)
   */
  public static double logChoose(int n, int k) {
    return logFactorial(n)-logFactorial(k)-logFactorial(n-k);
  }
  /*
   *  --------------- test code  ---------------
   * public static void main(String args[]) {
   * System.out.println(Factorials.factorial(3));
   * System.out.println(Factorials.factorial(20));
   * System.out.println(Factorials.factorial(21));
   * System.out.println(Factorials.factorial(22));
   * }
   */

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
