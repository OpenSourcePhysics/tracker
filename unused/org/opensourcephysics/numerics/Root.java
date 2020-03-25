/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * Class Root defines various root finding algorithms.
 *
 * This class cannot be subclassed or instantiated because all methods are static.
 *
 * @author Wolfgang Christian
 */
public class Root {
  static final int MAX_ITERATIONS = 15;

  private Root() {} // prohibit instantiation because all methods are static

  /**
   * Solves for the real roots of the quadratic equation
   * ax<sup>2</sup>+bx+c=0.
   *
   * @param a double quadratic term coefficient
   * @param b double linear term coefficient
   * @param c double constant term
   * @return double[] an array containing the two roots.
   */
  public static double[] quadraticReal(final double a, final double b, final double c) {
    final double roots[] = new double[2];
    final double q = -0.5*(b+((b<0.0) ? -1.0 : 1.0)*Math.sqrt(b*b-4.0*a*c));
    roots[0] = q/a;
    roots[1] = c/q;
    return roots;
  }

  /**
   * Solves for the complex roots of the quadratic equation
   * ax<sup>2</sup>+bx+c=0.
   *
   * @param a double quadratic term coefficient
   * @param b double linear term coefficient
   * @param c double constant term
   * @return double[] an array containing the two roots.
   */
  public static double[][] quadratic(final double a, final double b, final double c) {
    final double roots[][] = new double[2][2];
    double disc = b*b-4.0*a*c;
    if(disc<0) { // roots are complex
      roots[1][0] = roots[0][0] = -b/2/a;
      roots[1][1] -= roots[0][1] = Math.sqrt(-disc)/2/a;
      return roots;
    }
    final double q = -0.5*(b+((b<0.0) ? -1.0 : 1.0)*Math.sqrt(disc));
    roots[0][0] = q/a;
    roots[1][0] = c/q;
    return roots;
  }

  /**
   * Solves for the roots of the cubic equation
   * ax<sup>3</sup>+bx<sup>2</sup>+cx+d=0.
   *
   * @param a double cubic term coefficient
   * @param b double quadratic term coefficient
   * @param c double linear term coefficient
   * @param d double constant term
   * @return double[] an array containing the two roots.
   */
  public static double[][] cubic(final double a, final double b, final double c, final double d) {
    final double roots[][] = new double[3][2];
    // use standard form x<sup>3</sup>+Ax<sup>2</sup>+Bx+C=0.
    double A = b/a, B = c/a, C = d/a;
    double A2 = A*A;
    double Q = (3*B-A2)/9;
    double R = (9*A*B-27*C-2*A*A2)/54;
    double D = Q*Q*Q+R*R;
    if(D==0) {       // all roots are real and at least two are equal
      double S = (R<0) ? -Math.pow(-R, 1.0/3) : Math.pow(R, 1.0/3);
      roots[0][0] = -A/3+2*S;
      roots[2][0] = roots[1][0] = -A/3-S;
    } else if(D>0) { // one root is real and two are complex
      D = Math.sqrt(D);
      double S = (R+D<0) ? -Math.pow(-R-D, 1.0/3) : Math.pow(R+D, 1.0/3);
      double T = (R-D<0) ? -Math.pow(-R+D, 1.0/3) : Math.pow(R-D, 1.0/3);
      roots[0][0] = -A/3+S+T;
      roots[2][0] = roots[1][0] = -A/3-(S+T)/2;
      //complex parts
      roots[2][1] -= roots[1][1] = Math.sqrt(3)*(S-T)/2;
    } else {         // D<0;  all roots are real and unequal
      Q = -Q;        // make Q positive
      double theta = Math.acos(R/Math.sqrt(Q*Q*Q))/3;
      Q = 2*Math.sqrt(Q);
      A = A/3;
      roots[0][0] = Q*Math.cos(theta)-A;
      roots[1][0] = Q*Math.cos(theta+2*Math.PI/3)-A;
      roots[2][0] = Q*Math.cos(theta+4*Math.PI/3)-A;
    }
    return roots;
  }

