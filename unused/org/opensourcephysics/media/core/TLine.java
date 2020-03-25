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
import java.awt.geom.Line2D;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;

/**
 * This is a TShape that draws a line.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class TLine extends TShape {
  // instance fields
  protected Line2D line = new Line2D.Double();
  protected TPoint end1 = new LineEnd();
  protected TPoint end2 = new LineEnd();
  protected Rectangle end1Rect = new Rectangle(0, 0, 8, 8);
  protected Rectangle end2Rect = new Rectangle(0, 0, 8, 8);

  /**
   * Constructs a default TLine with end points at (0, 0).
   */
  public TLine() {

  /** empty block */
  }

  /**
   * Constructs a TLine with specified end points.
   *
   * @param x1 x position of end 1
   * @param y1 y position of end 1
   * @param x2 x position of end 2
   * @param y2 y position of end 2
   */
  public TLine(double x1, double y1, double x2, double y2) {
    end1.setXY(x1, y1);
    end2.setXY(x2, y2);
  }

  /**
   * Gets end 1.
   *
   * @return end 1
   */
  public TPoint getEnd1() {
    return end1;
  }

  /**
   * Gets end 2.
   *
   * @return end 2
   */
  public TPoint getEnd2() {
    return end2;
  }

  /**
   * Overrides TShape setStroke method.
   *
   * @param stroke the desired stroke
   */
  public void setStroke(BasicStroke stroke) {
    if(stroke!=null) {
      this.stroke = stroke;
    }
  }

  /**
   * Sets the x and y positions in imagespace.
   *
   * @param x the x position
   * @param y the y position
   */
  public void setXY(double x, double y) {
    double dx = x-getX();
    double dy = y-getY();
    end1.translate(dx, dy);
    end2.translate(dx, dy);
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
    if(end1Rect.contains(xpix, ypix)) {
      return end1;
    }
    if(end2Rect.contains(xpix, ypix)) {
      return end2;
    }
    setHitRectCenter(xpix, ypix);
    if(line.intersects(hitRect)) {
      return this;
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
    this.center(end1, end2);
    Point p1 = end1.getScreenPosition(vidPanel);
    Point p2 = end2.getScreenPosition(vidPanel);
    line.setLine(p1, p2);
    end1Rect.setLocation(p1.x-4, p1.y-4);
    end2Rect.setLocation(p2.x-4, p2.y-4);
    return stroke.createStrokedShape(line);
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
      return TLine.this.getBounds(vidPanel);
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
