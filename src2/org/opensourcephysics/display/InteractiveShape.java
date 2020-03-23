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
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;

/**
 * A shape that implements Interactive.
 * @author Wolfgang Christian
 * @version 1.0
 */
public class InteractiveShape extends AbstractInteractive implements Measurable {
  // fillColor uses color in the superclass
  public Color edgeColor = Color.red;   // the edge color
  protected Shape shape;
  protected String shapeClass;
  protected double theta;
  protected double width, height;       // an estimate of the shape's width and height
  protected double xoff, yoff;          // offset from center
  protected boolean pixelSized = false; // width and height are fixed and given in pixels
  AffineTransform toPixels = new AffineTransform();
  boolean enableMeasure = false;        // enables the measure so that this object affects a drawing panel's scale

  /**
   * Constructs an InteractiveShape with the given coordinates.
   *
   * @param s
   * @param _x coordinate
   * @param _y coordinate
   */
  public InteractiveShape(Shape s, double _x, double _y) {
    color = new Color(255, 128, 128, 128); // transparent light red fill color
    shape = s;
    x = _x;
    y = _y;
    if(shape==null) {
      return;
    }
    Rectangle2D bounds = shape.getBounds2D();
    width = bounds.getWidth();
    height = bounds.getHeight();
    shapeClass = shape.getClass().getName();
    shape = AffineTransform.getTranslateInstance(x, y).createTransformedShape(shape);
  }

  /**
   * Constructs an InteractiveShape at the origin.
   * @param s
   */
  public InteractiveShape(Shape s) {
    this(s, 0, 0);
  }

  /**
   * Creates an interactive ellipse.
   *
   * @param x
   * @param y
   * @param w
   * @param h
   * @return InteractiveShape
   */
  public static InteractiveShape createEllipse(double x, double y, double w, double h) {
    Shape shape = new Ellipse2D.Double(-w/2, -h/2, w, h);
    InteractiveShape is = new InteractiveShape(shape, x, y);
    is.width = w;
    is.height = h;
    return is;
  }

  /**
   * Creates an interactive circle.
   *
   * @param x
   * @param y
   * @param d the diameter
   * @return the interactive circle
   */
  public static InteractiveShape createCircle(double x, double y, double d) {
    return createEllipse(x, y, d, d);
  }

  /**
   * Creates an interactive rectangle.
   * @param x
   * @param y
   * @param w
   * @param h
   * @return the interactive rectangle
   */
  public static InteractiveShape createRectangle(double x, double y, double w, double h) {
    Shape shape = new Rectangle2D.Double(-w/2, -h/2, w, h);
    InteractiveShape is = new InteractiveShape(shape, x, y);
    is.width = w;
    is.height = h;
    return is;
  }

  /**
   * Creates an interactive triangle with a base parallel to the x axis.
   *
   * @param x
   * @param y
   * @param b  base
   * @param h  height
   * @return the interactive triangle
   */
  public static InteractiveShape createTriangle(double x, double y, double b, double h) {
    GeneralPath path = new GeneralPath();
    path.moveTo((float) (-b/2), (float) (-h/2));
    path.lineTo((float) (+b/2), (float) (-h/2));
    path.lineTo(0, (float) (h/2));
    path.closePath();
    Shape shape = path;
    InteractiveShape is = new InteractiveShape(shape, x, y);
    is.width = b;
    is.height = h;
    return is;
  }

  /**
   * Creates an interactive image.
   * @param x
   * @param y
   * @param image
   * @return the rectangle
   */
  public static InteractiveShape createImage(Image image, double x, double y) {
    InteractiveImage is = new InteractiveImage(image, x, y);
    return is;
  }

  /**
 * Creates an interactive image.
 * @param x
 * @param y
 * @param text
 * @return the rectangle
 */
  public static InteractiveShape createTextLine(double x, double y, String text) {
    InteractiveTextLine is = new InteractiveTextLine(text, x, y);
    return is;
  }

  /**
   * Creates an interactive arrow.
   * @param x
   * @param y
   * @param w base
   * @param h height
   * @return the arrow
   */
  public static InteractiveShape createArrow(double x, double y, double w, double h) {
    InteractiveArrow is = new InteractiveArrow(x, y, w, h);
    is.setHeightDrag(false);
    is.setWidthDrag(false);
    is.hideBounds = true;
    return is;
  }

