/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;

/**
 * This is an ObjectLoader implementation that attempts to create a new object
 * of class element.getObjectClass() but takes no other action. It is used as
 * the default loader and can be extended for use by particular classes.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class XMLLoader implements XML.ObjectLoader {
  /**
   * Empty method.
   *
   * @param control the control
   * @param obj the object
   */
  public void saveObject(XMLControl control, Object obj) {

  /** empty block */
  }

  /**
   * Creates a new object if the class type has a no-arg constructor.
   *
   * @param control the control
   * @return the new object
   */
  public Object createObject(XMLControl control) {
    try {
      return control.getObjectClass().newInstance();
    } catch(Exception ex) {
      return null;
    }
  }

  /**
   * Loads the object with xml data.
   *
   * Calculations and Animations should reinitialize after they are loaded.
   *
   * @param control the control
   * @param obj the object
   */
  public Object loadObject(XMLControl control, Object obj) {
    return obj;
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
