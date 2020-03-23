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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import org.opensourcephysics.controls.XML;

/**
 * A class that draws shapes using the Java 2D API.
 */
public class DrawableShape implements Drawable {
  // no harm in letting a client access the color
  public Color color = new Color(255, 128, 128, 128); // transparent light red fill color
  public Color edgeColor = Color.RED;
  Shape shape;
  double x, y;                                        // the shape's coordinates
  double theta;
  public String shapeClass;

  /**
   * Constructs a DrawableShape with the given coordinates.
   *
   * @param shape
   * @param x coordinate
   * @param y coordinate
   */
  public DrawableShape(Shape shape, double x, double y) {
    this.x = x;
    this.y = y;
    shapeClass = shape.getClass().getName();
    this.shape = AffineTransform.getTranslateInstance(x, y).createTransformedShape(shape);
  }

  /**
   * Creates a drawable circle.
   * @param x
   * @param y
   * @param d the diameter
   * @return the DrawableShape
   */
  public static DrawableShape createCircle(double x, double y, double d) {
    return new DrawableShape(new Ellipse2D.Double(-d/2, -d/2, d, d), x, y);
  }

  /**
   * Creates a drawable rectangle.
   * @param x
   * @param y
   * @param w
   * @param h
   * @return the drawable rectangle
   */
  public static DrawableShape createRectangle(double x, double y, double w, double h) {
    return new DrawableShape(new Rectangle2D.Double(-w/2, -h/2, w, h), x, y);
  }

  /**
   * Sets the shape's drawing colors.
   *
   * The error bar color is set equal to the edge color.
   *
   * @param  fillColor
   * @param  edgeColor
   */
  public void setMarkerColor(Color fillColor, Color edgeColor) {
    this.color = fillColor;
    this.edgeColor = edgeColor;
  }

  /**
   * Sets the rotation angle in radians.
   *
   * @param theta the new angle
   */
  public void setTheta(double theta) {
    shape = AffineTransform.getRotateInstance(theta-this.theta, x, y).createTransformedShape(shape);
    this.theta = theta;
  }

  /**
   * Gets the value of the roation angle theta.
   * @return double
   */
  public double getTheta() {
    return theta;
  }

  /**
   * Transforms the shape using the given transformation.
   *
   * @param transformation AffineTransform
   */
  public void transform(AffineTransform transformation) {
    shape = transformation.createTransformedShape(shape);
  }

  /**
   * Transforms the shape using the given matrix.
   *
   * @param mat double[][]
   */
  public void tranform(double[][] mat) {
    shape = (new AffineTransform(mat[0][0], mat[1][0], mat[0][1], mat[1][1], mat[0][2], mat[1][2])).createTransformedShape(shape);
  }

  /**
   * Sets the x and y coordinates.
   *
   * @param _x
   * @param _y
   */
  public void setXY(double _x, double _y) {
    shape = AffineTransform.getTranslateInstance(_x-x, _y-y).createTransformedShape(shape);
    x = _x;
    y = _y;
  }

  /**
   * Sets the x coordinate.
   *
   * @param _x
   */
  public void setX(double _x) {
    shape = AffineTransform.getTranslateInstance(_x-x, 0).createTransformedShape(shape);
    x = _x;
  }

  /**
   * Gets the value of x.
   * @return double
   */
  public double getX() {
    return x;
  }

  /**
   * Sets the y coordinate.
   *
   * @param _y
   */
  public void setY(double _y) {
    shape = AffineTransform.getTranslateInstance(0, _y-y).createTransformedShape(shape);
    y = _y;
  }

  /**
   * Gets the value of y.
   * @return double
   */
  public double getY() {
    return y;
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
    name += ",y="+y+']';                                //$NON-NLS-1$
    return name;
  }

  /**
   * Draws the shape.
   *
   * @param panel DrawingPanel
   * @param g Graphics
   */
  public void draw(DrawingPanel panel, Graphics g) {
    Shape temp = panel.getPixelTransform().createTransformedShape(shape);
    Graphics2D g2 = ((Graphics2D) g);
    g2.setPaint(color);
    g2.fill(temp);
    g2.setPaint(edgeColor);
    g2.draw(temp);
    g2.setPaint(Color.BLACK);
  }

  /**
   * Gets the XML object loader for this class.
   * @return ObjectLoader
   */
  public static XML.ObjectLoader getLoader() {
    return new DrawableShapeLoader();
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
