/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2015  Douglas Brown
 *
 * Tracker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tracker; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at <http://www.gnu.org/copyleft/gpl.html>
 *
 * For additional Tracker information and documentation, please see
 * <http://www.cabrillo.edu/~dbrown/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.*;
import java.awt.image.*;

import org.opensourcephysics.media.core.TPoint;

/**
 * This is a region determined by inside and outside pixel values.
 *
 * @author Douglas Brown
 */
public class TRegion extends Polygon {

  // instance fields
  private float brightLimit = 0.5f;
  private int width, height; // image dimensions
  private int w=100, h=100; // scan area dimensions
  private int[] pixels; // image pixels as integers
  private float[] hsb = new float[3];
  private int x0, y0;

  /**
   * Constructs a region starting from a specified image pixel position.
   * 
   * @param image the image
   * @param x0 the starting x position
   * @param y0 the starting y position
   *  
   */
  public TRegion(BufferedImage image, int x0, int y0) {
    width = image.getWidth();
    height = image.getHeight();
    pixels = new int[width * height];
    image.getRaster().getDataElements(0, 0, width, height, pixels);
    this.x0 = x0;
    this.y0 = y0;
    findEdge();
  }
  
  /**
   * Gets the center point of the region.
   * @return the center
   */
  public TPoint getCenter() {
  	if (npoints == 0) return null;
  	double x = this.getBounds2D().getCenterX();
  	double y = this.getBounds2D().getCenterY();
  	return new TPoint(x, y);
  }
  
  /**
   * Finds and traces the edge that defines the inside/outside of the region.
   */
  public void findEdge() {
    // find edge starting point
    boolean foundEdge = false, foundInside = false, moveUp = false;
    int x = x0;
    int y = y0;
    int rightLimit = Math.min(width, x0+w/2);
    int leftLimit = Math.max(0, x0-w/2);
    int topLimit = Math.max(0, y0-h/2);
    int bottomLimit = Math.min(height, y0+h/2);
    // scanning left to right, move up/down line by line until an inside pixel is found
    int n = 1;
    while (!foundInside) {
  		if (y <= topLimit || y >= bottomLimit) break; // stop scanning
    	if (isInside(x, y)) {
    		foundInside = true;
    	}
      if (!foundInside) {
      	if (x < rightLimit) x++; // continue scanning line
      	else {
      		if (moveUp) {
      			y = y0-n; // move up to next line
      			moveUp = false;
      		}
      		else {
      			y = y0+n; // move down to next line
      			moveUp = true;
      			n++;
      		}
      		x = leftLimit;
      	}
      }
    }
    // move left to find an outside pixel, then move back inside by one pixel
    while (foundInside && x >= leftLimit) {
      x--;
      if (!isInside(x, y)) {
        x++;
        foundEdge = true;
        break;
      }
    }
    // trace the edge 
    if (foundEdge) {
    	reset();
    	traceEdge(x, y, 'U');
    }
  }

  private boolean isInside(int x, int y) {
    // get the specified pixel rgb data
    int pixel = pixels[y * w + x];
    int r = (pixel >> 16) & 0xff; // red
    int g = (pixel >> 8) & 0xff; // green
    int b = (pixel) & 0xff; // blue
    // convert to hsb
    Color.RGBtoHSB(r, g, b, hsb);
    if (hsb[2] >= brightLimit)
      return true;
    return false;
  }

  /**
   * Traces the edge of a region starting from the point (x, y).
   * This defines the points (not pixels) that define the edge using the following 
   * 16-entry lookup table, and sets the vertices of the polygon to those points.
	 *
   *  Index        1234*   Code    Result
   *     0         0000     X     Should never happen
   *     1         000X     R     Go Right
   *     2         00X0     D     Go Down
   *     3         00XX     R     Go Right
   *     4         0X00     U     Go Up
   *     5         0X0X     U     Go Up
   *     6         0XX0     u     Go up or down depending on current direction
   *     7         0XXX     U     Go up
   *     8         X000     L     Go left
   *     9         X00X     l     Go left or right depending on current direction
   *    10         X0X0     D     Go down
   *    11         X0XX     R     Go right
   *    12         XX00     L     Go left
   *    13         XX0X     L     Go left
   *    14         XXX0     D     Go down
   *    15         XXXX     X     Should never happen
	 *
   *  *1 = upper-left, 2 = upper-right, 3 = lower-left, 4 = lower-right pixel,
   *  *X = pixel is outside, 0 = pixel is inside
   *  
   *  To understand this code, the distinction between points and pixels must be clear.
   *  A point lies at the intersection of the boundaries of a block of four pixels.
   *  Below is a magnified view of a block of four pixels (each pixel is filled with
   *  characters) and the point (the + sign) that lies between them:*
	 *
	 *   ********   ********
	 *   ********   ********
	 *   ********   ********
	 *   ********   ********
	 *            +
	 *   ********   ********
	 *   ********   ********
	 *   ********   ********
	 *   ********   ********
	 *
   * For every point there is an upper-left, upper-right, lower-left, and lower-right
   * diagonally adjacent pixel.
   * 
   * @param x the x-component of the point
   * @param y the y-component of the point
   * @param startingDirection the direction to move looking for an outside pixel
   *  
   */
  private void traceEdge(int x, int y, char startingDirection) {
    char[] table = new char[] {
        'X', 'R', 'D', 'R', 'U', 'U', 'u', 'U',
        'L', 'l', 'D', 'R', 'L', 'L', 'D', 'X'};
    char direction = startingDirection;
    int hloc = x;
    int vloc = y;
    boolean UL = isInside(hloc - 1, vloc - 1);
    boolean UR = isInside(hloc, vloc - 1);
    boolean LL = isInside(hloc - 1, vloc);
    boolean LR = isInside(hloc, vloc);
    this.addPoint(hloc, vloc); // starting edge point
    do {
      int index = 0;
      if (LR)
        index |= 1;
      if (LL)
        index |= 2;
      if (UR)
        index |= 4;
      if (UL)
        index |= 8;
      char newDirection = table[index];
      if (newDirection == 'u') {
        if (direction == 'R')
          newDirection = 'U';
        else
          newDirection = 'D';
      }
      if (newDirection == 'l') {
        if (direction == 'U')
          newDirection = 'L';
        else
          newDirection = 'R';
      }
      switch (newDirection) {
        case 'U':
          vloc--;
          LL = UL;
          LR = UR;
          UL = isInside(hloc - 1, vloc - 1);
          UR = isInside(hloc, vloc - 1);
          break;
        case 'D':
          vloc++;
          UL = LL;
          UR = LR;
          LL = isInside(hloc - 1, vloc);
          LR = isInside(hloc, vloc);
          break;
        case 'L':
          hloc--;
          UR = UL;
          LR = LL;
          UL = isInside(hloc - 1, vloc - 1);
          LL = isInside(hloc - 1, vloc);
          break;
        case 'R':
          hloc++;
          UL = UR;
          LL = LR;
          UR = isInside(hloc, vloc - 1);
          LR = isInside(hloc, vloc);
          break;
      }
      this.addPoint(hloc, vloc);
      direction = newDirection;
    }
    while (direction != 'X' 
    	&&!(hloc == x && vloc == y && direction == startingDirection));
  }

}
