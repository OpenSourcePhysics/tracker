/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * TriggerODE is a wrapper for an ODE that can be used by ODEEventSolvers.
 * It acts as a mediator between the ODE and the solver
 * The ODE state array must have fixed length
 * The ODESolver must call getState only once
 *
 * @author       Francisco Esquembre (March 2004)
 */
class TriggerODE implements ODE {
  protected ODE ode;
  private int size;
  private double[] odestate;
  private double[] state;

  /**
   * Constructor TriggerODE
   * @param _ode
   */
  public TriggerODE(ODE _ode) {
    ode = _ode;
    odestate = ode.getState();
    size = odestate.length;
    state = new double[size];
    System.arraycopy(odestate, 0, state, 0, size);
  }

  public void setState(double[] newstate) {
    System.arraycopy(newstate, 0, state, 0, size);
  }

  // Interface with the real ODE
  public void readRealState() {
    odestate = ode.getState();
    System.arraycopy(odestate, 0, state, 0, size);
  }

  public void updateRealState() {
    System.arraycopy(state, 0, odestate, 0, size);
  }

  // Masking the real ODE
  public double[] getState() {
    return state;
  }

  public void getRate(double[] _state, double[] _rate) {
    ode.getRate(_state, _rate); // Defer to the real ODE
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
