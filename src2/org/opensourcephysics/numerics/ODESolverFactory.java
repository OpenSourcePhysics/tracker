/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * <p>A factory class that creates an ODESolver using a name.</p>
 *
 * @author W. Christian
 * @version 1.0
 */
public class ODESolverFactory {
  private ODESolverFactory() {}

  /**
   * A factory method that creates an ODESolver using a name.
   * @param ode ODE
   * @param solverName String the name of the algorithm
   * @return ODESolver
   */
  public static ODESolver createODESolver(ODE ode, String solverName) {
    solverName = solverName.trim().toLowerCase();
    if(solverName.equals("rk4")) {                    //$NON-NLS-1$
      return new RK4(ode);
    } else if(solverName.equals("multistep")) {       //$NON-NLS-1$
      return new ODEMultistepSolver(ode);
    } else if(solverName.equals("adams4")) {          //$NON-NLS-1$
      return new Adams4(ode);
    } else if(solverName.equals("adams5")) {          //$NON-NLS-1$
      return new Adams5(ode);
    } else if(solverName.equals("adams6")) {          //$NON-NLS-1$
      return new Adams6(ode);
    } else if(solverName.equals("butcher5")) {        //$NON-NLS-1$
      return new Butcher5(ode);
    } else if(solverName.equals("cashkarp45")) {      //$NON-NLS-1$
      return new CashKarp45(ode);
    } else if(solverName.equals("dormandprince45")) { //$NON-NLS-1$
      return new DormandPrince45(ode);
    } else if(solverName.equals("eulerrichardson")) { //$NON-NLS-1$
      return new EulerRichardson(ode);
    } else if(solverName.equals("euler")) {           //$NON-NLS-1$
      return new Euler(ode);
    } else if(solverName.equals("fehlberg8")) {       //$NON-NLS-1$
      return new Fehlberg8(ode);
    } else if(solverName.equals("heun3")) {           //$NON-NLS-1$
      return new Heun3(ode);
    } else if(solverName.equals("ralston2")) {        //$NON-NLS-1$
      return new Ralston2(ode);
    } else if(solverName.equals("verlet")) {          //$NON-NLS-1$
      return new Verlet(ode);
    } else {
      return null;
    }
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
