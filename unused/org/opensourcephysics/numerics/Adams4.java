/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * Title:        Adams4
 * Description:  A fourth order Predictor-Corrector ODE solver.
 * @author       F. Esquembre
 * @version 1.0
 */
public class Adams4 extends RK4 {
  private double[] fn, fn1, fn2, fn3, temp_state, temp_rate;
  private int counter = 0;

  /**
   * Constructs the RK4 ODESolver for a system of ordinary  differential equations.
   *
   * @param ode the system of differential equations.
   */
  public Adams4(ODE ode) {
    super(ode);
  }

  /**
   * Initializes the ODE solver and allocates the rate and state arrays.
   * The number of differential equations is determined by invoking getState().length on the superclass.
   *
   * @param stepSize
   */
  public void initialize(double stepSize) {
    super.initialize(stepSize);
    fn = new double[numEqn];
    fn1 = new double[numEqn];
    fn2 = new double[numEqn];
    fn3 = new double[numEqn];
    temp_state = new double[numEqn];
    temp_rate = new double[numEqn];
    counter = 0;
  }

  /**
   * Steps (advances) the differential equations by the stepSize.
   *
   * The ODESolver invokes the ODE's getRate method to compute the rate at various intermediate states.
   *
   * The ODESolver then advances the solution and copies the new state into the
   * ODE's state array at the end of the solution step.
   *
   * @return the step size
   */
  public double step() {
    double state[] = ode.getState();
    if(state==null) {
      return stepSize;
    }
    if(state.length!=numEqn) {
      initialize(stepSize);
    }
    ode.getRate(state, fn);
    if(counter<3) {                   // Use Runge-Kutta 4 to start the method
      stepSize = super.step();
      counter++;
    } else {
      for(int i = 0; i<numEqn; i++) { // Predictor
        temp_state[i] = state[i]+stepSize*(55*fn[i]-59*fn1[i]+37*fn2[i]-9*fn3[i])/24;
      }
      ode.getRate(temp_state, temp_rate);
      for(int i = 0; i<numEqn; i++) { // Corrector
        state[i] = state[i]+stepSize*(9*temp_rate[i]+19*fn[i]-5*fn1[i]+fn2[i])/24;
      }
    }
    System.arraycopy(fn2, 0, fn3, 0, numEqn);
    System.arraycopy(fn1, 0, fn2, 0, numEqn);
    System.arraycopy(fn, 0, fn1, 0, numEqn);
    return stepSize;
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
