/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * Polynomial implements a mathematical polynomial:
 * c[0] + c[1] * x + c[2] * x^2 + ....
 *
 * This class is based on code published in Object-Oriented Implementation of
 * Numerical Methods by Didier H. Besset.
 * The code has been adapted to the OSP framework by Wolfgang Christian.
 */
public class Polynomial implements Function {
  /**
   * Polynomial coefficients.
   */
  protected double[] coefficients;

  /**
   * Constructs a polynomial with the given coefficients.
   * @param coef polynomial coefficients.
   */
  public Polynomial(double[] coef) {
    coefficients = coef;
  }

  /**
   * Gets a clone of the polynomial coefficients c:
   * c[0] + c[1] * x + c[2] * x^2 + ....
   *
   * @return double[]
   */
  public double[] getCoefficients() {
    return coefficients.clone();
  }

  /**
   * Constructs a polynomial with the given coefficients.
   * @param coef polynomial coefficients.
   */
  public Polynomial(String[] coef) {
    coefficients = new double[coef.length];
    for(int i = 0, n = coef.length; i<n; i++) {
      try {
        coefficients[i] = Double.parseDouble(coef[i]);
      } catch(NumberFormatException ex) {
        coefficients[i] = 0;
      }
    }
  }

  /**
   * Evaluates a polynomial using the given coefficients.
   *
   * @param x
   * @param coeff the polynomial coefficients.
   */
  public static double evalPolynomial(final double x, final double[] coeff) {
    int n = coeff.length-1;
    double y = coeff[n];
    for(int i = n-1; i>=0; i--) {
      y = coeff[i]+(y*x);
    }
    return y;
  }

  /**
   *
   * @param r double    number added to the polynomial.
   * @return Polynomial
   */
  public Polynomial add(double r) {
    int n = coefficients.length;
    double coef[] = new double[n];
    coef[0] = coefficients[0]+r;
    for(int i = 1; i<n; i++) {
      coef[i] = coefficients[i];
    }
    return new Polynomial(coef);
  }

  /**
   * Adds the given polynomial to this polynomial.
   * @param p Polynomial
   * @return Polynomial
   */
  public Polynomial add(Polynomial p) {
    int n = Math.max(p.degree(), degree())+1;
    double[] coef = new double[n];
    for(int i = 0; i<n; i++) {
      coef[i] = coefficient(i)+p.coefficient(i);
    }
    return new Polynomial(coef);
  }

  /**
   * Gets the coefficient value at the desired position
   * @param n int    the position of the coefficient to be returned
   * @return double the coefficient value
   * @version 1.2
   */
  public double coefficient(int n) {
    return(n<coefficients.length) ? coefficients[n] : 0;
  }

  /**
   * Deflates the polynomial by removing the root.
   *
   * @param r double     a root of the polynomial (no check made).
   * @return Polynomial the receiver divided by polynomial (x - r).
   */
  public Polynomial deflate(double r) {
    int n = degree();
    double remainder = coefficients[n];
    double[] coef = new double[n];
    for(int k = n-1; k>=0; k--) {
      coef[k] = remainder;
      remainder = remainder*r+coefficients[k];
    }
    return new Polynomial(coef);
  }

  /**
   * Gets the degree of this polynomial function.
   * @return int degree of this polynomial function
   */
  public int degree() {
    return coefficients.length-1;
  }

  /**
   * Gets the derivative of this polynomial.
   * @return Polynomial the derivative.
   */
  public Polynomial derivative() {
    int n = degree();
    if(n==0) {
      double coef[] = {0};
      return new Polynomial(coef);
    }
    double coef[] = new double[n];
    for(int i = 1; i<=n; i++) {
      coef[i-1] = coefficients[i]*i;
    }
    return new Polynomial(coef);
  }

  /**
   * Divides this polynomial by a constant.
   * @param r double
   * @return Polynomial
   */
  public Polynomial divide(double r) {
    return multiply(1/r);
  }

  /**
   * Divides this polynomial by another polynomial.
   *
   * The remainder is dropped.
   *
   * @param p Polynomial
   * @return Polynomial
   */
  public Polynomial divide(Polynomial p) {
    return divideWithRemainder(p)[0];
  }

  /**
   * Divides this polynomial by another polynomial.
   *
   * @param p polynomial
   * @return polynomial array containing the answer and remainder
   */
  public Polynomial[] divideWithRemainder(Polynomial p) {
    Polynomial[] answer = new Polynomial[2];
    int m = degree();
    int n = p.degree();
    if(m<n) {
      double[] q = {0};
      answer[0] = new Polynomial(q);
      answer[1] = p;
      return answer;
    }
    double[] quotient = new double[m-n+1];
    double[] coef = new double[m+1];
    for(int k = 0; k<=m; k++) {
      coef[k] = coefficients[k];
    }
    double norm = 1/p.coefficient(n);
    for(int k = m-n; k>=0; k--) {
      quotient[k] = coef[n+k]*norm;
      for(int j = n+k-1; j>=k; j--) {
        coef[j] -= quotient[k]*p.coefficient(j-k);
      }
    }
    double[] remainder = new double[n];
    for(int k = 0; k<n; k++) {
      remainder[k] = coef[k];
    }
    answer[0] = new Polynomial(quotient);
    answer[1] = new Polynomial(remainder);
    return answer;
  }

  /**
   * Integrates this polynomial.
   * The integral has the value 0 at x = 0.
   *
   * @return Polynomial the integral
   */
  public Polynomial integral() {
    return integral(0);
  }

