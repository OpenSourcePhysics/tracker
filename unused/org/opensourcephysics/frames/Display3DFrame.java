/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.frames;
import org.opensourcephysics.display3d.simple3d.DrawingFrame3D;
import org.opensourcephysics.display3d.simple3d.DrawingPanel3D;
import org.opensourcephysics.display3d.core.Element;
import javax.swing.*;
import org.opensourcephysics.display3d.core.interaction.InteractionListener;
import org.opensourcephysics.display3d.core.Camera;

public class Display3DFrame extends DrawingFrame3D {
  public Display3DFrame(String title) {
    super(title, new DrawingPanel3D());
    setAnimated(true);
  }

  /**
   * Adds in interaction listener to the 3D drawing panel.
   *
   * InteractionListeners respond to mouse events.
   *
   * @param listener InteractionListener
   */
  public void addInteractionListener(InteractionListener listener) {
    drawingPanel.addInteractionListener(listener);
  }

  /**
   * Enables/Disables interaction with the DrawingPanel3D.
   * @param enable boolean
   */
  public void enableInteraction(boolean enable) {
    drawingPanel.getInteractionTarget(0).setEnabled(enable); // enables interactions that change the size
  }

  /**
   * Set the extrema in the X, Y and Z coordinates at once
   *
   * @param xmin double
   * @param xmax double
   * @param ymin double
   * @param ymax double
   * @param zmin double
   * @param zmax double
   */
  public void setPreferredMinMax(double xmin, double xmax, double ymin, double ymax, double zmin, double zmax) {
    drawingPanel.setPreferredMinMax(xmin, xmax, ymin, ymax, zmin, zmax);
  }

  /**
   * Provides the Camera object used to project the scene in 3D modes.
   * @return Camera
   * @see Camera
   */
  public Camera getCamera() {
    return drawingPanel.getCamera();
  }

  /**
   * Adds an Element to this DrawingPanel3D.
   * @param element Element
   * @see Element
   */
  public void addElement(Element element) {
    drawingPanel.addElement(element);
  }

  /**
   * Whether the panel should try to keep a square aspect.
   * Default value is true.
   * @param square boolean
   */
  public void setSquareAspect(boolean square) {
    drawingPanel.setSquareAspect(square);
  }

  /**
   * Whether the scene can be drawn quickly when it is dragged for a
   * new view point.
   *
   * @param allow the desired value
   */
  public void setAllowQuickRedraw(boolean allow) {
    drawingPanel.getVisualizationHints().setAllowQuickRedraw(allow);
  }

  /**
   * Sets whether or not paint messages received from the operating system
   * should be ignored.  This does not affect paint events generated in
   * software by the AWT, unless they are an immediate response to an
   * OS-level paint message.
   */
  public void setIgnoreRepaint(boolean ignoreRepaint) {
    super.setIgnoreRepaint(ignoreRepaint);
    ((JPanel) drawingPanel).setIgnoreRepaint(ignoreRepaint);
    // glassPanel.setIgnoreRepaint(ignoreRepaint);
  }

  /**
   * Types of decoration displayed. One of the following
   * <ul>
   *   <li><b>VisualizationHints.DECORATION_NONE</b>: No decoration</li>
   *   <li><b>VisualizationHints.DECORATION_AXES</b>: Display labelled axes</li>
   *   <li><b>VisualizationHints.DECORATION_CUBE</b>: Display the bounding box</li>
   * </ul>
   * @param value the desired value
   */
  public void setDecorationType(int value) {
    drawingPanel.getVisualizationHints().setDecorationType(value);
  }

  /**
   * Sets the angle theta angle in spherical polar coordinates (in radians) to rotate the camera about the z axis
   * before projecting. Default is 0.0.
   * @param theta the desired angle
   */
  public void setAzimuth(double theta) {
    drawingPanel.getCamera().setAzimuth(theta);
  }

  /**
   * Sets the angle phi in spherical polar coordiantes (in radians) to rotate the camera away from the z axis
   * before projecting. Default is 0.0.
   * @param phi the desired angle
   */
  public void setAltitude(double phi) {
    drawingPanel.getCamera().setAltitude(phi);
  }

  /**
   * Sets the projection mode for the camera.  Possible values are:
  * <ul>
  *   <li>Camera.MODE_PERSPECTIVE or Camera.MODE_PERSPECTIVE_ON : 3D mode in which objects far away look smaller.</li>
  *   <li>Camera.MODE_NO_PERSPECTIVE or Camera.MODE_PERSPECTIVE_OFF: 3D mode in which distance doesn't affect the size
  *       of the objects</li>
  *   <li>Camera.MODE_PLANAR_XY: 2D mode in which only the X and Y coordinates are displayed.</li>
  *   <li>Camera.MODE_PLANAR_XZ: 2D mode in which only the X and Z coordinates are displayed.</li>
  *   <li>Camera.MODE_PLANAR_YZ: 2D mode in which only the Y and Z coordinates are displayed.</li>
  * </ul>

   * @param mode the desired value
   */
  public void setProjectionMode(int mode) {
    drawingPanel.getCamera().setProjectionMode(mode);
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
