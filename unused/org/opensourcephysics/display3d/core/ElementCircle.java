/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.core;
import org.opensourcephysics.controls.XMLControl;

/**
 * <p>Title: ElementSegment</p>
 * <p>Description: Draws a circle at its position with the given size.
 * The default size is zero (which draws a point).</p>
 * Because a circle is essentialy a 2D object, it doesn't behave completely
 * as a 3D object. Thus, its center will be affected by transformations
 * of the element, BUT ITS SIZE WON'T. Moreover, in 3D visualizations, the
 * maximum of sizeX and sizeY is used for its horizontal size. In all other
 * views, the corresponding size is used.
 *
 * @author Francisco Esquembre
 * @version March 2005
 */
public interface ElementCircle extends Element {
  /**
   * Sets the rotation angle for the circle. Default is 0.
   * @param angle the rotation angle
   */
  public void setRotationAngle(double angle);

  /**
   * Gets the rotation angle for the circle
   */
  public double getRotationAngle();

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------
  static abstract class Loader extends Element.Loader {
    public void saveObject(XMLControl control, Object obj) {
      super.saveObject(control, obj);
      ElementCircle element = (ElementCircle) obj;
      control.setValue("rotation angle", element.getRotationAngle()); //$NON-NLS-1$
    }

    public Object loadObject(XMLControl control, Object obj) {
      super.loadObject(control, obj);
      ElementCircle element = (ElementCircle) obj;
      element.setRotationAngle(control.getDouble("rotation angle")); //$NON-NLS-1$
      return obj;
    }

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
