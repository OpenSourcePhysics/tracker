/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.simple3d;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display3d.simple3d.utils.ConeUtils;

/**
 * <p>Title: ElementCylinder</p>
 * <p>Description: Painter's algorithm implementation of a Cylinder</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
public class ElementCone extends AbstractTile implements org.opensourcephysics.display3d.core.ElementCone {
  // Configuration variables
  private boolean closedBottom = true, closedTop = true;
  private boolean closedLeft = true, closedRight = true;
  private int minAngle = 0, maxAngle = 360;
  private double truncationHeight = Double.NaN;
  // Implementation variables
  private boolean changeNTiles = true;
  private int nr = -1, nu = -1, nz = -1; // Make sure arrays are allocated
  private double[][][] standardCone = null;
  //Static
  static final protected double TO_RADIANS = Math.PI/180.0;

  { // Initialization block
    getStyle().setResolution(new Resolution(3, 12, 5));
  }

  // -------------------------------------
  // Configuration
  // -------------------------------------
  public void setTruncationHeight(double height) {
    if(height<0) {
      height = Double.NaN;
    }
    this.truncationHeight = height;
    setElementChanged(true);
    changeNTiles = true;
  }

  public double getTruncationHeight() {
    return truncationHeight;
  }

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

  public void setMinimumAngle(int angle) {
    this.minAngle = angle;
    setElementChanged(true);
    changeNTiles = true;
  }

  public int getMinimumAngle() {
    return this.minAngle;
  }

  public void setMaximumAngle(int angle) {
    this.maxAngle = angle;
    setElementChanged(true);
    changeNTiles = true;
  }

  public int getMaximumAngle() {
    return this.maxAngle;
  }

  // -------------------------------------
  // Private or protected methods
  // -------------------------------------
  protected synchronized void computeCorners() {
    int theNr = 1, theNu = 1, theNz = 1;
    double angle1 = minAngle, angle2 = maxAngle;
    if(Math.abs(angle2-angle1)>360) {
      angle2 = angle1+360;
    }
    org.opensourcephysics.display3d.core.Resolution res = getRealStyle().getResolution();
    if(res!=null) {
      switch(res.getType()) {
         case org.opensourcephysics.display3d.core.Resolution.DIVISIONS :
           theNr = Math.max(res.getN1(), 1);
           theNu = Math.max(res.getN2(), 1);
           theNz = Math.max(res.getN3(), 1);
           break;
         case org.opensourcephysics.display3d.core.Resolution.MAX_LENGTH :
           double dx = Math.abs(getSizeX())/2, dy = Math.abs(getSizeY())/2, dz = Math.abs(getSizeZ());
           if(!Double.isNaN(truncationHeight)) {
             dz = Math.min(dz, truncationHeight);
           }
           theNr = Math.max((int) Math.round(0.49+Math.max(dx, dy)/res.getMaxLength()), 1);
           theNu = Math.max((int) Math.round(0.49+Math.abs(angle2-angle1)*TO_RADIANS*(dx+dy)/res.getMaxLength()), 1);
           theNz = Math.max((int) Math.round(0.49+dz/res.getMaxLength()), 1);
           break;
      }
    }
    if((nr!=theNr)||(nu!=theNu)||(nz!=theNz)||changeNTiles) { // Reallocate arrays
      nr = theNr;
      nu = theNu;
      nz = theNz;
      changeNTiles = false;
      double height = truncationHeight/getSizeZ();
      if(!Double.isNaN(height)) {
        height = Math.min(height, 1.0);
      }
      standardCone = ConeUtils.createStandardCone(nr, nu, nz, angle1, angle2, closedTop, closedBottom, closedLeft, closedRight, height);
      setCorners(new double[standardCone.length][4][3]);
    }
    for(int i = 0; i<numberOfTiles; i++) {
      for(int j = 0, sides = corners[i].length; j<sides; j++) {
        System.arraycopy(standardCone[i][j], 0, corners[i][j], 0, 3);
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

  static protected class Loader extends org.opensourcephysics.display3d.core.ElementCone.Loader {
    public Object createObject(XMLControl control) {
      return new ElementCone();
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
