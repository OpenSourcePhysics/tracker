/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.core;
import org.opensourcephysics.controls.XMLControl;

/**
 * <p>Title: ElementSurface</p>
 * <p>Description: A 3D plane.</p>
 * The plane is specified by (its position, size, and) its direction vectors</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
public interface ElementPlane extends Element {
  /**
   * Sets the first direction vector of th eplane
   * @param data the double[3] array for the first vector
   */
  public void setFirstDirection(double[] vector);

  /**
   * Gets the first direction vector of th eplane
   * @return the double[3] array for the first vector
   */
  public double[] getFirstDirection();

  /**
   * Sets the second direction vector of th eplane
   * @param data the double[3] array for the first vector
   */
  public void setSecondDirection(double[] vector);

  /**
   * Gets the second direction vector of th eplane
   * @return the double[3] array for the first vector
   */
  public double[] getSecondDirection();

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------
  static abstract class ElementPlaneLoader extends Element.Loader {
    public void saveObject(XMLControl control, Object obj) {
      super.saveObject(control, obj);
      ElementPlane element = (ElementPlane) obj;
      control.setValue("first direction", element.getFirstDirection());   //$NON-NLS-1$
      control.setValue("second direction", element.getSecondDirection()); //$NON-NLS-1$
    }

    public Object loadObject(XMLControl control, Object obj) {
      super.loadObject(control, obj);
      ElementPlane element = (ElementPlane) obj;
      element.setFirstDirection((double[]) control.getObject("first direction"));   //$NON-NLS-1$
      element.setSecondDirection((double[]) control.getObject("second direction")); //$NON-NLS-1$
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