  /**
   * Solves for the roots of the polynomial with the given coefficients c:
   * c[0] + c[1] * x + c[2] * x^2 + ....
   *
   * The roots are the complex eigenvalues of the companion matrix.
   *
   * @param double[] c coefficients
   * @return double[][] complex roots
   */
  public static double[][] polynomial(double[] c) {
    int n = c.length;
    int highest = n-1;
    for(int i = highest; i>0; i--) {
      if(c[highest]!=0) {
        break;
      }
      highest = i;
    }
    double ch = c[highest]; //highest nonzero power coefficient
    double[][] companion = new double[highest][highest];
    companion[0][highest-1] = -c[0]/ch;
    for(int i = 0; i<highest-1; i++) {
      companion[0][i] = -c[highest-i-1]/ch;
      companion[i+1][i] = 1;
    }
    EigenvalueDecomposition eigen = new EigenvalueDecomposition(companion);
    double[][] roots = new double[2][];
    roots[0] = eigen.getRealEigenvalues();
    roots[1] = eigen.getImagEigenvalues();
    return roots;
  }

  /**
   * Implements Newton's method for finding the root of a function.
   * The derivative is calculated numerically using the central difference approximation.
   *
   * @param f Function the function
   * @param x double guess the root
   * @param tol double computation tolerance
   * @return double the root or NaN if root not found.
   */
  public static double newton(final Function f, double x, final double tol) {
    int count = 0;
    while(count<MAX_ITERATIONS) {
      double xold = x;     // save the old value to test for convergence
      double df = 0;
      try {
        //df = Derivative.romberg(f, x, Math.max(0.001, 0.001*Math.abs(x)), tol/10);
        df = fxprime(f, x, tol);
      } catch(NumericMethodException ex) {
        return Double.NaN; // did not converve
      }
      x -= f.evaluate(x)/df;
      if(Util.relativePrecision(Math.abs(x-xold), x)<tol) {
        return x;
      }
      count++;
    }
    NumericsLog.fine(count+" newton root trials made - no convergence achieved"); //$NON-NLS-1$
    return Double.NaN; // did not converve in max iterations
  }

  /**
   * Implements Newton's method for finding the root of a function.
   *
   * @param f Function the function
   * @param df Function the derivative of the function
   * @param x double guess the root
   * @param tol double computation tolerance
   * @return double the root or NaN if root not found.
   */
  public static double newton(final Function f, final Function df, double x, final double tol) {
    int count = 0;
    while(count<MAX_ITERATIONS) {
      double xold = x; // save the old value to test for convergence
      // approximate the derivative using the given derivative function
      x -= f.evaluate(x)/df.evaluate(x);
      if(Util.relativePrecision(Math.abs(x-xold), x)<tol) {
        return x;
      }
      count++;
    }
    NumericsLog.fine(count+" newton root trials made - no convergence achieved"); //$NON-NLS-1$
    return Double.NaN; // did not converve in max iterations
  }

  /**
   * Implements the bisection method for finding the root of a function.
   * @param f Function the function
   * @param x1 double lower
   * @param x2 double upper
   * @param tol double computation tolerance
   * @return double the root or NaN if root not found
   */
  public static double bisection(final Function f, double x1, double x2, final double tol) {
    int count = 0;
    int maxCount = (int) (Math.log(Math.abs(x2-x1)/tol)/Math.log(2));
    maxCount = Math.max(MAX_ITERATIONS, maxCount)+2;
    double y1 = f.evaluate(x1), y2 = f.evaluate(x2);
    if(y1*y2>0) {                                                                             // y1 and y2 must have opposite sign
      NumericsLog.fine(count+" bisection root - interval endpoints must have opposite sign"); //$NON-NLS-1$
      return Double.NaN;                                                                      // interval does not contain a root
    }
    while(count<maxCount) {
      double x = (x1+x2)/2;
      double y = f.evaluate(x);
      if(Util.relativePrecision(Math.abs(x1-x2), x)<tol) {
        return x;
      }
      if(y*y1>0) { // replace end-point that has the same sign
        x1 = x;
        y1 = y;
      } else {
        x2 = x;
        y2 = y;
      }
      count++;
    }
    NumericsLog.fine(count+" bisection root trials made - no convergence achieved"); //$NON-NLS-1$
    return Double.NaN; // did not converge in max iterations
  }

