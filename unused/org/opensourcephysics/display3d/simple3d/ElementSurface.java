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
public class ElementSurface extends AbstractTile implements org.opensourcephysics.display3d.core.ElementSurface {
  // Configuration variables
  protected double[][][] data;
  // Implementation variables
  protected int nu = -1, nv = -1; // Make sure arrays are allocated

  { // Initialization block
    setXYZ(0.0, 0.0, 0.0);
    setSizeXYZ(1.0, 1.0, 1.0);
  }

  // -------------------------------------
  // Configuration
  // -------------------------------------
  public void setData(double[][][] data) {
    this.data = data;
    setElementChanged(true);
  }

  public double[][][] getData() {
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
    //int theNu = data.length-1, theNv = data[0].length-1;
    for(int i = 0, n1 = data.length; i<n1; i++) {
      for(int j = 0, n2 = data[0].length; j<n2; j++) {
        System.arraycopy(data[i][j], 0, aPoint, 0, 3);
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
    int theNu = data.length-1, theNv = data[0].length-1;
    if((nu==theNu)&&(nv==theNv)) {
      // empty                                     // No need to reallocate arrays
    } else {
      nu = theNu;
      nv = theNv;
      setCorners(new double[nu*nv][4][3]); // Reallocate arrays
    }
    int tile = 0;
    for(int v = 0; v<nv; v++) {
      for(int u = 0; u<nu; u++, tile++) {
        for(int k = 0; k<3; k++) {
          corners[tile][0][k] = data[u][v][k];
          corners[tile][1][k] = data[u+1][v][k];
          corners[tile][2][k] = data[u+1][v+1][k];
          corners[tile][3][k] = data[u][v+1][k];
        }
      }
    }
    for(int i = 0; i<numberOfTiles; i++) {
      for(int j = 0, sides = corners[i].length; j<sides; j++) {
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
      return new ElementSurface();
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
