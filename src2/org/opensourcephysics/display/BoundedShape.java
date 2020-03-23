/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Color;
import java.awt.Cursor;
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

public class BoundedShape extends InteractiveShape implements Selectable {
  static int CENTER = 0;
  static int BOTTOM = 1;
  static int LEFT = 2;
  static int TOP = 3;
  static int RIGHT = 4;
  static int CORNER = 5;
  static int NONE = 6;
  int hotspot = NONE;
  int delta = 3;
  int deltaSqr = delta*delta;
  int d2 = 2*delta+1;
  boolean selected = false;
  boolean hideBounds = false;
  Color boundsColor = new Color(128, 128, 255);
  boolean widthDrag = false;
  boolean heightDrag = false;
  boolean xyDrag = true;
  boolean rotateDrag = false;
  Shape pixelBounds = new Rectangle2D.Double(0, 0, 0, 0); // bounding rectangle in pixel coordinates.
  Point2D[] hotSpots = new Point2D[6];
  XYDelegate xyDelegate = new XYDelegate();

  /**
   * Constructs a BoundedShape object for the given shape.
   *
   * @param s Shape
   * @param x double
   * @param y double
   */
  public BoundedShape(Shape s, double x, double y) {
    super(s, x, y);
    for(int i = 0, n = hotSpots.length; i<n; i++) {
      hotSpots[i] = new Point2D.Float(0, 0);
    }
  }

  /**
   * Creates a bounded rectangle.
   * @param x
   * @param y
   * @param w
   * @param h
   * @return the interactive rectangle
   */
  public static BoundedShape createBoundedRectangle(double x, double y, double w, double h) {
    Shape shape = new Rectangle2D.Double(-w/2, -h/2, w, h);
    return new BoundedShape(shape, x, y);
  }

  /**
   * Creates a bounded rectangle.
   * @param x
   * @param y
   * @param b base
   * @param h height
   * @return the rectangle
   */
  public static BoundedShape createBoundedTriangle(double x, double y, double b, double h) {
    GeneralPath path = new GeneralPath();
    path.moveTo((float) (-b/2), (float) (-h/2));
    path.lineTo((float) (+b/2), (float) (-h/2));
    path.lineTo(0, (float) (h/2));
    path.closePath();
    Shape shape = path;
    return new BoundedShape(shape, x, y);
  }

  /**
   * Creates a bounded arrow.
   * @param x
   * @param y
   * @param w base
   * @param h height
   * @return the arrow
   */
  public static BoundedShape createBoundedArrow(double x, double y, double w, double h) {
    InteractiveArrow ia = new InteractiveArrow(x, y, w, h);
    ia.hideBounds = false;
    return ia;
  }

  /**
   * Creates a bounded arrow.
   * @param x
   * @param y
   * @param w base
   * @param h height
   * @return the arrow
   */
  public static BoundedShape createBoundedCenteredArrow(double x, double y, double w, double h) {
    InteractiveCenteredArrow ica = new InteractiveCenteredArrow(x, y, w, h);
    ica.hideBounds = false;
    return ica;
  }

  /**
   * Creates a bounded image.
   * @param x
   * @param y
   * @param image
   * @return the rectangle
   */
  public static BoundedShape createBoundedImage(Image image, double x, double y) {
    return new BoundedImage(image, x, y);
  }

  /**
   * Creates a bounded ellipse.
   *
   * @param x
   * @param y
   * @param w
   * @param h
   * @return BoundedShape
   */
  public static BoundedShape createBoundedEllipse(double x, double y, double w, double h) {
    Shape shape = new Ellipse2D.Double(-w/2, -h/2, w, h);
    return new BoundedShape(shape, x, y);
  }

