/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * Polynomial least square fit without any error estimation.
 *
 * See Object Oriented Implementation of Numerical Methods by Didier H. Besset for fitting with error estimation.
 *
 * @author Wolfgang Christian.
 */
public class PolynomialLeastSquareFit extends Polynomial {
  double[][] systemMatrix;
  double[] systemConstants;

  /**
   * Constructs a PolynomialLeastSquareFit with the given order.
   * @param xd double[]
   * @param yd double[]
   * @param degree int the degree of the polynomial
   */
  public PolynomialLeastSquareFit(double[] xd, double[] yd, int degree) {
    super(new double[degree+1]);
    int ncoef = degree+1;
    systemMatrix = new double[ncoef][ncoef];
    systemConstants = new double[ncoef];
    // added by Doug Brown 12/1/05
    fitData(xd, yd);
  }

  /**
   * Constructs a PolynomialLeastSquareFit with the given coefficients.
   * Added by D Brown 12/20/2005.
   * @param coeffs the coefficients
   */
  public PolynomialLeastSquareFit(double[] coeffs) {
    super(coeffs);
    int n = coeffs.length;
    systemMatrix = new double[n][n];
    systemConstants = new double[n];
  }

  /**
   * Sets the data and updates the fit coefficients. Added by D Brown 12/1/05.
   *
   * @param xd double[]
   * @param yd double[]
   */
  public void fitData(double[] xd, double[] yd) {
    if(xd.length!=yd.length) {
      throw new IllegalArgumentException("Arrays must be of equal length."); //$NON-NLS-1$
    }
    // return if data array too short
    if(xd.length<degree()+1) {
      return;
    }
    // clear old matrix data
    for(int i = 0; i<systemConstants.length; i++) {
      systemConstants[i] = 0;
      for(int j = 0; j<systemConstants.length; j++) {
        systemMatrix[i][j] = 0;
      }
    }
    // fill matrix with new data
    for(int i = 0, n = xd.length; i<n; i++) {
      double xp1 = 1;
      for(int j = 0; j<systemConstants.length; j++) {
        systemConstants[j] += xp1*yd[i];
        double xp2 = xp1;
        for(int k = 0; k<=j; k++) {
          systemMatrix[j][k] += xp2;
          xp2 *= xd[i];
        }
        xp1 *= xd[i];
      }
    }
    // compute coefficients
    computeCoefficients();
  }

  /**
   * Computes the polynomial coefficients.
   */
  protected void computeCoefficients() {
    for(int i = 0; i<systemConstants.length; i++) {
      for(int j = i+1; j<systemConstants.length; j++) {
        systemMatrix[i][j] = systemMatrix[j][i];
      }
    }
    LUPDecomposition lupSystem = new LUPDecomposition(systemMatrix);
    double[][] components = lupSystem.inverseMatrixComponents();
    LUPDecomposition.symmetrizeComponents(components);
    coefficients = lupSystem.solve(systemConstants);
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
