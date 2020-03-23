/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * Heun3 implements Heun's third order algorithm for solving ODEs by evaluating the rate at the initial state,
 * and two intermediate states. It then uses a weighted average to advance the state.
 *
 * Heun's method is known as a predictor-corrector method because it first predicts the state (estimated_state) and
 * then corrects the rate based on the prediction.
 *
 * @version 1.0
 */
public class Heun3 extends AbstractODESolver {
  private double[] rate1, rate2, rate3, estimated_state;

  /**
   * Constructor Heun3
   * @param ode
   */
  public Heun3(ODE ode) {
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
    estimated_state = new double[numEqn];
  }

  /**
   * Steps (advances) the differential equations by the stepSize.
   *
   * The ODESolver invokes the ODE's getState method to obtain the initial state of the system.
   * The ODESolver then uses this state estimate the rate a intermediate points.
   * Finally, the ODESolver advances the solution and copies the new state into the
   * state array at the end of the solution step.
   *
   * @return the step size
   */
  public double step() {
    double state[] = ode.getState();
    if(state.length!=numEqn) {
      initialize(stepSize);
    }
    ode.getRate(state, rate1);
    for(int i = 0; i<numEqn; i++) {
      estimated_state[i] = state[i]+stepSize*rate1[i]/3;
    }
    ode.getRate(estimated_state, rate2);
    for(int i = 0; i<numEqn; i++) {
      estimated_state[i] = state[i]+2*stepSize*rate2[i]/3;
    }
    ode.getRate(estimated_state, rate3);
    for(int i = 0; i<numEqn; i++) {
      state[i] = state[i]+stepSize*(rate1[i]+3.0*rate3[i])/4.0;
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
