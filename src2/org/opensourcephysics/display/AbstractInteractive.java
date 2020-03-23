/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Color;
import java.awt.Graphics;

/**
 * AbstractInteractive implements common Interactive methods.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public abstract class AbstractInteractive implements Interactive {
  public Color color = new Color(255, 128, 128, 128); // transparent light red fill color
  protected double x = 0;
  protected double y = 0;
  boolean enabled = true;

  /**
   * Draws the object.
   *
   * @param panel the drawing panel
   * @param g  the graphics context
   */
  public abstract void draw(DrawingPanel panel, Graphics g);

  /**
   * Checks to see if this object is enabled and if the pixel coordinates are inside the drawable.
   *
   * @param panel
   * @param xpix
   * @param ypix
   * @return true if the pixel coordinates are inside; false otherwise
   */
  public abstract boolean isInside(DrawingPanel panel, int xpix, int ypix);

  /**
   * Sets the enabled flag.
   * @param _enabled
   */
  public void setEnabled(boolean _enabled) {
    enabled = _enabled;
  }

  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Sets the x and y coordinates.
   * @param _x
   * @param _y
   */
  public void setXY(double _x, double _y) {
    x = _x;
    y = _y;
  }

  /**
   * Finds the interactive object that will respond to mouse actions.
   *
   * @param panel DrawingPanel
   * @param xpix int
   * @param ypix int
   * @return Interactive
   */
  public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
    if(isInside(panel, xpix, ypix)&&enabled) {
      return this;
    }
    return null;
  }

  public boolean isMeasured() {
    return false;
  }

  public double getXMin() {
    return x;
  }

  public double getXMax() {
    return x;
  }

  public double getYMin() {
    return y;
  }

  public double getYMax() {
    return y;
  }

  /**
   * Gets the x coordinate.
   *
   * @return double x
   */
  public double getX() {
    return x;
  }

  /**
   * Sets the x coordinate.
   *
   * @param _x
   */
  public void setX(double _x) {
    x = _x;
  }

  /**
   * Gets the y coordinate.
   *
   * @return double y
   */
  public double getY() {
    return y;
  }

  /**
   * Sets the y coordinate.
   *
   * @param _y
   */
  public void setY(double _y) {
    y = _y;
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
