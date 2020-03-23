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
public class ElementObject extends ElementBox implements org.opensourcephysics.display3d.core.ElementObject {
  private String file = null;
  private double angle = 0.0;

  public void setObjectFile(String file) {
    this.file = file;
  }

  public String getObjectFile() {
    return this.file;
  }

  public void setRotationAngle(double angle) {
    this.angle = angle;
  }

  public double getRotationAngle() {
    return this.angle;
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

  static private class Loader extends org.opensourcephysics.display3d.core.ElementObject.Loader {
    public Object createObject(XMLControl control) {
      return new ElementObject();
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
