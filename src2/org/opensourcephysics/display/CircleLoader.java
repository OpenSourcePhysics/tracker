/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Color;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;

/**
 * A class to save and load Circle objects in an XMLControl.
 */
public class CircleLoader extends XMLLoader {
  /**
   * Saves the Circle's data in the xml control.
   * @param control XMLControl
   * @param obj Object
   */
  public void saveObject(XMLControl control, Object obj) {
    Circle circle = (Circle) obj;
    control.setValue("x", circle.x);                 //$NON-NLS-1$
    control.setValue("y", circle.y);                 //$NON-NLS-1$
    control.setValue("drawing r", circle.pixRadius); //$NON-NLS-1$
    control.setValue("color", circle.color);         //$NON-NLS-1$
  }

  /**
   * Creates a Circle.
   * @param control XMLControl
   * @return Object
   */
  public Object createObject(XMLControl control) {
    return new Circle();
  }

  /**
   * Loads data from the xml control into the Circle object.
   * @param control XMLControl
   * @param obj Object
   * @return Object
   */
  public Object loadObject(XMLControl control, Object obj) {
    Circle circle = (Circle) obj;
    circle.x = control.getDouble("x"); //$NON-NLS-1$
    circle.y = control.getDouble("y"); //$NON-NLS-1$
    int r = 6; // the default
    if(control.getObject("drawing r")!=null) { //$NON-NLS-1$
      r = control.getInt("drawing r");         //$NON-NLS-1$
    } else if(control.getObject("r")!=null) {  // included to be backward compatible with old loader //$NON-NLS-1$
      r = control.getInt("r");                 //$NON-NLS-1$
    }
    circle.pixRadius = (r<=0) ? 6 : r;
    circle.color = (Color) control.getObject("color"); //$NON-NLS-1$
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
