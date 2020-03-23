/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * ODE defines a system of differential equations by providing access to the rate equations.
 *
 * @author       Wolfgang Christian
 */
public interface ODE {
  /**
   * Gets the state variables.
   *
   * The getState method is invoked by an ODESolver to obtain the initial state of the system.
   * The ODE solver advances the solution and then copies new values into the
   * state array at the end of the solution step.
   *
   * @return state  the state
   */
  public double[] getState();

  /**
   * Gets the rate of change using the argument's state variables.
   *
   * This method may be invoked many times with different intermediate states
   * as an ODESolver is carrying out the solution.
   *
   * @param state  the state array
   * @param rate   the rate array
   */
  public void getRate(double[] state, double[] rate);

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
