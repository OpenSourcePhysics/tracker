/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.core.interaction;

/**
 * The basic interface for an active target in an InteractionSource
 */
public interface InteractionTarget {
  /**
   * Enables/Disables the target
   * @param value boolean
   */
  void setEnabled(boolean value);

  /**
   * Returns the enabled status of the target
   * @return boolean
   */
  boolean isEnabled();

  /**
   * Returns the action command of this target
   * @return String
   */
  String getActionCommand();

  /**
   * Sets the action commmand for this target
   * @param command String
   */
  void setActionCommand(String command);

  /**
   * Whether the interaction with the target affects the top-level group
   * of the element that contains it (instead of only affecting the element).
   * So, for instance, if the target allows to move the element and this
   * flag is set to <code>true</code>, trying to move the element will move
   * the whole group to which the element belongs.
   * This flag only makes sense for objects of the Element class (which
   * can belong to a Group). Default value of this flag is <code>false</code>.
   * @param value boolean
   * @see org.opensourcephysics.display3d.core.Element
   */
  void setAffectsGroup(boolean value);

  /**
   * Whether the target affects the top-level group of the element
   * @return boolean
   */
  boolean getAffectsGroup();

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
