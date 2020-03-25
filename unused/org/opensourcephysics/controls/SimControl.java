/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;

/**
 * This interface defines methods for setting values that can be changed after an animation has been initialized.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public interface SimControl extends Control {
  /**
   * Stores a boolean in the control that can be edited after initialization.
   *
   * @param name
   * @param val
   */
  public void setAdjustableValue(String name, boolean val);

  /**
   * Stores a double in the control that can be edited after initialization.
   *
   * @param name
   * @param val
   */
  public void setAdjustableValue(String name, double val);

  /**
   * Stores an integer in the control that can be edited after initialization.
   *
   * @param name
   * @param val
   */
  public void setAdjustableValue(String name, int val);

  /**
   * Stores an object in the control that can be edited after initialization.
   *
   * @param name
   * @param val
   */
  public void setAdjustableValue(String name, Object val);

  /**
   * Removes a parameter from this control.
   *
   * @param name
   */
  public void removeParameter(String name);

  /**
   * Sets the fixed property of the given parameter.
   * Fixed parameters can only be changed before initialization.
   */
  public void setParameterToFixed(String name, boolean fixed);

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
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
