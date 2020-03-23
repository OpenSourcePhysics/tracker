/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.core;
import org.opensourcephysics.controls.XMLControl;

/**
 * <p>Title: ElementCylinder</p>
 * <p>Description: A 3D Cone. The cone can be incomplete or double,
 * if height!=sizeZ.</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
public interface ElementCone extends Element {
  /**
   * Sets the truncation height for this cone.
   * The standard height of a cone is set using setSizeZ().
   * This method helps create truncated cones by setting the
   * truncation height at a value smaller than the Z size.
   * Negative, zero, or Double.NaN values set the cone to a standard
   * (complete) one. Values greater than the Z size are ignored.
   * @param height double
   */
  public void setTruncationHeight(double height);

  /**
   * Gets the truncation height for this cone.
   * @return double The truncation height (Double.NaN if the cone is complete.)
   * @see #setTruncationHeight()
   */
  public double getTruncationHeight();

  /**
   * Sets the minimum angle to build the bottom and, if neccesary, top faces of the cone.
   * Default is 0.
   * @param angle the minimum angle (in degrees)
   */
  public void setMinimumAngle(int angle);

  /**
   * Gets the minimum angle used to build the bottom and, if neccesary, top faces of the cone.
   * @return the minimum angle (in degrees)
   */
  public int getMinimumAngle();

  /**
   * Sets the maximum angle to build the bottom and, if neccesary, top faces of the cone.
   * Default is 360.
   * @param angle the maximum angle (in degrees)
   */
  public void setMaximumAngle(int angle);

  /**
   * Gets the maximum angle used to build the bottom and, if neccesary, top faces of the cone.
   * @return the maximum angle (in degrees)
   */
  public int getMaximumAngle();

  /**
   * Whether the cone should be closed at its bottom.
   * @param close the desired value
   */
  public void setClosedBottom(boolean close);

  /**
   * Whether the cone is closed at its bottom.
   * @return the value
   */
  public boolean isClosedBottom();

  /**
   * Whether an incomplete cone element should be closed at its top.
   * @param closed the desired value
   */
  public void setClosedTop(boolean close);

  /**
   * Whether the cone is closed at its top.
   * @return the value
   */
  public boolean isClosedTop();

  /**
   * Whether an incomplete element should be closed at its left side.
   * @param closed the desired value
   */
  public void setClosedLeft(boolean close);

  /**
   * Whether the element is closed at its left side.
   * @return the value
   */
  public boolean isClosedLeft();

  /**
   * Whether an incomplete element should be closed at its right side.
   * @param closed the desired value
   */
  public void setClosedRight(boolean close);

  /**
   * Whether the element is closed at its right side.
   * @return the value
   */
  public boolean isClosedRight();

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------
  static abstract class Loader extends Element.Loader {
    public void saveObject(XMLControl control, Object obj) {
      super.saveObject(control, obj);
      ElementCone element = (ElementCone) obj;
      if(Double.isNaN(element.getTruncationHeight())) {
        control.setValue("truncation height", -1.0);                          //$NON-NLS-1$
      } else {
        control.setValue("truncation height", element.getTruncationHeight()); //$NON-NLS-1$
      }
      control.setValue("closed top", element.isClosedTop());        //$NON-NLS-1$
      control.setValue("closed bottom", element.isClosedBottom());  //$NON-NLS-1$
      control.setValue("closed left", element.isClosedLeft());      //$NON-NLS-1$
      control.setValue("closed right", element.isClosedRight());    //$NON-NLS-1$
      control.setValue("minimum angle", element.getMinimumAngle()); //$NON-NLS-1$
      control.setValue("maximum angle", element.getMaximumAngle()); //$NON-NLS-1$
    }

    public Object loadObject(XMLControl control, Object obj) {
      super.loadObject(control, obj);
      ElementCone element = (ElementCone) obj;
      element.setTruncationHeight(control.getDouble("truncation height")); //$NON-NLS-1$
      element.setClosedTop(control.getBoolean("closed top"));              //$NON-NLS-1$
      element.setClosedBottom(control.getBoolean("closed bottom"));        //$NON-NLS-1$
      element.setClosedLeft(control.getBoolean("closed left"));            //$NON-NLS-1$
      element.setClosedRight(control.getBoolean("closed right"));          //$NON-NLS-1$
      element.setMinimumAngle(control.getInt("minimum angle"));            //$NON-NLS-1$
      element.setMaximumAngle(control.getInt("maximum angle"));            //$NON-NLS-1$
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
