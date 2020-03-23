/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/*

  @Author J E Hasbun 2007.

  Applies the Hessian method to find the parameters that minimize a

  function of those parameters also but uses LUPDecomposition's solve

  method instead of the inverse to get the new guesses

  @Copyright (c) 2017

  This software is to support the Open Source Physics library

  http://www.opensourcephysics.org under the terms of the GNU General Public

  License (GPL) as published by the Free Software Foundation.

*/
package org.opensourcephysics.numerics;

/**
 * Class description
 *
*/
public class HessianMinimize {
  int Iterations;
  double[][] H;
  double[] xp;
  double[] xm;
  double[] xpp;
  double[] xpm;
  double[] xmp;
  double[] xmm;
  private double rmsd_tmp, rmsd;
  private double[] xtmp;

  /*  Inputs

      Veq  - the function of m parameters whose minimum is sought

      x   - the array containing the guess to the solutions

      max - the maximum iteration number

      tol - the tolerance level

  */
  public double minimize(MultiVarFunction Veq, double[] x, int max, double tol) {
    int m = x.length;
    double[] xxn = new double[m];
    double[] D = new double[m];
    double[] dx = new double[m];
    xtmp = new double[m];
    System.arraycopy(x, 0, xtmp, 0, m);
    rmsd_tmp = Veq.evaluate(x);
    rmsd = 0;
    crudeGuess(Veq, x);          //obtain a crude guess
    check_rmsd(Veq, xtmp, x, m); //check if x is better, else keep old one
    for(int i = 0; i<m; i++) {
      dx[i] = (Math.abs(x[i])+1.0)/1e5; //step sizes for the finite differences
    }
    double err, relerr;
    err = 9999.;
    relerr = 9999.;
    //Use the Hessian method for an equation of several variables
    //start with a good guess.
    Iterations = 0;
    while((err>tol*1.e-6)&&(relerr>tol*1.e-6)&&(Iterations<max)) {
      Iterations++;
      LUPDecomposition lu = new LUPDecomposition(getHessian(Veq, x, D, dx));
      // use the LUPDecomposition's solve method
      xxn = lu.solve(D);      //the corrections
      for(int i = 0; i<m; i++) {
        xxn[i] = xxn[i]+x[i]; //new guesses
      }
      err = (x[0]-xxn[0])*(x[0]-xxn[0]);
      relerr = x[0]*x[0];
      x[0] = xxn[0];
      for(int i = 1; i<m; i++) {
        err = err+(x[i]-xxn[i])*(x[i]-xxn[i]);
        relerr = relerr+x[i]*x[i];
        x[i] = xxn[i];        //copy to go back
        //dx[i]=0.5*dx[i];         //could decrease the variation at each iteration
      }
      err = Math.sqrt(err);   //the error
      relerr = err/(relerr+tol);
    }
    check_rmsd(Veq, xtmp, x, m); //check if x is better, else keep old one
    return err;
  }

  private void allocateArrays(int m) {
    H = new double[m][m];
    xp = new double[m];
    xm = new double[m];
    xpp = new double[m];
    xpm = new double[m];
    xmp = new double[m];
    xmm = new double[m];
  }

  void crudeGuess(MultiVarFunction Veq, double[] x) {
    /*

        @Author J E Hasbun 2007.

        This is a crude method to obtain better starting guesses for the

        multiple funtion variable minimization problem. It uses a Naive

        Newton Raphson step for each of the variables.

        Ref: Computational Physics by P. L. Devries (J. Wiley, 1993)

        @Copyright (c) 2017

        This software is to support the Open Source Physics library

        http://www.opensourcephysics.org under the terms of the GNU General Public

        License (GPL) as published by the Free Software Foundation.

    */
    double sp, s0, sm;
    int m = x.length; //array size
    int Nc = 5;       //cycles to make
    double f = 0.35;  //moderates the step size to next crude guess
    int n = 0;        //cycle counter
    double[] xp = new double[m];
    double[] xm = new double[m];
    double[] dx = new double[m];
    for(int i = 0; i<m; i++) {
      dx[i] = (Math.abs(x[i])+1.0)/1.e3; //step sizes for the finite derivatives
    }
    //Cycle through each parameter Nc times
    while(n<Nc) {
      n++;
      for(int i = 0; i<m; i++) {
        //The SUM will be evaluated with the parameters
        //xp, a, and xm:
        for(int k = 0; k<m; k++) {
          if(k==i) {
            xp[i] = x[i]+dx[i];
            xm[i] = x[i]-dx[i];
          } else {
            xp[k] = x[k];
            xm[k] = x[k];
          }
        }
        sp = Veq.evaluate(xp); //  Evaluate the sum.
        s0 = Veq.evaluate(x);
        sm = Veq.evaluate(xm);
        //make the crude Newton-Raphson step next
        x[i] = x[i]-f*0.5*dx[i]*(sp-sm)/(sp-2.0*s0+sm);
        //As we move towards a minimum, we should decrease
        //step size used in calculating the derivative.
        dx[i] = 0.5*dx[i];
      }
    }
  }