  /**
   * Creates a bounded circle.
   *
   * @param x
   * @param y
   * @param d the diameter
   * @return the circle
   */
  public static BoundedShape createBoundedCircle(double x, double y, double d) {
    Shape shape = new Ellipse2D.Double(-d/2, -d/2, d, d);
    return new BoundedShape(shape, x, y);
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public boolean isSelected() {
    return selected;
  }

  /**
   * Sets the xy drag option.
   *
   * @param enable boolean
   */
  public void setXYDrag(boolean enable) {
    xyDrag = enable;
  }

  /**
   * Gets the xy drag boolean.
   *
   * @return boolean true if center can be dragged
   */
  public boolean isXYDrag() {
    return xyDrag;
  }

  /**
   * Sets the rotate drag option.
   *
   * @param enable boolean
   */
  public void setRotateDrag(boolean enable) {
    rotateDrag = enable;
  }

  /**
   * Gets the rotate drag option.
   * @return boolean
   */
  public boolean isRotateDrag() {
    return rotateDrag;
  }

  /**
   * Sets the width drag option.
   * @param enable boolean
   */
  public void setWidthDrag(boolean enable) {
    widthDrag = enable;
  }

  /**
   * Gets the width width drag option.
   *
   * @return boolean true if center can be dragged
   */
  public boolean isWidthDrag() {
    return widthDrag;
  }

  /**
   * Sets the height drag option.
   * @param enable boolean
   */
  public void setHeightDrag(boolean enable) {
    heightDrag = enable;
  }

  /**
   * Gets the height drag option.
   *
   * @return boolean true if center can be dragged
   */
  public boolean isHeightDrag() {
    return heightDrag;
  }

  public java.awt.Cursor getPreferredCursor() {
    if(xyDrag&&(hotspot==CENTER)) {
      return java.awt.Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    } else if(rotateDrag&&(hotspot==CORNER)) { // need better cursors!
      return java.awt.Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    } else if(widthDrag&&(hotspot==LEFT)) {
      return(theta==0) ? java.awt.Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR) : java.awt.Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    } else if(widthDrag&&(hotspot==RIGHT)) {
      return(theta==0) ? java.awt.Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR) : java.awt.Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    } else if(heightDrag&&(hotspot==TOP)) {
      return(theta==0) ? java.awt.Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR) : java.awt.Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    } else if(heightDrag&&(hotspot==BOTTOM)) {
      return(theta==0) ? java.awt.Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR) : java.awt.Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    } else if(selected) {
      return java.awt.Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    } else {
      return java.awt.Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    }
  }

  public void toggleSelected() {
    selected = !selected;
  }

  public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
    if(isInside(panel, xpix, ypix)) {
      return xyDelegate;
    }
    return null;
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
    hotspot = NONE;
    if(!enabled) {
      return false;
    }
    if(pixelBounds.contains(xpix, ypix)&&!selected) {
      return true;
    }
    if(selected) {
      hotspot = getHotSpotIndex(xpix, ypix, hotSpots);
      return true;
    }
    return false;
  }

  int getHotSpotIndex(int xpix, int ypix, Point2D[] hotSpots) {
    for(int i = 0, n = hotSpots.length; i<n; i++) {
      double dx = xpix-hotSpots[i].getX();
      double dy = ypix-hotSpots[i].getY();
      if(dx*dx+dy*dy<=deltaSqr) {
        return i;
      }
    }
    return NONE;
  }

  void computeScaledHotSpots(Rectangle2D rect, double ar) {
    double sin = Math.sin(theta);
    double cos = Math.cos(theta);
    double centerX = rect.getCenterX()-xoff*toPixels.getScaleX();
    double centerY = rect.getCenterY()-yoff*toPixels.getScaleY();
    double right = rect.getWidth()/2+xoff*toPixels.getScaleX();
    double left = rect.getWidth()/2-xoff*toPixels.getScaleX();
    double bottom = rect.getHeight()/2+yoff*toPixels.getScaleY();
    double top = rect.getHeight()/2-yoff*toPixels.getScaleY();
    hotSpots[CENTER].setLocation(centerX, centerY);                                                                                      // center
    hotSpots[BOTTOM].setLocation(centerX+xoff*toPixels.getScaleX()*cos+bottom*sin/ar, centerY-xoff*toPixels.getScaleX()*sin+bottom*cos); // bottom
    hotSpots[LEFT].setLocation(centerX-left*cos+yoff*toPixels.getScaleY()*sin, centerY+ar*left*sin+yoff*toPixels.getScaleY()*cos); // left
    hotSpots[TOP].setLocation(centerX+xoff*toPixels.getScaleX()*cos-top*sin/ar, centerY-xoff*toPixels.getScaleX()*sin-top*cos); // top
    hotSpots[RIGHT].setLocation(centerX+right*cos+yoff*toPixels.getScaleY()*sin, centerY-ar*right*sin+yoff*toPixels.getScaleY()*cos); // right
    hotSpots[CORNER].setLocation(centerX+right*cos-top*sin/ar, centerY-right*sin*ar-top*cos); // corner
  }

  void computeFixedHotSpots(Rectangle2D rect) {
    double sin = Math.sin(theta);
    double cos = Math.cos(theta);
    double cx = rect.getCenterX()-xoff; // center x
    double cy = rect.getCenterY()+yoff; // center y
    double right = rect.getWidth()/2+xoff;
    double left = xoff-rect.getWidth()/2;
    double bottom = yoff-rect.getHeight()/2;
    double top = rect.getHeight()/2+yoff;
    hotSpots[0].setLocation(cx, cy);                                         // center
    hotSpots[1].setLocation(cx+xoff*cos-bottom*sin, cy-bottom*cos-xoff*sin); // bottom
    hotSpots[2].setLocation(cx+left*cos-yoff*sin, cy-left*sin-yoff*cos);     // left
    hotSpots[3].setLocation(cx+xoff*cos-top*sin, cy-top*cos-xoff*sin);       // top
    hotSpots[4].setLocation(cx+right*cos-yoff*sin, cy-right*sin-yoff*cos);   // right
    hotSpots[5].setLocation(cx+right*cos-top*sin, cy-right*sin-top*cos);     // corner
  }

  /**
   * Sets the x and y coordinates using hotspots.
   *
   * @param y
   */
  void setHotSpotXY(double x, double y) {
    if(hideBounds) {
      setXY(x, y);
      return;
    }
    if(xyDrag&&selected&&(hotspot==CENTER)) {
      setXY(x, y);
    } else if(rotateDrag&&selected&&(hotspot==CORNER)) {
      if(pixelSized) {
        double r = -toPixels.getScaleY()/toPixels.getScaleX();
        double dx = x-this.x;
        double dy = y-this.y;
        theta = Math.atan2(r*dy, dx)-Math.atan2(height/2+yoff, (width/2+xoff));
      } else {
        double dx = x-this.x;
        double dy = y-this.y;
        double theta1 = Math.atan2(height/2+yoff, width/2+xoff);
        double theta2 = Math.atan2(dy, dx);
        setTheta(theta2-theta1);
      }
    } else if(widthDrag&&selected&&((hotspot==LEFT)||(hotspot==RIGHT))) {
      if(pixelSized) {
        double dx = toPixels.getScaleX()*(x-this.x)-xoff;
        double dy = toPixels.getScaleY()*(y-this.y)+yoff;
        BoundedShape.this.setWidth(2*Math.sqrt(dx*dx+dy*dy));
      } else {
        double dx = (x-this.x-xoff);
        double dy = (y-this.y-yoff);
        setWidth(2*Math.sqrt(dx*dx+dy*dy));
      }
    } else if(heightDrag&&selected&&((hotspot==TOP)||(hotspot==BOTTOM))) {
      if(pixelSized) {
        double dx = toPixels.getScaleX()*(x-this.x)-xoff;
        double dy = toPixels.getScaleY()*(y-this.y)+yoff;
        BoundedShape.this.setHeight(2*Math.sqrt(dx*dx+dy*dy));
      } else {
        double dx = (x-this.x-xoff);
        double dy = (y-this.y-yoff);
        setHeight(2*Math.sqrt(dx*dx+dy*dy));
      }
    }
  }

  /**
   * Draws the shape.
   *
   * @param panel the drawing panel
   * @param g  the graphics context
   */
  public void draw(DrawingPanel panel, Graphics g) {
    super.draw(panel, g);
    if(pixelSized) {
      drawFixedBounds(panel, g);
    } else {
      drawScaledBounds(panel, g);
    }
  }

  /**
   * Draws the shape.
   *
   * @param panel the drawing panel
   * @param g  the graphics context
   */
  private void drawScaledBounds(DrawingPanel panel, Graphics g) {
    double r = -toPixels.getScaleY()/toPixels.getScaleX();
    if(theta==0) {
      Shape temp = toPixels.createTransformedShape(shape.getBounds2D());
      computeScaledHotSpots(temp.getBounds2D(), r);
      pixelBounds = temp.getBounds2D();
    } else {
      // rotate the shape into standard position to get correct x-y bounds
      Shape temp = AffineTransform.getRotateInstance(-theta, x, y).createTransformedShape(shape);
      // the following alternate should also give the correct bounds in world coordinates
      // Shape temp = new Rectangle2D.Double(x-width/2, y-height/2, width, height);
      temp = toPixels.createTransformedShape(temp);
      computeScaledHotSpots(temp.getBounds2D(), r);
      pixelBounds = temp.getBounds2D();
      if(panel.isSquareAspect()) {
        pixelBounds = AffineTransform.getRotateInstance(-theta, ((Rectangle2D) pixelBounds).getCenterX()-xoff*toPixels.getScaleX(), ((Rectangle2D) pixelBounds).getCenterY()-yoff*toPixels.getScaleY()).createTransformedShape(pixelBounds);
      } else {
        double px = ((Rectangle2D) pixelBounds).getCenterX()-xoff*toPixels.getScaleX();
        double py = ((Rectangle2D) pixelBounds).getCenterY()-yoff*toPixels.getScaleY();
        pixelBounds = AffineTransform.getTranslateInstance(-px, -py).createTransformedShape(pixelBounds);
        pixelBounds = AffineTransform.getScaleInstance(1, 1.0/r).createTransformedShape(pixelBounds);
        pixelBounds = AffineTransform.getRotateInstance(-theta).createTransformedShape(pixelBounds);
        pixelBounds = AffineTransform.getScaleInstance(1, r).createTransformedShape(pixelBounds);
        pixelBounds = AffineTransform.getTranslateInstance(px, py).createTransformedShape(pixelBounds);
      }
    }
    if(!selected||hideBounds) {
      return;
    }
    Graphics2D g2 = ((Graphics2D) g);
    g2.setPaint(boundsColor);
    g2.draw(pixelBounds);
    if(rotateDrag) {
      g2.fillOval((int) hotSpots[CORNER].getX()-delta, (int) hotSpots[CORNER].getY()-delta, d2, d2);
    }
    if(heightDrag) {
      g2.fillRect((int) hotSpots[TOP].getX()-delta, (int) hotSpots[TOP].getY()-delta, d2, d2);
      g2.fillRect((int) hotSpots[BOTTOM].getX()-delta, (int) hotSpots[BOTTOM].getY()-delta, d2, d2);
    }
    if(widthDrag) {
      g2.fillRect((int) hotSpots[LEFT].getX()-delta, (int) hotSpots[LEFT].getY()-delta, d2, d2);
      g2.fillRect((int) hotSpots[RIGHT].getX()-delta, (int) hotSpots[RIGHT].getY()-delta, d2, d2);
    }
    if(xyDrag) {
      g2.fillRect((int) hotSpots[CENTER].getX()-delta, (int) hotSpots[CENTER].getY()-delta, d2, d2);
      g2.setColor(edgeColor);
      g2.fillOval((int) hotSpots[CENTER].getX()-1, (int) hotSpots[CENTER].getY()-1, 3, 3);
      g2.setPaint(boundsColor);
    }
    g.setColor(Color.BLACK);
  }

  /**
   * Draws the shape.
   *
   * @param panel the drawing panel
   * @param g  the graphics context
   */
  private void drawFixedBounds(DrawingPanel panel, Graphics g) {
    if(theta==0) {
      Point2D pt = new Point2D.Double(x, y);
      pt = toPixels.transform(pt, pt);
      Shape temp = AffineTransform.getTranslateInstance(-x+pt.getX()+xoff, -y+pt.getY()-yoff).createTransformedShape(shape.getBounds2D());
      computeFixedHotSpots(temp.getBounds2D());
      pixelBounds = temp.getBounds2D();
    } else {
      // rotate the shape into standard position to get correct x-y bounds
      // Shape temp = AffineTransform.getRotateInstance(-theta, x, y).createTransformedShape(shape);
      Point2D pt = new Point2D.Double(x, y);
      pt = toPixels.transform(pt, pt);
      Shape temp = AffineTransform.getTranslateInstance(-x+pt.getX()+xoff, -y+pt.getY()-yoff).createTransformedShape(shape);
      // temp = AffineTransform.getTranslateInstance(pt.getX(), pt.getY()).createTransformedShape(temp);
      computeFixedHotSpots(temp.getBounds2D());
      pixelBounds = temp.getBounds2D();
      pixelBounds = AffineTransform.getRotateInstance(-theta, pt.getX(), pt.getY()).createTransformedShape(pixelBounds);
    }
    if(!selected||hideBounds) {
      return;
    }
    Graphics2D g2 = ((Graphics2D) g);
    g2.setPaint(boundsColor);
    g2.draw(pixelBounds);
    if(rotateDrag) {
      g2.fillOval((int) hotSpots[CORNER].getX()-delta, (int) hotSpots[CORNER].getY()-delta, d2, d2);
    }
    if(heightDrag) {
      g2.fillRect((int) hotSpots[TOP].getX()-delta, (int) hotSpots[TOP].getY()-delta, d2, d2);
      g2.fillRect((int) hotSpots[BOTTOM].getX()-delta, (int) hotSpots[BOTTOM].getY()-delta, d2, d2);
    }
    if(widthDrag) {
      g2.fillRect((int) hotSpots[LEFT].getX()-delta, (int) hotSpots[LEFT].getY()-delta, d2, d2);
      g2.fillRect((int) hotSpots[RIGHT].getX()-delta, (int) hotSpots[RIGHT].getY()-delta, d2, d2);
    }
    if(xyDrag) {
      g2.fillRect((int) hotSpots[CENTER].getX()-delta, (int) hotSpots[CENTER].getY()-delta, d2, d2);
      g2.setColor(edgeColor);
      g2.fillOval((int) hotSpots[CENTER].getX()-1, (int) hotSpots[CENTER].getY()-1, 3, 3);
      g2.setPaint(boundsColor);
    }
    g.setColor(Color.BLACK);
  }

  /**
   * Gets a description of this object.
   * @return String
   */
  public String toString() {
    return "BoundedShape:"+"\n \t shape="+shapeClass+"\n \t x="+x+"\n \t y="+y+"\n \t width="+width+"\n \t height="+height+"\n \t theta="+theta; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
  }

  /**
   * Gets the XML object loader for this class.
   * @return ObjectLoader
   */
  public static XML.ObjectLoader getLoader() {
    return new BoundedShapeLoader();
  }

  /**
   *  A class to save and load BoundedShape in an XMLControl.
   */
  protected static class BoundedShapeLoader extends InteractiveShapeLoader {
    public void saveObject(XMLControl control, Object obj) {
      super.saveObject(control, obj);
      BoundedShape boundedShape = (BoundedShape) obj;
      control.setValue("xy drag", boundedShape.isXYDrag());         //$NON-NLS-1$
      control.setValue("width drag", boundedShape.isWidthDrag());   //$NON-NLS-1$
      control.setValue("height drag", boundedShape.isHeightDrag()); //$NON-NLS-1$
      control.setValue("rotate drag", boundedShape.isRotateDrag()); //$NON-NLS-1$
    }

    public Object createObject(XMLControl control) {
      return new BoundedShape(new Rectangle2D.Double(0, 0, 0, 0), 0, 0); // default shape is a rectangle for now
    }

    public Object loadObject(XMLControl control, Object obj) {
      BoundedShape boundedShape = (BoundedShape) obj;
      boundedShape.setXYDrag(control.getBoolean("xy drag"));         //$NON-NLS-1$
      boundedShape.setWidthDrag(control.getBoolean("width drag"));   //$NON-NLS-1$
      boundedShape.setHeightDrag(control.getBoolean("height drag")); //$NON-NLS-1$
      boundedShape.setRotateDrag(control.getBoolean("rotate drag")); //$NON-NLS-1$
      super.loadObject(control, obj);
      return boundedShape;
    }

  }

  class XYDelegate extends AbstractInteractive implements Selectable {
    public void draw(DrawingPanel panel, Graphics g) {}

    public boolean isInside(DrawingPanel panel, int xpix, int ypix) {
      return BoundedShape.this.isInside(panel, xpix, ypix);
    }

    public void setXY(double x, double y) {
      BoundedShape.this.setHotSpotXY(x, y);
    }

    public void setSelected(boolean selectable) {
      BoundedShape.this.setSelected(selectable);
    }

    public void toggleSelected() {
      BoundedShape.this.toggleSelected();
    }

    public boolean isSelected() {
      return BoundedShape.this.isSelected();
    }

    public Cursor getPreferredCursor() {
      return BoundedShape.this.getPreferredCursor();
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
