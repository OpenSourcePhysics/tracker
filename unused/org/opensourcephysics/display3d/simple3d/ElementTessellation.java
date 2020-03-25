/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.simple3d;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

/**
 * <p>Title: ElementBox</p>
 * <p>Description: Painter's algorithm implementation of a Surface</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
public class ElementTessellation extends AbstractTile implements org.opensourcephysics.display3d.core.ElementTessellation {
  // Configuration variables
  protected double[][][] data;

  { // Initialization block
    setXYZ(0.0, 0.0, 0.0);
    setSizeXYZ(1.0, 1.0, 1.0);
  }

  // -------------------------------------
  // Configuration
  // -------------------------------------
  public void setTiles(double[][][] data) {
    if(this.data==data) {
      return;
    }
    int n = data.length;
    double[][][] newCorners = new double[n][][];
    for(int i = 0; i<n; i++) {
      newCorners[i] = new double[data[i].length][3];
    }
    setCorners(newCorners);
    this.data = data;
    setElementChanged(true);
  }

  public double[][][] getTiles() {
    return this.data;
  }

  // -------------------------------------
  // Private or protected methods
  // -------------------------------------
  public void getExtrema(double[] min, double[] max) {
    double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
    double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
    double minZ = Double.POSITIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
    double[] aPoint = new double[3];
    for(int i = 0; i<numberOfTiles; i++) {
      int sides = corners[i].length;
      for(int j = 0; j<sides; j++) {
        System.arraycopy(corners[i][j], 0, aPoint, 0, 3);
        sizeAndToSpaceFrame(aPoint);
        minX = Math.min(minX, aPoint[0]);
        maxX = Math.max(maxX, aPoint[0]);
        minY = Math.min(minY, aPoint[1]);
        maxY = Math.max(maxY, aPoint[1]);
        minZ = Math.min(minZ, aPoint[2]);
        maxZ = Math.max(maxZ, aPoint[2]);
      }
    }
    min[0] = minX;
    max[0] = maxX;
    min[1] = minY;
    max[1] = maxY;
    min[2] = minZ;
    max[2] = maxZ;
  }

  protected synchronized void computeCorners() {
    if(data==null) {
      return;
    }
    //int tile = 0;
    for(int i = 0; i<data.length; i++) {
      for(int j = 0, n = data[i].length; j<n; j++) {
        System.arraycopy(data[i][j], 0, corners[i][j], 0, 3);
      }
    }
    for(int i = 0; i<numberOfTiles; i++) {
      for(int j = 0, sides = corners[i].length; j<sides; j++) {
        System.arraycopy(data[i][j], 0, corners[i][j], 0, 3);
        sizeAndToSpaceFrame(corners[i][j]);
      }
    }
    setElementChanged(false);
  }

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------

  /**
   * Returns an XML.ObjectLoader to save and load object data.
   * @return the XML.ObjectLoader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  static private class Loader extends org.opensourcephysics.display3d.core.ElementSurface.Loader {
    public Object createObject(XMLControl control) {
      return new ElementTessellation();
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
