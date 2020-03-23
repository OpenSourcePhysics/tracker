/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.core.interaction;

/**
 *
 * <p>Title: InteractionSource</p>
 *
 * <p>Description: This is the interface for an object that the user
 * can interact with. The object contains one or several interaction targets,
 * which are hot spots that respond to user interaction by issuing
 * an interaction event.</p>
 * <p>Classes implementing this class should document what targets
 * the contain.</p>
 * <p>Classes implementing the InteractionListener interface can register
 * to receive interaction events using the <code>addInteractionListener</code>
 * method of this interface.</p>
 *
 * <p>Copyright: Open Source Physics project</p>
 * @author Francisco Esquembre
 * @version May 2005
 * @see InteractionEvent
 * @see InteractionListener
 */
public interface InteractionSource {
  /**
   * Gives access to one of the targets of this source.
   * Sources should document the list of their available targets.
   * @param target An integer number that identifies the target in the source.
   * @return InteractionTarget
   */
  InteractionTarget getInteractionTarget(int target);

  /**
   * Adds the specified interaction listener to receive interaction events
   * to any of its targets from this source.
   * @param listener An object that implements the InteractionListener interface
   * @see InteractionListener
   */
  void addInteractionListener(InteractionListener listener);

  /**
   * Removes the specified interaction listener
   * @see InteractionListener
   */
  void removeInteractionListener(InteractionListener listener);

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
