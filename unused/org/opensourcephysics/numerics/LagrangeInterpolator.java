/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * LagrangeInterpolator uses a polynomial interpolation formula to evaluate values between data points.
 *
 * @author W. Christian
 * @version 1.0
 */
public class LagrangeInterpolator implements Function {
  /**
   * Polynomial coefficients.
   */
  protected double[] hornerCoef; // generalized Horner expansion coefficients
  double[] xd;
  double[] yd;

  /**
   * Constructs a Lagrange interpolating polynomial from the given data using Horner's expansion for
   * the representation of the polynomial.
   *
   * @param xdata double[]
   * @param ydata double[]
   */
  public LagrangeInterpolator(double[] xdata, double[] ydata) {
    hornerCoef = new double[xdata.length];
    xd = xdata;
    yd = ydata;
    computeCoefficients(xdata, ydata);
  }

  private void computeCoefficients(double[] xd, double[] yd) {
    int n = xd.length;
    // coefficients = new double[size];
    for(int i = 0; i<n; i++) {
      hornerCoef[i] = yd[i];
    }
    n -= 1;
    for(int i = 0; i<n; i++) {
      for(int k = n; k>i; k--) {
        int k1 = k-1;
        int kn = k-(i+1);
        hornerCoef[k] = (hornerCoef[k]-hornerCoef[k1])/(xd[k]-xd[kn]);
      }
    }
  }

  /**
   * Computes the interpolated y value for a given x value.
   * @param  x value
   * @return interpolated y value
   */
  public double evaluate(double x) {
    int n = hornerCoef.length;
    double answer = hornerCoef[--n];
    while(--n>=0) {
      answer = answer*(x-xd[n])+hornerCoef[n];
    }
    return answer;
  }

  /**
   * Gets the polynomial coefficients c in
   * c[0] + c[1] * x + c[2] * x^2 + ....
   * This routine should be used with care because the Vandermonde matrix that is being
   * solved is ill conditioned.  See Press et al.
   *
   * @return double[]
   */
  public double[] getCoefficients() {
    int n = xd.length;
    double[] temp = new double[n];
    double[] coef = new double[n];
    temp[n-1] = -xd[0];
    for(int i = 1; i<n; i++) {
      for(int j = n-i-1; j<n-1; j++) {
        temp[j] -= xd[i]*temp[j+1];
      }
      temp[n-1] -= xd[i];
    }
    for(int j = 0; j<n; j++) {
      double a = n;
      for(int k = n-1; k>=1; k--) {
        a = k*temp[k]+xd[j]*a;
      }
      double b = yd[j]/a;
      double c = 1.0;
      for(int k = n-1; k>=0; k--) {
        coef[k] += c*b;
        c = temp[k]+xd[j]*c;
      }
    }
    return coef;
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
