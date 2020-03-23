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
import java.awt.Color;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;

/**
 * This is a Trackable circle that extends TShape.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class TCircle extends TShape {
  // instance fields
  protected Ellipse2D circle = new Ellipse2D.Double();
  protected int radius = 5;
  protected int n;

  /**
   * Constructs a TCircle with image coordinates (0, 0).
   *
   * @param n the video frame number
   */
  public TCircle(int n) {
    this(n, 0, 0);
  }

  /**
   * Constructs a TCircle with specified image coordinates.
   *
   * @param n the video frame number
   * @param x the x coordinate
   * @param y the y coordinate
   */
  public TCircle(int n, double x, double y) {
    super(x, y);
    this.n = n;
    setColor(Color.red);
  }

  /**
   * Sets the radius of this circle.
   *
   * @param radius the radius in screen pixels
   */
  public void setRadius(int radius) {
    this.radius = radius;
  }

  /**
   * Gets the radius of this circle.
   *
   * @return the radius in screen pixels
   */
  public int getRadius() {
    return radius;
  }

  /**
   * Overrides TPoint getFrameNumber method.
   *
   * @param vidPanel the video panel drawing this circle
   * @return the frame number
   */
  public int getFrameNumber(VideoPanel vidPanel) {
    return n;
  }

  /**
   * Returns this if it is enabled and visible and the specified
   * pixel position falls within the bounds of this circle.
   *
   * @param panel the drawing panel
   * @param xpix the x pixel position on the panel
   * @param ypix the y pixel position on the panel
   * @return this if enabled and hit, otherwise null
   */
  public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
    if(!(panel instanceof VideoPanel)) {
      return null;
    }
    VideoPanel vidPanel = (VideoPanel) panel;
    if(!isEnabled()||!isVisible()) {
      return null;
    }
    if(getBounds(vidPanel).contains(xpix, ypix)) {
      return this;
    }
    return null;
  }

  //________________________ protected methods ________________________

  /**
   * Gets the circle shape to be filled in the draw method.
   *
   * @param vidPanel the video panel
   * @return the circle shape
   */
  protected Shape getShape(VideoPanel vidPanel) {
    Point p = getScreenPosition(vidPanel);
    double xpix = p.x-radius;
    double ypix = p.y-radius;
    if(stroke==null) {
      circle.setFrame(xpix, ypix, 2*radius, 2*radius);
      return circle;
    }
    double w = stroke.getLineWidth();
    circle.setFrame(xpix+w/2, ypix+w/2, 2*radius-w, 2*radius-w);
    return stroke.createStrokedShape(circle);
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