  /**
   * Creates an interactive arrow.
   * @param x
   * @param y
   * @param w base
   * @param h height
   * @return the arrow
   */
  public static InteractiveShape createCenteredArrow(double x, double y, double w, double h) {
    InteractiveCenteredArrow is = new InteractiveCenteredArrow(x, y, w, h);
    is.setHeightDrag(false);
    is.setWidthDrag(false);
    is.hideBounds = true;
    return is;
  }

  /**
   * Creates an interactive square.
   * @param x
   * @param y
   * @param w
   * @return the interactive square
   */
  public static InteractiveShape createSquare(double x, double y, double w) {
    Shape shape = new Rectangle2D.Double(-w/2, -w/2, w, w);
    return new InteractiveShape(shape, x, y);
  }

  /**
   * Transforms the shape.
   *
   * @param transformation AffineTransform
   */
  public void transform(AffineTransform transformation) {
    shape = transformation.createTransformedShape(shape);
  }

  /**
   * Draws the shape.
   *
   * @param panel the drawing panel
   * @param g  the graphics context
   */
  public void draw(DrawingPanel panel, Graphics g) {
    Graphics2D g2 = ((Graphics2D) g);
    toPixels = panel.getPixelTransform();
    Shape temp;
    if(pixelSized) {
      Point2D pt = new Point2D.Double(x, y);
      pt = toPixels.transform(pt, pt);
      // translate the shape to correct pixel coordinates
      temp = new AffineTransform(1, 0, 0, -1, -x+pt.getX()+xoff, y+pt.getY()-yoff).createTransformedShape(shape);
      temp = AffineTransform.getRotateInstance(-theta, pt.getX(), pt.getY()).createTransformedShape(temp);
    } else {
      temp = toPixels.createTransformedShape(shape);
    }
    g2.setPaint(color);
    g2.fill(temp);
    g2.setPaint(edgeColor);
    g2.draw(temp);
  }

  /**
   * Tests if the specified coordinates are inside the boundary of the
   * <code>Shape</code>.
   * @param x
   * @param  y
   * @return <code>true</code> if the specified coordinates are inside
   *         the <code>Shape</code> boundary; <code>false</code>
   *         otherwise.
   */
  public boolean contains(double x, double y) {
    if(shape.contains(x, y)) {
      return true;
    }
    return false;
  }

  /**
   * Gets the Java shape that is being drawn.
   * @return the shape
   */
  public Shape getShape() {
    return shape;
  }

  /**
   * Transforms the shape using the given matrix.
   * @param mat double[][]
   */
  public void tranform(double[][] mat) {
    shape = (new AffineTransform(mat[0][0], mat[1][0], mat[0][1], mat[1][1], mat[0][2], mat[1][2])).createTransformedShape(shape);
  }

  /**
   * Determines if the shape is enabled and if the given pixel coordinates are within the shape.
   *
   * @param panel DrawingPanel
   * @param xpix int
   * @param ypix int
   * @return boolean
   */
  public boolean isInside(DrawingPanel panel, int xpix, int ypix) {
    if((shape==null)||!enabled) {
      return false;
    }
    if(shape.contains(panel.pixToX(xpix), panel.pixToY(ypix))) {
      return true;
    }
    return false;
  }

  /**
   * Sets the shape's drawing colors.
   *
   * The error bar color is set equal to the edge color.
   *
   * @param  _fillColor
   * @param  _edgeColor
   */
  public void setMarkerColor(Color _fillColor, Color _edgeColor) {
    color = _fillColor;
    edgeColor = _edgeColor;
  }

  /**
   * Sets the rotation angle in radians.
   *
   * @param theta the new angle
   */
  public void setTheta(double theta) {
    if(!pixelSized) {
      shape = AffineTransform.getRotateInstance(theta-this.theta, x, y).createTransformedShape(shape);
    }
    this.theta = theta;
  }

  /**
   * Sets the pixelSized flag.
   *
   * Pixel sized shapes use pixels for width and height.
   *
   * @param enable boolean
   */
  public void setPixelSized(boolean enable) {
    this.pixelSized = enable;
  }

  /**
   * Gets the width of this shape.
   *
   * @return double
   */
  public double getWidth() {
    return width;
  }

