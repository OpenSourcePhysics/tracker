/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

/**
 * AbstractTrail defines a trail of pixels on the screen.  This object is often used to
 * show the path of a moving object.
 *
 * @author       Wolfgang Christian
 * @version 1.0
 */
public abstract class AbstractTrail implements Drawable, Measurable {
  public Color color = Color.black; // changing the color is harmless so this can be public
  protected boolean enableMeasure = false;
  protected double xmin = Double.MAX_VALUE, xmax = -Double.MAX_VALUE, ymin = Double.MAX_VALUE, ymax = -Double.MAX_VALUE;
  protected double xmaxLogscale = -Double.MAX_VALUE;
  // the maximum x value in the trail when using a log scale

  protected double ymaxLogscale = -Double.MAX_VALUE;
  // the maximum y value in the trail when using a log scale

  protected double xminLogscale = Double.MAX_VALUE;
  // the minimum x value in the trail when using a log scale

  protected double yminLogscale = Double.MAX_VALUE;
  // the minimum y value in the trail when using a log scale  

  protected int numpts = 0;         // the number of points in the trail
  protected boolean closed = false;
  protected Stroke drawingStroke;

  /**
   * Adds a point to the trail.
   * @param x double
   * @param y double
   */
  public abstract void addPoint(double x, double y);

  /**
   * Closes the path by connecting the first point to the last point.
   * Points cannot be added to a closed path;
   */
  public abstract void closeTrail();

  /**
   * Clears all points from the trail.
   */
  public abstract void clear();

  /**
   * Sets the drawing stroke.
   * @param stroke Stroke
   */
  public void setStroke(Stroke stroke) {
    drawingStroke = stroke;
  }

  /**
   * Sets the the dash line stroke.
   *
   * @param int dashPoint
   * @param int dashLength
   */
  public void setDashedStroke(int dashPoint, int dashLength) {
    if(dashLength==0) {
      drawingStroke = new BasicStroke(dashPoint, 0, 1, 0, null, 0);                     //regular line
    } else {
      drawingStroke = new BasicStroke(dashPoint, 0, 1, 0, new float[] {dashLength}, 0); //dashes
    }
  }

  /**
   * Gets the drawing stroke.
   * @return Stroke
   */
  public Stroke getStroke() {
    return drawingStroke;
  }

  /**
   * Gets the number of points stored in the trail.
   * @return int
   */
  public int getNumberOfPoints() {
    return numpts;
  }

  /**
   * Enables the measured flag so that this circle effects the scale of a drawing panel.
   *
   * @return minimum
   */
  public void setMeasured(boolean _enableMeasure) {
    enableMeasure = _enableMeasure;
  }

  /**
   * Determines if this trail scales the drawing panel.
   *
   * @return boolean
   */
  public boolean isMeasured() {
    return enableMeasure&&(this.numpts>0);
  }

  /**
   * Gets the minimum x value in the trail.
   * @return double
   */
  public double getXMin() {
    return xmin;
  }

  /**
   * Gets the maximum x value in the trail.
   * @return double
   */
  public double getXMax() {
    return xmax;
  }

  /**
   * Gets the minimum y value in the trail.
   * @return double
   */
  public double getYMin() {
    return ymin;
  }

  /**
   * Gets the maximum y value in the trail.
   * @return double
   */
  public double getYMax() {
    return ymax;
  }

  /**
   * Gets the minimum x needed to draw this object on a log scale.
   * @return minimum
   */
  public double getXMinLogscale() {
    return xminLogscale;
  }

  /**
   * Gets the maximum x needed to draw this object on a log scale.
   * @return maximum
   */
  public double getXMaxLogscale() {
    return xmaxLogscale;
  }

  /**
   * Gets the minimum y needed to draw this object on a log scale.
   * @return minimum
   */
  public double getYMinLogscale() {
    return yminLogscale;
  }

  /**
   * Gets the maximum y needed to draw this object on a log scale on a log scale.
   * @return maximum
   */
  public double getYMaxLogscale() {
    return ymaxLogscale;
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
