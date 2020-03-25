/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * Title:        RK4
 * Description:  A fourth order Runge-Kutta ODE solver.
 * @author       Wolfgang Christian
 * @version 1.0
 */
public class RK4 extends AbstractODESolver {
  private double[] rate1, rate2, rate3, rate4, estimated_state;

  /**
   * Constructs the RK4 ODESolver for a system of ordinary  differential equations.
   *
   * @param ode the system of differential equations.
   */
  public RK4(ODE ode) {
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
    rate1 = new double[numEqn];
    rate2 = new double[numEqn];
    rate3 = new double[numEqn];
    rate4 = new double[numEqn];
    estimated_state = new double[numEqn];
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
    ode.getRate(state, rate1);
    for(int i = 0; i<numEqn; i++) {
      estimated_state[i] = state[i]+stepSize*rate1[i]/2;
    }
    ode.getRate(estimated_state, rate2);
    for(int i = 0; i<numEqn; i++) {
      estimated_state[i] = state[i]+stepSize*rate2[i]/2;
    }
    ode.getRate(estimated_state, rate3);
    for(int i = 0; i<numEqn; i++) {
      estimated_state[i] = state[i]+stepSize*rate3[i];
    }
    ode.getRate(estimated_state, rate4);
    for(int i = 0; i<numEqn; i++) {
      state[i] = state[i]+stepSize*(rate1[i]+2*rate2[i]+2*rate3[i]+rate4[i])/6.0;
    }
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
