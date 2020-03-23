/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 * Author J E Hasbun 2007.
 */

package org.opensourcephysics.numerics;

/**
 * LevenbergMarquardt performs a minimization of a nonlinear multivariable function using
 * the Levenberg-Marquardt algorithm.
 *
 * @author J E Hasbun
 * @version 1.0
 */
public class LevenbergMarquardt {
  int Iterations;
  double[][] H;
  private double rmsd_tmp, rmsd_tmp1, rmsd;
  private double[] xtmp, xtmp1;
  HessianMinimize hessianMinimize = new HessianMinimize();

  /*
   *  Inputs
   *
   *   Veq  - the function of m parameters whose minimum is sought
   *
   *   x   - the array containing the guess to the solutions
   *
   *   max - the maximum iteration number
   *
   *   tol - the tolerance level
   *
   */
  public double minimize(MultiVarFunction Veq, double[] x, int max, double tol) {
    int m = x.length;
    H = new double[m][m];
    double[] xxn = new double[m];
    double[] D = new double[m];
    double[] dx = new double[m];
    xtmp = new double[m];
    xtmp1 = new double[m];
    rmsd_tmp = Veq.evaluate(x); //remember initial deviation
    rmsd_tmp1 = rmsd_tmp;       //remembers current deviation
    System.arraycopy(x, 0, xtmp, 0, m);  //xtmp remembers incoming guess
    System.arraycopy(x, 0, xtmp1, 0, m); //xtmp1 remembers current better guess
    for(int i = 0; i<m; i++) {
      dx[i] = (Math.abs(x[i])+1.0)/1e5; //step sizes for the finite differences
    }
    double err, relerr, Lambda;
    Lambda = 0.001;
    err = 9999.;
    relerr = 9999.;
    //Use the Levenberg-Marquardt alogorithm along with the modified Hessian
    //for an equation of several variables start with a reasonable guess.
    Iterations = 0;
    while((err>tol*1.e-6)&&(relerr>tol*1.e-6)&&(Iterations<max)&&(Lambda>1e-6)) {
      Iterations++;
      //The Levenberg-Marquardt trick, adds Lambda to the Hessian diagonals
      //We find the modified H and D for Veq. Here Lambda is a parameter to be changed.
      //Ref: K. Madsen, H. B. Nielsen, O. Tngleff, Methods for Non-Linear
      H = hessianMinimize.getHessian(Veq, x, D, dx);
      for(int i = 0; i<m; i++) {
        H[i][i] = H[i][i]+Lambda;
      }
      LUPDecomposition lu = new LUPDecomposition(H);
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
      //The Levenberg-Marquardt change of Lambda process
      rmsd = Veq.evaluate(x);
      if(rmsd<rmsd_tmp1) {
        //remember better guess and decrease Lambda
        Lambda = Lambda/10.;
        rmsd_tmp1 = rmsd;
        System.arraycopy(x, 0, xtmp1, 0, m);
      } else {
        //keep previous guess and increase Lambda
        System.arraycopy(xtmp1, 0, x, 0, m);
        Lambda = 10.*Lambda;
      }
      err = Math.sqrt(err);   //the error
      relerr = err/(relerr+tol);
    }
    check_rmsd(Veq, xtmp, x, m); //check if x is better, else keep old one
    return err;
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
