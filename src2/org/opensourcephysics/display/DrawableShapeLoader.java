/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;

/**
 * A class to save and load DrawableShapes in an XMLControl.
 */
public class DrawableShapeLoader extends XMLLoader {
  /**
   * Saves a DrawableShape by saving the general path.
   * @param control XMLControl
   * @param obj Object
   */
  public void saveObject(XMLControl control, Object obj) {
    DrawableShape drawableShape = (DrawableShape) obj;
    control.setValue("geometry", drawableShape.shapeClass);  //$NON-NLS-1$
    control.setValue("x", drawableShape.x);                  //$NON-NLS-1$
    control.setValue("y", drawableShape.y);                  //$NON-NLS-1$
    control.setValue("theta", drawableShape.theta);          //$NON-NLS-1$
    control.setValue("fill color", drawableShape.color);     //$NON-NLS-1$
    control.setValue("edge color", drawableShape.edgeColor); //$NON-NLS-1$
    Shape shape = AffineTransform.getRotateInstance(-drawableShape.theta, drawableShape.x, drawableShape.y).createTransformedShape(drawableShape.shape);
    control.setValue("general path", shape); //$NON-NLS-1$
  }

  /**
   * Creates the DrawableShape containing a GeneralPath.
   * @param control XMLControl
   * @return Object
   */
  public Object createObject(XMLControl control) {
    return new DrawableShape(new GeneralPath(), 0, 0); // default shape is a path
  }

  /**
   * Loads the DrawableShape using the path stored in the control.
   *
   * @param control XMLControl
   * @param obj Object
   * @return Object
   */
  public Object loadObject(XMLControl control, Object obj) {
    DrawableShape drawableShape = (DrawableShape) obj;
    String geometry = control.getString("geometry"); //$NON-NLS-1$
    double x = control.getDouble("x");               //$NON-NLS-1$
    double y = control.getDouble("y");               //$NON-NLS-1$
    double theta = control.getDouble("theta");       //$NON-NLS-1$
    drawableShape.shape = (Shape) control.getObject("general path");   //$NON-NLS-1$
    drawableShape.shapeClass = geometry;
    drawableShape.x = x;
    drawableShape.y = y;
    drawableShape.color = (Color) control.getObject("fill color");     //$NON-NLS-1$
    drawableShape.edgeColor = (Color) control.getObject("edge color"); //$NON-NLS-1$
    drawableShape.setTheta(theta);
    return obj;
  }

  static { // needs this loader
    XML.setLoader(java.awt.geom.GeneralPath.class, new GeneralPathLoader());
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
