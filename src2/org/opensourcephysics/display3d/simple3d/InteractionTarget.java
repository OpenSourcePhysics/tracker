/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.simple3d;

/**
 * The simple3d implementation of InteractionTarget
 */
public class InteractionTarget implements org.opensourcephysics.display3d.core.interaction.InteractionTarget {
  private final Element element;
  private final int type;
  private boolean enabled = false;
  private boolean affectsGroup = false;
  private String command = null;

  /**
   * Constructor for the class
   * @param _element Element
   * @param _type int Either Element.TARGET_POSITION or Element.TARGET_SIZE
   */
  InteractionTarget(Element _element, int _type) {
    element = _element;
    type = _type;
  }

  /**
   * Returns the Element that contains the target
   * @return Element
   */
  final Element getElement() {
    return element;
  }

  /**
   * Returns the type of target
   * @return int
   */
  final int getType() {
    return type;
  }

  /**
   * Enables/Disables the target
   * @param value boolean
   */
  final public void setEnabled(boolean value) {
    enabled = value;
  }

  /**
   * Returns the enabled status of the target
   * @return boolean
   */
  final public boolean isEnabled() {
    return enabled;
  }

  /**
   * Returns the action command of this target
   * @return String
   */
  final public String getActionCommand() {
    return command;
  }

  /**
   * Sets the action commmand for this target
   * @param command String
   */
  final public void setActionCommand(String command) {
    this.command = command;
  }

  /**
   * Whether the interaction with the target affects the top-level group
   * of the element that contains it (instead of only affecting the element).
   * Default is false.
   * @param value boolean
   */
  final public void setAffectsGroup(boolean value) {
    affectsGroup = value;
  }

  /**
   * Whether the target affects the top-level group
   * @return boolean
   */
  final public boolean getAffectsGroup() {
    return affectsGroup;
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
