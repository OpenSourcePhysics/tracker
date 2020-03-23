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
 * <p>Description: A 3D Surface of the form (x(u,v),y(u,v),z(u,v))</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
public interface ElementSurface extends Element {
  /**
   * Sets the data of the surface.
   * @param data the double[nu][nv][3] array of coordinates for the surface.
   */
  public void setData(double[][][] data);

  /**
   * Gets the data of the surface.
   * @return the double[nu][nv][3] array of coordinates of the surface.
   */
  public double[][][] getData();

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------
  static abstract class Loader extends Element.Loader {
    public void saveObject(XMLControl control, Object obj) {
      super.saveObject(control, obj);
      ElementSurface element = (ElementSurface) obj;
      control.setValue("data", element.getData()); //$NON-NLS-1$
    }

    public Object loadObject(XMLControl control, Object obj) {
      super.loadObject(control, obj);
      ElementSurface element = (ElementSurface) obj;
      element.setData((double[][][]) control.getObject("data")); //$NON-NLS-1$
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
