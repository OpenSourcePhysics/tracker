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
 * <p>Description: Painter's algorithm implementation of a Box</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
public class ElementBox extends AbstractTile implements org.opensourcephysics.display3d.core.ElementBox {
  // Configuration variables
  private boolean closedBottom = true, closedTop = true;
  // Implementation variables
  private boolean changeNTiles = true;
  private int nx = -1, ny = -1, nz = -1; // Make sure arrays are allocated
  private double[][][] standardBox = null;

  { // Initialization block
    getStyle().setResolution(new Resolution(3, 3, 3));
  }

  // -------------------------------------
  // Configuration
  // -------------------------------------
  public void setClosedBottom(boolean close) {
    this.closedBottom = close;
    setElementChanged(true);
    changeNTiles = true;
  }

  public boolean isClosedBottom() {
    return this.closedBottom;
  }

  public void setClosedTop(boolean close) {
    this.closedTop = close;
    setElementChanged(true);
    changeNTiles = true;
  }

  public boolean isClosedTop() {
    return this.closedTop;
  }

  // -------------------------------------
  // Private or protected methods
  // -------------------------------------
  protected synchronized void computeCorners() {
    int theNx = 1, theNy = 1, theNz = 1;
    org.opensourcephysics.display3d.core.Resolution res = getRealStyle().getResolution();
    if(res!=null) {
      switch(res.getType()) {
         case org.opensourcephysics.display3d.core.Resolution.DIVISIONS :
           theNx = Math.max(res.getN1(), 1);
           theNy = Math.max(res.getN2(), 1);
           theNz = Math.max(res.getN3(), 1);
           break;
         case org.opensourcephysics.display3d.core.Resolution.MAX_LENGTH :
           theNx = Math.max((int) Math.round(0.49+Math.abs(getSizeX())/res.getMaxLength()), 1);
           theNy = Math.max((int) Math.round(0.49+Math.abs(getSizeY())/res.getMaxLength()), 1);
           theNz = Math.max((int) Math.round(0.49+Math.abs(getSizeZ())/res.getMaxLength()), 1);
           break;
      }
    }
    if((nx!=theNx)||(ny!=theNy)||(nz!=theNz)||changeNTiles) { // Reallocate arrays
      nx = theNx;
      ny = theNy;
      nz = theNz;
      changeNTiles = false;
      standardBox = createStandardBox(nx, ny, nz, closedTop, closedBottom);
      setCorners(new double[standardBox.length][4][3]);
    }
    for(int i = 0; i<numberOfTiles; i++) {
      for(int j = 0, sides = corners[i].length; j<sides; j++) {
        System.arraycopy(standardBox[i][j], 0, corners[i][j], 0, 3);
        sizeAndToSpaceFrame(corners[i][j]);
      }
    }
    setElementChanged(false);
  }

  /**
   * Returns the data for a standard box (from (-0.5,-0.5,-0.5) to (0.5,0.5,0.5) )
   * with the given parameters
   */
  static private double[][][] createStandardBox(int nx, int ny, int nz, boolean top, boolean bottom) {
    int nTotal = 2*nx*nz+2*ny*nz;
    if(bottom) {
      nTotal += nx*ny;
    }
    if(top) {
      nTotal += nx*ny;
    }
    double[][][] data = new double[nTotal][4][3];
    int tile = 0;
    double dx = 1.0/nx, dy = 1.0/ny, dz = 1.0/nz;
    for(int i = 0; i<nx; i++) { // x-y sides
      double theX = i*dx-0.5;
      for(int j = 0; j<ny; j++) {
        double theY = j*dy-0.5;
        if(bottom) {
          data[tile][0][0] = theX;
          data[tile][0][1] = theY;
          data[tile][0][2] = -0.5;
          data[tile][1][0] = theX+dx;
          data[tile][1][1] = theY;
          data[tile][1][2] = -0.5;
          data[tile][2][0] = theX+dx;
          data[tile][2][1] = theY+dy;
          data[tile][2][2] = -0.5;
          data[tile][3][0] = theX;
          data[tile][3][1] = theY+dy;
          data[tile][3][2] = -0.5;
          tile++;
        }
        if(top) {               // The upper side
          data[tile][0][0] = theX;
          data[tile][0][1] = theY;
          data[tile][0][2] = 0.5;
          data[tile][1][0] = theX+dx;
          data[tile][1][1] = theY;
          data[tile][1][2] = 0.5;
          data[tile][2][0] = theX+dx;
          data[tile][2][1] = theY+dy;
          data[tile][2][2] = 0.5;
          data[tile][3][0] = theX;
          data[tile][3][1] = theY+dy;
          data[tile][3][2] = 0.5;
          tile++;
        }
      }
    }
    for(int i = 0; i<nx; i++) { // x-z sides
      double theX = i*dx-0.5;
      for(int k = 0; k<nz; k++) {
        double theZ = k*dz-0.5;
        data[tile][0][0] = theX;
        data[tile][0][2] = theZ;
        data[tile][0][1] = -0.5;
        data[tile][1][0] = theX+dx;
        data[tile][1][2] = theZ;
        data[tile][1][1] = -0.5;
        data[tile][2][0] = theX+dx;
        data[tile][2][2] = theZ+dz;
        data[tile][2][1] = -0.5;
        data[tile][3][0] = theX;
        data[tile][3][2] = theZ+dz;
        data[tile][3][1] = -0.5;
        tile++;                 // The upper side
        data[tile][0][0] = theX;
        data[tile][0][2] = theZ;
        data[tile][0][1] = 0.5;
        data[tile][1][0] = theX+dx;
        data[tile][1][2] = theZ;
        data[tile][1][1] = 0.5;
        data[tile][2][0] = theX+dx;
        data[tile][2][2] = theZ+dz;
        data[tile][2][1] = 0.5;
        data[tile][3][0] = theX;
        data[tile][3][2] = theZ+dz;
        data[tile][3][1] = 0.5;
        tile++;
      }
    }
    for(int k = 0; k<nz; k++) { // y-z sides
      double theZ = k*dz-0.5;
      for(int j = 0; j<ny; j++) {
        double theY = j*dy-0.5;
        data[tile][0][2] = theZ;
        data[tile][0][1] = theY;
        data[tile][0][0] = -0.5;
        data[tile][1][2] = theZ+dz;
        data[tile][1][1] = theY;
        data[tile][1][0] = -0.5;
        data[tile][2][2] = theZ+dz;
        data[tile][2][1] = theY+dy;
        data[tile][2][0] = -0.5;
        data[tile][3][2] = theZ;
        data[tile][3][1] = theY+dy;
        data[tile][3][0] = -0.5;
        tile++;                 // The upper side
        data[tile][0][2] = theZ;
        data[tile][0][1] = theY;
        data[tile][0][0] = 0.5;
        data[tile][1][2] = theZ+dz;
        data[tile][1][1] = theY;
        data[tile][1][0] = 0.5;
        data[tile][2][2] = theZ+dz;
        data[tile][2][1] = theY+dy;
        data[tile][2][0] = 0.5;
        data[tile][3][2] = theZ;
        data[tile][3][1] = theY+dy;
        data[tile][3][0] = 0.5;
        tile++;
      }
    }
    return data;
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

  static private class Loader extends org.opensourcephysics.display3d.core.ElementBox.Loader {
    public Object createObject(XMLControl control) {
      return new ElementBox();
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
