/*
* Open Source Physics software is free software as described near the bottom of this code file.
*
* For additional information and documentation on Open Source Physics please see:
* <http://www.opensourcephysics.org/>
*/

package org.opensourcephysics.display3d.simple3d;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display3d.simple3d.utils.TetrahedronUtils;

/**
* <p>Title: ElementTetrahedron</p>
* <p>Description: A Tetrahedron using the painter's algorithm</p>
* @author Carlos Jara Bravo and Francisco Esquembre
* @version December 2008
*/
public class ElementTetrahedron extends AbstractTile implements org.opensourcephysics.display3d.core.ElementTetrahedron {
  //Configuration variables
  private boolean closedBottom = true, closedTop = true;
  private double truncationHeight = Double.NaN;
  // Implementation variables
  private boolean changeNTiles = true;
  private double[][][] standardTetra = null;

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

  protected void computeCorners() {
    if(changeNTiles) { // Reallocate arrays
      changeNTiles = false;
      double height = truncationHeight/getSizeZ();
      if(!Double.isNaN(height)) {
        height = Math.min(height, 1.0);
      }
      standardTetra = TetrahedronUtils.createStandardTetrahedron(closedTop, closedBottom, height);
      setCorners(new double[standardTetra.length][4][3]);
    }
    for(int i = 0; i<numberOfTiles; i++) {
      for(int j = 0, sides = corners[i].length; j<sides; j++) {
        System.arraycopy(standardTetra[i][j], 0, corners[i][j], 0, 3);
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

  static private class Loader extends org.opensourcephysics.display3d.core.ElementTetrahedron.Loader {
    public Object createObject(XMLControl control) {
      return new ElementTetrahedron();
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