  /**
   * Sets the width of the shape to the given value.
   *
   * @param width double
   */
  public void setWidth(double width) {
    width = Math.abs(width);
    double w = width/this.width;
    if(w<0.02) {
      return;
    }
    if(pixelSized) {
      shape = AffineTransform.getTranslateInstance(-x, -y).createTransformedShape(shape);
      shape = AffineTransform.getScaleInstance(w, 1).createTransformedShape(shape);
      shape = AffineTransform.getTranslateInstance(x, y).createTransformedShape(shape);
    } else {
      shape = AffineTransform.getTranslateInstance(-x, -y).createTransformedShape(shape);
      shape = AffineTransform.getRotateInstance(-theta).createTransformedShape(shape);
      shape = AffineTransform.getScaleInstance(w, 1).createTransformedShape(shape);
      shape = AffineTransform.getRotateInstance(theta).createTransformedShape(shape);
      shape = AffineTransform.getTranslateInstance(x, y).createTransformedShape(shape);
    }
    xoff *= w;
    this.width = width;
  }

  /**
   * Gets the height of this shape.
   *
   * @return double
   */
  public double getHeight() {
    return height;
  }

  /**
   * Sets the height of the shape to the given value.
   * @param height double
   */
  public void setHeight(double height) {
    height = Math.abs(height);
    double h = height/this.height;
    if(h<0.02) {
      return;
    }
    if(pixelSized) {
      shape = AffineTransform.getTranslateInstance(-x, -y).createTransformedShape(shape);
      shape = AffineTransform.getScaleInstance(1, h).createTransformedShape(shape);
      shape = AffineTransform.getTranslateInstance(x, y).createTransformedShape(shape);
    } else {
      shape = AffineTransform.getTranslateInstance(-x, -y).createTransformedShape(shape);
      shape = AffineTransform.getRotateInstance(-theta).createTransformedShape(shape);
      shape = AffineTransform.getScaleInstance(1, h).createTransformedShape(shape);
      shape = AffineTransform.getRotateInstance(theta).createTransformedShape(shape);
      shape = AffineTransform.getTranslateInstance(x, y).createTransformedShape(shape);
    }
    yoff *= h;
    this.height = height;
  }

