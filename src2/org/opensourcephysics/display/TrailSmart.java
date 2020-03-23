/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Graphics2D;
import java.awt.Shape;

/**
 * TrailSmart adds points to a Bezier trail only if the new point deviates from a straight line.
 * The smart trail algorithm minimizes the number of points drawn.
 *
 * @author       Wolfgang Christian
 * @version 1.0
 */
public class TrailSmart extends TrailBezier {
  double x1, y1, x2, y2; // shadows superclass fields
  double dx1, dy1, ds1;
  double max_error = 0.001;

  /**
   * Sets the maximum error.
   * @param max double
   */
  public void setMaxError(double max) {
    max_error = max;
  }

  /**
   * Adds a point to the trail.
   * @param x double
   * @param y double
   */
  public synchronized void addPoint(double x, double y) {
    double dx2 = x-endPts[2];
    double dy2 = y-endPts[3];
    double ds2 = Math.sqrt(dy2*dy2+dx2*dx2);
    if((numpts>1)&&(ds2==0)) {
      return; // do not add the same point
    }
    double xx = endPts[2]+ds2*dx1/ds1;
    double yy = endPts[3]+ds2*dy1/ds1;
    double err = Math.sqrt((x-xx)*(x-xx)+(y-yy)*(y-yy));
    double cos = (dx1*dx2+dy1*dy2)/ds1/ds2;
    if((numpts<3)||(err>max_error)||(cos<0.99)||Double.isNaN(cos)) {
      super.addPoint(x, y);
      ds1 = ds2;
      dx1 = dx2;
      dy1 = dy2;
    }
    x1 = x2;
    y1 = y2;
    x2 = x;
    y2 = y;
  }

  /**
* Draws the points that have not yet been added to the Bezier spline.
* @param panel DrawingPanel
* @param g2 Graphics2D
*/
  protected void drawPathEnd(DrawingPanel panel, Graphics2D g2) {
    pathEnd.reset();
    path.moveTo(endPts[0], endPts[1]); // start the path at the last point
    path.lineTo(endPts[2], endPts[3]);
    path.lineTo((float) x1, (float) y1);
    path.lineTo((float) x2, (float) y2);
    Shape s = pathEnd.createTransformedShape(panel.getPixelTransform());
    g2.draw(s);
  }

  /**
   * Gets the minimum x value in the trail.
   * @return double
   */
  public double getXMin() {
    return Math.min(x2, xmin);
  }

  /**
   * Gets the maximum x value in the trail.
   * @return double
   */
  public double getXMax() {
    return Math.max(x2, xmax);
  }

  /**
   * Gets the minimum y value in the trail.
   * @return double
   */
  public double getYMin() {
    return Math.min(y2, ymin);
  }

  /**
   * Gets the maximum y value in the trail.
   * @return double
   */
  public double getYMax() {
    return Math.max(y2, ymax);
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
