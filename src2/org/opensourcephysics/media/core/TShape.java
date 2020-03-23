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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;

/**
 * This is the base class for all TPoint objects that draw a Shape.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class TShape extends TPoint {
  // static fields
  protected static Rectangle hitRect = new Rectangle(0, 0, 8, 8);
  // instance fields
  protected Color color = Color.black;
  protected boolean visible = true;
  protected Shape fillShape = hitRect;
  protected BasicStroke stroke = new BasicStroke();

  /**
   * Constructs a TPoint object with coordinates (0, 0).
   */
  public TShape() {
    super();
  }

  /**
   * Constructs a TPoint object with specified image coordinates.
   *
   * @param x the x coordinate
   * @param y the y coordinate
   */
  public TShape(double x, double y) {
    super(x, y);
  }

  /**
   * Constructs a TPoint object with image coordinates specified by
   * a Point2D (commonly another TPoint).
   *
   * @param point the Point2D
   */
  public TShape(Point2D point) {
    super(point);
  }

  /**
   * Sets the color of the shape.
   *
   * @param color the desired color
   */
  public void setColor(Color color) {
    this.color = color;
  }

  /**
   * Gets the color of the shape.
   *
   * @return the color
   */
  public Color getColor() {
    return color;
  }

  /**
   * Sets the stroke.
   *
   * @param stroke the desired stroke
   */
  public void setStroke(BasicStroke stroke) {
    this.stroke = stroke;
  }

  /**
   * Gets the stroke.
   *
   * @return the color
   */
  public BasicStroke getStroke() {
    return stroke;
  }

  /**
   * Gets the screen bounds of the shape.
   *
   * @param vidPanel the video panel
   * @return the bounding rectangle
   */
  public Rectangle getBounds(VideoPanel vidPanel) {
    return getShape(vidPanel).getBounds();
  }

  /**
   * Sets the visible state.
   *
   * @param visible <code>true</code> to make this visible.
   */
  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  /**
   * Gets the current visible state.
   *
   * @return <code>true</code> if this is visible.
   */
  public boolean isVisible() {
    return visible;
  }

  /**
   * Overrides TPoint draw method.
   *
   * @param panel the drawing panel requesting the drawing
   * @param _g the graphics context on which to draw
   */
  public void draw(DrawingPanel panel, Graphics _g) {
    if(!(panel instanceof VideoPanel)||!isVisible()) {
      return;
    }
    VideoPanel vidPanel = (VideoPanel) panel;
    fillShape = getShape(vidPanel);
    Graphics2D g = (Graphics2D) _g;
    Paint gpaint = g.getPaint();
    g.setPaint(color);
    g.fill(fillShape);
    g.setPaint(gpaint);
  }

  /**
   * Overrides TPoint findInteractive method.
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
    VideoPanel vidPanel = (VideoPanel) panel;
    if(!isEnabled()||!isVisible()) {
      return null;
    }
    setHitRectCenter(xpix, ypix);
    if(hitRect.contains(getScreenPosition(vidPanel))) {
      return this;
    }
    return null;
  }

  /**
   * Returns a String describing this TPoint.
   *
   * @return a descriptive string
   */
  public String toString() {
    return "TShape ["+x+", "+y+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  /**
   * Gets the shape to be filled in the draw method.
   *
   * @param vidPanel the video panel
   * @return the fill shape
   */
  protected Shape getShape(VideoPanel vidPanel) {
    Point p = getScreenPosition(vidPanel);
    setHitRectCenter(p.x, p.y);
    if(stroke==null) {
      return(Rectangle) hitRect.clone();
    }
    return stroke.createStrokedShape(hitRect);
  }

  /**
   * Centers the hit testing rectangle on the specified screen point.
   *
   * @param xpix the x pixel position
   * @param ypix the y pixel position
   */
  protected void setHitRectCenter(int xpix, int ypix) {
    hitRect.setLocation(xpix-hitRect.width/2, ypix-hitRect.height/2);
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
