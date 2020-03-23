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
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;

/**
 * TrailBezier defines a trail of points connected by a Bezier spline.  This object is often used to
 * show the path of a moving object.
 *
 * @author       Wolfgang Christian
 * @version 1.0
 */
public class TrailBezier extends AbstractTrail {
  GeneralPath path = new GeneralPath();
  GeneralPath pathStart = new GeneralPath();
  GeneralPath pathEnd = new GeneralPath();
  float x0, y0, x1, y1, x2, y2;
  float[] endPts = new float[4];
  float dxEstimate = 0, dyEstimate = 0;
  float slack = (float) 0.15;
  float dx2, dy2;

  /**
   * Adds a point to the trail.
   * @param x double
   * @param y double
   */
  public synchronized void addPoint(double x, double y) {
    if(closed) {
      throw new IllegalStateException("Cannot add points to a closed trail."); //$NON-NLS-1$
    }
    xmin = Math.min(xmin, x);
    xmax = Math.max(xmax, x);
    ymin = Math.min(ymin, y);
    ymax = Math.max(ymax, y);
    if(numpts==0) {
      pathStart.moveTo((float) x, (float) y);
      endPts[0] = x0 = (float) x;
      endPts[1] = y0 = (float) y;
    } else if(numpts==1) {
      endPts[2] = x1 = (float) x;
      endPts[3] = y1 = (float) y;
      path.moveTo(x1, y1);
    } else if(numpts==2) {
      x2 = (float) x;
      y2 = (float) y;
      dx2 = x2-endPts[0];
      dy2 = y2-endPts[1];
      float dx1 = -2*x2-4*endPts[0]+6*endPts[2];
      float dy1 = -2*y2-4*endPts[1]+6*endPts[3];
      pathStart.curveTo(endPts[0]+slack*dx1, endPts[1]+slack*dy1, endPts[2]-slack*dx2, endPts[3]-slack*dy2, endPts[2], endPts[3]);
      endPts[0] = endPts[2];
      endPts[1] = endPts[3];
      endPts[2] = x2;
      endPts[3] = y2;
    } else {
      float dx1 = dx2, dy1 = dy2;
      float fx = (float) x;
      float fy = (float) y;
      dx2 = fx-endPts[0];
      dy2 = fy-endPts[1];
      path.curveTo(endPts[0]+slack*dx1, endPts[1]+slack*dy1, endPts[2]-slack*dx2, endPts[3]-slack*dy2, endPts[2], endPts[3]);
      dxEstimate = 2*endPts[0]+4*fx-6*endPts[2];
      dyEstimate = 2*endPts[1]+4*fy-6*endPts[3];
      endPts[0] = endPts[2];
      endPts[1] = endPts[3];
      endPts[2] = fx;
      endPts[3] = fy;
    }
    numpts++;
  }

  /**
   * Sets the slack which determines the position of the control points.
   * @param slack double
   */
  public void setSlack(double slack) {
    this.slack = (float) slack;
  }

  /**
   * Closes the trail by connecting the first point to the last point.
   */
  public void closeTrail() {
    addPoint(x0, y0);
    addPoint(x1, y1);
    addPoint(x2, y2);
    closed = true;
    pathStart.reset();
    pathEnd.reset();
    path.closePath();
  }

  /**
   * Clears all points from the trail.
   */
  public synchronized void clear() {
    numpts = 0;
    xmin = Double.MAX_VALUE;
    xmax = -Double.MAX_VALUE;
    ymin = Double.MAX_VALUE;
    ymax = -Double.MAX_VALUE;
    path.reset();
    pathStart.reset();
    pathEnd.reset();
    closed = false;
  }

  /**
   * Draw the trail on the panel.
   * @param g
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if(numpts==0) {
      return;
    }
    Graphics2D g2 = (Graphics2D) g;
    g2.setColor(color);
    // transform paths from world to pixel coordinates
    Shape s = path.createTransformedShape(panel.getPixelTransform());
    if(drawingStroke!=null) {
      Stroke stroke = g2.getStroke();
      g2.setStroke(drawingStroke);
      g2.draw(s);
      g2.setStroke(stroke);
    } else {
      g2.draw(s);
    }
    if(closed) {
      return;
    }
    s = pathStart.createTransformedShape(panel.getPixelTransform());
    g2.draw(s);
    if(numpts>2) {
      drawPathEnd(panel, g2);
    }
  }

  /**
   * Draws the points that have not yet been added to the spline.
   * @param panel DrawingPanel
   * @param g2 Graphics2D
   */
  protected void drawPathEnd(DrawingPanel panel, Graphics2D g2) {
    pathEnd.reset();
    path.moveTo(endPts[0], endPts[1]); // start the path at the last point
    path.curveTo(endPts[0]+slack*dx2, endPts[1]+slack*dy2, endPts[2]-slack*dxEstimate, endPts[3]-slack*dyEstimate, endPts[2], endPts[3]);
    Shape s = pathEnd.createTransformedShape(panel.getPixelTransform());
    g2.draw(s);
  }

  /**
   * Returns the XML.ObjectLoader for this class.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
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
   * A class to save and load Dataset data in an XMLControl.
   */
  private static class Loader extends XMLLoader {
    public void saveObject(XMLControl control, Object obj) {
      TrailBezier trail = (TrailBezier) obj;
      control.setValue("closed", trail.closed);        //$NON-NLS-1$
      control.setValue("color", trail.color);          //$NON-NLS-1$
      control.setValue("number of pts", trail.numpts); //$NON-NLS-1$
      //control.setValue("general path", trail.generalPath);
    }

    public Object createObject(XMLControl control) {
      return new TrailBezier();
    }

    public Object loadObject(XMLControl control, Object obj) {
      TrailBezier trail = (TrailBezier) obj;
      trail.closed = control.getBoolean("closed");      //$NON-NLS-1$
      trail.color = (Color) control.getObject("color"); //$NON-NLS-1$
      trail.numpts = control.getInt("number of pts");   //$NON-NLS-1$
      //trail.generalPath = (GeneralPath) control.getObject("general path");
      return obj;
    }

  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
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
