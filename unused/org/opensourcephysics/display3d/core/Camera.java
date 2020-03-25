/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.core;
import org.opensourcephysics.controls.XMLControl;

/**
 * <p>Title: Camera</p>
 * <p>Description: This class provides access to the position of the camera,
 * its focus point and its distance to the projection screen that are used
 * to view the 3D scenes. The camera can also be rotated around the line
 * of sight (i.e. the line which conects the camera with the focus point).</p>
 * <p>The camera position can be set using either the desired X,Y,Z coordinates
 * or spherical coordinates around the focus point. This makes it
 * easy to rotate the scene both horizontally and vertically (around the focus).</p>
 * <p>Panning can be achieved by moving the focus point to one side.</p>
 * <p>Zooming is done increasing (positive zoom) or decreasing the distance
 * between the camera and the projection screen.</p>
 * <p> The projection screen is always normal to the line of sight and has
 * its origin at the intersection of this line with the screen itself.</p>
 * <p>The camera provides fives different modes of projecting points in space
 * to the screen. Two modes are truly three-dimensional. The other three are
 * planar modes.
 *
 * @author Francisco Esquembre
 * @version June 2005
 * @see #setProjectionMode(int)
 */
public interface Camera {
  static public final int MODE_PLANAR_XY = 0;
  static public final int MODE_PLANAR_XZ = 1;
  static public final int MODE_PLANAR_YZ = 2;
  static public final int MODE_PERSPECTIVE_OFF = 3;
  static public final int MODE_PERSPECTIVE_ON = 4;
  static public final int MODE_NO_PERSPECTIVE = 10;
  static public final int MODE_PERSPECTIVE = 11;

  /**
   * Sets one of the projecting modes. Possible values are:
   * <ul>
   *   <li>MODE_PERSPECTIVE or MODE_PERSPECTIVE_ON: 3D mode in which objects far away look smaller.</li>
   *   <li>MODE_NO_PERSPECTIVE or MODE_PERSPECTIVE_OFF: 3D mode in which distance doesn't affect the size
   *       of the objects</li>
   *   <li>MODE_PLANAR_XY: 2D mode in which only the X and Y coordinates are displayed.</li>
   *   <li>MODE_PLANAR_XZ: 2D mode in which only the X and Z coordinates are displayed.</li>
   *   <li>MODE_PLANAR_YZ: 2D mode in which only the Y and Z coordinates are displayed.</li>
   * </ul>
   * <p>Changing the mode does not reset the camera.
   * @param mode int
   */
  public void setProjectionMode(int mode);

  /**
   * Gets the projecting mode of the camera.
   * @return int
   * #see #setProjectionMode(int)
   */
  public int getProjectionMode();

  /**
   * Resets the camera to the default.
   * The camera is placed along the X direction, at a reasonable distance
   * from the center of the panel, which becomes the focus, and is not rotated.
   * The screen is also placed at a reasonable distance so that to view the
   * whole scene.
   */
  public void reset();

  /**
   * Sets the position of the camera.
   * @param x double
   * @param y double
   * @param z double
   */
  public void setXYZ(double x, double y, double z);

  /**
   * Sets the position of the camera.
   * @param point double[]
   */
  public void setXYZ(double[] point);

  /**
   * Returns the camera X coordinate
   * @return double the X coordinate of the camera position
   */
  public double getX();

  /**
   * Returns the camera Y coordinate
   * @return double the Y coordinate of the camera position
   */
  public double getY();

  /**
   * Returns the camera Z coordinate
   * @return double the Z coordinate of the camera position
   */
  public double getZ();

  /**
   * Sets the focus point of the camera. That it, the point in space
   * at which the camera is pointing.
   * @param x double
   * @param y double
   * @param z double
   */
  public void setFocusXYZ(double x, double y, double z);

  /**
   * Sets the focus of the camera.
   * @param point double[]
   */
  public void setFocusXYZ(double[] point);

  /**
   * Returns the focus X coordinate
   * @return double the X coordinate of the focus position
   */
  public double getFocusX();

  /**
   * Returns the focus Y coordinate
   * @return double the Y coordinate of the focus position
   */
  public double getFocusY();

  /**
   * Returns the focus Z coordinate
   * @return double the Z coordinate of the focus position
   */
  public double getFocusZ();

