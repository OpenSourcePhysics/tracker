/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.simple3d;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display3d.simple3d.utils.EllipsoidUtils;

/**
 * <p>Title: ElementEllipsoid</p>
 * <p>Description: Painter's algorithm implementation of an Ellipsoid</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
public class ElementEllipsoid extends AbstractTile implements org.opensourcephysics.display3d.core.ElementEllipsoid {
  // Configuration variables
  private boolean closedBottom = true, closedTop = true;
  private boolean closedLeft = true, closedRight = true;
  private int minAngleU = 0, maxAngleU = 360;
  private int minAngleV = -90, maxAngleV = 90;
  // Implementation variables
  private boolean changeNTiles = true;
  private int nr = -1, nu = -1, nv = -1; // Make sure arrays are allocated
  private double[][][] standardSphere = null;
  //Static
  static final protected double TO_RADIANS = Math.PI/180.0;

  { // Initialization block
    getStyle().setResolution(new Resolution(3, 12, 12));
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

  public void setClosedLeft(boolean close) {
    this.closedLeft = close;
    setElementChanged(true);
    changeNTiles = true;
  }

  public boolean isClosedLeft() {
    return this.closedLeft;
  }

  public void setClosedRight(boolean close) {
    this.closedRight = close;
    setElementChanged(true);
    changeNTiles = true;
  }

  public boolean isClosedRight() {
    return this.closedRight;
  }

  public void setMinimumAngleU(int angle) {
    this.minAngleU = angle;
    setElementChanged(true);
    changeNTiles = true;
  }

  public int getMinimumAngleU() {
    return this.minAngleU;
  }

  public void setMaximumAngleU(int angle) {
    this.maxAngleU = angle;
    setElementChanged(true);
    changeNTiles = true;
  }

  public int getMaximumAngleU() {
    return this.maxAngleU;
  }

  public void setMinimumAngleV(int angle) {
    this.minAngleV = angle;
    setElementChanged(true);
    changeNTiles = true;
  }

  public int getMinimumAngleV() {
    return this.minAngleV;
  }

  public void setMaximumAngleV(int angle) {
    this.maxAngleV = angle;
    setElementChanged(true);
    changeNTiles = true;
  }

  public int getMaximumAngleV() {
    return this.maxAngleV;
  }

  // -------------------------------------
  // Private or protected methods
  // -------------------------------------
  protected synchronized void computeCorners() {
    int theNr = 1, theNu = 1, theNv = 1;
    double angleu1 = minAngleU, angleu2 = maxAngleU;
    if(Math.abs(angleu2-angleu1)>360) {
      angleu2 = angleu1+360;
    }
    double anglev1 = minAngleV, anglev2 = maxAngleV;
    if(Math.abs(anglev2-anglev1)>180) {
      anglev2 = anglev1+180;
    }
    org.opensourcephysics.display3d.core.Resolution res = getRealStyle().getResolution();
    if(res!=null) {
      switch(res.getType()) {
         case org.opensourcephysics.display3d.core.Resolution.DIVISIONS :
           theNr = Math.max(res.getN1(), 1);
           theNu = Math.max(res.getN2(), 1);
           theNv = Math.max(res.getN3(), 1);
           break;
         case org.opensourcephysics.display3d.core.Resolution.MAX_LENGTH :
           double maxRadius = Math.max(Math.max(Math.abs(getSizeX()), Math.abs(getSizeY())), Math.abs(getSizeZ()))/2;
           theNr = Math.max((int) Math.round(0.49+maxRadius/res.getMaxLength()), 1);
           theNu = Math.max((int) Math.round(0.49+Math.abs(angleu2-angleu1)*TO_RADIANS*maxRadius/res.getMaxLength()), 1);
           theNv = Math.max((int) Math.round(0.49+Math.abs(anglev2-anglev1)*TO_RADIANS*maxRadius/res.getMaxLength()), 1);
           break;
      }
    }
    if((nr!=theNr)||(nu!=theNu)||(nv!=theNv)||changeNTiles) { // Reallocate arrays
      nr = theNr;
      nu = theNu;
      nv = theNv;
      standardSphere = EllipsoidUtils.createStandardEllipsoid(nr, nu, nv, angleu1, angleu2, anglev1, anglev2, closedTop, closedBottom, closedLeft, closedRight);
      setCorners(new double[standardSphere.length][4][3]);
      changeNTiles = false;
    }
    for(int i = 0; i<numberOfTiles; i++) {
      for(int j = 0, sides = corners[i].length; j<sides; j++) {
        System.arraycopy(standardSphere[i][j], 0, corners[i][j], 0, 3);
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

  static private class Loader extends org.opensourcephysics.display3d.core.ElementEllipsoid.Loader {
    public Object createObject(XMLControl control) {
      return new ElementEllipsoid();
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
