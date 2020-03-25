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
 * <p>Description: Painter's algorithm implementation of a Plane</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
public class ElementPlane extends AbstractTile implements org.opensourcephysics.display3d.core.ElementPlane {
  // Configuration variables
  private double vectorU[] = {1.0, 0.0, 0.0};
  private double vectorV[] = {0.0, 1.0, 0.0};
  // Implementation variables
  private int nu = -1, nv = -1; // Make sure arrays are allocated
  private double vectorUSize = 1.0, vectorVSize = 1.0;

  { // Initialization block
    setXYZ(0.0, 0.0, 0.0);
    setSizeXYZ(1.0, 1.0, 1.0);
  }

  // -------------------------------------
  // Configuration
  // -------------------------------------
  public void setFirstDirection(double[] vector) {
    vectorU[0] = vector[0];
    vectorU[1] = vector[1];
    vectorU[2] = vector[2];
    vectorUSize = Math.sqrt(vectorU[0]*vectorU[0]+vectorU[1]*vectorU[1]+vectorU[2]*vectorU[2]);
    setElementChanged(true);
  }

  public double[] getFirstDirection() {
    return new double[] {vectorU[0], vectorU[1], vectorU[2]};
  }

  public void setSecondDirection(double[] vector) {
    vectorV[0] = vector[0];
    vectorV[1] = vector[1];
    vectorV[2] = vector[2];
    vectorVSize = Math.sqrt(vectorV[0]*vectorV[0]+vectorV[1]*vectorV[1]+vectorV[2]*vectorV[2]);
    setElementChanged(true);
  }

  public double[] getSecondDirection() {
    return new double[] {vectorV[0], vectorV[1], vectorV[2]};
  }

  // -------------------------------------
  // Private or protected methods
  // -------------------------------------
  protected synchronized void computeCorners() {
    int theNu = 1, theNv = 1;
    org.opensourcephysics.display3d.core.Resolution res = getRealStyle().getResolution();
    if(res!=null) {
      switch(res.getType()) {
         case org.opensourcephysics.display3d.core.Resolution.DIVISIONS :
           theNu = Math.max(res.getN1(), 1);
           theNv = Math.max(res.getN2(), 1);
           break;
         case org.opensourcephysics.display3d.core.Resolution.MAX_LENGTH :
           theNu = Math.max((int) Math.round(0.49+Math.abs(getSizeX())*vectorUSize/res.getMaxLength()), 1);
           theNv = Math.max((int) Math.round(0.49+Math.abs(getSizeY())*vectorVSize/res.getMaxLength()), 1);
           break;
      }
    }
    if((nu!=theNu)||(nv!=theNv)) { // Reallocate arrays
      nu = theNu;
      nv = theNv;
      setCorners(new double[nu*nv][4][3]);
    }
    int tile = 0;
    double du = getSizeX()/nu, dv = getSizeY()/nv;
    for(int i = 0; i<nu; i++) { // x-y sides
      double u = i*du-getSizeX()/2;
      for(int j = 0; j<nv; j++) {
        double v = j*dv-getSizeY()/2;
        for(int k = 0; k<3; k++) {
          corners[tile][0][k] = u*vectorU[k]+v*vectorV[k];
        }
        for(int k = 0; k<3; k++) {
          corners[tile][1][k] = (u+du)*vectorU[k]+v*vectorV[k];
        }
        for(int k = 0; k<3; k++) {
          corners[tile][2][k] = (u+du)*vectorU[k]+(v+dv)*vectorV[k];
        }
        for(int k = 0; k<3; k++) {
          corners[tile][3][k] = u*vectorU[k]+(v+dv)*vectorV[k];
        }
        tile++;                 // The upper side
      }
    }
    for(int i = 0; i<numberOfTiles; i++) {
      for(int j = 0, sides = corners[i].length; j<sides; j++) {
        toSpaceFrame(corners[i][j]);
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

  static private class Loader extends org.opensourcephysics.display3d.core.ElementPlane.Loader {
    public Object createObject(XMLControl control) {
      return new ElementPlane();
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
