/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.core;
import java.util.Collection;
import java.util.Iterator;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.tools.VideoTool;

/**
 * <p>Title: DrawingPanel3D</p>
 * <p>Description: DrawingPanel3D is the basic 3D drawing panel</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
public interface DrawingPanel3D extends org.opensourcephysics.display3d.core.interaction.InteractionSource {
  /** The panel itself as the only target of the panel */
  static public final int TARGET_PANEL = 0;

  /** Message box location */
  public static final int BOTTOM_LEFT = 0;

  /** Message box location */
  public static final int BOTTOM_RIGHT = 1;

  /** Message box location */
  public static final int TOP_RIGHT = 2;

  /** Message box location */
  public static final int TOP_LEFT = 3;
  //CJB

  /** Axis Modes **/
  public static final int MODE_XYZ = 0;
  public static final int MODE_YXZ = 1;
  public static final int MODE_XZY = 2;
  public static final int MODE_YZX = 3;
  public static final int MODE_ZYX = 4;
  public static final int MODE_ZXY = 5;
  //CJB

  //Static int variables for lightning
  public static final int BACKGROUND = 0;
  public static final int AMBIENT_LIGHT = 1;
  public static final int DIRECTIONAL_LIGHT = 2;
  public static final int POINT_LIGHT = 3;
  public static final int SPOT_LIGHT = 4;

  /**
   * Getting the pointer to the real JPanel in it
   * @return JFrame
   */
  public java.awt.Component getComponent();

  /**
   * Sets the background image
   * @param imageFile
   */
  public void setBackgroundImage(String imageFile);

  /**
   * Returns the background image
   */
  public String getBackgroundImage();

  // ---------------------------------
  // Customization of the panel
  // ---------------------------------

  /**
   * Sets the preferred extrema for the panel. This resets the camera
   * of the panel to its default.
   * @param minX double
   * @param maxX double
   * @param minY double
   * @param maxY double
   * @param minZ double
   * @param maxZ double
   * @see Camera
   */
  public void setPreferredMinMax(double minX, double maxX, double minY, double maxY, double minZ, double maxZ);

  /**
   * Gets the preferred minimum in the X coordinate
   * @return double
   */
  public double getPreferredMinX();

  /**
   * Gets the preferred maximum in the X coordinate
   * @return double
   */
  public double getPreferredMaxX();

  /**
   * Gets the preferred minimum in the Y coordinate
   * @return double
   */
  public double getPreferredMinY();

  /**
   * Gets the preferred maximum in the Y coordinate
   * @return double
   */
  public double getPreferredMaxY();

  /**
   * Gets the preferred minimum in the Z coordinate
   * @return double
   */
  public double getPreferredMinZ();

  /**
   * Gets the preferred maximum in the Z coordinate
   * @return double
   */
  public double getPreferredMaxZ();

  //CJB

  /**
   * Sets the scale factor of the scene in X,Y,Z axis.
   * @param factorX double
   * @param factorY double
   * @param factorZ double
   */
  public void setScaleFactor(double factorX, double factorY, double factorZ);

  /**
   * Gets the scale factor in the X axis
   * @return double
   */
  public double getScaleFactorX();

  /**
   * Gets the scale factor in the Y axis
   * @return double
   */
  public double getScaleFactorY();

  /**
   * Gets the scale factor in the Z axis
   * @return double
   */
  public double getScaleFactorZ();

  /**
   * Sets the axes mode
   * @param mode int
   */
  public void setAxesMode(int mode);

  /**
   * Returns the axes mode
   */
  public int getAxesMode();

  /**
   * Sets the preferred min and max in each dimension so that all
   * elements currently in the panel are visible.
   */
  public void zoomToFit();

  /**
   * Whether the panel should try to keep a square aspect.
   * Default value is true.
   * @param square boolean
   */
  public void setSquareAspect(boolean square);

  /**
   * Whether the panel tries to keep a square aspect.
   * @return boolean
   */
  public boolean isSquareAspect();

  /**
   * Provides the list of visualization hints that the panel uses
   * to display the 3D scene
   * @return VisualizationHints
   * @see VisualizationHints
   */
  public VisualizationHints getVisualizationHints();

  /**
   * Provides the Camera object used to project the scene in 3D modes.
   * @return Camera
   * @see Camera
   */
  public Camera getCamera();

  /**
   * Gets the video capture tool. May be null.
   *
   * @return the video capture tool
   */
  public VideoTool getVideoTool();

  /**
   * Sets the video capture tool. May be set to null.
   *
   * @param videoCap the video capture tool
   */
  public void setVideoTool(VideoTool videoTool);

  /**
   * Paints the panel immediately from within the calling thread.
   * @return BufferedImage the generated image
   */
  public java.awt.image.BufferedImage render();

  /**
   * Paints the scene using the graphic context of the provided image
   * @param image Image
   * @return Image the generated image
   */
  public java.awt.image.BufferedImage render(java.awt.image.BufferedImage image);

  /**
   * Repaints the panel using the event queue.
   */
  public void repaint();

  /**
   * Adds an Element to this DrawingPanel3D.
   * @param element Element
   * @see Element
   */
  public void addElement(Element element);

  /**
   * Removes an Element from this DrawingPanel3D
   * @param element Element
   * @see Element
   */
  public void removeElement(Element element);

  /**
   * Removes all Elements from this DrawingPanel3D
   * @see Element
   */
  public void removeAllElements();

  /**
   * Gets the (cloned) list of Elements.
   * (Should be synchronized.)
   * @return cloned list
   */
  public java.util.List<Element> getElements();

  // ----------------------------------------------------
  // Lights
  // ----------------------------------------------------

  /**
   * Enable disable a light
   * @param _state
   * @param nlight
   */
  public void setLightEnabled(boolean _state, int nlight);

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------
  static abstract class Loader implements org.opensourcephysics.controls.XML.ObjectLoader {
    abstract public Object createObject(XMLControl control);

    public void saveObject(XMLControl control, Object obj) {
      DrawingPanel3D panel = (DrawingPanel3D) obj;
      control.setValue("preferred x min", panel.getPreferredMinX());          //$NON-NLS-1$
      control.setValue("preferred x max", panel.getPreferredMaxX());          //$NON-NLS-1$
      control.setValue("preferred y min", panel.getPreferredMinY());          //$NON-NLS-1$
      control.setValue("preferred y max", panel.getPreferredMaxY());          //$NON-NLS-1$
      control.setValue("preferred z min", panel.getPreferredMinZ());          //$NON-NLS-1$
      control.setValue("preferred z max", panel.getPreferredMaxZ());          //$NON-NLS-1$
      control.setValue("visualization hints", panel.getVisualizationHints()); //$NON-NLS-1$
      control.setValue("camera", panel.getCamera());                          //$NON-NLS-1$
      control.setValue("elements", panel.getElements());                      //$NON-NLS-1$
    }

    public Object loadObject(XMLControl control, Object obj) {
      DrawingPanel3D panel = (DrawingPanel3D) obj;
      double minX = control.getDouble("preferred x min"); //$NON-NLS-1$
      double maxX = control.getDouble("preferred x max"); //$NON-NLS-1$
      double minY = control.getDouble("preferred y min"); //$NON-NLS-1$
      double maxY = control.getDouble("preferred y max"); //$NON-NLS-1$
      double minZ = control.getDouble("preferred z min"); //$NON-NLS-1$
      double maxZ = control.getDouble("preferred z max"); //$NON-NLS-1$
      panel.setPreferredMinMax(minX, maxX, minY, maxY, minZ, maxZ);
      Collection<?> elements = Collection.class.cast(control.getObject("elements")); //$NON-NLS-1$
      if(elements!=null) {
        panel.removeAllElements();
        Iterator<?> it = elements.iterator();
        while(it.hasNext()) {
          panel.addElement((Element) it.next());
        }
      }
      // The subclass is responsible to load unmutable objects such as
      // the visualization hints or the camera
      // It is also responsible to update the screen after loading
      return obj;
    }

  } // End of static class DrawingPanel3DLoader

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
