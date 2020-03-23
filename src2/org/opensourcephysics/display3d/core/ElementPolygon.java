/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.core;
import org.opensourcephysics.controls.XMLControl;

/**
 * <p>Title: ElementPolygon</p>
 * <p>Description: A 3D polygon.</p>
 * Polygons can be closed (a real polygon) or open (polylines).
 * @author Francisco Esquembre
 * @version March 2005
 */
public interface ElementPolygon extends Element {
  /**
   * Sets whether the polygon is closed
   * @param closed boolean
   */
  public void setClosed(boolean closed);

  /**
   * Gets whether the polygon is closed
   * @return boolean
   */
  public boolean isClosed();

  /**
   * Sets the data for the points of the polygon.
   * Each entry in the data array corresponds to one vertex.
   * If the polygon is closed, the last point will be connected
   * to the first one and the interior will be filled
   * (unless the fill color of the style is set to null).
   * @param data double[][] the double[nPoints][3] array with the data
   */
  public void setData(double[][] data);

  /**
   * Sets the data for the points of the polygon.
   * Each entry in the data array corresponds to one vertex.
   * If the polygon is closed, the last point will be connected
   * to the first one and the interior will be filled
   * (unless the fill color of the style is set to null).
   * The data array is copied, so subsequence changes to the original
   * array do not affect the polygon, until the setData() method is invoked.
   * If the arrays have different lengths, the last element of the shortest
   * array is repeated to match the longest array.
   * @param xArray double[] the double[nPoints] array with the X coordinates
   * @param yArray double[] the double[nPoints] array with the Y coordinates
   * @param zArray double[] the double[nPoints] array with the Z coordinates
   */
  public void setData(double[] xArray, double[] yArray, double[] zArray);

  /**
   * Gets ths data of the points fo the polygon
   * @return double[][] the double[nPoints][3] array with the data
   */
  public double[][] getData();

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------
  static abstract class ElementPolygonLoader extends Element.Loader {
    public void saveObject(XMLControl control, Object obj) {
      super.saveObject(control, obj);
      ElementPolygon element = (ElementPolygon) obj;
      control.setValue("closed", element.isClosed()); //$NON-NLS-1$
      control.setValue("data", element.getData());    //$NON-NLS-1$
    }

    public Object loadObject(XMLControl control, Object obj) {
      super.loadObject(control, obj);
      ElementPolygon element = (ElementPolygon) obj;
      element.setClosed(control.getBoolean("closed"));         //$NON-NLS-1$
      element.setData((double[][]) control.getObject("data")); //$NON-NLS-1$
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
