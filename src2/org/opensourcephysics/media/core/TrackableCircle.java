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
import java.awt.Graphics;
import org.opensourcephysics.display.Circle;
import org.opensourcephysics.display.DrawingPanel;

/**
 * This is a circle that implements the Trackable interface and
 * is associated with a single video frame. The draw method
 * illustrates the process of transforming imagespace to
 * worldspace coordinates.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class TrackableCircle extends Circle implements Trackable {
  // instance fields
  protected int n;

  /**
   * Constructs a TrackableCircle.
   *
   * @param n the video frame number
   * @param imageX the x position in imagespace
   * @param imageY the y position in imagespace
   */
  public TrackableCircle(int n, double imageX, double imageY) {
    super(imageX, imageY);
    this.n = n;
    color = Color.green;
  }

  /**
   * Gets the frame number.
   *
   * @return the frame number
   */
  public int getFrameNumber() {
    return n;
  }

  /**
   * Overrides the Circle draw method.
   *
   * @param panel the drawing panel requesting the drawing
   * @param g the graphics context on which to draw
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if(!(panel instanceof VideoPanel)) {
      return;
    }
    VideoPanel vidPanel = (VideoPanel) panel; // only draws on video panels
    double x = getX();                        // image position
    double y = getY();                        // image position
    if(!vidPanel.isDrawingInImageSpace()) { // convert to worldspace
      x = vidPanel.getCoords().imageToWorldX(n, getX(), getY());
      y = vidPanel.getCoords().imageToWorldY(n, getX(), getY());
    }
    // standard drawing panel drawing from here on
    int xpix = panel.xToPix(x)-pixRadius;
    int ypix = panel.yToPix(y)-pixRadius;
    g.setColor(color);
    g.fillOval(xpix, ypix, 2*pixRadius, 2*pixRadius);
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
