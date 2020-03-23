/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * ODEAdaptiveSolver extends the ODE solver to add adaptive step size capabilities.
 *
 * Adaptive ODE solvers adjust the step size until that the desired tolerance is reached.
 *
 * The client's state can effect the internal state of the ODE solver. Some adaptive solvers
 * advance an internal copy of client's state.  This internal state is then copied to the client after
 * every step.  Other solvers estimate the optimal time step using the client's state. Clients
 * should therfore always invoke the solver's initialize method after setting their initial conditions.
 *
 * @author       Wolfgang Christian
 */
public interface ODEAdaptiveSolver extends ODESolver {
  public static final int NO_ERROR = 0;
  public static final int DID_NOT_CONVERGE = 1;
  public static final int BISECTION_EVENT_NOT_FOUND = 2;

  /**
   * Sets the tolerance of the adaptive ODE sovler.
   * @param tol the tolerance
   */
  public void setTolerance(double tol);

  /**
   * Gets the tolerance of the adaptive ODE sovler.
   * @return
   */
  public double getTolerance();

  /**
   * Gets the error code.
   * Error codes:
   *   ODEAdaptiveSolver.NO_ERROR
   *   ODEAdaptiveSolver.DID_NOT_CONVERGE
   *   ODEAdaptiveSolver.BISECTION_EVENT_NOT_FOUND=2;
   * @return int
   */
  public int getErrorCode();

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
