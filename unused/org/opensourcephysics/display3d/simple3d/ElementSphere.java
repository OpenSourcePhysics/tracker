/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.simple3d;

/**
 * <p>Title: ElementSphere</p>
 * <p>Description: An Ellipsoid with the same size in all dimensions.</p>
 * <p>Changing the size in any dimension also sets the other sizes.
 * Setting different sizes to X, Y, and Z will result in setting the radius to
 * the largest of the three dimensions.</p>
 * @author Francisco Esquembre
 * @version May 2005
 */
public class ElementSphere extends ElementEllipsoid implements org.opensourcephysics.display3d.core.ElementSphere {
  /**
   * Constructor ElementSphere
   */
  public ElementSphere() {
    super();
  }

  /**
   * Constructor ElementSphere
   * @param radius
   */
  public ElementSphere(double radius) {
    super();
    setRadius(radius);
  }

  public void setRadius(double radius) {
    radius *= 2;
    super.setSizeXYZ(radius, radius, radius);
  }

  public double getRadius() {
    return this.getSizeX()/2;
  }

  // -------------------------------------
  // Overwrite parent methods
  // -------------------------------------
  public void setSizeX(double sizeX) {
    super.setSizeXYZ(sizeX, sizeX, sizeX);
  }

  public void setSizeY(double sizeY) {
    super.setSizeXYZ(sizeY, sizeY, sizeY);
  }

  public void setSizeZ(double sizeZ) {
    super.setSizeXYZ(sizeZ, sizeZ, sizeZ);
  }

  public void setSizeXYZ(double sizeX, double sizeY, double sizeZ) {
    double max = Math.max(Math.max(sizeX, sizeY), sizeZ);
    super.setSizeXYZ(max, max, max);
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