  void check_rmsd(MultiVarFunction Veq, double[] xtmp, double[] xx, int mx) {
    //checks whether xtmp or xx is better, and keep the better one
    if(java.lang.Double.isNaN(ArrayLib.sum(xx))) {
      rmsd = rmsd_tmp;
      System.arraycopy(xtmp, 0, xx, 0, mx);
    } else {
      rmsd = Veq.evaluate(xx);
      if(rmsd<=rmsd_tmp) {
        rmsd_tmp = rmsd;
        System.arraycopy(xx, 0, xtmp, 0, mx);
      } else {
        rmsd = rmsd_tmp;
        System.arraycopy(xtmp, 0, xx, 0, mx);
      }
    }
  }

  public int getIterations() {
    return Iterations;
  }

  public double[][] getHessian(MultiVarFunction Veq, double[] x, double[] D, double[] dx) {
    /*

          @Author J E Hasbun 2007.

          Finds the Hessian of a function of several variables

          The method is similar to the Newton method but for the derivative of a

          general function. The idea is that to "minimize" a multivariable function

          Veq({X}), we need the guess {Xnew}={Xold}+inverse{H}*{D}, so here we find

          H and D for Veq. Ref: Computational Physics by P. L. Devries (J. Wiley, 1993)

          Input:

          Veq - the function of m parameters whose minimum is sought

          x   - the parameters

          dx  - the size of the variations used in the derivatives



          Uses:

          H   - the square matrix containing the calculated Hessian as a return

                the Hessian is a square matrix of 2nd order partial derivatives of

                of Veq with respect to the variables

          D   - the 1st order negative partial derivative of Veq

                builds the Hessian H - used in a multidimensional newton method

                the arrays contain:

          x   - the variable parameters of function Veq

          x   - x[1], ...,      x[i],..., x[m]

          xp  - x[1], ..., x[i]+dx[i],..., x[m]

          xm  - x[1], ..., x[i]-dx[i],..., x[m]

          xpp - x[1],..,x[i]+dx[i],..., x[j]+dx[j],..., x[m]

          xpm - x[1],..,x[i]+dx[i],..., x[j]-dx[j],..., x[m]

          xmp - x[1],..,x[i]-dx[i],..., x[j]+dx[j],..., x[m]

          xmm - x[1],..,x[i]-dx[i],..., x[j]-dx[j],..., x[m]

    */
    //The Hessian H is calculated by the finite difference method
    int m = x.length;
    if((xp==null)||(xp.length!=m)) {
      allocateArrays(m);
    }
    //  Compute the Hessian:
    for(int i = 0; i<m; i++) {
      for(int j = i; j<m; j++) {
        if(i==j) {
          for(int k = 0; k<m; k++) { //reset the x's
            xp[k] = x[k];
            xm[k] = x[k];
          }
          xp[i] = x[i]+dx[i];        //change the ith one
          xm[i] = x[i]-dx[i];
          H[i][i] = (Veq.evaluate(xp)-2.0*Veq.evaluate(x)+Veq.evaluate(xm))/(dx[i]*dx[i]);
        } else {
          for(int k = 0; k<m; k++) { //reset the x's
            xpp[k] = x[k];
            xpm[k] = x[k];
            xmp[k] = x[k];
            xmm[k] = x[k];
          }
          xpp[i] = x[i]+dx[i];       //change the ith, jth ones
          xpp[j] = x[j]+dx[j];
          xpm[i] = x[i]+dx[i];
          xpm[j] = x[j]-dx[j];
          xmp[i] = x[i]-dx[i];
          xmp[j] = x[j]+dx[j];
          xmm[i] = x[i]-dx[i];
          xmm[j] = x[j]-dx[j];
          H[i][j] = ((Veq.evaluate(xpp)-Veq.evaluate(xpm))/(2.0*dx[j])-(Veq.evaluate(xmp)-Veq.evaluate(xmm))/(2.0*dx[j]))/(2.0*dx[i]);
          H[j][i] = H[i][j];
        }
      }
    }
    // note the D function is the negative of the partial derivative
    for(int i = 0; i<m; i++) {
      for(int k = 0; k<m; k++) { //reset the x's
        xp[k] = x[k];
        xm[k] = x[k];
      }
      xp[i] = x[i]+dx[i];        //change the ith one
      xm[i] = x[i]-dx[i];
      D[i] = -(Veq.evaluate(xp)-Veq.evaluate(xm))/(2.0*dx[i]);
    }
    return H;
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