  /**
   * Implements Newton's method for finding the root but switches to the bisection method if the
   * the estimate is not between xleft and xright.
   *
   * Method contributed by: J E Hasbun
   *
   * A Newton Raphson result is accepted if it is within the known bounds,
   * else a bisection step is taken.
   * Ref: Computational Physics by P. L. Devries (J. Wiley, 1993)
   * input: [xleft,xright] is the interval wherein fx() has a root, icmax is the
   * maximum iteration number, and tol is the tolerance level
   * output: returns xbest as the value of the function
   * Reasonable values of icmax and tol are 25, 5e-3.
   *
   * Returns the root or NaN if root not found.
   *
   * @param xleft double
   * @param xright double
   * @param tol double tolerance
   * @param icmax int number of trials
   * @return double the root
   */
  public static double newtonBisection(Function f, double xleft, double xright, double tol, int icmax) {
    double rtest = 10*tol;
    double xbest, fleft, fright, fbest, derfbest, delta;
    int icount = 0, iflag = 0; //loop counter
    //variables
    fleft = f.evaluate(xleft);
    fright = f.evaluate(xright);
    if(fleft*fright>=0) {
      iflag = 1;
    }
    switch(iflag) {
       case 1 :
         System.out.println("No solution possible"); //$NON-NLS-1$
         break;
    }
    if(Math.abs(fleft)<=Math.abs(fright)) {
      xbest = xleft;
      fbest = fleft;
    } else {
      xbest = xright;
      fbest = fright;
    }
    derfbest = fxprime(f, xbest, tol);
    while((icount<icmax)&&(rtest>tol)) {
      icount++;
      //decide Newton-Raphson or Bisection method to do:
      if((derfbest*(xbest-xleft)-fbest)*(derfbest*(xbest-xright)-fbest)<=0) {
        //Newton-Raphson step
        delta = -fbest/derfbest;
        xbest = xbest+delta;
        //System.out.println("Newton: count="+icount+", fx="+fbest);
      } else {
        //bisection step
        delta = (xright-xleft)/2;
        xbest = (xleft+xright)/2;
        //System.out.println("Bisection: count="+icount+", fx ="+fbest);
      }
      rtest = Math.abs(delta/xbest);
      //Compare the relative error to the tolerance
      if(rtest<=tol) {
        //if the error is small, the root has been found
        //System.out.println("root found="+xbest);
      } else {
        //the error is still large, so loop
        fbest = f.evaluate(xbest);
        derfbest = fxprime(f, xbest, tol);
        //adjust brackets
        if(fleft*fbest<=0) {
          //root is in the xleft subinterval:
          xright = xbest;
          fright = fbest;
        } else {
          //root is in the xright subinterval:
          xleft = xbest;
          fleft = fbest;
        }
      }
    }
    //reach here if either the error is too large or icount reached icmax
    if((icount>icmax)||(rtest>tol)) {
      NumericsLog.fine(icmax+" Newton and bisection trials made - no convergence achieved"); //$NON-NLS-1$
      return Double.NaN;
    }
    return xbest;
  }

