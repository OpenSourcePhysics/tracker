/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * CashKarp45 implements a RKF 4/5 ODE solver with variable step size using Cash-Karp coefficients.
 * @author       W. Christian
 * @author       F. Esquembre
 * @version 1.0
 */
public class CashKarp45 implements ODEAdaptiveSolver {
  int error_code = ODEAdaptiveSolver.NO_ERROR;
  // embedding constants Cash-Karp 4th and 5th order
  static final double[][] a = {
    {1.0/5.0}, {3.0/40.0, 9.0/40.0}, {3.0/10.0, -9.0/10.0, 6.0/5.0}, {-11.0/54.0, 5.0/2.0, -70.0/27.0, 35.0/27.0}, {1631.0/55296.0, 175.0/512.0, 575.0/13824.0, 44275.0/110592.0, 253.0/4096.0}
  };
  // b4 are 4th order coefficients
  // static final double[]   b4={ 2825./27648., 0., 18575./48384., 13525./55296.,  277./14336.,     1./4.};
  // b5 are 5th oreer coefficients
  static final double[] b5 = {37./378., 0., 250./621., 125./594., 0., 512./1771.};
  static final double[] er = {277./64512., 0., -6925./370944., 6925./202752., 277./14336., -277./7084.};
  static final int numStages = 6; // number of intermediate rate computations
  private double stepSize = 0.01;
  private int numEqn = 0;
  private double[] temp_state;
  private double[][] k;
  private double truncErr;
  private ODE ode;
  protected double tol = 1.0e-6;
  protected boolean enableExceptions = false;

  /**
   * Constructs the CashKarp45 ODESolver for a system of ordinary  differential equations.
   *
   * @param _ode the system of differential equations.
   */
  public CashKarp45(ODE _ode) {
    ode = _ode;
    initialize(stepSize);
  }

  /**
   * Initializes the ODE solver.
   *
   * Temporary state and rate arrays are allocated.
   * The number of differential equations is determined by invoking getState().length on the ODE.
   *
   * @param _stepSize
   */
  public void initialize(double _stepSize) {
    stepSize = _stepSize;
    double state[] = ode.getState();
    if(state==null) { // state vector not defined.
      return;
    }
    if(numEqn!=state.length) {
      numEqn = state.length;
      temp_state = new double[numEqn];
      k = new double[numStages][numEqn]; // six intermediate rates
    }
  }

  /**
   * Steps (advances) the differential equations by the stepSize.
   *
   * The ODESolver invokes the ODE's getRate method to obtain the initial state of the system.
   * The ODESolver then advances the solution and copies the new state into the
   * state array at the end of the solution step.
   *
   * @return the step size
   */
  public double step() {
    error_code = ODEAdaptiveSolver.NO_ERROR;
    int iterations = 10;
    double currentStep = stepSize, error = 0;
    double state[] = ode.getState();
    ode.getRate(state, k[0]); // get the initial rate
    do {
      iterations--;
      currentStep = stepSize;
      // Compute the k's
      for(int s = 1; s<numStages; s++) {
        for(int i = 0; i<numEqn; i++) {
          temp_state[i] = state[i];
          for(int j = 0; j<s; j++) {
            temp_state[i] = temp_state[i]+stepSize*a[s-1][j]*k[j][i];
          }
        }
        ode.getRate(temp_state, k[s]);
      }
      // Compute the error
      error = 0;
      for(int i = 0; i<numEqn; i++) {
        truncErr = 0;
        for(int s = 0; s<numStages; s++) {
          truncErr = truncErr+stepSize*er[s]*k[s][i];
        }
        error = Math.max(error, Math.abs(truncErr));
      }
      if(error<=Float.MIN_VALUE) { // error too small to be meaningful,
        error = tol/1.0e5;         // increase stepSize x10
      }
      // find h step for the next try.
      if(error>tol) {              // shrink, no more than x10
        double fac = 0.9*Math.pow(error/tol, -0.25);
        stepSize = stepSize*Math.max(fac, 0.1);
      } else if(error<tol/10.0) {  // grow, but no more than factor of 10
        double fac = 0.9*Math.pow(error/tol, -0.2);
        if(fac>1) {                // sometimes fac is <1 because error/tol is close to one
          stepSize = stepSize*Math.min(fac, 10);
        }
      }
    } while((error>tol)&&(iterations>0));
    // advance the state
    for(int i = 0; i<numEqn; i++) {
      for(int s = 0; s<numStages; s++) {
        state[i] += currentStep*b5[s]*k[s][i];
      }
    }
    if(iterations==0) {
      error_code = ODEAdaptiveSolver.DID_NOT_CONVERGE;
      if(enableExceptions) {
        throw new ODESolverException("CashKarp45 ODE solver did not converge."); //$NON-NLS-1$
      }
    }
    return currentStep; // the value of the step actually taken.
  }

  /**
   * Enables runtime exceptions if the solver does not converge.
   * @param enable boolean
   */
  public void enableRuntimeExpecptions(boolean enable) {
    this.enableExceptions = enable;
  }

  /**
   * Sets the step size.
   *
   * The step size may change when the step method is invoked.
   *
   * @param stepSize
   */
  public void setStepSize(double stepSize) {
    this.stepSize = stepSize;
  }

  /**
   * Gets the step size.
   *
   * The stepsize is adaptive and may change as the step() method is invoked.
   *
   * @return the step size
   */
  public double getStepSize() {
    return stepSize;
  }

  /**
   * Method setTolerance
   *
   * @param _tol
   */
  public void setTolerance(double _tol) {
    tol = Math.abs(_tol);
    if(tol<1.0E-12) {
      String err_msg = "Error: CashKarp ODE solver tolerance cannot be smaller than 1.0e-12."; //$NON-NLS-1$
      if(enableExceptions) {
        throw new ODESolverException(err_msg);
      }
      System.err.println(err_msg);
    }
  }

  /**
   * Method getTolerance
   *
   * @return tolerance
   */
  public double getTolerance() {
    return tol;
  }

  /**
 * Gets the error code.
 * Error codes:
 *   ODEAdaptiveSolver.NO_ERROR
 *   ODEAdaptiveSolver.DID_NOT_CONVERGE
 * @return int
 */
  public int getErrorCode() {
    return error_code;
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
