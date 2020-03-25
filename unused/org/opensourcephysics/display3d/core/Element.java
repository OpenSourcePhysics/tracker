/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.core;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.numerics.Transformation;

/**
 * <p>Title: Element</p>
 * <p>Description: A basic individual, interactive 3D element.</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
public interface Element extends org.opensourcephysics.display3d.core.interaction.InteractionSource {
  /**
   * The id for the target that allows to reposition the element.
   */
  static final int TARGET_POSITION = 0;

  /**
   * The id for the target that allows to resize the element.
   */
  static final int TARGET_SIZE = 1;

  /**
   * Returns the DrawingPanel3D in which it (or its final ancestor group)
   * is displayed.
   * @return DrawingPanel3D
   */
  public DrawingPanel3D getDrawingPanel3D();

  // ----------------------------------------
  // Name of the element
  // ----------------------------------------

  /**
   * Gives a name to the element.
   * Naming an element is optional, but the element may use its name
   * to identify itself in XML files, for instance.
   * @param name String
   */
  public void setName(String name);

  /**
   * Gets the name of the element
   * @return String the name
   */
  public String getName();

  // ----------------------------------------
  // Position of the element
  // ----------------------------------------

  /**
   * Set the X coordinate of the element
   * @param x double
   */
  public void setX(double x);

  /**
   * Get the X coordinate of the element
   * @return double
   */
  public double getX();

  /**
   * Set the Y coordinate of the element
   * @param y double
   */
  public void setY(double y);

  /**
   * Get the Y coordinate of the element
   * @return double
   */
  public double getY();

  /**
   * Set the Z coordinate of the element
   * @param z double
   */
  public void setZ(double z);

  /**
   * Get the Z coordinate of the element
   * @return double
   */
  public double getZ();

  /**
   * Set the X, Y, and Z coordinates of the element
   * @param x double
   * @param y double
   * @param z double
   */
  public void setXYZ(double x, double y, double z);

  /**
   * Sets the coordinates of the element.
   * If pos.length<=2 it sets only X and Y.
   * If pos.length>2 it sets X, Y, and Z.
   * @param pos double[]
   */
  public void setXYZ(double[] pos);

  // ----------------------------------------
  // Size of the element
  // ----------------------------------------

  /**
   * Set the size along the X axis
   * @param sizeX double
   */
  public void setSizeX(double sizeX);

  /**
   * Get the size along the X axis
   * @return double
   */
  public double getSizeX();

  /**
   * Set the size along the Y axis
   * @param sizeY double
   */
  public void setSizeY(double sizeY);

  /**
   * Get the size along the Y axis
   * @return double
   */
  public double getSizeY();

  /**
   * Set the size along the Z axis
   * @param sizeZ double
   */
  public void setSizeZ(double sizeZ);

  /**
   * Get the size along the Z axis
   * @return double
   */
  public double getSizeZ();

  /**
   * Set the size along the X, Y and Z axes
   * @param sizeX double
   * @param sizeY double
   * @param sizeZ double
   */
  public void setSizeXYZ(double sizeX, double sizeY, double sizeZ);

  /**
   * Sets the size of the element.
   * If size.length<=2 it sets only the size in X and Y.
   * If size.length>3 it sets the size in X, Y, and Z.
   * @param size double[]
   */
  public void setSizeXYZ(double[] size);

  // -------------------------------------
  // Visibility and style
  // -------------------------------------

  /**
   * Sets the visibility of the element
   * @param _visible boolean
   */
  public void setVisible(boolean _visible);

  /**
   * Whether the element is visible
   * @return boolean
   */
  public boolean isVisible();

  /**
   * Gets the style of the element
   * @return Style
   * @see Style
   */
  public Style getStyle();

  // ----------------------------------------
  // Transformation of the element
  // ----------------------------------------

  /**
   * Sets the internal transformation of the element, that is, the
   * transformation that converts the standard XYZ axes to the body's
   * internal reference axes.
   * The transformation is copied and cannot be accessed by users
   * directy. This implies that changing the original transformation
   * has no effect on the element unless a new setTransformation() is invoked.
   * The transformation uses the body's position as its origin.
   * @param transformation the new transformation
   * @see org.opensourcephysics.numerics.Transformation
   */
  public void setTransformation(org.opensourcephysics.numerics.Transformation transformation);

  /**
   * Returns a clone of the element transformation
   * @return Transformation a clone of the element's transformation
   */
  public Transformation getTransformation();

  /**
   * This method transforms a double[3] vector from the body's frame to
   * the space's frame.
   * @param vector double[] The original coordinates in the body frame
   * @return double[] The same array once transformed
   */
  public double[] toSpaceFrame(double[] vector);

  /**
   * This method converts a double[3] vector from the space's frame to
   * the body's frame. </p>
   * This only works properly if the internal transformation is not set
   * (i.e. it is the identity) or if it is invertible.
   * Otherwise, a call to this method will throw an
   * UnsupportedOperationException exception.
   * @param vector double[] The original coordinates in the space
   * @return double[] The same array with the body coordinates
   */
  public double[] toBodyFrame(double[] vector) throws UnsupportedOperationException;

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------

  /**
   * Loads unmutable objects of the Element, such as the style,
   * as well as perform any extra implementation-specific initialization.
   * For the internal use of the XML loeader. Not to be used by final users.
   * @param control XMLControl
   */
  public void loadUnmutableObjects(XMLControl control);

  /**
   * A class to save and load Element data.
   */
  static abstract class Loader implements XML.ObjectLoader {
    public abstract Object createObject(XMLControl control);

    public void saveObject(XMLControl control, Object obj) {
      Element element = (Element) obj;
      if(element.getName().length()>0) {
        control.setValue("name", element.getName()); //$NON-NLS-1$
      }
      control.setValue("x", element.getX());                           //$NON-NLS-1$
      control.setValue("y", element.getY());                           //$NON-NLS-1$
      control.setValue("z", element.getZ());                           //$NON-NLS-1$
      control.setValue("x size", element.getSizeX());                  //$NON-NLS-1$
      control.setValue("y size", element.getSizeY());                  //$NON-NLS-1$
      control.setValue("z size", element.getSizeZ());                  //$NON-NLS-1$
      control.setValue("visible", element.isVisible());                //$NON-NLS-1$
      control.setValue("style", element.getStyle());                   //$NON-NLS-1$
      control.setValue("transformation", element.getTransformation()); //$NON-NLS-1$
    }

    public Object loadObject(XMLControl control, Object obj) {
      Element element = (Element) obj;
      String name = control.getString("name"); //$NON-NLS-1$
      if(name!=null) {
        element.setName(name);
      }
      element.setXYZ(control.getDouble("x"), control.getDouble("y"), control.getDouble("z")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      element.setSizeXYZ(control.getDouble("x size"), control.getDouble("y size"), control.getDouble("z size")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      element.setVisible(control.getBoolean("visible"));                               //$NON-NLS-1$
      element.setTransformation((Transformation) control.getObject("transformation")); //$NON-NLS-1$
      // Subclasses are responsible of loading unmutable elements, such as the style
      // this is done by the following method:
      element.loadUnmutableObjects(control);
      return obj;
    }

  } // End of static class Loader

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
