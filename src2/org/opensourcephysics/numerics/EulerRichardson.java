/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * An Euler-Richardson (midpoint) method ODE solver.
 *
 * The Euler-Richardson method uses the state at the beginning of the interval
 * to estimate the state at the midpoint.
 *
 * x(midpoint) = x(n) + v(n)*dt/2
 * v(midpoint) = v(n) + a(n)*dt/2
 * t(midpoint) = t(n) + dt/2
 *
 * The midpoint state is then used to calculate the final state.
 *
 * @author       Wolfgang Christian
 * @version 1.0
 */
public class EulerRichardson extends AbstractODESolver {
  private double[] rate;     // array that stores the rate
  private double[] midstate; // midpoint

  /**
   * Constructs the EulerRichardson ODESolver for a system of ordinary  differential equations.
   *
   * @param ode the system of differential equations.
   */
  public EulerRichardson(ODE ode) {
    super(ode);
  }

  /**
   * Initializes the ODE solver.
   *
   * The rate and midstate arrays are allocated.
   * The number of differential equations is determined by invoking getState().length on the ODE.
   *
   * @param stepSize
   */
  public void initialize(double stepSize) {
    super.initialize(stepSize);
    rate = new double[numEqn];
    midstate = new double[numEqn];
  }

  /**
   * Steps (advances) the differential equations by the stepSize.
   *
   * The ODESolver invokes the ODE's getState method to obtain the initial state of the system.
   * The ODESolver advances the solution and copies the new state into the
   * state array at the end of the solution step.
   *
   * @return the step size
   */
  public double step() {
    double[] state = ode.getState();
    ode.getRate(state, rate); // get the rate at the start
    double dt2 = stepSize/2;
    for(int i = 0; i<numEqn; i++) {
      // estimate the state at the midpoint
      midstate[i] = state[i]+rate[i]*dt2;
    }
    ode.getRate(midstate, rate); // get the rate at the midpoint
    for(int i = 0; i<numEqn; i++) {
      state[i] = state[i]+stepSize*rate[i];
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