  /**
   * Integrates this polynomial having the specified value for x = 0.
   * @param value double    value of the integral at x=0
   * @return Polynomial the integral.
   */
  public Polynomial integral(double value) {
    int n = coefficients.length+1;
    double coef[] = new double[n];
    coef[0] = value;
    for(int i = 1; i<n; i++) {
      coef[i] = coefficients[i-1]/i;
    }
    return new Polynomial(coef);
  }

  /**
   * Multiplies this polynomial by a constant.
   * @param r double
   * @return Polynomial
   */
  public Polynomial multiply(double r) {
    int n = coefficients.length;
    double coef[] = new double[n];
    for(int i = 0; i<n; i++) {
      coef[i] = coefficients[i]*r;
    }
    return new Polynomial(coef);
  }

  /**
   * Multiplies this polynomial by another polynomial.
   * @param p Polynomial
   * @return Polynomial
   */
  public Polynomial multiply(Polynomial p) {
    int n = p.degree()+degree();
    double[] coef = new double[n+1];
    for(int i = 0; i<=n; i++) {
      coef[i] = 0;
      for(int k = 0; k<=i; k++) {
        coef[i] += p.coefficient(k)*coefficient(i-k);
      }
    }
    return new Polynomial(coef);
  }

  /**
   * Gets the complex roots of this polynomial.
   * @return double[]
   */
  public double[][] roots() {
    int highest = coefficients.length-1; // start with the degree
    for(int i = highest; i>0; i--) {
      if(coefficients[highest]!=0) {
        break;
      }
      highest = i;
    }
    if(highest==0)return new double[2][0];
    double ch = coefficients[highest]; //highest nonzero coefficient
    double[][] companion = new double[highest][highest];
    companion[0][highest-1] = -coefficients[0]/ch;
    for(int i = 0; i<highest-1; i++) {
      companion[0][i] = -coefficients[highest-i-1]/ch;
      companion[i+1][i] = 1;
    }
    EigenvalueDecomposition eigen = new EigenvalueDecomposition(companion);
    double[][] roots = new double[2][];
    roots[0] = eigen.getRealEigenvalues();
    roots[1] = eigen.getImagEigenvalues();
    return roots;
  }

  /**
   * Gets the real roots of this polynomial.
   * @return double[]
   */
  public double[] rootsReal() {
    double[][] roots = roots();
    int n = roots[0].length;
    double[] temp = new double[n];
    int count = 0;
    for(int i = 0; i<n; i++) {
      double magSquared = roots[0][i]*roots[0][i]+roots[1][i]*roots[1][i];
      if(roots[1][i]*roots[1][i]/magSquared<1.0e-12) { // skip small imaginary values
        temp[count] = roots[0][i];
        count++;
      }
    }
    double[] re = new double[count];
    System.arraycopy(temp, 0, re, 0, count);
    return re;
  }

  /**
   * Subtracts a constant from this polynomial.
   *
   * @param r the constant
   * @return Polynomial
   */
  public Polynomial subtract(double r) {
    return add(-r);
  }

  /**
   * Subtracts another polynomial from this polynomial.
   * @return Polynomial
   * @param p Polynomial
   */
  public Polynomial subtract(Polynomial p) {
    int n = Math.max(p.degree(), degree())+1;
    double[] coef = new double[n];
    for(int i = 0; i<n; i++) {
      coef[i] = coefficient(i)-p.coefficient(i);
    }
    return new Polynomial(coef);
  }

  /**
   * Converts this polynomial to a String.
   */
  public String toString() {
    if((coefficients==null)||(coefficients.length<1)) {
      return "Polynomial coefficients are undefined."; //$NON-NLS-1$
    }
    StringBuffer sb = new StringBuffer();
    boolean firstNonZeroCoefficientPrinted = false;
    for(int n = 0, m = coefficients.length; n<m; n++) {
      if(coefficients[n]!=0) {
        if(firstNonZeroCoefficientPrinted) {
          sb.append((coefficients[n]>0) ? " + " : " "); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
          firstNonZeroCoefficientPrinted = true;
        }
        if((n==0)||(coefficients[n]!=1)) {
          sb.append(Double.toString(coefficients[n]));
        }
        if(n>0) {
          sb.append(" x^"+n);                           //$NON-NLS-1$
        }
      }
    }
    String str = sb.toString();
    if(str.equals("")) { //$NON-NLS-1$
      return "0";        //$NON-NLS-1$
    }
    return str;
  }

  /**
   * Evaluates the polynomial for the specified variable value.
   * @param x double    value at which the polynomial is evaluated
   * @return double polynomial value.
   */
  public double evaluate(double x) {
    int n = coefficients.length;
    double answer = coefficients[--n];
    while(n>0) {
      answer = answer*x+coefficients[--n];
    }
    return answer;
  }

  /**
   * Returns the value and the derivative of this polynomial
   * for the specified variable value in an array of two elements
   *
   * @param x double    value at which the polynomial is evaluated
   * @return double[0]   the value of the polynomial
   * @return double[1]   the derivative of the polynomial
   */
  public double[] valueAndDerivative(double x) {
    int n = coefficients.length;
    double[] answer = new double[2];
    answer[0] = coefficients[--n];
    answer[1] = 0;
    while(n>0) {
      answer[1] = answer[1]*x+answer[0];
      answer[0] = answer[0]*x+coefficients[--n];
    }
    return answer;
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
