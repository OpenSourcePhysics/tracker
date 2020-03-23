/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs;

/**
 * A base interface for the model of a simulation
 */
public interface Model {
  /**
   * Sets the simulation in which this model operates
   * @param Simulation _simulation  The simulation that will use this
   * object as model
   */
  // public void setSimulation (Simulation _simulation);

  /**
   * Gets the simulation in which this model runs (if any)
   */
  public Simulation getSimulation();

  /**
   * Gets the view for this model (if any)
   */
  public View getView();

  /**
   * Sets the view for this model
   */
  // public void setView (View _aView);
  // --------------------------------------------------------
  // Model states
  // --------------------------------------------------------
  public void reset();

  public void initialize();

  public void step();

  public void update();

} // End of class

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
