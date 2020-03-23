/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs;

/**
 * A base interface for the graphical user interface of a simulation
 */
public interface View extends org.opensourcephysics.controls.Control {
  /**
   * Clearing any previous data
   */
  public void reset();

  /**
   * updating all possible data
   */
  public void initialize();

  /**
   * Read current data
   */
  public void read();

  /**
   * Read a single variable
   */
  public void read(String _variable);

  /**
   * Accept data sent
   */
  public void update();

  /**
   * Get a graphical object
   * @param _name A keyword that identifies the graphical object that
   * must be retrieved. Typically its name.
   * @return The graphical component
   */
  public java.awt.Component getComponent(String _name);

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
