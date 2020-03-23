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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;

/**
 * An image is bounded by hot spots for dragging and resizing.
 *
 * @author W. Christian
 * @version 1.0
 */
public class BoundedImage extends BoundedShape implements ImageObserver {
  protected Image image;

  /**
   * Constructs a bounded image centered at the given location.
   *
   * @param x double
   * @param y double
   * @param image Image
   */
  public BoundedImage(Image image, double x, double y) {
    super(null, x, y);
    this.image = image;
    width = image.getWidth(this);
    width = Math.max(0, width);   // -1 if image is not available
    height = image.getHeight(this);
    height = Math.max(0, height); // -1 if image is not available
    shapeClass = image.getClass().getName();
    setPixelSized(true);
  }

  /**
   * Draws the image.
   *
   * @param panel  the world in which the arrow is viewed
   * @param g  the graphics context upon which to draw
   */
  public void draw(DrawingPanel panel, Graphics g) {
    toPixels = panel.getPixelTransform();
    Point2D pt = new Point2D.Double(x, y);
    pt = toPixels.transform(pt, pt);
    Graphics2D g2 = (Graphics2D) g;
    g2.translate(pt.getX(), pt.getY());
    AffineTransform trans = new AffineTransform();
    trans.translate(-width/2, -height/2);
    trans.rotate(-theta, width/2, height/2);
    trans.scale(width/image.getWidth(null), height/image.getHeight(null));
    g2.drawImage(image, trans, null);
    g2.translate(-pt.getX(), -pt.getY());
    drawFixedBounds(panel, g);
  }

  /**
   * Draws the bounds around the image.
   *
   * @param panel the drawing panel
   * @param g  the graphics context
   */
  private void drawFixedBounds(DrawingPanel panel, Graphics g) {
    Point2D pt = new Point2D.Double(x, y);
    pt = toPixels.transform(pt, pt);
    Shape temp = new Rectangle2D.Double(pt.getX()-width/2, pt.getY()-height/2, width, height);
    computeFixedHotSpots(temp.getBounds2D());
    pixelBounds = temp.getBounds2D();
    if(theta!=0) {
      pixelBounds = AffineTransform.getRotateInstance(-theta, pt.getX(), pt.getY()).createTransformedShape(pixelBounds);
    }
    if(!selected) {
      return;
    }
    Graphics2D g2 = ((Graphics2D) g);
    g2.setPaint(boundsColor);
    g2.draw(pixelBounds);
    if(xyDrag) {
      g2.fillRect((int) hotSpots[CENTER].getX()-delta, (int) hotSpots[CENTER].getY()-delta, d2, d2);
      g2.setColor(edgeColor);
      g2.fillOval((int) hotSpots[CENTER].getX()-1, (int) hotSpots[CENTER].getY()-1, 3, 3);
      g2.setPaint(boundsColor);
    }
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
    g.setColor(Color.BLACK);
  }

  public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
    if((infoflags&ImageObserver.WIDTH)==1) {
      this.width = width;
    }
    if((infoflags&ImageObserver.HEIGHT)==1) {
      this.height = height;
    }
    if((infoflags&ImageObserver.ALLBITS)==1) {
      return false; // further updates not needed
    }
    return true;
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
