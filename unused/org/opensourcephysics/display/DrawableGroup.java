/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Draws a group of shapes.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public class DrawableGroup implements Drawable {
  protected double x = 0, y = 0, theta = 0;
  protected ArrayList<Drawable> drawableList = new ArrayList<Drawable>(); // list of Drawable objects

  /**
   * Adds a drawable object to the drawable list.
   * @param drawable
   */
  public void addDrawable(Drawable drawable) {
    if((drawable!=null)&&!drawableList.contains(drawable)) {
      drawableList.add(drawable);
    }
  }

  /**
   * Draws the shapes in the drawable list.
   *
   * @param panel the drawing panel
   * @param g  the graphics context
   */
  public void draw(DrawingPanel panel, Graphics g) {
    int xpix = panel.xToPix(0);
    int ypix = panel.yToPix(0);
    Graphics2D g2 = (Graphics2D) g;
    Iterator<Drawable> it = drawableList.iterator();
    AffineTransform oldAT = g2.getTransform();
    AffineTransform at = g2.getTransform();
    at.concatenate(AffineTransform.getRotateInstance(-theta, xpix, ypix));
    double xt = x*panel.getXPixPerUnit()*Math.cos(theta)+y*panel.getYPixPerUnit()*Math.sin(theta);
    double yt = x*panel.getXPixPerUnit()*Math.sin(theta)-y*panel.getYPixPerUnit()*Math.cos(theta);
    at.concatenate(AffineTransform.getTranslateInstance(xt, yt));
    g2.setTransform(at);
    while(it.hasNext()) {
      Drawable drawable = it.next();
      drawable.draw(panel, g2);
    }
    g2.setTransform(oldAT);
  }

  /**
 * Sets the x and y coordinates.
 *
 * @param _x double
 * @param _y double
 */
  public void setXY(double _x, double _y) {
    x = _x;
    y = _y;
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
   * Gets the x location.
   * @return x
   */
  public double getX() {
    return x;
  }

  /**
   * Gets the y location.
   * @return y
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

  /**
   * Gets the rotation angle in radians.
   * @return theta
   */
  public double getTheta() {
    return theta;
  }

  /**
   * Sets the rotation angle in radians.
   *
   * @param _theta
   */
  public void setTheta(double _theta) {
    theta = _theta;
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
