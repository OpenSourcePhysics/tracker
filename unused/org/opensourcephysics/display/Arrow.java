/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import org.opensourcephysics.controls.XML;

/**
 * A Drawable arrow that uses Java 2D drawing.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public class Arrow implements Drawable {
  protected float headSize = 8;        // size of the arrow head
  protected Color color = Color.black; // default drawing color
  protected double x = 0, y = 0;       // location of arrow
  protected double a = 0, b = 0;       // horizontal and vertical components

  /**
   * Constructs an Arrow with the given postion and components.
   *
   * @param _x  postion
   * @param _y  position
   * @param _a  horizontal component
   * @param _b  vertical component
   */
  public Arrow(double _x, double _y, double _a, double _b) {
    x = _x;
    y = _y;
    a = _a;
    b = _b;
  }

  /**
   * Gets the x coordinate.
   *
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
    setXY(x, y);
  }

  /**
   * Gets the y coordinate.
   *
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
    setXY(x, y);
  }

  /**
   * Sets the x and y coordinates.
   *
   * @param y
   */
  public void setXY(double x, double y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Sets the arrow's color.
   * @param c
   */
  public void setColor(Color c) {
    color = c;
  }

  /**
   * Sets the arrow's horizontal component.
   * @param dx
   */
  public void setXlength(double dx) {
    a = dx;
  }

  /**
   * Sets the arrows vertical component.
   * @param dy
   */
  public void setYlength(double dy) {
    b = dy;
  }

  /**
   * Gets the horizontal component.
   * @return horizontal
   */
  public double getXlength() {
    return a;
  }

  /**
   * Gets the vertical component.
   * @return vertical
   */
  public double getYlength() {
    return b;
  }

  /**
   * Gets the headsize for the arrow.
   * @return float
   */
  public float getHeadSize() {
    return headSize;
  }

  /**
   * Sets the headsize for the arrow.
   *
   * @param size float the head size in pixels.
   */
  public void setHeadSize(float size) {
    headSize = size;
  }

  /**
   * Draws the arrow.
   *
   * @param panel  the drawing panel in which the arrow is viewed
   * @param g  the graphics context upon which to draw
   */
  public void draw(DrawingPanel panel, Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    AffineTransform toPixels = panel.getPixelTransform();
    g2.setPaint(color);
    // draw the shaft
    g2.draw(toPixels.createTransformedShape(new Line2D.Double(x, y, x+a, y+b)));
    Point2D pt = new Point2D.Double(x+a, y+b);
    pt = toPixels.transform(pt, pt);
    double aspect = panel.isSquareAspect() ? 1 : -toPixels.getScaleX()/toPixels.getScaleY();
    Shape head = getHead(Math.atan2(b, aspect*a));
    Shape temp = AffineTransform.getTranslateInstance(pt.getX(), pt.getY()).createTransformedShape(head);
    // draw the head
    g2.fill(temp);
    g2.setPaint(Color.BLACK);
  }

  /**
   * Gets the arrowhead shape.
   * @param theta double the angle of the arrow
   * @return Shape
   */
  protected Shape getHead(double theta) {
    GeneralPath path = new GeneralPath();
    path.moveTo(1, 0);
    path.lineTo(-headSize, -headSize/2);
    path.lineTo(-headSize, +headSize/2);
    path.closePath();
    AffineTransform rot = AffineTransform.getRotateInstance(-theta);
    Shape head = rot.createTransformedShape(path);
    return head;
  }

  /**
   * Gets a loader that allows a Circle to be represented as XML data.
   * Objects without XML loaders cannot be saved and retrieved from an XML file.
   *
   * @return ObjectLoader
   */
  public static XML.ObjectLoader getLoader() {
    return new ArrowLoader();
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
