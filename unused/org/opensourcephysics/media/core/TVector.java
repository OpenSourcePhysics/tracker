/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/*
 * The org.opensourcephysics.media.core package defines the Open Source Physics
 * media framework for working with video and other media.
 *
 * Copyright (c) 2017  Douglas Brown and Wolfgang Christian.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
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
 * For additional information and documentation on Open Source Physics,
 * please see <http://www.opensourcephysics.org/>.
 */
package org.opensourcephysics.media.core;
import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;

/**
 * This is a TShape that draws a vector.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class TVector extends TShape {
  // instance fields
  protected GeneralPath path = new GeneralPath();
  protected Line2D line = new Line2D.Double();
  protected Shape head;
  protected Shape shaft;
  protected int length = 16;
  protected int width = 4;
  protected AffineTransform rotation = new AffineTransform();
  protected TPoint tail = new LineEnd();
  protected TPoint tip = new LineEnd();
  protected Rectangle tipRect = new Rectangle(0, 0, 8, 8);
  protected boolean tipEnabled = true;

  /**
   * Constructs a default TVector with tail position (0, 0) and
   * components (0, 0).
   */
  public TVector() {
    setStroke(new BasicStroke(1));
  }

  /**
   * Constructs a TVector with specified tail and components.
   *
   * @param xt x position of tail
   * @param yt y position of tail
   * @param xc x component
   * @param yc y component
   */
  public TVector(double xt, double yt, double xc, double yc) {
    tail.setXY(xt, yt);
    tip.setXY(xt+xc, yt+yc);
    setStroke(new BasicStroke(1));
  }

  /**
   * Gets the tip.
   *
   * @return the tip
   */
  public TPoint getTip() {
    return tip;
  }

  /**
   * Gets the tail.
   *
   * @return the tail
   */
  public TPoint getTail() {
    return tail;
  }

  /**
   * Sets the x component.
   *
   * @param x the x component
   */
  public void setXComponent(double x) {
    tip.setX(tail.getX()+x);
  }

  /**
   * Sets the y component.
   *
   * @param y the y component
   */
  public void setYComponent(double y) {
    tip.setY(tail.getY()+y);
  }

  /**
   * Sets the x and y components.
   *
   * @param x the x component
   * @param y the y component
   */
  public void setXYComponents(double x, double y) {
    tip.setXY(tail.getX()+x, tail.getY()+y);
  }

  /**
   * Gets the x component.
   *
   * @return the x component
   */
  public double getXComponent() {
    return tip.getX()-tail.getX();
  }

  /**
   * Gets the y component.
   *
   * @return the y component
   */
  public double getYComponent() {
    return tip.getY()-tail.getY();
  }

  /**
   * Overrides TPoint setXY method to move both tip and tail.
   *
   * @param x the x position
   * @param y the y position
   */
  public void setXY(double x, double y) {
    double dx = x-getX();
    double dy = y-getY();
    tip.translate(dx, dy);
    tail.translate(dx, dy);
  }

  /**
   * Enables and disables the interactivity of the tip.
   *
   * @param enabled <code>true</code> to enable the tip
   */
  public void setTipEnabled(boolean enabled) {
    tipEnabled = enabled;
  }

  /**
   * Gets whether the tip is enabled.
   *
   * @return <code>true</code> if the tip is enabled
   */
  public boolean isTipEnabled() {
    return tipEnabled;
  }

  /**
   * Sets the length of the arrow tip.
   *
   * @param tipLength the tip length in pixels
   */
  public void setTipLength(int tipLength) {
    tipLength = Math.max(8, tipLength);
    width = tipLength/4;
    length = 4*width;
  }

  /**
   * Overrides TPoint setStroke method.
   *
   * @param stroke the desired stroke
   */
  public void setStroke(BasicStroke stroke) {
    if(stroke==null) {
      return;
    }
    this.stroke = new BasicStroke(stroke.getLineWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 8, stroke.getDashArray(), stroke.getDashPhase());
  }

  /**
   * Returns the interactive drawable object at the specified pixel
   * position.
   *
   * @param panel the drawing panel
   * @param xpix the x pixel position on the panel
   * @param ypix the y pixel position on the panel
   * @return the interactive drawable object
   */
  public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
    if(!(panel instanceof VideoPanel)) {
      return null;
    }
    if(!isEnabled()||!isVisible()) {
      return null;
    }
    setHitRectCenter(xpix, ypix);
    if((shaft!=null)&&shaft.intersects(hitRect)) {
      return this;
    }
    if(tipEnabled&&(head!=null)&&head.intersects(hitRect)) {
      return tip;
    }
    return null;
  }

  //________________________ protected methods ________________________

  /**
   * Gets the shape to be filled in the draw method.
   *
   * @param vidPanel the video panel
   * @return the line shape
   */
  protected Shape getShape(VideoPanel vidPanel) {
    this.center(tip, tail);
    Point p1 = tail.getScreenPosition(vidPanel); // tail
    Point p2 = tip.getScreenPosition(vidPanel);  // tip
    // set up transform
    double theta = Math.atan2(p2.y-p1.y, p2.x-p1.x);
    rotation.setToRotation(theta, p1.x, p1.y);
    rotation.translate(p1.x, p1.y);
    // get line length d
    float d = (float) p1.distance(p2);
    // set up head hit shape usng full line length
    path.reset();
    path.moveTo(d-4, 0);
    path.lineTo(d-6, -2);
    path.lineTo(d, 0);
    path.lineTo(d-6, 2);
    path.closePath();
    head = rotation.createTransformedShape(path);
    // shorten line length to account for stroke width
    // see Java 2D API Graphics, by VJ Hardy (Sun, 2000) page 147
    float w = stroke.getLineWidth();
    d = d-1.58f*w;
    // set up shaft hit shape using shortened line length
    line.setLine(0, 0, d-length, 0);
    shaft = rotation.createTransformedShape(line);
    // set up and return fill shape usng shortened line length
    path.reset();
    path.moveTo(0, 0);
    path.lineTo(d-length+width, 0);
    path.lineTo(d-length, -width);
    path.lineTo(d, 0);
    path.lineTo(d-length, width);
    path.lineTo(d-length+width, 0);
    Shape vector = rotation.createTransformedShape(path);
    return stroke.createStrokedShape(vector);
  }

  //_________________________ inner End classes _________________________
  class LineEnd extends TPoint {
    /**
     * Overrides TPoint getBounds method
     *
     * @param vidPanel the video panel
     * @return the bounding rectangle
     */
    public Rectangle getBounds(VideoPanel vidPanel) {
      return TVector.this.getBounds(vidPanel);
    }

    /**
      * Overrides TPoint getFrameNumber method
      *
      * @param vidPanel the video panel
      * @return the frame number
      */
    public int getFrameNumber(VideoPanel vidPanel) {
      return TVector.this.getFrameNumber(vidPanel);
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
