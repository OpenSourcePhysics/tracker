/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * LeapFrog method ODE solver.
 *
 * The LeapFrog algorithm is a third order algorithm that uses the acceleration to estimate the
 * final position. Note that the velocity plays no part in the integration of the equations.
 *
 * x(n+1) = 2*x(n) - x(n-1) + a(n)*dt*dt
 * v(n+1) = (x(n+1) - x(n-1))/(2 dt) + a(n)*dt
 *
 * The LeapFrog algorithm is equivalent to the velocity Verlet algorithm except that it is not self starting.
 * It is faster than the the velocity Verlet algorithm because it only evaluates the rate once per step.
 *
 * CAUTION! You MUST call the initialize if the state array is changed.
 * The LeapFrog algorithm is not self-starting.  The current state and a prior state
 * must both be known to advance the solution.  Since the prior state is not known
 * for the initial conditions, a prior state is estimated when the
 * initialize method is invoked.
 *
 * CAUTION! This implementation assumes that the state vector has 2*N + 1 variables.
 * These variables alternate between position and velocity with the last variable being time.
 * That is, the  state vector is ordered as follows:
 *
 * x1, d x1/dt, x2, d x2/dt, x3, d x3/dt ..... xN, d xN/dt, t
 *
 * @author       Wolfgang Christian
 * @version 1.0
 */
public class LeapFrog extends AbstractODESolver {
  private double[] rate;         // array that stores the rate
  private double[] priorState;   // previous state
  private double[] currentState; // current state

  /**
   * Constructs the LeapFrog ODESolver for a system of ordinary  differential equations.
   *
   * @param ode the system of differential equations.
   */
  public LeapFrog(ODE ode) {
    super(ode);
  }

  /**
   * Initializes the ODE solver.
   *
   * Two temporary state arrays and one rate array are allocated.
   * The number of differential equations is determined by invoking getState().length on the ODE.
   *
   * @param stepSize
   */
  public void initialize(double stepSize) {
    super.initialize(stepSize);
    rate = new double[numEqn];
    priorState = new double[numEqn];
    currentState = new double[numEqn];
    estimatePreviousState();
  }

  /**
   * Sets the step size.
   *
   * The step size remains fixed in this algorithm
   *
   * @param stepSize
   */
  public void setStepSize(double stepSize) {
    initialize(stepSize);
  }

  /**
   * Estimate's the previous state using the velocity Verlet method.
   */
  void estimatePreviousState() {
    double[] state = (ode==null) ? null : ode.getState();
    if(state==null) {
      return;
    }
    // save the current state
    System.arraycopy(state, 0, currentState, 0, state.length);
    // step the current state back to the previous state
    ODESolver verlet = new Verlet(ode);
    verlet.setStepSize(-stepSize); // reverse sign for backward step
    verlet.step();                 // do a backward step
    // save the state after it has been stepped backward
    System.arraycopy(state, 0, priorState, 0, state.length);
    // restore the current state
    System.arraycopy(currentState, 0, state, 0, state.length);
    verlet = null;
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
    // state[]: x1, d x1/dt, x2, d x2/dt .... xN, d xN/dt, t
    double[] state = ode.getState();
    if(state.length!=numEqn) {
      initialize(stepSize);
    }
    System.arraycopy(state, 0, currentState, 0, numEqn); // save the current state
    ode.getRate(state, rate);                            // get the rate
    double dtSquared = stepSize*stepSize; // the step size squared
    double dt2 = 2*stepSize;
    // advance the state
    for(int i = 0; i<numEqn-1; i += 2) {
      // note that "+= state[i]" is correct because the leapfrog algorithm uses 2*state[i].
      state[i] += state[i]-priorState[i]+dtSquared*rate[i+1];       // even numbers are positions
      // x[i] has been advanced; advance v[i]
      state[i+1] = (state[i]-priorState[i])/dt2+rate[i+1]*stepSize; // advance the velocity
    }
    if(numEqn%2==1) {                             // advance last equation if  we have an odd number of equations
      state[numEqn-1] += stepSize*rate[numEqn-1]; // usually the independent variable
    }
    System.arraycopy(currentState, 0, priorState, 0, numEqn); // save the current state as the prior state
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
