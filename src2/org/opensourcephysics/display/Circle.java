/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Color;
import java.awt.Graphics;
import org.opensourcephysics.controls.XML;

/**
 * A Drawable circle that uses awt drawing.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public class Circle implements Drawable {
  public Color color = Color.red; // the drawing color
  public int pixRadius = 6;
  protected double x = 0;
  protected double y = 0;

  /**
   * Constructs a fixed radius circle at the origin.
   */
  public Circle() {
    this(0, 0);
  }

  /**
   * Constructs a fixed radius circle at the given coordinates.
   *
   * The default radius is 6 pixels.
   *
   * @param _x
   * @param _y
   */
  public Circle(double _x, double _y) {
    x = _x;
    y = _y;
  }

  /**
   * Constructs a fixed radius circle at the given coordinates with the given radius.
   *
   * The radius is given in pixels.
   *
   * @param _x
   * @param _y
   * @param _r
   */
  public Circle(double _x, double _y, int _r) {
    x = _x;
    y = _y;
    pixRadius = _r;
  }

  /**
   * Draws the circle.
   *
   * @param panel
   * @param g
   */
  public void draw(DrawingPanel panel, Graphics g) {
    int xpix = panel.xToPix(x)-pixRadius;
    int ypix = panel.yToPix(y)-pixRadius;
    g.setColor(color);
    g.fillOval(xpix, ypix, 2*pixRadius, 2*pixRadius); // draw the circle onto the screen
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
   * @param x
   */
  public void setX(double x) {
    this.x = x;
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
   * @param y
   */
  public void setY(double y) {
    this.y = y;
  }

  /**
   * Sets the x and y coordinates.
   *
   * @param x
   * @param y
   */
  public void setXY(double x, double y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Returns a string representation of the circle.
   *
   * @return String
   */
  public String toString() {
    String name = getClass().getName();
    name = name.substring(1+name.lastIndexOf("."))+'['; //$NON-NLS-1$
    name += "x="+x;                                     //$NON-NLS-1$
    name += ",y="+y;                                    //$NON-NLS-1$
    name += ",r_pix="+pixRadius+']';                    //$NON-NLS-1$
    return name;
  }

  /**
   * Gets a loader that allows a Circle to be represented as XML data.
   * Objects without XML loaders cannot be saved and retrieved from an XML file.
   *
   * @return ObjectLoader
   */
  public static XML.ObjectLoader getLoader() {
    return new CircleLoader();
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