  /*
   *  Inputs
   *   feqs - the function containing n equations with n unknowns and whose zeros we seek
   *   xx - the array containing the guess to the solutions
   *   max - the maximum iteration number
   *   tol - the tolerance level
   *   @return double the error
   */
  public static double newtonMultivar(VectorFunction feqs, double xx[], int max, double tol) {
    int Ndim = xx.length;
    double[] xxn = new double[Ndim];
    double[] F = new double[Ndim];
    int Iterations = 0;
    double err, relerr;
    err = 9999.;
    relerr = 9999.;
    //Use the Newton-Raphson method for systems of equations - employs the Jacobian
    //Needs a good guess - use one found by a grid method if one is not available
    while((err>tol*1.e-6)&&(relerr>tol*1.e-6)&&(Iterations<max)) {
      Iterations++;
      LUPDecomposition lu = new LUPDecomposition(getJacobian(feqs, Ndim, xx, tol/100.));
      //use the LUPDecomposition's solve method
      F = feqs.evaluate(xx, F); //the functions
      xxn = lu.solve(F);        //the corrections
      for(int i = 0; i<Ndim; i++) {
        xxn[i] = xx[i]-xxn[i];  //new guesses
      }
      err = (xx[0]-xxn[0])*(xx[0]-xxn[0]);
      relerr = xx[0]*xx[0];
      xx[0] = xxn[0];
      for(int i = 1; i<Ndim; i++) {
        err = err+(xx[i]-xxn[i])*(xx[i]-xxn[i]);
        relerr = relerr+xx[i]*xx[i];
        xx[i] = xxn[i];
      }
      err = Math.sqrt(err);
      relerr = err/(relerr+tol);
    }
    return err;
  }

  /**
 * Computes the Jacobian using a finite difference approximation.
 * Contributed to OSP by J E Hasbun 2007.
 *
 * @param feqs VectorFunction - the function containing n equations
 * @param n int - number of equations
 * @param xx double[] - the variable array at which the Jacobian is calculated
 * @param tol double - the small change to find the derivatives
 * @return double[][]  J - the square matrix containing the Jacobian
 */
  public static double[][] getJacobian(VectorFunction feqs, int n, double xx[], double tol) {
    //builds the Jacobian
    //xxp, xxm contain the varied parameter values to evaluate the equations on
    //fp, fm comtain the transpose of the function evaluations needed in the Jacobian
    //The Jacobian is calculated by the finite difference method
    double[][] J = new double[n][n];
    double[][] xxp = new double[n][n];
    double[][] xxm = new double[n][n];
    double[][] fp = new double[n][n];
    double[][] fm = new double[n][n];
    //build the coordinates for the derivatives
    for(int i = 0; i<n; i++) {
      for(int j = 0; j<n; j++) {
        xxp[i][j] = xx[j];
        xxm[i][j] = xx[j];
      }
      xxp[i][i] = xxp[i][i]+tol;
      xxm[i][i] = xxm[i][i]-tol;
    }
    for(int i = 0; i<n; i++) {              //f's here are built in transpose form
      fp[i] = feqs.evaluate(xxp[i], fp[i]); //i=1: f's at x+tol; i=2 f's at y+tol, etc
      fm[i] = feqs.evaluate(xxm[i], fm[i]); //i=1: f's at x-tol; i=2 f's at y-tol, etc
    }
    //Builds the Jacobian by the differences methods
    //becasue the f's above are in transpose form, we swap i, j in the derivative
    for(int i = 0; i<n; i++) {
      for(int j = 0; j<n; j++) {
        J[i][j] = (fp[j][i]-fm[j][i])/tol/2.; //the derivatives df_i/dx_j
      }
    }
    return J;
  }

  /**
   * Central difference approximation to the derivative for use in Newton's mehtod.
   * @param f Function
   * @param x double
   * @param tol double
   * @return double
   */
  final static double fxprime(Function f, double x, double tol) {
    //numerical derivative evaluation
    double del = tol/10;
    double der = (f.evaluate(x+del)-f.evaluate(x-del))/del/2.0;
    return der;
  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
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
