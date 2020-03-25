/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

/**
 * A measured circle that implements Interactive.
 *
 * The InteractiveCircle class also demonstrates how to implement a simple XML loader.
 *
 * @author Wolfgang Christian
 * @author Francisco Equembre
 * @version 1.0
 */
public class InteractiveCircle extends MeasuredCircle implements Interactive {
  boolean enableInteraction = true;

  /**
   * Constructs an InteractiveCircle with the given parameters.
   *
   * @param x
   * @param y
   */
  public InteractiveCircle(double x, double y) {
    super(x, y);
  }

  /**
   * Constructs an InteractiveCircle at the origin.
   */
  public InteractiveCircle() {
    this(0, 0);
  }

  /**
   * Enables mouse interactions.
   * @param _enableInteraction
   */
  public void setEnabled(boolean _enableInteraction) {
    enableInteraction = _enableInteraction;
  }

  public boolean isEnabled() {
    return enableInteraction;
  }

  public boolean isInside(DrawingPanel panel, int xpix, int ypix) {
    if(findInteractive(panel, xpix, ypix)==null) {
      return false;
    }
    return true;
  }

  public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
    if(!enableInteraction) {
      return null;
    }
    int xcpix = panel.xToPix(x); // convert x to pixel units
    int ycpix = panel.yToPix(y); // convert y to pixel units
    if((Math.abs(xcpix-xpix)<=pixRadius)&&(Math.abs(ycpix-ypix)<=pixRadius)) {
      return this;
    }
    return null;
  }

  public static XML.ObjectLoader getLoader() {
    return new InteractiveCircleLoader();
  }

  /**
   * A class to save and load InteractiveCircle data in an XMLControl.
   */
  protected static class InteractiveCircleLoader extends CircleLoader {
    /**
     * Saves the InteractiveCircle's data in the xml control.
     * @param control XMLControl
     * @param obj Object
     */
    public void saveObject(XMLControl control, Object obj) {
      super.saveObject(control, obj);
      InteractiveCircle circle = (InteractiveCircle) obj;
      control.setValue("interaction enabled", circle.enableInteraction); //$NON-NLS-1$
      control.setValue("measure enabled", circle.enableInteraction);     //$NON-NLS-1$
    }

    /**
     * Creates a default InteractiveCircle.
     * @param control XMLControl
     * @return Object
     */
    public Object createObject(XMLControl control) {
      return new InteractiveCircle();
    }

    /**
     * Loads data from the xml control into the InteractiveCircle object.
     * @param control XMLControl
     * @param obj Object
     * @return Object
     */
    public Object loadObject(XMLControl control, Object obj) {
      super.loadObject(control, obj);
      InteractiveCircle circle = (InteractiveCircle) obj;
      circle.enableInteraction = control.getBoolean("interaction enabled"); //$NON-NLS-1$
      circle.enableMeasure = control.getBoolean("measure enabled");         //$NON-NLS-1$
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
