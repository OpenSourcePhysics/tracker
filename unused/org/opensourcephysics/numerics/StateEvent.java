/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * StateEvent defines an event consisting in that a given numeric value
 * changes sign from positive to negative. This numeric value usually
 * consists in an expression of the variables of an ODE.
 * Every valid model state must (in principle) provide a value f(t)>=+tolerance
 * at every instant of time, where tolerance is a (small positive) number
 * prescribed by the user.
 * However, values in the range (-tolerance,+tolerance) are still accepted.
 * The event really happens when the value at the next instant of time
 * satisfies f(t+dt)<=-tolerance.
 * Then, finding the moment when the event takes place corresponds to finding
 * a root of f(t). More precisely, and because of numerical approximation,
 * a root is considered to be found when -tolerance<f(t)<+tolerance.
 * When the event takes place (that is, at this instant of time) an action is
 * triggered. The action must guarantee that either the state at that moment
 * is again valid (f(t)>=+tolerance) or, if this is not true, at least
 * the state at the next instant t+dt will not trigger an event
 * (i.e. f(t+dt)>-tolerance). (If this rule is violated an infinite loop may be
 * caused.)
 *
 * @author Francisco Esquembre (Feb 2004, corrected May 2005)
 */
public interface StateEvent extends MultiVarFunction {
  /**
   * Returns the tolerance for the event.
   * @return the tolerance
   */
  public double getTolerance();

  /**
   * Returns the value f(t) for a given state of the model.
   * The values returned by this method will be used by the solver
   * to find the exact time at which the event took place.
   * @param state The current state of the ODE
   * @return the value for this state
   */
  public double evaluate(double[] state);

  /**
   * What to do when the event has taken place.
   * The return value tells the solver wether it should stop the
   * computation at the exact moment of the event or continue
   * solving the ODE for the rest of the prescribed dt.
   * @return true if the solver should return at this instant of time,
   * false otherwise.
   */
  public boolean action();

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
