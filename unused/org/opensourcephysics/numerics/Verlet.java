/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * Verlet:  A velocity Verlet method ODE solver.
 *
 * The velocity Verlet algorithm is a self-starting equivalent of the Verlet algorithm.
 * It assumes a constant acceleration to estimate the final position
 * and an average accleration to estimate the final velocity.
 * The position is first updated, the force is calcualted at the new position,
 * and then the velocity is updated.
 *
 * x(n+1) = x(n) + v(n)* dt + a(n)*dt*dt/2
 * a_est=F(x(n+1),v(n),t)/m
 * v(n+1) = v(n) + (a(n)+a_est)*dt/2
 *
 * CAUTION! This implementation assumes that the state variables alternate
 * between position and velocity with the last variable being time.
 * That is, the  state vector is ordered as follows:
 *
 * x1, d x1/dt, x2, d x2/dt, x3, d x3/dt ..... xN, d xN/dt, t
 *
 * @author       Wolfgang Christian
 * @version 1.1
 */
public class Verlet extends AbstractODESolver {
  private double[] rate1;       // stores the initial rate
  private double[] rate2;       // used to compute the estimated the acceleration at x(n+1).
  private int rateCounter = -1; // step has not yet been called

  /**
   * Constructs the velocity Verlet ODESolver for a system of ordinary  differential equations.
   *
   * @param ode the system of differential equations.
   */
  public Verlet(ODE ode) {
    super(ode);
  }

  /**
   * Initializes the ODE solver.
   *
   * The rate array is allocated.  The number of differential equations is
   * determined by invoking getState().length on the ODE.
   *
   * @param stepSize
   */
  public void initialize(double stepSize) {
    super.initialize(stepSize);
    rate1 = new double[numEqn];
    rate2 = new double[numEqn];
    rateCounter = -1; // step has not yet been called
  }

  /**
   * Gets the counter that records the number of times the rate has been evaluated during the current step.
   *
   * This method allows a model to improve its performance
   * by enabling the model to determine if this is the first or second time that the rate is being evaluated.
   * The Verlet algorithm first invokes the model's getRate method to update the position and
   * then again to update velocity.  Because the force at the new position is computed the
   * second time that getRate is invoked, a model can improve its performance if it skips the force computation
   * during the first call to getRate.
   *
   * A typical dynamics simulation should comptute the force when rateCounter is one and stores this force
   * for use during the next postion update.
   *
   * The Verlet algorithm will perform correctly (but more slowly) if the
   * force is computed every time that getRate is invoked.
   *
   * @return int  the counter
   */
  public int getRateCounter() {
    return rateCounter;
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
    // state[]: x1, d x1/dt, x2, d x2/dt .... xN, d xN/dt, t
    double[] state = ode.getState();
    if(state.length!=numEqn) {
      initialize(stepSize);
    }
    rateCounter = 0; // getRate has not been called
    ode.getRate(state, rate1); // get the initial rate
    double dt2 = stepSize*stepSize; // the step size squared
    // increment the positions using the velocity and acceleration
    for(int i = 0; i<numEqn-1; i += 2) {
      state[i] += stepSize*rate1[i]+dt2*rate1[i+1]/2;
    }
    rateCounter = 1; // getRate has been called once
    ode.getRate(state, rate2); // rate at the new positions
    rateCounter = 2; // getRate has been called twice
    for(int i = 1; i<numEqn; i += 2) {
      // increment the velocities with the average rate
      state[i] += stepSize*(rate1[i]+rate2[i])/2.0;
    }
    if(numEqn%2==1) {                              // last equation if  we have an odd number of equations
      state[numEqn-1] += stepSize*rate1[numEqn-1]; // usually the independent variable
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
