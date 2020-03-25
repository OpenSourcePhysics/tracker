/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * AbstractODE provides a common superclass for ODESolvers.
 *
 * @author       Wolfgang Christian
 * @version 1.0
 */
public abstract class AbstractODESolver extends Object implements ODESolver {
  protected double stepSize = 0.1; // parameter increment such as delta time
  protected int numEqn = 0;        // number of equations
  protected ODE ode;               // object that computes rate

  /**
   * Constructs the ODESolver for a system of ordinary differential equations.
   *
   * @param _ode the system of differential equations.
   */
  public AbstractODESolver(ODE _ode) {
    ode = _ode;
    initialize(0.1);
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
  abstract public double step();

  /**
   * Sets the step size.
   *
   * The step size remains fixed in this algorithm
   *
   * @param _stepSize
   */
  public void setStepSize(double _stepSize) {
    stepSize = _stepSize;
  }

  /**
   * Initializes the ODE solver.
   *
   * The rate array is allocated.  The number of differential equations is
   * determined by invoking getState().length on the ODE.
   *
   * @param _stepSize
   */
  public void initialize(double _stepSize) {
    stepSize = _stepSize;
    double state[] = ode.getState();
    if(state==null) { // state vector not defined
      numEqn = 0;
    } else {
      numEqn = state.length;
    }
  }

  /**
   * Gets the step size.
   *
   * The stepsize is constant in this algorithm
   *
   * @return the step size
   */
  public double getStepSize() {
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