  /**
   * Sets the drawing offset;
   *
   * Fixed size shapes cannot be offset.
   *
   * @param xoffset double
   * @param yoffset double
   */
  public void setOffset(double xoffset, double yoffset) {
    if(!pixelSized) { // change the actual shape
      shape = AffineTransform.getTranslateInstance(x+xoffset, y+yoffset).createTransformedShape(shape);
    }
    xoff = xoffset;
    yoff = yoffset;
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
   * Sets the y coordinate.
   *
   * @param _y
   */
  public void setY(double _y) {
    shape = AffineTransform.getTranslateInstance(0, _y-y).createTransformedShape(shape);
    y = _y;
  }

  /**
   * Gets a description of this object.
   * @return String
   */
  public String toString() {
    return "InteractiveShape:"+"\n \t shape="+shapeClass+"\n \t x="+x+"\n \t y="+y+"\n \t width="+width+"\n \t height="+height+"\n \t theta="+theta; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
  }

  /**
   * Enables the measured flag so that this arrow effects the scale of a drawing panel.
   *
   * @return minimum
   */
  public void setMeasured(boolean _enableMeasure) {
    enableMeasure = _enableMeasure;
  }

  /**
   * Determines if this circle should effect the scale of a drawing panel.
   *
   * @return minimum
   */
  public boolean isMeasured() {
    return enableMeasure;
  }

  /**
   * Implements measurable by getting the x center of the circle.
   *
   * @return minimum
   */
  public double getXMin() {
    if(pixelSized) {
      return x-width/toPixels.getScaleX()/2;
    }
    return shape.getBounds2D().getX();
  }

  /**
   * Implements measurable by getting the x center of the circle.
   *
   * @return maximum
   */
  public double getXMax() {
    if(pixelSized) {
      return x+width/toPixels.getScaleX()/2;
    }
    return shape.getBounds2D().getX()+shape.getBounds2D().getWidth();
  }

  /**
   * Implements measurable by getting the y center of the circle.
   *
   * @return minimum
   */
  public double getYMin() {
    if(pixelSized) {
      return y-height/toPixels.getScaleY()/2;
    }
    return shape.getBounds2D().getY();
  }

  /**
   * Implements measurable by getting the y center of the circle.
   *
   * @return maximum
   */
  public double getYMax() {
    if(pixelSized) {
      return y+height/toPixels.getScaleY()/2;
    }
    return shape.getBounds2D().getY()+shape.getBounds2D().getHeight();
  }

  /**
   * Gets the XML object loader for this class.
   * @return ObjectLoader
   */
  public static XML.ObjectLoader getLoader() {
    return new InteractiveShapeLoader();
  }

  /**
  * A class to save and load InteractiveShape in an XMLControl.
  */
  protected static class InteractiveShapeLoader extends XMLLoader {
    public void saveObject(XMLControl control, Object obj) {
      InteractiveShape interactiveShape = (InteractiveShape) obj;
      control.setValue("geometry", interactiveShape.shapeClass);      //$NON-NLS-1$
      control.setValue("x", interactiveShape.x);                      //$NON-NLS-1$
      control.setValue("y", interactiveShape.y);                      //$NON-NLS-1$
      control.setValue("width", interactiveShape.width);              //$NON-NLS-1$
      control.setValue("height", interactiveShape.height);            //$NON-NLS-1$
      control.setValue("x offset", interactiveShape.xoff);            //$NON-NLS-1$
      control.setValue("y offset", interactiveShape.yoff);            //$NON-NLS-1$
      control.setValue("theta", interactiveShape.theta);              //$NON-NLS-1$
      control.setValue("pixel sized", interactiveShape.pixelSized);   //$NON-NLS-1$
      control.setValue("is enabled", interactiveShape.isEnabled());   //$NON-NLS-1$
      control.setValue("is measured", interactiveShape.isMeasured()); //$NON-NLS-1$
      control.setValue("color", interactiveShape.color);              //$NON-NLS-1$
      Shape shape = AffineTransform.getRotateInstance(-interactiveShape.theta, interactiveShape.x, interactiveShape.y).createTransformedShape(interactiveShape.shape);
      control.setValue("general path", shape); //$NON-NLS-1$
    }

    public Object createObject(XMLControl control) {
      return new InteractiveShape(new Rectangle2D.Double(0, 0, 0, 0)); // default shape is a rectangle for now
    }

    protected Shape getShape(String type, double x, double y, double w, double h) {
      if(type.equals(Ellipse2D.Double.class.getName())) {
        return new Ellipse2D.Double(x-w/2, y-h/2, w, h);
      } else if(type.equals(Rectangle2D.Double.class.getName())) {
        return new Rectangle2D.Double(x-w/2, y-h/2, w, h);
      } else {
        return null;
      }
    }

    public Object loadObject(XMLControl control, Object obj) {
      InteractiveShape interactiveShape = (InteractiveShape) obj;
      String type = control.getString("geometry");                                                 //$NON-NLS-1$
      double x = control.getDouble("x");                                                           //$NON-NLS-1$
      double y = control.getDouble("y");                                                           //$NON-NLS-1$
      double theta = control.getDouble("theta");                                                   //$NON-NLS-1$
      Shape shape = getShape(type, x, y, control.getDouble("width"), control.getDouble("height")); //$NON-NLS-1$ //$NON-NLS-2$
      if(shape==null) {                                                           // check for special geometry
        interactiveShape.shape = (GeneralPath) control.getObject("general path"); //$NON-NLS-1$
      } else {
        interactiveShape.shape = shape;
      }
      // the shape should already be scaled so just set the instatance fields
      interactiveShape.width = control.getDouble("width");   //$NON-NLS-1$
      interactiveShape.height = control.getDouble("height"); //$NON-NLS-1$
      interactiveShape.xoff = control.getDouble("x offset"); //$NON-NLS-1$
      interactiveShape.yoff = control.getDouble("y offset"); //$NON-NLS-1$
      interactiveShape.x = x;
      interactiveShape.y = y;
      interactiveShape.setPixelSized(control.getBoolean("pixel sized")); //$NON-NLS-1$
      interactiveShape.setEnabled(control.getBoolean("is enabled"));     //$NON-NLS-1$
      interactiveShape.setMeasured(control.getBoolean("is measured"));   //$NON-NLS-1$
      interactiveShape.color = (Color) control.getObject("color"); //$NON-NLS-1$
      interactiveShape.setTheta(theta); // orient the shape
      return obj;
    }

    static { // needs this loader
      XML.setLoader(java.awt.geom.GeneralPath.class, new GeneralPathLoader());
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
