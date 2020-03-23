/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.core;
import org.opensourcephysics.controls.XMLControl;

/**
 * <p>Title: ElementEllipsoid</p>
 * <p>Description: A 3D Ellipsoid.</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
public interface ElementEllipsoid extends Element {
  /**
   * Sets the minimum angle for the meridians.
   * Default is 0.
   * @param angle the minimum angle (in degrees)
   */
  public void setMinimumAngleU(int angle);

  /**
   * Gets the minimum angle for the meridians
   * @return the minimum angle (in degrees)
   */
  public int getMinimumAngleU();

  /**
   * Sets the maximum angle for the meridians.
   * Default is 360.
   * @param angle the maximum angle (in degrees)
   */
  public void setMaximumAngleU(int angle);

  /**
   * Gets the maximum angle for the meridians
   * @return the maximum angle (in degrees)
   */
  public int getMaximumAngleU();

  /**
   * Sets the minimum angle for the parallels
   * Default is -90.
   * @param angle the minimum angle (in degrees)
   */
  public void setMinimumAngleV(int angle);

  /**
   * Gets the minimum angle for the parallels
   * @return the minimum angle (in degrees)
   */
  public int getMinimumAngleV();

  /**
   * Sets the maximum angle for the parallels
   * Default is 90.
   * @param angle the maximum angle (in degrees)
   */
  public void setMaximumAngleV(int angle);

  /**
   * Gets the maximum angle for the parallels
   * @return the maximum angle (in degrees)
   */
  public int getMaximumAngleV();

  /**
   * Whether an incomplete ellipsoid should be closed at its bottom.
   * @param closed the desired value
   */
  public void setClosedBottom(boolean close);

  /**
   * Whether the ellipsoid is closed at its bottom.
   * @return the value
   */
  public boolean isClosedBottom();

  /**
   * Whether an incomplete ellipsoid should be closed at its top.
   * @param closed the desired value
   */
  public void setClosedTop(boolean close);

  /**
   * Whether the ellipsoid is closed at its top.
   * @return the value
   */
  public boolean isClosedTop();

  /**
   * Whether an incomplete ellipsoid should be closed at its left side.
   * @param closed the desired value
   */
  public void setClosedLeft(boolean close);

  /**
   * Whether the ellipsoid is closed at its left side.
   * @return the value
   */
  public boolean isClosedLeft();

  /**
   * Whether an incomplete ellipsoid should be closed at its right side.
   * @param closed the desired value
   */
  public void setClosedRight(boolean close);

  /**
   * Whether the ellipsoid is closed at its right side.
   * @return the value
   */
  public boolean isClosedRight();

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------
  static abstract class Loader extends Element.Loader {
    public void saveObject(XMLControl control, Object obj) {
      super.saveObject(control, obj);
      ElementEllipsoid element = (ElementEllipsoid) obj;
      control.setValue("closed top", element.isClosedTop());           //$NON-NLS-1$
      control.setValue("closed bottom", element.isClosedBottom());     //$NON-NLS-1$
      control.setValue("closed left", element.isClosedLeft());         //$NON-NLS-1$
      control.setValue("closed right", element.isClosedRight());       //$NON-NLS-1$
      control.setValue("minimum u angle", element.getMinimumAngleU()); //$NON-NLS-1$
      control.setValue("maximum u angle", element.getMaximumAngleU()); //$NON-NLS-1$
      control.setValue("minimum v angle", element.getMinimumAngleV()); //$NON-NLS-1$
      control.setValue("maximum v angle", element.getMaximumAngleV()); //$NON-NLS-1$
    }

    public Object loadObject(XMLControl control, Object obj) {
      super.loadObject(control, obj);
      ElementEllipsoid element = (ElementEllipsoid) obj;
      element.setClosedTop(control.getBoolean("closed top"));       //$NON-NLS-1$
      element.setClosedBottom(control.getBoolean("closed bottom")); //$NON-NLS-1$
      element.setClosedLeft(control.getBoolean("closed left"));     //$NON-NLS-1$
      element.setClosedRight(control.getBoolean("closed right"));   //$NON-NLS-1$
      element.setMinimumAngleU(control.getInt("minimum u angle"));  //$NON-NLS-1$
      element.setMaximumAngleU(control.getInt("maximum u angle"));  //$NON-NLS-1$
      element.setMinimumAngleV(control.getInt("minimum v angle"));  //$NON-NLS-1$
      element.setMaximumAngleV(control.getInt("maximum v angle"));  //$NON-NLS-1$
      return obj;
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