  /**
   * Sets the angle that the camera is rotated along the line of sight.
   * Default is 0.
   * @param angle double The angle in radians
   */
  public void setRotation(double angle);

  /**
   * Returns the angle that the camera is rotated along the line of sight.
   * @return double
   */
  public double getRotation();

  /**
   * Sets the distance from the camera to the projecting screen.
   * @param distance double
   */
  public void setDistanceToScreen(double distance);

  /**
   * Returns the distance from the camera to the projecting screen.
   * @return double
   */
  public double getDistanceToScreen();

  /**
   * Set the azimuthal (horizontal) angle of the camera position in spherical
   * coordinates with respect to the focus point. A value of 0 places the
   * camera in the XZ plane.
   * @param angle the desired angle in radians
   */
  public void setAzimuth(double angle);

  /**
   * Get the horizontal angle of the camera position in spherical coordinates
   * with respect to the focus point. A value of 0 means the camera is in the
   * XZ plane.
   * @return double
   */
  public double getAzimuth();

  /**
   * Set the elevation (vertical) angle of the camera position in spherical
   * coordinates with respect to the focus point. A value of 0 places the
   * camera is in the XY plane.
   * @param angle the desired angle in radians in the range [-Math.PI/2,Math.PI/2]
   */
  public void setAltitude(double angle);

  /**
   * Get the elevation (vertical) angle of the camera position in spherical
   * coordinates with respect to the focus point. A value of 0 means the
   * camera is in the XY plane.
   * @return double
   */
  public double getAltitude();

  /**
   * Set the angles of the camera position in spherical coordinates
   * with respect to the focus point.
   * @param azimuth the desired azimuthal angle in radians
   * @param altitude the desired altitude angle in radians in the range [-Math.PI/2,Math.PI/2]
   */
  public void setAzimuthAndAltitude(double azimuth, double altitude);

  /**
   * Returns the transfomation used to project (x,y,z) points in space
   * to points of the form (a,b,distance). (a,b) are the coordinates
   * of the projected point in the screen coordinate system. distance
   * is a measure of how far the point is from the camera. Typically,
   * points in the plane parallel to the screen at the focus point
   * are at distance=1.
   * @return double
   */
  public org.opensourcephysics.numerics.Transformation getTransformation();

  /**
   * Copies its configuration from another camera
   * @param camera
   */
  public void copyFrom(Camera camera);

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------
  abstract static class Loader implements org.opensourcephysics.controls.XML.ObjectLoader {
    abstract public Object createObject(XMLControl control);

    public void saveObject(XMLControl control, Object obj) {
      Camera camera = (Camera) obj;
      control.setValue("projection mode", camera.getProjectionMode());      //$NON-NLS-1$
      control.setValue("x", camera.getX());                                 //$NON-NLS-1$
      control.setValue("y", camera.getY());                                 //$NON-NLS-1$
      control.setValue("z", camera.getZ());                                 //$NON-NLS-1$
      control.setValue("focus x", camera.getFocusX());                      //$NON-NLS-1$
      control.setValue("focus y", camera.getFocusY());                      //$NON-NLS-1$
      control.setValue("focus z", camera.getFocusZ());                      //$NON-NLS-1$
      control.setValue("rotation", camera.getRotation());                   //$NON-NLS-1$
      control.setValue("distance to screen", camera.getDistanceToScreen()); //$NON-NLS-1$
    }

    public Object loadObject(XMLControl control, Object obj) {
      Camera camera = (Camera) obj;
      camera.setProjectionMode(control.getInt("projection mode")); //$NON-NLS-1$
      double x = control.getDouble("x"); //$NON-NLS-1$
      double y = control.getDouble("y"); //$NON-NLS-1$
      double z = control.getDouble("z"); //$NON-NLS-1$
      camera.setXYZ(x, y, z);
      x = control.getDouble("focus x"); //$NON-NLS-1$
      y = control.getDouble("focus y"); //$NON-NLS-1$
      z = control.getDouble("focus z"); //$NON-NLS-1$
      camera.setFocusXYZ(x, y, z);
      camera.setRotation(control.getDouble("rotation"));                   //$NON-NLS-1$
      camera.setDistanceToScreen(control.getDouble("distance to screen")); //$NON-NLS-1$
      return camera;
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
